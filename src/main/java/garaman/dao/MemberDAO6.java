package garaman.dao;

import garaman.entity.*;
import garaman.exception.DataAccessException6;
import java.security.*;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import java.sql.*;

public class MemberDAO6 extends DAO6 {
    public MemberDAO6() {super();}
    public MemberDAO6(Connection con) {super(con);}
    public Member6 authenticate(String username,String password) {
        try(var ps=con.prepareStatement("SELECT m.*,c.customerCode,e.jobTitle FROM tblMember6 m LEFT JOIN tblCustomer6 c ON c.tblMember6id=m.id LEFT JOIN tblEmployee6 e ON e.tblMember6id=m.id WHERE m.username=?")) {
            ps.setString(1,username);try(var rs=ps.executeQuery()) {
                if(!rs.next() || !"ACTIVE".equals(rs.getString("status")) || !verifyPassword(password,rs.getString("password"))) return null;
                if(rs.getString("customerCode")!=null && rs.getString("jobTitle")==null) return new CustomerDAO6(con).readCustomer(rs);
                if(rs.getString("customerCode")==null && rs.getString("jobTitle")!=null) return findEmployeeById(rs.getObject("id",Integer.class));
                return null; // Ambiguous membership never grants a role implicitly.
            }
        }catch(SQLException e){throw new DataAccessException6("Không xác thực được tài khoản",e);}
    }

    static void fillMember(ResultSet rs, Member6 m) throws SQLException {
        m.setId(rs.getObject("id",Integer.class)); m.setFullName(rs.getString("fullName"));
        m.setPhone(rs.getString("phone")); m.setEmail(rs.getString("email")); m.setAddress(rs.getString("address"));
        if (rs.getString("username") != null) {
            Account6 a = new Account6(); a.setId(m.getId()); a.setUsername(rs.getString("username"));
            a.setPassword(rs.getString("password")); a.setProfileImage(rs.getString("profileImage")); a.setStatus(rs.getString("status")); m.setAcc(a);
        }
    }
    Employee6 findEmployeeById(Integer id) throws SQLException {
        if(id==null||id<=0)return null;
        try(var ps=con.prepareStatement("SELECT m.*,e.employeeCode,e.jobTitle FROM tblMember6 m JOIN tblEmployee6 e ON e.tblMember6id=m.id WHERE m.id=?")) {
            ps.setInt(1,id); try(var rs=ps.executeQuery()) {
                if(!rs.next()) return null;
                Employee6 e=switch(rs.getString("jobTitle")){case "MANAGER"->new Manager6();case "SALE_STAFF"->new SaleStaff6();case "TECHNICIAN"->new Technician6();default->new Employee6();};
                fillMember(rs,e);e.setEmployeeCode(rs.getString("employeeCode"));e.setJobTitle(rs.getString("jobTitle"));return e;
            }
        }
    }
    SaleStaff6 findSaleStaffById(Integer id) throws SQLException {
        if(id==null) return null;
        Employee6 e=findEmployeeById(id);
        if(!(e instanceof SaleStaff6 s)) throw new SQLException("FK nhân viên bán hàng không đúng vai trò");
        return s;
    }
    Technician6 findTechnicianById(Integer id) throws SQLException {
        Employee6 e=findEmployeeById(id);
        if(!(e instanceof Technician6 t)) throw new SQLException("FK kỹ thuật viên không đúng vai trò");
        return t;
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
