# HTTP và view

Context /garaman6. JSP chỉ trong WEB-INF/views, hiển thị EL/JSTL, không gọi DAO. Tất cả POST có csrfToken do phiên sinh; session currentUser:Member6, currentRole:CUSTOMER/MANAGER, csrfToken:String. Thông tin cá nhân/role/ownership luôn nạp từ DB/session.

| Method / route / action | Wire input và handler | Output |
|---|---|---|
| GET /login | LoginServlet6 | LoginView6.jsp, message |
| POST /login login/logout | username/password/action/csrfToken | đổi session ID và redirect home theo role; logout release hold của phiên, invalidate |
| GET /booking?action=home | CUSTOMER | CustomerHomeView6.jsp, cus |
| GET /booking | context UUID tùy chọn; appointmentDate yyyy-MM-dd | BookAppointmentView6.jsp, cus,vehicleList,availableSlots,draftAppointment,context,appointmentDate |
| POST /booking hold | context,vehicle int,appointmentSlot int,appointmentDate,requestDescription <=255 | validate rồi hold, draft Appointment6 trong session bookingContexts[context], forward ConfirmView6.jsp |
| POST /booking edit | context | release đúng token, bỏ token cũ, redirect form giữ nội dung |
| POST /booking addVehicle | context và input form | lưu form dở (chưa hold), redirect /vehicle?context=... |
| GET /booking?action=confirm | context | đọc draft/kết quả đã lưu, không gia hạn; ConfirmView6.jsp |
| POST /appointment | context | kiểm tra/consume/INSERT transaction, kết quả theo context, redirect confirm để refresh không POST |
| GET /vehicle | context | AddNewVehicleView6.jsp, cus,vehicleBrandList |
| POST /vehicle | context,vehicleBrand int,vehicleModel,licensePlate,manufactureYear optional int | saveVehicle, redirect booking giữ form và chọn xe mới |
| GET /revenue | action home/menu/filter/list | ManagerHomeView6 / ReportMenuView6 / RevenueStatisticsFilterView6 / RevenueStatisticsListView6.jsp |
| GET /revenue?action=list | startDate,endDate yyyy-MM-dd,statisticType SERVICE/PART/ALL,page int | revenueStats[],totalRevenue BigDecimal,currentPage,totalPages và filter |
| GET /service-part | itemType SERVICE/PART,itemCode,startDate,endDate,statisticType,page (trang báo cáo),usagePage | ServicePartDetailView6.jsp, service hoặc sparePart, mảng usage nhánh tương ứng, lifetimeUsageCount, phân trang |
| GET /service-part?action=invoice | bộ context như detail + usageId int | xác minh usage thuộc item/filter, tra invoiceCode rồi redirect /invoice |
| GET /invoice | invoiceCode + context detail đầy đủ | InvoiceDetailView6.jsp, invoice object graph đầy đủ; back giữ trang/ngày/loại |

Query GET không mutate. Riêng slot được sinh ở setup CLI, không sinh trong GET. FullName/phone/email hiển thị readonly, mọi input giả mạo bị bỏ qua. Draft UUID map riêng từng phiên/tab; không session key filter toàn cục, báo cáo dùng query string explicit. Context không có trong phiên: 400; sai role/xe:403; object không có:404; hold xung đột/hết hạn:409; dữ liệu sai:400, form giữ nội dung; DB:500 trang chung, log server. Login sai:401. CSRF thiếu/sai:403. Ngày từ java.time.LocalDate chuyển sang java.util.Date đầu ngày Asia/Ho_Chi_Minh. Page <=0 đưa về 1, vượt phạm vi clamp; empty:1/0.
