# Quy tắc đã chốt

Áp dụng đầy đủ Q01–Q18 tại input/GaraMan_Quyet_dinh_trien_khai_v2.md (nguồn chuẩn kèm project).

1. Đăng nhập hash PBKDF2 600000, account ACTIVE, role DB, đổi session ID; logout POST.
2. Hồ sơ cá nhân chỉ đọc; cus đọc lại DB, không snapshot/contact mutation.
3. Một slot một lịch hoạt động; UNIQUE activeSlotId.
4. Giờ demo 08–17, nghỉ 12–13, 60 phút, 30 ngày từ hiện tại; setup idempotent, chỉ tương lai có thể đặt. Cấu hình trong local properties.
5. Edit/back POST release đúng token; giữ form, lần sau token mới; refresh không gia hạn.
6. Lịch PENDING/Chờ tiếp nhận, ss NULL; slot AVAILABLE/HELD/BOOKED/CLOSED.
7. X/LH + tối thiểu 4 số, sequence khóa DB cùng transaction; không MAX+1 runtime.
8. Biển trim uppercase bỏ khoảng trắng/chấm/gạch; unique toàn hệ thống, hãng/dòng/biển bắt buộc, năm Integer nullable 1886..năm hiện tại+1, km=0.
9. Ngày báo cáo Invoice.issueDate, đủ hai đầu [start,end+1).
10. Chỉ Payment.status=PAID cho doanh thu/history.
11. usageCount=COUNT dòng, lifetime mọi dòng mọi trạng thái/ngày.
12. Giá lịch sử; dịch vụ SUM saleUnitPrice, phụ tùng SUM usedQuantity*saleUnitPrice; không thuế/giảm giá. SQL DECIMAL, tổng BigDecimal.
13. 20 dòng/trang, revenue DESC/code ASC/type ASC; history issueDate DESC/id DESC; tổng toàn bộ kết quả; empty page1/0.
14. Usage.order ánh xạ FK hiện có, không đổi mảng API.
15. usageId + loại → invoiceCode → toàn hóa đơn; back giữ query context giữa các tab.
16. Bỏ đánh giá khỏi tất cả UI, không backend rating.
17. Java inheritance; DB Member+Customer/Employee shared PK; Account gộp, role jobTitle.
18. Empty list mảng rỗng, not-found null; lỗi DB ném exception, xung đột lưu rollback.

Giữ chỗ dùng DB NOW(6) 3 phút, không transaction chờ người dùng. consume cùng INSERT và sequence trong một transaction. Session lưu kết quả mỗi context để retry trả lịch cũ; unique DB bảo vệ tranh chấp connection. Hóa đơn hiển thị nhân viên bán hàng từ order.ss và nhân viên thu tiền payment.ss, có thể khác nhau. Thống kê khách hàng trong menu disabled vì ngoài phạm vi.

Expected seed cố định theo tháng demo được ghi ở sql/seed-expected.json sau khi tạo, gồm timestamp đầu/cuối ngày, nhiều services/parts một phiếu, unpaid, ngoài khoảng, quantity>1 và giá danh mục khác lịch sử. Kết quả kiểm thử phải đối chiếu con số độc lập.
