# Những ô cần sửa trên hai sơ đồ

Bản này thay bảng cùng tên trước đây theo quyết định Servlet đóng gói. Hợp đồng đầy đủ, gồm cả constructor/phương thức private/package-private/throws/thứ tự và tên tham số, ở [01](01_Hop_dong_lop.md). Không tạo lớp khác tên để thay helper.

## Các field integer/decimal — tên Java chính xác

Các hàng dưới đây được liệt kê từ field thật của 21 entity; là checklist cập nhật từng ô, không phải bằng chứng độc lập rằng ảnh gốc đúng. id thuộc superclass chỉ khai báo ở superclass; các subclass thừa kế, không khai báo trùng id. Field Integer mới mặc định null cho đến khi hydrate/set; count query không có dữ liệu trả Integer 0.

| Lớp | Field | Kiểu Java | Getter | Setter |
|---|---|---|---|---|
| Account6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| Appointment6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| AppointmentSlot6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| Category6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| Invoice6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| Invoice6 | totalAmount | `BigDecimal` | `getTotalAmount(): BigDecimal` | `setTotalAmount(BigDecimal totalAmount): void` |
| Member6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| PartBrand6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| PartUsage6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| PartUsage6 | usedQuantity | `Integer` | `getUsedQuantity(): Integer` | `setUsedQuantity(Integer usedQuantity): void` |
| PartUsage6 | saleUnitPrice | `BigDecimal` | `getSaleUnitPrice(): BigDecimal` | `setSaleUnitPrice(BigDecimal saleUnitPrice): void` |
| Payment6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| Payment6 | paymentAmount | `BigDecimal` | `getPaymentAmount(): BigDecimal` | `setPaymentAmount(BigDecimal paymentAmount): void` |
| RepairOrder6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| RevenueStats6 | usageCount | `Integer` | `getUsageCount(): Integer` | `setUsageCount(Integer usageCount): void` |
| RevenueStats6 | totalRevenue | `BigDecimal` | `getTotalRevenue(): BigDecimal` | `setTotalRevenue(BigDecimal totalRevenue): void` |
| Service6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| Service6 | unitPrice | `BigDecimal` | `getUnitPrice(): BigDecimal` | `setUnitPrice(BigDecimal unitPrice): void` |
| ServiceUsage6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| ServiceUsage6 | saleUnitPrice | `BigDecimal` | `getSaleUnitPrice(): BigDecimal` | `setSaleUnitPrice(BigDecimal saleUnitPrice): void` |
| SparePart6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| SparePart6 | unitPrice | `BigDecimal` | `getUnitPrice(): BigDecimal` | `setUnitPrice(BigDecimal unitPrice): void` |
| SparePart6 | stockQuantity | `Integer` | `getStockQuantity(): Integer` | `setStockQuantity(Integer stockQuantity): void` |
| Vehicle6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |
| Vehicle6 | currentMileage | `Integer` | `getCurrentMileage(): Integer` | `setCurrentMileage(Integer currentMileage): void` |
| Vehicle6 | manufactureYear | `Integer` | `getManufactureYear(): Integer` | `setManufactureYear(Integer manufactureYear): void` |
| VehicleBrand6 | id | `Integer` | `getId(): Integer` | `setId(Integer id): void` |

## Hợp đồng thao tác cần ghi lại

| Ô | Nội dung chính xác sau sửa |
|---|---|
| Vehicle6.getVehicleCode | `public String getVehicleCode()`; null nếu id null/<=0, ngược lại X + String.format(Locale.ROOT,"%04d",id); không field/setter/cột vehicleCode, không cắt >9999. |
| VehicleBrandDAO6.getVehicleBrands | `public VehicleBrand6[] getVehicleBrands()` — không phải VehicleBrandDAO6[]. |
| VehicleBrandDAO6.findById | `public VehicleBrand6 findById(Integer id)`. |
| VehicleDAO6 | `public Vehicle6[] getVehiclesOfCustomer(Integer customerId)`; `public Vehicle6 findById(Integer vehicleId)`; `public Vehicle6 saveVehicle(Vehicle6 vehicle, Integer customerId)`. |
| SlotDAO6 | `getAvailableSlots(LocalDate appointmentDate): AppointmentSlot6[]`; `findById(Integer slotId): AppointmentSlot6`; `holdSlot(Integer slotId): AppointmentSlot6`; `validateHold(Integer slotId,String holdCode): boolean`; `releaseSlot(Integer slotId,String holdCode): boolean`; `consumeHold(Integer slotId,String holdCode): boolean`; `remainingHoldMillis(Integer slotId,String holdCode): long`. Tất cả public; long/boolean không đổi sang Integer. |
| AppointmentDAO6 | `public Appointment6 createAppointment(Appointment6 appointment,String holdCode)`; `findById(Integer id): Appointment6` package-private; `private static String temporaryCode()`. Placeholder INSERT hợp lệ, getGeneratedKeys, hoàn tất LH trước commit; không sequence/MAX+1. |
| ServiceUsageDAO6 | `public Integer getTotalUsageCount(String serviceCode)`; `public ServiceUsage6[] getAllServiceUsageOfService(String serviceCode,Date startDate,Date endDate)`; `ServiceUsage6[] byOrder(Integer orderId)` package-private; private readUsage(ResultSet). |
| PartUsageDAO6 | `public Integer getTotalUsageCount(String partCode)`; `public PartUsage6[] getAllPartUsageOfPart(String partCode,Date startDate,Date endDate)`; `PartUsage6[] byOrder(Integer orderId)` package-private; private readUsage(ResultSet). |
| InvoiceDAO6 | `public String getInvoiceCodeByUsage(String itemType,Integer usageId)`; `public Invoice6 getInvoiceDetail(String invoiceCode)`; `RepairOrder6 findOrderById(Integer id)` package-private dùng chung cho graph invoice/history, không tạo lớp DAO giả tên helper. |
| RevenueDAO6 | `public RevenueStats6[] getRevenueStatistics(Date startDate,Date endDate,String statisticType)`. |
| CustomerDAO6 | `public Customer6 getCustomerInfo(String customerCode)`; `Customer6 findById(Integer id)` và `Customer6 readCustomer(ResultSet rs)` package-private cho graph/hydration. |
| MemberDAO6 | `public Member6 authenticate(String username,String password)`; `public static String hashPassword(String password)`; fillMember/findEmployeeById/findSaleStaffById/findTechnicianById package-private; verifyPassword/derive private. |
| DAO6 | Giữ con:Connection và owner:boolean final, constructor DAO6()/DAO6(Connection); config load private, setting/number/zone static public; close chỉ đóng connection sở hữu. Không gom cả logic nghiệp vụ vào DAO6. |
| BookingServlet6 | doGet/doPost public; tạo Appointment6 bằng constructor/setCus/setVehicle/setSlot/setRequestDescription ngay trong doPost sau hold; giữ draft phía server. context/confirm static package-private; contexts/release và các hàm đọc params/view private; releaseAll public static cho listener. |
| VehicleServlet6 | doPost kiểm dữ liệu → new Vehicle6/setCus/setBrand/setLicensePlate/setVehicleModel/setManufactureYear → saveVehicle → redirect booking; id/Km null, không mass-assign client. |
| AppointmentServlet6 | Lấy draft/context server, ownership Objects.equals, validateHold, createAppointment, completed + xóa hold/draft, PRG; không new draft lần hai. |
| Servlet báo cáo | Các hàm param/date/URL/forward/pagination chuyển thành private tại Servlet sử dụng; JSP chỉ render/gửi request. Không đổi control/bố cục. |
| Session context | `bookingContexts: Map<String,Map<String,Object>>`, UUID mỗi tab. Map con giữ id, appointmentDate, vehicleId, slotId, requestDescription, draft, holdCode, holdExpiresAt, completed. Đồng bộ trên từng Map. Không có lớp context thay tên. |
| Thành phần container | Giữ AppFilter6/AuthFilter6 cho UTF-8/CSRF/role/error; BookingSessionListener6 gọi BookingServlet6.releaseAll khi session bị hủy. Listener không phải helper nghiệp vụ. |

Không dùng == giữa hai Integer: ownership và membership dùng Objects.equals. JDBC/HTTP status, vòng lặp, iteration PBKDF và giá trị kỹ thuật nội bộ có thể dùng int; chúng không phải field integer nghiệp vụ trên sơ đồ. ID/query/count contract thuộc thiết kế dùng Integer. Đã thử các ID >127 qua HTTP và DB thật.

## Quan hệ giữ đúng

- Member6.acc → Account6 0..1; account không có thì cả bốn cột null; có thì username/password/status hợp lệ, profileImage tùy chọn.
- Appointment6.cus/vehicle/slot bắt buộc khi lưu; ss 0..1; Slot có nhiều lịch sử, tối đa 1 active (generated unique trong DB).
- RepairOrder6.appointment 0..1 với FK nullable UNIQUE; Invoice6.order 1, FK UNIQUE giới hạn một invoice/order.
- Invoice6.payment 0..1; UNPAID null, PAID dựng từ Invoice columns, id logic=Invoice.id; không Payment table.
- RepairOrder6.ss là người bán, Payment6.ss là người thu với hai FK riêng. Không có rating.
- Usage.order/service-or-part/tech và các graph con do DAO phù hợp hydrate. Nét liền chỉ field reference, nét đứt cho dependency gọi thật, kế thừa dùng tam giác.

## Xóa và chuyển trách nhiệm

| Lớp/JSP đã xóa | Nơi tiếp nhận trách nhiệm |
|---|---|
| RowMapper6 | CustomerDAO6.readCustomer/findById, MemberDAO6.fillMember/findEmployeeById/findSaleStaffById/findTechnicianById, VehicleDAO6.readVehicle, SlotDAO6.readSlot, ServiceDAO6.readService, SparePartDAO6.readPart, InvoiceDAO6.findOrderById, AppointmentDAO6.findById |
| UsageMapper6 | ServiceUsageDAO6.readUsage và PartUsageDAO6.readUsage (private) |
| BusinessCode6 | Vehicle6.getVehicleCode dẫn xuất X; AppointmentDAO6.temporaryCode và UPDATE mã LH trước commit |
| DateUtil6 | Các phương thức private date/localDate/start/endExclusive trong Servlet/DAO sử dụng ngày |
| Web6 | Các phương thức private đọc param/validation/forward/URL/phân trang trong từng Servlet; error forward trong filter |
| BookingContext6 | BookingServlet6 quản lý session Map<String,Map<String,Object>>, mỗi tab/context một Map, khóa synchronized(c) |
| Config6 | DAO6.load/setting/number/zone; cấu hình vẫn từ system property → environment → file private, không hardcode secret |
| PasswordUtil6 | MemberDAO6.hashPassword/verifyPassword/derive; PBKDF2-HMAC-SHA256, salt riêng, 600000 vòng; không plaintext |
| SlotSetup6 | SlotDAO6.prepareSlots/main; scripts/prepare_slots.py gọi trực tiếp DAO CLI |
| PrepareAppointment6.jsp | BookingServlet6.doPost tạo Appointment6 sau validation/ownership/hold, c.put("draft",draft), forward ConfirmView6 |
| PrepareVehicle6.jsp | VehicleServlet6.doPost tạo Vehicle6 sau validation, gọi VehicleDAO6.saveVehicle rồi redirect booking |

Đã bỏ các action nội bộ, PACKAGING_GUARD, trusted*/prepared* request attrs và acceptDraft/savePrepared chỉ phục vụ vòng forward cũ. Không thêm màn hình. Các input/contact readonly, CSRF, ownership, giới hạn context/tab, expiry/edit/double-submit và transaction vẫn giữ.

Các phân loại D017/D018/D038/D042/D057/D059 đã chốt theo prompt trước vẫn giữ: mã xe dẫn xuất, không sequence/marker table, Payment folded, Km nullable, account optional/status chuẩn. Phần yêu cầu JSP đóng gói của lượt trước nay bị thay thế. D049–D054 validation/money/countdown/guard không bị bỏ trong refactor.
