package garaman.dao;

import garaman.entity.VehicleBrand6;
import garaman.exception.DataAccessException6;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class VehicleBrandDAO6 extends DAO6 {
    public VehicleBrandDAO6() {
        super();
    }

    public VehicleBrandDAO6(Connection con) {
        super(con);
    }

    public VehicleBrand6 findById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        String sql = """
                SELECT id, name FROM tblVehicleBrand6
                WHERE id = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                VehicleBrand6 brand = new VehicleBrand6();
                brand.setId(resultSet.getObject("id", Integer.class));
                brand.setName(resultSet.getString("name"));
                return brand;
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được hãng xe", e);
        }
    }

    public VehicleBrand6[] getVehicleBrands() {
        String sql = """
                SELECT * FROM tblVehicleBrand6
                ORDER BY name, id
                """;
        try (PreparedStatement statement = con.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            ArrayList<VehicleBrand6> brands = new ArrayList<>();
            while (resultSet.next()) {
                VehicleBrand6 brand = new VehicleBrand6();
                brand.setId(resultSet.getObject("id", Integer.class));
                brand.setName(resultSet.getString("name"));
                brands.add(brand);
            }
            return brands.toArray(VehicleBrand6[]::new);
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được hãng xe", e);
        }
    }
}
