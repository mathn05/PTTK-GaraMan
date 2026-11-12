package garaman.dao;

import garaman.entity.Vehicle6;
import garaman.exception.DataAccessException6;
import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.Locale;

public class VehicleDAO6 extends DAO6 {
    public VehicleDAO6(){super();}
    public VehicleDAO6(Connection con){super(con);}
    public Vehicle6[] getVehiclesOfCustomer(Integer customerId){
        if(customerId==null||customerId<=0)return new Vehicle6[0];
        try(var ps=con.prepareStatement("SELECT * FROM tblVehicle6 WHERE tblCustomer6tblMember6id=? ORDER BY id")) {
            ps.setInt(1,customerId);try(var rs=ps.executeQuery()){var rows=new ArrayList<Vehicle6>();while(rs.next())rows.add(readVehicle(rs));return rows.toArray(Vehicle6[]::new);}
        }catch(SQLException e){throw new DataAccessException6("Không tải được xe",e);}
    }
    public Vehicle6 findById(Integer vehicleId){
        if(vehicleId==null||vehicleId<=0)return null;
        try(var ps=con.prepareStatement("SELECT * FROM tblVehicle6 WHERE id=?")) {
            ps.setInt(1,vehicleId);try(var rs=ps.executeQuery()){return rs.next()?readVehicle(rs):null;}
        }catch(SQLException e){throw new DataAccessException6("Không tải được xe",e);}
    }
    public Vehicle6 saveVehicle(Vehicle6 vehicle,Integer customerId){
        if(vehicle==null)throw new IllegalArgumentException("Thông tin xe không hợp lệ.");
        String plate=vehicle.getLicensePlate()==null?"":vehicle.getLicensePlate().trim().toUpperCase(Locale.ROOT).replaceAll("[\\s.\\-]","");
        String model=vehicle.getVehicleModel()==null?"":vehicle.getVehicleModel().trim();
        Integer year=vehicle.getManufactureYear();
        if(!plate.matches("[A-Z0-9]{1,20}") || model.isBlank() || model.length()>255 || vehicle.getBrand()==null
            || vehicle.getBrand().getId()==null || vehicle.getBrand().getId()<=0 || (customerId==null||customerId<=0)
            || (vehicle.getCurrentMileage()!=null && vehicle.getCurrentMileage()<0)
            || (year!=null && (year<1886 || year>Year.now(DAO6.zone()).getValue()+1))) throw new IllegalArgumentException("Thông tin xe không hợp lệ.");
        if(vehicle.getId()!=null)throw new IllegalArgumentException("Xe mới phải chưa có ID.");
        boolean tx=false;
        try {
            if(!owner && con.getAutoCommit()) throw new IllegalStateException("Connection mượn cần transaction caller");
            tx=owner; if(tx)con.setAutoCommit(false);
            try(var check=con.prepareStatement("SELECT id FROM tblVehicleBrand6 WHERE id=?")) {
                check.setInt(1,vehicle.getBrand().getId());try(var rs=check.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Hãng xe không tồn tại.");}
            }
            int id;
            try(var ps=con.prepareStatement("INSERT INTO tblVehicle6(licensePlate,vehicleModel,manufactureYear,tblCustomer6tblMember6id,tblVehicleBrand6id,currentMileage) VALUES(?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1,plate);ps.setString(2,model);ps.setObject(3,year,Types.INTEGER);ps.setInt(4,customerId);ps.setInt(5,vehicle.getBrand().getId());ps.setObject(6,vehicle.getCurrentMileage(),Types.INTEGER);ps.executeUpdate();
                try(var rs=ps.getGeneratedKeys()){if(!rs.next())throw new SQLException("Không có ID xe mới");id=rs.getInt(1);}
            }
            Vehicle6 saved=findById(id);if(tx)con.commit();return saved;
        }catch(SQLException e){
            rollback(tx,e);
            if(e.getErrorCode()==1062)throw new IllegalArgumentException("Biển số xe đã tồn tại trong hệ thống.",e);
            throw new DataAccessException6("Không lưu được xe",e);
        }catch(RuntimeException e){rollback(tx,e);throw e;}
        finally{if(tx)try{con.setAutoCommit(true);}catch(SQLException e){throw new DataAccessException6("Không khôi phục được kết nối",e);}}
    }
    private void rollback(boolean tx,Exception cause){if(tx)try{con.rollback();}catch(SQLException e){cause.addSuppressed(e);}}

    private Vehicle6 readVehicle(ResultSet rs) throws SQLException {
        Vehicle6 v=new Vehicle6();v.setId(rs.getObject("id",Integer.class));
        v.setLicensePlate(rs.getString("licensePlate"));v.setVehicleModel(rs.getString("vehicleModel"));
        v.setCurrentMileage(rs.getObject("currentMileage",Integer.class));v.setManufactureYear(rs.getObject("manufactureYear",Integer.class));
        v.setCus(new CustomerDAO6(con).findById(rs.getObject("tblCustomer6tblMember6id",Integer.class)));
        v.setBrand(new VehicleBrandDAO6(con).findById(rs.getObject("tblVehicleBrand6id",Integer.class)));
        return v;
    }
}
