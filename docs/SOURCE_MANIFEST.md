# Nguồn và baseline triển khai GaraMan6

Đã đọc toàn bộ input ngày 12/11/2026 trên máy Windows của người dùng.

| Mã | File thực tế | Vai trò / ưu tiên |
|---|---|---|
| REQUIREMENTS-V2 | input/GaraMan_Prompt_Code_2_Module.md | Phạm vi, hợp đồng API, kiến trúc Entity → DAO → Servlet → JSP |
| DECISIONS-V2 | input/GaraMan_Quyet_dinh_trien_khai_v2.md | Q01–Q18, thay thế các điểm cũ trong ảnh |
| DOSSIER-V2 | input/GaraMan_Ho_so_dau_vao_va_quy_tac.md | Truy vết, kiểm thử, quy trình bàn giao |
| M1-DESIGN | input/tk_md1.jpg | Đã đọc trực quan; entity/DAO/Servlet/JSP module đặt lịch |
| M2-DESIGN | input/tk_md2.jpg | Đã đọc trực quan; module doanh thu, Usage.order đã xuất hiện |
| SYSTEM-DESIGN | input/test_tk.jpg | Đã đọc trực quan; kiểu, kế thừa, quan hệ. Usage.attribute được thay bằng order theo V2 |
| DB-OLD | input/grm_db.jpg | Ảnh drawDB cũ: thanh toán gộp Invoice, FK tên dài; không dùng làm baseline cuối |
| UI | input/giao diện mẫu.pdf (6 trang) | Đã render và xem đủ: bố cục xanh, các bảng, nút, xác nhận, chi tiết; bỏ rating, thêm SaleStaff |
| SCENARIO-M1/M2 | Hai scenario người dùng dán trong hội thoại, 31 / 42 bước | Trình tự nghiệp vụ pha phân tích; DAO và Servlet đảm nhiệm truy xuất/điều khiển ở pha thiết kế |

## Nguồn được nhắc nhưng không được cung cấp

Không tìm thấy `GaraMan_CSDL(1).sql`, các tên ảnh có hậu tố `(7)/(2)/(1)`, LAB PDF hoặc nguồn PlantUML trong project/thư mục BTL. Không tuyên bố đã đọc các file đó. `sql/V1__reconstructed_baseline.sql` là bản tái dựng từ hợp đồng V2 và ảnh, **không phải SQL V1 nguyên bản**. Không chạy lên database cũ. `sql/V2__booking_changes.sql` ghi riêng thay đổi đã chốt; `sql/schema.sql` là schema cuối cho cài mới. Khi có V1 nguyên bản, cần đối chiếu migration trên bản sao trước khi áp dụng cho dữ liệu thật.

## Môi trường thực tế ban đầu

Project: `C:\Users\admin\Documents\tai lieu nam 4\PTTK\BTL\garaman`. Windows 10 Pro 10.0.19045 x64, Temurin JDK/Javac 21.0.10+7. Không phải cloud/WSL. Source ban đầu trống, pom Java 21, chưa cấu hình WAR. Không có .git và không tìm thấy AGENTS.md áp dụng. MySQL94 đã cài nhưng service dừng; binary ở `C:\Program Files\MySQL\MySQL Server 9.4\bin`. Maven không ở PATH, có bản cache; Tomcat chưa tìm thấy. Sẽ kiểm chứng phiên bản server bằng JDBC/SQL sau khi chạy instance project riêng.
