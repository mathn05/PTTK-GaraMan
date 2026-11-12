# Bảng truy vết chi tiết — Module 2

Source SHA-256: `f0c79f6eb835cc5a8145068a161d6e15a97f682e65176925e039de5c334e736b`; [manifest](evidence/snapshot-after.json).

Đi kèm [V3 chính](Scenario_V3_Module2.md). Đây là bằng chứng source kết hợp regression runtime, không phải instrumentation log từng setter. Các dòng dưới đây giải thích caller/query/đóng gói/return; không biến chúng thành hàng trăm bước trong V3 chính.

DAO() sở hữu connection, DAO(Connection) mượn; chỉ owner commit/close. Các DAO con dùng cùng connection khi hydrate trong transaction. Filter mapping REQUEST kiểm CSRF/role trước handler; JSP chỉ render và gửi scalar. Date ở Java là java.util.Date, ID/count business Integer, tiền BigDecimal; private date methods dùng DAO6.zone().

| Dòng | Bước V3 | Caller và thao tác thật | Nguồn |
|---|---|---|---|
| 1 | 1 | Thao tác người dùng → link GET. | [ManagerHomeView6.jsp:6](../../src/main/webapp/WEB-INF/views/manager/ManagerHomeView6.jsp#L6) |
| 2 | 2 | Switch action menu, RequestDispatcher qua private view. | [RevenueServlet6.java:27](../../src/main/java/garaman/servlet/RevenueServlet6.java#L27) |
| 3 | 3 | Link chọn loại báo cáo. | [ReportMenuView6.jsp:6](../../src/main/webapp/WEB-INF/views/manager/ReportMenuView6.jsp#L6) |
| 4 | 4 | Lấy ngày theo DAO6.zone, đặt start/end/type request attrs. | [RevenueServlet6.java:36](../../src/main/java/garaman/servlet/RevenueServlet6.java#L36) |
| 5 | 4 | Forward filter view. | [RevenueServlet6.java:41](../../src/main/java/garaman/servlet/RevenueServlet6.java#L41) |
| 6 | 5 | Giữ nguyên control, ngày HTTP String ISO. | [RevenueStatisticsFilterView6.jsp:9](../../src/main/webapp/WEB-INF/views/manager/RevenueStatisticsFilterView6.jsp#L9) |
| 7 | 6 | Kiểm ngày/loại/thứ tự khoảng, chuyển sang luồng list. | [RevenueServlet6.java:30](../../src/main/java/garaman/servlet/RevenueServlet6.java#L30) |
| 8 | 7 | Private date(LocalDate) dùng zone thống nhất. | [RevenueServlet6.java:45](../../src/main/java/garaman/servlet/RevenueServlet6.java#L45) |
| 9 | 8 | COUNT(*) service và SUM historical price; phần part SUM quantity*price. | [RevenueDAO6.java:16](../../src/main/java/garaman/dao/RevenueDAO6.java#L16) |
| 10 | 8 | ALL UNION ALL, bind [start,end+1), ổn định mã/loại khi bằng tiền. | [RevenueDAO6.java:18](../../src/main/java/garaman/dao/RevenueDAO6.java#L18) |
| 11 | 9 | Setter void theo từng dòng, getBigDecimal không double. | [RevenueDAO6.java:24](../../src/main/java/garaman/dao/RevenueDAO6.java#L24) |
| 12 | 10 | Tổng toàn tập trước slice. | [RevenueServlet6.java:47](../../src/main/java/garaman/servlet/RevenueServlet6.java#L47) |
| 13 | 10 | Forward list với request attrs. | [RevenueServlet6.java:50](../../src/main/java/garaman/servlet/RevenueServlet6.java#L50) |
| 14 | 11 | GET detail chứa itemType/itemCode/kỳ/type/page. | [RevenueStatisticsListView6.jsp:8](../../src/main/webapp/WEB-INF/views/manager/RevenueStatisticsListView6.jsp#L8) |
| 15 | 12 | Nhánh SERVICE sau validate. | [ServicePartServlet6.java:31](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L31) |
| 16 | 13 | Query theo serviceCode. | [ServiceDAO6.java:10](../../src/main/java/garaman/dao/ServiceDAO6.java#L10) |
| 17 | 13 | new Service6, setter void, unitPrice getBigDecimal. | [ServiceDAO6.java:20](../../src/main/java/garaman/dao/ServiceDAO6.java#L20) |
| 18 | 14 | Thứ tự history trước lifetime trong code. | [ServicePartServlet6.java:32](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L32) |
| 19 | 15 | Query JOIN Invoice, biên ngày nửa mở, sort issueDate DESC/id DESC. | [ServiceUsageDAO6.java:20](../../src/main/java/garaman/dao/ServiceUsageDAO6.java#L20) |
| 20 | 15 | new usage + historical BigDecimal; ServiceDAO.findById, MemberDAO.findTechnicianById, InvoiceDAO.findOrderById. | [ServiceUsageDAO6.java:32](../../src/main/java/garaman/dao/ServiceUsageDAO6.java#L32) |
| 21 | 15 | Nạp order/customer/vehicle/salesperson/optional appointment, không tự nạp ngược usages. | [InvoiceDAO6.java:42](../../src/main/java/garaman/dao/InvoiceDAO6.java#L42) |
| 22 | 16 | Đặt item/slice rồi gọi lifetime. | [ServicePartServlet6.java:33](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L33) |
| 23 | 16 | COUNT mọi dòng, không lọc kỳ/PAID; không có dòng thì trả Integer 0. | [ServiceUsageDAO6.java:14](../../src/main/java/garaman/dao/ServiceUsageDAO6.java#L14) |
| 24 | 17 | Đặt attrs/URLs giữ kỳ/page/usagePage. | [ServicePartServlet6.java:54](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L54) |
| 25 | 17 | EL/JSTL render SERVICE, lifetime riêng history. | [ServicePartDetailView6.jsp:5](../../src/main/webapp/WEB-INF/views/manager/ServicePartDetailView6.jsp#L5) |
| 26 | 18 | backUrl về report list giữ reportParams, không thêm lượt chi tiết. | [ServicePartServlet6.java:46](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L46) |
| 27 | 19 | Loại PART phân biệt kể cả code giống service. | [RevenueStatisticsListView6.jsp:8](../../src/main/webapp/WEB-INF/views/manager/RevenueStatisticsListView6.jsp#L8) |
| 28 | 20 | Nhánh PART sau validate chung. | [ServicePartServlet6.java:38](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L38) |
| 29 | 21 | vwSparePart6 có stock receipts−usages. | [SparePartDAO6.java:12](../../src/main/java/garaman/dao/SparePartDAO6.java#L12) |
| 30 | 21 | new SparePart6/PartBrand6/Category6, setters void, unitPrice BigDecimal. | [SparePartDAO6.java:21](../../src/main/java/garaman/dao/SparePartDAO6.java#L21) |
| 31 | 22 | History theo bộ lọc gốc. | [ServicePartServlet6.java:39](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L39) |
| 32 | 23 | Query/sort giống nguyên tắc service, quantity không phải count. | [PartUsageDAO6.java:20](../../src/main/java/garaman/dao/PartUsageDAO6.java#L20) |
| 33 | 23 | new PartUsage6, usedQuantity Integer/price BigDecimal; gọi các DAO item/role/order phù hợp. | [PartUsageDAO6.java:32](../../src/main/java/garaman/dao/PartUsageDAO6.java#L32) |
| 34 | 24 | Item/slice rồi lifetime. | [ServicePartServlet6.java:40](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L40) |
| 35 | 24 | COUNT dòng, không SUM quantity, thiếu dữ liệu trả0. | [PartUsageDAO6.java:14](../../src/main/java/garaman/dao/PartUsageDAO6.java#L14) |
| 36 | 25 | Whitelist local asset, giữ back/filter/page. | [ServicePartServlet6.java:41](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L41) |
| 37 | 25 | JSP render PART và link usageId mỗi dòng. | [ServicePartDetailView6.jsp:18](../../src/main/webapp/WEB-INF/views/manager/ServicePartDetailView6.jsp#L18) |
| 38 | 26 | Scalar usageId của đúng dòng đang hiển thị. | [ServicePartDetailView6.jsp:10](../../src/main/webapp/WEB-INF/views/manager/ServicePartDetailView6.jsp#L10) |
| 39 | 27 | Không dùng0 để bỏ qua invoice action. | [ServicePartServlet6.java:28](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L28) |
| 40 | 27 | Membership so giá trị Integer trên all đã lọc, kể cả ID>127; query lặp đúng thứ tự nhánh PART. | [ServicePartServlet6.java:34](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L34) |
| 41 | 28 | Handler gọi lookup scalar. | [ServicePartServlet6.java:49](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L49) |
| 42 | 28 | Whitelist usage table + Invoice JOIN orderFK, không new Invoice trong lookup. | [InvoiceDAO6.java:14](../../src/main/java/garaman/dao/InvoiceDAO6.java#L14) |
| 43 | 29 | Không object Java qua URL. | [ServicePartServlet6.java:51](../../src/main/java/garaman/servlet/ServicePartServlet6.java#L51) |
| 44 | 30 | Mở DAO, query code validated. | [InvoiceServlet6.java:23](../../src/main/java/garaman/servlet/InvoiceServlet6.java#L23) |
| 45 | 31 | new Invoice6/setter void, total getBigDecimal. | [InvoiceDAO6.java:22](../../src/main/java/garaman/dao/InvoiceDAO6.java#L22) |
| 46 | 31 | Graph order trên cùng connection, appointment null trong fixture. | [InvoiceDAO6.java:24](../../src/main/java/garaman/dao/InvoiceDAO6.java#L24) |
| 47 | 32 | Nạp tất cả service lines, không filter kỳ. | [InvoiceDAO6.java:25](../../src/main/java/garaman/dao/InvoiceDAO6.java#L25) |
| 48 | 32 | Nạp tất cả part lines, cùng private readUsage/graph con; setters void. | [InvoiceDAO6.java:26](../../src/main/java/garaman/dao/InvoiceDAO6.java#L26) |
| 49 | 33 | Không bảng Payment; id logic=Invoice.id, amount BigDecimal. | [InvoiceDAO6.java:28](../../src/main/java/garaman/dao/InvoiceDAO6.java#L28) |
| 50 | 33 | Cashier từ FK Invoice khác người bán ở Order. | [InvoiceDAO6.java:33](../../src/main/java/garaman/dao/InvoiceDAO6.java#L33) |
| 51 | 33 | Khớp tiền rồi setPayment(void), return invoice đầy đủ. | [InvoiceDAO6.java:34](../../src/main/java/garaman/dao/InvoiceDAO6.java#L34) |
| 52 | 34 | Forward object trong request, Back đúng item/kỳ/page/usagePage. | [InvoiceServlet6.java:30](../../src/main/java/garaman/servlet/InvoiceServlet6.java#L30) |
| 53 | 34 | Hiển thị hai vai trò tách biệt, tất cả dòng/BigDecimal/getLineAmount, không rating. | [InvoiceDetailView6.jsp:9](../../src/main/webapp/WEB-INF/views/manager/InvoiceDetailView6.jsp#L9) |
