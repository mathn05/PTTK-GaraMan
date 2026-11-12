# Thay đổi thiết kế được áp dụng

| ID | Nguồn cũ / vấn đề | Trước → Sau | Code / thiết kế / kiểm chứng |
|---|---|---|---|
| D01 | Scenario phân tích gọi entity từ JSP | Entity truy cập DB → DAO truy cập, Servlet điều khiển, JSP chỉ render | mọi DAO/Servlet/JSP; scenarios/sequence cuối |
| D02 | Q01 | thiếu auth → LoginServlet6/MemberDAO6/AuthFilter6/PasswordUtil6 | test login/role/logout thật |
| D03 | UI contact editable | nhập contact → readonly và nạp lại DB | booking tamper test |
| D04 | Q05 | back chưa rõ → POST release, draft UUID mỗi tab | hold/edit/multitab test |
| D05 | Vehicle thiếu mã/năm primitive | thêm vehicleCode, int→Integer; sequence DB | V2 migration, vehicle concurrent tests |
| D06 | SYSTEM-DESIGN Usage.attribute | attribute→order:RepairOrder6 theo M2/V2 | usage mapper / history/invoice tests |
| D07 | UI chi tiết dịch vụ có đánh giá | bỏ ô đánh giá | JSP detail, browser assertion |
| D08 | UI hóa đơn thiếu SaleStaff | hiển thị order.ss và payment.ss | InvoiceDAO6, invoice JSP/browser |
| D09 | DAO6 con private/ownership thiếu | protected con + constructor Connection + AutoCloseable | shared transaction không đóng connection mượn |
| D10 | Slot chưa unique startTime | unique + seed cấu hình, chỉ setup ghi | V2, concurrent slot tests |
| D11 | Thiếu SQL gốc; drawDB invoice gộp payment | tái dựng V1 từ mô tả V2, giữ riêng Payment, view tổng | không nhận là V1 gốc; chỉ instance mới |
| D12 | PRG sau confirm, nhiều tab | GET /booking?action=confirm đọc kết quả theo context | bổ sung handler doGet sẵn có; retry/UI |
| D13 | Windows MySQL thiếu bảng timezone tên vùng, lỗi JDBC thực tế | không ép tên vùng vào MySQL khi mở connection; JDBC connectionTimeZone=Asia/Ho_Chi_Minh, sau đó SET time_zone='+07:00' | DAO6/local config, test @@session.time_zone |
| D14 | Maven 3.10 WAR reproducible giữ timestamp 1980; Tomcat tái dùng JSP bytecode cũ sau deploy | khai báo UTF-8 mọi JSP/JSPF + web.xml; script dừng JVM rồi xóa đúng cache JSP riêng của garaman6, kiểm tra absolute path trước xóa | run_tomcat.py, browser kiểm tra tiếng Việt và JSP compile |
| D15 | Windows PowerShell 5.1 đọc script UTF-8 không BOM như ANSI và lỗi parser tiếng Việt | lưu các .ps1 bằng UTF-8 BOM, giữ UTF-8 chuẩn cho Java/JSP/Python | đã phát hiện bằng chạy Stop/Start thực, không suy ra từ file tồn tại |

Các getter/setter/constructor không tham số JavaBean là boilerplate, không thay nghiệp vụ. Các helper public kỹ thuật sẽ được liệt kê từ source trong CONTRACTS. Thiết kế cuối docs/design là nguồn text có thể sửa; ảnh input giữ nguyên làm lịch sử.
