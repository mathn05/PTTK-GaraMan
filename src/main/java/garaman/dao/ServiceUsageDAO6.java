package garaman.dao;

import garaman.entity.ServiceUsage6;
import garaman.entity.Service6;
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

public class ServiceUsageDAO6 extends DAO6 {
    public ServiceUsageDAO6() {
        super();
    }

    public ServiceUsageDAO6(Connection con) {
        super(con);
    }

    public Integer getTotalUsageCount(String serviceCode) {
        // Lifetime đếm số dòng, không lọc thời gian hoặc trạng thái thanh toán.
        String sql = """
                SELECT COUNT(*)
                FROM tblServiceUsage6 u
                JOIN tblService6 s ON s.id = u.tblService6id
                WHERE s.serviceCode = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, serviceCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không đếm được lần sử dụng dịch vụ", e);
        }
    }

    public ServiceUsage6[] getAllServiceUsageOfService(String serviceCode, Date startDate, Date endDate) {
        if (startDate == null || endDate == null || localDate(startDate).isAfter(localDate(endDate))) {
            throw new IllegalArgumentException("Khoảng thống kê không hợp lệ.");
        }
        String sql = """
                SELECT u.*
                FROM tblServiceUsage6 u
                JOIN tblService6 s ON s.id = u.tblService6id
                JOIN tblInvoice6 i ON i.tblRepairOrder6id = u.tblRepairOrder6id
                WHERE s.serviceCode = ? AND i.issueDate >= ? AND i.issueDate < ?
                    AND i.paymentStatus = 'PAID'
                ORDER BY i.issueDate DESC, u.id DESC
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, serviceCode);
            statement.setTimestamp(2, start(startDate));
            statement.setTimestamp(3, endExclusive(endDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                ArrayList<ServiceUsage6> rows = new ArrayList<>();
                while (resultSet.next()) {
                    ServiceUsage6 usage = readUsage(resultSet);
                    rows.add(usage);
                }
                return rows.toArray(ServiceUsage6[]::new);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được lịch sử dịch vụ", e);
        }
    }

    ServiceUsage6[] byOrder(Integer orderId) throws SQLException {
        // Hóa đơn cần mọi dòng của phiếu sửa chữa, kể cả item khác với báo cáo.
        String sql = """
                SELECT * FROM tblServiceUsage6
                WHERE tblRepairOrder6id = ?
                ORDER BY id
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                ArrayList<ServiceUsage6> rows = new ArrayList<>();
                while (resultSet.next()) {
                    ServiceUsage6 usage = readUsage(resultSet);
                    rows.add(usage);
                }
                return rows.toArray(ServiceUsage6[]::new);
            }
        }
    }

    private ServiceUsage6 readUsage(ResultSet rs) throws SQLException {
        ServiceUsage6 usage = new ServiceUsage6();
        usage.setId(rs.getObject("id", Integer.class));
        usage.setUsageTime(rs.getTimestamp("usageTime"));
        usage.setSaleUnitPrice(rs.getBigDecimal("saleUnitPrice"));
        usage.setNote(rs.getString("note"));
        Integer itemId = rs.getObject("tblService6id", Integer.class);
        Service6 item = new ServiceDAO6(con).findById(itemId);
        usage.setService(item);
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
