package garaman.servlet;

import garaman.dao.DAO6;
import garaman.dao.SlotDAO6;
import garaman.dao.VehicleDAO6;
import garaman.entity.Appointment6;
import garaman.entity.AppointmentSlot6;
import garaman.entity.Customer6;
import garaman.entity.Vehicle6;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/booking")
public class BookingServlet6 extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = text(req, "action");
        if ("home".equals(action)) {
            req.setAttribute("cus", customer(req));
            view(req, resp, "customer/CustomerHomeView6");
            return;
        }

        Map<String, Object> c = context(req, true);
        // Khóa riêng ngữ cảnh để hai request của cùng tab không thay đổi nháp đồng thời.
        synchronized (c) {
            boolean showingConfirmation = "confirm".equals(action);
            boolean hasHold = c.get("holdCode") != null;
            boolean hasCompletedAppointment = c.get("completed") != null;
            if (showingConfirmation || hasHold || hasCompletedAppointment) {
                confirm(req, resp, c);
                return;
            }

            if (!text(req, "appointmentDate").isEmpty()) {
                c.put("appointmentDate", date(req, "appointmentDate"));
            }
            form(req, resp, c);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Map<String, Object> c = context(req, false);
        synchronized (c) {
            String action = text(req, "action");
            if (!Set.of("edit", "hold", "addVehicle").contains(action)) {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            if ("edit".equals(action)) {
                Map<String, Object> redirectParams;
                if (c.get("completed") == null) {
                    release(c);
                    redirectParams = Map.of("context", c.get("id"));
                } else {
                    redirectParams = Map.of("action", "confirm", "context", c.get("id"));
                }
                resp.sendRedirect(url(req, "/booking", redirectParams));
                return;
            }

            // Gửi lại form không được tạo nháp hoặc giữ chỗ thứ hai.
            if (c.get("completed") != null || c.get("holdCode") != null) {
                confirm(req, resp, c);
                return;
            }

            try {
                // Đọc và giữ dữ liệu nhập trước khi kiểm tra hoặc chuyển sang thêm xe.
                LocalDate day = date(req, "appointmentDate");
                String description = text(req, "requestDescription");
                Integer vehicleId = null;
                if (!text(req, "vehicle").isEmpty()) {
                    vehicleId = integer(req, "vehicle");
                }
                Integer slotId = null;
                if (!text(req, "appointmentSlot").isEmpty()) {
                    slotId = integer(req, "appointmentSlot");
                }
                c.put("appointmentDate", day);
                c.put("requestDescription", description);
                c.put("vehicleId", vehicleId);
                c.put("slotId", slotId);

                if ("addVehicle".equals(action)) {
                    resp.sendRedirect(url(req, "/vehicle", Map.of("context", c.get("id"))));
                    return;
                }

                LocalDate today = LocalDate.now(DAO6.zone());
                LocalDate lastBookingDate = today.plusDays(DAO6.number("GARAMAN_SLOT_DAYS", 30) - 1);
                if (day.isBefore(today) || day.isAfter(lastBookingDate)) {
                    throw new IllegalArgumentException("Ngày hẹn nằm ngoài lịch làm việc có thể đặt.");
                }
                if (description.isBlank() || description.length() > 255 || vehicleId == null || slotId == null) {
                    throw new IllegalArgumentException(
                            "Vui lòng chọn xe, khung giờ và nhập nội dung yêu cầu (tối đa 255 ký tự).");
                }

                // M1 bước 25–29: kiểm tra chủ xe bằng khách hàng đã đăng nhập.
                Customer6 customer = customer(req);
                Vehicle6 selectedVehicle;
                try (VehicleDAO6 vehicleDAO = new VehicleDAO6(customer)) {
                    selectedVehicle = vehicleDAO.findById(vehicleId);
                }
                if (selectedVehicle == null
                        || !Objects.equals(selectedVehicle.getCus().getId(), customer.getId())) {
                    error(req, resp, 403, "Xe không thuộc khách hàng hiện tại.");
                    return;
                }

                // M1 bước 30–38: kiểm tra ngày trước khi xin giữ chỗ theo thời gian DB.
                AppointmentSlot6 heldSlot;
                try (SlotDAO6 slotDAO = new SlotDAO6()) {
                    AppointmentSlot6 selectedSlot = slotDAO.findById(slotId);
                    if (selectedSlot == null) {
                        throw new IllegalArgumentException("Khung giờ không thuộc ngày đã chọn.");
                    }
                    LocalDate slotDate = selectedSlot.getStartTime().toInstant()
                            .atZone(DAO6.zone()).toLocalDate();
                    if (!slotDate.equals(day)) {
                        throw new IllegalArgumentException("Khung giờ không thuộc ngày đã chọn.");
                    }
                    heldSlot = slotDAO.holdSlot(slotId);
                }
                if (heldSlot == null) {
                    resp.setStatus(409);
                    req.setAttribute("message",
                            "Khung giờ vừa được giữ hoặc đã đặt. Vui lòng chọn khung giờ khác.");
                    form(req, resp, c);
                    return;
                }

                c.put("holdCode", heldSlot.getHoldCode());
                c.put("holdExpiresAt", heldSlot.getHoldExpiresAt());
                try {
                    // M1 bước 39–40: Servlet tạo nháp sau khi đã giữ chỗ thành công.
                    Appointment6 draftAppointment = new Appointment6();
                    draftAppointment.setCus(customer);
                    draftAppointment.setVehicle(selectedVehicle);
                    draftAppointment.setSlot(heldSlot);
                    draftAppointment.setRequestDescription(description);
                    c.put("draft", draftAppointment);
                    confirm(req, resp, c);
                } catch (ServletException | IOException | RuntimeException e) {
                    release(c);
                    throw e;
                }
            } catch (IllegalArgumentException e) {
                resp.setStatus(400);
                req.setAttribute("message", e.getMessage());
                form(req, resp, c);
            }
        }
    }

    private void form(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> c)
            throws ServletException, IOException {
        Customer6 customer = customer(req);
        LocalDate appointmentDate = (LocalDate) c.get("appointmentDate");
        req.setAttribute("context", c.get("id"));
        req.setAttribute("cus", customer);
        req.setAttribute("appointmentDate", appointmentDate.toString());
        req.setAttribute("selectedVehicle", c.get("vehicleId"));
        req.setAttribute("selectedSlot", c.get("slotId"));
        req.setAttribute("requestDescription", c.get("requestDescription"));
        req.setAttribute("draftAppointment", c.get("draft"));
        req.setAttribute("minDate", LocalDate.now(DAO6.zone()).toString());
        LocalDate maxDate = LocalDate.now(DAO6.zone()).plusDays(DAO6.number("GARAMAN_SLOT_DAYS", 30) - 1);
        req.setAttribute("maxDate", maxDate.toString());

        // Nạp xe và khung giờ; không truy vấn lại hồ sơ khách hàng trong luồng đặt lịch.
        try (VehicleDAO6 vehicleDAO = new VehicleDAO6(customer); SlotDAO6 slotDAO = new SlotDAO6()) {
            Vehicle6[] vehicleList = vehicleDAO.getVehiclesOfCustomer(customer.getId());
            req.setAttribute("vehicleList", vehicleList);
            AppointmentSlot6[] availableSlots = slotDAO.getAvailableSlots(appointmentDate);
            req.setAttribute("availableSlots", availableSlots);
        }
        view(req, resp, "customer/BookAppointmentView6");
    }

    static void confirm(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> c)
            throws ServletException, IOException {
        if (c.get("draft") == null && c.get("completed") == null) {
            throw new IllegalArgumentException("Chưa có lịch hẹn cần xác nhận. Vui lòng chọn lại thời gian.");
        }

        boolean completed = c.get("completed") != null;
        Object appointment = c.get("draft");
        if (completed) {
            appointment = c.get("completed");
        }
        req.setAttribute("context", c.get("id"));
        req.setAttribute("appointment", appointment);
        req.setAttribute("completed", completed);
        req.setAttribute("holdCode", c.get("holdCode"));
        req.setAttribute("holdExpiresAt", c.get("holdExpiresAt"));

        // TTL từ DB giúp bộ đếm không phụ thuộc đồng hồ tuyệt đối của máy khách.
        if (c.get("holdCode") != null && c.get("slotId") != null) {
            Integer slotId = (Integer) c.get("slotId");
            String holdCode = (String) c.get("holdCode");
            try (SlotDAO6 slotDAO = new SlotDAO6()) {
                long holdRemainingMillis = slotDAO.remainingHoldMillis(slotId, holdCode);
                req.setAttribute("holdRemainingMillis", holdRemainingMillis);
            }
        }
        view(req, resp, "customer/ConfirmView6");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> contexts(HttpSession session) {
        synchronized (session) {
            Map<String, Map<String, Object>> all =
                    (Map<String, Map<String, Object>>) session.getAttribute("bookingContexts");
            if (all == null) {
                all = new ConcurrentHashMap<>();
                session.setAttribute("bookingContexts", all);
            }
            return all;
        }
    }

    static Map<String, Object> context(HttpServletRequest req, boolean create) {
        Map<String, Map<String, Object>> all = contexts(req.getSession());
        String id = text(req, "context");
        if (id.isEmpty() && create) {
            synchronized (all) {
                if (all.size() >= 64) {
                    throw new IllegalArgumentException("Đã mở quá nhiều biểu mẫu trong phiên. Hãy đăng nhập lại.");
                }
                id = UUID.randomUUID().toString();
                Map<String, Object> c = new HashMap<>();
                c.put("id", id);
                c.put("appointmentDate", LocalDate.now(DAO6.zone()).plusDays(1));
                c.put("requestDescription", "");
                all.put(id, c);
                return c;
            }
        }
        Map<String, Object> c = all.get(id);
        if (c == null) {
            throw new IllegalArgumentException("Biểu mẫu không thuộc phiên hiện tại. Vui lòng mở lại đặt lịch.");
        }
        return c;
    }

    private static void release(Map<String, Object> c) {
        synchronized (c) {
            if (c.get("holdCode") != null && c.get("slotId") != null) {
                try (SlotDAO6 slotDAO = new SlotDAO6()) {
                    slotDAO.releaseSlot((Integer) c.get("slotId"), (String) c.get("holdCode"));
                }
            }
            c.remove("holdCode");
            c.remove("holdExpiresAt");
            c.remove("draft");
        }
    }

    public static void releaseAll(HttpSession session) {
        for (Map<String, Object> c : contexts(session).values()) {
            release(c);
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

    private static LocalDate date(HttpServletRequest req, String name) {
        try {
            return LocalDate.parse(text(req, name));
        } catch (java.time.DateTimeException e) {
            throw new IllegalArgumentException("Ngày không hợp lệ.");
        }
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
