package garaman.dao;

import garaman.entity.*;
import garaman.exception.DataAccessException6;
import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.ArrayList;

public class ServiceUsageDAO6 extends DAO6 {
    public ServiceUsageDAO6(){super();}
    public ServiceUsageDAO6(Connection con){super(con);}
    public Integer getTotalUsageCount(String serviceCode){
        try(var ps=con.prepareStatement("SELECT COUNT(*) FROM tblServiceUsage6 u JOIN tblService6 s ON s.id=u.tblService6id WHERE s.serviceCode=?")) {
            ps.setString(1,serviceCode);try(var rs=ps.executeQuery()){rs.next();return rs.getInt(1);}
        }catch(SQLException e){throw new DataAccessException6("Không đếm được lần sử dụng dịch vụ",e);}
    }
    public ServiceUsage6[] getAllServiceUsageOfService(String serviceCode,Date startDate,Date endDate){
        if(startDate==null||endDate==null||localDate(startDate).isAfter(localDate(endDate)))throw new IllegalArgumentException("Khoảng thống kê không hợp lệ.");
        String sql="SELECT u.* FROM tblServiceUsage6 u JOIN tblService6 s ON s.id=u.tblService6id JOIN tblInvoice6 i ON i.tblRepairOrder6id=u.tblRepairOrder6id WHERE s.serviceCode=? AND i.issueDate>=? AND i.issueDate<? AND i.paymentStatus='PAID' ORDER BY i.issueDate DESC,u.id DESC";
        try(var ps=con.prepareStatement(sql)){
            ps.setString(1,serviceCode);ps.setTimestamp(2,start(startDate));ps.setTimestamp(3,endExclusive(endDate));
            try(var rs=ps.executeQuery()){var rows=new ArrayList<ServiceUsage6>();while(rs.next())rows.add(readUsage(rs));return rows.toArray(ServiceUsage6[]::new);}
        }catch(SQLException e){throw new DataAccessException6("Không tải được lịch sử dịch vụ",e);}
    }
    ServiceUsage6[] byOrder(Integer orderId) throws SQLException {
        try(var ps=con.prepareStatement("SELECT * FROM tblServiceUsage6 WHERE tblRepairOrder6id=? ORDER BY id")) {
            ps.setInt(1,orderId);try(var rs=ps.executeQuery()){var rows=new ArrayList<ServiceUsage6>();while(rs.next())rows.add(readUsage(rs));return rows.toArray(ServiceUsage6[]::new);}
        }
    }

    private ServiceUsage6 readUsage(ResultSet rs) throws SQLException {
        ServiceUsage6 u=new ServiceUsage6();u.setId(rs.getObject("id",Integer.class));u.setUsageTime(rs.getTimestamp("usageTime"));u.setSaleUnitPrice(rs.getBigDecimal("saleUnitPrice"));
        u.setNote(rs.getString("note"));u.setService(new ServiceDAO6(con).findById(rs.getObject("tblService6id",Integer.class)));u.setTech(new MemberDAO6(con).findTechnicianById(rs.getObject("tblEmployee6tblMember6id",Integer.class)));
        u.setOrder(new InvoiceDAO6(con).findOrderById(rs.getObject("tblRepairOrder6id",Integer.class)));return u;
    }

    private static Date date(LocalDate date) { return Date.from(date.atStartOfDay(DAO6.zone()).toInstant()); }
    private static LocalDate localDate(Date date) { return date.toInstant().atZone(DAO6.zone()).toLocalDate(); }
    private static Timestamp start(Date date) { return new Timestamp(date(localDate(date)).getTime()); }
    private static Timestamp endExclusive(Date date) { return new Timestamp(date(localDate(date).plusDays(1)).getTime()); }
}
