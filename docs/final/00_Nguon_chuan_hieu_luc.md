# Nguồn hiệu lực — cách A: Servlet đóng gói

Quyết định trực tiếp mới nhất của người dùng ưu tiên cao nhất: JSP gửi scalar → Servlet validate/đóng gói → DAO transaction → Servlet forward/redirect JSP. Quyết định này thay thế hoàn toàn yêu cầu JSP đóng gói trước đây. Không JSP gọi DAO/SQL hay tạo/gán entity nghiệp vụ; không live Java object qua form/URL.

Đã đọc bản thiết kế mới input/tk_md1.jpg, tk_md2.jpg, test_tk.jpg (mtime 12/11/2026 17:18–17:19) và ảnh DB input/grm_db.jpg, đối chiếu code. Không ghi đè các ảnh. Các prompt/Quyết định v2 cũ chỉ còn hiệu lực ở phần không mâu thuẫn: hold 3 phút/sức chứa 1, nullable Km/năm, Payment folded, generated unique, tiền chính xác, auth/CSRF/ownership/context, nghiệp vụ báo cáo.

Quy ước mới: integer → java.lang.Integer; decimal → java.math.BigDecimal; Date → java.util.Date; getAvailableSlots nhận java.time.LocalDate. Tất cả ID entity chưa lưu là null; count thiếu dữ liệu trả Integer 0. boolean/long giữ đúng mục đích.

Thiết kế bài giảng/LAB đã đọc ở lượt trước tiếp tục dùng cho cách diễn đạt giao diện → handler → DAO/entity → trả → hiển thị. Bản V3 chính nhóm đóng gói thuộc tính con/lặp, bảng kỹ thuật riêng có source anchors; không dùng ví dụ JSP cũ để phủ định quyết định mới.

Checkpoint trước sửa: `.local/checkpoints/servlet-architecture-20261112_172201/source-before.zip` và manifest.json (336 file); database-before.json chứa fingerprints, không đưa secret vào docs. Không có Git repository. Bản sửa này không chạy DDL/migration/seed trên main. Source/SQL/tools/tài liệu cũ trong checkpoint có thể dùng đối chiếu; input và docs/review, docs/scenario lịch sử giữ nguyên so với checkpoint hiện tại.

Native Windows PowerShell, JDK Temurin21, Maven Wrapper3.10.0, MySQL9.4.0, Tomcat10.1.61; không thay bằng môi trường cloud/WSL. Config private giữ nguyên, không hardcode mật khẩu.

SQL gốc GaraMan_CSDL(1).sql và một số sơ đồ state/sequence độc lập chưa được cung cấp. Schema V3 hiện hành đã được kiểm trên DB thực; không tuyên bố khớp toàn bộ hệ thống/nguồn chưa có. Các tài liệu final hiện tại thay bản cùng tên trước đây; checkpoint giữ lịch sử.

SHA-256 ảnh đã đọc:

- grm_db.jpg: `719d86828ed07fb6f81510bf6883c54f35a84944d2c2d1e0027fa91646f51aea`.
- test_tk.jpg: `308e695d31793afd66f7f17b7c4f85d05b95895407d15c2d4286c99c8323611d`.
- tk_md1.jpg: `53e15cc6bafbc0d3d1b7924422184c81badbc9c1e108fa44e39b0e09d4f1e182`.
- tk_md2.jpg: `d0441121abd9dae8bdd5ab94214b1d755e68c3430244e8b6fdc962ee4038e25c`.
