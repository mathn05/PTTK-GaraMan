package garaman.dao;

import garaman.entity.RevenueStats6;
import garaman.exception.DataAccessException6;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class RevenueDAO6 extends DAO6 {
    public RevenueDAO6() {
        super();
    }

    public RevenueDAO6(Connection con) {
        super(con);
    }

    public RevenueStats6[] getRevenueStatistics(Date startDate, Date endDate, String statisticType) {
        if (!Set.of("SERVICE", "PART", "ALL").contains(statisticType)
                || startDate == null || endDate == null
                || localDate(startDate).isAfter(localDate(endDate))) {
            throw new IllegalArgumentException("Bộ lọc thống kê không hợp lệ.");
        }

        // M2 bước 16–18: chỉ hóa đơn PAID, giá lịch sử, usageCount là số dòng.
        String services = """
                SELECT s.serviceCode AS itemCode, s.serviceName AS itemName,
                    'SERVICE' AS itemType, COUNT(*) AS usageCount,
                    SUM(u.saleUnitPrice) AS totalRevenue
                FROM tblServiceUsage6 u
                JOIN tblService6 s ON s.id = u.tblService6id
                JOIN tblInvoice6 i ON i.tblRepairOrder6id = u.tblRepairOrder6id
                WHERE i.issueDate >= ? AND i.issueDate < ? AND i.paymentStatus = 'PAID'
                GROUP BY s.id, s.serviceCode, s.serviceName
                """;
        String parts = """
                SELECT s.partCode AS itemCode, s.partName AS itemName,
                    'PART' AS itemType, COUNT(*) AS usageCount,
                    SUM(u.usedQuantity * u.saleUnitPrice) AS totalRevenue
                FROM tblPartUsage6 u
                JOIN tblSparePart6 s ON s.id = u.tblSparePart6id
                JOIN tblInvoice6 i ON i.tblRepairOrder6id = u.tblRepairOrder6id
                WHERE i.issueDate >= ? AND i.issueDate < ? AND i.paymentStatus = 'PAID'
                GROUP BY s.id, s.partCode, s.partName
                """;

        String sql;
        int datePairs;
        if ("ALL".equals(statisticType)) {
            sql = "(" + services + ") UNION ALL (" + parts + ")";
            datePairs = 2;
        } else if ("SERVICE".equals(statisticType)) {
            sql = services;
            datePairs = 1;
        } else {
            sql = parts;
            datePairs = 1;
        }
        sql += " ORDER BY totalRevenue DESC, itemCode ASC, itemType ASC";

        try (PreparedStatement statement = con.prepareStatement(sql)) {
            for (int pair = 0; pair < datePairs; pair++) {
                statement.setTimestamp(pair * 2 + 1, start(startDate));
                statement.setTimestamp(pair * 2 + 2, endExclusive(endDate));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                ArrayList<RevenueStats6> rows = new ArrayList<>();
                while (resultSet.next()) {
                    RevenueStats6 revenueStats = new RevenueStats6();
                    revenueStats.setItemCode(resultSet.getString("itemCode"));
                    revenueStats.setItemName(resultSet.getString("itemName"));
                    revenueStats.setItemType(resultSet.getString("itemType"));
                    revenueStats.setUsageCount(resultSet.getInt("usageCount"));
                    revenueStats.setTotalRevenue(resultSet.getBigDecimal("totalRevenue"));
                    rows.add(revenueStats);
                }
                return rows.toArray(RevenueStats6[]::new);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tổng hợp được doanh thu", e);
        }
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
