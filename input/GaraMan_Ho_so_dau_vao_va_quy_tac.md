# GaraMan — Hồ sơ đầu vào để AI triển khai đúng hai module

Bản V2 đã nhận câu trả lời Q01–Q18 và chọn phương án cho các mục người dùng giao đề xuất. Dùng cùng GaraMan_Prompt_Code_2_Module.md và GaraMan_Quyet_dinh_trien_khai_v2.md. Các thay đổi yêu cầu mới phải được cập nhật ngược vào thiết kế khi triển khai; không coi ảnh cũ là đã tự thay đổi.

Phạm vi: hai module đặt lịch/thống kê doanh thu, nhánh thêm xe và đăng nhập/đăng xuất tối thiểu hỗ trợ truy cập. Kiến trúc Entity — DAO — Servlet — JSP; không làm cả GaraMan.

## 1. Tài liệu cần cung cấp

| Mức độ | Tài liệu | Cung cấp thế nào | AI dùng để làm gì |
|---|---|---|---|
| Bắt buộc | Biểu đồ lớp thực thể toàn hệ thống pha thiết kế bản cuối | Ảnh rõ + mã PlantUML hoặc bảng lớp/thuộc tính nếu có | Chép đúng tên lớp, thuộc tính, kiểu dữ liệu, kế thừa, tham chiếu và bội số của phần liên quan |
| Bắt buộc | Biểu đồ lớp chi tiết pha thiết kế module 1 bản cuối | Cả tên trường, DAO, Servlet và chữ ký phương thức phải đọc được | Xác định hợp đồng code module đặt lịch |
| Bắt buộc | Biểu đồ lớp chi tiết pha thiết kế module 2 bản cuối | Tương tự module 1 | Xác định hợp đồng code module thống kê |
| Bắt buộc | CSDL bản cuối và DDL SQL thực tế | File `.sql`, không chỉ ảnh; kèm migration nếu có | Ánh xạ bảng/cột, PK/FK, UNIQUE, NULL, trạng thái, kiểu dữ liệu và cơ chế kế thừa |
| Bắt buộc | Scenario V2 cuối của hai module | Văn bản có số bước | Kiểm tra đủ chức năng; không lấy bản V2 cũ có bước thông báo nhân viên đã bị bỏ |
| Bắt buộc nếu đã làm | Scenario V3 và biểu đồ tuần tự pha thiết kế | Văn bản + nguồn biểu đồ | Xác định chủ thể thực hiện, thứ tự gọi DAO, request/response và transaction |
| Bắt buộc | Bộ giao diện của hai module | Tất cả ảnh, kể cả thêm xe, xác nhận, dịch vụ, phụ tùng, hóa đơn | Đảm bảo tên trường, cột bảng, nút, bố cục, dữ liệu và đường quay lại |
| Bắt buộc | Quy tắc nghiệp vụ đã chốt | Áp dụng quyết định V2 tại mục 3 | Không để AI tự quyết định các quy tắc ảnh hưởng dữ liệu hoặc doanh thu |
| Bắt buộc | Môi trường và thông tin chạy | Áp dụng môi trường đã chọn tại mục 2 | Chọn dependency, namespace Servlet, driver và hướng dẫn chạy tương thích |
| Nên có | Activity diagram hai module | Bản cuối | Kiểm tra luồng màn hình và vị trí xác nhận cuối |
| Nên có | Communication diagram V2 cuối | Module 1 31 bước; module 2 42 bước nếu đó vẫn là bản chốt | Kiểm tra không thiếu tương tác; phân biệt trách nhiệm pha phân tích và pha thiết kế |
| Nên có | State diagram liên quan | Appointment, Slot, Payment, Invoice/RepairOrder nếu có | Ánh xạ chính xác chuyển trạng thái và điều kiện tính doanh thu |
| Nên có | Biểu đồ lớp pha phân tích hai module | Bản cuối | Giải thích nguồn gốc nghiệp vụ, không dùng để ghi đè trách nhiệm Servlet/DAO của pha thiết kế |
| Nên có | Repo hoặc project đang có | Source + hướng dẫn build, không chỉ ảnh IDE | Tích hợp vào project thay vì dựng lại sai cấu trúc |
| Nên có | Dữ liệu mẫu có kết quả mong đợi | SQL seed và bảng tính tay | Kiểm chứng doanh thu, số lần sử dụng và liên kết hóa đơn |
| Không cần để bắt đầu hai module | Toàn bộ use case/biểu đồ của những module khác | Chỉ gửi khi chúng ràng buộc hai module này | Tránh triển khai lan sang cả hệ thống |

**Quan trọng:** ảnh diagram không thay thế được DDL; DDL không thay thế được quy tắc nghiệp vụ. Cần cả hai để code đúng và chạy được.

Nếu chưa có V3/sequence thiết kế thì AI được dựng chúng từ hợp đồng đã xác định trong bước chuẩn bị; không yêu cầu người dùng hoàn tất trước mọi sơ đồ mới bắt đầu được. Những phần đã có thì phải dùng làm nguồn đối chiếu.

### Đánh dấu phiên bản rõ ràng

Mỗi file nên có mã nguồn tài liệu, ví dụ `SYSTEM-DESIGN`, `M1-DESIGN`, `M2-DESIGN`, `DB-FINAL`, `M1-V2`, `M2-V2`, `UI-M1`, `UI-M2`. Ghi rõ file nào là bản cuối và file nào chỉ để tham khảo.

Bản mới nhất đã xác định: tk_md1(2).jpg, tk_md2(1).jpg và test_tk(7).jpg; SQL baseline GaraMan_CSDL(1).sql. Các quyết định mới của người dùng về login/thông tin cá nhân/slot/doanh thu và phương án V2 phải được ưu tiên khi ảnh chưa cập nhật. AI giữ bản gốc và xuất thiết kế/migration cuối theo code.

Ưu tiên gửi `.puml`/bảng text bên cạnh ảnh để tránh đọc nhầm `Date`, `IDate`, hậu tố `6`, tên constructor, chính tả phương thức hoặc kiểu mảng bị cắt.

## 2. Môi trường đã chọn

Windows 10, IntelliJ IDEA, JDK 21, Maven WAR/Wrapper 3.10.0, Tomcat 10.1.x bản vá hiện hành, jakarta.servlet, MySQL qua JDBC. Project GaraMan6, artifact garaman6, root package garaman, schema garaman6, DB mặc định 127.0.0.1:3306, HTTP 8080, context /garaman6, múi giờ Asia/Ho_Chi_Minh.

Không cần người dùng lựa chọn lại các mục “tùy”. AI kiểm tra bản MySQL server bằng SELECT VERSION(), port/quyền thật khi có terminal đúng máy đích; không lấy version Linux/cloud làm version Windows. Mật khẩu DB do chủ máy cấp ở local, không suy ra hoặc commit vào source. Maven Wrapper/README/script PowerShell hỗ trợ chạy không cần tích hợp Tomcat trả phí trong IDE.

Baseline SQL là GaraMan_CSDL(1).sql (MySQL 8.0.16+), thêm migration V2 theo quyết định mới; không dùng bản drawDB export làm schema chuẩn. Chi tiết lệnh kiểm tra, cấu hình và mapping trong GaraMan_Quyet_dinh_trien_khai_v2.md.

## 3. Q01–Q18 đã được giải quyết

| Mã | Quyết định áp dụng |
|---|---|
| Q01 | Login/logout thật tối thiểu, MemberDAO6 + LoginServlet6 + LoginView6 + AuthFilter6; session/role từ DB |
| Q02 | Thông tin cá nhân readonly, server đọc hồ sơ khách từ session/DB; không thêm contact snapshot |
| Q03 | Một slot có một cuộc hẹn đang hoạt động, giữ UNIQUE activeSlotId |
| Q04 | Mặc định demo hằng ngày 08–17h, nghỉ 12–13h; slot 60 phút, 30 ngày, chỉ tương lai, sinh idempotent, cấu hình được |
| Q05 | Edit/back giải phóng hold đúng token; lần xác nhận lại mới giữ chỗ với token/expiry mới |
| Q06 | Lịch PENDING/“Chờ tiếp nhận”, nhân viên NULL; trạng thái slot theo SQL baseline |
| Q07 | Tiền tố + tối thiểu bốn số, sequence DB trong transaction; thêm vehicleCode và hỗ trợ cấp mã có cập nhật thiết kế |
| Q08 | Biển số chuẩn hóa/unique toàn hệ thống, hãng/dòng/biển bắt buộc, năm tùy chọn dùng Integer, km ban đầu 0 |
| Q09 | Khoảng ngày căn cứ Invoice.issueDate, bao gồm đủ hai ngày |
| Q10 | Báo cáo và lịch sử lọc chỉ hóa đơn có Payment.status=PAID |
| Q11 | COUNT dòng Usage, không SUM usedQuantity; lifetime count mọi dòng không lọc ngày/trạng thái |
| Q12 | Dùng saleUnitPrice lịch sử; part nhân usedQuantity; không thuế/giảm giá |
| Q13 | 20 dòng/trang; revenue DESC rồi itemCode ASC/itemType ASC; history issueDate DESC rồi usageId DESC |
| Q14 | Thêm order:RepairOrder6 vào hai Usage, mapping FK repairOrderId đã có, không đổi mảng public API |
| Q15 | getInvoiceCodeByUsage(itemType, usageId), rồi getInvoiceDetail(invoiceCode), hóa đơn đầy đủ |
| Q16 | Bỏ rating khỏi UI, không thêm backend đánh giá |
| Q17 | Java kế thừa; DB dùng Member + Customer/Employee shared PK, role bằng jobTitle, Account đã gộp vào Member |
| Q18 | List không có dữ liệu trả mảng rỗng; object không tìm thấy trả NULL; boolean false cho thất bại nghiệp vụ; lỗi DB ném DataAccessException6, xung đột lưu ném BookingConflictException6 |

Chi tiết đầy đủ, chữ ký mới, nullable/mapping, chuẩn hóa dữ liệu, status, transaction và migration trong GaraMan_Quyet_dinh_trien_khai_v2.md; toàn bộ nội dung này cũng được tích hợp vào prompt V2. Không hỏi lại bảng Q01–Q18. AI chỉ cần làm rõ vấn đề thật sự mới không có lời giải trong các nguồn và kết quả kiểm tra máy đích.

## 4. Luồng module 1 phải giữ

1. Khách hàng đã được xác định trong session chọn Đặt lịch hẹn ở `CustomerHomeView6.jsp`.
2. Request đến `BookingServlet6.doGet`; Servlet lấy khách hàng qua `CustomerDAO6.getCustomerInfo(customerCode)` và xe qua `VehicleDAO6.getVehiclesOfCustomer(customerId)`.
3. Servlet cấp `cus`, `vehicleList` và các dữ liệu nền được thiết kế cho `BookAppointmentView6.jsp`.
4. Khách chọn ngày; Servlet gọi `SlotDAO6.getAvailableSlots(appointmentDate)` và trả các slot hợp lệ của ngày đó.
5. Khách chọn slot, xe, kiểm tra thông tin cá nhân chỉ đọc và nhập yêu cầu; form gửi POST đến `BookingServlet6`.
6. Servlet kiểm tra dữ liệu phía server, đặc biệt xe thuộc khách hiện tại và slot thuộc ngày đã chọn.
7. Servlet gọi `SlotDAO6.holdSlot(slotId)`; giữ chỗ thành công trong 3 phút, nhận slot có `holdCode`, `holdExpiresAt`.
8. **`BookingServlet6` tạo đối tượng `Appointment6` tạm**; chưa INSERT lịch hẹn, chưa cần dùng DAO chỉ để đóng gói.
9. Dữ liệu tạm được giữ theo người dùng/phiên làm việc; Servlet truyền `appointment`, `holdCode`, `holdExpiresAt` và `message` tới `ConfirmView6.jsp`.
10. Trang xác nhận hiển thị dữ liệu và thời hạn giữ chỗ. Khách xác nhận lần cuối bằng POST đến `AppointmentServlet6`.
11. Servlet xác định lại draft/slot/token thuộc phiên hiện tại, gọi `validateHold(slotId, holdCode)` theo thiết kế.
12. Servlet gọi `AppointmentDAO6.createAppointment(appointment, holdCode)`.
13. Bên trong transaction lưu cuối, phải kiểm tra lại và consume hold hợp lệ đồng thời với INSERT; kiểm tra trước ở bước 11 không đủ chống tranh chấp.
14. Lưu mã lịch, thời điểm tạo, trạng thái Chờ tiếp nhận theo mã trạng thái đã chốt; COMMIT toàn bộ hoặc ROLLBACK toàn bộ.
15. Trả đối tượng đã lưu có ID/mã thật; hiển thị thành công trên `ConfirmView6.jsp` theo thiết kế cuối.
16. Không gửi thông báo cho nhân viên: hai bước thông báo đó đã được người dùng bỏ khỏi scenario. Không thêm `BookingSuccessView6.jsp` nếu bản cuối vẫn dùng Confirm để hiển thị thành công.

### Nhánh thêm xe trong module 1

1. Link Thêm xe mới gửi GET đến `VehicleServlet6`.
2. Servlet lấy hãng qua `VehicleBrandDAO6.getVehicleBrands()`, cấp `cus`, `vehicleBrandList` và dữ liệu form cho `AddNewVehicleView6.jsp`.
3. POST lưu xe: kiểm tra đầu vào/biển số/hãng, gắn khách từ session, gọi `VehicleDAO6.saveVehicle(vehicle, customerId)`.
4. Lưu thành công thì `sendRedirect` đến URL `BookingServlet6`; trình duyệt gửi GET mới.
5. Booking Servlet tải lại danh sách xe, giữ lại dữ liệu form trước đó nếu đó là yêu cầu đã chốt; xe mới phải xuất hiện. Không để Vehicle Servlet gọi trực tiếp Java `doGet` của Booking Servlet.

Nhánh này thuộc thiết kế module 1 dù không xuất hiện trong happy-case V2 31 bước; không vì vậy triển khai cả chức năng quản lý xe.

## 5. Luồng module 2 phải giữ

1. Từ `ManagerHomeView6.jsp`, chọn Xem báo cáo; `RevenueServlet6` mở `ReportMenuView6.jsp`.
2. Chọn thống kê doanh thu; Servlet mở `RevenueStatisticsFilterView6.jsp`.
3. Nhận ngày đầu, ngày cuối và loại thống kê; kiểm tra server-side.
4. Gọi `RevenueDAO6.getRevenueStatistics(startDate, endDate, statisticType)`.
5. Trả các dòng `RevenueStats6`, sắp doanh thu giảm dần; tổng cộng và phân trang phải căn cứ cùng một bộ lọc.
6. Servlet cấp dữ liệu tới `RevenueStatisticsListView6.jsp`; không hardcode số liệu mẫu.
7. Link một dòng mang `itemType` và `itemCode` đến `ServicePartServlet6`, kèm/khôi phục khoảng ngày và ngữ cảnh danh sách.
8. Nếu dịch vụ: `ServiceDAO6.getServiceInfo(serviceCode)`, `ServiceUsageDAO6.getTotalUsageCount(serviceCode)`, `getAllServiceUsageOfService(serviceCode, startDate, endDate)`.
9. Nếu phụ tùng: `SparePartDAO6.getPartInfo(partCode)`, `PartUsageDAO6.getTotalUsageCount(partCode)`, `getAllPartUsageOfPart(partCode, startDate, endDate)`.
10. Dùng chung `ServicePartDetailView6.jsp`. Chỉ nạp và hiển thị nhánh tương ứng; không yêu cầu cả `service` và `sparePart` cùng khác NULL.
11. `lifetimeUsageCount` không bị giới hạn bởi bộ lọc ngày; danh sách sử dụng có bị giới hạn bởi bộ lọc ngày. Căn cứ ngày đã chốt là Invoice.issueDate, chỉ Payment.status=PAID cho danh sách trong khoảng.
12. Chọn Xem hóa đơn ở một dòng: gửi loại và ID lần sử dụng; tra `invoiceCode` bằng nguồn đã chốt; chuyển hướng đến `InvoiceServlet6`.
13. `InvoiceDAO6.getInvoiceDetail(invoiceCode)` dựng toàn bộ hóa đơn, không chỉ dòng dịch vụ/phụ tùng vừa chọn.
14. `InvoiceDetailView6.jsp` hiển thị đúng phiếu, khách, xe, dịch vụ/phụ tùng, số lượng, đơn giá lịch sử, thành tiền, tổng và thanh toán.
15. Quay lại giữ đúng đối tượng, ngày lọc, loại thống kê và trang trước đó; không dùng một session key toàn cục dễ bị ghi đè giữa nhiều báo cáo mà không có cách phân biệt ngữ cảnh.
16. V2 mô tả xem dịch vụ rồi xem phụ tùng là hai lượt tương tác minh họa. Code phải cho phép chọn loại nào trước cũng được, không ép người dùng xem dịch vụ trước mới được xem phụ tùng.

## 6. Các bất biến cần kiểm thử

- Mọi mutation đặt lịch/giữ chỗ/lưu xe dùng POST; truy vấn dùng GET. Không sao chép việc xóa/sửa dữ liệu qua GET chỉ vì ví dụ CRUD trong LAB có route như vậy.
- Token hold được sinh phía server; slot và token phải khớp; thời hạn dùng đồng hồ server/DB thống nhất. Đồng hồ đếm ngược ở trình duyệt chỉ để hiển thị.
- Không giữ transaction/khóa DB mở suốt 3 phút chờ khách xác nhận.
- Hai khách tranh một slot chỉ có một người giữ được nếu slot có sức chứa 1. Hai request xác nhận đồng thời không tạo hai lịch.
- Kiểm tra và consume hold cùng INSERT là một transaction. Không consume ở connection này rồi INSERT ở connection khác.
- Double-click/reload/retry không tạo lịch trùng; cơ chế chống trùng phải có bằng chứng ở code và kiểm thử.
- Tiền phụ tùng = `usedQuantity * saleUnitPrice`; tiền dịch vụ theo giá lịch sử của lần sử dụng, không lấy giá hiện tại trong danh mục để sửa doanh thu cũ.
- Không dùng `totalProfit` thay `totalRevenue`. Không cộng cùng một dòng nhiều lần vì JOIN hai danh sách con hoặc nhiều bản ghi thanh toán.
- Hai loại có cùng chuỗi mã vẫn phân biệt được bằng cặp `(itemType, itemCode)`; không thêm `id` cho dòng `RevenueStats6`.
- Ngày đầu/cuối được hiểu theo ngày nghiệp vụ; điều kiện timestamp thường dùng khoảng nửa mở từ đầu ngày đầu đến đầu ngày sau ngày cuối để không bỏ sót giao dịch cuối ngày.
- Phân trang dùng thứ tự ổn định; tổng doanh thu là tổng toàn bộ kết quả lọc, không phải tổng trang hiện tại.
- Hóa đơn được mở theo một lần sử dụng nhưng hiển thị toàn bộ hóa đơn; dùng các FK thực tế, không đoán khách/xe từ dữ liệu của phiên hiện tại.
- Entity Java không mặc định tương ứng 1-1 với bảng vật lý, đặc biệt kế thừa và kết quả tổng hợp. DDL cuối quyết định mapping.
- Lỗi DB không được trả thành thành công giả, boolean true hay mảng rỗng như không có dữ liệu.

## 7. Hồ sơ hợp đồng AI phải xuất trước và cập nhật trong khi code

1. `SOURCE_MANIFEST.md`: mã tài liệu, tên file, phiên bản, phạm vi, nguồn nào có quyền quyết định loại thông tin nào.
2. `CONTRACTS.md`: toàn bộ lớp, field, type có import đầy đủ khi dễ nhầm, visibility, superclass; toàn bộ phương thức, constructor, thứ tự/tên/kiểu tham số, return type, throws và hành vi không tìm thấy/thất bại.
3. `SCHEMA_MAPPING.md`: thuộc tính Java ↔ bảng/cột/FK/join ↔ nullability/UNIQUE; mapping kế thừa khi bỏ/gộp bảng; quy tắc tiền và thời gian.
4. `HTTP_VIEW_CONTRACT.md`: method, URL, action, input name/type, request attributes, session keys, đối tượng được phép NULL, forward/redirect, trang đích và dữ liệu cần phục hồi.
5. `BUSINESS_RULES.md`: các câu trả lời Q01–Q18, trạng thái, công thức và ví dụ tính tay.
6. `TRACEABILITY.md`: bước scenario/UI column → Servlet handler → DAO method → SQL/source field → phép kiểm thử.
7. `DESIGN_CHANGES.md`: mọi khác biệt đã thực hiện so với baseline, bản sửa thiết kế theo code và kiểm thử chứng minh.

Nếu ảnh khó đọc thì đánh dấu vị trí chưa rõ và hỏi/bổ sung nguồn text; không suy đoán một chữ ký rồi coi như chuẩn 100%.

## 8. Code mốc và sửa ngược thiết kế

Mục tiêu là code chạy được và thiết kế cuối mô tả đúng code, không giữ thiết kế sai để tạo code giả.

- Khả thi: giữ nguyên toàn bộ tên/kiểu/chữ ký đã chốt, kể cả tên như `createAt`, `cus`, `ss`, kiểu mảng và tên lớp DAO6.
- Cần bổ sung kỹ thuật: getter/setter, constructor hỗ trợ, quản lý connection/transaction, enum hay kiểu dữ liệu phụ chỉ thêm khi cần và phân loại rõ. Không tự thêm một tầng Service/Repository thay kiến trúc.
- Nếu bắt buộc đổi: ghi nguồn gốc vấn đề, chứng minh bản cũ thiếu/không chạy đúng, nêu thay đổi nhỏ nhất, triển khai, kiểm thử rồi sửa `.puml`/bảng thiết kế/DDL/scenario thiết kế liên quan. Đánh dấu tài liệu ảnh cũ đã bị thay thế.
- Không hỏi lại quyền cho từng sửa kỹ thuật trong phạm vi này; người dùng đã cho phép lấy code chạy đúng làm mốc. Chỉ hỏi khi có nhiều cách hiểu nghiệp vụ chưa đủ bằng chứng để chọn.
- Không hợp thức hóa việc đổi tùy ý Array thành List, đổi tên method hoặc bỏ bước nghiệp vụ bằng câu “code là mốc”.
- Trước khi sửa DB đang có dữ liệu, dùng migration và dữ liệu thử; không tự DROP/ghi đè dữ liệu thật. Công việc triển khai hai module không mặc nhiên cho phép phá CSDL hiện có.

## 9. Cách dùng hai file bàn giao

1. Chuẩn bị các file bắt buộc tại mục 1; chỉ rõ bản cuối.
2. Dùng các quyết định V2 ở mục 2/3 và file quyết định đầy đủ; chỉ bổ sung thông tin kết nối local và kết quả kiểm tra runtime thật. Không cần trả lời lại Q01–Q18.
3. Gửi cả hồ sơ này và các tài liệu thiết kế cho AI.
4. Dán toàn bộ nội dung `GaraMan_Prompt_Code_2_Module.md` làm yêu cầu triển khai.
5. Yêu cầu AI tạo project thực, migration/seed, hướng dẫn chạy và bằng chứng kiểm thử; không chỉ xuất các đoạn code rời hoặc thông báo “đã hoàn thành”.

## 10. Tài liệu LAB đã tham khảo

`LAB1-JavaProgrammingBasic.pdf`, 28 trang: trang 7–8 minh họa JavaBean; trang 8–11 minh họa DAO/JDBC; trang 11–13 minh họa Servlet đọc request, gọi DAO, dựng model, đặt request attribute, forward/redirect; trang 13–17 minh họa JSP/JSTL và form; trang 22–27 trình bày DAO pattern với dữ liệu trong bộ nhớ để minh họa.

Áp dụng cấu trúc và cách phân trách nhiệm; không dùng danh sách in-memory của ví dụ thay cho CSDL thật, không sao chép version dependency/mật khẩu hardcode/mã xử lý lỗi của ví dụ một cách máy móc. Không bắt buộc thêm cặp interface/implementation DAO nếu thiết kế đã chọn các DAO cụ thể kế thừa `DAO6`.
