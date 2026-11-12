# GaraMan — bản hiệu lực cách A: Servlet đóng gói

Ghi đè tài liệu cùng tên theo quyết định mới. Bản trước sửa trong `.local/checkpoints/servlet-architecture-20261112_172201/source-before.zip`. Input/ảnh gốc và review/scenario lịch sử giữ nguyên.

| File | Nội dung |
|---|---|
| [00 nguồn hiệu lực](00_Nguon_chuan_hieu_luc.md) | Quyết định mới nhất và checkpoint. |
| [01 hợp đồng47 lớp](01_Hop_dong_lop.md) | Field/constructor/method/params/return/visibility/throws từ bytecode có -parameters. |
| [02 bảng sửa sơ đồ/chuyển trách nhiệm](02_Bang_thay_doi_thiet_ke.md) | Từng ô Integer/BigDecimal, method, relation;9 lớp +2 JSP đã xóa và nơi tiếp nhận. |
| [03 DB/ràng buộc](03_Anh_xa_DB_va_rang_buoc.md) | Schema giữ nguyên; tất cả column/type/FK/index/CHECK/generated/null mapping. |
| [UML module1](design/module1-v3.puml) · [SVG](design/module1-v3.svg) | Không helper/JSP trung gian; context bằng Java Map. |
| [UML module2](design/module2-v3.puml) · [SVG](design/module2-v3.svg) | DAO hydrate đúng thực thể, tiền/Integer và role staff. |
| [V3 module1](Scenario_V3_Module1.md) · [truy vết](Scenario_V3_Ky_thuat_Module1.md) | 29 bước happy case; Servlet tạo draft, final handler dùng lại. |
| [V3 module2](Scenario_V3_Module2.md) · [truy vết](Scenario_V3_Ky_thuat_Module2.md) | 34 bước liên tục, đúng hai lượt SERVICE rồi PART, đoạn trước/sau chung. |
| [05 nhánh hỗ trợ](05_Nhanh_ho_tro_Module1.md) | Servlet tạo Vehicle6, edit/logout/listener và graph optional. |
| [06 kết quả thực](06_Ket_qua_kiem_thu.md) | 27 JUnit +49 browser +38 regression +4 guard +11 main chỉ đọc. |
| [07 giới hạn](07_Khac_biet_va_gioi_han_con_lai.md) | Nguồn thiếu, D055, miền thời gian, hiệu năng và hoạt động đồng thời main. |
| [08 file thay đổi](08_File_thay_doi.md) | So với checkpoint trước lượt sửa kiến trúc. |
| [Evidence](evidence/) | Log/screenshots/hashes/runtime/proof/trace/QA tài liệu. |

Ứng dụng main: http://localhost:8080/garaman6/login. Schema/dữ liệu gốc không bị reset; lịch phát sinh đồng thời được giữ. Config/backup chứa credential nằm ngoài docs công khai. Inventory47 lớp chỉ mô tả phạm vi code hiện có, không ép toàn hệ thống/sơ đồ tổng thành47 lớp.
