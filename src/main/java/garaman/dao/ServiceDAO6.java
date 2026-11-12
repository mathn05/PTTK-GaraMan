package garaman.dao;

import garaman.entity.Service6;
import garaman.exception.DataAccessException6;
import java.sql.*;

public class ServiceDAO6 extends DAO6 {
    public ServiceDAO6(){super();}
    public ServiceDAO6(Connection con){super(con);}
    public Service6 getServiceInfo(String serviceCode){
        try(var ps=con.prepareStatement("SELECT * FROM tblService6 WHERE serviceCode=?")) {
            ps.setString(1,serviceCode);try(var rs=ps.executeQuery()){return rs.next()?readService(rs):null;}
        }catch(SQLException e){throw new DataAccessException6("Không tải được dịch vụ",e);}
    }
    Service6 findById(Integer id) throws SQLException {
        if(id==null||id<=0)return null;
        try(var ps=con.prepareStatement("SELECT * FROM tblService6 WHERE id=?")){ps.setInt(1,id);try(var rs=ps.executeQuery()){return rs.next()?readService(rs):null;}}
    }

    private Service6 readService(ResultSet rs) throws SQLException {
        Service6 s=new Service6();s.setId(rs.getObject("id",Integer.class));s.setServiceCode(rs.getString("serviceCode"));s.setServiceName(rs.getString("serviceName"));
        s.setDescription(rs.getString("description"));s.setUnitPrice(rs.getBigDecimal("unitPrice"));return s;
    }
}
