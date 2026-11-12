# Hợp đồng đầy đủ từ bytecode đã build

Source SHA-256: `f0c79f6eb835cc5a8145068a161d6e15a97f682e65176925e039de5c334e736b`; [manifest](evidence/snapshot-after.json).

Sinh bằng scripts/ExportContracts.java; compiler bật -parameters, vì vậy tên/thứ tự tham số là tên source thật. Date là java.util.Date trừ LocalDate được ghi rõ. Runtime exceptions: mọi DAO SQL ném DataAccessException6; createAppointment ném BookingConflictException6 khi mất hold; validation ném IllegalArgumentException. Không tìm thấy object trả null, list trả mảng rỗng.

## garaman.dao.AppointmentDAO6

```java
public class AppointmentDAO6 extends garaman.dao.DAO6 {
  public AppointmentDAO6();
  public AppointmentDAO6(java.sql.Connection con);
  public garaman.entity.Appointment6 createAppointment(garaman.entity.Appointment6 appointment, java.lang.String holdCode);
   garaman.entity.Appointment6 findById(java.lang.Integer id) throws java.sql.SQLException;
  private void rollback(boolean tx, java.lang.Exception cause);
  private static java.lang.String temporaryCode();
}
```

## garaman.dao.CustomerDAO6

```java
public class CustomerDAO6 extends garaman.dao.DAO6 {
  public CustomerDAO6();
  public CustomerDAO6(java.sql.Connection con);
   garaman.entity.Customer6 findById(java.lang.Integer id) throws java.sql.SQLException;
  public garaman.entity.Customer6 getCustomerInfo(java.lang.String customerCode);
   garaman.entity.Customer6 readCustomer(java.sql.ResultSet rs) throws java.sql.SQLException;
}
```

## garaman.dao.DAO6

```java
public class DAO6 extends java.lang.Object implements java.lang.AutoCloseable {
  protected final java.sql.Connection con;
  protected final boolean owner;
  private static final java.util.Properties LOCAL;
  public DAO6();
  public DAO6(java.sql.Connection con);
  public void close();
  private static java.util.Properties load();
  public static java.lang.Integer number(java.lang.String key, int fallback);
  public java.util.Map<java.lang.String, java.lang.String> runtimeInfo();
  public static java.lang.String setting(java.lang.String key, java.lang.String fallback);
  private static void verifyTestConnection(java.sql.Connection connection) throws java.sql.SQLException;
  public static java.time.ZoneId zone();
}
```

## garaman.dao.InvoiceDAO6

```java
public class InvoiceDAO6 extends garaman.dao.DAO6 {
  public InvoiceDAO6();
  public InvoiceDAO6(java.sql.Connection con);
   garaman.entity.RepairOrder6 findOrderById(java.lang.Integer id) throws java.sql.SQLException;
  public java.lang.String getInvoiceCodeByUsage(java.lang.String itemType, java.lang.Integer usageId);
  public garaman.entity.Invoice6 getInvoiceDetail(java.lang.String invoiceCode);
}
```

## garaman.dao.MemberDAO6

```java
public class MemberDAO6 extends garaman.dao.DAO6 {
  private static final int ITERATIONS;
  public MemberDAO6();
  public MemberDAO6(java.sql.Connection con);
  public garaman.entity.Member6 authenticate(java.lang.String username, java.lang.String password);
  private static byte[] derive(java.lang.String password, byte[] salt, int iterations);
  static void fillMember(java.sql.ResultSet rs, garaman.entity.Member6 m) throws java.sql.SQLException;
   garaman.entity.Employee6 findEmployeeById(java.lang.Integer id) throws java.sql.SQLException;
   garaman.entity.SaleStaff6 findSaleStaffById(java.lang.Integer id) throws java.sql.SQLException;
   garaman.entity.Technician6 findTechnicianById(java.lang.Integer id) throws java.sql.SQLException;
  public static java.lang.String hashPassword(java.lang.String password);
  private static boolean verifyPassword(java.lang.String password, java.lang.String encodedHash);
}
```

## garaman.dao.PartUsageDAO6

```java
public class PartUsageDAO6 extends garaman.dao.DAO6 {
  public PartUsageDAO6();
  public PartUsageDAO6(java.sql.Connection con);
   garaman.entity.PartUsage6[] byOrder(java.lang.Integer orderId) throws java.sql.SQLException;
  private static java.util.Date date(java.time.LocalDate date);
  private static java.sql.Timestamp endExclusive(java.util.Date date);
  public garaman.entity.PartUsage6[] getAllPartUsageOfPart(java.lang.String partCode, java.util.Date startDate, java.util.Date endDate);
  public java.lang.Integer getTotalUsageCount(java.lang.String partCode);
  private static java.time.LocalDate localDate(java.util.Date date);
  private garaman.entity.PartUsage6 readUsage(java.sql.ResultSet rs) throws java.sql.SQLException;
  private static java.sql.Timestamp start(java.util.Date date);
}
```

## garaman.dao.RevenueDAO6

```java
public class RevenueDAO6 extends garaman.dao.DAO6 {
  public RevenueDAO6();
  public RevenueDAO6(java.sql.Connection con);
  private static java.util.Date date(java.time.LocalDate date);
  private static java.sql.Timestamp endExclusive(java.util.Date date);
  public garaman.entity.RevenueStats6[] getRevenueStatistics(java.util.Date startDate, java.util.Date endDate, java.lang.String statisticType);
  private static java.time.LocalDate localDate(java.util.Date date);
  private static java.sql.Timestamp start(java.util.Date date);
}
```

## garaman.dao.ServiceDAO6

```java
public class ServiceDAO6 extends garaman.dao.DAO6 {
  public ServiceDAO6();
  public ServiceDAO6(java.sql.Connection con);
   garaman.entity.Service6 findById(java.lang.Integer id) throws java.sql.SQLException;
  public garaman.entity.Service6 getServiceInfo(java.lang.String serviceCode);
  private garaman.entity.Service6 readService(java.sql.ResultSet rs) throws java.sql.SQLException;
}
```

## garaman.dao.ServiceUsageDAO6

```java
public class ServiceUsageDAO6 extends garaman.dao.DAO6 {
  public ServiceUsageDAO6();
  public ServiceUsageDAO6(java.sql.Connection con);
   garaman.entity.ServiceUsage6[] byOrder(java.lang.Integer orderId) throws java.sql.SQLException;
  private static java.util.Date date(java.time.LocalDate date);
  private static java.sql.Timestamp endExclusive(java.util.Date date);
  public garaman.entity.ServiceUsage6[] getAllServiceUsageOfService(java.lang.String serviceCode, java.util.Date startDate, java.util.Date endDate);
  public java.lang.Integer getTotalUsageCount(java.lang.String serviceCode);
  private static java.time.LocalDate localDate(java.util.Date date);
  private garaman.entity.ServiceUsage6 readUsage(java.sql.ResultSet rs) throws java.sql.SQLException;
  private static java.sql.Timestamp start(java.util.Date date);
}
```

## garaman.dao.SlotDAO6

```java
public class SlotDAO6 extends garaman.dao.DAO6 {
  public SlotDAO6();
  public SlotDAO6(java.sql.Connection con);
  private boolean changeHold(java.lang.Integer slotId, java.lang.String holdCode, boolean consume);
  public boolean consumeHold(java.lang.Integer slotId, java.lang.String holdCode);
  private static java.util.Date date(java.time.LocalDate date);
  private static java.sql.Timestamp endExclusive(java.util.Date date);
  public garaman.entity.AppointmentSlot6 findById(java.lang.Integer slotId);
  public garaman.entity.AppointmentSlot6[] getAvailableSlots(java.time.LocalDate appointmentDate);
  public garaman.entity.AppointmentSlot6 holdSlot(java.lang.Integer slotId);
  private static java.time.LocalDate localDate(java.util.Date date);
  public static void main(java.lang.String[] args);
  public java.lang.Integer prepareSlots();
  private garaman.entity.AppointmentSlot6 readSlot(java.sql.ResultSet rs) throws java.sql.SQLException;
  public boolean releaseSlot(java.lang.Integer slotId, java.lang.String holdCode);
  public long remainingHoldMillis(java.lang.Integer slotId, java.lang.String holdCode);
  private static java.sql.Timestamp start(java.util.Date date);
  public boolean validateHold(java.lang.Integer slotId, java.lang.String holdCode);
}
```

## garaman.dao.SparePartDAO6

```java
public class SparePartDAO6 extends garaman.dao.DAO6 {
  private static final java.lang.String SELECT;
  public SparePartDAO6();
  public SparePartDAO6(java.sql.Connection con);
   garaman.entity.SparePart6 findById(java.lang.Integer id) throws java.sql.SQLException;
  public garaman.entity.SparePart6 getPartInfo(java.lang.String partCode);
  private garaman.entity.SparePart6 readPart(java.sql.ResultSet rs) throws java.sql.SQLException;
}
```

## garaman.dao.VehicleBrandDAO6

```java
public class VehicleBrandDAO6 extends garaman.dao.DAO6 {
  public VehicleBrandDAO6();
  public VehicleBrandDAO6(java.sql.Connection con);
  public garaman.entity.VehicleBrand6 findById(java.lang.Integer id);
  public garaman.entity.VehicleBrand6[] getVehicleBrands();
}
```

## garaman.dao.VehicleDAO6

```java
public class VehicleDAO6 extends garaman.dao.DAO6 {
  public VehicleDAO6();
  public VehicleDAO6(java.sql.Connection con);
  public garaman.entity.Vehicle6 findById(java.lang.Integer vehicleId);
  public garaman.entity.Vehicle6[] getVehiclesOfCustomer(java.lang.Integer customerId);
  private garaman.entity.Vehicle6 readVehicle(java.sql.ResultSet rs) throws java.sql.SQLException;
  private void rollback(boolean tx, java.lang.Exception cause);
  public garaman.entity.Vehicle6 saveVehicle(garaman.entity.Vehicle6 vehicle, java.lang.Integer customerId);
}
```

## garaman.entity.Account6

```java
public class Account6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.String username;
  private java.lang.String password;
  private java.lang.String profileImage;
  private java.lang.String status;
  public Account6();
  public java.lang.Integer getId();
  public java.lang.String getPassword();
  public java.lang.String getProfileImage();
  public java.lang.String getStatus();
  public java.lang.String getUsername();
  public void setId(java.lang.Integer id);
  public void setPassword(java.lang.String password);
  public void setProfileImage(java.lang.String profileImage);
  public void setStatus(java.lang.String status);
  public void setUsername(java.lang.String username);
}
```

## garaman.entity.Appointment6

```java
public class Appointment6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.String appointmentCode;
  private java.lang.String requestDescription;
  private java.util.Date createAt;
  private java.lang.String status;
  private garaman.entity.SaleStaff6 ss;
  private garaman.entity.Vehicle6 vehicle;
  private garaman.entity.AppointmentSlot6 slot;
  private garaman.entity.Customer6 cus;
  public Appointment6();
  public java.lang.String getAppointmentCode();
  public java.util.Date getCreateAt();
  public garaman.entity.Customer6 getCus();
  public java.lang.Integer getId();
  public java.lang.String getRequestDescription();
  public garaman.entity.AppointmentSlot6 getSlot();
  public garaman.entity.SaleStaff6 getSs();
  public java.lang.String getStatus();
  public garaman.entity.Vehicle6 getVehicle();
  public void setAppointmentCode(java.lang.String appointmentCode);
  public void setCreateAt(java.util.Date createAt);
  public void setCus(garaman.entity.Customer6 cus);
  public void setId(java.lang.Integer id);
  public void setRequestDescription(java.lang.String requestDescription);
  public void setSlot(garaman.entity.AppointmentSlot6 slot);
  public void setSs(garaman.entity.SaleStaff6 ss);
  public void setStatus(java.lang.String status);
  public void setVehicle(garaman.entity.Vehicle6 vehicle);
}
```

## garaman.entity.AppointmentSlot6

```java
public class AppointmentSlot6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.util.Date startTime;
  private java.lang.String holdCode;
  private java.util.Date holdExpiresAt;
  private java.lang.String status;
  public AppointmentSlot6();
  public java.lang.String getHoldCode();
  public java.util.Date getHoldExpiresAt();
  public java.lang.Integer getId();
  public java.util.Date getStartTime();
  public java.lang.String getStatus();
  public void setHoldCode(java.lang.String holdCode);
  public void setHoldExpiresAt(java.util.Date holdExpiresAt);
  public void setId(java.lang.Integer id);
  public void setStartTime(java.util.Date startTime);
  public void setStatus(java.lang.String status);
}
```

## garaman.entity.Category6

```java
public class Category6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.String categoryName;
  public Category6();
  public java.lang.String getCategoryName();
  public java.lang.Integer getId();
  public void setCategoryName(java.lang.String categoryName);
  public void setId(java.lang.Integer id);
}
```

## garaman.entity.Customer6

```java
public class Customer6 extends garaman.entity.Member6 {
  private java.lang.String customerCode;
  public Customer6();
  public java.lang.String getCustomerCode();
  public void setCustomerCode(java.lang.String customerCode);
}
```

## garaman.entity.Employee6

```java
public class Employee6 extends garaman.entity.Member6 {
  private java.lang.String employeeCode;
  private java.lang.String jobTitle;
  public Employee6();
  public java.lang.String getEmployeeCode();
  public java.lang.String getJobTitle();
  public void setEmployeeCode(java.lang.String employeeCode);
  public void setJobTitle(java.lang.String jobTitle);
}
```

## garaman.entity.Invoice6

```java
public class Invoice6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.String invoiceCode;
  private java.math.BigDecimal totalAmount;
  private java.util.Date issueDate;
  private garaman.entity.RepairOrder6 order;
  private garaman.entity.Payment6 payment;
  public Invoice6();
  public java.lang.Integer getId();
  public java.lang.String getInvoiceCode();
  public java.util.Date getIssueDate();
  public garaman.entity.RepairOrder6 getOrder();
  public garaman.entity.Payment6 getPayment();
  public java.math.BigDecimal getTotalAmount();
  public void setId(java.lang.Integer id);
  public void setInvoiceCode(java.lang.String invoiceCode);
  public void setIssueDate(java.util.Date issueDate);
  public void setOrder(garaman.entity.RepairOrder6 order);
  public void setPayment(garaman.entity.Payment6 payment);
  public void setTotalAmount(java.math.BigDecimal totalAmount);
}
```

## garaman.entity.Manager6

```java
public class Manager6 extends garaman.entity.Employee6 {
  public Manager6();
}
```

## garaman.entity.Member6

```java
public class Member6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.String fullName;
  private java.lang.String phone;
  private java.lang.String email;
  private java.lang.String address;
  private garaman.entity.Account6 acc;
  public Member6();
  public garaman.entity.Account6 getAcc();
  public java.lang.String getAddress();
  public java.lang.String getEmail();
  public java.lang.String getFullName();
  public java.lang.Integer getId();
  public java.lang.String getPhone();
  public void setAcc(garaman.entity.Account6 acc);
  public void setAddress(java.lang.String address);
  public void setEmail(java.lang.String email);
  public void setFullName(java.lang.String fullName);
  public void setId(java.lang.Integer id);
  public void setPhone(java.lang.String phone);
}
```

## garaman.entity.PartBrand6

```java
public class PartBrand6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.String name;
  public PartBrand6();
  public java.lang.Integer getId();
  public java.lang.String getName();
  public void setId(java.lang.Integer id);
  public void setName(java.lang.String name);
}
```

## garaman.entity.PartUsage6

```java
public class PartUsage6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.Integer usedQuantity;
  private java.util.Date usageTime;
  private java.math.BigDecimal saleUnitPrice;
  private java.lang.String note;
  private garaman.entity.SparePart6 sparePart;
  private garaman.entity.Technician6 tech;
  private garaman.entity.RepairOrder6 order;
  public PartUsage6();
  public java.lang.Integer getId();
  public java.math.BigDecimal getLineAmount();
  public java.lang.String getNote();
  public garaman.entity.RepairOrder6 getOrder();
  public java.math.BigDecimal getSaleUnitPrice();
  public garaman.entity.SparePart6 getSparePart();
  public garaman.entity.Technician6 getTech();
  public java.util.Date getUsageTime();
  public java.lang.Integer getUsedQuantity();
  public void setId(java.lang.Integer id);
  public void setNote(java.lang.String note);
  public void setOrder(garaman.entity.RepairOrder6 order);
  public void setSaleUnitPrice(java.math.BigDecimal saleUnitPrice);
  public void setSparePart(garaman.entity.SparePart6 sparePart);
  public void setTech(garaman.entity.Technician6 tech);
  public void setUsageTime(java.util.Date usageTime);
  public void setUsedQuantity(java.lang.Integer usedQuantity);
}
```

## garaman.entity.Payment6

```java
public class Payment6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.math.BigDecimal paymentAmount;
  private java.util.Date paymentTime;
  private java.lang.String paymentMethod;
  private java.lang.String status;
  private garaman.entity.SaleStaff6 ss;
  private java.lang.String transactionReference;
  public Payment6();
  public java.lang.Integer getId();
  public java.math.BigDecimal getPaymentAmount();
  public java.lang.String getPaymentMethod();
  public java.util.Date getPaymentTime();
  public garaman.entity.SaleStaff6 getSs();
  public java.lang.String getStatus();
  public java.lang.String getTransactionReference();
  public void setId(java.lang.Integer id);
  public void setPaymentAmount(java.math.BigDecimal paymentAmount);
  public void setPaymentMethod(java.lang.String paymentMethod);
  public void setPaymentTime(java.util.Date paymentTime);
  public void setSs(garaman.entity.SaleStaff6 ss);
  public void setStatus(java.lang.String status);
  public void setTransactionReference(java.lang.String transactionReference);
}
```

## garaman.entity.RepairOrder6

```java
public class RepairOrder6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.String repairOrderCode;
  private java.util.Date createTime;
  private java.util.Date completionTime;
  private java.lang.String vehicleCondition;
  private java.lang.String repairDescription;
  private java.lang.String repairResult;
  private java.lang.String repairStatus;
  private garaman.entity.ServiceUsage6[] serviceUsage;
  private garaman.entity.PartUsage6[] partUsage;
  private garaman.entity.Vehicle6 vehicle;
  private garaman.entity.Customer6 cus;
  private garaman.entity.SaleStaff6 ss;
  private garaman.entity.Appointment6 appointment;
  public RepairOrder6();
  public garaman.entity.Appointment6 getAppointment();
  public java.util.Date getCompletionTime();
  public java.util.Date getCreateTime();
  public garaman.entity.Customer6 getCus();
  public java.lang.Integer getId();
  public garaman.entity.PartUsage6[] getPartUsage();
  public java.lang.String getRepairDescription();
  public java.lang.String getRepairOrderCode();
  public java.lang.String getRepairResult();
  public java.lang.String getRepairStatus();
  public garaman.entity.ServiceUsage6[] getServiceUsage();
  public garaman.entity.SaleStaff6 getSs();
  public garaman.entity.Vehicle6 getVehicle();
  public java.lang.String getVehicleCondition();
  public void setAppointment(garaman.entity.Appointment6 appointment);
  public void setCompletionTime(java.util.Date completionTime);
  public void setCreateTime(java.util.Date createTime);
  public void setCus(garaman.entity.Customer6 cus);
  public void setId(java.lang.Integer id);
  public void setPartUsage(garaman.entity.PartUsage6[] partUsage);
  public void setRepairDescription(java.lang.String repairDescription);
  public void setRepairOrderCode(java.lang.String repairOrderCode);
  public void setRepairResult(java.lang.String repairResult);
  public void setRepairStatus(java.lang.String repairStatus);
  public void setServiceUsage(garaman.entity.ServiceUsage6[] serviceUsage);
  public void setSs(garaman.entity.SaleStaff6 ss);
  public void setVehicle(garaman.entity.Vehicle6 vehicle);
  public void setVehicleCondition(java.lang.String vehicleCondition);
}
```

## garaman.entity.RevenueStats6

```java
public class RevenueStats6 extends java.lang.Object {
  private java.lang.String itemCode;
  private java.lang.String itemName;
  private java.lang.String itemType;
  private java.lang.Integer usageCount;
  private java.math.BigDecimal totalRevenue;
  public RevenueStats6();
  public java.lang.String getItemCode();
  public java.lang.String getItemName();
  public java.lang.String getItemType();
  public java.math.BigDecimal getTotalRevenue();
  public java.lang.Integer getUsageCount();
  public void setItemCode(java.lang.String itemCode);
  public void setItemName(java.lang.String itemName);
  public void setItemType(java.lang.String itemType);
  public void setTotalRevenue(java.math.BigDecimal totalRevenue);
  public void setUsageCount(java.lang.Integer usageCount);
}
```

## garaman.entity.SaleStaff6

```java
public class SaleStaff6 extends garaman.entity.Employee6 {
  public SaleStaff6();
}
```

## garaman.entity.Service6

```java
public class Service6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.String serviceCode;
  private java.lang.String serviceName;
  private java.lang.String description;
  private java.math.BigDecimal unitPrice;
  public Service6();
  public java.lang.String getDescription();
  public java.lang.Integer getId();
  public java.lang.String getServiceCode();
  public java.lang.String getServiceName();
  public java.math.BigDecimal getUnitPrice();
  public void setDescription(java.lang.String description);
  public void setId(java.lang.Integer id);
  public void setServiceCode(java.lang.String serviceCode);
  public void setServiceName(java.lang.String serviceName);
  public void setUnitPrice(java.math.BigDecimal unitPrice);
}
```

## garaman.entity.ServiceUsage6

```java
public class ServiceUsage6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.util.Date usageTime;
  private java.math.BigDecimal saleUnitPrice;
  private java.lang.String note;
  private garaman.entity.Service6 service;
  private garaman.entity.Technician6 tech;
  private garaman.entity.RepairOrder6 order;
  public ServiceUsage6();
  public java.lang.Integer getId();
  public java.lang.String getNote();
  public garaman.entity.RepairOrder6 getOrder();
  public java.math.BigDecimal getSaleUnitPrice();
  public garaman.entity.Service6 getService();
  public garaman.entity.Technician6 getTech();
  public java.util.Date getUsageTime();
  public void setId(java.lang.Integer id);
  public void setNote(java.lang.String note);
  public void setOrder(garaman.entity.RepairOrder6 order);
  public void setSaleUnitPrice(java.math.BigDecimal saleUnitPrice);
  public void setService(garaman.entity.Service6 service);
  public void setTech(garaman.entity.Technician6 tech);
  public void setUsageTime(java.util.Date usageTime);
}
```

## garaman.entity.SparePart6

```java
public class SparePart6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.String partCode;
  private java.lang.String partName;
  private java.lang.String description;
  private java.lang.String unit;
  private java.lang.String technicalSpecifications;
  private java.math.BigDecimal unitPrice;
  private java.lang.Integer stockQuantity;
  private java.lang.String image;
  private garaman.entity.PartBrand6 brand;
  private garaman.entity.Category6 cat;
  public SparePart6();
  public garaman.entity.PartBrand6 getBrand();
  public garaman.entity.Category6 getCat();
  public java.lang.String getDescription();
  public java.lang.Integer getId();
  public java.lang.String getImage();
  public java.lang.String getPartCode();
  public java.lang.String getPartName();
  public java.lang.Integer getStockQuantity();
  public java.lang.String getTechnicalSpecifications();
  public java.lang.String getUnit();
  public java.math.BigDecimal getUnitPrice();
  public void setBrand(garaman.entity.PartBrand6 brand);
  public void setCat(garaman.entity.Category6 cat);
  public void setDescription(java.lang.String description);
  public void setId(java.lang.Integer id);
  public void setImage(java.lang.String image);
  public void setPartCode(java.lang.String partCode);
  public void setPartName(java.lang.String partName);
  public void setStockQuantity(java.lang.Integer stockQuantity);
  public void setTechnicalSpecifications(java.lang.String technicalSpecifications);
  public void setUnit(java.lang.String unit);
  public void setUnitPrice(java.math.BigDecimal unitPrice);
}
```

## garaman.entity.Technician6

```java
public class Technician6 extends garaman.entity.Employee6 {
  public Technician6();
}
```

## garaman.entity.Vehicle6

```java
public class Vehicle6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.String licensePlate;
  private java.lang.String vehicleModel;
  private java.lang.Integer currentMileage;
  private java.lang.Integer manufactureYear;
  private garaman.entity.Customer6 cus;
  private garaman.entity.VehicleBrand6 brand;
  public Vehicle6();
  public garaman.entity.VehicleBrand6 getBrand();
  public java.lang.Integer getCurrentMileage();
  public garaman.entity.Customer6 getCus();
  public java.lang.Integer getId();
  public java.lang.String getLicensePlate();
  public java.lang.Integer getManufactureYear();
  public java.lang.String getVehicleCode();
  public java.lang.String getVehicleModel();
  public void setBrand(garaman.entity.VehicleBrand6 brand);
  public void setCurrentMileage(java.lang.Integer currentMileage);
  public void setCus(garaman.entity.Customer6 cus);
  public void setId(java.lang.Integer id);
  public void setLicensePlate(java.lang.String licensePlate);
  public void setManufactureYear(java.lang.Integer manufactureYear);
  public void setVehicleModel(java.lang.String vehicleModel);
}
```

## garaman.entity.VehicleBrand6

```java
public class VehicleBrand6 extends java.lang.Object {
  private java.lang.Integer id;
  private java.lang.String name;
  public VehicleBrand6();
  public java.lang.Integer getId();
  public java.lang.String getName();
  public void setId(java.lang.Integer id);
  public void setName(java.lang.String name);
}
```

## garaman.exception.BookingConflictException6

```java
public class BookingConflictException6 extends java.lang.RuntimeException {
  public BookingConflictException6(java.lang.String message);
}
```

## garaman.exception.DataAccessException6

```java
public class DataAccessException6 extends java.lang.RuntimeException {
  public DataAccessException6(java.lang.String message, java.lang.Throwable cause);
}
```

## garaman.filter.AppFilter6

```java
public class AppFilter6 extends java.lang.Object implements jakarta.servlet.Filter {
  private static final java.util.logging.Logger LOG;
  public AppFilter6();
  public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException;
  private static void error(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, int status, java.lang.String message) throws jakarta.servlet.ServletException, java.io.IOException;
  public void init(jakarta.servlet.FilterConfig config);
  private static void view(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.lang.String name) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.filter.AuthFilter6

```java
public class AuthFilter6 extends java.lang.Object implements jakarta.servlet.Filter {
  public AuthFilter6();
  public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain) throws jakarta.servlet.ServletException, java.io.IOException;
  private static void error(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, int status, java.lang.String message) throws jakarta.servlet.ServletException, java.io.IOException;
  private static void view(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.lang.String name) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.AppointmentServlet6

```java
public class AppointmentServlet6 extends jakarta.servlet.http.HttpServlet {
  public AppointmentServlet6();
  private static garaman.entity.Customer6 customer(jakarta.servlet.http.HttpServletRequest req);
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.String encode(java.lang.String value);
  private static void error(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, int status, java.lang.String message) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.String url(jakarta.servlet.http.HttpServletRequest req, java.lang.String route, java.util.Map<java.lang.String, ?> params);
  private static void view(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.lang.String name) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.BookingServlet6

```java
public class BookingServlet6 extends jakarta.servlet.http.HttpServlet {
  public BookingServlet6();
  static void confirm(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.util.Map<java.lang.String, java.lang.Object> c) throws jakarta.servlet.ServletException, java.io.IOException;
  static java.util.Map<java.lang.String, java.lang.Object> context(jakarta.servlet.http.HttpServletRequest req, boolean create);
  private static java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Object>> contexts(jakarta.servlet.http.HttpSession session);
  private static garaman.entity.Customer6 customer(jakarta.servlet.http.HttpServletRequest req);
  private static java.time.LocalDate date(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.String encode(java.lang.String value);
  private static void error(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, int status, java.lang.String message) throws jakarta.servlet.ServletException, java.io.IOException;
  private void form(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.util.Map<java.lang.String, java.lang.Object> c) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.Integer integer(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static void release(java.util.Map<java.lang.String, java.lang.Object> c);
  public static void releaseAll(jakarta.servlet.http.HttpSession session);
  private static java.lang.String text(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static java.lang.String url(jakarta.servlet.http.HttpServletRequest req, java.lang.String route, java.util.Map<java.lang.String, ?> params);
  private static void view(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.lang.String name) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.InvoiceServlet6

```java
public class InvoiceServlet6 extends jakarta.servlet.http.HttpServlet {
  public InvoiceServlet6();
  private static java.time.LocalDate date(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.String encode(java.lang.String value);
  private static void error(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, int status, java.lang.String message) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.Integer page(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static void reportFilter(jakarta.servlet.http.HttpServletRequest req);
  private static java.util.Map<java.lang.String, java.lang.Object> reportParams(jakarta.servlet.http.HttpServletRequest req);
  private static java.lang.String text(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static java.lang.String url(jakarta.servlet.http.HttpServletRequest req, java.lang.String route, java.util.Map<java.lang.String, ?> params);
  private static void view(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.lang.String name) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.LoginServlet6

```java
public class LoginServlet6 extends jakarta.servlet.http.HttpServlet {
  public LoginServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private static void error(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, int status, java.lang.String message) throws jakarta.servlet.ServletException, java.io.IOException;
  private void home(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws java.io.IOException;
  private void invalid(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.String text(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static void view(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.lang.String name) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.RevenueServlet6

```java
public class RevenueServlet6 extends jakarta.servlet.http.HttpServlet {
  public RevenueServlet6();
  private static java.lang.Integer clampPage(java.lang.Integer page, java.lang.Integer length);
  private static java.time.LocalDate date(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static java.util.Date date(java.time.LocalDate date);
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.String encode(java.lang.String value);
  private static void error(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, int status, java.lang.String message) throws jakarta.servlet.ServletException, java.io.IOException;
  private void filter(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private void list(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.time.LocalDate localDate(java.util.Date date);
  private static java.lang.Integer page(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static void reportFilter(jakarta.servlet.http.HttpServletRequest req);
  private static java.util.Map<java.lang.String, java.lang.Object> reportParams(jakarta.servlet.http.HttpServletRequest req);
  private static T[] slice(T[] all, java.lang.Integer page);
  private static java.lang.String text(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static java.lang.String url(jakarta.servlet.http.HttpServletRequest req, java.lang.String route, java.util.Map<java.lang.String, ?> params);
  private static void view(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.lang.String name) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.ServicePartServlet6

```java
public class ServicePartServlet6 extends jakarta.servlet.http.HttpServlet {
  public ServicePartServlet6();
  private static java.lang.Integer clampPage(java.lang.Integer page, java.lang.Integer length);
  private static java.time.LocalDate date(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static java.util.Date date(java.time.LocalDate date);
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.String encode(java.lang.String value);
  private static void error(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, int status, java.lang.String message) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.Integer integer(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static java.time.LocalDate localDate(java.util.Date date);
  private static java.lang.Integer page(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static void reportFilter(jakarta.servlet.http.HttpServletRequest req);
  private static java.util.Map<java.lang.String, java.lang.Object> reportParams(jakarta.servlet.http.HttpServletRequest req);
  private static T[] slice(T[] all, java.lang.Integer page);
  private static java.lang.String text(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static java.lang.String url(jakarta.servlet.http.HttpServletRequest req, java.lang.String route, java.util.Map<java.lang.String, ?> params);
  private static void view(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.lang.String name) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.TestRuntimeServlet6

```java
public class TestRuntimeServlet6 extends jakarta.servlet.http.HttpServlet {
  public TestRuntimeServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws java.io.IOException;
}
```

## garaman.servlet.VehicleServlet6

```java
public class VehicleServlet6 extends jakarta.servlet.http.HttpServlet {
  public VehicleServlet6();
  private static garaman.entity.Customer6 customer(jakarta.servlet.http.HttpServletRequest req);
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.String encode(java.lang.String value);
  private void form(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.util.Map<java.lang.String, java.lang.Object> c) throws jakarta.servlet.ServletException, java.io.IOException;
  private static java.lang.Integer integer(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static java.lang.String text(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static java.lang.String url(jakarta.servlet.http.HttpServletRequest req, java.lang.String route, java.util.Map<java.lang.String, ?> params);
  private static void view(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.lang.String name) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.util.BookingSessionListener6

```java
public class BookingSessionListener6 extends java.lang.Object implements jakarta.servlet.http.HttpSessionListener {
  public BookingSessionListener6();
  public void sessionDestroyed(jakarta.servlet.http.HttpSessionEvent event);
}
```

