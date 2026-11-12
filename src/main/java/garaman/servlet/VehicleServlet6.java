package garaman.servlet;

import garaman.dao.DAO6;
import garaman.dao.VehicleBrandDAO6;
import garaman.dao.VehicleDAO6;
import garaman.entity.Customer6;
import garaman.entity.Vehicle6;
import garaman.entity.VehicleBrand6;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Locale;
import java.util.Map;

@WebServlet("/vehicle")
public class VehicleServlet6 extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Map<String, Object> c = BookingServlet6.context(req, true);
        synchronized (c) {
            form(req, resp, c);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Map<String, Object> c = BookingServlet6.context(req, false);
        synchronized (c) {
            try {
                if (c.get("completed") != null || c.get("holdCode") != null) {
                    throw new IllegalArgumentException(
                            "Hãy quay lại và chỉnh sửa lịch hẹn trước khi thêm xe.");
                }
                if (!text(req, "action").isEmpty()) {
                    throw new IllegalArgumentException("Thao tác không hợp lệ.");
                }

                Integer brandId = integer(req, "vehicleBrand");
                if (brandId <= 0) {
                    throw new IllegalArgumentException("Vui lòng chọn hãng xe hợp lệ.");
                }
                VehicleBrand6 brand;
                try (VehicleBrandDAO6 brandDAO = new VehicleBrandDAO6()) {
                    brand = brandDAO.findById(brandId);
                }
                if (brand == null) {
                    throw new IllegalArgumentException("Hãng xe không tồn tại.");
                }

                String plate = text(req, "licensePlate");
                String model = text(req, "vehicleModel");
                Integer year = null;
                if (!text(req, "manufactureYear").isEmpty()) {
                    year = integer(req, "manufactureYear");
                }
                String normalizedPlate = plate.toUpperCase(Locale.ROOT).replaceAll("[\\s.\\-]", "");
                int maxYear = Year.now(DAO6.zone()).getValue() + 1;
                if (!normalizedPlate.matches("[A-Z0-9]{1,20}") || model.isBlank() || model.length() > 255
                        || (year != null && (year < 1886 || year > maxYear))) {
                    throw new IllegalArgumentException("Thông tin xe không hợp lệ.");
                }

                // Chỉ Servlet gán dữ liệu nghiệp vụ; Km chưa biết giữ nguyên null.
                Customer6 customer = customer(req);
                Vehicle6 vehicle = new Vehicle6();
                vehicle.setCus(customer);
                vehicle.setBrand(brand);
                vehicle.setLicensePlate(plate);
                vehicle.setVehicleModel(model);
                vehicle.setManufactureYear(year);
                try (VehicleDAO6 vehicleDAO = new VehicleDAO6(customer)) {
                    Vehicle6 savedVehicle = vehicleDAO.saveVehicle(vehicle, customer.getId());
                    c.put("vehicleId", savedVehicle.getId());
                }
                resp.sendRedirect(url(req, "/booking", Map.of("context", c.get("id"))));
            } catch (IllegalArgumentException e) {
                resp.setStatus(400);
                req.setAttribute("message", e.getMessage());
                form(req, resp, c);
            }
        }
    }

    private void form(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> c)
            throws ServletException, IOException {
        req.setAttribute("context", c.get("id"));
        req.setAttribute("cus", customer(req));
        req.setAttribute("maxYear", Year.now(DAO6.zone()).getValue() + 1);
        for (String name : new String[]{"licensePlate", "vehicleModel", "manufactureYear"}) {
            req.setAttribute(name, text(req, name));
        }

        // Chuỗi brand không hợp lệ không được đưa vào EL để ép kiểu số.
        Integer selected = null;
        try {
            Integer id = integer(req, "vehicleBrand");
            if (id > 0) {
                selected = id;
            }
        } catch (IllegalArgumentException ignored) {
            // Giữ lựa chọn rỗng khi hiển thị lại form lỗi.
        }
        req.setAttribute("selectedBrandId", selected);
        try (VehicleBrandDAO6 brandDAO = new VehicleBrandDAO6()) {
            VehicleBrand6[] vehicleBrandList = brandDAO.getVehicleBrands();
            req.setAttribute("vehicleBrandList", vehicleBrandList);
        }
        view(req, resp, "customer/AddNewVehicleView6");
    }

    private static Customer6 customer(HttpServletRequest req) {
        // Danh tính lấy từ session đăng nhập, không nhận mã khách hàng từ form.
        Object user = req.getSession().getAttribute("currentUser");
        if (!(user instanceof Customer6 c) || c.getId() == null || c.getId() <= 0) {
            throw new IllegalArgumentException("Phiên khách hàng không hợp lệ.");
        }
        return c;
    }

    private static Integer integer(HttpServletRequest req, String name) {
        try {
            return Integer.parseInt(text(req, name));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá trị " + name + " không hợp lệ.");
        }
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
