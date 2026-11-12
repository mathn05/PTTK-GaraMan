# Hợp đồng lớp trước triển khai

Bản **đầy đủ sau build**, gồm mọi field, visibility, constructor, public/private method, import đầy đủ, tên/thứ tự tham số và throws: [CONTRACTS_GENERATED.md](CONTRACTS_GENERATED.md). Sinh trực tiếp từ reflection bytecode với `-parameters`; bảng dưới là hợp đồng nghiệp vụ đã khóa trước code.

Nguồn: SOURCE_MANIFEST; áp dụng chữ ký mục 6 của prompt. Entity là JavaBean public, private fields, constructor không tham số và getter/setter theo đúng tên. Date là `java.util.Date`; `getAvailableSlots` dùng `java.time.LocalDate`. Public API trả mảng, không List. Kiểu tiền public double giữ theo thiết kế, SQL tính DECIMAL, tổng Servlet dùng BigDecimal.

## Entity fields

| Lớp / cha | Private fields |
|---|---|
| Account6 | int id; String username,password,profileImage,status |
| Member6 | int id; String fullName,phone,email,address; Account6 acc |
| Customer6 extends Member6 | String customerCode |
| Employee6 extends Member6 | String employeeCode,jobTitle |
| Manager6 / SaleStaff6 / Technician6 extends Employee6 | không khai báo lại field |
| VehicleBrand6 / PartBrand6 | int id; String name |
| Category6 | int id; String categoryName |
| Vehicle6 | int id,currentMileage; String vehicleCode,licensePlate,vehicleModel; Integer manufactureYear; Customer6 cus; VehicleBrand6 brand |
| AppointmentSlot6 | int id; Date startTime,holdExpiresAt; String holdCode,status |
| Appointment6 | int id; String appointmentCode,requestDescription,status; Date createAt; SaleStaff6 ss; Vehicle6 vehicle; AppointmentSlot6 slot; Customer6 cus |
| RevenueStats6 | String itemCode,itemName,itemType; int usageCount; double totalRevenue |
| Service6 | int id; String serviceCode,serviceName,description; double unitPrice |
| SparePart6 | int id,stockQuantity; String partCode,partName,description,unit,technicalSpecifications,image; double unitPrice; PartBrand6 brand; Category6 cat |
| ServiceUsage6 | int id; Date usageTime; double saleUnitPrice; String note; Service6 service; Technician6 tech; RepairOrder6 order |
| PartUsage6 | int id,usedQuantity; Date usageTime; double saleUnitPrice; String note; SparePart6 sparePart; Technician6 tech; RepairOrder6 order |
| RepairOrder6 | int id; String repairOrderCode,vehicleCondition,repairDescription,repairResult,repairStatus; Date createTime,completionTime; ServiceUsage6[] serviceUsage; PartUsage6[] partUsage; Vehicle6 vehicle; Customer6 cus; SaleStaff6 ss; Appointment6 appointment |
| Payment6 | int id; double paymentAmount; Date paymentTime; String paymentMethod,status,transactionReference; SaleStaff6 ss |
| Invoice6 | int id; String invoiceCode; double totalAmount; Date issueDate; RepairOrder6 order; Payment6 payment |

## DAO và lifetime connection

`DAO6 implements AutoCloseable`: protected Connection con, public DAO6(), public DAO6(Connection con), close(). Mỗi request tạo DAO trong try-with-resources; con mượn không tự đóng/commit. Không connection static/shared.

Mọi DAO public không tham số kế thừa DAO6, thêm constructor Connection cho các mapper/transaction. Các chữ ký nghiệp vụ chính giữ nguyên ở mục 6 của input/GaraMan_Prompt_Code_2_Module.md; bản đầy đủ có tên tham số sẽ được xuất từ source sau khi code, bao gồm các helper kỹ thuật. Không có dữ liệu: mảng rỗng / object null. Xung đột hold: null/false; createAppointment ném BookingConflictException6. Lỗi SQL: DataAccessException6 có cause, tuyệt đối không giả thành kết quả rỗng.

`CodeSequenceDAO6(Connection con).nextCode(String prefix):String`: caller phải đang trong transaction. `PasswordUtil6.hash(String password):String` / `verify(String password,String encodedHash):boolean`: PBKDF2 SHA256 600000 vòng, salt 16 byte, hash 32 byte. DataAccessException6(String message,Throwable cause), BookingConflictException6(String message) extends RuntimeException.

## Servlet và kỹ thuật bổ sung

BookingServlet6 doGet/doPost; AppointmentServlet6 doPost; VehicleServlet6 doGet/doPost; RevenueServlet6/ServicePartServlet6/InvoiceServlet6 doGet; LoginServlet6 doGet/doPost. Các override public, throws ServletException, IOException. Servlet không giữ dữ liệu người dùng trong fields. AuthFilter6 doFilter; AppFilter6 UTF8/CSRF/lỗi; session draft map theo context riêng từng tab. Chi tiết URL/attribute ở HTTP_VIEW_CONTRACT.md.
