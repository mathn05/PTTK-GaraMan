package garaman.dao;

import garaman.entity.AppointmentSlot6;
import garaman.exception.DataAccessException6;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SlotDAO6 extends DAO6 {
    public SlotDAO6() {
        super();
    }

    public SlotDAO6(Connection con) {
        super(con);
    }

    public AppointmentSlot6[] getAvailableSlots(LocalDate appointmentDate) {
        if (appointmentDate == null) {
            throw new IllegalArgumentException("Ngày hẹn không hợp lệ.");
        }
        String sql = """
                SELECT s.*
                FROM tblAppointmentSlot6 s
                WHERE startTime >= ? AND startTime < ?
                    AND startTime > NOW(6)
                    AND (status = 'AVAILABLE' OR (status = 'HELD' AND holdExpiresAt <= NOW(6)))
                    AND NOT EXISTS (
                        SELECT 1 FROM tblAppointment6 a WHERE a.activeSlotId = s.id
                    )
                ORDER BY startTime, id
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            Timestamp dayStart = new Timestamp(date(appointmentDate).getTime());
            Timestamp nextDayStart = new Timestamp(date(appointmentDate.plusDays(1)).getTime());
            statement.setTimestamp(1, dayStart);
            statement.setTimestamp(2, nextDayStart);
            try (ResultSet resultSet = statement.executeQuery()) {
                ArrayList<AppointmentSlot6> slots = new ArrayList<>();
                while (resultSet.next()) {
                    AppointmentSlot6 slot = readSlot(resultSet);
                    slots.add(slot);
                }
                return slots.toArray(AppointmentSlot6[]::new);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được khung giờ", e);
        }
    }

    public AppointmentSlot6 findById(Integer slotId) {
        if (slotId == null || slotId <= 0) {
            return null;
        }
        String sql = """
                SELECT * FROM tblAppointmentSlot6
                WHERE id = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, slotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return readSlot(resultSet);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được khung giờ", e);
        }
    }

    public AppointmentSlot6 holdSlot(Integer slotId) {
        if (slotId == null || slotId <= 0) {
            return null;
        }
        String holdCode = UUID.randomUUID().toString();
        // UPDATE có điều kiện quyết định người giữ chỗ; không mở transaction chờ khách.
        String sql = """
                UPDATE tblAppointmentSlot6 s
                SET status = 'HELD', holdCode = ?,
                    holdExpiresAt = DATE_ADD(NOW(6), INTERVAL 3 MINUTE)
                WHERE id = ? AND startTime > NOW(6)
                    AND (status = 'AVAILABLE' OR (status = 'HELD' AND holdExpiresAt <= NOW(6)))
                    AND NOT EXISTS (
                        SELECT 1 FROM tblAppointment6 a WHERE a.activeSlotId = s.id
                    )
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, holdCode);
            statement.setInt(2, slotId);
            if (statement.executeUpdate() != 1) {
                return null;
            }

            // Đọc đúng token vừa tạo, kể cả khi kết nối khác đang thao tác cùng slot.
            String readSql = """
                    SELECT * FROM tblAppointmentSlot6
                    WHERE id = ? AND holdCode = ?
                    """;
            try (PreparedStatement read = con.prepareStatement(readSql)) {
                read.setInt(1, slotId);
                read.setString(2, holdCode);
                try (ResultSet resultSet = read.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    return readSlot(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không giữ được khung giờ", e);
        }
    }

    public boolean validateHold(Integer slotId, String holdCode) {
        if (slotId == null || slotId <= 0 || holdCode == null) {
            return false;
        }
        String sql = """
                SELECT id FROM tblAppointmentSlot6
                WHERE id = ? AND status = 'HELD' AND holdCode = ?
                    AND holdExpiresAt > NOW(6) AND startTime > NOW(6)
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, slotId);
            statement.setString(2, holdCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không kiểm tra được giữ chỗ", e);
        }
    }

    public long remainingHoldMillis(Integer slotId, String holdCode) {
        if (slotId == null || slotId <= 0 || holdCode == null) {
            return 0L;
        }
        String sql = """
                SELECT GREATEST(0, TIMESTAMPDIFF(MICROSECOND, NOW(6), holdExpiresAt) DIV 1000)
                FROM tblAppointmentSlot6
                WHERE id = ? AND status = 'HELD' AND holdCode = ? AND startTime > NOW(6)
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, slotId);
            statement.setString(2, holdCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0L;
                }
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không đọc được thời hạn giữ chỗ", e);
        }
    }

    public boolean releaseSlot(Integer slotId, String holdCode) {
        if (slotId == null || slotId <= 0 || holdCode == null) {
            return false;
        }
        return changeHold(slotId, holdCode, false);
    }

    public boolean consumeHold(Integer slotId, String holdCode) {
        if (slotId == null || slotId <= 0 || holdCode == null) {
            return false;
        }
        try {
            if (con.getAutoCommit()) {
                throw new IllegalStateException("Consume hold bắt buộc trong transaction lưu lịch");
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không đọc được trạng thái transaction", e);
        }
        return changeHold(slotId, holdCode, true);
    }

    private boolean changeHold(Integer slotId, String holdCode, boolean consume) {
        String sql = """
                UPDATE tblAppointmentSlot6
                SET status = ?, holdCode = NULL, holdExpiresAt = NULL
                WHERE id = ? AND status = 'HELD' AND holdCode = ?
                """;
        String nextStatus = "AVAILABLE";
        if (consume) {
            // Kiểm tra lại ngay trong UPDATE để không có khoảng hở giữa kiểm tra và tiêu thụ.
            sql += " AND holdExpiresAt > NOW(6) AND startTime > NOW(6)";
            nextStatus = "BOOKED";
        }
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, nextStatus);
            statement.setInt(2, slotId);
            statement.setString(3, holdCode);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessException6("Không cập nhật được giữ chỗ", e);
        }
    }

    private AppointmentSlot6 readSlot(ResultSet rs) throws SQLException {
        AppointmentSlot6 slot = new AppointmentSlot6();
        slot.setId(rs.getObject("id", Integer.class));
        slot.setStartTime(rs.getTimestamp("startTime"));
        slot.setHoldCode(rs.getString("holdCode"));
        slot.setHoldExpiresAt(rs.getTimestamp("holdExpiresAt"));
        slot.setStatus(rs.getString("status"));
        return slot;
    }

    public Integer prepareSlots() {
        int days = DAO6.number("GARAMAN_SLOT_DAYS", 30);
        int open = DAO6.number("GARAMAN_OPEN_HOUR", 8);
        int close = DAO6.number("GARAMAN_CLOSE_HOUR", 17);
        int breakStart = DAO6.number("GARAMAN_BREAK_START", 12);
        int breakEnd = DAO6.number("GARAMAN_BREAK_END", 13);
        int minutes = DAO6.number("GARAMAN_SLOT_MINUTES", 60);
        if (days < 1 || days > 366 || minutes < 1 || minutes > 240
                || open < 0 || close > 24 || open >= close
                || breakStart < open || breakEnd > close || breakStart > breakEnd) {
            throw new IllegalArgumentException("Cấu hình lịch làm việc không hợp lệ");
        }
        String sql = """
                INSERT INTO tblAppointmentSlot6 (startTime, status)
                VALUES (?, 'AVAILABLE')
                ON DUPLICATE KEY UPDATE startTime = startTime
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            LocalDate today = LocalDate.now(DAO6.zone());
            int inserted = 0;
            for (int dayOffset = 0; dayOffset < days; dayOffset++) {
                for (int minute = open * 60; minute + minutes <= close * 60; minute += minutes) {
                    boolean overlapsBreak = minute < breakEnd * 60 && minute + minutes > breakStart * 60;
                    if (overlapsBreak) {
                        continue;
                    }
                    ZonedDateTime slotTime = today.plusDays(dayOffset).atStartOfDay()
                            .plusMinutes(minute).atZone(DAO6.zone());
                    if (!slotTime.toInstant().isAfter(Instant.now())) {
                        continue;
                    }
                    statement.setTimestamp(1, Timestamp.from(slotTime.toInstant()));
                    inserted += statement.executeUpdate();
                }
            }
            return inserted;
        } catch (SQLException e) {
            throw new DataAccessException6("Không chuẩn bị được slot", e);
        }
    }

    public static void main(String[] args) {
        try (SlotDAO6 setup = new SlotDAO6()) {
            System.out.println("Slots prepared idempotently, changed rows=" + setup.prepareSlots());
        } finally {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        }
    }

    private static Date date(LocalDate date) {
        return Date.from(date.atStartOfDay(DAO6.zone()).toInstant());
    }
}
