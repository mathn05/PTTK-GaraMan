package garaman.dao;

import garaman.entity.Customer6;
import garaman.entity.Vehicle6;
import garaman.entity.VehicleBrand6;
import garaman.exception.DataAccessException6;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Year;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class VehicleDAO6 extends DAO6 {
    private final Customer6 knownCustomer;

    public VehicleDAO6() {
        super();
        knownCustomer = null;
    }

    public VehicleDAO6(Connection con) {
        this(con, null);
    }

    /** Tái sử dụng khách hàng tin cậy do caller cung cấp; DAO không truy cập HttpSession. */
    public VehicleDAO6(Customer6 customer) {
        super();
        knownCustomer = Objects.requireNonNull(customer);
    }

    public VehicleDAO6(Connection con, Customer6 customer) {
        super(con);
        knownCustomer = customer;
    }

    public Vehicle6[] getVehiclesOfCustomer(Integer customerId) {
        if (customerId == null || customerId <= 0) {
            return new Vehicle6[0];
        }
        if (knownCustomer != null && !Objects.equals(customerId, knownCustomer.getId())) {
            throw new IllegalArgumentException("Khách hàng không khớp ngữ cảnh.");
        }
        String sql = """
                SELECT * FROM tblVehicle6
                WHERE tblCustomer6tblMember6id = ?
                ORDER BY id
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                ArrayList<Vehicle6> vehicles = new ArrayList<>();
                while (resultSet.next()) {
                    Vehicle6 vehicle = readVehicle(resultSet);
                    vehicles.add(vehicle);
                }
                return vehicles.toArray(Vehicle6[]::new);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được xe", e);
        }
    }

    public Vehicle6 findById(Integer vehicleId) {
        if (vehicleId == null || vehicleId <= 0) {
            return null;
        }
        String sql = """
                SELECT * FROM tblVehicle6
                WHERE id = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, vehicleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return readVehicle(resultSet);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được xe", e);
        }
    }

    public Vehicle6 saveVehicle(Vehicle6 vehicle, Integer customerId) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Thông tin xe không hợp lệ.");
        }
        if (knownCustomer != null && !Objects.equals(customerId, knownCustomer.getId())) {
            throw new IllegalArgumentException("Khách hàng không khớp ngữ cảnh.");
        }
        String plate = "";
        if (vehicle.getLicensePlate() != null) {
            plate = vehicle.getLicensePlate().trim().toUpperCase(Locale.ROOT).replaceAll("[\\s.\\-]", "");
        }
        String model = "";
        if (vehicle.getVehicleModel() != null) {
            model = vehicle.getVehicleModel().trim();
        }
        Integer year = vehicle.getManufactureYear();
        if (!plate.matches("[A-Z0-9]{1,20}") || model.isBlank() || model.length() > 255
                || vehicle.getBrand() == null || vehicle.getBrand().getId() == null
                || vehicle.getBrand().getId() <= 0 || customerId == null || customerId <= 0
                || (vehicle.getCurrentMileage() != null && vehicle.getCurrentMileage() < 0)
                || (year != null && (year < 1886 || year > Year.now(DAO6.zone()).getValue() + 1))) {
            throw new IllegalArgumentException("Thông tin xe không hợp lệ.");
        }
        if (vehicle.getId() != null) {
            throw new IllegalArgumentException("Xe mới phải chưa có ID.");
        }

        boolean tx = false;
        try {
            if (!owner && con.getAutoCommit()) {
                throw new IllegalStateException("Connection mượn cần transaction caller");
            }
            tx = owner;
            if (tx) {
                con.setAutoCommit(false);
            }

            String brandSql = """
                    SELECT id FROM tblVehicleBrand6
                    WHERE id = ?
                    """;
            try (PreparedStatement check = con.prepareStatement(brandSql)) {
                check.setInt(1, vehicle.getBrand().getId());
                try (ResultSet resultSet = check.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new IllegalArgumentException("Hãng xe không tồn tại.");
                    }
                }
            }

            String insertSql = """
                    INSERT INTO tblVehicle6 (
                        licensePlate, vehicleModel, manufactureYear,
                        tblCustomer6tblMember6id, tblVehicleBrand6id, currentMileage
                    )
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
            int id;
            try (PreparedStatement statement = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, plate);
                statement.setString(2, model);
                // Không biết năm/Km phải ghi SQL NULL, không chuyển thành 0.
                statement.setObject(3, year, Types.INTEGER);
                statement.setInt(4, customerId);
                statement.setInt(5, vehicle.getBrand().getId());
                statement.setObject(6, vehicle.getCurrentMileage(), Types.INTEGER);
                statement.executeUpdate();
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new SQLException("Không có ID xe mới");
                    }
                    id = generatedKeys.getInt(1);
                }
            }
            Vehicle6 savedVehicle = findById(id);
            if (tx) {
                con.commit();
            }
            return savedVehicle;
        } catch (SQLException e) {
            rollback(tx, e);
            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("Biển số xe đã tồn tại trong hệ thống.", e);
            }
            throw new DataAccessException6("Không lưu được xe", e);
        } catch (RuntimeException e) {
            rollback(tx, e);
            throw e;
        } finally {
            if (tx) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new DataAccessException6("Không khôi phục được kết nối", e);
                }
            }
        }
    }

    private void rollback(boolean tx, Exception cause) {
        if (tx) {
            try {
                con.rollback();
            } catch (SQLException e) {
                cause.addSuppressed(e);
            }
        }
    }

    private Vehicle6 readVehicle(ResultSet rs) throws SQLException {
        Vehicle6 vehicle = new Vehicle6();
        vehicle.setId(rs.getObject("id", Integer.class));
        vehicle.setLicensePlate(rs.getString("licensePlate"));
        vehicle.setVehicleModel(rs.getString("vehicleModel"));
        vehicle.setCurrentMileage(rs.getObject("currentMileage", Integer.class));
        vehicle.setManufactureYear(rs.getObject("manufactureYear", Integer.class));

        Integer ownerId = rs.getObject("tblCustomer6tblMember6id", Integer.class);
        if (knownCustomer == null) {
            // Báo cáo có thể cần hồ sơ của khách hàng khác với người đang đăng nhập.
            Customer6 customer = new CustomerDAO6(con).findById(ownerId);
            vehicle.setCus(customer);
        } else if (Objects.equals(ownerId, knownCustomer.getId())) {
            vehicle.setCus(knownCustomer);
        } else {
            // Chỉ giữ FK chủ xe thật để Servlet từ chối xe lạ, không đọc lại hồ sơ.
            Customer6 vehicleOwner = new Customer6();
            vehicleOwner.setId(ownerId);
            vehicle.setCus(vehicleOwner);
        }
        Integer brandId = rs.getObject("tblVehicleBrand6id", Integer.class);
        VehicleBrand6 brand = new VehicleBrandDAO6(con).findById(brandId);
        vehicle.setBrand(brand);
        return vehicle;
    }
}
