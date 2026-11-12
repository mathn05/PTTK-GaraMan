package garaman.dao;

import garaman.entity.Appointment6;
import garaman.entity.Customer6;
import garaman.entity.Invoice6;
import garaman.entity.PartUsage6;
import garaman.entity.Payment6;
import garaman.entity.RepairOrder6;
import garaman.entity.SaleStaff6;
import garaman.entity.ServiceUsage6;
import garaman.entity.Vehicle6;
import garaman.exception.DataAccessException6;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InvoiceDAO6 extends DAO6 {
    public InvoiceDAO6() {
        super();
    }

    public InvoiceDAO6(Connection con) {
        super(con);
    }

    public String getInvoiceCodeByUsage(String itemType, Integer usageId) {
        // Tên bảng chỉ lấy từ hai giá trị cho phép; usageId vẫn gán bằng PreparedStatement.
        String table = switch (itemType) {
            case "SERVICE" -> "tblServiceUsage6";
            case "PART" -> "tblPartUsage6";
            default -> throw new IllegalArgumentException("Loại đối tượng không hợp lệ.");
        };
        if (usageId == null || usageId <= 0) {
            throw new IllegalArgumentException("Mã lần sử dụng phải lớn hơn 0.");
        }
        String sql = """
                SELECT i.invoiceCode
                FROM %s u
                JOIN tblInvoice6 i ON i.tblRepairOrder6id = u.tblRepairOrder6id
                WHERE u.id = ?
                """.formatted(table);
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, usageId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tìm được hóa đơn", e);
        }
    }

    public Invoice6 getInvoiceDetail(String invoiceCode) {
        String sql = """
                SELECT * FROM vwInvoice6
                WHERE invoiceCode = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, invoiceCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                // M2 bước 78–79: hóa đơn và phiếu sửa chữa cùng các đối tượng liên kết.
                Invoice6 invoice = new Invoice6();
                invoice.setId(resultSet.getObject("id", Integer.class));
                invoice.setInvoiceCode(resultSet.getString("invoiceCode"));
                invoice.setIssueDate(resultSet.getTimestamp("issueDate"));
                BigDecimal totalAmount = resultSet.getBigDecimal("totalAmount");
                invoice.setTotalAmount(totalAmount);
                Integer orderId = resultSet.getObject("tblRepairOrder6id", Integer.class);
                RepairOrder6 order = findOrderById(orderId);

                // M2 bước 80: lấy tất cả dòng của hóa đơn, không áp lại bộ lọc item/kỳ báo cáo.
                ServiceUsage6[] serviceUsage = new ServiceUsageDAO6(con).byOrder(order.getId());
                order.setServiceUsage(serviceUsage);
                PartUsage6[] partUsage = new PartUsageDAO6(con).byOrder(order.getId());
                order.setPartUsage(partUsage);
                invoice.setOrder(order);

                // M2 bước 81–82: Payment6 ánh xạ từ chính dòng hóa đơn, không có bảng Payment.
                if ("PAID".equals(resultSet.getString("paymentStatus"))) {
                    Payment6 payment = new Payment6();
                    payment.setId(invoice.getId());
                    payment.setPaymentAmount(resultSet.getBigDecimal("paymentAmount"));
                    payment.setPaymentTime(resultSet.getTimestamp("paymentTime"));
                    payment.setPaymentMethod(resultSet.getString("paymentMethod"));
                    payment.setStatus("PAID");
                    payment.setTransactionReference(resultSet.getString("transactionReference"));
                    Integer cashierId = resultSet.getObject("tblEmployee6tblMember6id", Integer.class);
                    SaleStaff6 cashier = new MemberDAO6(con).findSaleStaffById(cashierId);
                    payment.setSs(cashier);
                    if (totalAmount.compareTo(payment.getPaymentAmount()) != 0) {
                        throw new SQLException("Số tiền đã thanh toán không khớp tổng hóa đơn");
                    }
                    invoice.setPayment(payment);
                }
                return invoice;
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được chi tiết hóa đơn", e);
        }
    }

    RepairOrder6 findOrderById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            return null;
        }
        String sql = """
                SELECT * FROM tblRepairOrder6
                WHERE id = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                RepairOrder6 order = new RepairOrder6();
                order.setId(id);
                order.setRepairOrderCode(resultSet.getString("repairOrderCode"));
                order.setCreateTime(resultSet.getTimestamp("createTime"));
                order.setCompletionTime(resultSet.getTimestamp("completionTime"));
                order.setVehicleCondition(resultSet.getString("vehicleCondition"));
                order.setRepairDescription(resultSet.getString("repairDescription"));
                order.setRepairResult(resultSet.getString("repairResult"));
                order.setRepairStatus(resultSet.getString("repairStatus"));

                Integer customerId = resultSet.getObject("tblCustomer6tblMember6id", Integer.class);
                Customer6 customer = new CustomerDAO6(con).findById(customerId);
                order.setCus(customer);
                Integer vehicleId = resultSet.getObject("tblVehicle6id", Integer.class);
                Vehicle6 vehicle = new VehicleDAO6(con).findById(vehicleId);
                order.setVehicle(vehicle);
                Integer saleStaffId = resultSet.getObject("tblEmployee6tblMember6id", Integer.class);
                SaleStaff6 saleStaff = new MemberDAO6(con).findSaleStaffById(saleStaffId);
                order.setSs(saleStaff);

                // Phiếu sửa chữa có thể không phát sinh từ một lịch hẹn.
                Integer appointmentId = resultSet.getObject("tblAppointment6id", Integer.class);
                if (appointmentId != null) {
                    Appointment6 appointment = new AppointmentDAO6(con).findById(appointmentId);
                    order.setAppointment(appointment);
                }
                return order;
            }
        }
    }
}
