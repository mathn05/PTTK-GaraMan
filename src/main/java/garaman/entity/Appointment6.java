package garaman.entity;

import java.util.Date;

public class Appointment6 {
    private Integer id;
    private String appointmentCode;
    private String requestDescription;
    private Date createAt;
    private String status;
    private SaleStaff6 ss;
    private Vehicle6 vehicle;
    private AppointmentSlot6 slot;
    private Customer6 cus;

    public Appointment6() {}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getAppointmentCode() { return appointmentCode; }
    public void setAppointmentCode(String appointmentCode) { this.appointmentCode = appointmentCode; }
    public String getRequestDescription() { return requestDescription; }
    public void setRequestDescription(String requestDescription) { this.requestDescription = requestDescription; }
    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public SaleStaff6 getSs() { return ss; }
    public void setSs(SaleStaff6 ss) { this.ss = ss; }
    public Vehicle6 getVehicle() { return vehicle; }
    public void setVehicle(Vehicle6 vehicle) { this.vehicle = vehicle; }
    public AppointmentSlot6 getSlot() { return slot; }
    public void setSlot(AppointmentSlot6 slot) { this.slot = slot; }
    public Customer6 getCus() { return cus; }
    public void setCus(Customer6 cus) { this.cus = cus; }
}
