package garaman.dao;

import garaman.entity.VehicleBrand6;
import garaman.exception.DataAccessException6;
import java.sql.*;
import java.util.ArrayList;

public class VehicleBrandDAO6 extends DAO6 {
    public VehicleBrandDAO6(){super();}
    public VehicleBrandDAO6(Connection con){super(con);}
    public VehicleBrand6 findById(Integer id){
        if(id==null||id<=0)return null;
        try(var ps=con.prepareStatement("SELECT id,name FROM tblVehicleBrand6 WHERE id=?")){
            ps.setInt(1,id);try(var rs=ps.executeQuery()){if(!rs.next())return null;VehicleBrand6 b=new VehicleBrand6();b.setId(rs.getObject("id",Integer.class));b.setName(rs.getString("name"));return b;}
        }catch(SQLException e){throw new DataAccessException6("Không tải được hãng xe",e);}
    }
    public VehicleBrand6[] getVehicleBrands(){
        try(var ps=con.prepareStatement("SELECT * FROM tblVehicleBrand6 ORDER BY name,id");var rs=ps.executeQuery()) {
            var rows=new ArrayList<VehicleBrand6>();while(rs.next()){VehicleBrand6 b=new VehicleBrand6();b.setId(rs.getObject("id",Integer.class));b.setName(rs.getString("name"));rows.add(b);}return rows.toArray(VehicleBrand6[]::new);
        }catch(SQLException e){throw new DataAccessException6("Không tải được hãng xe",e);}
    }
}
