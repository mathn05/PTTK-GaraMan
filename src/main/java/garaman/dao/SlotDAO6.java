package garaman.dao;

import garaman.entity.AppointmentSlot6;
import garaman.exception.DataAccessException6;
import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
import java.time.*;
import java.util.ArrayList;
import java.util.UUID;

public class SlotDAO6 extends DAO6 {
    public SlotDAO6(){super();}
    public SlotDAO6(Connection con){super(con);}
    public AppointmentSlot6[] getAvailableSlots(LocalDate appointmentDate){
        if(appointmentDate==null)throw new IllegalArgumentException("Ngày hẹn không hợp lệ.");
        String sql="SELECT s.* FROM tblAppointmentSlot6 s WHERE startTime>=? AND startTime<? AND startTime>NOW(6) AND (status='AVAILABLE' OR (status='HELD' AND holdExpiresAt<=NOW(6))) AND NOT EXISTS(SELECT 1 FROM tblAppointment6 a WHERE a.activeSlotId=s.id) ORDER BY startTime,id";
        try(var ps=con.prepareStatement(sql)) {
            ps.setTimestamp(1,new Timestamp(date(appointmentDate).getTime()));ps.setTimestamp(2,new Timestamp(date(appointmentDate.plusDays(1)).getTime()));
            try(var rs=ps.executeQuery()){var rows=new ArrayList<AppointmentSlot6>();while(rs.next())rows.add(readSlot(rs));return rows.toArray(AppointmentSlot6[]::new);}
        }catch(SQLException e){throw new DataAccessException6("Không tải được khung giờ",e);}
    }
    public AppointmentSlot6 findById(Integer slotId){
        if(slotId==null||slotId<=0)return null;
        try(var ps=con.prepareStatement("SELECT * FROM tblAppointmentSlot6 WHERE id=?")) {
            ps.setInt(1,slotId);try(var rs=ps.executeQuery()){return rs.next()?readSlot(rs):null;}
        }catch(SQLException e){throw new DataAccessException6("Không tải được khung giờ",e);}
    }
    public AppointmentSlot6 holdSlot(Integer slotId){
        if(slotId==null||slotId<=0)return null;
        String token=UUID.randomUUID().toString();
        String sql="UPDATE tblAppointmentSlot6 s SET status='HELD',holdCode=?,holdExpiresAt=DATE_ADD(NOW(6),INTERVAL 3 MINUTE) WHERE id=? AND startTime>NOW(6) AND (status='AVAILABLE' OR (status='HELD' AND holdExpiresAt<=NOW(6))) AND NOT EXISTS(SELECT 1 FROM tblAppointment6 a WHERE a.activeSlotId=s.id)";
        try(var ps=con.prepareStatement(sql)) {
            ps.setString(1,token);ps.setInt(2,slotId);
            if(ps.executeUpdate()!=1)return null;
            // Read only our token even if another connection acts immediately afterward.
            try(var read=con.prepareStatement("SELECT * FROM tblAppointmentSlot6 WHERE id=? AND holdCode=?")) {
                read.setInt(1,slotId);read.setString(2,token);try(var rs=read.executeQuery()){return rs.next()?readSlot(rs):null;}
            }
        }catch(SQLException e){throw new DataAccessException6("Không giữ được khung giờ",e);}
    }
    public boolean validateHold(Integer slotId,String holdCode){
        if(slotId==null||slotId<=0||holdCode==null)return false;
        try(var ps=con.prepareStatement("SELECT id FROM tblAppointmentSlot6 WHERE id=? AND status='HELD' AND holdCode=? AND holdExpiresAt>NOW(6) AND startTime>NOW(6)")) {
            ps.setInt(1,slotId);ps.setString(2,holdCode);try(var rs=ps.executeQuery()){return rs.next();}
        }catch(SQLException e){throw new DataAccessException6("Không kiểm tra được giữ chỗ",e);}
    }
    public long remainingHoldMillis(Integer slotId,String holdCode){
        if(slotId==null||slotId<=0||holdCode==null)return 0L;
        String sql="SELECT GREATEST(0,TIMESTAMPDIFF(MICROSECOND,NOW(6),holdExpiresAt) DIV 1000) FROM tblAppointmentSlot6 WHERE id=? AND status='HELD' AND holdCode=? AND startTime>NOW(6)";
        try(var ps=con.prepareStatement(sql)){ps.setInt(1,slotId);ps.setString(2,holdCode);try(var rs=ps.executeQuery()){return rs.next()?rs.getLong(1):0L;}}
        catch(SQLException e){throw new DataAccessException6("Không đọc được thời hạn giữ chỗ",e);}
    }
    public boolean releaseSlot(Integer slotId,String holdCode){
        if(slotId==null||slotId<=0||holdCode==null)return false;return changeHold(slotId,holdCode,false);}
    public boolean consumeHold(Integer slotId,String holdCode){
        if(slotId==null||slotId<=0||holdCode==null)return false;
        try{if(con.getAutoCommit())throw new IllegalStateException("Consume hold bắt buộc trong transaction lưu lịch");}
        catch(SQLException e){throw new DataAccessException6("Không đọc được trạng thái transaction",e);}
        return changeHold(slotId,holdCode,true);
    }
    private boolean changeHold(Integer slotId,String holdCode,boolean consume){
        String sql="UPDATE tblAppointmentSlot6 SET status=?,holdCode=NULL,holdExpiresAt=NULL WHERE id=? AND status='HELD' AND holdCode=?"+(consume?" AND holdExpiresAt>NOW(6) AND startTime>NOW(6)":"");
        try(var ps=con.prepareStatement(sql)){ps.setString(1,consume?"BOOKED":"AVAILABLE");ps.setInt(2,slotId);ps.setString(3,holdCode);return ps.executeUpdate()==1;}
        catch(SQLException e){throw new DataAccessException6("Không cập nhật được giữ chỗ",e);}
    }

    private AppointmentSlot6 readSlot(ResultSet rs) throws SQLException {
        AppointmentSlot6 s=new AppointmentSlot6();s.setId(rs.getObject("id",Integer.class));s.setStartTime(rs.getTimestamp("startTime"));
        s.setHoldCode(rs.getString("holdCode"));s.setHoldExpiresAt(rs.getTimestamp("holdExpiresAt"));s.setStatus(rs.getString("status"));return s;
    }
    public Integer prepareSlots(){
        int days=DAO6.number("GARAMAN_SLOT_DAYS",30),open=DAO6.number("GARAMAN_OPEN_HOUR",8),close=DAO6.number("GARAMAN_CLOSE_HOUR",17),breakStart=DAO6.number("GARAMAN_BREAK_START",12),breakEnd=DAO6.number("GARAMAN_BREAK_END",13),minutes=DAO6.number("GARAMAN_SLOT_MINUTES",60);
        if(days<1||days>366||minutes<1||minutes>240||open<0||close>24||open>=close||breakStart<open||breakEnd>close||breakStart>breakEnd)throw new IllegalArgumentException("Cấu hình lịch làm việc không hợp lệ");
        try(var ps=con.prepareStatement("INSERT INTO tblAppointmentSlot6(startTime,status) VALUES(?,'AVAILABLE') ON DUPLICATE KEY UPDATE startTime=startTime")) {
            LocalDate today=LocalDate.now(DAO6.zone());int inserted=0;
            for(int d=0;d<days;d++)for(int m=open*60;m+minutes<=close*60;m+=minutes) {
                if(m<breakEnd*60 && m+minutes>breakStart*60)continue;
                var t=today.plusDays(d).atStartOfDay().plusMinutes(m).atZone(DAO6.zone());
                if(!t.toInstant().isAfter(Instant.now()))continue;
                ps.setTimestamp(1,Timestamp.from(t.toInstant()));inserted+=ps.executeUpdate();
            }return inserted;
        }catch(SQLException e){throw new DataAccessException6("Không chuẩn bị được slot",e);}
    }
    public static void main(String[] args){
        try(var setup=new SlotDAO6()){System.out.println("Slots prepared idempotently, changed rows="+setup.prepareSlots());}
        finally{com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();}
    }

    private static Date date(LocalDate date) { return Date.from(date.atStartOfDay(DAO6.zone()).toInstant()); }
    private static LocalDate localDate(Date date) { return date.toInstant().atZone(DAO6.zone()).toLocalDate(); }
    private static Timestamp start(Date date) { return new Timestamp(date(localDate(date)).getTime()); }
    private static Timestamp endExclusive(Date date) { return new Timestamp(date(localDate(date).plusDays(1)).getTime()); }
}
