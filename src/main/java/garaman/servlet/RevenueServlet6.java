package garaman.servlet;

import garaman.dao.DAO6;
import garaman.dao.RevenueDAO6;
import garaman.entity.RevenueStats6;
import java.math.BigDecimal;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@WebServlet("/revenue")
public class RevenueServlet6 extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = text(req, "action");
        switch (action) {
            case "home" -> view(req, resp, "manager/ManagerHomeView6");
            case "", "menu" -> view(req, resp, "manager/ReportMenuView6");
            case "filter" -> filter(req, resp);
            case "list" -> {
                try {
                    reportFilter(req);
                    list(req, resp);
                } catch (IllegalArgumentException e) {
                    resp.setStatus(400);
                    req.setAttribute("message", e.getMessage());
                    filter(req, resp);
                }
            }
            default -> error(req, resp, 400, "Thao tác báo cáo không hợp lệ.");
        }
    }

    private void filter(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LocalDate lastDayOfPreviousMonth = LocalDate.now(DAO6.zone()).withDayOfMonth(1).minusDays(1);
        String startDate = text(req, "startDate");
        if (startDate.isEmpty()) {
            startDate = lastDayOfPreviousMonth.withDayOfMonth(1).toString();
        }
        String endDate = text(req, "endDate");
        if (endDate.isEmpty()) {
            endDate = lastDayOfPreviousMonth.toString();
        }
        String statisticType = text(req, "statisticType");
        if (statisticType.isEmpty()) {
            statisticType = "ALL";
        }

        req.setAttribute("startDate", startDate);
        req.setAttribute("endDate", endDate);
        req.setAttribute("statisticType", statisticType);
        view(req, resp, "manager/RevenueStatisticsFilterView6");
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // M2 bước 14–21: bộ lọc đã được kiểm tra trước khi gọi DAO.
        LocalDate selectedStartDate = LocalDate.parse((String) req.getAttribute("startDate"));
        LocalDate selectedEndDate = LocalDate.parse((String) req.getAttribute("endDate"));
        Date startDate = date(selectedStartDate);
        Date endDate = date(selectedEndDate);
        String statisticType = (String) req.getAttribute("statisticType");
        RevenueStats6[] all;
        try (RevenueDAO6 revenueDAO = new RevenueDAO6()) {
            all = revenueDAO.getRevenueStatistics(startDate, endDate, statisticType);
        }

        // M2 bước 22–24: tổng doanh thu của toàn tập, sau đó lấy trang hiển thị.
        int currentPage = clampPage(page(req, "page"), all.length);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (RevenueStats6 row : all) {
            totalRevenue = totalRevenue.add(row.getTotalRevenue());
        }
        RevenueStats6[] pageRows = slice(all, currentPage);
        int totalPages = (all.length + 19) / 20;
        req.setAttribute("revenueStats", pageRows);
        req.setAttribute("totalRevenue", totalRevenue);
        req.setAttribute("currentPage", currentPage);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalRows", all.length);

        // Giữ cùng bộ lọc khi phân trang và quay lại màn hình chọn điều kiện.
        Map<String, Object> params = reportParams(req);
        params.put("action", "list");
        params.put("page", currentPage - 1);
        req.setAttribute("previousUrl", url(req, "/revenue", params));
        params.put("page", currentPage + 1);
        req.setAttribute("nextUrl", url(req, "/revenue", params));
        params.put("action", "filter");
        params.remove("page");
        req.setAttribute("backUrl", url(req, "/revenue", params));
        view(req, resp, "manager/RevenueStatisticsListView6");
    }

    private static LocalDate date(HttpServletRequest req, String name) {
        try {
            return LocalDate.parse(text(req, name));
        } catch (java.time.DateTimeException e) {
            throw new IllegalArgumentException("Ngày không hợp lệ.");
        }
    }

    private static Integer page(HttpServletRequest req, String name) {
        String value = text(req, name);
        if (value.isEmpty()) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static void reportFilter(HttpServletRequest req) {
        LocalDate start = date(req, "startDate");
        LocalDate end = date(req, "endDate");
        if (start.isAfter(end) || start.getYear() < 1900 || end.getYear() > 9998) {
            throw new IllegalArgumentException("Khoảng ngày thống kê không hợp lệ.");
        }
        String type = text(req, "statisticType");
        if (!Set.of("SERVICE", "PART", "ALL").contains(type)) {
            throw new IllegalArgumentException("Loại thống kê không hợp lệ.");
        }
        req.setAttribute("startDate", start.toString());
        req.setAttribute("endDate", end.toString());
        req.setAttribute("statisticType", type);
    }

    private static Map<String, Object> reportParams(HttpServletRequest req) {
        Map<String, Object> params = new LinkedHashMap<>();
        for (String key : List.of("startDate", "endDate", "statisticType")) {
            params.put(key, req.getAttribute(key));
        }
        params.put("page", page(req, "page"));
        return params;
    }

    private static Integer clampPage(Integer page, Integer length) {
        int pages = (length + 19) / 20;
        if (pages == 0) {
            return 1;
        }
        return Math.min(page, pages);
    }

    private static <T> T[] slice(T[] all, Integer page) {
        int from = Math.min(all.length, (page - 1) * 20);
        int to = Math.min(all.length, from + 20);
        return Arrays.copyOfRange(all, from, to);
    }

    private static Date date(LocalDate date) {
        return Date.from(date.atStartOfDay(DAO6.zone()).toInstant());
    }

    private static void error(HttpServletRequest req, HttpServletResponse resp, int status, String message)
            throws ServletException, IOException {
        resp.setStatus(status);
        req.setAttribute("message", message);
        req.setAttribute("errorStatus", status);
        view(req, resp, "ErrorView6");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String text(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static String url(HttpServletRequest req, String route, Map<String, ?> params) {
        StringBuilder result = new StringBuilder(req.getContextPath());
        result.append(route);
        String separator = "?";
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            result.append(separator);
            result.append(encode(entry.getKey()));
            result.append('=');
            result.append(encode(String.valueOf(entry.getValue())));
            separator = "&";
        }
        return result.toString();
    }

    private static void view(HttpServletRequest req, HttpServletResponse resp, String name)
            throws ServletException, IOException {
        req.setAttribute("ctx", req.getContextPath());
        req.setAttribute("businessZone", DAO6.zone().getId());
        req.getRequestDispatcher("/WEB-INF/views/" + name + ".jsp").forward(req, resp);
    }
}
