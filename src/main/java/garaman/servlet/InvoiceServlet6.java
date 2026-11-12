package garaman.servlet;

import garaman.dao.DAO6;
import garaman.dao.InvoiceDAO6;
import garaman.entity.Invoice6;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@WebServlet("/invoice")
public class InvoiceServlet6 extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // M2 bước 75–82: mã hóa đơn chỉ dùng làm khóa tra cứu; DAO đóng gói toàn bộ chi tiết.
        String invoiceCode = text(req, "invoiceCode");
        if (invoiceCode.isEmpty() || invoiceCode.length() > 30) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        }
        Invoice6 invoice;
        try (InvoiceDAO6 invoiceDAO = new InvoiceDAO6()) {
            invoice = invoiceDAO.getInvoiceDetail(invoiceCode);
        }
        if (invoice == null) {
            error(req, resp, 404, "Không tìm thấy hóa đơn.");
            return;
        }
        req.setAttribute("invoice", invoice);

        // M2 bước 83–84: dựng đường quay lại với đủ bộ lọc và hai số trang.
        if (!text(req, "itemCode").isEmpty()) {
            reportFilter(req);
            String type = text(req, "itemType");
            if (!Set.of("SERVICE", "PART").contains(type)) {
                throw new IllegalArgumentException("Loại đối tượng không hợp lệ.");
            }
            Map<String, Object> params = reportParams(req);
            params.put("itemType", type);
            params.put("itemCode", text(req, "itemCode"));
            params.put("usagePage", page(req, "usagePage"));
            req.setAttribute("backUrl", url(req, "/service-part", params));
        } else {
            req.setAttribute("backUrl", req.getContextPath() + "/revenue?action=filter");
        }
        view(req, resp, "manager/InvoiceDetailView6");
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
