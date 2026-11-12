# Scenario pha thiết kế cuối

## M1: truy vết toàn bộ scenario 31 bước

1. [V2 1–2] Khách hàng đã đăng nhập chọn Đặt lịch hẹn trên CustomerHomeView6.jsp. GET /booking đến BookingServlet6.
2. [3–5] Servlet lấy mã khách từ currentUser, gọi CustomerDAO6.getCustomerInfo(customerCode); entity Customer6 chứa thông tin Member từ DB.
3. [6–9] VehicleDAO6.getVehiclesOfCustomer(cus.id); forward cus,vehicleList,context và biểu mẫu BookAppointmentView6.jsp. Họ tên, điện thoại, email readonly.
4. [10–14] Khi chọn ngày, JavaScript gửi GET /booking?action=slots; SlotDAO6.getAvailableSlots(LocalDate) chỉ SELECT slot tương lai khả dụng. Cập nhật vùng khung giờ; giữ nội dung form.
5. [15–16] POST hold gồm context, CSRF, ngày, slot, xe, yêu cầu. Servlet validate server-side; bỏ mọi contact/customerId giả mạo. Kiểm tra xe thuộc khách, slot thuộc ngày trong lịch 30 ngày.
6. [17–19] SlotDAO6.holdSlot(slotId) guarded UPDATE nguyên tử, token UUID mới, thời hạn DB NOW+3 phút; trả slot/token/expiry, hoặc null khi tranh chấp.
7. [20–23] BookingServlet6 tạo new Appointment6 tạm, chưa INSERT. Lưu trong map bookingContexts của session theo UUID tab; ConfirmView6.jsp hiển thị thông tin và hạn giữ chỗ.
8. [24–27] Khách xác nhận bằng POST /appointment. AppointmentServlet6 khóa object context, đọc draft tin cậy, gọi validateHold. Hết hạn trả 409, cho chỉnh sửa.
9. [28–30] AppointmentDAO6.createAppointment(draft,holdCode): BEGIN, guarded consume cùng connection, khóa slot; kiểm tra lại ownership, khóa sequence LH, INSERT PENDING/ss NULL với DB NOW, COMMIT; mọi lỗi ROLLBACK.
10. [31] Session lưu completed trước khi trả redirect GET confirm. ConfirmView6.jsp hiện thành công, mã/ID/thời điểm/trạng thái thật. Retry cùng context trả completed, không tạo lịch thứ hai. Không thêm bước thông báo nhân viên.
11. Nhánh chỉnh sửa: POST edit release đúng token, giữ ngày/xe/yêu cầu, xóa draft/token cũ. Lần hold sau cấp token mới; refresh không gia hạn. Đóng tab tự hết hạn; logout/session timeout cố gắng release.
12. Nhánh thêm xe: POST addVehicle giữ form → GET /vehicle → brands → AddNewVehicleView6.jsp. POST /vehicle validate + chuẩn biển + cấp mã X trong transaction + INSERT; redirect booking, tải xe lại và chọn xe mới. Không gọi Java doGet của Servlet khác.

## M2: truy vết toàn bộ scenario 42 bước

1. [1–8] ManagerHome → GET RevenueServlet6 home/menu/filter/list. Menu báo cáo khách hàng disabled. Bộ lọc ngày yyyy-MM-dd và SERVICE/PART/ALL kiểm tra server-side.
2. [9–12] RevenueDAO6.getRevenueStatistics(java.util.Date startDate,endDate,statisticType) tổng hợp độc lập mỗi loại, chỉ Invoice.issueDate trong [start,end+1) và payment PAID; COUNT dòng; giá lịch sử; sort revenue DESC/code ASC/type ASC. Servlet tổng BigDecimal toàn bộ và slice 20 dòng, JSP hiển thị page/totalPages (empty=1/0).
3. [13–17] GET /service-part chuyển itemType=SERVICE,itemCode và query context. ServiceDAO6.getServiceInfo(code); không có trả 404.
4. [18–24] ServiceUsageDAO6.getTotalUsageCount không lọc; getAllServiceUsageOfService lọc issueDate/PAID, sort ngày hóa đơn DESC/id DESC; hydrate order nông đủ mã phiếu/khách/xe/completionTime. JSP chung hiển thị nhánh dịch vụ, không đánh giá.
5. [25–29] Có thể chọn PART trước hoặc sau SERVICE. GET /service-part itemType=PART,itemCode; SparePartDAO6.getPartInfo nạp brand/category và tồn kho view, không phục hồi thiết kế gộp Invoice cũ.
6. [30–36] PartUsageDAO6.getTotalUsageCount toàn thời gian; getAllPartUsageOfPart lọc đúng điều kiện. Cùng JSP hiển thị thông số/đơn vị/giá hiện tại để tham khảo/tồn kho/ảnh và history. Doanh thu không lấy giá danh mục này.
7. [37–38] Xem hóa đơn gửi itemType,usageId và query context; ServicePartServlet6 kiểm tra dòng thuộc item/bộ lọc rồi InvoiceDAO6.getInvoiceCodeByUsage; redirect InvoiceServlet6 với mã thực.
8. [39–42] InvoiceDAO6.getInvoiceDetail dựng toàn bộ Invoice6, order/cus/vehicle, cả hai mảng usage, payment, order.ss (nhân viên bán hàng) và payment.ss (nhân viên thu tiền). Đối chiếu tổng đã PAID. JSP hiển thị tất cả dòng và giá lịch sử/số lượng/tổng. Back khôi phục query context hoàn toàn; không dùng filter session chung giữa các tab.

Ảnh cũ giữ ở input để truy vết; các nguồn classes-final.puml, sequence và hợp đồng sinh từ code là thiết kế cuối.
