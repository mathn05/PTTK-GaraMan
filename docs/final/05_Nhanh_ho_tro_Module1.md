# Nhánh thêm xe/chỉnh sửa và graph tùy chọn

Không trộn nhánh thay thế này vào happy case chọn xe cũ trong V3 chính.

| Bước | Caller / hành vi thật | Nguồn |
|---|---|---|
| 1 | Khách bấm Thêm xe; BookingServlet6 giữ dữ liệu form trong Map context rồi redirect /vehicle?context. | [BookingServlet6:43](../../src/main/java/garaman/servlet/BookingServlet6.java#L43) |
| 2 | VehicleServlet6 lấy context, form query customer/VehicleBrandDAO.getVehicleBrands rồi forward AddNewVehicleView6. | [VehicleServlet6:19](../../src/main/java/garaman/servlet/VehicleServlet6.java#L19) |
| 3 | POST được role/CSRF bảo vệ; validate brand parse/tồn tại, plate/model/năm; Customer từ session/DB. | [VehicleServlet6:27](../../src/main/java/garaman/servlet/VehicleServlet6.java#L27) |
| 4 | Servlet new Vehicle6 id=null, gán trusted cus/brand/plate/model/year, Km giữ null, không nhận id/customer/code giả từ client. | [VehicleServlet6:37](../../src/main/java/garaman/servlet/VehicleServlet6.java#L37) |
| 5 | Servlet gọi VehicleDAO6.saveVehicle(v,customer.id). | [VehicleServlet6:39](../../src/main/java/garaman/servlet/VehicleServlet6.java#L39) |
| 6 | DAO validate, transaction sở hữu, kiểm brand, INSERT năm/Km setObject INTEGER nullable và long FKs, getGeneratedKeys. | [VehicleDAO6:43](../../src/main/java/garaman/dao/VehicleDAO6.java#L43) |
| 7 | DAO readback graph trong transaction, commit rồi return saved; mã xe chỉ getter dẫn xuất. | [VehicleDAO6:47](../../src/main/java/garaman/dao/VehicleDAO6.java#L47) |
| 8 | Servlet giữ id xe, redirect booking/context; form chọn xe mới, giữ ngày/nội dung. | [VehicleServlet6:39](../../src/main/java/garaman/servlet/VehicleServlet6.java#L39) |
| 9 | Chỉnh sửa khi đang hold: release đúng slot/token, xóa hold/draft, redirect form; không hủy completed. | [BookingServlet6:33](../../src/main/java/garaman/servlet/BookingServlet6.java#L33) |
| 10 | Logout và container session listener release tất cả context, token DB vẫn là guard. | [BookingServlet6:104](../../src/main/java/garaman/servlet/BookingServlet6.java#L104) |
| 11 | RepairOrder có appointment thì gọi AppointmentDAO.findById và graph customer/vehicle/slot/optional staff; fixture HD0001 không chạy nhánh đó. | [InvoiceDAO6:51](../../src/main/java/garaman/dao/InvoiceDAO6.java#L51) |
| 12 | UNPAID giữ Invoice.payment=null, không tạo Payment giả; khác nhánh PAID của V3 chính. | [InvoiceDAO6:27](../../src/main/java/garaman/dao/InvoiceDAO6.java#L27) |

Các nhánh lỗi không thuộc happy case: CSRF/role/ownership/context sai, validation400, not found404, conflict/expiry409. Consume + INSERT/mã rollback cùng transaction. Đã regression các nhánh này, không diễn đạt lỗi như bước thành công.
