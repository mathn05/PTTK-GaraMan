# Scenario V3 — Module 1: Đặt lịch hẹn

Source SHA-256: `f0c79f6eb835cc5a8145068a161d6e15a97f682e65176925e039de5c334e736b`; [manifest](evidence/snapshot-after.json).

Tiền điều kiện: khách đã đăng nhập role CUSTOMER, hồ sơ ACTIVE, có xe của mình và slot tương lai còn trống. Chọn xe đã lưu trong luồng chính; [nhánh thêm xe/chỉnh sửa](05_Nhanh_ho_tro_Module1.md) viết riêng. Thông tin liên hệ readonly, một context UUID mỗi tab, sức chứa1/hold3 phút. Cách A: Servlet đóng gói, không JSP xử lý trung gian.

Các bước dưới đây chỉ là happy case. Constructor khởi tạo tham chiếu, setter void; bên gọi giữ object. Nhóm hydrate graph con/lặp được diễn đạt gọn, nguồn chi tiết nằm ở bảng truy vết riêng. HTTP được container dispatch, không có lời gọi handler vào chính nó.

1. Tại CustomerHomeView6.jsp, khách chọn Đặt lịch hẹn; trình duyệt gửi GET /booking.
2. Container đi qua AppFilter6 và AuthFilter6 rồi dispatch BookingServlet6.doGet(req,resp).
3. BookingServlet6 lấy context/tab qua phương thức context, tạo Map mới trong session nếu cần, khóa Map rồi gọi form.
4. BookingServlet6.form gọi customer(req), dùng mã khách trong session để gọi CustomerDAO6.getCustomerInfo(customerCode).
5. CustomerDAO6 đọc ResultSet, tạo Customer6 và dùng MemberDAO6.fillMember gán hồ sơ/Account nếu có; trả Customer6 về Servlet.
6. BookingServlet6.form gọi VehicleDAO6.getVehiclesOfCustomer(customerId); DAO đóng gói từng xe cùng customer/brand và trả Vehicle6[].
7. BookingServlet6.form gọi SlotDAO6.getAvailableSlots(appointmentDate); DAO query slot trong ngày, đóng gói AppointmentSlot6[] rồi trả kết quả.
8. BookingServlet6 đặt dữ liệu request và forward BookAppointmentView6.jsp; JSP hiển thị khách, xe, ngày/slot và biểu mẫu.
9. Khách đổi ngày; app.js gửi GET /booking?action=slots với context và ngày đã chọn.
10. BookingServlet6.doGet lấy lại context, cập nhật ngày rồi gọi form; form tải lại hồ sơ/xe/slot theo thứ tự trên và trả HTML.
11. JavaScript thay fieldset slot từ HTML phản hồi; xe và nội dung khách đang nhập được giữ trên trang.
12. Khách chọn xe, slot, kiểm tra liên hệ readonly, nhập yêu cầu rồi bấm Xác nhận; JSP POST /booking action=hold.
13. Sau kiểm CSRF/role, BookingServlet6.doPost lấy và khóa context, parse/validate action, ngày, nội dung và lựa chọn.
14. BookingServlet6 lấy Customer6 trusted từ DB, gọi VehicleDAO6.findById rồi so ownership bằng Objects.equals.
15. BookingServlet6 gọi SlotDAO6.findById(slotId), nhận slot và kiểm ngày của startTime khớp ngày đã chọn.
16. BookingServlet6 gọi SlotDAO6.holdSlot(slotId); DAO dùng token riêng và guarded UPDATE giữ slot 3 phút theo DB, đọc lại đúng token rồi trả AppointmentSlot6.
17. Sau khi hold thành công, BookingServlet6 khởi tạo Appointment6 tạm, gán khách/xe/slot/nội dung bằng setter và giữ draft/token/hạn trong Map server của tab.
18. BookingServlet6.confirm đặt lịch tạm vào request, gọi SlotDAO6.remainingHoldMillis để lấy TTL server rồi forward ConfirmView6.jsp.
19. ConfirmView6.jsp hiển thị lịch và hạn giữ chỗ; JavaScript đếm elapsed bằng performance.now trên TTL server.
20. Khách chọn Xác nhận đặt lịch; JSP POST /appointment cùng context và CSRF token.
21. AppointmentServlet6 lấy và khóa Map context, lấy lại draft đã có, kiểm ownership với hồ sơ mới; không tạo lại draft.
22. AppointmentServlet6 gọi SlotDAO6.validateHold(slotId,holdCode), nhận true trong happy case.
23. AppointmentServlet6 gọi AppointmentDAO6.createAppointment(draft,holdCode); DAO validate graph và mở transaction sở hữu.
24. AppointmentDAO6 gọi SlotDAO6.consumeHold trên cùng connection, rồi SELECT xe thuộc khách FOR SHARE.
25. AppointmentDAO6 tạo placeholder unique hợp lệ, INSERT lịch PENDING với staff null và lấy generated ID; DB tự sinh activeSlotId UNIQUE.
26. AppointmentDAO6 cập nhật mã cuối LH từ generated ID ngay trong transaction trước khi trả kết quả.
27. AppointmentDAO6.findById đọc lại lịch đã lưu cùng customer/vehicle/slot/optional staff; DAO commit và trả Appointment6 persisted cho AppointmentServlet6.
28. AppointmentServlet6 giữ completed, xóa draft/hold khỏi Map và redirect GET /booking?action=confirm theo PRG.
29. BookingServlet6.doGet lấy completed, forward ConfirmView6.jsp hiển thị Đặt lịch hẹn thành công, mã lịch, thời điểm tạo và Chờ tiếp nhận.

## Mapping truy vết

| Bước V3 | Dòng truy vết |
|---|---|
| 1 | 1–1 |
| 2 | 2–4 |
| 3 | 5–6 |
| 4 | 7–8 |
| 5 | 9–10 |
| 6 | 11–13 |
| 7 | 14–15 |
| 8 | 16–17 |
| 9 | 18–19 |
| 10 | 20–21 |
| 11 | 22–22 |
| 12 | 23–23 |
| 13 | 24–25 |
| 14 | 26–27 |
| 15 | 28–28 |
| 16 | 29–30 |
| 17 | 31–32 |
| 18 | 33–35 |
| 19 | 36–37 |
| 20 | 38–38 |
| 21 | 39–40 |
| 22 | 41–42 |
| 23 | 43–44 |
| 24 | 45–46 |
| 25 | 47–49 |
| 26 | 50–50 |
| 27 | 51–52 |
| 28 | 53–54 |
| 29 | 55–56 |

[Bảng truy vết source](Scenario_V3_Ky_thuat_Module1.md).
