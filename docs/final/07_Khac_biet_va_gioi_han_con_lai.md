# Khác biệt và giới hạn còn lại

- Cách A Servlet đóng gói đã thực hiện; không còn vấn đề chờ chấp thuận JSP/Servlet. Không có helper nghiệp vụ thay tên. Các phương thức private tại DAO/Servlet là nội bộ của đúng lớp chịu trách nhiệm; config ở DAO6 và password ở MemberDAO6.
- Input không có SQL gốc GaraMan_CSDL(1).sql và một số biểu đồ state/sequence độc lập. Có thể đối chiếu code/ảnh mới/schema live nhưng không khẳng định 100% toàn hệ thống.
- TIMESTAMP(6) giữ theo DB design, zone Việt Nam; miền thời gian có giới hạn quanh2038. Test tương lai dùng2030/2031. Không âm thầm đổi DATETIME.
- Km lịch sử0 được giữ vì không biết 0 thật hay default cũ; xe mới thiếu Km là null. Năm cũng nullable.
- D055: sau hết session, returnRoute whitelist chỉ giữ path /booking,/vehicle,/revenue, chưa giữ đầy đủ query của màn hình detail/invoice. Khi session còn, Back giữ kỳ/loại/item/page/usagePage.
- DAO nạp toàn tập rồi Servlet slice20; có các query graph con N+1. Chưa load test hoặc tối ưu SQL pagination/batch ngoài scope.
- Đồng bộ mỗi context/tab trên một JVM và ràng buộc DB chống tranh slot; chưa kiểm cluster/distributed session/crash giữa commit và HTTP hoặc idempotency bền qua restart. Session restart mất draft; hold DB tự hết hạn.
- Cấu hình runtime Việt Nam/Windows đã kiểm, không tuyên bố các môi trường Linux/WSL/cloud hay mọi timezone đã chạy.
- 47 lớp là inventory Java hiện tại, không buộc toàn hệ thống/sơ đồ tổng chỉ có47 lớp. Supplier/receipt/warehouse chỉ DDL/fixture cần cho stock, không xây module ngoài scope.
- Không đổi bố cục/control/JSP hiển thị trong lượt này. UI date HTTP là String ISO, DAO dùng Date hoặc LocalDate theo contract; các thuộc tính UI trên ảnh không phải instance field Java của JSP.
- Trong khi sửa, main phát sinh thêm LH0006 lúc17:24:08. Đối chiếu cho thấy nguyên5 lịch trước checkpoint còn nguyên, chỉ thêm lịch mới và slot liên quan chuyển BOOKED; 16 bảng khác không đổi. Không xóa/hoàn nguyên hoạt động đồng thời này. Chi tiết evidence/main/concurrent-activity.json; 11 kiểm tra sau deploy chỉ đọc không làm đổi bảng nghiệp vụ.

Xem kết quả thực tế và bằng chứng tại [06](06_Ket_qua_kiem_thu.md). Tài liệu review/scenario lịch sử giữ nguyên; docs/final được ghi đè theo yêu cầu, bản trước được lưu trong checkpoint.
