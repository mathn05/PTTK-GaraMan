package garaman.dao;

import garaman.entity.RevenueStats6;
import garaman.exception.DataAccessException6;
import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.ArrayList;
import java.util.Set;

public class RevenueDAO6 extends DAO6 {
    public RevenueDAO6(){super();}
    public RevenueDAO6(Connection con){super(con);}
    public RevenueStats6[] getRevenueStatistics(Date startDate,Date endDate,String statisticType){
        if(!Set.of("SERVICE","PART","ALL").contains(statisticType) || startDate==null || endDate==null || localDate(startDate).isAfter(localDate(endDate)))throw new IllegalArgumentException("Bộ lọc thống kê không hợp lệ.");
        String services="SELECT s.serviceCode AS itemCode,s.serviceName AS itemName,'SERVICE' AS itemType,COUNT(*) AS usageCount,SUM(u.saleUnitPrice) AS totalRevenue FROM tblServiceUsage6 u JOIN tblService6 s ON s.id=u.tblService6id JOIN tblInvoice6 i ON i.tblRepairOrder6id=u.tblRepairOrder6id WHERE i.issueDate>=? AND i.issueDate<? AND i.paymentStatus='PAID' GROUP BY s.id,s.serviceCode,s.serviceName";
        String parts="SELECT s.partCode AS itemCode,s.partName AS itemName,'PART' AS itemType,COUNT(*) AS usageCount,SUM(u.usedQuantity*u.saleUnitPrice) AS totalRevenue FROM tblPartUsage6 u JOIN tblSparePart6 s ON s.id=u.tblSparePart6id JOIN tblInvoice6 i ON i.tblRepairOrder6id=u.tblRepairOrder6id WHERE i.issueDate>=? AND i.issueDate<? AND i.paymentStatus='PAID' GROUP BY s.id,s.partCode,s.partName";
        String sql=("ALL".equals(statisticType)?"("+services+") UNION ALL ("+parts+")":"SERVICE".equals(statisticType)?services:parts)+" ORDER BY totalRevenue DESC,itemCode ASC,itemType ASC";
        try(var ps=con.prepareStatement(sql)) {
            int pairs="ALL".equals(statisticType)?2:1;
            for(int i=0;i<pairs;i++){ps.setTimestamp(i*2+1,start(startDate));ps.setTimestamp(i*2+2,endExclusive(endDate));}
            try(var rs=ps.executeQuery()) {
                var rows=new ArrayList<RevenueStats6>();while(rs.next()){
                    RevenueStats6 r=new RevenueStats6();r.setItemCode(rs.getString("itemCode"));r.setItemName(rs.getString("itemName"));r.setItemType(rs.getString("itemType"));
                    r.setUsageCount(rs.getInt("usageCount"));r.setTotalRevenue(rs.getBigDecimal("totalRevenue"));rows.add(r);
                }return rows.toArray(RevenueStats6[]::new);
            }
        }catch(SQLException e){throw new DataAccessException6("Không tổng hợp được doanh thu",e);}
    }

    private static Date date(LocalDate date) { return Date.from(date.atStartOfDay(DAO6.zone()).toInstant()); }
    private static LocalDate localDate(Date date) { return date.toInstant().atZone(DAO6.zone()).toLocalDate(); }
    private static Timestamp start(Date date) { return new Timestamp(date(localDate(date)).getTime()); }
    private static Timestamp endExclusive(Date date) { return new Timestamp(date(localDate(date).plusDays(1)).getTime()); }
}
