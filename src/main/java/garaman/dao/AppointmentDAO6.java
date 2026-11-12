package garaman.dao;

import garaman.entity.*;
import garaman.exception.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.sql.*;

public class AppointmentDAO6 extends DAO6 {
    public AppointmentDAO6(){super();}
    public AppointmentDAO6(Connection con){super(con);}
    public Appointment6 createAppointment(Appointment6 appointment,String holdCode){
        if(appointment==null || appointment.getCus()==null || appointment.getVehicle()==null || appointment.getSlot()==null
            || appointment.getId()!=null || appointment.getCus().getId()==null || appointment.getVehicle().getId()==null || appointment.getSlot().getId()==null
            || appointment.getCus().getId()<=0 || appointment.getVehicle().getId()<=0 || appointment.getSlot().getId()<=0
            || appointment.getRequestDescription()==null || appointment.getRequestDescription().isBlank() || appointment.getRequestDescription().length()>255)
            throw new IllegalArgumentException("Thông tin lịch hẹn không hợp lệ.");
        boolean tx=false;
        try {
            if(!owner && con.getAutoCommit())throw new IllegalStateException("Connection mượn cần transaction caller");
            tx=owner;if(tx)con.setAutoCommit(false);
            // Row lock is acquired by the guarded UPDATE, then held until INSERT commits.
            if(!new SlotDAO6(con).consumeHold(appointment.getSlot().getId(),holdCode))throw new BookingConflictException6("Giữ chỗ đã hết hạn hoặc không còn hợp lệ. Vui lòng chọn lại khung giờ.");
            try(var ps=con.prepareStatement("SELECT id FROM tblVehicle6 WHERE id=? AND tblCustomer6tblMember6id=? FOR SHARE")) {
                ps.setInt(1,appointment.getVehicle().getId());ps.setInt(2,appointment.getCus().getId());
                try(var rs=ps.executeQuery()){if(!rs.next())throw new BookingConflictException6("Xe không thuộc khách hàng hiện tại.");}
            }
            String temporary=temporaryCode();int id;
            try(var ps=con.prepareStatement("INSERT INTO tblAppointment6(appointmentCode,requestDescription,createAt,status,tblEmployee6tblMember6id,tblVehicle6id,tblAppointmentSlot6id,tblCustomer6tblMember6id) VALUES(?,?,NOW(6),'PENDING',NULL,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1,temporary);ps.setString(2,appointment.getRequestDescription().trim());ps.setInt(3,appointment.getVehicle().getId());ps.setInt(4,appointment.getSlot().getId());ps.setInt(5,appointment.getCus().getId());ps.executeUpdate();
                try(var rs=ps.getGeneratedKeys()){if(!rs.next())throw new SQLException("Không có ID lịch hẹn");id=rs.getInt(1);}
            }
            // The placeholder satisfies NOT NULL/UNIQUE. No temporary code escapes the transaction.
            try(var ps=con.prepareStatement("UPDATE tblAppointment6 SET appointmentCode=? WHERE id=? AND appointmentCode=?")) {
                ps.setString(1,"LH"+String.format(Locale.ROOT,"%04d",id));ps.setInt(2,id);ps.setString(3,temporary);
                if(ps.executeUpdate()!=1)throw new SQLException("Cannot finalize appointment code");
            }
            Appointment6 saved=findById(id);
            if(tx)con.commit();return saved;
        }catch(SQLException e){rollback(tx,e);if(e.getErrorCode()==1062)throw new BookingConflictException6("Khung giờ đã có lịch hẹn hoạt động.");throw new DataAccessException6("Không lưu được lịch hẹn",e);}
        catch(RuntimeException e){rollback(tx,e);throw e;}
        finally{if(tx)try{con.setAutoCommit(true);}catch(SQLException e){throw new DataAccessException6("Không khôi phục được kết nối",e);}}
    }
    private void rollback(boolean tx,Exception cause){if(tx)try{con.rollback();}catch(SQLException e){cause.addSuppressed(e);}}

    Appointment6 findById(Integer id) throws SQLException {
        if(id==null||id<=0)return null;
        try(var ps=con.prepareStatement("SELECT * FROM tblAppointment6 WHERE id=?")) {
            ps.setInt(1,id);try(var rs=ps.executeQuery()) {
                if(!rs.next()) return null;
                Appointment6 a=new Appointment6();a.setId(id);a.setAppointmentCode(rs.getString("appointmentCode"));a.setCreateAt(rs.getTimestamp("createAt"));
                a.setRequestDescription(rs.getString("requestDescription"));a.setStatus(rs.getString("status"));a.setCus(new CustomerDAO6(con).findById(rs.getObject("tblCustomer6tblMember6id",Integer.class)));
                a.setVehicle(new VehicleDAO6(con).findById(rs.getObject("tblVehicle6id",Integer.class)));a.setSlot(new SlotDAO6(con).findById(rs.getObject("tblAppointmentSlot6id",Integer.class)));
                a.setSs(new MemberDAO6(con).findSaleStaffById(rs.getObject("tblEmployee6tblMember6id",Integer.class)));return a;
            }
        }
    }
    private static String temporaryCode() {
        UUID u=UUID.randomUUID();
        byte[] bytes=ByteBuffer.allocate(16).putLong(u.getMostSignificantBits()).putLong(u.getLeastSignificantBits()).array();
        return "~"+Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
