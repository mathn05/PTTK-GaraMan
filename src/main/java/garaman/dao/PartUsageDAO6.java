package garaman.dao;

import garaman.entity.PartUsage6;
import garaman.entity.SparePart6;
import garaman.entity.Technician6;
import garaman.entity.RepairOrder6;
import garaman.exception.DataAccessException6;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class PartUsageDAO6 extends DAO6 {
    public PartUsageDAO6() {
        super();
    }

    public PartUsageDAO6(Connection con) {
        super(con);
    }

    public Integer getTotalUsageCount(String partCode) {
        // Lifetime đếm số dòng, không lọc thời gian hoặc trạng thái thanh toán.
        String sql = """
                SELECT COUNT(*)
                FROM tblPartUsage6 u
                JOIN tblSparePart6 s ON s.id = u.tblSparePart6id
                WHERE s.partCode = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, partCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không đếm được lần sử dụng phụ tùng", e);
        }
    }

    public PartUsage6[] getAllPartUsageOfPart(String partCode, Date startDate, Date endDate) {
        if (startDate == null || endDate == null || localDate(startDate).isAfter(localDate(endDate))) {
            throw new IllegalArgumentException("Khoảng thống kê không hợp lệ.");
        }
        String sql = """
                SELECT u.*
                FROM tblPartUsage6 u
                JOIN tblSparePart6 s ON s.id = u.tblSparePart6id
                JOIN tblInvoice6 i ON i.tblRepairOrder6id = u.tblRepairOrder6id
                WHERE s.partCode = ? AND i.issueDate >= ? AND i.issueDate < ?
                    AND i.paymentStatus = 'PAID'
                ORDER BY i.issueDate DESC, u.id DESC
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, partCode);
            statement.setTimestamp(2, start(startDate));
            statement.setTimestamp(3, endExclusive(endDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                ArrayList<PartUsage6> rows = new ArrayList<>();
                while (resultSet.next()) {
                    PartUsage6 usage = readUsage(resultSet);
                    rows.add(usage);
                }
                return rows.toArray(PartUsage6[]::new);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được lịch sử phụ tùng", e);
        }
    }

    PartUsage6[] byOrder(Integer orderId) throws SQLException {
        // Hóa đơn cần mọi dòng của phiếu sửa chữa, kể cả item khác với báo cáo.
        String sql = """
                SELECT * FROM tblPartUsage6
                WHERE tblRepairOrder6id = ?
                ORDER BY id
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                ArrayList<PartUsage6> rows = new ArrayList<>();
                while (resultSet.next()) {
                    PartUsage6 usage = readUsage(resultSet);
                    rows.add(usage);
                }
                return rows.toArray(PartUsage6[]::new);
            }
        }
    }

    private PartUsage6 readUsage(ResultSet rs) throws SQLException {
        PartUsage6 usage = new PartUsage6();
        usage.setId(rs.getObject("id", Integer.class));
        usage.setUsageTime(rs.getTimestamp("usageTime"));
        usage.setSaleUnitPrice(rs.getBigDecimal("saleUnitPrice"));
        usage.setUsedQuantity(rs.getInt("usedQuantity"));
        usage.setNote(rs.getString("note"));
        Integer itemId = rs.getObject("tblSparePart6id", Integer.class);
        SparePart6 item = new SparePartDAO6(con).findById(itemId);
        usage.setSparePart(item);
        Integer technicianId = rs.getObject("tblEmployee6tblMember6id", Integer.class);
        Technician6 technician = new MemberDAO6(con).findTechnicianById(technicianId);
        usage.setTech(technician);
        Integer orderId = rs.getObject("tblRepairOrder6id", Integer.class);
        RepairOrder6 order = new InvoiceDAO6(con).findOrderById(orderId);
        usage.setOrder(order);
        return usage;
    }

    private static Date date(LocalDate date) {
        return Date.from(date.atStartOfDay(DAO6.zone()).toInstant());
    }

    private static LocalDate localDate(Date date) {
        return date.toInstant().atZone(DAO6.zone()).toLocalDate();
    }

    private static Timestamp start(Date date) {
        LocalDate selectedDate = localDate(date);
        return new Timestamp(date(selectedDate).getTime());
    }

    private static Timestamp endExclusive(Date date) {
        // [đầu ngày bắt đầu, đầu ngày sau ngày kết thúc) bao gồm đủ ngày cuối.
        LocalDate nextDay = localDate(date).plusDays(1);
        return new Timestamp(date(nextDay).getTime());
    }
}
