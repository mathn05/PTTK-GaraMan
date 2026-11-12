package garaman.dao;

import garaman.entity.Customer6;
import garaman.exception.DataAccessException6;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO6 extends DAO6 {
    public CustomerDAO6() {
        super();
    }

    public CustomerDAO6(Connection con) {
        super(con);
    }

    public Customer6 getCustomerInfo(String customerCode) {
        String sql = """
                SELECT m.*, c.customerCode
                FROM tblCustomer6 c
                JOIN tblMember6 m ON m.id = c.tblMember6id
                WHERE c.customerCode = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, customerCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return readCustomer(resultSet);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được khách hàng", e);
        }
    }

    Customer6 readCustomer(ResultSet rs) throws SQLException {
        Customer6 customer = new Customer6();
        MemberDAO6.fillMember(rs, customer);
        customer.setCustomerCode(rs.getString("customerCode"));
        return customer;
    }

    Customer6 findById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            return null;
        }
        String sql = """
                SELECT m.*, c.customerCode
                FROM tblMember6 m
                JOIN tblCustomer6 c ON c.tblMember6id = m.id
                WHERE m.id = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return readCustomer(resultSet);
            }
        }
    }
}
