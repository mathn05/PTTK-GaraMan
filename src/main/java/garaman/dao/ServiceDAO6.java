package garaman.dao;

import garaman.entity.Service6;
import garaman.exception.DataAccessException6;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServiceDAO6 extends DAO6 {
    public ServiceDAO6() {
        super();
    }

    public ServiceDAO6(Connection con) {
        super(con);
    }

    public Service6 getServiceInfo(String serviceCode) {
        String sql = """
                SELECT * FROM tblService6
                WHERE serviceCode = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, serviceCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return readService(resultSet);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được dịch vụ", e);
        }
    }

    Service6 findById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            return null;
        }
        String sql = """
                SELECT * FROM tblService6
                WHERE id = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return readService(resultSet);
            }
        }
    }

    private Service6 readService(ResultSet rs) throws SQLException {
        Service6 service = new Service6();
        service.setId(rs.getObject("id", Integer.class));
        service.setServiceCode(rs.getString("serviceCode"));
        service.setServiceName(rs.getString("serviceName"));
        service.setDescription(rs.getString("description"));
        service.setUnitPrice(rs.getBigDecimal("unitPrice"));
        return service;
    }
}
