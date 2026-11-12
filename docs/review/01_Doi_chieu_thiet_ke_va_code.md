# Đối chiếu thiết kế và code hiện trạng

Review theo nguồn/thứ tự ưu tiên trong [00](00_Nguon_chuan_va_pham_vi.md); không sửa code để làm mất khác biệt. Snapshot: ab485f1be8360b8dc1e1b1c9d9f0151c052f145c0ca8a0c4dc2adff642fe0ffb.

## 1. Kết quả phân loại

Có **61 mục D001–D061**; mỗi mục là một khác biệt hoặc giới hạn đối chiếu có bằng chứng, không đồng nghĩa 61 bug.

Thay đổi triển khai hợp lý cần cập nhật thiết kế: **26**; Thay đổi nghiệp vụ đã được chấp thuận: **15**; Khác biệt hình thức: **10**; Chưa đủ bằng chứng: **5**; Lỗi cần sửa: **5**

Có **5 lỗi cần sửa**: D049–D052 được tái hiện runtime; D053 là kết luận tĩnh về caption giờ cấu hình. D001–D002 riêng là **khác yêu cầu vị trí đóng gói**: code tuân theo controller tạo entity như ví dụ LAB, nhưng không có luồng JSP tạo entity → JSP nhận lại → Servlet lấy object như yêu cầu muốn kiểm tra. Không sửa hoặc giả vờ đã có luồng đó.

## 2. Inventory lớp, chữ ký, field và quan hệ

Toàn bộ **55 lớp main** được liệt kê dưới đây. Phụ lục 8 trong chính file này là hợp đồng đầy đủ sinh lại từ bytecode vừa build với -parameters: mọi field, visibility, final/static, kế thừa, constructor, overload, tên/thứ tự/kiểu tham số, kiểu trả và checked exception. Đã đọc call site source, không dùng reflection để suy luận thứ tự chạy. [javap -p](evidence/javap-inventory.txt) xác nhận độc lập các declaration, kể cả package-private/private.

| Lớp/package | Khai báo visibility/kế thừa | Source:file:dòng | Vai trò |
|---|---|---|---|
| garaman.dao.AppointmentDAO6 | public class AppointmentDAO6 extends DAO6 | src/main/java/garaman/dao/AppointmentDAO6.java:7 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.CodeSequenceDAO6 | public class CodeSequenceDAO6 extends DAO6 | src/main/java/garaman/dao/CodeSequenceDAO6.java:7 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.CustomerDAO6 | public class CustomerDAO6 extends DAO6 | src/main/java/garaman/dao/CustomerDAO6.java:7 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.DAO6 | public class DAO6 implements AutoCloseable | src/main/java/garaman/dao/DAO6.java:9 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.InvoiceDAO6 | public class InvoiceDAO6 extends DAO6 | src/main/java/garaman/dao/InvoiceDAO6.java:8 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.MemberDAO6 | public class MemberDAO6 extends DAO6 | src/main/java/garaman/dao/MemberDAO6.java:8 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.PartUsageDAO6 | public class PartUsageDAO6 extends DAO6 | src/main/java/garaman/dao/PartUsageDAO6.java:10 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.RevenueDAO6 | public class RevenueDAO6 extends DAO6 | src/main/java/garaman/dao/RevenueDAO6.java:11 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.RowMapper6 | final class RowMapper6 | src/main/java/garaman/dao/RowMapper6.java:7 | Helper hydrate package-private, static; throws SQLException |
| garaman.dao.ServiceDAO6 | public class ServiceDAO6 extends DAO6 | src/main/java/garaman/dao/ServiceDAO6.java:7 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.ServiceUsageDAO6 | public class ServiceUsageDAO6 extends DAO6 | src/main/java/garaman/dao/ServiceUsageDAO6.java:10 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.SlotDAO6 | public class SlotDAO6 extends DAO6 | src/main/java/garaman/dao/SlotDAO6.java:11 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.SparePartDAO6 | public class SparePartDAO6 extends DAO6 | src/main/java/garaman/dao/SparePartDAO6.java:7 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.UsageMapper6 | final class UsageMapper6 | src/main/java/garaman/dao/UsageMapper6.java:6 | Helper hydrate package-private, static; throws SQLException |
| garaman.dao.VehicleBrandDAO6 | public class VehicleBrandDAO6 extends DAO6 | src/main/java/garaman/dao/VehicleBrandDAO6.java:8 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.dao.VehicleDAO6 | public class VehicleDAO6 extends DAO6 | src/main/java/garaman/dao/VehicleDAO6.java:11 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.entity.Account6 | public class Account6 | src/main/java/garaman/entity/Account6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.Appointment6 | public class Appointment6 | src/main/java/garaman/entity/Appointment6.java:5 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.AppointmentSlot6 | public class AppointmentSlot6 | src/main/java/garaman/entity/AppointmentSlot6.java:5 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.Category6 | public class Category6 | src/main/java/garaman/entity/Category6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.Customer6 | public class Customer6 extends Member6 | src/main/java/garaman/entity/Customer6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.Employee6 | public class Employee6 extends Member6 | src/main/java/garaman/entity/Employee6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.Invoice6 | public class Invoice6 | src/main/java/garaman/entity/Invoice6.java:5 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.Manager6 | public class Manager6 extends Employee6 | src/main/java/garaman/entity/Manager6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.Member6 | public class Member6 | src/main/java/garaman/entity/Member6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.PartBrand6 | public class PartBrand6 | src/main/java/garaman/entity/PartBrand6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.PartUsage6 | public class PartUsage6 | src/main/java/garaman/entity/PartUsage6.java:5 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.Payment6 | public class Payment6 | src/main/java/garaman/entity/Payment6.java:5 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.RepairOrder6 | public class RepairOrder6 | src/main/java/garaman/entity/RepairOrder6.java:5 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.RevenueStats6 | public class RevenueStats6 | src/main/java/garaman/entity/RevenueStats6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.SaleStaff6 | public class SaleStaff6 extends Employee6 | src/main/java/garaman/entity/SaleStaff6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.Service6 | public class Service6 | src/main/java/garaman/entity/Service6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.ServiceUsage6 | public class ServiceUsage6 | src/main/java/garaman/entity/ServiceUsage6.java:5 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.SparePart6 | public class SparePart6 | src/main/java/garaman/entity/SparePart6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.Technician6 | public class Technician6 extends Employee6 | src/main/java/garaman/entity/Technician6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.Vehicle6 | public class Vehicle6 | src/main/java/garaman/entity/Vehicle6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.entity.VehicleBrand6 | public class VehicleBrand6 | src/main/java/garaman/entity/VehicleBrand6.java:3 | Dữ liệu; constructor/get/set, không persistence |
| garaman.exception.BookingConflictException6 | public class BookingConflictException6 extends RuntimeException | src/main/java/garaman/exception/BookingConflictException6.java:3 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.exception.DataAccessException6 | public class DataAccessException6 extends RuntimeException | src/main/java/garaman/exception/DataAccessException6.java:3 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.filter.AppFilter6 | public class AppFilter6 implements Filter | src/main/java/garaman/filter/AppFilter6.java:15 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.filter.AuthFilter6 | public class AuthFilter6 implements Filter | src/main/java/garaman/filter/AuthFilter6.java:9 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.servlet.AppointmentServlet6 | public class AppointmentServlet6 extends HttpServlet | src/main/java/garaman/servlet/AppointmentServlet6.java:13 | HTTP handler/helper; throws ServletException, IOException |
| garaman.servlet.BookingServlet6 | public class BookingServlet6 extends HttpServlet | src/main/java/garaman/servlet/BookingServlet6.java:14 | HTTP handler/helper; throws ServletException, IOException |
| garaman.servlet.InvoiceServlet6 | public class InvoiceServlet6 extends HttpServlet | src/main/java/garaman/servlet/InvoiceServlet6.java:13 | HTTP handler/helper; throws ServletException, IOException |
| garaman.servlet.LoginServlet6 | public class LoginServlet6 extends HttpServlet | src/main/java/garaman/servlet/LoginServlet6.java:12 | HTTP handler/helper; throws ServletException, IOException |
| garaman.servlet.RevenueServlet6 | public class RevenueServlet6 extends HttpServlet | src/main/java/garaman/servlet/RevenueServlet6.java:15 | HTTP handler/helper; throws ServletException, IOException |
| garaman.servlet.ServicePartServlet6 | public class ServicePartServlet6 extends HttpServlet | src/main/java/garaman/servlet/ServicePartServlet6.java:14 | HTTP handler/helper; throws ServletException, IOException |
| garaman.servlet.VehicleServlet6 | public class VehicleServlet6 extends HttpServlet | src/main/java/garaman/servlet/VehicleServlet6.java:13 | HTTP handler/helper; throws ServletException, IOException |
| garaman.util.BookingContext6 | public final class BookingContext6 | src/main/java/garaman/util/BookingContext6.java:8 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.util.BookingSessionListener6 | public class BookingSessionListener6 implements HttpSessionListener | src/main/java/garaman/util/BookingSessionListener6.java:8 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.util.Config6 | public final class Config6 | src/main/java/garaman/util/Config6.java:11 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.util.DateUtil6 | public final class DateUtil6 | src/main/java/garaman/util/DateUtil6.java:7 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.util.PasswordUtil6 | public final class PasswordUtil6 | src/main/java/garaman/util/PasswordUtil6.java:9 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |
| garaman.util.SlotSetup6 | public class SlotSetup6 extends DAO6 | src/main/java/garaman/util/SlotSetup6.java:9 | CLI chuẩn bị slot; không phải HTTP handler |
| garaman.util.Web6 | public final class Web6 | src/main/java/garaman/util/Web6.java:14 | Hỗ trợ thực thi; chữ ký đầy đủ bên dưới |

Các quy tắc chung của inventory:

- 21 entity đều public, field private, constructor không tham số public, setter void. Customer6 extends Member6; Employee6 extends Member6; Manager6/SaleStaff6/Technician6 extends Employee6. Không có SalesStaff6 hay InvoiceServlet không hậu tố; tên thật là SaleStaff6 và InvoiceServlet6. Appointment6 dùng createAt/cus/ss, không phải createdAt/customer/salesStaff. Đây là các tên **đã khớp**, không tạo khác biệt giả từ ví dụ trong prompt.
- Trường thời gian entity dùng java.util.Date; JDBC getTimestamp tạo Timestamp (subclass Date). Ngày chọn/lọc ở utility/context dùng LocalDate, SQL DATETIME(6), biên query Timestamp. Date không phải java.sql.Date trong public contract.
- Tất cả API danh sách nghiệp vụ trả mảng đúng kiểu: Vehicle6[], VehicleBrand6[], AppointmentSlot6[], RevenueStats6[], ServiceUsage6[], PartUsage6[]. ArrayList chỉ là biến tích lũy cục bộ trong DAO; không có entity trả List.
- Các field tiền public vẫn double; bảng SQL DECIMAL(18,2), aggregate JDBC BigDecimal, tổng báo cáo attribute BigDecimal. Đã ghi lợi ích và giới hạn chuyển đổi tại D034/D051; không đổi kiểu trong V3 để che mất double.
- RevenueStats6 có itemCode/itemName/itemType/usageCount/totalRevenue, không id, không bảng vật lý. Status/jobTitle/paymentMethod/itemType/statisticType là String, không enum. ManufactureYear Integer nullable; currentMileage int không nullable (xung đột D042).
- Mọi DAO nghiệp vụ kế thừa DAO6. RowMapper6/UsageMapper6 là helper final có visibility package-private, không extends DAO6. SlotSetup6 trong util extends DAO6. AppFilter6/AuthFilter6 implements Filter; BookingSessionListener6 implements HttpSessionListener.
- DAO6 có protected final con và owner. Không dùng shared static Connection. Constructor mặc định mở connection, constructor Connection mượn; close không đóng connection mượn.
- DataAccessException6 và BookingConflictException6 là RuntimeException. Public DAO bọc lỗi JDBC thành DataAccessException6; các helper/package-private hydrate có throws SQLException và bên gọi public bọc lỗi. Servlet handler khai báo throws ServletException, IOException. Đường lỗi không trả mảng rỗng để giả thành công.

Các declaration không được đưa tùy tiện vào happy case:

| Declaration | Có call site production hay không | Cách xử lý trong V3 |
|---|---|---|
| CustomerDAO6(Connection), VehicleBrandDAO6(Connection), RevenueDAO6(Connection), InvoiceDAO6(Connection), MemberDAO6(Connection) | Không thấy new tương ứng trong src/main/java; các constructor mặc định mới được dùng tại handler/helper tương ứng | Giữ ở inventory, không thêm lời gọi mượn connection giả |
| AppointmentDAO6(Connection) | Không dùng trong handler production; có test kiểm tra borrowed transaction | Happy case HTTP dùng AppointmentDAO6(), owner commit thật trước khi trả |
| VehicleDAO6(Connection), SlotDAO6(Connection), CodeSequenceDAO6(Connection), ServiceDAO6(Connection), SparePartDAO6(Connection), ServiceUsageDAO6(Connection), PartUsageDAO6(Connection) | Có call site trong mapper/DAO phụ, chi tiết bảng truy vết | Nêu đúng DAO phụ và cùng connection; không đóng connection của caller |
| DAO6() / DAO6(Connection) | Qua super() của subclass, không phải một DAO trung gian nhận kết quả | Không vẽ entity trả về một instance DAO6 mới |
| Config6/Web6/DateUtil6/PasswordUtil6/RowMapper6/UsageMapper6 constructor private | Không có lời gọi tạo instance ngoài lớp | Không vẽ new utility trong sequence |
| PasswordUtil6.main/hash, SlotSetup6.main/prepareSlots | Tool seed/CLI có thật, không phải user handler hai happy case | Review riêng bootstrap; không chen bước CLI vào thao tác đặt lịch/thống kê |
| RowMapper6.appointment khi order.appointmentId khác NULL | Có nhánh production tại RowMapper6.java:78, không chạy trong các order được chọn ở fixture happy case | Không bỏ khỏi inventory; khai báo tiền điều kiện của V3 và nêu giới hạn runtime ở báo cáo 03 |

## 3. UI, route, tham số, attribute và scope

Tất cả JSP màn hình ở /WEB-INF/views; người dùng đi qua Servlet. Include JSPF là include phía server, khác HTTP redirect. web.xml:4–7 đặt AppFilter6 trước AuthFilter6 cho REQUEST; không có mapping dispatcher FORWARD riêng, nên forward render không tạo thêm vòng kiểm tra REQUEST. web.xml:9 cấm scriptlet.

| Màn hình/action | Control / request parameter đúng chữ hoa thường | Servlet/helper và đích | Model/scope/kiểu đáng chú ý |
|---|---|---|---|
| LoginView6.jsp | POST /login; action=login,csrfToken,username,password; btnLogin | LoginServlet6.doPost → MemberDAO6.authenticate → redirect home/returnRoute whitelist | session currentUser:Member6,currentRole:String,csrfToken:String; changeSessionId |
| CustomerHomeView6.jsp | link GET /booking | BookingServlet6.doGet → context(req,true) → form → BookAppointmentView6.jsp | home trước đó là GET /booking?action=home; cus lấy qua Web6.customer |
| BookAppointmentView6.jsp chọn ngày | appointmentDate (input date); action=slots,context qua fetch GET | BookingServlet6.doGet → form; toàn HTML JSP; app.js chỉ thay .slot-fieldset | appointmentDate/minDate/maxDate String; availableSlots AppointmentSlot6[]; no JSON |
| BookAppointmentView6.jsp hold | POST /booking, action=hold; context,csrfToken,vehicle,appointmentSlot,requestDescription,appointmentDate | doPost → Web6.customer → VehicleDAO6.findById → SlotDAO6.findById/holdSlot → new Appointment6 → confirm | vehicle/appointmentSlot gửi id int; fullName/phone/email readonly bị bỏ qua khi POST; cus từ server |
| BookAppointmentView6.jsp thêm xe | POST /booking action=addVehicle, cùng context và input đang nhập | doPost lưu form fields vào c rồi redirect GET /vehicle?context=... | ngày/xe/mô tả lưu trong BookingContext6 của session qua redirect |
| AddNewVehicleView6.jsp | POST /vehicle; vehicleBrand=id,vehicleModel,licensePlate,manufactureYear; csrfToken,context | VehicleServlet6.doPost tạo Vehicle6/VehicleBrand6 → VehicleDAO6.saveVehicle → redirect /booking | customerId lấy từ Web6.customer; brand list VehicleBrand6[]; năm Integer; km không input |
| ConfirmView6.jsp | POST /appointment csrfToken,context; id=confirmBooking | AppointmentServlet6.doPost lấy c.draft/c.holdCode → validateHold → createAppointment → PRG /booking?action=confirm | holdCode attribute có ở request nhưng không gửi token qua hidden form; server dùng session |
| ConfirmView6.jsp edit | POST /booking action=edit,csrfToken,context | BookingServlet6.doPost → Web6.release → SlotDAO6.releaseSlot → redirect form | giữ các scalar ngày/xe/mô tả trong c, xóa draft/token/expiry |
| ManagerHomeView6.jsp / ReportMenuView6.jsp | GET /revenue?action=menu rồi action=filter | RevenueServlet6.doGet → đúng menu tồn tại rồi filter | Không xóa menu để rút sequence; mục báo cáo khách hàng disable ngoài phạm vi |
| RevenueStatisticsFilterView6.jsp | GET /revenue action=list; startDate,endDate,statisticType=ALL/SERVICE/PART | RevenueServlet6.doGet → reportFilter → list → RevenueDAO6 | request dates String; DAO dates java.util.Date; không phải statisticTypeIn |
| RevenueStatisticsListView6.jsp | link GET /service-part; itemType,itemCode,startDate,endDate,statisticType,page | ServicePartServlet6.doGet | revenueStats RevenueStats6[] cắt trang; totalRevenue BigDecimal toàn tập; currentPage,totalPages,totalRows int |
| ServicePartDetailView6.jsp | itemType=SERVICE/PART; itemCode=serviceCode/partCode; usagePage; invoice link action=invoice&usageId=u.id | ServicePartServlet6 tải lại item/history/lifetime, xác minh membership rồi tra invoiceCode và redirect /invoice | service/ sparePart; serviceUsageList/partUsageList mảng cắt trang; lifetimeUsageCount int; reportPage giữ trang báo cáo, currentPage là trang usage |
| InvoiceDetailView6.jsp | invoiceCode và filter/page/usagePage/itemType/itemCode trong URL ngữ cảnh | InvoiceServlet6.doGet → getInvoiceDetail → view | invoice:Invoice6 đầy đủ; backUrl String; order.ss người bán, payment.ss người thu tiền |
| shared/top/pagination/bottom.jspf | logout POST /login action=logout; previousUrl/nextUrl/backUrl | include server; Web6.url encode tham số nội bộ | locale vi_VN; timezone cố định Asia/Ho_Chi_Minh; ngày thống kê caption ISO nhưng ngày dòng fmt |
| ErrorView6.jsp / NotFoundView6.jsp | message/errorStatus render c:out; index.jsp redirect login | Web6.error hoặc error-page 404 | Exception chi tiết log server, không in SQL/secret/stack lên UI |

Nguồn call site: src/main/webapp/WEB-INF/views/**/*.jsp; src/main/webapp/assets/app.js:17–45; src/main/java/garaman/servlet/*.java; Web6.java:16–66; web.xml:4–11. Bảng truy vết file 02 cho từng bước cung cấp số dòng cụ thể.

## 4. Đóng gói và tất cả thao tác ghi DB liên quan

Kết luận: **không có** luồng JSP phía server tạo entity → giữ/nhận object → chuyển object đó cho Servlet lưu. JSP dùng EL/JSTL và HTTP params. Hai write model từ form được tạo trong Servlet. Mapper đóng gói entity **sau SELECT** là luồng đọc/readback, không phải bước JSP đóng gói trước lưu. UPDATE trực tiếp bằng scalar không có lý do ép thêm entity.

| Thao tác ghi thực | Điểm tạo/cập nhật entity | Nơi giữ dữ liệu/đối tượng | Đường đi đến DAO / phương thức lưu | Transaction và bằng chứng file:dòng | Kết luận vị trí đóng gói |
|---|---|---|---|---|---|
| Giữ slot: UPDATE HELD, token, expiry | Không có entity trước UPDATE; SlotDAO6 tạo token scalar; RowMapper6.slot tạo AppointmentSlot6 sau SELECT lại | Token/expiry của held đưa vào c ở session | POST /booking → BookingServlet6.doPost → SlotDAO6.holdSlot(int slotId) | SlotDAO6.java:26–35 atomic guarded UPDATE autocommit, affected=1 rồi SELECT theo id/token; BookingServlet6.java:48–50 | Không áp mẫu tạo entity cho UPDATE scalar; không phải JSP tạo AppointmentSlot6 |
| Tiêu thụ hold: UPDATE BOOKED, xóa token/expiry | Không new slot; dùng slotId/holdCode của draft/context | Tham số của AppointmentDAO6 trên cùng connection | AppointmentDAO6.createAppointment → new SlotDAO6(con).consumeHold → changeHold(...true) | AppointmentDAO6.java:17–19; SlotDAO6.java:44–52; yêu cầu autocommit=false, khóa đến commit/rollback của tạo lịch | Scalar đúng kỹ thuật; không có entity trả từ boolean |
| Tăng sequence LH | Không entity cấp mã | lastValue/prefix/code cục bộ | AppointmentDAO6 → CodeSequenceDAO6(con).nextCode("LH") | AppointmentDAO6.java:24; CodeSequenceDAO6.java:9–25 FOR UPDATE + UPDATE; cùng tx INSERT lịch | Q07 cho phép; String return không phải entity |
| INSERT lịch hẹn | BookingServlet6.doPost new Appointment6 + setter; lúc xác nhận AppointmentServlet6 chỉ lấy lại c.draft | session.bookingContexts[context].draft; completed nhận saved sau commit | Book form POST → BookingServlet6:51 → Confirm forward → POST /appointment → AppointmentServlet6:23 → AppointmentDAO6.createAppointment(appointment,holdCode) | AppointmentDAO6.java:11–33: consume → ownership FOR SHARE → sequence → INSERT → RowMapper6.appointment readback → commit → return; lỗi rollback. saleStaffId NULL vẫn FK | **Khác yêu cầu vị trí đóng gói (D001)**; không được viết JSP tạo object hoặc xác nhận tạo draft lần hai |
| Tăng sequence X | Không entity cấp mã | scalar trong VehicleDAO6 | VehicleDAO6.saveVehicle → CodeSequenceDAO6(con).nextCode("X") | VehicleDAO6.java:33–42; sequence+INSERT xe cùng tx | Scalar, cùng cơ chế được Q07 chấp thuận |
| INSERT xe | VehicleServlet6.doPost new Vehicle6/new VehicleBrand6 + setter; DAO chuẩn hóa plate/validate nhưng không gọi setter cập nhật object đầu vào | v local; saved đọc lại, c.vehicleId=saved.id giữ qua redirect | AddNewVehicle form POST → VehicleServlet6:21–25 → VehicleDAO6.saveVehicle(v,serverCustomerId) | VehicleDAO6.java:24–48 kiểm tra hãng, sequence, INSERT, findById readback trước commit; owner commit rồi return; rollback và duplicate translated | **Khác yêu cầu vị trí đóng gói (D002)**; currentMileage=0 theo Q08, năm có thể NULL |
| Giải phóng slot: UPDATE AVAILABLE, clear hold fields | Không entity mới; dùng c.slotId/c.holdCode | context thuộc session; sau đó clear draft/token/expiry | POST edit → Web6.release → SlotDAO6.releaseSlot → changeHold(...false); logout/sessionDestroyed → releaseAll → release | Web6.java:43–49; SlotDAO6.java:43,49–52; BookingServlet6.java:28–30; LoginServlet6.java:17,25; BookingSessionListener6.java:9–10. Một UPDATE token-matched autocommit khi dùng DAO mặc định | Không áp mẫu entity; không release theo slotId đơn độc; đóng tab chỉ dựa expiry, không có tín hiệu browser tự release |
| Chuẩn bị slot CLI: INSERT ... ON DUPLICATE KEY UPDATE startTime=startTime | Không entity; Timestamp từ lịch cấu hình | Biến loop của CLI | SlotSetup6.main → prepareSlots() | SlotSetup6.java:11–26; mỗi câu INSERT autocommit; review chạy chỉ garaman6_it, dọn các slot mới. GET danh sách không gọi hàm này | Bootstrap kỹ thuật, không phải GUI ghi lịch |
| Cài schema/migrate/seed và marker tblSchemaVersion6 | SQL params/dòng seed; không entity Java từ form | Script setup/local fixture | scripts/local_runtime.py:69–105, sql/V1…,V2…,seed…; seed_data.py | DDL có implicit commit MySQL; seed trong transaction riêng khi cài mới; review **không chạy lại setup/migration/seed trên main**. Marker kỹ thuật khác DDL được ghi D057 | Không phải luồng lưu GUI; không bịa JSP đóng gói các hàng SQL |

Đối với constructor DAO mượn connection, createAppointment/saveVehicle yêu cầu caller đã mở transaction và không tự commit; return khi đó không chứng minh persistence durable. Đường production HTTP dùng DAO mặc định sở hữu, commit trước khi trả; V3 đã chỉ rõ owner=true. Review không gán cùng ý nghĩa “đã commit” cho mọi overload.

## 5. SQL và mapping đã kiểm tra trên schema thật

Nguồn metadata: [schema-live.json](evidence/schema-live.json); không có dump credential/row người dùng. MySQL9.4/InnoDB, strict SQL + ONLY_FULL_GROUP_BY; native Windows lower_case_table_names=1, tên table/view lưu lowercase.

| Nhóm | DDL/khóa/null/default/index | Mapping và ý nghĩa review |
|---|---|---|
| Member/Customer/Employee | V1:3–19; Member id PK; Customer.memberId/Employee.memberId PK+FK; code/username unique, account nullable, jobTitle CHECK | RowMapper6.member/customer/employee; shared Member.id; không bảng role rỗng/account riêng |
| Vehicle/Brand | V1:20–28 + V2:2–5; vehicleCode/plate unique; customer/brand FK not null; km NOT NULL DEFAULT0 CHECK>=0; year NULL | Vehicle6 có Integer year, int mileage; findById lọc id, list lọc customerId; year tương lai kiểm ở Java. Mâu thuẫn nullable Km D042 |
| Slot | V1:29–37 + V2:6; DATETIME(6), startTime unique; status CHECK; HELD phải có token/expiry, trạng thái khác phải NULL cả hai | getAvailableSlots không UPDATE expired; hold guarded atomic update; release/consume clear fields; boolean affected rows có ý nghĩa |
| Appointment | V1:38–51; code unique; FK customer/vehicle/slot; saleStaff nullable vẫn FK; generated activeSlotId unique có điều kiện status | capacity1 cho lịch hoạt động, không unique vô điều kiện lịch sử slot. Ownership còn được kiểm trong tx vì FK riêng không tự đảm bảo xe thuộc cus |
| Service/SparePart/Brand/Category | V1:52–66; codes unique, giá DECIMAL(18,2) CHECK>=0; part brand/category FK; mô tả/ảnh/spec nullable | RowMapper6.service/part; part stock từ view; không thêm id vào RevenueStats6 |
| RepairOrder | V1:67–75; customer/vehicle/employee FK, appointmentId nullable, completionTime nullable | RowMapper6.order nạp graph nông; không join usage để tránh recursion; đọc lại appointment khi FK khác NULL có thật |
| ServiceUsage/PartUsage | V1:76–91; order/item/technician FK; saleUnitPrice DECIMAL CHECK>=0; quantity>0; index item+order | usageCount=COUNT dòng; part revenue nhân quantity, không SUM quantity làm count. public returned arrays; dao rows ArrayList |
| Payment/Invoice | V1:92–104; payment status CHECK, paymentTime/method có thể NULL, amount NOT NULL khi có Payment; Invoice.paymentId NULL UNIQUE FK, orderId NOT NULL UNIQUE; issueDate index | Invoice không có payment được map payment=null; chưa trả tiền không phải một fake PAID payment. Người bán lấy order.ss, người thu lấy payment.ss |
| Receipt/Supplier/ReceiptDetail | V1:106–120; FK receipt/part/employee, receipt qty>0/price>=0 | Chỉ persistence/fixture để tính tồn; không có module nhập kho UI trong phạm vi |
| Views | V1:121–130 | vwInvoice6 pre-aggregate service/part riêng, không nhân tiền do nhiều dòng hai loại; vwSparePart6 nhập trừ xuất |
| Sequence và version | V2:7–13 tblCodeSequence6 prefix PK,lastValue BIGINT; local_runtime.py:99 tblSchemaVersion6 | Sequence LH/X khóa dòng transaction; MAX chỉ migration/backfill. Marker có thật ngoài schema tổng hợp, không thay thế kiểm ràng buộc live |

RevenueDAO6.java:16–26 và hai UsageDAO6.java:18–23 thống nhất issueDate, PAID và [start,nextDay(end)). DateUtil6.java:9–12, DAO6.java:16–19, AppFilter6.java:17 và shared/top.jspf:3 đồng nhất giờ Việt Nam trong cấu hình đã chạy. Config có thể đổi ZoneId nhưng DAO/JSP vẫn cố định +07/Vietnam: không suy rộng kết quả sang timezone khác. Không thay config trong lượt này.

Revenue SQL lấy lịch sử giá usage, không dùng catalog price, không có thuế/giảm giá. Danh sách thống kê sắp xếp totalRevenue DESC,itemCode ASC,itemType ASC; chi tiết issueDate DESC,u.id DESC. Phân trang 20 ở Servlet trên mảng đã query toàn tập; tổng toàn tập. Invoice byOrder không nhận ngày/bộ lọc, trả tất cả dòng đúng order FK.

## 6. Bảng toàn bộ khác biệt

Cột Code thực tế dùng đường dẫn rút gọn khi đã nêu thư mục cùng câu: Servlet trong src/main/java/garaman/servlet, DAO/mapper trong garaman/dao, entity trong garaman/entity, utility trong garaman/util; JSP nằm dưới src/main/webapp/WEB-INF/views/(customer|manager|auth). Inventory/bảng truy vết cung cấp đường dẫn đầy đủ. Mức runtime/tĩnh được ghi riêng; các mục chưa đủ bằng chứng không phải kết luận code sai.

| Mã | Module/tầng | Nguồn thiết kế và vị trí | Thiết kế/yêu cầu | Code thực tế (file:dòng) | Phân loại/mức ảnh hưởng | Tác động | Đề xuất sửa code hay sửa thiết kế |
|---|---|---|---|---|---|---|---|
| D001 | M1/lưu lịch | Prompt review §3; bài giảng trang in 110; LAB trang 13 | Kiểm tra đường giao diện xử lý → entity → giao diện → Servlet/DAO. | JSP chỉ submit params; BookingServlet6.doPost tạo Appointment6() và setCus/setVehicle/setSlot/setRequestDescription tại src/main/java/garaman/servlet/BookingServlet6.java:51; c.draft trong session; AppointmentServlet6:23 lấy lại rồi gọi DAO. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | KHÁC YÊU CẦU VỊ TRÍ ĐÓNG GÓI. Constructor và setter không nằm trong JSP; trước Confirm có draft, lúc xác nhận không tạo lại. | Chốt giữ controller theo LAB hay thêm JSP xử lý server có kiểm soát; phụ lục báo cáo 03, chưa sửa. |
| D002 | M1/lưu xe | Prompt review §3; AddNewVehicleView6 trong tk_md1.jpg | Kiểm tra JSP tạo Vehicle6 rồi chuyển đối tượng tới Servlet lưu. | src/main/java/garaman/servlet/VehicleServlet6.java:21-24 tạo Vehicle6(), VehicleBrand6(), setter, gọi saveVehicle; JSP form tại src/main/webapp/WEB-INF/views/customer/AddNewVehicleView6.jsp:7-14 chỉ gửi chuỗi/id. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | KHÁC YÊU CẦU VỊ TRÍ ĐÓNG GÓI; cùng sai khác kiến trúc với D001. | Cập nhật thiết kế vị trí đóng gói hoặc đề xuất xử lý server trung gian, không gán trách nhiệm cho HTML. |
| D003 | M1/DAO | Scenario V2 dán hội thoại bước 3–8,11–13,17–21,25–30; tk_md1.jpg | V2 viết Customer6/Vehicle6/AppointmentSlot6/Appointment6 tự thực hiện truy vấn/lưu. | src/main/java/garaman/util/Web6.java:30; servlet/BookingServlet6.java:43-51,60-61; servlet/AppointmentServlet6.java:20-23 gọi DAO; entity chỉ dữ liệu/get/set. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Cần đưa DAO/helper/Servlet vào V3; không dùng Customer6.getCustomerInfo giả. | Sửa scenario/communication/sequence theo call site thật; các file Java không thay đổi. |
| D004 | M2/DAO | Scenario V2 hội thoại bước 9–11,15–23,27–41; tk_md2.jpg | V2 viết entity RevenueStats6/Service6/.../Invoice6 tự truy vấn. | src/main/java/garaman/servlet/RevenueServlet6.java:38; ServicePartServlet6.java:23-42; InvoiceServlet6.java:16 gọi các DAO, DAO gọi RowMapper6/UsageMapper6. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Entity không có các hàm persistence nêu trong V2; cần giữ activation mapper/DAO phụ. | Sửa sequence và V3; entity không nhận trách nhiệm truy vấn. |
| D005 | Hai module/điều hướng | V2 các câu JSP/lớp View gọi View tiếp; tk_md1.jpg/tk_md2.jpg | Chuyển màn hình thường được viết trực tiếp View→View. | Annotation /booking,/appointment,/vehicle,/revenue,/service-part,/invoice trong 6 Servlet; src/main/java/garaman/util/Web6.java:16-18 forward vào WEB-INF; web.xml:9 cấm scripting. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | GET/POST/forward/redirect có scope khác nhau; JSP không gọi trực tiếp JSP kế tiếp. | Đưa route/container/Servlet/Web6 và scope vào sequence, không vẽ self-call handler. |
| D006 | M2/thứ tự | V2 bước 18–23,30–35; Prompt review §6.4 cấu trúc mong muốn | Lấy lifetime count rồi lấy lịch sử trong kỳ. | src/main/java/garaman/servlet/ServicePartServlet6.java:25-26,32-33 lấy getAll… trước, getTotalUsageCount sau. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Kết quả happy case vẫn đúng nhưng thứ tự message không khớp thiết kế. | Sửa thứ tự sequence nếu giữ code; V3 đã theo code. |
| D007 | M2/Xem hóa đơn | V2 bước 37–40; Q15 | Từ lần sử dụng đi tra mã/chi tiết hóa đơn, không mô tả tải lại chi tiết. | src/main/java/garaman/servlet/ServicePartServlet6.java:21-44 chạy lại getPartInfo/getAllPartUsageOfPart/getTotalUsageCount (hoặc nhánh service), kiểm tra containsUsage rồi getInvoiceCodeByUsage và redirect. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Có các lời gọi đọc bổ sung để xác minh dòng thuộc bộ lọc; không được lược mất khỏi activation hiện trạng. | Bổ sung call graph/sequence; tối ưu riêng nếu cần, giữ kiểm tra membership. |
| D008 | M1/chọn ngày | V2 bước 10–14 | Chỉ lấy các slot và giao diện hiển thị slot. | src/main/webapp/assets/app.js:22-35 fetch Accept:text/html; BookingServlet6.java:17-21,57-62 nạp lại profile+vehicles+slots, forward toàn bộ BookAppointmentView6 rồi client trích .slot-fieldset. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Không có API JSON; truy vấn lại khách/xe; DOM giữ input ngoài fieldset. | Sửa activity/HTTP/sequence thành HTML fetch + DOMParser, không bịa JSON. |
| D009 | M1/mở form | V2 bước 9 rồi bước 10 chọn ngày | Lấy slot sau lần chọn ngày đầu tiên. | src/main/java/garaman/util/BookingContext6.java:18 mặc định ngày mai; BookingServlet6.java:61 lấy slot ngay lần mở form. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Mở form đã có sẵn slot, trước sự kiện đổi ngày. | Thêm bước query slot lần đầu vào thiết kế. |
| D010 | M1/context | Ảnh tk_md1.jpg; Q03–Q05/Q18 | Ảnh không có BookingContext6/Map UUID/session locking/CSRF. | src/main/java/garaman/util/BookingContext6.java:8-18; Web6.java:32-46; BookingServlet6.java:18,26; AppointmentServlet6.java:16; AppFilter6.java:24-27. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Một context mỗi tab, token và completed ở server; giới hạn 64 context/phiên; chống nhầm tab và double-submit trong phiên. | Thêm lớp kỹ thuật, scope/session, lock và token vào thiết kế; không đổi entity nghiệp vụ. |
| D011 | M1/thành công | V2 bước 30–31; ConfirmView6 trong tk_md1.jpg | Thông báo sau ghi nhận tại ConfirmView. | src/main/java/garaman/servlet/AppointmentServlet6.java:23-26 lưu completed rồi PRG; BookingServlet6.java:64-68 forward chính ConfirmView6.jsp:6-18 nhánh completed. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Có redirect và request mới; không có SuccessView6 riêng; success lấy từ session context. | Ghi PRG và cùng một JSP hai trạng thái vào sequence/state UI. |
| D012 | M1/chỉnh sửa | Q03–Q05 dòng mô tả giữ ngày/xe/yêu cầu trong draft | Giữ dữ liệu nháp khi edit; release token cũ. | src/main/java/garaman/util/Web6.java:43-46 xóa cả c.draft nhưng giữ appointmentDate/vehicleId/slotId/requestDescription trên context; BookingServlet6.java:28-30 redirect form. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Thông tin vẫn giữ nhưng đối tượng Appointment6 draft không còn; lần hold kế tiếp mới dựng draft khác. | Ghi rõ dữ liệu nháp ở BookingContext6, không nói giữ cùng tham chiếu Appointment6. |
| D013 | Hai module/xác thực | Q01 thay thế thiết kế ban đầu thiếu login | Đăng nhập thật, không chọn role/id giả. | LoginServlet6.java:11-35; MemberDAO6.java:11-19; AuthFilter6.java:10-18; PasswordUtil6.java:10-34; LoginView6.jsp:9-15. | Thay đổi nghiệp vụ đã được chấp thuận | Bổ sung đăng nhập/đăng xuất, PBKDF2, subtype và sessionID mới; không mở module quản trị tài khoản. | Đồng bộ class/package/route/tiền điều kiện; không ghi là mở rộng nghiệp vụ trái phép. |
| D014 | M1/liên hệ | Q02 thay V2 bước 15 và control nhập trong tk_md1.jpg | Họ tên/điện thoại/email trước đây nằm form có thể bị hiểu là khách nhập. | BookAppointmentView6.jsp:22-24 readonly; BookingServlet6.java:42,51; Web6.java:27-30 tải lại từ session/DB, bỏ qua contact giả mạo. | Thay đổi nghiệp vụ đã được chấp thuận | Không có contact snapshot mới trong Appointment6; chỉ cus; profile thay đổi ở module khác có thể phản ánh khi đọc lại. | Sửa UI và scenario thành kiểm tra thông tin chỉ đọc. |
| D015 | M1/trạng thái | Q06; V2 dùng chuỗi tiếng Việt | Lịch mới “Chờ tiếp nhận”, nhân viên chưa tiếp nhận có thể NULL. | AppointmentDAO6.java:25 ghi PENDING và saleStaffId=NULL; ConfirmView6.jsp:16 dịch nhãn; RowMapper6.java:34-35 giữ null nhưng DDL FK vẫn tồn tại. | Thay đổi nghiệp vụ đã được chấp thuận | Trạng thái lưu là String mã kỹ thuật; không ghi chuỗi nhãn tiếng Việt vào DB. | Cập nhật state/mapping giữa mã và nhãn, không bỏ FK. |
| D016 | M1/slot | Q03–Q05 thay lịch minh họa tĩnh; tk_md1.jpg | Slot sức chứa 1; lịch demo cấu hình, 30 ngày, chuẩn bị tách khỏi GET. | sql/V2__booking_changes.sql:6 unique startTime; SlotSetup6.java:11-21 sinh tương lai idempotent; SlotDAO6.java:14-18 chỉ SELECT khi mở form. | Thay đổi nghiệp vụ đã được chấp thuận | Có CLI SlotSetup6, không seed slot bằng JS; UNIQUE activeSlotId vẫn là ràng buộc lịch hoạt động riêng. | Thêm init/unique và lịch cấu hình vào deployment/activity. |
| D017 | M1/mã xe | Q07 thay Vehicle6 trong tk_md1.jpg/test_tk.jpg không có vehicleCode | Thêm mã nghiệp vụ xe X0001…; PK/FK vẫn id. | src/main/java/garaman/entity/Vehicle6.java:5,16-17; VehicleDAO6.java:37-40; sql/V2__booking_changes.sql:2-4. | Thay đổi nghiệp vụ đã được chấp thuận | vehicleCode mới là String unique; form gửi vehicle.id, không gửi mã X để lọc FK. | Cập nhật class/DDL/mapping, không đổi id thành code. |
| D018 | M1/cấp mã | Q07 cho phép bảng kỹ thuật; ảnh cũ chỉ có DAO mặc định | Bổ sung tblCodeSequence6 và CodeSequenceDAO6 dùng transaction caller. | sql/V2__booking_changes.sql:7-13; CodeSequenceDAO6.java:9-25; AppointmentDAO6.java:24; VehicleDAO6.java:37. | Thay đổi nghiệp vụ đã được chấp thuận | Cấp mã khóa FOR UPDATE; migration MAX dùng một lần, runtime không MAX+1; constructor Connection có căn cứ đã chốt. | Thêm lớp/bảng/lock trong thiết kế; không coi bảng cấp mã là tự ý thêm. |
| D019 | M1/năm xe | Q08 thay manufactureYear:int trong tk_md1.jpg | Năm tùy chọn phải phản ánh NULL. | Vehicle6.java:9,24-25 dùng Integer; RowMapper6.java:48 getObject(Integer.class); VehicleDAO6.java:40 setObject(...Types.INTEGER); AddNewVehicleView6.jsp:12 không required. | Thay đổi nghiệp vụ đã được chấp thuận | Phân biệt năm chưa biết với 0; đây là thay đổi public type có chấp thuận. | Cập nhật field/get/set/SQL nullable trong thiết kế. |
| D020 | M2/quan hệ usage | Q14 thay tên attribute:RepairOrder6 ở test_tk.jpg; tk_md2.jpg đã dùng order | Dùng order:RepairOrder6; không đổi mảng DAO hay thêm cột. | ServiceUsage6.java:12; PartUsage6.java:13; UsageMapper6.java:11,16; ServicePartDetailView6.jsp:10,18 dùng u.order.*. | Thay đổi nghiệp vụ đã được chấp thuận | Ảnh toàn hệ thống và ảnh module khác nhau về tên; Q14 giải quyết theo order. | Sửa ảnh toàn hệ thống/EL liên quan, giữ FK repairOrderId. |
| D021 | Hai module/kế thừa/account | Q17 thay cách hiểu entity=bảng trong ảnh hệ thống | Member account gộp; Customer/Employee shared PK; subtype theo jobTitle. | sql/V1__reconstructed_baseline.sql:3-19; RowMapper6.java:9-43; không có tblAccount6 hay bảng Manager/SaleStaff/Technician riêng. | Thay đổi nghiệp vụ đã được chấp thuận | Account6.id dùng Member.id; Customer6.id là Member.id, không có customer.id thứ hai. | Cập nhật ORM/DB mapping; vẫn ghi thiếu SQL gốc tại D043. |
| D022 | M2/rating | Q16 và yêu cầu người dùng thay giao diện mẫu.pdf trang 5 | Bỏ dòng đánh giá khách hàng trong hai module. | ServicePartDetailView6.jsp:5-25 và RevenueStatisticsListView6.jsp:6-14 không có rating; không entity/DAO/bảng rating trong inventory/schema. | Thay đổi nghiệp vụ đã được chấp thuận | UI hiện tại đã bỏ đánh giá; không phải field thiếu. | Cập nhật mockup mẫu, không thêm rating trở lại. |
| D023 | M2/hóa đơn nhân viên | Ghi chú tay giao diện mẫu.pdf trang 6; yêu cầu trực tiếp bổ sung nhân viên bán hàng | Hóa đơn cần người bán hàng. | InvoiceDetailView6.jsp:9 hiển thị order.ss fullName/employeeCode, :21 hiển thị payment.ss là người thu tiền riêng; RowMapper6.java:77 và InvoiceDAO6.java:30 nạp hai FK độc lập. | Thay đổi nghiệp vụ đã được chấp thuận | UI hiện tại có cả người bán và người thu tiền; không được nhầm 2 tên, seed Lan/Ngân khác nhau. | Đồng bộ mockup/sequence hai lần nạp nhân viên và vai trò. |
| D024 | M2/trường dẫn xuất | Q09–Q13; entity có totalAmount/stockQuantity nhưng Q17 baseline không lưu cột đó | Tổng hóa đơn/tồn kho tính từ dữ liệu chi tiết. | sql/V1__reconstructed_baseline.sql:121-130 tạo vwInvoice6/vwSparePart6; InvoiceDAO6.java:18-22; SparePartDAO6.java:8 đọc view. | Thay đổi nghiệp vụ đã được chấp thuận | Java vẫn có field; không yêu cầu cột tổng/tồn vật lý hay bảng RevenueStats6. | Cập nhật sơ đồ ánh xạ/trường dẫn xuất, giữ nguồn giá lịch sử. |
| D025 | DAO/kết nối | tk_md1.jpg/tk_md2.jpg: DAO6 -con:Connection, không thể hiện owner/close | Connection private, DAO chủ yếu constructor mặc định. | src/main/java/garaman/dao/DAO6.java:9-28 implements AutoCloseable, con protected final, owner protected final; constructor Connection và close chỉ đóng connection sở hữu. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Visibility/finality/giao thức ownership khác ảnh; hỗ trợ transaction chia sẻ, không có connection static dùng chung. | Sửa class design và sequence lifetime connection, không giữ tx 3 phút. |
| D026 | DAO/helpers | Ảnh module không có RowMapper6/UsageMapper6 và các hàm findById/byOrder | Ảnh chỉ liệt kê method nghiệp vụ chính. | RowMapper6.java:7-91; UsageMapper6.java:6-17 package-private final, static methods; ServiceDAO6.java:15 và SparePartDAO6.java:16 findById package-private; UsageDAO6.byOrder package-private. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Thêm activation helper/DAO phụ thực sự; không biến các hàm phụ thành public hay entity methods. | Cập nhật class chi tiết và sequence, xem chữ ký chính xác ở phụ lục inventory. |
| D027 | M2/cách hydrate | Q14 nói DAO Usage JOIN RepairOrder/Member/Customer/Vehicle | Thiết kế dự kiến nạp lịch sử bằng JOIN dữ liệu hiển thị. | ServiceUsageDAO6.java:19,22; PartUsageDAO6.java:19,22 JOIN để lọc rồi gọi UsageMapper6; UsageMapper6.java:10-16 gọi DAO phụ/RowMapper6.order; order nạp thêm nhiều SELECT. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | N+1 thay truy vấn JOIN nạp graph; fixture đúng nhưng chưa đo tải lớn. Đọc riêng nhiều query có thể thấy thay đổi dữ liệu giữa thời điểm. | Cập nhật sequence/mô tả hydration; tối ưu/snapshot read riêng khi được yêu cầu, không khẳng định hiệu năng đã đo. |
| D028 | M2/graph nông | Q14; RepairOrder6 trong ảnh có các mảng usage | Ảnh quan hệ không nói mức hydrate của từng API. | RowMapper6.java:70-79 không nạp usage arrays; RepairOrder6.java:14-15 mặc định rỗng; InvoiceDAO6.java:24 chủ động nạp byOrder hai loại. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Không được coi usage.order.serviceUsage rỗng là không có lịch sử thật; tránh đệ quy vô hạn. | Tài liệu hóa hợp đồng nông/đầy đủ; V3 hóa đơn khai triển hai byOrder. |
| D029 | UI/thuộc tính | tk_md1.jpg/tk_md2.jpg các field tường minh/ẩn của View | Sơ đồ ghi field UI như cus,vehicleList,appointment,table/button. | JSP dùng form/EL/JSTL, attributes request và session context; không có Java instance field của lớp View trong src/main/java. Web6.java:16-18; BookAppointmentView6.jsp:6-32. | Khác biệt hình thức | Khác biểu diễn UML→JSP, không kết luận thiếu field chỉ vì không có Java class View. | Giữ bảng ánh xạ control/attribute của báo cáo, chú thích kiểu triển khai. |
| D030 | M2/kiểu ngày UI | tk_md2.jpg: startDate/endDate:Date ở view | Các field UI biểu diễn Date. | Web6.java:60 attributes startDate/endDate là String yyyy-MM-dd; ServicePartServlet6.java:19 và RevenueServlet6.java:38 chuyển qua LocalDate rồi Date; DAO public giữ java.util.Date. | Khác biệt hình thức | Không đổi chữ ký DAO thành String/LocalDate trong scenario; DateUtil tạo biên Timestamp cho SQL. | Cập nhật kiểu view model và conversion trong thiết kế. |
| D031 | M2/control loại | tk_md2.jpg RevenueStatisticsFilterView6.statisticTypeIn:Select | Tên control statisticTypeIn. | RevenueStatisticsFilterView6.jsp:9 name/id statisticType; Web6.java:59 đọc statisticType; filter helper RevenueServlet6.java:33 cũng cùng tên. | Khác biệt hình thức | Khác tên control có thể làm sơ đồ/tài liệu HTTP sai; code hiện tự nhất quán. | Đổi tên trong thiết kế/UI contract thành statisticType. |
| D032 | M1/control ngày/giờ | tk_md1.jpg: appointmentDate:Select, appointmentSlot:Select, tblAvailableDate/tblAvailableSlot:Table | Ngày/slot dạng Select và bảng ngày/slot theo bản vẽ UI. | BookAppointmentView6.jsp:11 input type=date name appointmentDate thay bảng chọn ngày; :15 radio name appointmentSlot value slot.id trong .slot-fieldset thay bảng/select slot; app.js:35 thay fieldset. | Khác biệt hình thức | Cùng dữ liệu nghiệp vụ nhưng khác control/ẩn hiện và thao tác DOM. | Cập nhật thuộc tính tường minh/bố cục UI; không viết dropdown JSON. |
| D033 | Hai module/nút/link | tk_md1.jpg: bookButton/continueButton/returnButton/editButton; tk_md2.jpg các link/table | Các tên khái niệm nút trên ảnh. | CustomerHomeView6.jsp:10 là link; BookAppointmentView6.jsp:32 button name=action value=hold; ConfirmView6.jsp:21 form action=edit và :22 id=confirmBooking; không có Java field button tương ứng. AddNewVehicleView6.jsp:11 nhận licensePlate tối đa 30 ký tự trình bày, DAO chuẩn hóa rồi giới hạn 20 tại VehicleDAO6.java:25-29. ReportMenuView6.jsp:7 dùng button disabled cho btnCustomerStatistics ngoài phạm vi thay link. | Khác biệt hình thức | Phân biệt nhãn/nút, id, name, action; continue không phải tên method. | Sửa UI dictionary cho đúng form/control, giữ ý nghĩa người dùng. |
| D034 | M2/tổng view | tk_md2.jpg RevenueStatisticsListView6.totalRevenue:double; Q09–Q13 yêu cầu đối chiếu decimal | View tổng double ở ảnh; entity double được giữ. | RevenueServlet6.java:40-41 totalRevenue attribute là BigDecimal; RevenueStats6.totalRevenue vẫn double, DAO tại RevenueDAO6.java:25 chuyển double trước khi cộng lại. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Cộng toàn tập bằng BigDecimal có ích nhưng không phục hồi phần chính xác đã mất ở biên double, xem D051. | Ghi kiểu view model khác entity; chốt kiểm soát biên/kiểu tiền sau. |
| D035 | M2/format ngày | giao diện mẫu.pdf trang 3–5 hiển thị ngày kiểu dd/MM/yyyy | Ngày hiển thị dạng ngày/tháng/năm. | RevenueStatisticsListView6.jsp:6 và ServicePartDetailView6.jsp:22 in startDate/endDate String ISO; lịch sử/invoice dùng fmt pattern dd/MM/yyyy HH:mm tại detail:10,18 và invoice:7. | Khác biệt hình thức | Caption khoảng thống kê dùng yyyy-MM-dd, còn cột ngày dùng dd/MM; không phải đổi căn cứ lọc. | Thống nhất UI format trong thiết kế hoặc đề xuất format caption. |
| D036 | M2/mã mẫu | V2 hội thoại bước 26 ghi PT005; Q07 dùng PT0001… | Ví dụ mã PT005. | Seed tháng 10 và link hiện tại chọn PT0005; SparePartDAO6.java:11-13 lọc đúng partCode, không tự thêm/bớt số 0; Q07 không yêu cầu sửa mọi mã cũ. | Khác biệt hình thức | PT005 và PT0005 là hai chuỗi khác; không dùng PT005 trong happy case của fixture PT0005. | Ghi đúng mã dữ liệu và tên partCode; không tự migration đổi mã cũ. |
| D037 | DB/tên FK | grm_db.jpg: các FK dạng tblMember6id/tblRepairOrder6id, technicalSpec | Tên cột theo ảnh drawDB. | sql/V1__reconstructed_baseline.sql:10-19,59-89 dùng memberId/customerId/repairOrderId/technicalSpecifications; SHOW CREATE TABLE trong evidence/schema-live.json xác nhận tên đang chạy. | Khác biệt hình thức | Khác tên vật lý, không suy ra mất quan hệ; SQL gốc không có nên không chứng minh đã bảo toàn V1 cũ. | Cập nhật physical model theo schema thật, giữ cảnh báo provenance D043. |
| D038 | DB/Payment | grm_db.jpg: khối tblInvoice6 có các thông tin thanh toán; Q17/baseline mô tả paymentId | Ảnh drawDB không đồng bộ việc tách Payment với baseline mô tả. | sql/V1__reconstructed_baseline.sql:92-104 có tblPayment6 riêng và Invoice.paymentId NULL UNIQUE FK; InvoiceDAO6.java:25-31 nạp Payment6. | Thay đổi nghiệp vụ đã được chấp thuận | Hóa đơn có thể chưa có payment; các chi tiết thanh toán chưa có không buộc giả giá trị. Không áp ảnh drawDB để nhập cột trở lại Invoice. | Cập nhật DB mapping theo quyết định/baseline hiện trạng; không khẳng định tương đương SQL gốc bị thiếu. |
| D039 | DB/chữ hoa thường | DDL nguồn dùng tblAppointment6/vwInvoice6… | Tên camel-case trong file SQL/Java. | MySQL native Windows @@lower_case_table_names=1; evidence/schema-live.json lưu tblappointment6/vwinvoice6… bằng lowercase, cột giữ case; DAO SQL camel-case vẫn chạy. | Khác biệt hình thức | Khác tên lưu trữ theo OS; chưa kiểm chứng Linux lower_case_table_names=0. | Ghi rõ môi trường và chuẩn đặt tên trước di chuyển, không tuyên bố WSL/Linux đã pass. |
| D040 | Quan hệ/slot-lịch | tk_md1.jpg AppointmentSlot6–Appointment6: tối đa một lịch theo đầu slot | Quan hệ lịch sử thể hiện 0..1. | sql/V1__reconstructed_baseline.sql:43-45 unique activeSlotId chỉ áp status PENDING/CONFIRMED/RECEIVED; không unique vô điều kiện appointmentSlotId. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Một slot có thể có nhiều lịch sử CANCELLED/COMPLETED, tối đa một lịch hoạt động; slot BOOKED không tự mở lại trong hai module. | Chú thích 0..* lịch sử và ràng buộc active≤1, không đổi ràng buộc thành UNIQUE(slotId). |
| D041 | Quan hệ/hãng-xe | tk_md1.jpg/test_tk.jpg VehicleBrand6→Vehicle6 1..*; tk_md2.jpg PartBrand6/Category6→SparePart6 và Technician6→PartUsage6 1..* | Mỗi hãng tối thiểu một xe trên ảnh. | DDL vehicleBrandId NOT NULL chỉ bắt mỗi xe có hãng; không bắt hãng phải có xe; seed có hãng Hyundai chưa có xe (sql/seed.sql). Các FK partBrandId/categoryId/technicianId tại V1:59-66,84-91 cũng không buộc từng danh mục/nhân viên có tối thiểu một dòng con. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | DB/ứng dụng cho phép danh mục/nhân viên chưa được dùng (0..*); FK chỉ buộc mỗi dòng con có cha hợp lệ. Hyundai chưa có xe là ví dụ fixture có thật. | Sửa multiplicity nếu muốn giữ danh mục hãng chưa được dùng. |
| D042 | M1/Km | Prompt review §5 mới: km nullable; Q08 trong quyết định v2: thêm mới currentMileage=0, không ô km; grm_db.jpg nullable | Hai nguồn yêu cầu mâu thuẫn, bản mới yêu cầu xác nhận lại theo hồ sơ. | Vehicle6.java:8 int; RowMapper6.java:48 getInt; VehicleDAO6.java:38 ghi 0; sql/V1__reconstructed_baseline.sql:23 NOT NULL DEFAULT 0 CHECK>=0. | Chưa đủ bằng chứng | Code khớp Q08, không đáp ứng nhánh nullable của checklist mới; chưa đủ căn cứ chốt nghĩa 0=đã biết hay chưa biết. | Cần chốt nguồn ưu tiên trước sửa public Integer/DDL/seed/mapper/UI; không tự đổi trong review. |
| D043 | Nguồn/SQL | Quyết định v2 §2 chọn GaraMan_CSDL(1).sql làm V1 | Cần SQL gốc để kiểm chứng migration bảo toàn baseline. | input không có file này; sql/V1__reconstructed_baseline.sql:1-2 ghi rõ reconstructed; sql/schema.sql là tổng hợp cài mới, schema thật đọc được. | Chưa đủ bằng chứng | Không thể chứng nhận migration V2 đã áp an toàn lên DB gốc người dùng hay tên cột cũ chính xác. | Bổ sung SQL gốc/snapshot cũ để review migration riêng; không reset DB hiện tại. |
| D044 | Nguồn/phiên bản ảnh | Quyết định v2 gọi test_tk(7).jpg,tk_md1(2).jpg,tk_md2(1).jpg | Cần đúng bản thiết kế cuối. | Trong input chỉ có test_tk.jpg,tk_md1.jpg,tk_md2.jpg và grm_db.jpg; không có lịch sử version/commit để chứng minh bằng bản có hậu tố. | Chưa đủ bằng chứng | Đã đọc ảnh có thật, không dùng mtime để khẳng định đúng bản cuối; kết luận so ảnh giới hạn ở bản cung cấp. | Ghi nguồn cụ thể, bổ sung phiên bản cuối khi có. |
| D045 | Nguồn/biểu đồ và mẫu | Prompt review §1–2/§6 | Cần pha phân tích, trạng thái, activity, communication/sequence gốc và file mẫu V3 độc lập. | Không tìm thấy các bản gốc độc lập trong input/docs/input (docs/input không tồn tại); docs/design/classes-final.puml và SCENARIO_V3.md là tài liệu sau code; hai PDF hướng dẫn ở thư mục PTTK cấp trên đã tìm và đọc. | Chưa đủ bằng chứng | Không đối chiếu được thông điệp/multiplicity ở biểu đồ chưa cung cấp; không tự dựng làm nguồn chuẩn cũ. | Dùng mẫu diễn đạt trong prompt + bài giảng/LAB, ghi rõ thiếu nguồn gốc. |
| D046 | Nguồn/chữ ký ảnh | tk_md1.jpg: VehicleBrandDAO6.getVehicleBrands vùng kiểu trả bị cắt | Cần đối chiếu chính xác kiểu trả cũ. | Phần return trong ảnh bị cắt; prompt triển khai/hồ sơ chốt mảng; code VehicleBrandDAO6.java:11-14 trả VehicleBrand6[]. | Chưa đủ bằng chứng | Không kết luận mảng khác List trên căn cứ hình bị cắt; không tự đọc chữ bị mất. | Lấy ảnh đầy đủ nếu cần xác nhận chữ ký cũ, hiện trạng đã có bytecode/source. |
| D047 | Tài liệu/sơ đồ sinh | docs/design/classes-final.puml; scripts/ExportContracts.java | Tài liệu “final” cần đủ helper/constructor/UI để thay thiết kế chi tiết. | scripts/ExportContracts.java:31 chỉ xuất public method vào UML, không có method package-private của RowMapper6/UsageMapper6/byOrder/findById và JSP; inventory reflection đầy đủ hơn. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Tên final không chứng minh đủ sequence contract; không dùng sơ đồ sinh làm nguồn độc lập chứng minh code tự khớp. | Đề xuất cập nhật export/sơ đồ sau; review này không chạy ghi đè file cũ. |
| D048 | Tài liệu/scenario cũ | docs/design/SCENARIO_V3.md | V3 cần lời gọi, trả, mapper, helper, điều hướng chi tiết. | Bản cũ rút gọn 12/8 bước, không khai triển các invocation và nhánh đọc lại của invoice; hai V3 mới theo source, bảng truy vết 370 bước riêng. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Bản rút gọn không đủ để vẽ sequence theo độ chi tiết prompt review. | Dùng hai file mới cho hiện trạng, giữ nguyên bản cũ làm dấu vết. |
| D049 | M1/validation thêm xe | Q18: input sai trả 400, giữ input an toàn | vehicleBrand không phải số phải là lỗi form. | VehicleServlet6.java:22,26,31 chuyển raw vehicleBrand='abc' sang attribute; AddNewVehicleView6.jsp:9 so sánh EL với b.id ép kiểu số, ném exception. Runtime POST thực tế nhận 500, evidence/runtime-review.json observations[0]. | Lỗi cần sửa | Trung bình: lỗi nhập liệu thành lỗi hệ thống; UI không lộ stack trace nhưng không đúng HTTP contract. | Đề xuất chuẩn hóa typed selectedBrandId/validation và render không ép raw chuỗi; thêm regression; chưa sửa. |
| D050 | M2/action invoice | Q18: tham số/đối tượng không hợp lệ 400/404; ServicePartServlet6 intent | usageId phải là id hợp lệ khi action=invoice. | ServicePartServlet6.java:21 target=0 khi usageId=0 và :40 chỉ xử lý invoice khi target!=0; GET thật nhận 200 trang chi tiết (runtime-review observations[1]). | Lỗi cần sửa | Thấp: yêu cầu Xem hóa đơn malformed bị coi là xem chi tiết; thiếu validation nhất quán. | Đề xuất tách action boolean khỏi sentinel, bắt usageId>0 cho invoice; kiểm thử HTTP 0/âm/thiếu. |
| D051 | M2/tiền lớn | Q09–Q13 kiểm soát biên double; DECIMAL(18,2) | Tổng/giá trong miền SQL không được mất phần lẻ âm thầm. | RevenueDAO6.java:25; RowMapper6.java:61,66; UsageMapper6.java:9,14; InvoiceDAO6.java:22,28 đổi BigDecimal→double. Test hóa đơn exact 9007199257840991.01 render data-value 9.007199257840992E15, lệch 0.99 (runtime-review observations[2]). | Lỗi cần sửa | Cao về tính đúng tiền: SQL và kiểm tra PAID BigDecimal đúng nhưng public model/UI mất chính xác; test .25 nhỏ pass không phủ hết DECIMAL. | Đề xuất chặn miền không biểu diễn an toàn hoặc chốt chuyển public money sang BigDecimal rồi cập nhật hợp đồng; không tự đổi kiểu. |
| D052 | M1/countdown | Q03–Q05: hold hiệu lực theo server 3 phút | Không khóa khả năng xác nhận chỉ vì đồng hồ máy khách chạy nhanh. | app.js:7,12 lấy expires-Date.now và disable confirmBooking. Runtime giả lập client nhanh 10 phút: nút disabled, DB vẫn có hold hợp lệ=1 (observations[3]); SlotDAO6.java:38-40 dùng NOW đúng. | Lỗi cần sửa | Trung bình: chặn khách hợp lệ tại UI; server vẫn chống nhận hold hết hạn. | Đề xuất tính độ lệch/remaining TTL từ server hoặc cho gửi xác nhận để server quyết định; test clock skew; chưa sửa. |
| D053 | M1/giờ hiển thị | Q03–Q05: lịch làm việc cấu hình được | Giờ thông báo phải thống nhất cấu hình sinh slot. | BookAppointmentView6.jsp:19 ghi cứng 08:00–17:00/nghỉ12:00–13:00; SlotSetup6.java:12-18 đọc GARAMAN_OPEN_HOUR/CLOSE_HOUR/BREAK/slotMinutes; không truyền các giá trị này cho JSP. | Lỗi cần sửa | Thấp, phát hiện tĩnh: đổi cấu hình lịch có thể làm caption sai dù radio lấy slot DB đúng; cấu hình mặc định đang thống nhất. | Đề xuất truyền nhãn giờ từ cùng cấu hình; chưa chạy đổi cấu hình/DB để tránh thay runtime hiện có. |
| D054 | Kiểm thử/cô lập | Prompt review §4.4/§8 cấm ghi DB demo/người dùng | Review runtime chỉ dùng schema test có dọn dẹp. | scripts/browser_test.py:8,29-32 target mặc định main garaman6, chạy thêm xe/lịch, không cleanup cuối; đợt review dùng import/in-memory override BASE/OUT/count_appointments vào testDB và cleanup riêng. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Không thể chạy nguyên script browser trên main để chứng minh review an toàn; script smoke còn có thao tác shutdown DB. | Đề xuất tham số hóa rõ target/DB guard và fixture cleanup ở lượt sửa sau; hiện không đổi script. |
| D055 | Auth/điều hướng quay lại | Q18 giữ URL quay lại whitelist nội bộ | Có thể hiểu giữ nguyên URL và query đã yêu cầu. | AuthFilter6.java:13 chỉ lưu servletPath của /booking,/vehicle,/revenue; LoginServlet6.java:28-29 redirect đường dẫn đã whitelist, không giữ query/filter/page và không nhớ /invoice,/service-part. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Giới hạn hiện trạng: khôi phục route cơ bản, không khôi phục đầy đủ ngữ cảnh báo cáo khi session hết hạn. | Ghi rõ giới hạn contract hoặc đề xuất lưu path+query đã whitelist/encode và kiểm tra scope, không tin return URL client. |
| D056 | M1/Servlet: tên tham số | tk_md1.jpg: BookingServlet6.doPost, AppointmentServlet6.doPost, VehicleServlet6.doPost | Ba doPost trong ảnh dùng request/response; các doGet và handler module 2 đã dùng req/resp. | Code dùng req,resp ở BookingServlet6.java:25, AppointmentServlet6.java:14, VehicleServlet6.java:17. Kiểu HttpServletRequest,HttpServletResponse, thứ tự và throws không đổi. doGet và handler module 2 không có khác biệt tên này. | Khác biệt hình thức | Đổi tên hình thức, không đổi chữ ký nhị phân hay nghiệp vụ; không gọi nhầm tên tham số khi ghi hợp đồng. | Cập nhật spelling trong inventory/sequence; không coi là lỗi. |
| D057 | DB/marker schema | DDL nguồn trong sql/ và sơ đồ DB; local_runtime.py | Baseline/migration/schema.sql mô tả các bảng nghiệp vụ và bảng cấp mã. | scripts/local_runtime.py:99 tạo thêm tblSchemaVersion6(version PK,appliedAt) tại setup; evidence/schema-live.json xác nhận 2 version. Kiểm tra DB đã có ở script:80-86 chủ yếu nhìn cột vehicleCode, không đối chiếu toàn bộ marker/index/FK. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Bảng kỹ thuật đánh dấu migration có thật nhưng không nằm schema.sql/ảnh; marker không tự chứng minh mọi migration/ràng buộc đã đúng. | Ghi vào deployment/schema inventory; đề xuất kiểm tra version và ràng buộc khi tái chạy setup, không sửa/tạo lại schema trong review. |
| D058 | DB/kiểu thời gian và số nguyên | grm_db.jpg: issueDate/startTime/holdExpiresAt/usageTime/createAt/createTime/paymentTime/receiptDate là timestamp, id integer(10) | Các cột thời gian timestamp theo ảnh; số nguyên có display width (10). | sql/V1__reconstructed_baseline.sql:30,40,69-70,77,85,94,101,111 dùng DATETIME(6); khóa/id INT không ghi display width. SHOW CREATE TABLE trong schema-live.json xác nhận datetime(6)/int. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Literal kiểu/độ chính xác thời gian khác ảnh; DATETIME không mang timezone và app phải diễn giải nhất quán. Không tuyên bố migration đổi kiểu từ DB gốc vì chưa có SQL gốc. | Cập nhật physical model kiểu và microseconds, đánh giá dữ liệu/timezone trước migration tương lai. |
| D059 | DB/nullable và độ dài status | grm_db.jpg: Member.status VARCHAR(50) nullable; Employee.jobTitle VARCHAR(50); Appointment.status VARCHAR(255) | Độ dài/nullable như ảnh; ảnh không đủ chứng minh CHECK/default có hay không trong DDL gốc. | sql/V1__reconstructed_baseline.sql:8 Member.status VARCHAR(30) NOT NULL DEFAULT ACTIVE; :16 jobTitle VARCHAR(30) NOT NULL CHECK; :41 Appointment.status VARCHAR(30) NOT NULL DEFAULT PENDING và :51 CHECK; schema-live.json xác nhận. | Thay đổi triển khai hợp lý cần cập nhật thiết kế | Khác kích thước/nullability và mã trạng thái bị giới hạn. Không được coi đó là bảo toàn nguyên SQL V1 cũ; dữ liệu NULL/giá trị dài cũ cần preflight khi có DB gốc. | Đồng bộ DB diagram/rule mapping; bổ sung đối chiếu migration khi có baseline gốc, không tự sửa dữ liệu cũ. |
| D060 | DB/kiểu tiền Payment | grm_db.jpg: tblInvoice6.paymentAmount double(10); Q09–Q13 tiền SQL dùng DECIMAL | Ảnh cũ lưu paymentAmount double trong Invoice, quyết định sau yêu cầu tiền SQL DECIMAL/đối chiếu BigDecimal. | sql/V1__reconstructed_baseline.sql:92-98 tách Payment và dùng paymentAmount DECIMAL(18,2) NOT NULL CHECK>=0; InvoiceDAO6.java:31 kiểm tra PAID bằng BigDecimal; Payment6.java:7 public field vẫn double. | Thay đổi nghiệp vụ đã được chấp thuận | SQL cải thiện độ chính xác nhưng biên Java vẫn có lỗi D051; part/service price DECIMAL trong ảnh vốn đã khớp, không ghi chúng thành thay đổi giả. | Cập nhật physical Payment và domain amount/precision; giữ cảnh báo D043 về nguồn V1. |
| D061 | Phạm vi/entity toàn hệ thống | test_tk.jpg: Supplier6,WarehouseStaff6,PartReceipt6,ReceiptDetail6,CustomerStats6,SupplierStats6,PartStats6,ServiceStats6; prompt chỉ hai module | Ảnh toàn hệ thống rộng hơn hai module đã giao. | Inventory 55 lớp không triển khai các lớp/UI/CRUD ngoài hai module này; Receipt/Supplier chỉ có bảng và seed làm nguồn tính tồn kho. Báo cáo doanh thu dùng RevenueStats6 chung như tk_md2.jpg, không tạo hai subclass thống kê riêng. | Thay đổi nghiệp vụ đã được chấp thuận | Chênh phạm vi có căn cứ từ yêu cầu triển khai hai module, không phải thiếu chức năng của happy case; không thể công bố toàn hệ thống đã được review/triển khai đầy đủ. | Ghi rõ subset trong package/class diagram; chỉ bổ sung module khác theo yêu cầu mới. |

## 7. Các điểm khớp quan trọng, không tạo “khác biệt” giả

- Tên SaleStaff6/InvoiceServlet6/createAt/cus/ss và thứ tự tham số DAO như inventory là có thật. Không có cặp tên sai chỉ vì prompt dùng ví dụ.
- RevenueStats6 không có id/bảng; public mảng không đổi thành List. Getter/setter đều có return type thật; void chỉ trả quyền điều khiển.
- Không tin contact/customerId phía client; xe được kiểm trước hold và trong transaction lưu; role từ DB/session, SQL dùng PreparedStatement.
- Giữ 3 phút bằng thời gian DB, không giữ transaction mở 3 phút. Kiểm tra validateHold bên ngoài không phải bảo vệ duy nhất: consume có guard trong tx và unique activeSlotId là chốt cuối.
- Đọc invoice đủ hai loại dòng, không lọc theo khoảng báo cáo; các query service/part được tổng hợp riêng tránh nhân tổng. PAID và issueDate đúng quyết định. Lifetime không lọc ngày/status.
- Không rating, có người bán/thu tiền. Khớp các điểm này không phủ định những lỗi và nguồn thiếu đã ghi.

## 8. Phụ lục chữ ký đầy đủ của 55 lớp

Sinh lại sau build bằng reflection dùng chính mã exporter nhưng đổi đích trong một bản tạm; không chạy exporter ghi đè CONTRACTS_GENERATED.md/classes-final.puml cũ. Không khởi tạo DAO khi reflection. Bản nguyên [contracts-reflection.md](evidence/contracts-reflection.md); bản javap độc lập ở trên. Class liệt kê theo tên package, thứ tự method do exporter sắp tên, **không phải thứ tự thực thi**. Runtime exceptions không cần có trong throws; kiểm tra chữ ký public lẫn package-private phía dưới.

### garaman.dao.AppointmentDAO6

```java
public class AppointmentDAO6 extends garaman.dao.DAO6 {
  public AppointmentDAO6();
  public AppointmentDAO6(java.sql.Connection con);
  public garaman.entity.Appointment6 createAppointment(garaman.entity.Appointment6 appointment, java.lang.String holdCode);
  private void rollback(boolean tx, java.lang.Exception cause);
}
```

### garaman.dao.CodeSequenceDAO6

```java
public class CodeSequenceDAO6 extends garaman.dao.DAO6 {
  public CodeSequenceDAO6(java.sql.Connection con);
  public java.lang.String nextCode(java.lang.String prefix);
}
```

### garaman.dao.CustomerDAO6

```java
public class CustomerDAO6 extends garaman.dao.DAO6 {
  public CustomerDAO6();
  public CustomerDAO6(java.sql.Connection con);
  public garaman.entity.Customer6 getCustomerInfo(java.lang.String customerCode);
}
```

### garaman.dao.DAO6

```java
public class DAO6 extends java.lang.Object implements java.lang.AutoCloseable {
  protected final java.sql.Connection con;
  protected final boolean owner;
  public DAO6();
  public DAO6(java.sql.Connection con);
  public void close();
}
```

### garaman.dao.InvoiceDAO6

```java
public class InvoiceDAO6 extends garaman.dao.DAO6 {
  public InvoiceDAO6();
  public InvoiceDAO6(java.sql.Connection con);
  public java.lang.String getInvoiceCodeByUsage(java.lang.String itemType, int usageId);
  public garaman.entity.Invoice6 getInvoiceDetail(java.lang.String invoiceCode);
}
```

### garaman.dao.MemberDAO6

```java
public class MemberDAO6 extends garaman.dao.DAO6 {
  public MemberDAO6();
  public MemberDAO6(java.sql.Connection con);
  public garaman.entity.Member6 authenticate(java.lang.String username, java.lang.String password);
}
```

### garaman.dao.PartUsageDAO6

```java
public class PartUsageDAO6 extends garaman.dao.DAO6 {
  public PartUsageDAO6();
  public PartUsageDAO6(java.sql.Connection con);
   garaman.entity.PartUsage6[] byOrder(int orderId) throws java.sql.SQLException;
  public garaman.entity.PartUsage6[] getAllPartUsageOfPart(java.lang.String partCode, java.util.Date startDate, java.util.Date endDate);
  public int getTotalUsageCount(java.lang.String partCode);
}
```

### garaman.dao.RevenueDAO6

```java
public class RevenueDAO6 extends garaman.dao.DAO6 {
  public RevenueDAO6();
  public RevenueDAO6(java.sql.Connection con);
  public garaman.entity.RevenueStats6[] getRevenueStatistics(java.util.Date startDate, java.util.Date endDate, java.lang.String statisticType);
}
```

### garaman.dao.RowMapper6

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

### garaman.dao.ServiceDAO6

```java
public class ServiceDAO6 extends garaman.dao.DAO6 {
  public ServiceDAO6();
  public ServiceDAO6(java.sql.Connection con);
   garaman.entity.Service6 findById(int id) throws java.sql.SQLException;
  public garaman.entity.Service6 getServiceInfo(java.lang.String serviceCode);
}
```

### garaman.dao.ServiceUsageDAO6

```java
public class ServiceUsageDAO6 extends garaman.dao.DAO6 {
  public ServiceUsageDAO6();
  public ServiceUsageDAO6(java.sql.Connection con);
   garaman.entity.ServiceUsage6[] byOrder(int orderId) throws java.sql.SQLException;
  public garaman.entity.ServiceUsage6[] getAllServiceUsageOfService(java.lang.String serviceCode, java.util.Date startDate, java.util.Date endDate);
  public int getTotalUsageCount(java.lang.String serviceCode);
}
```

### garaman.dao.SlotDAO6

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

### garaman.dao.SparePartDAO6

```java
public class SparePartDAO6 extends garaman.dao.DAO6 {
  private static final java.lang.String SELECT;
  public SparePartDAO6();
  public SparePartDAO6(java.sql.Connection con);
   garaman.entity.SparePart6 findById(int id) throws java.sql.SQLException;
  public garaman.entity.SparePart6 getPartInfo(java.lang.String partCode);
}
```

### garaman.dao.UsageMapper6

```java
final class UsageMapper6 extends java.lang.Object {
  private UsageMapper6();
  static garaman.entity.PartUsage6 part(java.sql.Connection con, java.sql.ResultSet rs) throws java.sql.SQLException;
  static garaman.entity.ServiceUsage6 service(java.sql.Connection con, java.sql.ResultSet rs) throws java.sql.SQLException;
}
```

### garaman.dao.VehicleBrandDAO6

```java
public class VehicleBrandDAO6 extends garaman.dao.DAO6 {
  public VehicleBrandDAO6();
  public VehicleBrandDAO6(java.sql.Connection con);
  public garaman.entity.VehicleBrand6[] getVehicleBrands();
}
```

### garaman.dao.VehicleDAO6

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

### garaman.entity.Account6

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

### garaman.entity.Appointment6

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

### garaman.entity.AppointmentSlot6

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

### garaman.entity.Category6

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

### garaman.entity.Customer6

```java
public class Customer6 extends garaman.entity.Member6 {
  private java.lang.String customerCode;
  public Customer6();
  public java.lang.String getCustomerCode();
  public void setCustomerCode(java.lang.String customerCode);
}
```

### garaman.entity.Employee6

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

### garaman.entity.Invoice6

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

### garaman.entity.Manager6

```java
public class Manager6 extends garaman.entity.Employee6 {
  public Manager6();
}
```

### garaman.entity.Member6

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

### garaman.entity.PartBrand6

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

### garaman.entity.PartUsage6

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

### garaman.entity.Payment6

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

### garaman.entity.RepairOrder6

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

### garaman.entity.RevenueStats6

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

### garaman.entity.SaleStaff6

```java
public class SaleStaff6 extends garaman.entity.Employee6 {
  public SaleStaff6();
}
```

### garaman.entity.Service6

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

### garaman.entity.ServiceUsage6

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

### garaman.entity.SparePart6

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

### garaman.entity.Technician6

```java
public class Technician6 extends garaman.entity.Employee6 {
  public Technician6();
}
```

### garaman.entity.Vehicle6

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

### garaman.entity.VehicleBrand6

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

### garaman.exception.BookingConflictException6

```java
public class BookingConflictException6 extends java.lang.RuntimeException {
  public BookingConflictException6(java.lang.String message);
}
```

### garaman.exception.DataAccessException6

```java
public class DataAccessException6 extends java.lang.RuntimeException {
  public DataAccessException6(java.lang.String message, java.lang.Throwable cause);
}
```

### garaman.filter.AppFilter6

```java
public class AppFilter6 extends java.lang.Object implements jakarta.servlet.Filter {
  private static final java.util.logging.Logger LOG;
  public AppFilter6();
  public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException;
  public void init(jakarta.servlet.FilterConfig config);
}
```

### garaman.filter.AuthFilter6

```java
public class AuthFilter6 extends java.lang.Object implements jakarta.servlet.Filter {
  public AuthFilter6();
  public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

### garaman.servlet.AppointmentServlet6

```java
public class AppointmentServlet6 extends jakarta.servlet.http.HttpServlet {
  public AppointmentServlet6();
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

### garaman.servlet.BookingServlet6

```java
public class BookingServlet6 extends jakarta.servlet.http.HttpServlet {
  public BookingServlet6();
  static void confirm(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, garaman.util.BookingContext6 c) throws jakarta.servlet.ServletException, java.io.IOException;
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private void form(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, garaman.util.BookingContext6 c) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

### garaman.servlet.InvoiceServlet6

```java
public class InvoiceServlet6 extends jakarta.servlet.http.HttpServlet {
  public InvoiceServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

### garaman.servlet.LoginServlet6

```java
public class LoginServlet6 extends jakarta.servlet.http.HttpServlet {
  public LoginServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private void home(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws java.io.IOException;
  private void invalid(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

### garaman.servlet.RevenueServlet6

```java
public class RevenueServlet6 extends jakarta.servlet.http.HttpServlet {
  public RevenueServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private void filter(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private void list(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

### garaman.servlet.ServicePartServlet6

```java
public class ServicePartServlet6 extends jakarta.servlet.http.HttpServlet {
  public ServicePartServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

### garaman.servlet.VehicleServlet6

```java
public class VehicleServlet6 extends jakarta.servlet.http.HttpServlet {
  public VehicleServlet6();
  public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, java.io.IOException;
  private void form(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp, garaman.util.BookingContext6 c) throws jakarta.servlet.ServletException, java.io.IOException;
}
```

### garaman.util.BookingContext6

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

### garaman.util.BookingSessionListener6

```java
public class BookingSessionListener6 extends java.lang.Object implements jakarta.servlet.http.HttpSessionListener {
  public BookingSessionListener6();
  public void sessionDestroyed(jakarta.servlet.http.HttpSessionEvent event);
}
```

### garaman.util.Config6

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

### garaman.util.DateUtil6

```java
public final class DateUtil6 extends java.lang.Object {
  private DateUtil6();
  public static java.util.Date date(java.time.LocalDate date);
  public static java.sql.Timestamp endExclusive(java.util.Date date);
  public static java.time.LocalDate localDate(java.util.Date date);
  public static java.sql.Timestamp start(java.util.Date date);
}
```

### garaman.util.PasswordUtil6

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

### garaman.util.SlotSetup6

```java
public class SlotSetup6 extends garaman.dao.DAO6 {
  public SlotSetup6();
  public static void main(java.lang.String[] args);
  public int prepareSlots();
}
```

### garaman.util.Web6

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


