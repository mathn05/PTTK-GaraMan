# GaraMan — Các quyết định triển khai đã chốt và phương án đề xuất

Bản cập nhật theo câu trả lời của người dùng ngày 12/11/2026. Đây là đầu vào nghiệp vụ mới, có quyền thay thế các mô tả cũ chưa đồng bộ trong ảnh thiết kế. Các mục “đề xuất chốt” là phương án được chọn để AI có thể triển khai tiếp; không cần hỏi lại người dùng những câu Q01–Q18 đã được giải quyết ở đây.

Phạm vi vẫn là hai module nghiệp vụ, bổ sung đăng nhập/đăng xuất tối thiểu để truy cập đúng vai trò. Không triển khai cả hệ thống.

## 1. Môi trường được chọn

| Mục | Quyết định |
|---|---|
| Hệ điều hành | Windows 10, theo người dùng xác nhận |
| IDE | IntelliJ IDEA; không phụ thuộc tính năng triển khai Tomcat trả phí, có đường chạy ngoài IDE |
| Java | JDK 21; compiler release 21 |
| Build | Maven WAR; cung cấp Maven Wrapper cho PowerShell/CMD, pin Maven 3.10.0 trong wrapper |
| Server | Apache Tomcat 10.1.x, dùng bản vá ổn định hiện hành khi cài và ghi chính xác bản đã kiểm thử |
| Namespace | `jakarta.servlet.*`, JSP/Servlet/JSTL tương thích Tomcat 10.1; không dùng thư viện Servlet `javax.servlet.*` trong cùng project |
| Lưu ý namespace | Không thay bừa mọi `javax.*`: JDBC `javax.sql` và crypto `javax.crypto` vẫn là các package Java hợp lệ |
| JDBC | MySQL Connector/J phiên bản tương thích MySQL Server thực tế; pin trong pom |
| MySQL | Kiểm tra bản server đang chạy; SQL baseline yêu cầu MySQL 8.0.16+; nếu chưa có server, kiểm tra bản MySQL 8.4 LTS và khả năng cài trên máy đích rồi mới chọn bộ cài phù hợp |
| Project / artifact | `GaraMan6` / `garaman6`; WAR có tên `garaman6.war` |
| Root package | `garaman`; các tầng `garaman.entity`, `garaman.dao`, `garaman.servlet`; thêm `garaman.filter`/`garaman.util`/`garaman.exception` khi cần cho hỗ trợ kỹ thuật |
| Database | `garaman6`, giữ tên trong SQL baseline |
| Host, port DB | Mặc định `127.0.0.1:3306`; kiểm tra port thật và cho phép cấu hình lại |
| Tài khoản DB | `garaman_app` chỉ dùng quyền cần thiết trên schema; thông tin kết nối từ cấu hình local hoặc environment, không dùng root trong app |
| HTTP | Mặc định `8080`, context `/garaman6`; nếu port bận thì chọn port trống và cập nhật README, không tắt tiến trình lạ |
| Múi giờ | `Asia/Ho_Chi_Minh` cho nghiệp vụ; JDBC/DB/JVM phải diễn giải DATETIME thống nhất |
| Giao diện | JSP/EL/JSTL + CSS/JS, tiếng Việt, giữ style đã thiết kế |

Không được tự báo “đã kiểm tra MySQL trên Windows 10” nếu AI chỉ đang chạy trong container Linux/cloud. AI phải kiểm tra đúng máy đích khi có terminal hoặc hướng dẫn chạy lệnh và lấy kết quả. `mysql --version` là version client; bản server cần truy vấn `SELECT VERSION(), @@port, @@session.time_zone;`.

Lệnh khám phá môi trường PowerShell, không chứa mật khẩu:

```powershell
java -version
Get-Command java, mysql, mvn -ErrorAction SilentlyContinue
Get-Service *mysql* -ErrorAction SilentlyContinue
```

Khi đã có project/wrapper và quyền kết nối:

```powershell
.\mvnw.cmd -version
mysql -h 127.0.0.1 -P 3306 -u <tai_khoan_co_san> -p -e "SELECT VERSION(), @@port, @@session.time_zone;"
```

Mật khẩu DB không thể suy ra từ tài liệu: dùng giá trị local do chủ máy thiết lập. Không yêu cầu người dùng gửi mật khẩu vào hội thoại công khai. Nếu cần quyền quản trị để tạo schema/user, hướng dẫn riêng thao tác setup, không hardcode quyền đó vào ứng dụng.

Cấu hình thống nhất tên `GARAMAN_DB_URL`, `GARAMAN_DB_USER`, `GARAMAN_DB_PASSWORD`, `GARAMAN_TIME_ZONE`. URL mặc định trỏ 127.0.0.1:3306/garaman6; password để local, không cung cấp một mật khẩu mặc định chung. Session JDBC đặt múi giờ +07:00 phù hợp quy tắc nghiệp vụ; kiểm tra `@@session.time_zone` trên chính connection của ứng dụng. Không chỉ đổi format hiển thị trong khi NOW()/expiry vẫn dùng múi giờ khác.

## 2. Baseline tài liệu và SQL

- `test_tk(7).jpg`: nguồn thực thể toàn hệ thống mới nhất đã tìm được.
- `tk_md1(2).jpg`: thiết kế chi tiết module 1 mới nhất đã tìm được.
- `tk_md2(1).jpg`: thiết kế chi tiết module 2 mới nhất đã tìm được.
- `GaraMan_CSDL(1).sql`: chọn làm **SQL baseline V1**, không coi bản export drawDB là schema chạy chính.
- Các quyết định trong file này là thay đổi yêu cầu đã được nêu sau các ảnh; AI cập nhật ảnh nguồn/PlantUML/scenario và mapping khi code.

Baseline SQL đã đọc thực tế có:

- `tblMember6` chứa cả thông tin Member và các cột account; không có `tblAccount6` riêng.
- `tblCustomer6.memberId` và `tblEmployee6.memberId` là PK đồng thời FK về Member.
- Không có bảng role rỗng cho Manager/SaleStaff/Technician; role nằm ở `tblEmployee6.jobTitle`.
- `tblAppointment6.saleStaffId` cho NULL.
- `activeSlotId` generated + unique ngăn trùng đặt lịch đang hoạt động; không thay bằng UNIQUE vô điều kiện trên appointmentSlotId.
- Usage đã có `repairOrderId`; Invoice có `repairOrderId` và `paymentId`.
- `tblInvoice6` không lưu totalAmount; `vwInvoice6` tính tổng từ giá lịch sử. `vwSparePart6` tính tồn kho.

Khi triển khai, giữ nguyên SQL V1 để truy vết, thêm migration V2 cho các thay đổi cần thiết. Sinh một schema tổng hợp cuối cho cài mới; không chạy lại nguyên V1 CREATE TABLE lên DB đã tồn tại và không tự DROP dữ liệu.

## 3. Q01 — Đăng nhập thật tối thiểu

Đã chốt: dùng đăng nhập, không dùng đường tắt chọn role/ID để thay xác thực.

- Thêm `LoginView6.jsp`, `LoginServlet6` và `MemberDAO6` cho đăng nhập/đăng xuất. `AuthFilter6` kiểm tra session và role cho các route được bảo vệ.
- `MemberDAO6()` kế thừa DAO6. Utility `PasswordUtil6.hash(password : String) : String` tạo chuỗi hash để seed, `PasswordUtil6.verify(password : String, encodedHash : String) : boolean` kiểm tra mật khẩu; không nhầm với mã hóa có thể giải mã.
- `MemberDAO6.authenticate(username : String, password : String) : Member6` trả đối tượng đã xác thực, có subtype/phân vai được ánh xạ đúng từ bảng; sai thông tin/trạng thái không hoạt động trả NULL; lỗi DB ném lỗi truy cập dữ liệu.
- `LoginServlet6.doGet(req, resp) : void`: mở form. `doPost(req, resp) : void`: xử lý login hoặc logout theo action; logout qua POST.
- `LoginView6.jsp`: `username : text`, `password : password`, `message : String`, `btnLogin : submit`. Dùng style GaraMan hiện có.
- `AuthFilter6.doFilter(request : ServletRequest, response : ServletResponse, chain : FilterChain) : void`; lọc các route Servlet, không chặn vòng lặp trang login/assets.
- Session dùng `currentUser : Member6`, `currentRole : String`; role hỗ trợ là CUSTOMER và MANAGER. Với Manager cần `jobTitle='MANAGER'`, không chỉ tin giá trị role gửi từ form.
- Account chỉ đăng nhập khi trạng thái ACTIVE. Đổi session ID sau đăng nhập; logout xóa ngữ cảnh của phiên và giải phóng hold còn thuộc phiên nếu có.
- CUSTOMER vào home đặt lịch; MANAGER vào home quản lý. Người đã đăng nhập sai role nhận 403; chưa đăng nhập thì về login. Các vai trò khác có thể xác thực nếu mapper hỗ trợ nhưng không được truy cập hai module này.
- Seed hai tài khoản tách biệt cho CUSTOMER/MANAGER. Nếu gặp tài khoản có nhiều vai trò thật, chỉ cấp quyền suy ra được từ DB, không tự mặc định là quản lý.
- Hash password bằng PBKDF2-HMAC-SHA256, salt ngẫu nhiên riêng từng mật khẩu, 600.000 vòng, hash 256 bit; lưu chuỗi có version/parameters/salt/hash trong cột password VARCHAR(255). Không lưu mật khẩu rõ hoặc chỉ SHA-256 nhanh. Cung cấp utility tạo hash cho seed, không làm đăng ký/quên mật khẩu/quản lý tài khoản.
- Cấp account demo trong README cho local; ví dụ username `customer6`, `manager6`, password demo `GaraMan6@Demo`, DB chỉ lưu hash. Không ghi đè mật khẩu tài khoản thật đang có.

Các lớp hỗ trợ này là bổ sung thiết kế có lý do từ Q01, không phải tự mở rộng thành hệ thống tài khoản đầy đủ.

## 4. Q02 — Khách không sửa thông tin cá nhân khi đặt lịch

Đã chốt: họ tên, điện thoại, email lấy từ hồ sơ khách đã đăng nhập, chỉ đọc.

- Giữ tên control `fullName`, `phone`, `email` nếu đã có nhưng biểu diễn readonly/output, không cho sửa.
- Server bỏ qua mọi giá trị liên hệ/customerId giả mạo từ request; tải thông tin khách bằng session và CSDL.
- Khách chỉ chọn ngày/slot/xe và nhập nội dung yêu cầu. Không cập nhật hồ sơ cá nhân trong module này.
- `Appointment6.cus` trỏ đúng khách; không cần thêm snapshot contact hay các field liên hệ mới vào Appointment.
- Sửa scenario, UI và thiết kế tương ứng: thay “hoàn thiện họ tên/điện thoại/email” bằng “kiểm tra thông tin cá nhân được hệ thống hiển thị”.

## 5. Q03–Q05 — Slot, lịch hoạt động và quay lại chỉnh sửa

Đã chốt: một slot chỉ nhận một cuộc hẹn đang hoạt động; hold 3 phút; edit/back thì giải phóng rồi giữ lại.

Vì Q04 mới xác nhận sức chứa chứ chưa chỉ ra lịch hoạt động, chọn mặc định demo cấu hình được:

- Mở cửa hằng ngày 08:00–17:00, nghỉ 12:00–13:00.
- Mỗi slot bắt đầu cách nhau 60 phút: 08, 09, 10, 11, 13, 14, 15, 16 giờ; không có slot 12 hoặc 17 giờ.
- Chuẩn bị 30 ngày tính từ ngày hiện tại của múi giờ nghiệp vụ; chỉ hiển thị slot có giờ bắt đầu ở tương lai.
- Script/init chuẩn bị slot theo ngày thực tế, idempotent. Không hardcode tháng 10/2026 chỉ vì UI ví dụ dùng ngày đó. Không tạo slot giả ở trình duyệt.
- Thêm unique `tblAppointmentSlot6.startTime` do đây là một gara, một slot/giờ và sức chứa 1. Kiểm tra dữ liệu trùng trước migration, không tự xóa slot có lịch để tạo index.
- Chuẩn bị slot bằng script/setup trước chạy demo hoặc tác vụ khởi tạo có kiểm soát; truy vấn GET danh sách không tự phát sinh hàng loạt nghiệp vụ ghi dữ liệu.
- Edit/back dùng POST action của BookingServlet6 để release đúng slot/holdCode; giữ dữ liệu ngày/xe/yêu cầu trong draft nhưng xóa token/expiry cũ.
- Khi người dùng gửi lại Xác nhận từ form Book, hold lại với token mới và thời hạn mới. Token cũ không được dùng để xác nhận sau khi đã release.
- Không gia hạn hold chỉ vì refresh trang Confirm; đóng tab/không bấm nút thì slot tự hết hiệu lực theo expiry trên server.
- Mỗi ngữ cảnh đặt lịch có token riêng; tránh một tab giải phóng nhầm hold của tab/người khác. Điều kiện release phải kiểm tra cặp slotId/holdCode và ngữ cảnh phiên.

Các mặc định giờ/ngày/30 ngày là quyết định triển khai mới được đề xuất; ghi trong BUSINESS_RULES và cấu hình để có thể đổi sau mà không sửa DAO signature.

## 6. Q06 — Nhân viên và trạng thái

- Lịch mới: `Appointment6.status='PENDING'`, UI hiển thị “Chờ tiếp nhận”.
- `Appointment6.ss = null`, `tblAppointment6.saleStaffId = NULL` cho đến khi có tiếp nhận ở module khác. SQL baseline đã hỗ trợ, không cần gán nhân viên giả.
- Slot theo baseline: AVAILABLE → HELD → BOOKED; release/expiry hợp lệ đưa về khả dụng; CLOSED không được chọn.
- Khi BOOKED/release khỏi HELD, xóa holdCode/holdExpiresAt theo CHECK hiện có. Không consume xong để lại token gây lỗi CHECK.
- Giữ UNIQUE activeSlotId để chống trùng lịch hoạt động; khóa/kiểm tra/consume/INSERT phải cùng transaction. Không giữ transaction mở 3 phút.

## 7. Q07 — Mã nghiệp vụ tăng dần

Chọn mã mới theo tiền tố + tối thiểu 4 chữ số: X0001, LH0001, KH0001, NV0001, DV0001, PT0001, PSC0001, HD0001. Khi vượt 9999 thì tăng thành 10000, không cắt hoặc quay vòng. Mã đã tồn tại trong dữ liệu cũ không bị đổi hàng loạt chỉ để cùng format.

Đối với hai module, sinh mã mới chủ yếu là xe và lịch hẹn; những mã còn lại phục vụ seed/hiển thị dữ liệu đã có, không làm CRUD ngoài phạm vi.

- Xe hiện chưa có field mã nghiệp vụ riêng: đề xuất thêm `Vehicle6.vehicleCode : String`, cột `tblVehicle6.vehicleCode VARCHAR(30) NOT NULL UNIQUE`. Đây là thay đổi nghiệp vụ do Q07, phải sửa thiết kế và migration, không âm thầm thêm.
- Thêm bảng kỹ thuật `tblCodeSequence6(prefix VARCHAR(10) PRIMARY KEY, lastValue BIGINT NOT NULL)` để cấp mã an toàn.
- Thêm `CodeSequenceDAO6(Connection con)` và `nextCode(prefix : String) : String`. DAO cấp mã dùng connection/transaction của DAO lưu xe/lịch, khóa dòng sequence bằng SELECT ... FOR UPDATE rồi tăng và format.
- Thêm constructor hỗ trợ connection vào DAO6/DAO cần chia sẻ transaction nếu code cần; ghi trong contract và thiết kế. Không đóng connection mượn từ caller.
- Cấp mã và INSERT trong cùng transaction. Không dùng MAX(code)+1 trong đường ghi runtime, không dùng biến static hoặc đếm phía trình duyệt.
- Migration chỉ dùng dữ liệu hiện có để backfill/khởi tạo counter một lần; đặt giá trị counter đủ lớn so với mã cũ, giữ UNIQUE. Không thay đổi PK/FK từ id sang code.
- Không yêu cầu mã không có khoảng trống; yêu cầu là tăng dần, duy nhất, không tái sử dụng mã đã cấp cho bản ghi đã lưu.

## 8. Q08 — Quy tắc thêm xe được đề xuất

- Bắt buộc: hãng có thật trong DB, dòng xe không trống, biển số không trống. Khách sở hữu lấy từ session.
- Biển số unique toàn hệ thống như SQL baseline, không chỉ unique trong từng khách.
- Chuẩn hóa trước kiểm tra/lưu: trim, uppercase, bỏ khoảng trắng/dấu chấm/dấu gạch ngang dùng để trình bày; ví dụ `30A-123.45` → `30A12345`. Giữ chữ/số và độ dài tối đa theo cột VARCHAR(20); không giả định một regex pháp lý duy nhất cho mọi loại biển.
- Trùng biển số thì báo rõ dữ liệu đã tồn tại, không tự chuyển xe sang khách khác và không ghi đè xe cũ.
- Năm sản xuất không bắt buộc, nếu nhập thì từ 1886 đến năm hiện tại + 1. Do DB cho NULL, đổi `manufactureYear : int` thành `manufactureYear : Integer` trong entity/thiết kế để phản ánh đúng giá trị chưa biết.
- `currentMileage = 0` lúc thêm mới theo DEFAULT của DB; không tự thêm ô km khi UI cuối không có ô đó.
- Mã xe do DAO cấp theo mục Q07; người dùng không nhập ID hoặc mã xe.
- Save thành công redirect về BookingServlet6, tải lại xe và đưa xe mới vào danh sách, có thể chọn sẵn xe mới; giữ ngày/nội dung yêu cầu trước đó trong ngữ cảnh đặt lịch.

## 9. Q09–Q13 — Quy tắc thống kê đã được xác định

### Ngày và trạng thái

- Khoảng ngày lọc theo **`tblInvoice6.issueDate`**, không theo usageTime, completionTime hoặc paymentTime.
- Chỉ tính bản ghi có hóa đơn liên kết với `tblPayment6.status='PAID'`.
- Khoảng ngày bao gồm cả hai ngày: `issueDate >= đầu ngày bắt đầu AND issueDate < đầu ngày sau ngày kết thúc`, theo múi giờ nghiệp vụ.
- Dùng chính điều kiện này cho báo cáo và lịch sử sử dụng trong khoảng thống kê. Cột “Ngày hoàn thành” vẫn hiển thị RepairOrder.completionTime, không vì tên cột mà đổi căn cứ lọc.
- Lifetime count đếm tất cả dòng Usage của mã đó trong CSDL, không lọc ngày và không bắt buộc đã có hóa đơn/đã thanh toán; ghi nhãn rõ “Số lần sử dụng toàn thời gian”. Đây là thống kê mức sử dụng, không phải doanh thu.

### Số lần và tiền

- `usageCount` là COUNT các dòng/sự kiện Usage thỏa điều kiện báo cáo. Một dòng PartUsage có usedQuantity=4 vẫn là một lần sử dụng, doanh thu nhân 4.
- Dịch vụ: SUM(ServiceUsage.saleUnitPrice).
- Phụ tùng: SUM(PartUsage.usedQuantity * PartUsage.saleUnitPrice).
- Không thuế, giảm giá hoặc hoàn tiền trong phạm vi. Không lấy unitPrice hiện tại của danh mục tính doanh thu lịch sử.
- Tiền SQL/đối chiếu dùng DECIMAL/BigDecimal; nếu entity đã chốt double thì giữ public type và chuyển đổi có kiểm soát ở biên. Không đổi public type không ghi thay đổi thiết kế.
- `Invoice6.totalAmount` lấy từ `vwInvoice6` hoặc truy vấn tương đương; `SparePart6.stockQuantity` lấy từ `vwSparePart6`. Không tự thêm lại cột dẫn xuất đã bỏ khỏi bảng.
- Seed đã thanh toán phải có paymentAmount khớp tổng hóa đơn do không có giảm giá/thuế/thanh toán nhiều lần trong phạm vi. Nếu dữ liệu đang có lệch, báo lỗi đối chiếu dữ liệu; không tự sửa tiền hoặc loại bỏ dòng doanh thu âm thầm.
- Tránh nhân bản tiền khi một phiếu có nhiều service và part; tổng hợp từng nguồn đúng cấp rồi kết hợp.

### Phân trang và sắp xếp

- 20 dòng/trang cho danh sách thống kê và lịch sử sử dụng.
- Báo cáo: totalRevenue DESC, itemCode ASC, itemType ASC để có thứ tự ổn định khi cùng mã giữa hai loại.
- Lịch sử: issueDate DESC, usageId DESC làm mặc định ổn định.
- Tổng tiền báo cáo là tổng toàn bộ kết quả lọc, không phải tổng 20 dòng của trang đang xem.
- Khi không có dòng: currentPage=1, totalPages=0, tổng=0, ẩn/vô hiệu hóa chuyển trang. Trang vượt phạm vi được đưa về trang hợp lệ; không dùng số âm hoặc offset âm.

## 10. Q14 — Dữ liệu phiếu/khách/xe ở từng lần sử dụng

Chọn **bổ sung tham chiếu ngược trong Java**, giữ nguyên mảng và chữ ký DAO:

```text
ServiceUsage6
-order : RepairOrder6

PartUsage6
-order : RepairOrder6
```

- SQL đã có `repairOrderId` trong cả hai bảng Usage nên không cần thêm FK/cột DB cho việc này.
- DAO Usage JOIN sang RepairOrder, Member/Customer và Vehicle; dựng `order` đủ dữ liệu hiển thị.
- JSP lấy `usage.order.repairOrderCode`, `usage.order.cus.fullName`, `usage.order.vehicle.licensePlate`, `usage.order.completionTime`.
- Mapper chỉ dựng order ở mức nông khi nạp lịch sử, không đệ quy lại danh sách Usage của order rồi nạp order tiếp. Nếu một relation không được hydrate thì tài liệu hóa, không giả định toàn object graph đều được nạp.
- Hóa đơn đầy đủ có thể hydrate danh sách Usage và order theo hướng được kiểm soát, không serialize đệ quy vô hạn.
- Cập nhật hai thuộc tính `order` và tính điều hướng quan hệ trong thiết kế toàn hệ thống/module 2. Không thêm một bộ DTO và đổi return type mảng chỉ vì tiện code.

## 11. Q15 — Tìm hóa đơn từ dòng sử dụng

Giữ phương án đã đề xuất:

```text
InvoiceDAO6
+getInvoiceCodeByUsage(itemType : String, usageId : int) : String
```

SERVICE: tìm `tblServiceUsage6.id` → repairOrderId → tblInvoice6.repairOrderId. PART: đường tương tự qua tblPartUsage6. Loại chỉ nhận SERVICE/PART từ whitelist; không ghép tên bảng từ input chưa kiểm tra.

Link gửi itemType + usageId, không dùng itemCode để đoán một hóa đơn. Khi có mã, ServicePartServlet6 redirect đến InvoiceServlet6 rồi gọi `getInvoiceDetail(invoiceCode)` và hiển thị **toàn bộ hóa đơn**.

## 12. Q16 — Đánh giá dịch vụ

Bỏ hoàn toàn dòng/ô đánh giá khách hàng trong UI của hai module. Không tạo bảng/field/DAO rating và không hardcode giá trị mẫu. Đây là sửa UI theo yêu cầu mới.

## 13. Q17 — Ánh xạ kế thừa theo SQL thật

| Lớp Java | Nguồn DB |
|---|---|
| Member6 | tblMember6 |
| Account6 qua Member6.acc | Các cột username/password/profileImage/status trong tblMember6; không có bảng account riêng. Nếu cần Account6.id trong mapper, dùng định danh hàng Member sở hữu và ghi rõ đây là mapping account đã gộp |
| Customer6 extends Member6 | JOIN tblCustomer6.memberId = tblMember6.id |
| Employee6 extends Member6 | JOIN tblEmployee6.memberId = tblMember6.id |
| Manager6 extends Employee6 | Nhân viên có jobTitle=MANAGER |
| SaleStaff6 extends Employee6 | jobTitle=SALE_STAFF |
| Technician6 extends Employee6 | jobTitle=TECHNICIAN |
| RevenueStats6 | Kết quả SELECT tổng hợp, không có bảng và không có id riêng |

`Customer6.id` là id kế thừa từ Member. `VehicleDAO6.getVehiclesOfCustomer(customerId)` nhận ID này, khớp FK tblVehicle6.customerId → tblCustomer6.memberId. Không tạo “customer.id” thứ hai.

FK theo vai trò trong SQL trỏ về tblEmployee6; DAO cần đảm bảo jobTitle đúng khi thao tác ghi thuộc phạm vi. Không tạo lại các bảng role rỗng đã bỏ.

## 14. Q18 — Quy tắc trả kết quả và báo lỗi

| Tình huống | Hợp đồng |
|---|---|
| Query danh sách thành công, không có dòng | Mảng rỗng đúng kiểu, không trả NULL |
| Query một đối tượng không tìm thấy | NULL; Servlet xử lý 404/thông báo phù hợp |
| getInvoiceCodeByUsage không có hóa đơn | NULL; không lấy đại hóa đơn khác |
| authenticate sai thông tin/không ACTIVE | NULL và thông báo đăng nhập chung, không tiết lộ trường nào đúng |
| holdSlot gặp slot không còn khả dụng | NULL; form báo slot vừa được đặt/không còn khả dụng |
| validateHold không hợp lệ/hết hạn | false |
| releaseSlot/consumeHold không cập nhật được cặp token hợp lệ | false; không coi là SQL exception nếu truy vấn chạy bình thường |
| createAppointment gặp hold không còn hợp lệ | Ném BookingConflictException6, rollback; Servlet báo xung đột, không báo thành công |
| createAppointment/saveVehicle thành công | Trả entity thật có id/code đã lưu |
| Lỗi JDBC/SQL/connection | Ném DataAccessException6 có cause, log phía server; không biến thành mảng rỗng/false/NULL như tình huống nghiệp vụ |
| Dữ liệu form sai | HTTP 400 hoặc render lại form kèm lỗi theo HTTP contract; giữ input an toàn |
| Chưa đăng nhập | Redirect login, giữ URL quay lại trong whitelist nội bộ |
| Sai role/xe không thuộc khách | 403; không tin ID từ hidden input |
| Không tìm thấy đối tượng | 404 |
| Slot xung đột/hold hết hạn | 409 hoặc render thông báo nghiệp vụ có ghi rõ trong HTTP contract |
| Lỗi hệ thống | 500 + thông báo chung; không đưa SQL/password/stack trace ra UI |

DataAccessException6 và BookingConflictException6 là RuntimeException kỹ thuật/nghiệp vụ, thêm vào package exception và hợp đồng thiết kế. Constructor đề xuất: `DataAccessException6(message : String, cause : Throwable)` và `BookingConflictException6(message : String)`. Không đổi return type hiện có để nhét thông báo lỗi vào String hay List.

Double-submit: một lịch được lưu tối đa một lần. Dùng token ngữ cảnh ở server, lưu kết quả đã hoàn tất theo token, UNIQUE activeSlotId và transaction. Request lặp cùng ngữ cảnh trả lại kết quả đã ghi nhận khi xác định được; request tranh chấp khác nhận xung đột. Không retry mù khi không biết transaction trước đã commit hay chưa.

## 15. Những thay đổi thiết kế/migration cần ghi khi AI triển khai

1. Thêm đăng nhập tối thiểu: LoginView6.jsp, LoginServlet6, MemberDAO6.authenticate, AuthFilter6; hỗ trợ hash/session/quyền.
2. Book: thông tin cá nhân readonly, server đọc từ hồ sơ. Không thêm contact snapshot.
3. Edit/back release hold cũ; lần tiếp theo giữ lại bằng token mới.
4. Vehicle6.vehicleCode + cột unique; CodeSequenceDAO6 + tblCodeSequence6; constructor Connection hỗ trợ transaction nếu cần.
5. Vehicle6.manufactureYear đổi int → Integer do năm tùy chọn/DB NULL.
6. Hai Usage thêm order : RepairOrder6, không đổi mảng trả về và không thêm cột DB.
7. Bỏ rating khỏi UI.
8. Unique startTime của slot, cấu hình giờ làm và seed slot idempotent.
9. Quy tắc báo cáo issueDate/PAID/COUNT dòng/20 dòng, sort code và nguồn dữ liệu dẫn xuất.
10. Giữ mọi thay đổi kỹ thuật phát sinh trong DESIGN_CHANGES, đồng bộ code — SQL — thiết kế cuối — scenario V3/tests; không triển khai lan sang module khác.

Danh sách này chỉ định những thay đổi AI phải thực hiện khi code. Tài liệu này không tự sửa các ảnh đã đính kèm hoặc tuyên bố project đã được triển khai/kiểm thử.

## 16. Nguồn kỹ thuật đã kiểm tra

- Apache Tomcat 10.1 migration guide: https://tomcat.apache.org/migration-10.1.html — yêu cầu Java 11+, Servlet 6.0/JSP 3.1.
- Apache Tomcat downloads: https://tomcat.apache.org/download-10.cgi — namespace Jakarta cho Tomcat 10+.
- Apache Maven: https://maven.apache.org/download.cgi — bản 3.10.0 hiện được khuyến nghị ở thời điểm soạn.
- MySQL VERSION(): https://dev.mysql.com/doc/refman/8.4/en/information-functions.html — xác định bản server thực tế.
- OWASP Password Storage: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html — PBKDF2-HMAC-SHA256 600.000 vòng.

Các phiên bản vá/dependency phải được pin và ghi theo bản thật đã cài/test khi triển khai, không coi việc duyệt tài liệu là đã cài phần mềm trên máy người dùng.
