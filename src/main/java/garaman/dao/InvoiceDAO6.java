package garaman.dao;

import garaman.entity.*;
import garaman.exception.DataAccessException6;
import java.math.BigDecimal;
import java.sql.*;

public class InvoiceDAO6 extends DAO6 {
    public InvoiceDAO6(){super();}
    public InvoiceDAO6(Connection con){super(con);}
    public String getInvoiceCodeByUsage(String itemType,Integer usageId){
        String table=switch(itemType){case "SERVICE"->"tblServiceUsage6";case "PART"->"tblPartUsage6";default->throw new IllegalArgumentException("Loại đối tượng không hợp lệ.");};
        if(usageId==null||usageId<=0)throw new IllegalArgumentException("Mã lần sử dụng phải lớn hơn 0.");
        try(var ps=con.prepareStatement("SELECT i.invoiceCode FROM "+table+" u JOIN tblInvoice6 i ON i.tblRepairOrder6id=u.tblRepairOrder6id WHERE u.id=?")) {
            ps.setInt(1,usageId);try(var rs=ps.executeQuery()){return rs.next()?rs.getString(1):null;}
        }catch(SQLException e){throw new DataAccessException6("Không tìm được hóa đơn",e);}
    }
    public Invoice6 getInvoiceDetail(String invoiceCode){
        try(var ps=con.prepareStatement("SELECT * FROM vwInvoice6 WHERE invoiceCode=?")) {
            ps.setString(1,invoiceCode);try(var rs=ps.executeQuery()) {
                if(!rs.next())return null;
                Invoice6 i=new Invoice6();i.setId(rs.getObject("id",Integer.class));i.setInvoiceCode(rs.getString("invoiceCode"));i.setIssueDate(rs.getTimestamp("issueDate"));
                BigDecimal total=rs.getBigDecimal("totalAmount");i.setTotalAmount(total);
                RepairOrder6 o=findOrderById(rs.getObject("tblRepairOrder6id",Integer.class));
                o.setServiceUsage(new ServiceUsageDAO6(con).byOrder(o.getId()));
                o.setPartUsage(new PartUsageDAO6(con).byOrder(o.getId()));i.setOrder(o);
                if("PAID".equals(rs.getString("paymentStatus"))){
                    Payment6 payment=new Payment6();
                    // Payment is an optional value object folded into this invoice, not a physical row/table.
                    payment.setId(i.getId());payment.setPaymentAmount(rs.getBigDecimal("paymentAmount"));
                    payment.setPaymentTime(rs.getTimestamp("paymentTime"));payment.setPaymentMethod(rs.getString("paymentMethod"));
                    payment.setStatus("PAID");payment.setTransactionReference(rs.getString("transactionReference"));
                    payment.setSs(new MemberDAO6(con).findSaleStaffById(rs.getObject("tblEmployee6tblMember6id",Integer.class)));
                    if(total.compareTo(payment.getPaymentAmount())!=0)throw new SQLException("Số tiền đã thanh toán không khớp tổng hóa đơn");
                    i.setPayment(payment);
                }
                return i;
            }
        }catch(SQLException e){throw new DataAccessException6("Không tải được chi tiết hóa đơn",e);}
    }

    RepairOrder6 findOrderById(Integer id) throws SQLException {
        if(id==null||id<=0)return null;
        try(var ps=con.prepareStatement("SELECT * FROM tblRepairOrder6 WHERE id=?")) {
            ps.setInt(1,id);try(var rs=ps.executeQuery()) {
                if(!rs.next()) return null;
                RepairOrder6 o=new RepairOrder6();o.setId(id);o.setRepairOrderCode(rs.getString("repairOrderCode"));o.setCreateTime(rs.getTimestamp("createTime"));
                o.setCompletionTime(rs.getTimestamp("completionTime"));o.setVehicleCondition(rs.getString("vehicleCondition"));o.setRepairDescription(rs.getString("repairDescription"));
                o.setRepairResult(rs.getString("repairResult"));o.setRepairStatus(rs.getString("repairStatus"));o.setCus(new CustomerDAO6(con).findById(rs.getObject("tblCustomer6tblMember6id",Integer.class)));
                o.setVehicle(new VehicleDAO6(con).findById(rs.getObject("tblVehicle6id",Integer.class)));o.setSs(new MemberDAO6(con).findSaleStaffById(rs.getObject("tblEmployee6tblMember6id",Integer.class)));
                if(rs.getObject("tblAppointment6id",Integer.class)!=null) o.setAppointment(new AppointmentDAO6(con).findById(rs.getObject("tblAppointment6id",Integer.class)));
                return o;
            }
        }
    }
}
