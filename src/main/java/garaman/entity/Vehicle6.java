package garaman.entity;

import java.util.Locale;

public class Vehicle6 {
    private Integer id;
    private String licensePlate;
    private String vehicleModel;
    private Integer currentMileage;
    private Integer manufactureYear;
    private Customer6 cus;
    private VehicleBrand6 brand;

    public Vehicle6() {}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public Integer getCurrentMileage() { return currentMileage; }
    public void setCurrentMileage(Integer currentMileage) { this.currentMileage = currentMileage; }
    public Integer getManufactureYear() { return manufactureYear; }
    public void setManufactureYear(Integer manufactureYear) { this.manufactureYear = manufactureYear; }
    public Customer6 getCus() { return cus; }
    public void setCus(Customer6 cus) { this.cus = cus; }
    public VehicleBrand6 getBrand() { return brand; }
    public void setBrand(VehicleBrand6 brand) { this.brand = brand; }
    public String getVehicleCode() { return id == null || id <= 0 ? null : "X" + String.format(Locale.ROOT, "%04d", id); }
}
