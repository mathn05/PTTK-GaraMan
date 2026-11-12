# Bảng truy vết chi tiết — Module 1

Source SHA-256: `f0c79f6eb835cc5a8145068a161d6e15a97f682e65176925e039de5c334e736b`; [manifest](evidence/snapshot-after.json).

Đi kèm [V3 chính](Scenario_V3_Module1.md). Đây là bằng chứng source kết hợp regression runtime, không phải instrumentation log từng setter. Các dòng dưới đây giải thích caller/query/đóng gói/return; không biến chúng thành hàng trăm bước trong V3 chính.

DAO() sở hữu connection, DAO(Connection) mượn; chỉ owner commit/close. Các DAO con dùng cùng connection khi hydrate trong transaction. Filter mapping REQUEST kiểm CSRF/role trước handler; JSP chỉ render và gửi scalar. Date ở Java là java.util.Date, ID/count business Integer, tiền BigDecimal; private date methods dùng DAO6.zone().

| Dòng | Bước V3 | Caller và thao tác thật | Nguồn |
|---|---|---|---|
| 1 | 1 | Khách → link JSP → HTTP GET, không gọi trực tiếp lớp Java. | [CustomerHomeView6.jsp:10](../../src/main/webapp/WEB-INF/views/customer/CustomerHomeView6.jsp#L10) |
| 2 | 2 | AppFilter kiểm UTF-8/session/CSRF với POST. | [AppFilter6.java:22](../../src/main/java/garaman/filter/AppFilter6.java#L22) |
| 3 | 2 | AuthFilter kiểm CUSTOMER cho route booking. | [AuthFilter6.java:16](../../src/main/java/garaman/filter/AuthFilter6.java#L16) |
| 4 | 2 | Container dispatch GET. | [BookingServlet6.java:19](../../src/main/java/garaman/servlet/BookingServlet6.java#L19) |
| 5 | 3 | Context thuộc session map, cap64, UUID, ngày mặc định ngày mai. | [BookingServlet6.java:90](../../src/main/java/garaman/servlet/BookingServlet6.java#L90) |
| 6 | 3 | Khóa từng context, không dùng một bộ attribute chung ghi đè các tab. | [BookingServlet6.java:22](../../src/main/java/garaman/servlet/BookingServlet6.java#L22) |
| 7 | 4 | Servlet lấy currentUser Customer6 từ session, query hồ sơ mới. | [BookingServlet6.java:106](../../src/main/java/garaman/servlet/BookingServlet6.java#L106) |
| 8 | 4 | DAO query Customer JOIN Member theo customerCode. | [CustomerDAO6.java:10](../../src/main/java/garaman/dao/CustomerDAO6.java#L10) |
| 9 | 5 | DAO new Customer6, setter customerCode, nhận profile từ fillMember. | [CustomerDAO6.java:16](../../src/main/java/garaman/dao/CustomerDAO6.java#L16) |
| 10 | 5 | Gán các setter void của Member; username có thì new Account6 và setAcc, không có giữ null. | [MemberDAO6.java:25](../../src/main/java/garaman/dao/MemberDAO6.java#L25) |
| 11 | 6 | Servlet gọi DAO với id hồ sơ trusted. | [BookingServlet6.java:72](../../src/main/java/garaman/servlet/BookingServlet6.java#L72) |
| 12 | 6 | Query physical customerFK, ORDER BY id, vòng readVehicle. | [VehicleDAO6.java:13](../../src/main/java/garaman/dao/VehicleDAO6.java#L13) |
| 13 | 6 | new Vehicle6, getObject Integer cho Km/năm, CustomerDAO.findById và VehicleBrandDAO.findById; trả xe. | [VehicleDAO6.java:57](../../src/main/java/garaman/dao/VehicleDAO6.java#L57) |
| 14 | 7 | Lọc ngày nửa mở, tương lai, AVAILABLE/HELD hết hạn, không active appointment; sort startTime,id. | [SlotDAO6.java:15](../../src/main/java/garaman/dao/SlotDAO6.java#L15) |
| 15 | 7 | new AppointmentSlot6, setter void id/startTime/token/expiry/status, return array qua caller. | [SlotDAO6.java:68](../../src/main/java/garaman/dao/SlotDAO6.java#L68) |
| 16 | 8 | form giữ lựa chọn context, min/max ngày, request attrs rồi forward. | [BookingServlet6.java:67](../../src/main/java/garaman/servlet/BookingServlet6.java#L67) |
| 17 | 8 | EL/JSTL render contact readonly và các control cũ, không tạo entity. | [BookAppointmentView6.jsp:22](../../src/main/webapp/WEB-INF/views/customer/BookAppointmentView6.jsp#L22) |
| 18 | 9 | Đọc HTML attribute để tránh control action che form.action, URLSearchParams. | [app.js:29](../../src/main/webapp/assets/app.js#L29) |
| 19 | 9 | Fetch HTML, version phản hồi hiện hành. | [app.js:31](../../src/main/webapp/assets/app.js#L31) |
| 20 | 10 | Cập nhật ngày trong Map khi GET. | [BookingServlet6.java:24](../../src/main/java/garaman/servlet/BookingServlet6.java#L24) |
| 21 | 10 | form đọc lại customer → vehicle list → slot list, không endpoint JSON. | [BookingServlet6.java:72](../../src/main/java/garaman/servlet/BookingServlet6.java#L72) |
| 22 | 11 | DOMParser chọn slot-fieldset, chỉ replace phản hồi version hiện tại. | [app.js:36](../../src/main/webapp/assets/app.js#L36) |
| 23 | 12 | Form gửi context/csrfToken/ngày/slot/vehicle/description, không live Java object. | [BookAppointmentView6.jsp:32](../../src/main/webapp/WEB-INF/views/customer/BookAppointmentView6.jsp#L32) |
| 24 | 13 | Container dispatch POST. | [BookingServlet6.java:28](../../src/main/java/garaman/servlet/BookingServlet6.java#L28) |
| 25 | 13 | Whitelist action, chưa completed/hold, validate ngày cấu hình và nội dung≤255. | [BookingServlet6.java:32](../../src/main/java/garaman/servlet/BookingServlet6.java#L32) |
| 26 | 14 | Đọc hồ sơ mới; không tin customer/contact client. | [BookingServlet6.java:47](../../src/main/java/garaman/servlet/BookingServlet6.java#L47) |
| 27 | 14 | So sánh giá trị Integer, kể cả >127. | [BookingServlet6.java:49](../../src/main/java/garaman/servlet/BookingServlet6.java#L49) |
| 28 | 15 | Query slot và so ngày theo DAO6.zone(). | [BookingServlet6.java:52](../../src/main/java/garaman/servlet/BookingServlet6.java#L52) |
| 29 | 16 | UUID + UPDATE tương lai/chưa active + NOW(6)+3 phút. | [SlotDAO6.java:29](../../src/main/java/garaman/dao/SlotDAO6.java#L29) |
| 30 | 16 | Đọc đúng token và readSlot; autocommit hoàn tất, không giữ transaction chờ khách. | [SlotDAO6.java:37](../../src/main/java/garaman/dao/SlotDAO6.java#L37) |
| 31 | 17 | Context giữ token/expiry do server trả. | [BookingServlet6.java:57](../../src/main/java/garaman/servlet/BookingServlet6.java#L57) |
| 32 | 17 | Servlet new entity (id null), setters void, c.put draft; không request trusted/guard/forward vòng. | [BookingServlet6.java:59](../../src/main/java/garaman/servlet/BookingServlet6.java#L59) |
| 33 | 18 | Chọn draft hoặc completed, đặt request attrs. | [BookingServlet6.java:77](../../src/main/java/garaman/servlet/BookingServlet6.java#L77) |
| 34 | 18 | Query TIMESTAMPDIFF/GREATEST cho đúng HELD/token, trả long TTL. | [SlotDAO6.java:48](../../src/main/java/garaman/dao/SlotDAO6.java#L48) |
| 35 | 18 | Forward thẳng JSP hiển thị. | [BookingServlet6.java:82](../../src/main/java/garaman/servlet/BookingServlet6.java#L82) |
| 36 | 19 | Render thông tin lịch và thời hạn. | [ConfirmView6.jsp:19](../../src/main/webapp/WEB-INF/views/customer/ConfirmView6.jsp#L19) |
| 37 | 19 | Không dựa vào Date.now máy khách để quyết định mốc hết hạn. | [app.js:6](../../src/main/webapp/assets/app.js#L6) |
| 38 | 20 | Trình duyệt chỉ gửi scalar context/token. | [ConfirmView6.jsp:22](../../src/main/webapp/WEB-INF/views/customer/ConfirmView6.jsp#L22) |
| 39 | 21 | Lấy draft/token/slot từ server. | [AppointmentServlet6.java:25](../../src/main/java/garaman/servlet/AppointmentServlet6.java#L25) |
| 40 | 21 | Bảo vệ ownership bằng giá trị Integer. | [AppointmentServlet6.java:27](../../src/main/java/garaman/servlet/AppointmentServlet6.java#L27) |
| 41 | 22 | Servlet gọi validateHold. | [AppointmentServlet6.java:29](../../src/main/java/garaman/servlet/AppointmentServlet6.java#L29) |
| 42 | 22 | DB kiểm HELD/token/expiry/startTime tương lai, trả boolean. | [SlotDAO6.java:44](../../src/main/java/garaman/dao/SlotDAO6.java#L44) |
| 43 | 23 | Truyền đúng draft cũ vào DAO. | [AppointmentServlet6.java:31](../../src/main/java/garaman/servlet/AppointmentServlet6.java#L31) |
| 44 | 23 | Chỉ transaction owner mở/commit/rollback; borrowed cần autoCommit=false. | [AppointmentDAO6.java:21](../../src/main/java/garaman/dao/AppointmentDAO6.java#L21) |
| 45 | 24 | Guarded consume đúng token còn hạn → BOOKED, xóa hold; cùng transaction. | [AppointmentDAO6.java:23](../../src/main/java/garaman/dao/AppointmentDAO6.java#L23) |
| 46 | 24 | Khóa kiểm ownership trước INSERT, rollback nếu thất bại. | [AppointmentDAO6.java:24](../../src/main/java/garaman/dao/AppointmentDAO6.java#L24) |
| 47 | 25 | Private UUID placeholder không sequence/MAX+1. | [AppointmentDAO6.java:28](../../src/main/java/garaman/dao/AppointmentDAO6.java#L28) |
| 48 | 25 | INSERT NOT NULL/UNIQUE hợp lệ ngay từ đầu, NOW(6), FK physical dài. | [AppointmentDAO6.java:29](../../src/main/java/garaman/dao/AppointmentDAO6.java#L29) |
| 49 | 25 | Lấy generated key, không suy đoán MAX+1. | [AppointmentDAO6.java:31](../../src/main/java/garaman/dao/AppointmentDAO6.java#L31) |
| 50 | 26 | UPDATE theo id và placeholder, affectedRows=1, không mã tạm ra ngoài. | [AppointmentDAO6.java:35](../../src/main/java/garaman/dao/AppointmentDAO6.java#L35) |
| 51 | 27 | new Appointment6 persisted, CustomerDAO/VehicleDAO/SlotDAO/MemberDAO dùng cùng con. | [AppointmentDAO6.java:46](../../src/main/java/garaman/dao/AppointmentDAO6.java#L46) |
| 52 | 27 | Commit sau hydrate thành công; rollback bao gồm consume/INSERT/mã nếu lỗi. | [AppointmentDAO6.java:39](../../src/main/java/garaman/dao/AppointmentDAO6.java#L39) |
| 53 | 28 | Giữ kết quả sau DAO return. | [AppointmentServlet6.java:31](../../src/main/java/garaman/servlet/AppointmentServlet6.java#L31) |
| 54 | 28 | URL chỉ action/context, không object. | [AppointmentServlet6.java:34](../../src/main/java/garaman/servlet/AppointmentServlet6.java#L34) |
| 55 | 29 | GET success chọn completed, không INSERT lại. | [BookingServlet6.java:23](../../src/main/java/garaman/servlet/BookingServlet6.java#L23) |
| 56 | 29 | JSP hiển thị persisted graph; refresh/double-submit cùng context giữ cùng kết quả. | [ConfirmView6.jsp:6](../../src/main/webapp/WEB-INF/views/customer/ConfirmView6.jsp#L6) |
