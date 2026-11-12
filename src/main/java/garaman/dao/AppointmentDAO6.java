package garaman.dao;

import garaman.entity.Appointment6;
import garaman.entity.Customer6;
import garaman.entity.Vehicle6;
import garaman.entity.AppointmentSlot6;
import garaman.entity.SaleStaff6;
import garaman.exception.BookingConflictException6;
import garaman.exception.DataAccessException6;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class AppointmentDAO6 extends DAO6 {
    public AppointmentDAO6() {
        super();
    }

    public AppointmentDAO6(Connection con) {
        super(con);
    }

    public Appointment6 createAppointment(Appointment6 appointment, String holdCode) {
        if (appointment == null || appointment.getCus() == null || appointment.getVehicle() == null
                || appointment.getSlot() == null || appointment.getId() != null) {
            throw new IllegalArgumentException("Thông tin lịch hẹn không hợp lệ.");
        }
        Integer customerId = appointment.getCus().getId();
        Integer vehicleId = appointment.getVehicle().getId();
        Integer slotId = appointment.getSlot().getId();
        String description = appointment.getRequestDescription();
        if (customerId == null || vehicleId == null || slotId == null
                || customerId <= 0 || vehicleId <= 0 || slotId <= 0
                || description == null || description.isBlank() || description.length() > 255) {
            throw new IllegalArgumentException("Thông tin lịch hẹn không hợp lệ.");
        }

        boolean tx = false;
        try {
            // Connection mượn thuộc transaction của caller; chỉ DAO sở hữu mới commit/rollback.
            if (!owner && con.getAutoCommit()) {
                throw new IllegalStateException("Connection mượn cần transaction caller");
            }
            tx = owner;
            if (tx) {
                con.setAutoCommit(false);
            }

            // M1 bước 55–58: consume giữ khóa slot đến khi cả lịch hẹn được commit.
            SlotDAO6 slotDAO = new SlotDAO6(con);
            boolean consumed = slotDAO.consumeHold(slotId, holdCode);
            if (!consumed) {
                throw new BookingConflictException6(
                        "Giữ chỗ đã hết hạn hoặc không còn hợp lệ. Vui lòng chọn lại khung giờ.");
            }

            String ownershipSql = """
                    SELECT id
                    FROM tblVehicle6
                    WHERE id = ? AND tblCustomer6tblMember6id = ?
                    FOR SHARE
                    """;
            try (PreparedStatement statement = con.prepareStatement(ownershipSql)) {
                statement.setInt(1, vehicleId);
                statement.setInt(2, customerId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new BookingConflictException6("Xe không thuộc khách hàng hiện tại.");
                    }
                }
            }

            // M1 bước 59: mã tạm thỏa NOT NULL/UNIQUE; mã chính thức dựa trên ID tự tăng.
            String temporary = temporaryCode();
            String insertSql = """
                    INSERT INTO tblAppointment6 (
                        appointmentCode, requestDescription, createAt, status,
                        tblEmployee6tblMember6id, tblVehicle6id,
                        tblAppointmentSlot6id, tblCustomer6tblMember6id
                    )
                    VALUES (?, ?, NOW(6), 'PENDING', NULL, ?, ?, ?)
                    """;
            int id;
            try (PreparedStatement statement = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, temporary);
                statement.setString(2, description.trim());
                statement.setInt(3, vehicleId);
                statement.setInt(4, slotId);
                statement.setInt(5, customerId);
                statement.executeUpdate();
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new SQLException("Không có ID lịch hẹn");
                    }
                    id = generatedKeys.getInt(1);
                }
            }

            String appointmentCode = "LH" + String.format(Locale.ROOT, "%04d", id);
            String finalizeSql = """
                    UPDATE tblAppointment6
                    SET appointmentCode = ?
                    WHERE id = ? AND appointmentCode = ?
                    """;
            try (PreparedStatement statement = con.prepareStatement(finalizeSql)) {
                statement.setString(1, appointmentCode);
                statement.setInt(2, id);
                statement.setString(3, temporary);
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("Cannot finalize appointment code");
                }
            }

            // M1 bước 60–61: đọc lại trong cùng giao dịch, tái sử dụng khách hàng của nháp.
            Appointment6 savedAppointment = findById(id, appointment.getCus());
            if (tx) {
                con.commit();
            }
            return savedAppointment;
        } catch (SQLException e) {
            rollback(tx, e);
            if (e.getErrorCode() == 1062) {
                throw new BookingConflictException6("Khung giờ đã có lịch hẹn hoạt động.");
            }
            throw new DataAccessException6("Không lưu được lịch hẹn", e);
        } catch (RuntimeException e) {
            rollback(tx, e);
            throw e;
        } finally {
            if (tx) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new DataAccessException6("Không khôi phục được kết nối", e);
                }
            }
        }
    }

    private void rollback(boolean tx, Exception cause) {
        if (tx) {
            try {
                con.rollback();
            } catch (SQLException e) {
                cause.addSuppressed(e);
            }
        }
    }

    Appointment6 findById(Integer id) throws SQLException {
        return findById(id, null);
    }

    private Appointment6 findById(Integer id, Customer6 knownCustomer) throws SQLException {
        if (id == null || id <= 0) {
            return null;
        }
        String sql = """
                SELECT *
                FROM tblAppointment6
                WHERE id = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Appointment6 appointment = new Appointment6();
                appointment.setId(id);
                appointment.setAppointmentCode(resultSet.getString("appointmentCode"));
                appointment.setCreateAt(resultSet.getTimestamp("createAt"));
                appointment.setRequestDescription(resultSet.getString("requestDescription"));
                appointment.setStatus(resultSet.getString("status"));

                Integer customerId = resultSet.getObject("tblCustomer6tblMember6id", Integer.class);
                if (knownCustomer != null && !Objects.equals(customerId, knownCustomer.getId())) {
                    throw new SQLException("Khách hàng của lịch hẹn không khớp ngữ cảnh.");
                }
                Customer6 customer = knownCustomer;
                if (customer == null) {
                    customer = new CustomerDAO6(con).findById(customerId);
                }
                appointment.setCus(customer);

                Integer vehicleId = resultSet.getObject("tblVehicle6id", Integer.class);
                Vehicle6 vehicle = new VehicleDAO6(con, knownCustomer).findById(vehicleId);
                appointment.setVehicle(vehicle);
                Integer slotId = resultSet.getObject("tblAppointmentSlot6id", Integer.class);
                AppointmentSlot6 slot = new SlotDAO6(con).findById(slotId);
                appointment.setSlot(slot);
                Integer staffId = resultSet.getObject("tblEmployee6tblMember6id", Integer.class);
                SaleStaff6 saleStaff = new MemberDAO6(con).findSaleStaffById(staffId);
                appointment.setSs(saleStaff);
                return appointment;
            }
        }
    }

    private static String temporaryCode() {
        UUID randomId = UUID.randomUUID();
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(randomId.getMostSignificantBits());
        buffer.putLong(randomId.getLeastSignificantBits());
        byte[] bytes = buffer.array();
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return "~" + encoded;
    }
}
