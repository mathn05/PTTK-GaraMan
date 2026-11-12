package garaman.entity;

import java.util.Date;

public class AppointmentSlot6 {
    private Integer id;
    private Date startTime;
    private String holdCode;
    private Date holdExpiresAt;
    private String status;

    public AppointmentSlot6() {}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public String getHoldCode() { return holdCode; }
    public void setHoldCode(String holdCode) { this.holdCode = holdCode; }
    public Date getHoldExpiresAt() { return holdExpiresAt; }
    public void setHoldExpiresAt(Date holdExpiresAt) { this.holdExpiresAt = holdExpiresAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
