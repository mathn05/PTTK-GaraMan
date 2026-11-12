package garaman.servlet;

import garaman.dao.AppointmentDAO6;
import garaman.dao.DAO6;
import garaman.dao.SlotDAO6;
import garaman.entity.Appointment6;
import garaman.entity.Customer6;
import garaman.exception.BookingConflictException6;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@WebServlet("/appointment")
public class AppointmentServlet6 extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Map<String, Object> c = BookingServlet6.context(req, false);
        synchronized (c) {
            // Một context đã hoàn tất chỉ chuyển về kết quả, không lưu thêm lịch hẹn.
            if (c.get("completed") == null) {
                Appointment6 draftAppointment = (Appointment6) c.get("draft");
                String holdCode = (String) c.get("holdCode");
                Integer slotId = (Integer) c.get("slotId");
                if (draftAppointment == null || holdCode == null || slotId == null) {
                    throw new BookingConflictException6("Giữ chỗ không còn hiệu lực. Vui lòng đặt lại lịch.");
                }
                Customer6 customer = customer(req);
                if (!Objects.equals(draftAppointment.getCus().getId(), customer.getId())) {
                    error(req, resp, 403, "Lịch hẹn không thuộc phiên hiện tại.");
                    return;
                }

                // M1 bước 50–52: kiểm tra trước khi lưu; DAO vẫn kiểm tra lại trong transaction.
                try (SlotDAO6 slotDAO = new SlotDAO6()) {
                    boolean validHold = slotDAO.validateHold(slotId, holdCode);
                    if (!validHold) {
                        resp.setStatus(409);
                        req.setAttribute("message",
                                "Giữ chỗ đã hết hạn. Chọn Chỉnh sửa để chọn lại khung giờ.");
                        BookingServlet6.confirm(req, resp, c);
                        return;
                    }
                }

                // M1 bước 53–62: dùng lại nháp do BookingServlet6 tạo.
                try (AppointmentDAO6 appointmentDAO = new AppointmentDAO6()) {
                    Appointment6 savedAppointment =
                            appointmentDAO.createAppointment(draftAppointment, holdCode);
                    c.put("completed", savedAppointment);
                }
                c.remove("holdCode");
                c.remove("holdExpiresAt");
                c.remove("draft");
            }

            // Redirect yêu cầu trình duyệt gửi GET tiếp theo (Post/Redirect/Get).
            Map<String, Object> redirectParams = Map.of("action", "confirm", "context", c.get("id"));
            resp.sendRedirect(url(req, "/booking", redirectParams));
        }
    }

    private static Customer6 customer(HttpServletRequest req) {
        // Danh tính lấy từ session đăng nhập, không nhận mã khách hàng từ form.
        Object user = req.getSession().getAttribute("currentUser");
        if (!(user instanceof Customer6 c) || c.getId() == null || c.getId() <= 0) {
            throw new IllegalArgumentException("Phiên khách hàng không hợp lệ.");
        }
        return c;
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
