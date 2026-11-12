# Hợp đồng đầy đủ từ bytecode đã build

Sinh bằng scripts/ExportContracts.java; compiler bật -parameters, vì vậy tên/thứ tự tham số là tên source thật. Date là java.util.Date trừ LocalDate được ghi rõ. Runtime exceptions: mọi DAO SQL ném DataAccessException6; createAppointment ném BookingConflictException6 khi mất hold; validation ném IllegalArgumentException. Không tìm thấy object trả null, list trả mảng rỗng.

## garaman.dao.AppointmentDAO6

```java
public class AppointmentDAO6 extends garaman.dao.DAO6 {
  public AppointmentDAO6();
  public AppointmentDAO6(java.sql.Connection con);
  public garaman.entity.Appointment6 createAppointment(garaman.entity.Appointment6 appointment, java.lang.String holdCode);
  private void rollback(boolean tx, java.lang.Exception cause);
}
```

## garaman.dao.CodeSequenceDAO6

```java
public class CodeSequenceDAO6 extends garaman.dao.DAO6 {
  public CodeSequenceDAO6(java.sql.Connection con);
  public java.lang.String nextCode(java.lang.String prefix);
}
```

## garaman.dao.CustomerDAO6

```java
public class CustomerDAO6 extends garaman.dao.DAO6 {
  public CustomerDAO6();
  public CustomerDAO6(java.sql.Connection con);
  public garaman.entity.Customer6 getCustomerInfo(java.lang.String customerCode);
}
```

## garaman.dao.DAO6

```java
public class DAO6 extends java.lang.Object implements java.lang.AutoCloseable {
  protected final java.sql.Connection con;
  protected final boolean owner;
  public DAO6();
  public DAO6(java.sql.Connection con);
  public void close();
}
```

## garaman.dao.InvoiceDAO6

```java
public class InvoiceDAO6 extends garaman.dao.DAO6 {
  public InvoiceDAO6();
  public InvoiceDAO6(java.sql.Connection con);
  public java.lang.String getInvoiceCodeByUsage(java.lang.String itemType, int usageId);
  public garaman.entity.Invoice6 getInvoiceDetail(java.lang.String invoiceCode);
}
```

## garaman.dao.MemberDAO6

```java
public class MemberDAO6 extends garaman.dao.DAO6 {
  public MemberDAO6();
  public MemberDAO6(java.sql.Connection con);
  public garaman.entity.Member6 authenticate(java.lang.String username, java.lang.String password);
}
```

## garaman.dao.PartUsageDAO6

```java
public class PartUsageDAO6 extends garaman.dao.DAO6 {
  public PartUsageDAO6();
  public PartUsageDAO6(java.sql.Connection con);
   garaman.entity.PartUsage6[] byOrder(int orderId) throws java.sql.SQLException;
  public garaman.entity.PartUsage6[] getAllPartUsageOfPart(java.lang.String partCode, java.util.Date startDate, java.util.Date endDate);
  public int getTotalUsageCount(java.lang.String partCode);
}
```

## garaman.dao.RevenueDAO6

```java
public class RevenueDAO6 extends garaman.dao.DAO6 {
  public RevenueDAO6();
  public RevenueDAO6(java.sql.Connection con);
  public garaman.entity.RevenueStats6[] getRevenueStatistics(java.util.Date startDate, java.util.Date endDate, java.lang.String statisticType);
}
```

## garaman.dao.RowMapper6

```java
final class RowMapper6 extends java.lang.Object {
  private RowMapper6();
  static garaman.entity.Appointment6 appointment(java.sql.Connection con, int id) throws java.sql.SQLException;
  static garaman.entity.Customer6 customer(java.sql.Connection con, int id) throws java.sql.SQLException;
  static garaman.entity.Customer6 customer(java.sql.ResultSet rs) throws java.sql.SQLException;
  static garaman.entity.Employee6 employee(java.sql.Connection con, int id) throws java.sql.SQLException;
  static void member(java.sql.ResultSet rs, garaman.entity.Member6 m) throws java.sql.SQLException;
  static garaman.entity.RepairOrder6 order(java.sql.Connection con, int id) throws java.sql.SQLException;
  static garaman.entity.SparePart6 part(java.sql.ResultSet rs) throws java.sql.SQLException;
  static garaman.entity.SaleStaff6 saleStaff(java.sql.Connection con, int id) throws java.sql.SQLException;
  static garaman.entity.Service6 service(java.sql.ResultSet rs) throws java.sql.SQLException;
  static garaman.entity.AppointmentSlot6 slot(java.sql.ResultSet rs) throws java.sql.SQLException;
  static garaman.entity.Technician6 technician(java.sql.Connection con, int id) throws java.sql.SQLException;
  static garaman.entity.Vehicle6 vehicle(java.sql.Connection con, java.sql.ResultSet rs) throws java.sql.SQLException;
}
```

## garaman.dao.ServiceDAO6

```java
public class ServiceDAO6 extends garaman.dao.DAO6 {
  public ServiceDAO6();
  public ServiceDAO6(java.sql.Connection con);
   garaman.entity.Service6 findById(int id) throws java.sql.SQLException;
  public garaman.entity.Service6 getServiceInfo(java.lang.String serviceCode);
}
```

## garaman.dao.ServiceUsageDAO6

```java
public class ServiceUsageDAO6 extends garaman.dao.DAO6 {
  public ServiceUsageDAO6();
  public ServiceUsageDAO6(java.sql.Connection con);
   garaman.entity.ServiceUsage6[] byOrder(int orderId) throws java.sql.SQLException;
  public garaman.entity.ServiceUsage6[] getAllServiceUsageOfService(java.lang.String serviceCode, java.util.Date startDate, java.util.Date endDate);
  public int getTotalUsageCount(java.lang.String serviceCode);
}
```

## garaman.dao.SlotDAO6

```java
public class SlotDAO6 extends garaman.dao.DAO6 {
  public SlotDAO6();
  public SlotDAO6(java.sql.Connection con);
  private boolean changeHold(int slotId, java.lang.String holdCode, boolean consume);
  public boolean consumeHold(int slotId, java.lang.String holdCode);
  public garaman.entity.AppointmentSlot6 findById(int slotId);
  public garaman.entity.AppointmentSlot6[] getAvailableSlots(java.time.LocalDate appointmentDate);
  public garaman.entity.AppointmentSlot6 holdSlot(int slotId);
  public boolean releaseSlot(int slotId, java.lang.String holdCode);
  public boolean validateHold(int slotId, java.lang.String holdCode);
}
```

## garaman.dao.SparePartDAO6

```java
public class SparePartDAO6 extends garaman.dao.DAO6 {
  private static final java.lang.String SELECT;
  public SparePartDAO6();
  public SparePartDAO6(java.sql.Connection con);
   garaman.entity.SparePart6 findById(int id) throws java.sql.SQLException;
  public garaman.entity.SparePart6 getPartInfo(java.lang.String partCode);
}
```

## garaman.dao.UsageMapper6

```java
final class UsageMapper6 extends java.lang.Object {
  private UsageMapper6();
  static garaman.entity.PartUsage6 part(java.sql.Connection con, java.sql.ResultSet rs) throws java.sql.SQLException;
  static garaman.entity.ServiceUsage6 service(java.sql.Connection con, java.sql.ResultSet rs) throws java.sql.SQLException;
}
```

## garaman.dao.VehicleBrandDAO6

```java
public class VehicleBrandDAO6 extends garaman.dao.DAO6 {
  public VehicleBrandDAO6();
  public VehicleBrandDAO6(java.sql.Connection con);
  public garaman.entity.VehicleBrand6[] getVehicleBrands();
}
```

## garaman.dao.VehicleDAO6

```java
public class VehicleDAO6 extends garaman.dao.DAO6 {
  public VehicleDAO6();
  public VehicleDAO6(java.sql.Connection con);
  public garaman.entity.Vehicle6 findById(int vehicleId);
  public garaman.entity.Vehicle6[] getVehiclesOfCustomer(int customerId);
  private void rollback(boolean tx, java.lang.Exception cause);
  public garaman.entity.Vehicle6 saveVehicle(garaman.entity.Vehicle6 vehicle, int customerId);
}
```

## garaman.entity.Account6

```java
public class Account6 extends java.lang.Object {
  private int id;
  private java.lang.String username;
  private java.lang.String password;
  private java.lang.String profileImage;
  private java.lang.String status;
  public Account6();
  public int getId();
  public java.lang.String getPassword();
  public java.lang.String getProfileImage();
  public java.lang.String getStatus();
  public java.lang.String getUsername();
  public void setId(int id);
  public void setPassword(java.lang.String password);
  public void setProfileImage(java.lang.String profileImage);
  public void setStatus(java.lang.String status);
  public void setUsername(java.lang.String username);
}
```

## garaman.entity.Appointment6

```java
public class Appointment6 extends java.lang.Object {
  private int id;
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
  public int getId();
  public java.lang.String getRequestDescription();
  public garaman.entity.AppointmentSlot6 getSlot();
  public garaman.entity.SaleStaff6 getSs();
  public java.lang.String getStatus();
  public garaman.entity.Vehicle6 getVehicle();
  public void setAppointmentCode(java.lang.String appointmentCode);
  public void setCreateAt(java.util.Date createAt);
  public void setCus(garaman.entity.Customer6 cus);
  public void setId(int id);
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
  private int id;
  private java.util.Date startTime;
  private java.lang.String holdCode;
  private java.util.Date holdExpiresAt;
  private java.lang.String status;
  public AppointmentSlot6();
  public java.lang.String getHoldCode();
  public java.util.Date getHoldExpiresAt();
  public int getId();
  public java.util.Date getStartTime();
  public java.lang.String getStatus();
  public void setHoldCode(java.lang.String holdCode);
  public void setHoldExpiresAt(java.util.Date holdExpiresAt);
  public void setId(int id);
  public void setStartTime(java.util.Date startTime);
  public void setStatus(java.lang.String status);
}
```

## garaman.entity.Category6

```java
public class Category6 extends java.lang.Object {
  private int id;
  private java.lang.String categoryName;
  public Category6();
  public java.lang.String getCategoryName();
  public int getId();
  public void setCategoryName(java.lang.String categoryName);
  public void setId(int id);
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
  private int id;
  private java.lang.String invoiceCode;
  private double totalAmount;
  private java.util.Date issueDate;
  private garaman.entity.RepairOrder6 order;
  private garaman.entity.Payment6 payment;
  public Invoice6();
  public int getId();
  public java.lang.String getInvoiceCode();
  public java.util.Date getIssueDate();
  public garaman.entity.RepairOrder6 getOrder();
  public garaman.entity.Payment6 getPayment();
  public double getTotalAmount();
  public void setId(int id);
  public void setInvoiceCode(java.lang.String invoiceCode);
  public void setIssueDate(java.util.Date issueDate);
  public void setOrder(garaman.entity.RepairOrder6 order);
  public void setPayment(garaman.entity.Payment6 payment);
  public void setTotalAmount(double totalAmount);
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
  private int id;
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
  public int getId();
  public java.lang.String getPhone();
  public void setAcc(garaman.entity.Account6 acc);
  public void setAddress(java.lang.String address);
  public void setEmail(java.lang.String email);
  public void setFullName(java.lang.String fullName);
  public void setId(int id);
  public void setPhone(java.lang.String phone);
}
```

## garaman.entity.PartBrand6

```java
public class PartBrand6 extends java.lang.Object {
  private int id;
  private java.lang.String name;
  public PartBrand6();
  public int getId();
  public java.lang.String getName();
  public void setId(int id);
  public void setName(java.lang.String name);
}
```

## garaman.entity.PartUsage6

```java
public class PartUsage6 extends java.lang.Object {
  private int id;
  private int usedQuantity;
  private java.util.Date usageTime;
  private double saleUnitPrice;
  private java.lang.String note;
  private garaman.entity.SparePart6 sparePart;
  private garaman.entity.Technician6 tech;
  private garaman.entity.RepairOrder6 order;
  public PartUsage6();
  public int getId();
  public java.lang.String getNote();
  public garaman.entity.RepairOrder6 getOrder();
  public double getSaleUnitPrice();
  public garaman.entity.SparePart6 getSparePart();
  public garaman.entity.Technician6 getTech();
  public java.util.Date getUsageTime();
  public int getUsedQuantity();
  public void setId(int id);
  public void setNote(java.lang.String note);
  public void setOrder(garaman.entity.RepairOrder6 order);
  public void setSaleUnitPrice(double saleUnitPrice);
  public void setSparePart(garaman.entity.SparePart6 sparePart);
  public void setTech(garaman.entity.Technician6 tech);
  public void setUsageTime(java.util.Date usageTime);
  public void setUsedQuantity(int usedQuantity);
}
```

## garaman.entity.Payment6

```java
public class Payment6 extends java.lang.Object {
  private int id;
  private double paymentAmount;
  private java.util.Date paymentTime;
  private java.lang.String paymentMethod;
  private java.lang.String status;
  private garaman.entity.SaleStaff6 ss;
  private java.lang.String transactionReference;
  public Payment6();
  public int getId();
  public double getPaymentAmount();
  public java.lang.String getPaymentMethod();
  public java.util.Date getPaymentTime();
  public garaman.entity.SaleStaff6 getSs();
  public java.lang.String getStatus();
  public java.lang.String getTransactionReference();
  public void setId(int id);
  public void setPaymentAmount(double paymentAmount);
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
  private int id;
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
  public int getId();
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
  public void setId(int id);
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
  private int usageCount;
  private double totalRevenue;
  public RevenueStats6();
  public java.lang.String getItemCode();
  public java.lang.String getItemName();
  public java.lang.String getItemType();
  public double getTotalRevenue();
  public int getUsageCount();
  public void setItemCode(java.lang.String itemCode);
  public void setItemName(java.lang.String itemName);
  public void setItemType(java.lang.String itemType);
  public void setTotalRevenue(double totalRevenue);
  public void setUsageCount(int usageCount);
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
  private int id;
  private java.lang.String serviceCode;
  private java.lang.String serviceName;
  private java.lang.String description;
  private double unitPrice;
  public Service6();
  public java.lang.String getDescription();
  public int getId();
  public java.lang.String getServiceCode();
  public java.lang.String getServiceName();
  public double getUnitPrice();
  public void setDescription(java.lang.String description);
  public void setId(int id);
  public void setServiceCode(java.lang.String serviceCode);
  public void setServiceName(java.lang.String serviceName);
  public void setUnitPrice(double unitPrice);
}
```

## garaman.entity.ServiceUsage6

```java
public class ServiceUsage6 extends java.lang.Object {
  private int id;
  private java.util.Date usageTime;
  private double saleUnitPrice;
  private java.lang.String note;
  private garaman.entity.Service6 service;
  private garaman.entity.Technician6 tech;
  private garaman.entity.RepairOrder6 order;
  public ServiceUsage6();
  public int getId();
  public java.lang.String getNote();
  public garaman.entity.RepairOrder6 getOrder();
  public double getSaleUnitPrice();
  public garaman.entity.Service6 getService();
  public garaman.entity.Technician6 getTech();
  public java.util.Date getUsageTime();
  public void setId(int id);
  public void setNote(java.lang.String note);
  public void setOrder(garaman.entity.RepairOrder6 order);
  public void setSaleUnitPrice(double saleUnitPrice);
  public void setService(garaman.entity.Service6 service);
  public void setTech(garaman.entity.Technician6 tech);
  public void setUsageTime(java.util.Date usageTime);
}
```

## garaman.entity.SparePart6

```java
public class SparePart6 extends java.lang.Object {
  private int id;
  private java.lang.String partCode;
  private java.lang.String partName;
  private java.lang.String description;
  private java.lang.String unit;
  private java.lang.String technicalSpecifications;
  private double unitPrice;
  private int stockQuantity;
  private java.lang.String image;
  private garaman.entity.PartBrand6 brand;
  private garaman.entity.Category6 cat;
  public SparePart6();
  public garaman.entity.PartBrand6 getBrand();
  public garaman.entity.Category6 getCat();
  public java.lang.String getDescription();
  public int getId();
  public java.lang.String getImage();
  public java.lang.String getPartCode();
  public java.lang.String getPartName();
  public int getStockQuantity();
  public java.lang.String getTechnicalSpecifications();
  public java.lang.String getUnit();
  public double getUnitPrice();
  public void setBrand(garaman.entity.PartBrand6 brand);
  public void setCat(garaman.entity.Category6 cat);
  public void setDescription(java.lang.String description);
  public void setId(int id);
  public void setImage(java.lang.String image);
  public void setPartCode(java.lang.String partCode);
  public void setPartName(java.lang.String partName);
  public void setStockQuantity(int stockQuantity);
  public void setTechnicalSpecifications(java.lang.String technicalSpecifications);
  public void setUnit(java.lang.String unit);
  public void setUnitPrice(double unitPrice);
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
  private int id;
  private java.lang.String vehicleCode;
  private java.lang.String licensePlate;
  private java.lang.String vehicleModel;
  private int currentMileage;
  private java.lang.Integer manufactureYear;
  private garaman.entity.Customer6 cus;
  private garaman.entity.VehicleBrand6 brand;
  public Vehicle6();
  public garaman.entity.VehicleBrand6 getBrand();
  public int getCurrentMileage();
  public garaman.entity.Customer6 getCus();
  public int getId();
  public java.lang.String getLicensePlate();
  public java.lang.Integer getManufactureYear();
  public java.lang.String getVehicleCode();
  public java.lang.String getVehicleModel();
  public void setBrand(garaman.entity.VehicleBrand6 brand);
  public void setCurrentMileage(int currentMileage);
  public void setCus(garaman.entity.Customer6 cus);
  public void setId(int id);
  public void setLicensePlate(java.lang.String licensePlate);
  public void setManufactureYear(java.lang.Integer manufactureYear);
  public void setVehicleCode(java.lang.String vehicleCode);
  public void setVehicleModel(java.lang.String vehicleModel);
}
```

## garaman.entity.VehicleBrand6

```java
public class VehicleBrand6 extends java.lang.Object {
  private int id;
  private java.lang.String name;
  public VehicleBrand6();
  public int getId();
  public java.lang.String getName();
  public void setId(int id);
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
  public void init(jakarta.servlet.FilterConfig config);
}
```

## garaman.filter.AuthFilter6

```java
public class AuthFilter6 extends java.lang.Object implements jakarta.servlet.Filter {
  public AuthFilter6();
  public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.AppointmentServlet6

```java
public class AppointmentServlet6 extends jakarta.servlet.http.HttpServlet {
  public AppointmentServlet6();
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.BookingServlet6

```java
public class BookingServlet6 extends jakarta.servlet.http.HttpServlet {
  public BookingServlet6();
  static void confirm(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, garaman.util.BookingContext6 c) throws jakarta.servlet.ServletException, java.io.IOException;
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private void form(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, garaman.util.BookingContext6 c) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.InvoiceServlet6

```java
public class InvoiceServlet6 extends jakarta.servlet.http.HttpServlet {
  public InvoiceServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.LoginServlet6

```java
public class LoginServlet6 extends jakarta.servlet.http.HttpServlet {
  public LoginServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private void home(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws java.io.IOException;
  private void invalid(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.RevenueServlet6

```java
public class RevenueServlet6 extends jakarta.servlet.http.HttpServlet {
  public RevenueServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private void filter(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private void list(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.ServicePartServlet6

```java
public class ServicePartServlet6 extends jakarta.servlet.http.HttpServlet {
  public ServicePartServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.servlet.VehicleServlet6

```java
public class VehicleServlet6 extends jakarta.servlet.http.HttpServlet {
  public VehicleServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private void form(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, garaman.util.BookingContext6 c) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

## garaman.util.BookingContext6

```java
public final class BookingContext6 extends java.lang.Object {
  public final java.lang.String id;
  public java.time.LocalDate appointmentDate;
  public java.lang.Integer vehicleId;
  public java.lang.Integer slotId;
  public java.lang.String requestDescription;
  public java.lang.String holdCode;
  public java.util.Date holdExpiresAt;
  public garaman.entity.Appointment6 draft;
  public garaman.entity.Appointment6 completed;
  public BookingContext6(java.lang.String id);
}
```

## garaman.util.BookingSessionListener6

```java
public class BookingSessionListener6 extends java.lang.Object implements jakarta.servlet.http.HttpSessionListener {
  public BookingSessionListener6();
  public void sessionDestroyed(jakarta.servlet.http.HttpSessionEvent event);
}
```

## garaman.util.Config6

```java
public final class Config6 extends java.lang.Object {
  private static final java.util.Properties LOCAL;
  private Config6();
  public static java.lang.String get(java.lang.String key, java.lang.String fallback);
  private static java.util.Properties load();
  public static int number(java.lang.String key, int fallback);
  public static java.time.ZoneId zone();
}
```

## garaman.util.DateUtil6

```java
public final class DateUtil6 extends java.lang.Object {
  private DateUtil6();
  public static java.util.Date date(java.time.LocalDate date);
  public static java.sql.Timestamp endExclusive(java.util.Date date);
  public static java.time.LocalDate localDate(java.util.Date date);
  public static java.sql.Timestamp start(java.util.Date date);
}
```

## garaman.util.PasswordUtil6

```java
public final class PasswordUtil6 extends java.lang.Object {
  private static final int ITERATIONS;
  private PasswordUtil6();
  private static byte[] derive(java.lang.String password, byte[] salt, int iterations);
  public static java.lang.String hash(java.lang.String password);
  public static void main(java.lang.String[] args);
  public static boolean verify(java.lang.String password, java.lang.String encodedHash);
}
```

## garaman.util.SlotSetup6

```java
public class SlotSetup6 extends garaman.dao.DAO6 {
  public SlotSetup6();
  public static void main(java.lang.String[] args);
  public int prepareSlots();
}
```

## garaman.util.Web6

```java
public final class Web6 extends java.lang.Object {
  private Web6();
  public static int clampPage(int page, int length);
  public static garaman.util.BookingContext6 context(jakarta.servlet.http.HttpServletRequest req, boolean create);
  public static java.util.Map<java.lang.String, garaman.util.BookingContext6> contexts(jakarta.servlet.http.HttpSession session);
  public static garaman.entity.Customer6 customer(jakarta.servlet.http.HttpServletRequest req);
  public static java.time.LocalDate date(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  private static java.lang.String encode(java.lang.String value);
  public static void error(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, int status, java.lang.String message) throws jakarta.servlet.ServletException, java.io.IOException;
  public static int integer(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  public static int page(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  public static void release(garaman.util.BookingContext6 c);
  public static void releaseAll(jakarta.servlet.http.HttpSession session);
  public static void reportFilter(jakarta.servlet.http.HttpServletRequest req);
  public static java.util.Map<java.lang.String, java.lang.Object> reportParams(jakarta.servlet.http.HttpServletRequest req);
  public static T[] slice(T[] all, int page);
  public static java.lang.String text(jakarta.servlet.http.HttpServletRequest req, java.lang.String name);
  public static java.lang.String url(jakarta.servlet.http.HttpServletRequest req, java.lang.String route, java.util.Map<java.lang.String, ?> params);
  public static void view(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, java.lang.String name) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

