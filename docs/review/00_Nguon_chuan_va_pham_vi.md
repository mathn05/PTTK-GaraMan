# Nguồn chuẩn, phạm vi và quy tắc review

Ngày review: 12/11/2026, múi giờ Asia/Ho_Chi_Minh. Phạm vi là **review và viết tài liệu**, theo GaraMan_Prompt_Review_Code_va_Scenario_V3.md được người dùng chỉ định sau lượt triển khai. Không sửa source, SQL, cấu hình, script, test hay tài liệu cũ; không áp dụng đề xuất trong báo cáo. Các file mới nằm trong docs/review và docs/scenario; build tạo lại target, runtime kiểm thử có thư mục tạm riêng.

## 1. Định danh hiện trạng

- Workspace native Windows: C:\Users\admin\Documents\tai lieu nam 4\PTTK\BTL\garaman. PowerShell 5.1; không phải cloud/container hay WSL.
- Không có .git, không có commit/dirty status để công bố. Thay thế bằng [snapshot trước review](code_snapshot_before.json), 147 file gốc, thời điểm 2026-11-12 09:57:59 giờ máy.
- SHA-256 định danh tập snapshot: **ab485f1be8360b8dc1e1b1c9d9f0151c052f145c0ca8a0c4dc2adff642fe0ffb**. Cách tính: sắp xếp path theo thứ tự từ điển, ghép mỗi path + ký tự NUL + sha256 + LF, mã hóa UTF-8 rồi SHA-256. Các file báo cáo mới không thuộc tập gốc.
- Snapshot gồm src, scripts (không gồm __pycache__), sql, .mvn, input, tài liệu docs cũ và pom.xml/mvnw/mvnw.cmd/.gitignore/README.md. .local chứa secret không được đưa vào báo cáo; .runtime/target là runtime/build, không được xem là source chuẩn.
- [Đối chiếu hash sau review](evidence/snapshot-verification.json) là bằng chứng bảo toàn các file gốc. V3 chỉ gắn với snapshot này, không phải lời hứa cho phiên bản code thay đổi sau đó.

## 2. Danh mục nguồn và thứ tự ưu tiên

| Nguồn đã đọc | Phiên bản/vị trí | Vai trò và giới hạn |
|---|---|---|
| input/GaraMan_Prompt_Review_Code_va_Scenario_V3.md | Toàn bộ §1–10 | Yêu cầu lượt này: giữ dấu vết, inventory, khác biệt, V3 theo code, kiểm chứng, sáu đầu ra. Các câu minh họa không phải method có sẵn. |
| input/GaraMan_Quyet_dinh_trien_khai_v2.md | 281 dòng; Q01–Q18, §1–16 | Chuẩn nghiệp vụ đã chốt sau ảnh; ghi riêng những quyết định thay thiết kế cũ. Có mâu thuẫn Km với checklist review mới, chưa tự chọn một nguồn để sửa. |
| input/GaraMan_Prompt_Code_2_Module.md | Toàn bộ prompt triển khai | Ràng buộc package, JSP/Servlet/DAO/entity, mảng, tên lớp/chữ ký và phạm vi hai module. Lượt review mới không còn quyền tự sửa theo prompt triển khai cũ. |
| input/GaraMan_Ho_so_dau_vao_va_quy_tac.md | Toàn bộ hồ sơ | Nguồn đầu vào và các câu hỏi/quy tắc trước khi chốt v2; phần bị Q01–Q18 thay thế được đánh dấu theo quyết định mới. |
| Scenario V2 dán trực tiếp trong hội thoại | Module 1: 31 bước; Module 2: 42 bước | Nguồn mô tả cũ, có nhiều câu entity tự gọi persistence và JSP gọi JSP. Không phải bằng chứng lời gọi thật của source. |
| input/tk_md1.jpg | Ảnh đã xem: module 1 | Lớp, thuộc tính UI/ẩn, DAO và quan hệ. Kiểu trả getVehicleBrands ở mép ảnh bị cắt, không đọc đoán. |
| input/tk_md2.jpg | Ảnh đã xem: module 2 | Chữ ký, quan hệ, thuộc tính view; statisticTypeIn, Date và totalRevenue double được đối chiếu với tên/kiểu thực tế. |
| input/test_tk.jpg | Ảnh đã xem: entity toàn hệ thống | Có Usage.attribute trong ảnh, trong khi module 2/Q14 dùng order; không coi hai bản ảnh tự nhất quán. |
| input/grm_db.jpg | Ảnh drawDB đã xem | Tham khảo DB cũ, tên FK/Payment khác schema hiện tại. Không dùng thay SQL baseline gốc chưa được cung cấp. |
| input/giao diện mẫu.pdf | Đã xem 6/6 trang | Bố cục/nhãn; trang 5 có đánh giá đã bị Q16 bỏ, trang 6 có ghi chú bổ sung nhân viên bán hàng đã được yêu cầu trực tiếp. Không yêu cầu giữ các lỗi UI cũ. |
| BG HP TTTN 2 CNPM 2020 final (1).pdf | Ngoài project: C:\Users\admin\Documents\tai lieu nam 4\PTTK\ ; đọc trang in 96–113 | Tìm thấy trên chính Windows. PDF 235 trang. Trang in 96 là trang viewer 97 (index 96); đã xác định bằng tiêu đề/nội dung. Bản trích và ảnh kiểm tra trong evidence/pdf. |
| LAB1-JavaProgrammingBasic.pdf | Cùng thư mục PTTK ngoài project; đọc 28/28 trang | Ví dụ JavaBean/DAO/JDBC/Servlet/JSP và DAO pattern. Dùng để phân biệt quy ước diễn đạt với cấu trúc chương trình. |
| docs/CONTRACTS.md, CONTRACTS_GENERATED.md, HTTP_VIEW_CONTRACT.md, BUSINESS_RULES.md, SCHEMA_MAPPING.md, SOURCE_MANIFEST.md, TRACEABILITY.md, DESIGN_CHANGES.md, TEST_EVIDENCE.md, schema-metadata.json | Tài liệu đã sinh của project | Nguồn mô tả sau triển khai, có giá trị để tìm chỗ cần đồng bộ; không phải thiết kế gốc độc lập. Không lấy báo cáo pass cũ làm kết quả kiểm thử lượt này. |
| docs/design/classes-final.puml, SCENARIO_V3.md | Tài liệu sau code | UML sinh có giới hạn public method; scenario rút gọn. Được giữ nguyên để không mất dấu vết. |
| src/main/java | 55 lớp: entity 21, dao 16, servlet 7, util 7, filter 2, exception 2 | Đã lần theo handler → helper → DAO → mapper → entity/SQL, kể cả package-private method và constructor Connection. Chuẩn V3 hiện trạng. |
| src/main/webapp | JSP/JSPF, web.xml, META-INF/context.xml, index.jsp, assets JS/CSS/SVG | Link/form/control, GET/POST/fetch HTML, filter mapping, forward/redirect/include, escape và format dữ liệu. |
| pom.xml, .mvn/wrapper/maven-wrapper.properties, wrapper, scripts và README | Build, setup/run/test, seed/export | Kiểm tra phiên bản, cấu hình runtime, rủi ro script test mặc định ghi schema demo. Không chạy script setup/reset/stop main trong lượt review. |
| sql/V1__reconstructed_baseline.sql, V2__booking_changes.sql, schema.sql, preflight-v2.sql, seed.sql, seed-slots.sql, seed-expected.json | Đã đọc DDL/migration/fixture | V1 tự ghi là reconstructed. Chỉ đối chiếu được thiết kế cung cấp và schema hiện tại, chưa chứng minh được migration từ SQL gốc bị thiếu. |
| src/test/java/garaman/DatabaseIntegrationTest.java, PasswordAndContractTest.java | 19 + 2 test | Đã chạy lại trên garaman6_it; không thay nội dung test. |
| MySQL đang chạy | SHOW CREATE TABLE/metadata 23 object | [schema-live.json](evidence/schema-live.json): 21 base table và 2 view, MySQL 9.4.0, port 3306, session +07:00, lower_case_table_names=1. Đây là xác nhận DB thật, không chỉ đọc DDL. |

SHA-256 PDF bài giảng: 416f0401a70297300419d3abe6a72c4d3d816635e4f58c4b6cc5cd52a865d389. SHA-256 LAB: b6ae8af49be1735befa2ec9a3f2f744808c494e0790d708f0d2fc9a7ce53419a. Không dựa vào mtime để chọn chúng làm bản chuẩn.

**Không có** docs/input/. Không có SQL GaraMan_CSDL(1).sql mà quyết định v2 chọn làm baseline. Không có các file ảnh tên test_tk(7).jpg/tk_md1(2).jpg/tk_md2(1).jpg để chứng minh ảnh đơn giản trong input là đúng bản cuối. Không tìm thấy độc lập biểu đồ phân tích, trạng thái, activity, communication/sequence gốc hoặc file mẫu V3 đầy đủ; chỉ có mẫu cách diễn đạt trong prompt và scenario rút gọn sau code. Các giới hạn này không ngăn review source hiện có.

## 3. Quy tắc thực sự rút ra từ tài liệu

1. Bài giảng trang in 96–102: field tường minh/ẩn của giao diện được ánh xạ theo dữ liệu/control; DAO phải có tham số vào/ra và quan hệ entity liên quan. Không yêu cầu JSP có Java instance field mang tên một nút.
2. Trang in 96–97 đề cập tổ chức DAO dùng chung cơ chế kết nối; không có căn cứ buộc dùng một Connection static dùng chung mọi request. Project dùng một connection sở hữu trên mỗi DAO và constructor mượn khi cần transaction chung.
3. Trang in 103 §4.3.2: hành động phải có phương thức thiết kế, thời gian activation hợp lý; V3 hỗ trợ vẽ sequence. Sách có lược bước con cho gọn. Lượt này khai triển mapper/quan hệ vì prompt yêu cầu truy vết, không thêm phương thức không tồn tại.
4. Trang in 104–106: truy vấn → tạo/gán entity → trả kết quả → render. Thực thể không tự biết cách truy vấn chỉ vì sách nói “nhận thông tin”.
5. Trang in 110, ví dụ nhập điểm: giao diện xử lý gọi entity để gán, giữ đối tượng rồi gọi DAO lưu. Đây là mẫu để **kiểm tra vị trí**; hiện trạng hai lệnh lưu xe/lịch của project nằm trong Servlet. Setter void không trả entity; constructor cho tham chiếu và bên tạo tiếp tục giữ nó.
6. Trang in 110–113: drill-down báo cáo qua DAO/entity và màn hình chi tiết; bước trả phải về đúng bên gọi. Trang in 113 chia model/dao/view; không lấy ví dụ JSP→DAO để xóa tầng Servlet hiện có.
7. LAB trang 7–8: JavaBean có constructor/get/set. Trang 8–11: DAO dùng JDBC, tự tạo collection và đóng gói mỗi dòng. Trang 11–13: Servlet đọc request; insertUser ở trang 13 đọc params → new User → DAO.insert → redirect. Đây là cấu trúc ví dụ cụ thể, không chứng minh project trước khi đọc source. LAB có ví dụ doPost gọi doGet, còn project này không có self-call đó.
8. LAB trang 14–17: JSP/EL/JSTL/form render/gửi tham số. HTML/JS không chuyển đối tượng Java sống qua redirect. Trang 22–27 trình bày DAO pattern, không bắt buộc thêm interface/Service/in-memory repository vào project. Các ví dụ hardcode credential/nuốt exception/error JSP trong LAB không phải chuẩn an toàn để sao chép.
9. V3 chỉ happy case, số bước theo các invocation thật; exception/edit/add vehicle được review riêng. Các nhóm nhiều câu cùng invocation được ghi mã để không vẽ lời gọi trùng. SQL UPDATE dùng scalar không cần entity mới.
10. Code là chuẩn diễn tả hiện trạng; yêu cầu cuối là chuẩn đánh giá nghiệp vụ. Quyết định Q01–Q18 thay các ảnh/V2 cũ ở phạm vi ghi rõ. Điểm Km là xung đột nguồn cần chốt, không tự suy diễn “NULL = 0”.

## 4. Mức kiểm chứng và giới hạn

Đã chạy build lại, 21 JUnit test không skip trên MySQL thật, hai module qua native Windows Chrome/HTTP trên Tomcat riêng port 8082 dùng garaman6_it; 49 kiểm tra sẵn có pass và bốn probe bổ sung tái hiện lỗi. Đã cleanup dữ liệu test; fingerprint các base table DB demo trước/sau không đổi. Không reset/drop/truncate, không đổi mật khẩu, không dừng MySQL/Tomcat demo. Chi tiết và lệnh ở [03](03_Kiem_chung_va_de_xuat_cap_nhat.md).

Chưa kiểm chứng tải lớn, tất cả tổ hợp cấu hình, deploy Linux/WSL/cloud, crash recovery/cluster/session replication hay migration từ V1 gốc thiếu. Không có instrumentation trace từng setter khi chạy; bảng truy vết là bằng chứng source kết hợp kết quả đường đi runtime. Không tuyên bố khớp tuyệt đối hay đã kiểm tra các biểu đồ chưa có.

