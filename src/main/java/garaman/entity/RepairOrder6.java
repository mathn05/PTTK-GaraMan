package garaman.entity;

import java.util.Date;

public class RepairOrder6 {
    private Integer id;
    private String repairOrderCode;
    private Date createTime;
    private Date completionTime;
    private String vehicleCondition;
    private String repairDescription;
    private String repairResult;
    private String repairStatus;
    private ServiceUsage6[] serviceUsage = new ServiceUsage6[0];
    private PartUsage6[] partUsage = new PartUsage6[0];
    private Vehicle6 vehicle;
    private Customer6 cus;
    private SaleStaff6 ss;
    private Appointment6 appointment;

    public RepairOrder6() {}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getRepairOrderCode() { return repairOrderCode; }
    public void setRepairOrderCode(String repairOrderCode) { this.repairOrderCode = repairOrderCode; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getCompletionTime() { return completionTime; }
    public void setCompletionTime(Date completionTime) { this.completionTime = completionTime; }
    public String getVehicleCondition() { return vehicleCondition; }
    public void setVehicleCondition(String vehicleCondition) { this.vehicleCondition = vehicleCondition; }
    public String getRepairDescription() { return repairDescription; }
    public void setRepairDescription(String repairDescription) { this.repairDescription = repairDescription; }
    public String getRepairResult() { return repairResult; }
    public void setRepairResult(String repairResult) { this.repairResult = repairResult; }
    public String getRepairStatus() { return repairStatus; }
    public void setRepairStatus(String repairStatus) { this.repairStatus = repairStatus; }
    public ServiceUsage6[] getServiceUsage() { return serviceUsage; }
    public void setServiceUsage(ServiceUsage6[] serviceUsage) { this.serviceUsage = serviceUsage; }
    public PartUsage6[] getPartUsage() { return partUsage; }
    public void setPartUsage(PartUsage6[] partUsage) { this.partUsage = partUsage; }
    public Vehicle6 getVehicle() { return vehicle; }
    public void setVehicle(Vehicle6 vehicle) { this.vehicle = vehicle; }
    public Customer6 getCus() { return cus; }
    public void setCus(Customer6 cus) { this.cus = cus; }
    public SaleStaff6 getSs() { return ss; }
    public void setSs(SaleStaff6 ss) { this.ss = ss; }
    public Appointment6 getAppointment() { return appointment; }
    public void setAppointment(Appointment6 appointment) { this.appointment = appointment; }
}
