# Prompt giao cho AI: triển khai hai module GaraMan theo thiết kế

Bản V2 đã tích hợp câu trả lời Q01–Q18 và phương án triển khai cụ thể. Sao chép từ “BẮT ĐẦU PROMPT” đến “KẾT THÚC PROMPT”, gửi kèm thiết kế/SQL/UI bản mới nhất. Các quyết định mới trong mục 3 có quyền thay thế mô tả cũ mâu thuẫn trong ảnh; AI phải cập nhật thiết kế theo đó, không hỏi lại các lựa chọn đã chốt.

---

BẮT ĐẦU PROMPT

Bạn là lập trình viên Java web chịu trách nhiệm triển khai thực tế và đồng bộ tài liệu thiết kế cho bài GaraMan của tôi. Đọc toàn bộ đầu vào trước khi quyết định cấu trúc. Hãy viết project chạy được, không chỉ lập kế hoạch hoặc đưa pseudo-code.

## 1. Phạm vi chính xác

Triển khai hai module nghiệp vụ:

1. Khách hàng đặt lịch hẹn trực tuyến: xem thông tin cá nhân chỉ đọc, chọn xe/ngày/slot, giữ chỗ 3 phút, xác nhận cuối, lưu lịch và nhánh thêm xe trong thiết kế.
2. Quản lý thống kê dịch vụ/phụ tùng theo doanh thu: menu/bộ lọc/danh sách, màn hình chi tiết chung cho hai loại, số lần sử dụng, lịch sử và hóa đơn.

Bổ sung đăng nhập/đăng xuất thật tối thiểu và kiểm tra role để truy cập hai module vì người dùng chọn Q01=đăng nhập. Đây là chức năng hỗ trợ đã được cho phép; không dùng cơ chế chọn ID/role demo thay đăng nhập.

Không làm đăng ký, quên mật khẩu, quản trị tài khoản, CRUD danh mục, nhập kho, lập phiếu sửa chữa, thu tiền hoặc phát hành hóa đơn như một module mới. Các thực thể đó chỉ cần mapping/seed/truy vấn phục vụ hai module. Không chuyển sang Spring/JPA/SPA hoặc thêm tầng Service/Repository ngoài thiết kế.

## 2. Nguồn quyết định và nguyên tắc “code mốc”

Nguồn mới nhất đã xác định: test_tk(7).jpg, tk_md1(2).jpg, tk_md2(1).jpg và GaraMan_CSDL(1).sql. Đọc thêm scenario/UI/LAB được gửi. Chọn SQL này làm baseline V1, áp dụng migration V2 theo các quyết định tại mục 3; không chọn bản export drawDB làm runtime schema mặc định. Nếu có file mới hơn được gửi rõ ràng thì ghi vào manifest và đối chiếu.

Trước khi code, lập `SOURCE_MANIFEST.md`, chỉ rõ phiên bản cuối của từng nguồn. Phân vai nguồn:

- Lớp thực thể/thuộc tính/kế thừa: biểu đồ thực thể pha thiết kế toàn hệ thống, đồng bộ với module.
- DAO/Servlet/JSP và hợp đồng tương tác: biểu đồ thiết kế chi tiết từng module.
- Tên bảng/cột, khóa, nullable, unique, kế thừa vật lý: DDL/schema cuối.
- Chức năng/thứ tự/tình huống lý tưởng: scenario và sơ đồ hoạt động đã chốt.
- Trách nhiệm triển khai: Servlet điều khiển; DAO truy xuất; entity mang dữ liệu; JSP hiển thị.
- Nội dung hiển thị: UI cuối và dữ liệu thật.
- LAB: tham khảo cách lập trình, không có quyền ghi đè nghiệp vụ GaraMan.

Nếu các nguồn xung đột, ghi rõ xung đột, không âm thầm chọn bản tiện code. Tìm câu trả lời trong đầu vào trước khi hỏi tôi; chỉ hỏi câu hỏi thật sự thiếu, gom theo ảnh hưởng nghiệp vụ, và vẫn làm các phần độc lập.

**Yêu cầu đặc biệt đã được tôi cho phép:** nếu thiết kế hiện tại không thể triển khai đúng/chạy được hoặc bắt buộc phải đổi để bảo đảm tính đúng, lấy code đã kiểm thử làm mốc rồi sửa ngược thiết kế. Hãy thực hiện thay đổi kỹ thuật tối thiểu cần thiết, không dừng để xin lại quyền cho từng sửa kỹ thuật.

Mỗi thay đổi phải có bản ghi trong `DESIGN_CHANGES.md`:

| ID | Nguồn/vị trí cũ | Vấn đề và bằng chứng | Chữ ký/thuộc tính/luồng trước | Sau | File code | Tài liệu/SQL đã cập nhật | Kiểm thử |
|---|---|---|---|---|---|---|---|

Phân biệt:

- Bổ sung boilerplate Java cần thiết như getter/setter, import, constructor JavaBean: liệt kê trong contract, không coi là tự ý đổi nghiệp vụ.
- Bổ sung public method, đổi kiểu, đổi field, thêm tham chiếu, thay transaction, đổi cách truyền dữ liệu/route: cập nhật thiết kế và ghi thay đổi.
- Nhiều cách hiểu nghiệp vụ: không được tự chọn rồi gọi đó là thay đổi kỹ thuật bắt buộc. Hỏi đúng phần quyết định còn thiếu.

Sau mỗi thay đổi, cập nhật đồng thời code, bảng hợp đồng, PlantUML/bản text thiết kế, mapping SQL, scenario V3/sequence liên quan và tests. Nếu ảnh cũ không sửa trực tiếp được, xuất thiết kế nguồn đã sửa và đánh dấu ảnh cũ không còn là baseline cuối. Kết thúc với thiết kế cuối khớp code, không được tuyên bố khớp nguyên bản nếu đã có thay đổi.

## 3. Quyết định môi trường và nghiệp vụ V2 — áp dụng trực tiếp

Các lựa chọn bên dưới đã được người dùng trả lời hoặc đã giao cho tôi đề xuất. Không để lại câu hỏi Q01–Q18 chưa giải quyết, không hỏi lại phiên bản/cách làm khi đã chọn sẵn. Những thứ phải kiểm tra thực tế là bản MySQL trên máy đích, quyền kết nối, đường cài đặt và dữ liệu đang có; không được giả vờ đã kiểm tra máy Windows nếu chỉ có cloud runtime.

### 1. Môi trường được chọn

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

### 2. Baseline tài liệu và SQL

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

### 3. Q01 — Đăng nhập thật tối thiểu

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

### 4. Q02 — Khách không sửa thông tin cá nhân khi đặt lịch

Đã chốt: họ tên, điện thoại, email lấy từ hồ sơ khách đã đăng nhập, chỉ đọc.

- Giữ tên control `fullName`, `phone`, `email` nếu đã có nhưng biểu diễn readonly/output, không cho sửa.
- Server bỏ qua mọi giá trị liên hệ/customerId giả mạo từ request; tải thông tin khách bằng session và CSDL.
- Khách chỉ chọn ngày/slot/xe và nhập nội dung yêu cầu. Không cập nhật hồ sơ cá nhân trong module này.
- `Appointment6.cus` trỏ đúng khách; không cần thêm snapshot contact hay các field liên hệ mới vào Appointment.
- Sửa scenario, UI và thiết kế tương ứng: thay “hoàn thiện họ tên/điện thoại/email” bằng “kiểm tra thông tin cá nhân được hệ thống hiển thị”.

### 5. Q03–Q05 — Slot, lịch hoạt động và quay lại chỉnh sửa

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

### 6. Q06 — Nhân viên và trạng thái

- Lịch mới: `Appointment6.status='PENDING'`, UI hiển thị “Chờ tiếp nhận”.
- `Appointment6.ss = null`, `tblAppointment6.saleStaffId = NULL` cho đến khi có tiếp nhận ở module khác. SQL baseline đã hỗ trợ, không cần gán nhân viên giả.
- Slot theo baseline: AVAILABLE → HELD → BOOKED; release/expiry hợp lệ đưa về khả dụng; CLOSED không được chọn.
- Khi BOOKED/release khỏi HELD, xóa holdCode/holdExpiresAt theo CHECK hiện có. Không consume xong để lại token gây lỗi CHECK.
- Giữ UNIQUE activeSlotId để chống trùng lịch hoạt động; khóa/kiểm tra/consume/INSERT phải cùng transaction. Không giữ transaction mở 3 phút.

### 7. Q07 — Mã nghiệp vụ tăng dần

Chọn mã mới theo tiền tố + tối thiểu 4 chữ số: X0001, LH0001, KH0001, NV0001, DV0001, PT0001, PSC0001, HD0001. Khi vượt 9999 thì tăng thành 10000, không cắt hoặc quay vòng. Mã đã tồn tại trong dữ liệu cũ không bị đổi hàng loạt chỉ để cùng format.

Đối với hai module, sinh mã mới chủ yếu là xe và lịch hẹn; những mã còn lại phục vụ seed/hiển thị dữ liệu đã có, không làm CRUD ngoài phạm vi.

- Xe hiện chưa có field mã nghiệp vụ riêng: đề xuất thêm `Vehicle6.vehicleCode : String`, cột `tblVehicle6.vehicleCode VARCHAR(30) NOT NULL UNIQUE`. Đây là thay đổi nghiệp vụ do Q07, phải sửa thiết kế và migration, không âm thầm thêm.
- Thêm bảng kỹ thuật `tblCodeSequence6(prefix VARCHAR(10) PRIMARY KEY, lastValue BIGINT NOT NULL)` để cấp mã an toàn.
- Thêm `CodeSequenceDAO6(Connection con)` và `nextCode(prefix : String) : String`. DAO cấp mã dùng connection/transaction của DAO lưu xe/lịch, khóa dòng sequence bằng SELECT ... FOR UPDATE rồi tăng và format.
- Thêm constructor hỗ trợ connection vào DAO6/DAO cần chia sẻ transaction nếu code cần; ghi trong contract và thiết kế. Không đóng connection mượn từ caller.
- Cấp mã và INSERT trong cùng transaction. Không dùng MAX(code)+1 trong đường ghi runtime, không dùng biến static hoặc đếm phía trình duyệt.
- Migration chỉ dùng dữ liệu hiện có để backfill/khởi tạo counter một lần; đặt giá trị counter đủ lớn so với mã cũ, giữ UNIQUE. Không thay đổi PK/FK từ id sang code.
- Không yêu cầu mã không có khoảng trống; yêu cầu là tăng dần, duy nhất, không tái sử dụng mã đã cấp cho bản ghi đã lưu.

### 8. Q08 — Quy tắc thêm xe được đề xuất

- Bắt buộc: hãng có thật trong DB, dòng xe không trống, biển số không trống. Khách sở hữu lấy từ session.
- Biển số unique toàn hệ thống như SQL baseline, không chỉ unique trong từng khách.
- Chuẩn hóa trước kiểm tra/lưu: trim, uppercase, bỏ khoảng trắng/dấu chấm/dấu gạch ngang dùng để trình bày; ví dụ `30A-123.45` → `30A12345`. Giữ chữ/số và độ dài tối đa theo cột VARCHAR(20); không giả định một regex pháp lý duy nhất cho mọi loại biển.
- Trùng biển số thì báo rõ dữ liệu đã tồn tại, không tự chuyển xe sang khách khác và không ghi đè xe cũ.
- Năm sản xuất không bắt buộc, nếu nhập thì từ 1886 đến năm hiện tại + 1. Do DB cho NULL, đổi `manufactureYear : int` thành `manufactureYear : Integer` trong entity/thiết kế để phản ánh đúng giá trị chưa biết.
- `currentMileage = 0` lúc thêm mới theo DEFAULT của DB; không tự thêm ô km khi UI cuối không có ô đó.
- Mã xe do DAO cấp theo mục Q07; người dùng không nhập ID hoặc mã xe.
- Save thành công redirect về BookingServlet6, tải lại xe và đưa xe mới vào danh sách, có thể chọn sẵn xe mới; giữ ngày/nội dung yêu cầu trước đó trong ngữ cảnh đặt lịch.

### 9. Q09–Q13 — Quy tắc thống kê đã được xác định

#### Ngày và trạng thái

- Khoảng ngày lọc theo **`tblInvoice6.issueDate`**, không theo usageTime, completionTime hoặc paymentTime.
- Chỉ tính bản ghi có hóa đơn liên kết với `tblPayment6.status='PAID'`.
- Khoảng ngày bao gồm cả hai ngày: `issueDate >= đầu ngày bắt đầu AND issueDate < đầu ngày sau ngày kết thúc`, theo múi giờ nghiệp vụ.
- Dùng chính điều kiện này cho báo cáo và lịch sử sử dụng trong khoảng thống kê. Cột “Ngày hoàn thành” vẫn hiển thị RepairOrder.completionTime, không vì tên cột mà đổi căn cứ lọc.
- Lifetime count đếm tất cả dòng Usage của mã đó trong CSDL, không lọc ngày và không bắt buộc đã có hóa đơn/đã thanh toán; ghi nhãn rõ “Số lần sử dụng toàn thời gian”. Đây là thống kê mức sử dụng, không phải doanh thu.

#### Số lần và tiền

- `usageCount` là COUNT các dòng/sự kiện Usage thỏa điều kiện báo cáo. Một dòng PartUsage có usedQuantity=4 vẫn là một lần sử dụng, doanh thu nhân 4.
- Dịch vụ: SUM(ServiceUsage.saleUnitPrice).
- Phụ tùng: SUM(PartUsage.usedQuantity * PartUsage.saleUnitPrice).
- Không thuế, giảm giá hoặc hoàn tiền trong phạm vi. Không lấy unitPrice hiện tại của danh mục tính doanh thu lịch sử.
- Tiền SQL/đối chiếu dùng DECIMAL/BigDecimal; nếu entity đã chốt double thì giữ public type và chuyển đổi có kiểm soát ở biên. Không đổi public type không ghi thay đổi thiết kế.
- `Invoice6.totalAmount` lấy từ `vwInvoice6` hoặc truy vấn tương đương; `SparePart6.stockQuantity` lấy từ `vwSparePart6`. Không tự thêm lại cột dẫn xuất đã bỏ khỏi bảng.
- Seed đã thanh toán phải có paymentAmount khớp tổng hóa đơn do không có giảm giá/thuế/thanh toán nhiều lần trong phạm vi. Nếu dữ liệu đang có lệch, báo lỗi đối chiếu dữ liệu; không tự sửa tiền hoặc loại bỏ dòng doanh thu âm thầm.
- Tránh nhân bản tiền khi một phiếu có nhiều service và part; tổng hợp từng nguồn đúng cấp rồi kết hợp.

#### Phân trang và sắp xếp

- 20 dòng/trang cho danh sách thống kê và lịch sử sử dụng.
- Báo cáo: totalRevenue DESC, itemCode ASC, itemType ASC để có thứ tự ổn định khi cùng mã giữa hai loại.
- Lịch sử: issueDate DESC, usageId DESC làm mặc định ổn định.
- Tổng tiền báo cáo là tổng toàn bộ kết quả lọc, không phải tổng 20 dòng của trang đang xem.
- Khi không có dòng: currentPage=1, totalPages=0, tổng=0, ẩn/vô hiệu hóa chuyển trang. Trang vượt phạm vi được đưa về trang hợp lệ; không dùng số âm hoặc offset âm.

### 10. Q14 — Dữ liệu phiếu/khách/xe ở từng lần sử dụng

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

### 11. Q15 — Tìm hóa đơn từ dòng sử dụng

Giữ phương án đã đề xuất:

```text
InvoiceDAO6
+getInvoiceCodeByUsage(itemType : String, usageId : int) : String
```

SERVICE: tìm `tblServiceUsage6.id` → repairOrderId → tblInvoice6.repairOrderId. PART: đường tương tự qua tblPartUsage6. Loại chỉ nhận SERVICE/PART từ whitelist; không ghép tên bảng từ input chưa kiểm tra.

Link gửi itemType + usageId, không dùng itemCode để đoán một hóa đơn. Khi có mã, ServicePartServlet6 redirect đến InvoiceServlet6 rồi gọi `getInvoiceDetail(invoiceCode)` và hiển thị **toàn bộ hóa đơn**.

### 12. Q16 — Đánh giá dịch vụ

Bỏ hoàn toàn dòng/ô đánh giá khách hàng trong UI của hai module. Không tạo bảng/field/DAO rating và không hardcode giá trị mẫu. Đây là sửa UI theo yêu cầu mới.

### 13. Q17 — Ánh xạ kế thừa theo SQL thật

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

### 14. Q18 — Quy tắc trả kết quả và báo lỗi

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

### 15. Những thay đổi thiết kế/migration cần ghi khi AI triển khai

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

### Cấu trúc package/thư mục bổ sung

- Entity: src/main/java/garaman/entity/.
- DAO: src/main/java/garaman/dao/; gồm cả MemberDAO6 và CodeSequenceDAO6 hỗ trợ mới.
- Servlet: src/main/java/garaman/servlet/; gồm LoginServlet6.
- Filter/exception/util kỹ thuật trong package cùng tên dưới garaman; đây không phải tầng nghiệp vụ mới.
- JSP: src/main/webapp/WEB-INF/views/; chia customer/manager/auth nếu cần, giữ nguyên basename.
- CSS/JS/ảnh: src/main/webapp/assets/; dependency dùng được khi chạy demo, không phụ thuộc CDN không được kiểm chứng.
- Cấu hình mẫu: src/main/resources/; migration/seed: sql/; tests và docs trong cấu trúc Maven.
- Dùng chung một bản entity/DAO6 giữa hai module. Maven Wrapper và script PowerShell phải chạy được không cần plugin triển khai Tomcat đặc biệt của IDE.

## 4. Khóa hợp đồng trước khi triển khai

Tạo các tài liệu sau từ nguồn thực tế:

1. `docs/CONTRACTS.md` hoặc bản máy đọc tương đương: mọi class, superclass, visibility; mọi thuộc tính và kiểu đầy đủ; constructor; tên method; tên/thứ tự/kiểu tham số; kiểu trả về; throws; hành vi empty/not-found/failure.
2. `docs/SCHEMA_MAPPING.md`: entity.field ↔ bảng.cột/quan hệ join; PK/FK; unique; nullable; mapping kế thừa; trạng thái; tiền; múi giờ.
3. `docs/HTTP_VIEW_CONTRACT.md`: HTTP method, URL, action, tên input, cách parse, request attributes, session keys, forward/redirect, JSP đích, field nào NULL theo từng nhánh.
4. `docs/BUSINESS_RULES.md`: chép đầy đủ các quyết định V2 tại mục 3 và các kết quả kiểm tra runtime thực tế; không tự mở lại các câu Q01–Q18.
5. `docs/TRACEABILITY.md`: scenario step/UI field hoặc column → Servlet → DAO → SQL/source field → test.

Không chỉ tạo các file tên đẹp rồi để nội dung trống. Dùng chúng để kiểm tra code thực tế.

Quy tắc giữ nguyên:

- Hậu tố `6`, kể cả Servlet, constructor, array element type.
- Không sửa tên như `createAt` thành `createdAt`, `cus` thành `customer`, `ss` thành `salesStaff` vì sở thích.
- Không đổi `T[]` thành `List<T>` ở public API nếu nguồn đã chốt kiểu mảng. Có thể dùng List cục bộ rồi chuyển sang mảng trước khi return.
- Không đổi tham số nhận code String thành ID int hoặc ngược lại nếu chưa ghi thay đổi.
- Phân biệt `java.util.Date`, `java.sql.Date`, `LocalDate`, timestamp; đừng dùng `Date` mơ hồ. Các bộ chuyển đổi JDBC được thực hiện ở biên, không làm mất phần giờ của slot/expiry.
- Không coi `Select`, `Table`, `text`, `link`, `submit` là lớp Java bắt buộc tạo; đó là mô tả control của JSP.
- Không tạo association JSP–entity chỉ để trang hiển thị; thuộc tính/request data đã thể hiện dữ liệu dùng. Không thêm các đường đó với lý do “mẫu có” khi mẫu không có.
- Bỏ `mng` ở các UI không cần thông tin quản lý theo quyết định cuối; quyền truy cập vẫn lấy từ session. Nếu ảnh cũ còn `mng`, ghi nhận thay đổi này, không tự thêm lại vào mọi trang.
- Không thêm `id` hoặc bảng lưu thống kê cho `RevenueStats6`.

Nếu chữ trên ảnh mờ hoặc bị cắt, ghi vị trí và yêu cầu nguồn rõ/text; không tự đoán để tuyên bố đúng 100%.

## 5. Các lớp trong phạm vi

### Module 1

JSP: `CustomerHomeView6.jsp`, `BookAppointmentView6.jsp`, `ConfirmView6.jsp`, `AddNewVehicleView6.jsp`.

Servlet: `BookingServlet6`, `AppointmentServlet6`, `VehicleServlet6`.

DAO: `CustomerDAO6`, `VehicleDAO6`, `VehicleBrandDAO6`, `SlotDAO6`, `AppointmentDAO6`, dùng chung `DAO6`.

Các entity: `Customer6`, `Member6`, `Account6` được ánh xạ như mục 3; `Vehicle6`, `VehicleBrand6`, `Appointment6`, `AppointmentSlot6`; các lớp cha/kiểu tham chiếu như `SaleStaff6`, `Employee6` khi cần để code biên dịch và ánh xạ đúng. Không vì một kiểu tham chiếu mà code các chức năng ngoài module.

### Module 2

JSP: `ManagerHomeView6.jsp`, `ReportMenuView6.jsp`, `RevenueStatisticsFilterView6.jsp`, `RevenueStatisticsListView6.jsp`, `ServicePartDetailView6.jsp`, `InvoiceDetailView6.jsp`.

Servlet: `RevenueServlet6`, `ServicePartServlet6`, `InvoiceServlet6`.

DAO: `RevenueDAO6`, `ServiceDAO6`, `SparePartDAO6`, `ServiceUsageDAO6`, `PartUsageDAO6`, `InvoiceDAO6`, dùng chung `DAO6`.

Entity chính: `RevenueStats6`, `Service6`, `SparePart6`, `ServiceUsage6`, `PartUsage6`, `RepairOrder6`, `Invoice6`, `Payment6`, `Vehicle6`, `VehicleBrand6`, `PartBrand6`, `Category6`, `Customer6`, `Member6`, `Employee6`, `Manager6`, `SaleStaff6`, `Technician6`. Chép đủ thuộc tính từ bản thiết kế cuối, không chỉ phần xuất hiện trên UI.

### Hỗ trợ đăng nhập và cấp mã đã được bổ sung

- LoginView6.jsp, LoginServlet6, MemberDAO6.authenticate(username:String, password:String):Member6, AuthFilter6 và utility hash password theo mục 3.
- CodeSequenceDAO6(Connection con), nextCode(prefix:String):String, cùng tblCodeSequence6 để cấp mã trong transaction lưu.
- DataAccessException6 và BookingConflictException6 trong garaman.exception.
- Nếu cần DAO6(Connection con)/SlotDAO6(Connection con) để mượn connection cho transaction, bổ sung và cập nhật thiết kế; connection mượn không do DAO con tự commit/close.

Không bắt buộc tách DAO interface/implementation như phần DAO pattern của LAB nếu sơ đồ chọn DAO cụ thể kế thừa DAO6. Không tự đổi sang Spring/JPA.

## 6. Hợp đồng phương thức cần kiểm tra với bản thiết kế cuối

Giữ các chữ ký nghiệp vụ dưới đây, đối chiếu tên/import với bản cuối và các thay đổi đã cho phép ở mục 3. Date của module 2 là java.util.Date, ngày chọn của getAvailableSlots vẫn là LocalDate; chuyển đổi JDBC ở biên. Sửa lỗi chính tả trong ảnh bằng cách cập nhật code và thiết kế cùng lúc, không để hai tên method khác nhau.

### DAO module 1

| Lớp | Phương thức | Kết quả và trách nhiệm |
|---|---|---|
| `CustomerDAO6` | `getCustomerInfo(customerCode : String) : Customer6` | Khách hàng gồm thông tin Member kế thừa; hành vi không tìm thấy theo contract |
| `VehicleDAO6` | `getVehiclesOfCustomer(customerId : int) : Vehicle6[]` | Xe thuộc khách; không có xe trả mảng rỗng theo convention đã chốt |
| `VehicleDAO6` | `findById(vehicleId : int) : Vehicle6` | Lấy xe thật; kiểm tra quyền sở hữu trước khi đặt lịch |
| `VehicleDAO6` | `saveVehicle(vehicle : Vehicle6, customerId : int) : Vehicle6` | Lưu xe gắn khách server-side; return đối tượng đã có ID thật |
| `VehicleBrandDAO6` | `getVehicleBrands() : VehicleBrand6[]` | Các hãng để chọn; không trả kiểu DAO |
| `SlotDAO6` | `getAvailableSlots(appointmentDate : LocalDate) : AppointmentSlot6[]` | Các slot có thể đặt của ngày chọn theo trạng thái, hold hết hạn và lịch làm việc |
| `SlotDAO6` | `holdSlot(slotId : int) : AppointmentSlot6` | Atomic hold 3 phút; return slot có token/expiry; thất bại phải phân biệt với lỗi SQL |
| `SlotDAO6` | `validateHold(slotId : int, holdCode : String) : boolean` | Token khớp, chưa hết hạn, trạng thái hợp lệ |
| `SlotDAO6` | `releaseSlot(slotId : int, holdCode : String) : boolean` | Chỉ giải phóng hold tương ứng, không giải phóng hold người khác |
| `SlotDAO6` | `consumeHold(slotId : int, holdCode : String) : boolean` | Chỉ consume hold hợp lệ; tham gia transaction lưu cuối |
| `AppointmentDAO6` | `createAppointment(appointment : Appointment6, holdCode : String) : Appointment6` | Atomic persist + consume; return đối tượng đã lưu |

Giữ đúng các constructor đã chốt. Nếu cần constructor nhận Connection để chia sẻ transaction, bổ sung tối thiểu và ghi vào thay đổi thiết kế; không chỉ gắn nhãn “atomic” nhưng mỗi DAO vẫn dùng một connection riêng.

### DAO module 2

| Lớp | Phương thức | Trách nhiệm |
|---|---|---|
| `RevenueDAO6` | `getRevenueStatistics(startDate : Date, endDate : Date, statisticType : String) : RevenueStats6[]` | Tổng hợp, sort doanh thu giảm dần theo quy tắc đã chốt |
| `ServiceDAO6` | `getServiceInfo(serviceCode : String) : Service6` | Thông tin dịch vụ |
| `SparePartDAO6` | `getPartInfo(partCode : String) : SparePart6` | Thông tin phụ tùng và danh mục liên quan cần hiển thị |
| `ServiceUsageDAO6` | `getTotalUsageCount(serviceCode : String) : int` | Số lần sử dụng toàn thời gian, không nhận khoảng ngày |
| `ServiceUsageDAO6` | `getAllServiceUsageOfService(serviceCode : String, startDate : Date, endDate : Date) : ServiceUsage6[]` | Các lần sử dụng trong khoảng đã chọn |
| `PartUsageDAO6` | `getTotalUsageCount(partCode : String) : int` | Số lần sử dụng toàn thời gian |
| `PartUsageDAO6` | `getAllPartUsageOfPart(partCode : String, startDate : Date, endDate : Date) : PartUsage6[]` | Các lần sử dụng trong khoảng đã chọn |
| `InvoiceDAO6` | `getInvoiceDetail(invoiceCode : String) : Invoice6` | Hóa đơn đầy đủ, không chỉ dòng được chọn |
| `InvoiceDAO6` | `getInvoiceCodeByUsage(itemType : String, usageId : int) : String` | Hóa đơn chứa lần sử dụng cụ thể; giữ nếu đây là phương án đã chốt |

Constructor `DAO6()` phải đúng tên lớp, `con` có kiểu `Connection`, không phải `Connection()`. Nếu con private thì cần cách truy cập hợp lệ; nếu subclasses trực tiếp dùng thì chọn protected và phản ánh trong thiết kế.

Servlet: module 1 có `BookingServlet6.doGet/doPost`, `VehicleServlet6.doGet/doPost`, `AppointmentServlet6.doPost`; module 2 dùng `doGet` cho các truy vấn nếu đúng bản cuối. Những private handler có thể thêm để tổ chức code, liệt kê trong tài liệu triển khai. Thêm public `doGet`/route mới thì cập nhật contract/thiết kế, không giả định đã tồn tại.

## 7. Quy tắc entity và CSDL

Trích toàn bộ thuộc tính từ sơ đồ cuối vào contract trước, rồi sinh JavaBean/getter/setter đúng từng tên. Một số điểm không được làm sai:

- `RevenueStats6`: `itemCode:String`, `itemName:String`, `itemType:String`, `usageCount:int`, `totalRevenue:double`; không có `id`, không có `totalProfit` thay thế.
- Appointment6 giữ id, appointmentCode, requestDescription, createAt, status, ss, vehicle, slot, cus. Thông tin cá nhân chỉ đọc từ cus; không thêm contact snapshot hoặc dùng request để cập nhật hồ sơ. ss có thể NULL khi PENDING.
- `AppointmentSlot6`: `id`, `startTime`, `holdCode`, `holdExpiresAt`, `status`; giữ cả phần ngày và giờ thực sự.
- Vehicle6 giữ id, licensePlate, vehicleModel, currentMileage, manufactureYear, cus, brand; bổ sung vehicleCode:String theo Q07 và đổi manufactureYear sang Integer theo Q08. Không dùng lại isOwner/km, không thêm control nhập km.
- `Service6` có `serviceCode`; `SparePart6` có `partCode`; `Invoice6` có `invoiceCode`, `issueDate`, `order`, `payment`.
- ServiceUsage6 có saleUnitPrice; PartUsage6 có usedQuantity và saleUnitPrice. Bổ sung order:RepairOrder6 vào cả hai như Q14; mapping từ repairOrderId đã có, không đổi return type mảng.
- `RepairOrder6` có `repairOrderCode`, `completionTime`, danh sách serviceUsage/partUsage và các tham chiếu theo bản cuối.
- `Customer6`/`Employee6` kế thừa Member; các vai trò kế thừa Employee; không khai báo lại ID/họ tên ở subclass nếu đã kế thừa.

DDL cuối mới là nguồn tên bảng/cột vật lý. Có lớp Java không có nghĩa phải tạo một bảng riêng; bảng kế thừa rỗng hoặc bảng thống kê có thể đã được loại bỏ trong thiết kế CSDL. Ánh xạ rõ subtype Java với bảng còn lại và thông tin vai trò; không tự phục hồi các bảng đã bỏ.

Không sửa DDL hiện có để che lỗi mapping. Nếu DDL thiếu khả năng lưu dữ liệu bắt buộc, tạo migration tối thiểu và cập nhật thiết kế theo quy trình code mốc. Không tự DROP schema hoặc sửa dữ liệu thật.

## 8. Hợp đồng HTTP và view

Chọn /login cho LoginServlet6 (GET mở form, POST action login/logout), /booking, /appointment, /vehicle, /revenue, /service-part, /invoice cho các Servlet tương ứng, dưới context /garaman6. AuthFilter6 bảo vệ sáu route nghiệp vụ theo role. Ghi rõ actions/home/menu/filter/list/detail trong HTTP contract và dùng thống nhất trong JSP/JS/Servlet.

Mỗi request cần ghi đủ:

| HTTP method/URL/action | input name | kiểu wire | chuyển sang kiểu Java | lấy danh tính từ đâu | handler/DAO | request attributes/session | forward/redirect |
|---|---|---|---|---|---|---|---|

Lấy chính xác input name từ UI cuối. Ví dụ `vehicle : Select` có thể gửi giá trị ID qua input tên `vehicle`; DAO vẫn nhận tham số `vehicleId`/`customerId` theo signature. Không tự coi tên control và tên tham số DAO phải giống nhau; mapping phải được khai báo.

Các dữ liệu view cần đối chiếu:

- Book: `cus`, `vehicleList`, `availableSlots`, `draftAppointment` nếu dùng, các giá trị form, thông báo.
- Confirm: `appointment`, `holdCode`, `holdExpiresAt`, `message`.
- Add vehicle: `cus`, `vehicleBrandList`, dữ liệu form và `message` nếu thiết kế có.
- Revenue list: `revenueStats`, `totalRevenue`, khoảng ngày, `statisticType`, `currentPage`, `totalPages`.
- Detail: `service` hoặc `sparePart`; `serviceUsageList` hoặc `partUsageList`; `lifetimeUsageCount`, `itemType`, `itemCode`, khoảng ngày, ngữ cảnh lọc và phân trang.
- Invoice: `invoice`; dữ liệu liên quan có thể truy cập qua object graph của hóa đơn, không bắt buộc lặp thành các field UI độc lập.

Các field view là dữ liệu request/session, không phải mutable Java fields dùng chung trên Servlet/JSP-generated instance. Không lưu draft/hold/filter của nhiều người dùng trong static field hoặc field Servlet dùng chung.

JSP không tự truy vấn SQL, không trực tiếp gọi DAO để đóng gói hoặc lưu lịch hẹn, không gọi Java `doGet` của Servlet khác. Link/form gửi HTTP request; Servlet đặt dữ liệu và chọn JSP bằng forward. Redirect làm trình duyệt gửi request mới; request attributes không tự tồn tại qua redirect.

Sau POST dùng redirect khi có endpoint GET phù hợp và cần chống submit lại; nếu phải bổ sung endpoint thì ghi thay đổi. Đừng áp PRG bằng cách redirect đến URL mà Servlet chỉ có doPost rồi gây lỗi. Dù forward hay redirect, xác nhận lịch hẹn vẫn cần chống double-submit ở server.

## 9. Luồng module 1

### 9.1 Mở form và chọn ngày

1. Từ CustomerHome, request đến BookingServlet6; lấy khách hiện tại từ session/context đáng tin cậy.
2. CustomerDAO6 lấy hồ sơ; VehicleDAO6 lấy xe của đúng khách.
3. Hiển thị Book; không tự thêm ô km nếu UI cuối không có.
4. Khi chọn ngày, gọi getAvailableSlots(LocalDate); hiển thị các slot đúng ngày và khả dụng. Không có dữ liệu phải hiển thị trạng thái phù hợp, không tự dựng slot giả để đặt.

### 9.2 Giữ chỗ và đóng gói

5. Khách chọn slot/xe, kiểm tra thông tin cá nhân chỉ đọc, nhập nội dung rồi POST BookingServlet6; không gửi dữ liệu liên hệ để thay hồ sơ.
6. Server validate bắt buộc, ngày/slot, quyền sở hữu xe, định dạng/độ dài theo DDL và thiết kế.
7. Gọi holdSlot(slotId) để giữ 3 phút. Token sinh server-side; return đầy đủ slot/holdCode/expiry.
8. BookingServlet6 tạo new Appointment6(), gán cus từ khách đã xác thực/tải từ DB, xe thuộc khách và slot vừa hold, requestDescription; chưa INSERT. Bỏ qua giá trị fullName/phone/email/customerId do client tự sửa; Q02 đã bỏ sửa liên hệ.
9. Lưu draft theo phiên/ngữ cảnh, forward Confirm cùng thông tin giữ chỗ.

### 9.3 Xác nhận và lưu

10. POST AppointmentServlet6 khi khách xác nhận lần cuối.
11. Đọc draft đáng tin cậy, kiểm tra hold của phiên hiện tại; gọi validateHold theo thiết kế.
12. Gọi createAppointment(appointment, holdCode). Bên trong transaction cuối phải kiểm tra/consume hold atomically cùng INSERT.
13. Dùng cùng transaction/Connection cho mọi thao tác cần atomic; rollback nếu bất kỳ bước nào thất bại. Không mở khóa DB trong suốt 3 phút chờ khách.
14. Mã, ID, createAt, trạng thái và FK được lưu thật; nhân viên tiếp nhận chưa có thì xử lý NULL theo quy tắc/DDL cuối, không gán bừa một nhân viên.
15. Trả entity đã lưu; hiển thị thành công bằng ConfirmView6.jsp theo thiết kế. Không thêm thông báo nhân viên đã bị loại khỏi V2, không tự thêm trang BookingSuccess nếu không có trong thiết kế cuối.

### 9.4 Thêm xe

16. GET VehicleServlet6 → VehicleBrandDAO6 → AddNewVehicleView6.jsp.
17. POST VehicleServlet6 đọc hãng/dòng/biển số/năm đúng tên input; kiểm tra dữ liệu, gắn khách từ session, gọi saveVehicle(vehicle, customerId).
18. Redirect URL BookingServlet6, tải lại xe từ DB để thấy xe mới. Nếu cần giữ form dở, lưu/khôi phục theo contract; không làm mất dữ liệu người dùng mà không có quy tắc.

Áp dụng đầy đủ mặc định giờ làm/slot, release rồi hold lại, biển số unique toàn hệ thống, năm tùy chọn và currentMileage=0 ở mục 3. Không triển khai quản lý lịch gara hay chuyển quyền sở hữu xe. Edit/back dùng POST action để giải phóng hold, không mutation bằng GET.

## 10. Luồng module 2

### 10.1 Menu, lọc và danh sách

1. ManagerHome → request RevenueServlet6 → ReportMenu.
2. Chọn doanh thu → RevenueServlet6 → Filter.
3. Nhận khoảng ngày/loại, validate, gọi getRevenueStatistics.
4. Hiển thị các dòng thống kê, tổng toàn bộ kết quả và phân trang ổn định; giữ bộ lọc khi back.

### 10.2 Dùng chung màn hình chi tiết

5. Một dòng chứa `itemType` + `itemCode`. Đây là định danh logic của dòng kết quả; không thêm ID thống kê.
6. `statisticType` là bộ lọc danh sách, có thể ALL; `itemType` là loại một đối tượng đã chọn, chỉ SERVICE hoặc PART theo bộ giá trị đã chốt. Không đánh đồng hai biến.
7. Nhánh SERVICE: ServiceDAO6.getServiceInfo(itemCode), ServiceUsageDAO6.getTotalUsageCount(itemCode), ServiceUsageDAO6.getAllServiceUsageOfService(itemCode, startDate, endDate).
8. Nhánh PART: SparePartDAO6.getPartInfo(itemCode), PartUsageDAO6.getTotalUsageCount(itemCode), PartUsageDAO6.getAllPartUsageOfPart(itemCode, startDate, endDate).
9. Forward cùng `ServicePartDetailView6.jsp`; chỉ hiển thị nhánh tương ứng. Không tách hai JSP nếu không có lý do bắt buộc và bản sửa thiết kế đi kèm.
10. Lifetime count đếm mọi dòng Usage của đối tượng, không lọc ngày/trạng thái. Lịch sử trong khoảng lọc theo Invoice.issueDate và Payment.status=PAID như báo cáo. Render mã phiếu/khách/biển số/ngày hoàn thành qua usage.order đã bổ sung ở Q14. Không đổi mảng public API sang DTO/List.

### 10.3 Hóa đơn và quay lại

11. Link hóa đơn gắn một `usageId` và `itemType`, không chỉ itemCode. Theo phương án đã chốt, ServicePartServlet6 gọi InvoiceDAO6.getInvoiceCodeByUsage rồi redirect InvoiceServlet6 với invoiceCode.
12. InvoiceServlet6 gọi getInvoiceDetail(invoiceCode); dựng hóa đơn đầy đủ qua các FK thật.
13. Hóa đơn có thể chứa cả dịch vụ và phụ tùng; hiển thị tất cả dòng, đơn giá lịch sử, số lượng, thành tiền, tổng và thanh toán. Không giới hạn hóa đơn về đúng một dòng vừa chọn.
14. Back khôi phục đúng detail và bộ lọc/trang; back tiếp về danh sách thống kê đúng ngữ cảnh. Thiết kế session context sao cho các tab/người dùng không ghi đè dữ liệu của nhau.

Scenario V2 xem dịch vụ rồi phụ tùng là hai lượt minh họa. Code không được ép trình tự đó; người dùng chọn một trong hai loại từ danh sách bất cứ lúc nào.

## 11. Quy tắc thống kê đã chốt — không tự thay căn cứ

- Ngày lọc là tblInvoice6.issueDate; chỉ Payment.status=PAID. Không thay bằng usageTime/completionTime/paymentTime.
- Khoảng ngày tính đủ hai đầu bằng [đầu ngày bắt đầu, đầu ngày sau ngày kết thúc), múi giờ Asia/Ho_Chi_Minh.
- Báo cáo và lịch sử sử dụng trong khoảng dùng cùng điều kiện ngày/thanh toán; lifetime count là COUNT mọi dòng Usage của đối tượng, không lọc ngày/trạng thái.
- usageCount = COUNT dòng, không SUM usedQuantity. PartUsage usedQuantity=4 là một lần dùng nhưng tiền nhân 4.
- Tiền dịch vụ SUM(saleUnitPrice); tiền phụ tùng SUM(usedQuantity * saleUnitPrice). Không thuế, giảm giá, rating hay giá danh mục hiện tại.
- Tiền SQL dùng DECIMAL/BigDecimal để tính; giữ public double của entity khi đã chốt và chuyển đổi ở biên. Hiển thị VNĐ, không âm thầm bỏ phần thập phân có thật.
- 20 dòng/trang; revenue sort totalRevenue DESC, itemCode ASC, itemType ASC. History sort issueDate DESC, usageId DESC. Tổng báo cáo tính mọi dòng sau lọc, không chỉ trang hiện tại.
- Không có dòng: mảng rỗng, tổng 0, currentPage 1, totalPages 0, nút trang vô hiệu hóa.
- Invoice.totalAmount lấy vwInvoice6/truy vấn tương đương; SparePart.stockQuantity lấy vwSparePart6. Không tạo cột dẫn xuất không có trong schema.
- Tránh JOIN hai quan hệ 1-n làm nhân bản doanh thu. Đối chiếu seed có nhiều dịch vụ/phụ tùng trong cùng phiếu và giá hiện tại khác giá lịch sử.
- Các quyết định bổ sung, mã trạng thái và công thức chi tiết ở mục 3 là một phần hợp đồng triển khai.

## 12. Yêu cầu code thực tế

- PreparedStatement cho tham số người dùng; không ghép input trực tiếp vào SQL.
- Quản lý resources/Connection đúng vòng đời; không một mutable Connection dùng chung cho mọi request. Khi cần transaction nhiều DAO, quản lý rõ owner của connection/commit/rollback/close.
- Không nuốt SQLException rồi return true, null hoặc mảng rỗng như thành công. Tách tình huống không có dữ liệu khỏi lỗi truy cập DB.
- Validate server-side; dùng thông tin khách từ session để kiểm tra xe và dữ liệu được phép đọc/ghi. Kiểm tra token/ownership khi release/consume hold.
- Mutation qua POST; có cơ chế chống giả mạo request phù hợp với session, không dùng holdCode thay cho toàn bộ kiểm soát phiên. Mọi public helper/route bổ sung ghi trong contract.
- Hiển thị text người dùng bằng cơ chế escape phù hợp; giao diện tiếng Việt UTF-8; ngày/tiền theo format UI. Không đẩy lỗi SQL/stack trace ra trang người dùng.
- Không hardcode mật khẩu/secret vào source. Cung cấp file mẫu cấu hình và hướng dẫn thiết lập.
- JSP dùng EL/JSTL hoặc cơ chế view phù hợp với runtime, không chứa SQL/logic lưu trữ. JavaScript chỉ hỗ trợ tương tác; server giữ quyền quyết định tính hợp lệ.
- Bám sát ảnh UI về bố cục, nội dung, cột bảng, nút và màu sắc; có thể bổ sung CSS responsive cần thiết nhưng không tự thiết kế lại hoặc bỏ cột khó truy vấn.
- Nút thống kê khách hàng trong menu thuộc ngoài phạm vi: thể hiện vô hiệu hóa/chưa triển khai rõ ràng thay vì dẫn đến route lỗi; không mở rộng thành module thứ ba.
- Không hardcode số liệu thống kê, hóa đơn, rating hoặc danh sách xe thay DB. Mock chỉ được dùng trong test có ghi rõ.
- Không để TODO/stub/UnsupportedOperationException ở nhánh nghiệp vụ chính.
- Viết tối thiểu các xử lý lỗi cần để bảo vệ dữ liệu, dù scenario chỉ mô tả happy case. Không mở rộng thành module ngoại lệ/CRUD mới.

## 13. Dữ liệu seed và kiểm thử phải có

Tạo schema/migration theo bản cuối và seed tối thiểu cho hai module, không cần seed cả gara. Nếu DB đã có thì dùng database/test schema riêng hoặc migration không phá dữ liệu.

Seed phải có hai tài khoản CUSTOMER/MANAGER dùng password hash, không phải login bypass; dữ liệu và quyền theo schema mapping thực. Seed phải đủ:

- Khách có xe, khách/xe khác để kiểm tra ownership; hãng xe cho form thêm xe.
- Slot trống, slot đang held còn hạn, held hết hạn, slot đã dùng; ngày demo nằm trong lịch hoạt động được chốt.
- Dịch vụ/phụ tùng, brand/category, phiếu sửa chữa có thông tin khách/xe và các Usage.
- Vì stockQuantity lấy từ vwSparePart6, seed số lượng nhập ban đầu đủ qua ReceiptDetail/PartReceipt và các FK tối thiểu nếu cần. Đây chỉ là dữ liệu seed để view tồn kho đúng, không code chức năng nhập kho/nhà cung cấp.
- Một phiếu có nhiều dịch vụ và nhiều phụ tùng để bắt lỗi nhân bản JOIN.
- Phụ tùng có usedQuantity > 1; giá hiện tại khác giá bán lịch sử.
- Sử dụng trong khoảng và ngoài khoảng để phân biệt lifetime count với filtered history.
- Timestamp ở đầu ngày/cuối ngày; trạng thái được tính và không được tính doanh thu.
- Hóa đơn có cả dịch vụ và phụ tùng, payment đúng trạng thái; các bảng kế thừa ánh xạ theo schema thực.

Kiểm thử cần thiết, không chỉ tests lặp lại implementation:

1. Đăng nhập CUSTOMER/MANAGER bằng hash, sai mật khẩu/trạng thái bị chặn, đúng role mới vào module; logout làm phiên cũ không còn truy cập được. Build/package project thành công; tên class/field/signature khớp contract bằng kiểm tra source/AST/reflection phù hợp. Thứ tự/tên tham số kiểm tra từ source nếu bytecode không giữ tên.
2. DAO chạy với DB thật/test DB, không chỉ mock; migration và mapping kế thừa hoạt động.
3. Mở Book, chọn ngày, xe thuộc khách, giữ chỗ và Confirm rồi lưu đúng một Appointment thật.
4. Hai phiên/connection tranh cùng slot: chỉ một hold thành công nếu sức chứa 1; hold sai/hết hạn không được lưu.
5. Hai request xác nhận/double-click/retry không tạo hai lịch; rollback không để trạng thái slot và appointment lệch nhau.
6. Thêm xe có vehicleCode tăng dần, biển số chuẩn hóa/unique toàn hệ thống, năm NULL và currentMileage=0 xử lý đúng; quay về thấy xe mới và giữ form dở. Kiểm thử hai transaction cấp mã đồng thời không trùng.
7. Báo cáo SERVICE, PART, ALL đúng công thức và số liệu tính tay; không nhân bản JOIN, không dùng giá hiện tại thay giá lịch sử.
8. Đúng biên ngày, sort, tổng toàn kết quả, phân trang và bộ lọc sau Back.
9. Màn hình chung hiển thị đúng SERVICE/PART khi chọn loại nào trước cũng được; không lỗi null nhánh không dùng. Không còn rating; dữ liệu cá nhân ở Book readonly và sửa request không thay được cus.
10. Lifetime count khác filtered count khi seed có lịch sử ngoài khoảng; usedQuantity > 1 được tính đúng định nghĩa.
11. Mở đúng toàn bộ hóa đơn từ cả ServiceUsage và PartUsage; đúng khách/xe/phiếu, không chỉ hiện dòng được chọn.
12. Không có dữ liệu hiển thị đúng trạng thái; lỗi DB không báo thành công giả.
13. Chạy thật trên container, kiểm tra JSP compile/EL/JSTL và submit/redirect. `mvn package` đơn thuần chưa chứng minh JSP đã chạy được.

Xuất expected/actual, lệnh chạy và kết quả. Nếu môi trường không cho chạy container/DB/browser, nói chính xác phần nào chưa kiểm thử; không ghi “đã pass” hoặc “đúng 100%” khi chưa có bằng chứng.

## 14. Trình tự thực hiện và bàn giao

1. Đọc nguồn, lập manifest; phát hiện chữ ký/field/SQL/UI xung đột và quy tắc nghiệp vụ còn thiếu. Chỉ hỏi phần không thể suy ra, không hỏi lại những quyết định đã có.
2. Lập hợp đồng lớp/schema/HTTP và traceability. Không dừng ở đây: tiếp tục code các phần đã rõ.
3. Dựng cấu hình build/runtime/DB, dùng đúng package.
4. Viết entity, DAO và kiểm tra với DB thật; hoàn thiện transaction đặt lịch và SQL thống kê.
5. Viết Servlet rồi JSP/CSS/JS theo UI; kiểm tra đủ request/session/route và dữ liệu Back.
6. Khi code buộc khác, cập nhật DESIGN_CHANGES và nguồn thiết kế ngay trong quá trình làm; không đợi cuối rồi quên.
7. Chạy build, integration và luồng UI quan trọng; sửa lỗi đến khi các kiểm tra phù hợp pass.
8. Xuất project/source thực, SQL/migration/seed, cấu hình mẫu, tài liệu chạy, các hợp đồng, test evidence và thiết kế cuối đã đồng bộ.

README cần có: phiên bản thật đã kiểm tra, baseline V1 và migration V2, DB user/cấu hình local, Maven Wrapper trên Windows 10, deploy WAR qua Tomcat startup.bat ngoài IDE, URL /garaman6/login, tài khoản demo có password hash, role đúng, ngày demo có slot và cách chạy tests. Không dùng cách chọn role giả thay login.

Trong phản hồi cuối: nêu chức năng đã chạy, vị trí file/project, những thay đổi thiết kế thật sự, bằng chứng kiểm thử và giới hạn chưa kiểm chứng. Không chỉ đưa các đoạn code rời và yêu cầu tôi tự ghép.

## 15. Tiêu chí hoàn thành

Chỉ coi công việc hoàn thành khi:

- Hai module chạy được với persistence thật và UI đã cung cấp.
- Mỗi yêu cầu/bước/cột dữ liệu quan trọng truy vết được đến code và phép kiểm thử.
- Tên lớp, thuộc tính, public method, tham số và return type khớp baseline cuối; mọi khác biệt so với đầu vào ban đầu được liệt kê và đã sửa ngược thiết kế.
- Không bỏ dữ liệu liên hệ, không tạo trùng lịch, không giữ slot sai, không tính sai doanh thu do JOIN/giá/ngày/trạng thái.
- DDL, seed, entity mapping, HTTP contract, scenario thiết kế và sơ đồ cuối mô tả cùng một hệ thống đã triển khai.
- Không triển khai lan sang các chức năng ngoài hai module.

KẾT THÚC PROMPT

---

Ghi chú: các quyết định Q01–Q18 đã được tích hợp trong prompt V2 và file GaraMan_Quyet_dinh_trien_khai_v2.md. Chỉ còn kiểm tra thực tế môi trường, quyền kết nối và nguồn mới nếu người dùng bổ sung; không yêu cầu người dùng trả lời lại toàn bộ bảng câu hỏi cũ.
