package garaman.servlet;

import garaman.dao.DAO6;
import garaman.dao.InvoiceDAO6;
import garaman.dao.PartUsageDAO6;
import garaman.dao.ServiceDAO6;
import garaman.dao.ServiceUsageDAO6;
import garaman.dao.SparePartDAO6;
import garaman.entity.PartUsage6;
import garaman.entity.Service6;
import garaman.entity.ServiceUsage6;
import garaman.entity.SparePart6;
import java.util.Objects;

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


@WebServlet("/service-part")
public class ServicePartServlet6 extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Đọc và kiểm tra ngữ cảnh báo cáo trước khi truy vấn dữ liệu.
        reportFilter(req);
        String type = text(req, "itemType");
        String code = text(req, "itemCode");
        if (!Set.of("SERVICE", "PART").contains(type) || code.isEmpty() || code.length() > 30) {
            throw new IllegalArgumentException("Đối tượng thống kê không hợp lệ.");
        }
        Date start = date(LocalDate.parse((String) req.getAttribute("startDate")));
        Date end = date(LocalDate.parse((String) req.getAttribute("endDate")));
        req.setAttribute("itemType", type);
        req.setAttribute("itemCode", code);

        String action = text(req, "action");
        if (!Set.of("", "invoice").contains(action)) {
            throw new IllegalArgumentException("Thao tác chi tiết không hợp lệ.");
        }
        boolean invoiceAction = "invoice".equals(action);
        Integer targetUsageId = 0;
        if (invoiceAction) {
            targetUsageId = integer(req, "usageId");
            if (targetUsageId <= 0) {
                throw new IllegalArgumentException("Mã lần sử dụng phải lớn hơn 0.");
            }
        }

        Integer rows;
        Integer usagePage;
        boolean containsUsage = false;
        if (type.equals("SERVICE")) {
            // M2 bước 28–40: thông tin dịch vụ → lịch sử trong kỳ → đếm toàn thời gian.
            try (ServiceDAO6 serviceDAO = new ServiceDAO6();
                    ServiceUsageDAO6 usageDAO = new ServiceUsageDAO6()) {
                Service6 service = serviceDAO.getServiceInfo(code);
                if (service == null) {
                    error(req, resp, 404, "Không tìm thấy dịch vụ.");
                    return;
                }
                ServiceUsage6[] all = usageDAO.getAllServiceUsageOfService(code, start, end);
                rows = all.length;
                usagePage = clampPage(page(req, "usagePage"), rows);
                ServiceUsage6[] pageRows = slice(all, usagePage);
                req.setAttribute("service", service);
                req.setAttribute("serviceUsageList", pageRows);
                Integer lifetimeUsageCount = usageDAO.getTotalUsageCount(code);
                req.setAttribute("lifetimeUsageCount", lifetimeUsageCount);

                for (ServiceUsage6 usage : all) {
                    if (Objects.equals(usage.getId(), targetUsageId)) {
                        containsUsage = true;
                    }
                }
            }
        } else {
            // M2 bước 49–61: cùng thứ tự xử lý cho phụ tùng.
            try (SparePartDAO6 partDAO = new SparePartDAO6();
                    PartUsageDAO6 usageDAO = new PartUsageDAO6()) {
                SparePart6 part = partDAO.getPartInfo(code);
                if (part == null) {
                    error(req, resp, 404, "Không tìm thấy phụ tùng.");
                    return;
                }
                PartUsage6[] all = usageDAO.getAllPartUsageOfPart(code, start, end);
                rows = all.length;
                usagePage = clampPage(page(req, "usagePage"), rows);
                PartUsage6[] pageRows = slice(all, usagePage);
                req.setAttribute("sparePart", part);
                req.setAttribute("partUsageList", pageRows);
                Integer lifetimeUsageCount = usageDAO.getTotalUsageCount(code);
                req.setAttribute("lifetimeUsageCount", lifetimeUsageCount);

                String image = part.getImage();
                if (image != null && image.matches("/assets/[a-zA-Z0-9_.-]+\\.(svg|png|jpg|webp)")) {
                    req.setAttribute("partImageUrl", req.getContextPath() + image);
                }
                for (PartUsage6 usage : all) {
                    if (Objects.equals(usage.getId(), targetUsageId)) {
                        containsUsage = true;
                    }
                }
            }
        }

        req.setAttribute("currentPage", usagePage);
        req.setAttribute("totalPages", (rows + 19) / 20);
        req.setAttribute("totalRows", rows);
        req.setAttribute("reportPage", page(req, "page"));
        Map<String, Object> params = reportParams(req);
        params.put("action", "list");
        req.setAttribute("backUrl", url(req, "/revenue", params));
        params.remove("action");
        params.put("itemType", type);
        params.put("itemCode", code);
        params.put("usagePage", usagePage);

        if (invoiceAction) {
            // M2 bước 69–74: chỉ mở hóa đơn của lần sử dụng thuộc lịch sử đã lọc.
            if (!containsUsage) {
                error(req, resp, 404, "Lần sử dụng không thuộc đối tượng và khoảng thống kê đang xem.");
                return;
            }
            String invoiceCode;
            try (InvoiceDAO6 invoiceDAO = new InvoiceDAO6()) {
                invoiceCode = invoiceDAO.getInvoiceCodeByUsage(type, targetUsageId);
            }
            if (invoiceCode == null) {
                error(req, resp, 404, "Lần sử dụng chưa có hóa đơn.");
                return;
            }
            params.put("invoiceCode", invoiceCode);
            resp.sendRedirect(url(req, "/invoice", params));
            return;
        }

        params.put("usagePage", usagePage - 1);
        req.setAttribute("previousUrl", url(req, "/service-part", params));
        params.put("usagePage", usagePage + 1);
        req.setAttribute("nextUrl", url(req, "/service-part", params));
        params.put("usagePage", usagePage);
        params.put("action", "invoice");
        req.setAttribute("invoiceBaseUrl", url(req, "/service-part", params));
        view(req, resp, "manager/ServicePartDetailView6");
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

    private static Integer integer(HttpServletRequest req, String name) {
        try {
            return Integer.parseInt(text(req, name));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá trị " + name + " không hợp lệ.");
        }
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
