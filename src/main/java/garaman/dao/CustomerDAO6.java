package garaman.dao;

import garaman.entity.Customer6;
import garaman.exception.DataAccessException6;
import java.sql.*;

public class CustomerDAO6 extends DAO6 {
    public CustomerDAO6() {super();}
    public CustomerDAO6(Connection con) {super(con);}
    public Customer6 getCustomerInfo(String customerCode) {
        try(var ps=con.prepareStatement("SELECT m.*,c.customerCode FROM tblCustomer6 c JOIN tblMember6 m ON m.id=c.tblMember6id WHERE c.customerCode=?")) {
            ps.setString(1,customerCode);try(var rs=ps.executeQuery()){return rs.next()?readCustomer(rs):null;}
        }catch(SQLException e){throw new DataAccessException6("Không tải được khách hàng",e);}
    }

    Customer6 readCustomer(ResultSet rs) throws SQLException {
        Customer6 c = new Customer6(); MemberDAO6.fillMember(rs,c); c.setCustomerCode(rs.getString("customerCode")); return c;
    }
    Customer6 findById(Integer id) throws SQLException {
        if(id==null||id<=0)return null;
        try (var ps=con.prepareStatement("SELECT m.*,c.customerCode FROM tblMember6 m JOIN tblCustomer6 c ON c.tblMember6id=m.id WHERE m.id=?")) {
            ps.setInt(1,id); try(var rs=ps.executeQuery()){return rs.next()?readCustomer(rs):null;}
        }
    }
}
