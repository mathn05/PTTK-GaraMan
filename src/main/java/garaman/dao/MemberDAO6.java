package garaman.dao;

import garaman.entity.Account6;
import garaman.entity.Customer6;
import garaman.entity.Employee6;
import garaman.entity.Manager6;
import garaman.entity.Member6;
import garaman.entity.SaleStaff6;
import garaman.entity.Technician6;
import garaman.exception.DataAccessException6;
import java.security.*;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import java.sql.*;

public class MemberDAO6 extends DAO6 {
    public MemberDAO6() {
        super();
    }

    public MemberDAO6(Connection con) {
        super(con);
    }

    public Member6 authenticate(String username, String password) {
        String sql = """
                SELECT m.*, c.customerCode, e.jobTitle
                FROM tblMember6 m
                LEFT JOIN tblCustomer6 c ON c.tblMember6id = m.id
                LEFT JOIN tblEmployee6 e ON e.tblMember6id = m.id
                WHERE m.username = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next() || !"ACTIVE".equals(resultSet.getString("status"))
                        || !verifyPassword(password, resultSet.getString("password"))) {
                    return null;
                }
                String customerCode = resultSet.getString("customerCode");
                String jobTitle = resultSet.getString("jobTitle");
                if (customerCode != null && jobTitle == null) {
                    return new CustomerDAO6(con).readCustomer(resultSet);
                }
                if (customerCode == null && jobTitle != null) {
                    Integer memberId = resultSet.getObject("id", Integer.class);
                    return findEmployeeById(memberId);
                }
                // Thành viên có vai trò mâu thuẫn không được cấp quyền ngầm định.
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không xác thực được tài khoản", e);
        }
    }

    static void fillMember(ResultSet rs, Member6 m) throws SQLException {
        m.setId(rs.getObject("id", Integer.class));
        m.setFullName(rs.getString("fullName"));
        m.setPhone(rs.getString("phone"));
        m.setEmail(rs.getString("email"));
        m.setAddress(rs.getString("address"));
        if (rs.getString("username") != null) {
            Account6 account = new Account6();
            account.setId(m.getId());
            account.setUsername(rs.getString("username"));
            account.setPassword(rs.getString("password"));
            account.setProfileImage(rs.getString("profileImage"));
            account.setStatus(rs.getString("status"));
            m.setAcc(account);
        }
    }

    Employee6 findEmployeeById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            return null;
        }
        String sql = """
                SELECT m.*, e.employeeCode, e.jobTitle
                FROM tblMember6 m
                JOIN tblEmployee6 e ON e.tblMember6id = m.id
                WHERE m.id = ?
                """;
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                String jobTitle = resultSet.getString("jobTitle");
                Employee6 employee = switch (jobTitle) {
                    case "MANAGER" -> new Manager6();
                    case "SALE_STAFF" -> new SaleStaff6();
                    case "TECHNICIAN" -> new Technician6();
                    default -> new Employee6();
                };
                fillMember(resultSet, employee);
                employee.setEmployeeCode(resultSet.getString("employeeCode"));
                employee.setJobTitle(jobTitle);
                return employee;
            }
        }
    }

    SaleStaff6 findSaleStaffById(Integer id) throws SQLException {
        if (id == null) {
            return null;
        }
        Employee6 employee = findEmployeeById(id);
        if (!(employee instanceof SaleStaff6 saleStaff)) {
            throw new SQLException("FK nhân viên bán hàng không đúng vai trò");
        }
        return saleStaff;
    }

    Technician6 findTechnicianById(Integer id) throws SQLException {
        Employee6 employee = findEmployeeById(id);
        if (!(employee instanceof Technician6 technician)) {
            throw new SQLException("FK kỹ thuật viên không đúng vai trò");
        }
        return technician;
    }
    private static final int ITERATIONS=600_000;
    public static String hashPassword(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return "pbkdf2-sha256$v1$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }
    private static boolean verifyPassword(String password, String encodedHash) {
        if (password == null || encodedHash == null) return false;
        try {
            String[] parts = encodedHash.split("\\$");
            if (parts.length != 5 || !parts[0].equals("pbkdf2-sha256") || !parts[1].equals("v1")) return false;
            int iterations = Integer.parseInt(parts[2]);
            if (iterations < ITERATIONS || iterations > 2_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(parts[3]);
            byte[] expected = Base64.getDecoder().decode(parts[4]);
            return salt.length >= 16 && expected.length == 32 && MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException e) { return false; }
    }
    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (java.security.GeneralSecurityException e) { throw new IllegalStateException("PBKDF2 unavailable", e); }
        finally { spec.clearPassword(); }
    }
}
