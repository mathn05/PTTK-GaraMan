# Scenario V3 — Module 2: Thống kê doanh thu, hai lượt xem chi tiết và hóa đơn

Bản hiện trạng ngày 12/11/2026, snapshot SHA-256 ab485f1be8360b8dc1e1b1c9d9f0151c052f145c0ca8a0c4dc2adff642fe0ffb; không phải luồng đề xuất sửa. Bảng bằng chứng từng bước nằm trong [truy vết](../review/02_Truy_vet_Scenario_V3.md).

Tiền điều kiện: đã đăng nhập MANAGER và đang ở ManagerHomeView6.jsp; dùng fixture tháng 10/2026 của project, không thay đổi dữ liệu trong lúc xem. Chọn ALL, trang 1 có dịch vụ DV0001 và phụ tùng PT0005. Các phiếu sửa chữa được nạp trong các nhóm ánh xạ của happy case có appointmentId NULL, có đủ khách/xe/nhân viên đúng vai trò; hóa đơn HD0001 đã PAID, bằng tổng dòng. Chọn lần dùng phụ tùng id=1 thuộc HD0001. Đây là dữ liệu mẫu để diễn tả tham số có thật, không hardcode vào ứng dụng.

Thứ tự thực tế là lấy lịch sử trong kỳ trước, sau đó mới lấy tổng toàn thời gian ở cả hai lượt chi tiết. Khi bấm Xem hóa đơn, ServicePartServlet6 chạy lại nhánh phụ tùng, gồm thông tin/lịch sử/tổng dùng, trước khi tra mã hóa đơn. Các khác biệt thứ tự này được giữ nguyên và ghi ở báo cáo.

Mỗi HTTP request trong luồng đi qua AppFilter6 rồi AuthFilter6 trước handler theo mapping trong web.xml. Container dispatch vào handler; không có self-call doGet/doPost. Tên lớp không kèm package trong các bước; package đầy đủ có trong inventory. Constructor tạo tham chiếu; setter trả void. Nhóm ánh xạ “cho từng dòng” được lặp trong vòng while của DAO cho mọi dòng kết quả; entity không tự truy vấn hoặc tự tạo danh sách.

1. Tại ManagerHomeView6.jsp, nhân viên quản lý đã đăng nhập chọn “Xem báo cáo”.

2. ManagerHomeView6.jsp gửi GET /revenue?action=menu với cookie session.

3. AppFilter6 thiết lập encoding/header/CSRF trong session; AuthFilter6 kiểm tra currentUser/currentRole=MANAGER. Container dispatch vào RevenueServlet6.doGet; handler chọn action=menu.

4. RevenueServlet6.doGet gọi Web6.view(req, resp, "manager/ReportMenuView6"); Web6 gán ctx/businessZone và forward cùng request đến /WEB-INF/views/manager/ReportMenuView6.jsp.

5. ReportMenuView6.jsp hiển thị lựa chọn thống kê dịch vụ/phụ tùng theo doanh thu và đường quay về trang chủ.

6. Nhân viên quản lý chọn “Thống kê dịch vụ / phụ tùng theo doanh thu”; ReportMenuView6.jsp gửi GET /revenue?action=filter.

7. Qua AppFilter6/AuthFilter6, container dispatch RevenueServlet6.doGet. Handler action=filter gọi filter(req,resp); helper tính tháng trước theo Config6.zone và đặt startDate/endDate dạng String cùng statisticType mặc định ALL vào request.

8. RevenueServlet6.filter gọi Web6.view(req, resp, "manager/RevenueStatisticsFilterView6"); Web6 gán ctx/businessZone và forward cùng request đến /WEB-INF/views/manager/RevenueStatisticsFilterView6.jsp.

9. RevenueStatisticsFilterView6.jsp hiển thị control startDate, endDate, statisticType với ba lựa chọn ALL, SERVICE, PART và nút Thống kê.

10. Nhân viên quản lý chọn statisticType=ALL, startDate=2026-10-01, endDate=2026-10-31 và bấm Thống kê.

11. RevenueStatisticsFilterView6.jsp gửi GET /revenue?action=list&startDate=2026-10-01&endDate=2026-10-31&statisticType=ALL.

12. Qua hai filter, container dispatch RevenueServlet6.doGet. Handler gọi Web6.reportFilter(req) để parse ngày, kiểm tra thứ tự/giới hạn năm và whitelist loại, đặt lại startDate/endDate/statisticType thành attributes String; sau đó gọi list(req,resp).

13. RevenueServlet6.list tạo RevenueDAO6(), chuyển hai ngày String→LocalDate→java.util.Date bằng DateUtil6.date và gọi getRevenueStatistics(startDate,endDate,statisticType).

14. RevenueDAO6.getRevenueStatistics tổng hợp riêng dịch vụ và phụ tùng rồi UNION ALL. SQL dùng i.issueDate từ đầu ngày 01/10 đến trước đầu ngày 01/11, p.status='PAID'; COUNT(*) là số dòng sử dụng, SUM(saleUnitPrice) cho dịch vụ, SUM(usedQuantity*saleUnitPrice) cho phụ tùng; ORDER BY totalRevenue DESC,itemCode ASC,itemType ASC.

15. Với từng dòng tổng hợp, RevenueDAO6 tạo RevenueStats6(), gán itemCode,itemName,itemType,usageCount,totalRevenue; totalRevenue đọc BigDecimal rồi doubleValue. DAO thêm từng đối tượng vào ArrayList cục bộ.

16. RevenueDAO6.getRevenueStatistics chuyển rows thành RevenueStats6[] và trả all về RevenueServlet6.list; connection sở hữu được đóng.

17. RevenueServlet6.list lấy page qua Web6.page, clampPage; cộng totalRevenue bằng BigDecimal.valueOf từng giá trị double trên toàn bộ all. Helper slice lấy 20 dòng; handler đặt revenueStats,totalRevenue,currentPage,totalPages,totalRows.

18. RevenueServlet6.list gọi reportParams/url để tạo previousUrl,nextUrl,backUrl, giữ ngày/loại; URL quay bộ lọc không giữ page.

19. RevenueServlet6.list gọi Web6.view(req, resp, "manager/RevenueStatisticsListView6"); Web6 gán ctx/businessZone và forward cùng request đến /WEB-INF/views/manager/RevenueStatisticsListView6.jsp.

20. RevenueStatisticsListView6.jsp hiển thị danh sách ALL gồm dịch vụ/phụ tùng, số dòng dùng, doanh thu, tổng toàn tập và thông tin trang; mỗi link mang itemType,itemCode,startDate,endDate,statisticType,page.

21. Nhân viên quản lý chọn dịch vụ DV0001 trên danh sách. Link gửi GET /service-part với itemType=SERVICE,itemCode=DV0001 và ngữ cảnh bộ lọc/page.

22. Qua AppFilter6/AuthFilter6, container dispatch ServicePartServlet6.doGet. Handler gọi Web6.reportFilter(req), đọc itemType/itemCode, kiểm tra whitelist và độ dài mã; chuyển ngày thành java.util.Date, đặt itemType/itemCode vào request và đặt target=0 vì yêu cầu mở chi tiết không có action=invoice.

23. ServicePartServlet6.doGet tạo ServiceDAO6() và ServiceUsageDAO6().

24. ServicePartServlet6.doGet gọi ServiceDAO6.getServiceInfo(serviceCode). ServiceDAO6 truy vấn tblService6 theo mã nghiệp vụ đã bind.

25. ServiceDAO6.getServiceInfo gọi RowMapper6.service(rs). RowMapper6 tạo Service6(); gán id, serviceCode, serviceName, description, unitPrice.

26. RowMapper6.service trả Service6 về ServiceDAO6.getServiceInfo; phương thức DAO trả đối tượng này về ServicePartServlet6.doGet.

27. ServicePartServlet6.doGet gọi ServiceUsageDAO6.getAllServiceUsageOfService(serviceCode, startDate, endDate). DAO JOIN usage với đối tượng, tblInvoice6 và tblPayment6; lọc mã, issueDate >= đầu startDate và < đầu ngày sau endDate, p.status='PAID'; ORDER BY i.issueDate DESC, u.id DESC.

28. ServiceUsageDAO6.getAllServiceUsageOfService gọi UsageMapper6.service(con, rs) cho mỗi dòng. UsageMapper6 tạo ServiceUsage6(); gán id, usageTime, saleUnitPrice và note.

29. UsageMapper6.service gọi ServiceDAO6.findById(id). ServiceDAO6 truy vấn tblService6 theo id đã bind.

30. ServiceDAO6.findById gọi RowMapper6.service(rs). RowMapper6 tạo Service6(); gán id, serviceCode, serviceName, description, unitPrice.

31. RowMapper6.service trả Service6 về ServiceDAO6.findById; phương thức DAO trả đối tượng này về UsageMapper6.service.

32. UsageMapper6.service gán setService(...) bằng đối tượng vừa nhận.

33. UsageMapper6.service gọi RowMapper6.technician(con, id); helper gọi employee(con, id).

34. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo Technician6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

35. employee trả Employee6 về technician; technician kiểm tra đúng subtype Technician6 rồi trả tham chiếu về UsageMapper6.service.

36. UsageMapper6.service gán setTech(...) bằng Technician6 vừa nhận.

37. UsageMapper6.service gọi RowMapper6.order(con, id). order truy vấn tblRepairOrder6 theo id, tạo RepairOrder6() và gán id, repairOrderCode, createTime, completionTime, vehicleCondition, repairDescription, repairResult, repairStatus.

38. RowMapper6.order gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

39. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

40. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.order.

41. RowMapper6.order gán cus; tạo VehicleDAO6(con) mượn cùng connection và gọi findById(vehicleId).

42. VehicleDAO6.findById gọi RowMapper6.vehicle(con, rs) với dòng xe vừa truy vấn.

43. RowMapper6.vehicle tạo Vehicle6(); gán id, vehicleCode, licensePlate, vehicleModel, currentMileage và manufactureYear; getObject(..., Integer.class) giữ được năm NULL.

44. RowMapper6.vehicle gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

45. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

46. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.vehicle.

47. RowMapper6.vehicle gán cus bằng Customer6 vừa nhận; truy vấn tblVehicleBrand6 theo vehicleBrandId, tạo VehicleBrand6(), gán id/name và setBrand(brand).

48. RowMapper6.vehicle trả Vehicle6 đã có cus và brand về VehicleDAO6.findById.

49. VehicleDAO6.findById trả xe về RowMapper6.order; order gán setVehicle(...).

50. RowMapper6.order gọi RowMapper6.saleStaff(con, id); helper gọi employee(con, id).

51. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo SaleStaff6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

52. employee trả Employee6 về saleStaff; saleStaff kiểm tra đúng subtype SaleStaff6 rồi trả tham chiếu về RowMapper6.order.

53. RowMapper6.order gán ss. Với các phiếu được chọn có appointmentId NULL theo tiền điều kiện, order trả RepairOrder6 về UsageMapper6.service; serviceUsage và partUsage vẫn là hai mảng rỗng được khởi tạo trong entity.

54. UsageMapper6.service gán setOrder(...) và trả ServiceUsage6 về ServiceUsageDAO6.getAllServiceUsageOfService; DAO thêm đối tượng này vào ArrayList cục bộ của chính DAO. Các bước ánh xạ vừa nêu được lặp cho từng dòng ResultSet trước khi DAO chuyển sang mảng.

55. ServiceUsageDAO6.getAllServiceUsageOfService chuyển ArrayList của DAO thành ServiceUsage6[] và trả mảng về ServicePartServlet6.doGet.

56. ServicePartServlet6.doGet nhận mảng lịch sử, tính rows và usagePage bằng Web6.clampPage/page; đặt service và serviceUsageList bằng Web6.slice, rồi mới gọi ServiceUsageDAO6.getTotalUsageCount(serviceCode).

57. ServiceUsageDAO6.getTotalUsageCount COUNT(*) trên toàn bộ dòng sử dụng JOIN bảng đối tượng theo mã, không lọc ngày/hóa đơn/thanh toán, trả 5 về doGet; handler đặt lifetimeUsageCount.

58. ServicePartServlet6.doGet duyệt toàn bộ mảng lịch sử để so id với target, đóng hai DAO; đặt currentPage,totalPages,totalRows,reportPage, dùng Web6.reportParams/url để tạo backUrl giữ bộ lọc và trang danh sách.

59. ServicePartServlet6.doGet tạo previousUrl,nextUrl,invoiceBaseUrl với itemType/itemCode, ngày, statisticType,page,usagePage; invoiceBaseUrl có action=invoice.

60. ServicePartServlet6.doGet gọi Web6.view(req, resp, "manager/ServicePartDetailView6"); Web6 gán ctx/businessZone và forward cùng request đến /WEB-INF/views/manager/ServicePartDetailView6.jsp.

61. ServicePartDetailView6.jsp hiển thị thông tin dịch vụ DV0001, tổng dùng toàn thời gian 5 và 2 dòng dùng trong kỳ. Lịch sử thể hiện phiếu sửa chữa, khách hàng, biển số, ngày hoàn thành và link Xem hóa đơn theo u.id.

62. Nhân viên quản lý chọn “Quay lại danh sách”; GET backUrl về /revenue?action=list giữ startDate/endDate/statisticType=ALL/page=1 và thực thi lại đoạn dựng danh sách ở bước 12–20; sau đó vẫn ở cùng tập ALL.

63. Nhân viên quản lý chọn phụ tùng PT0005 trong danh sách ALL; link gửi GET /service-part với itemType=PART,itemCode=PT0005 và giữ ngày/statisticType/page.

64. Qua AppFilter6/AuthFilter6, container dispatch ServicePartServlet6.doGet. Handler gọi Web6.reportFilter(req), đọc itemType/itemCode, kiểm tra whitelist và độ dài mã; chuyển ngày thành java.util.Date, đặt itemType/itemCode vào request và đặt target=0 vì yêu cầu mở chi tiết không có action=invoice.

65. ServicePartServlet6.doGet tạo SparePartDAO6() và PartUsageDAO6().

66. ServicePartServlet6.doGet gọi SparePartDAO6.getPartInfo(partCode). SparePartDAO6 truy vấn vwSparePart6 JOIN tblPartBrand6/tblCategory6 theo mã nghiệp vụ đã bind.

67. SparePartDAO6.getPartInfo gọi RowMapper6.part(rs). RowMapper6 tạo SparePart6(); gán id, partCode, partName, description, unit, technicalSpecifications, unitPrice, stockQuantity, image; tiếp tục tạo PartBrand6()/Category6(), gán id/tên và setBrand(...)/setCat(...).

68. RowMapper6.part trả SparePart6 về SparePartDAO6.getPartInfo; phương thức DAO trả đối tượng này về ServicePartServlet6.doGet.

69. ServicePartServlet6.doGet gọi PartUsageDAO6.getAllPartUsageOfPart(partCode, startDate, endDate). DAO JOIN usage với đối tượng, tblInvoice6 và tblPayment6; lọc mã, issueDate >= đầu startDate và < đầu ngày sau endDate, p.status='PAID'; ORDER BY i.issueDate DESC, u.id DESC.

70. PartUsageDAO6.getAllPartUsageOfPart gọi UsageMapper6.part(con, rs) cho mỗi dòng. UsageMapper6 tạo PartUsage6(); gán id, usageTime, saleUnitPrice, usedQuantity và note.

71. UsageMapper6.part gọi SparePartDAO6.findById(id). SparePartDAO6 truy vấn vwSparePart6 JOIN tblPartBrand6/tblCategory6 theo id đã bind.

72. SparePartDAO6.findById gọi RowMapper6.part(rs). RowMapper6 tạo SparePart6(); gán id, partCode, partName, description, unit, technicalSpecifications, unitPrice, stockQuantity, image; tiếp tục tạo PartBrand6()/Category6(), gán id/tên và setBrand(...)/setCat(...).

73. RowMapper6.part trả SparePart6 về SparePartDAO6.findById; phương thức DAO trả đối tượng này về UsageMapper6.part.

74. UsageMapper6.part gán setSparePart(...) bằng đối tượng vừa nhận.

75. UsageMapper6.part gọi RowMapper6.technician(con, id); helper gọi employee(con, id).

76. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo Technician6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

77. employee trả Employee6 về technician; technician kiểm tra đúng subtype Technician6 rồi trả tham chiếu về UsageMapper6.part.

78. UsageMapper6.part gán setTech(...) bằng Technician6 vừa nhận.

79. UsageMapper6.part gọi RowMapper6.order(con, id). order truy vấn tblRepairOrder6 theo id, tạo RepairOrder6() và gán id, repairOrderCode, createTime, completionTime, vehicleCondition, repairDescription, repairResult, repairStatus.

80. RowMapper6.order gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

81. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

82. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.order.

83. RowMapper6.order gán cus; tạo VehicleDAO6(con) mượn cùng connection và gọi findById(vehicleId).

84. VehicleDAO6.findById gọi RowMapper6.vehicle(con, rs) với dòng xe vừa truy vấn.

85. RowMapper6.vehicle tạo Vehicle6(); gán id, vehicleCode, licensePlate, vehicleModel, currentMileage và manufactureYear; getObject(..., Integer.class) giữ được năm NULL.

86. RowMapper6.vehicle gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

87. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

88. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.vehicle.

89. RowMapper6.vehicle gán cus bằng Customer6 vừa nhận; truy vấn tblVehicleBrand6 theo vehicleBrandId, tạo VehicleBrand6(), gán id/name và setBrand(brand).

90. RowMapper6.vehicle trả Vehicle6 đã có cus và brand về VehicleDAO6.findById.

91. VehicleDAO6.findById trả xe về RowMapper6.order; order gán setVehicle(...).

92. RowMapper6.order gọi RowMapper6.saleStaff(con, id); helper gọi employee(con, id).

93. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo SaleStaff6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

94. employee trả Employee6 về saleStaff; saleStaff kiểm tra đúng subtype SaleStaff6 rồi trả tham chiếu về RowMapper6.order.

95. RowMapper6.order gán ss. Với các phiếu được chọn có appointmentId NULL theo tiền điều kiện, order trả RepairOrder6 về UsageMapper6.part; serviceUsage và partUsage vẫn là hai mảng rỗng được khởi tạo trong entity.

96. UsageMapper6.part gán setOrder(...) và trả PartUsage6 về PartUsageDAO6.getAllPartUsageOfPart; DAO thêm đối tượng này vào ArrayList cục bộ của chính DAO. Các bước ánh xạ vừa nêu được lặp cho từng dòng ResultSet trước khi DAO chuyển sang mảng.

97. PartUsageDAO6.getAllPartUsageOfPart chuyển ArrayList của DAO thành PartUsage6[] và trả mảng về ServicePartServlet6.doGet.

98. ServicePartServlet6.doGet nhận mảng lịch sử, tính rows và usagePage bằng Web6.clampPage/page; đặt sparePart và partUsageList bằng Web6.slice, rồi mới gọi PartUsageDAO6.getTotalUsageCount(partCode).

99. PartUsageDAO6.getTotalUsageCount COUNT(*) trên toàn bộ dòng sử dụng JOIN bảng đối tượng theo mã, không lọc ngày/hóa đơn/thanh toán, trả 3 về doGet; handler đặt lifetimeUsageCount.

100. ServicePartServlet6.doGet lấy sparePart.image, chấp nhận đường dẫn /assets/... có phần mở rộng cho phép và đặt partImageUrl; không dùng URL tùy ý từ client.

101. ServicePartServlet6.doGet duyệt toàn bộ mảng lịch sử để so id với target, đóng hai DAO; đặt currentPage,totalPages,totalRows,reportPage, dùng Web6.reportParams/url để tạo backUrl giữ bộ lọc và trang danh sách.

102. ServicePartServlet6.doGet tạo previousUrl,nextUrl,invoiceBaseUrl với itemType/itemCode, ngày, statisticType,page,usagePage; invoiceBaseUrl có action=invoice.

103. ServicePartServlet6.doGet gọi Web6.view(req, resp, "manager/ServicePartDetailView6"); Web6 gán ctx/businessZone và forward cùng request đến /WEB-INF/views/manager/ServicePartDetailView6.jsp.

104. ServicePartDetailView6.jsp hiển thị phụ tùng PT0005, danh mục/thương hiệu/đơn vị/giá hiện tại/thông số/tồn kho/ảnh, tổng dùng toàn thời gian 3 và 2 dòng dùng trong kỳ. Lịch sử thể hiện phiếu sửa chữa, khách hàng, biển số, ngày hoàn thành và link Xem hóa đơn theo u.id.

105. Nhân viên quản lý chọn “Xem hóa đơn” tại dòng phụ tùng có u.id=1 của phiếu liên quan HD0001.

106. ServicePartDetailView6.jsp gửi GET invoiceBaseUrl&usageId=1 đến /service-part với action=invoice,itemType=PART,itemCode=PT0005, ngày,statisticType,page,usagePage.

107. Qua AppFilter6/AuthFilter6, container dispatch ServicePartServlet6.doGet. Handler gọi Web6.reportFilter(req), đọc itemType/itemCode, kiểm tra whitelist và độ dài mã; chuyển ngày thành java.util.Date, đặt itemType/itemCode vào request và đọc target=usageId=1 vì action=invoice.

108. ServicePartServlet6.doGet tạo SparePartDAO6() và PartUsageDAO6().

109. ServicePartServlet6.doGet gọi SparePartDAO6.getPartInfo(partCode). SparePartDAO6 truy vấn vwSparePart6 JOIN tblPartBrand6/tblCategory6 theo mã nghiệp vụ đã bind.

110. SparePartDAO6.getPartInfo gọi RowMapper6.part(rs). RowMapper6 tạo SparePart6(); gán id, partCode, partName, description, unit, technicalSpecifications, unitPrice, stockQuantity, image; tiếp tục tạo PartBrand6()/Category6(), gán id/tên và setBrand(...)/setCat(...).

111. RowMapper6.part trả SparePart6 về SparePartDAO6.getPartInfo; phương thức DAO trả đối tượng này về ServicePartServlet6.doGet.

112. ServicePartServlet6.doGet gọi PartUsageDAO6.getAllPartUsageOfPart(partCode, startDate, endDate). DAO JOIN usage với đối tượng, tblInvoice6 và tblPayment6; lọc mã, issueDate >= đầu startDate và < đầu ngày sau endDate, p.status='PAID'; ORDER BY i.issueDate DESC, u.id DESC.

113. PartUsageDAO6.getAllPartUsageOfPart gọi UsageMapper6.part(con, rs) cho mỗi dòng. UsageMapper6 tạo PartUsage6(); gán id, usageTime, saleUnitPrice, usedQuantity và note.

114. UsageMapper6.part gọi SparePartDAO6.findById(id). SparePartDAO6 truy vấn vwSparePart6 JOIN tblPartBrand6/tblCategory6 theo id đã bind.

115. SparePartDAO6.findById gọi RowMapper6.part(rs). RowMapper6 tạo SparePart6(); gán id, partCode, partName, description, unit, technicalSpecifications, unitPrice, stockQuantity, image; tiếp tục tạo PartBrand6()/Category6(), gán id/tên và setBrand(...)/setCat(...).

116. RowMapper6.part trả SparePart6 về SparePartDAO6.findById; phương thức DAO trả đối tượng này về UsageMapper6.part.

117. UsageMapper6.part gán setSparePart(...) bằng đối tượng vừa nhận.

118. UsageMapper6.part gọi RowMapper6.technician(con, id); helper gọi employee(con, id).

119. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo Technician6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

120. employee trả Employee6 về technician; technician kiểm tra đúng subtype Technician6 rồi trả tham chiếu về UsageMapper6.part.

121. UsageMapper6.part gán setTech(...) bằng Technician6 vừa nhận.

122. UsageMapper6.part gọi RowMapper6.order(con, id). order truy vấn tblRepairOrder6 theo id, tạo RepairOrder6() và gán id, repairOrderCode, createTime, completionTime, vehicleCondition, repairDescription, repairResult, repairStatus.

123. RowMapper6.order gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

124. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

125. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.order.

126. RowMapper6.order gán cus; tạo VehicleDAO6(con) mượn cùng connection và gọi findById(vehicleId).

127. VehicleDAO6.findById gọi RowMapper6.vehicle(con, rs) với dòng xe vừa truy vấn.

128. RowMapper6.vehicle tạo Vehicle6(); gán id, vehicleCode, licensePlate, vehicleModel, currentMileage và manufactureYear; getObject(..., Integer.class) giữ được năm NULL.

129. RowMapper6.vehicle gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

130. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

131. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.vehicle.

132. RowMapper6.vehicle gán cus bằng Customer6 vừa nhận; truy vấn tblVehicleBrand6 theo vehicleBrandId, tạo VehicleBrand6(), gán id/name và setBrand(brand).

133. RowMapper6.vehicle trả Vehicle6 đã có cus và brand về VehicleDAO6.findById.

134. VehicleDAO6.findById trả xe về RowMapper6.order; order gán setVehicle(...).

135. RowMapper6.order gọi RowMapper6.saleStaff(con, id); helper gọi employee(con, id).

136. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo SaleStaff6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

137. employee trả Employee6 về saleStaff; saleStaff kiểm tra đúng subtype SaleStaff6 rồi trả tham chiếu về RowMapper6.order.

138. RowMapper6.order gán ss. Với các phiếu được chọn có appointmentId NULL theo tiền điều kiện, order trả RepairOrder6 về UsageMapper6.part; serviceUsage và partUsage vẫn là hai mảng rỗng được khởi tạo trong entity.

139. UsageMapper6.part gán setOrder(...) và trả PartUsage6 về PartUsageDAO6.getAllPartUsageOfPart; DAO thêm đối tượng này vào ArrayList cục bộ của chính DAO. Các bước ánh xạ vừa nêu được lặp cho từng dòng ResultSet trước khi DAO chuyển sang mảng.

140. PartUsageDAO6.getAllPartUsageOfPart chuyển ArrayList của DAO thành PartUsage6[] và trả mảng về ServicePartServlet6.doGet.

141. ServicePartServlet6.doGet nhận mảng lịch sử, tính rows và usagePage bằng Web6.clampPage/page; đặt sparePart và partUsageList bằng Web6.slice, rồi mới gọi PartUsageDAO6.getTotalUsageCount(partCode).

142. PartUsageDAO6.getTotalUsageCount COUNT(*) trên toàn bộ dòng sử dụng JOIN bảng đối tượng theo mã, không lọc ngày/hóa đơn/thanh toán, trả 3 về doGet; handler đặt lifetimeUsageCount.

143. ServicePartServlet6.doGet lấy sparePart.image, chấp nhận đường dẫn /assets/... có phần mở rộng cho phép và đặt partImageUrl; không dùng URL tùy ý từ client.

144. ServicePartServlet6.doGet duyệt toàn bộ mảng lịch sử để so id với target, đóng hai DAO; đặt currentPage,totalPages,totalRows,reportPage, dùng Web6.reportParams/url để tạo backUrl giữ bộ lọc và trang danh sách.

145. ServicePartServlet6.doGet đi nhánh target!=0, kiểm tra containsUsage=true; tạo InvoiceDAO6() và gọi getInvoiceCodeByUsage(type,target).

146. InvoiceDAO6.getInvoiceCodeByUsage chọn tblPartUsage6 từ switch whitelist, JOIN tblInvoice6 qua repairOrderId, lọc u.id=? và trả chuỗi HD0001 về ServicePartServlet6.doGet; không tạo Invoice6 tại phép tra mã này.

147. ServicePartServlet6.doGet thêm invoiceCode=HD0001 vào map URL và sendRedirect đến /invoice, giữ itemType,itemCode,startDate,endDate,statisticType,page,usagePage.

148. Trình duyệt GET URL /invoice sau redirect; qua hai filter, container dispatch InvoiceServlet6.doGet. Handler đọc/kiểm tra độ dài invoiceCode rồi tạo InvoiceDAO6() và gọi getInvoiceDetail(invoiceCode).

149. InvoiceDAO6.getInvoiceDetail SELECT vwInvoice6 theo invoiceCode. View tính tổng dịch vụ và phụ tùng riêng trước khi JOIN để tránh nhân dòng; DAO tạo Invoice6(), gán id/invoiceCode/issueDate và totalAmount bằng BigDecimal.doubleValue.

150. InvoiceDAO6.getInvoiceDetail gọi RowMapper6.order(con, id). order truy vấn tblRepairOrder6 theo id, tạo RepairOrder6() và gán id, repairOrderCode, createTime, completionTime, vehicleCondition, repairDescription, repairResult, repairStatus.

151. RowMapper6.order gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

152. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

153. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.order.

154. RowMapper6.order gán cus; tạo VehicleDAO6(con) mượn cùng connection và gọi findById(vehicleId).

155. VehicleDAO6.findById gọi RowMapper6.vehicle(con, rs) với dòng xe vừa truy vấn.

156. RowMapper6.vehicle tạo Vehicle6(); gán id, vehicleCode, licensePlate, vehicleModel, currentMileage và manufactureYear; getObject(..., Integer.class) giữ được năm NULL.

157. RowMapper6.vehicle gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

158. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

159. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.vehicle.

160. RowMapper6.vehicle gán cus bằng Customer6 vừa nhận; truy vấn tblVehicleBrand6 theo vehicleBrandId, tạo VehicleBrand6(), gán id/name và setBrand(brand).

161. RowMapper6.vehicle trả Vehicle6 đã có cus và brand về VehicleDAO6.findById.

162. VehicleDAO6.findById trả xe về RowMapper6.order; order gán setVehicle(...).

163. RowMapper6.order gọi RowMapper6.saleStaff(con, id); helper gọi employee(con, id).

164. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo SaleStaff6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

165. employee trả Employee6 về saleStaff; saleStaff kiểm tra đúng subtype SaleStaff6 rồi trả tham chiếu về RowMapper6.order.

166. RowMapper6.order gán ss. Với các phiếu được chọn có appointmentId NULL theo tiền điều kiện, order trả RepairOrder6 về InvoiceDAO6.getInvoiceDetail; serviceUsage và partUsage vẫn là hai mảng rỗng được khởi tạo trong entity.

167. InvoiceDAO6.getInvoiceDetail nhận o, tạo ServiceUsageDAO6(con) mượn connection hiện tại để lấy tất cả dòng dịch vụ của o.id.

168. InvoiceDAO6.getInvoiceDetail gọi ServiceUsageDAO6.byOrder(orderId). DAO truy vấn tất cả dòng theo repairOrderId, ORDER BY id; không dùng khoảng thống kê hoặc trạng thái thanh toán.

169. ServiceUsageDAO6.byOrder gọi UsageMapper6.service(con, rs) cho mỗi dòng. UsageMapper6 tạo ServiceUsage6(); gán id, usageTime, saleUnitPrice và note.

170. UsageMapper6.service gọi ServiceDAO6.findById(id). ServiceDAO6 truy vấn tblService6 theo id đã bind.

171. ServiceDAO6.findById gọi RowMapper6.service(rs). RowMapper6 tạo Service6(); gán id, serviceCode, serviceName, description, unitPrice.

172. RowMapper6.service trả Service6 về ServiceDAO6.findById; phương thức DAO trả đối tượng này về UsageMapper6.service.

173. UsageMapper6.service gán setService(...) bằng đối tượng vừa nhận.

174. UsageMapper6.service gọi RowMapper6.technician(con, id); helper gọi employee(con, id).

175. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo Technician6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

176. employee trả Employee6 về technician; technician kiểm tra đúng subtype Technician6 rồi trả tham chiếu về UsageMapper6.service.

177. UsageMapper6.service gán setTech(...) bằng Technician6 vừa nhận.

178. UsageMapper6.service gọi RowMapper6.order(con, id). order truy vấn tblRepairOrder6 theo id, tạo RepairOrder6() và gán id, repairOrderCode, createTime, completionTime, vehicleCondition, repairDescription, repairResult, repairStatus.

179. RowMapper6.order gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

180. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

181. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.order.

182. RowMapper6.order gán cus; tạo VehicleDAO6(con) mượn cùng connection và gọi findById(vehicleId).

183. VehicleDAO6.findById gọi RowMapper6.vehicle(con, rs) với dòng xe vừa truy vấn.

184. RowMapper6.vehicle tạo Vehicle6(); gán id, vehicleCode, licensePlate, vehicleModel, currentMileage và manufactureYear; getObject(..., Integer.class) giữ được năm NULL.

185. RowMapper6.vehicle gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

186. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

187. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.vehicle.

188. RowMapper6.vehicle gán cus bằng Customer6 vừa nhận; truy vấn tblVehicleBrand6 theo vehicleBrandId, tạo VehicleBrand6(), gán id/name và setBrand(brand).

189. RowMapper6.vehicle trả Vehicle6 đã có cus và brand về VehicleDAO6.findById.

190. VehicleDAO6.findById trả xe về RowMapper6.order; order gán setVehicle(...).

191. RowMapper6.order gọi RowMapper6.saleStaff(con, id); helper gọi employee(con, id).

192. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo SaleStaff6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

193. employee trả Employee6 về saleStaff; saleStaff kiểm tra đúng subtype SaleStaff6 rồi trả tham chiếu về RowMapper6.order.

194. RowMapper6.order gán ss. Với các phiếu được chọn có appointmentId NULL theo tiền điều kiện, order trả RepairOrder6 về UsageMapper6.service; serviceUsage và partUsage vẫn là hai mảng rỗng được khởi tạo trong entity.

195. UsageMapper6.service gán setOrder(...) và trả ServiceUsage6 về ServiceUsageDAO6.byOrder; DAO thêm đối tượng này vào ArrayList cục bộ của chính DAO. Các bước ánh xạ vừa nêu được lặp cho từng dòng ResultSet trước khi DAO chuyển sang mảng.

196. ServiceUsageDAO6.byOrder chuyển ArrayList của DAO thành ServiceUsage6[] và trả mảng về InvoiceDAO6.getInvoiceDetail.

197. InvoiceDAO6.getInvoiceDetail gán o.setServiceUsage(mảng vừa nhận), sau đó tạo PartUsageDAO6(con) trên cùng connection.

198. InvoiceDAO6.getInvoiceDetail gọi PartUsageDAO6.byOrder(orderId). DAO truy vấn tất cả dòng theo repairOrderId, ORDER BY id; không dùng khoảng thống kê hoặc trạng thái thanh toán.

199. PartUsageDAO6.byOrder gọi UsageMapper6.part(con, rs) cho mỗi dòng. UsageMapper6 tạo PartUsage6(); gán id, usageTime, saleUnitPrice, usedQuantity và note.

200. UsageMapper6.part gọi SparePartDAO6.findById(id). SparePartDAO6 truy vấn vwSparePart6 JOIN tblPartBrand6/tblCategory6 theo id đã bind.

201. SparePartDAO6.findById gọi RowMapper6.part(rs). RowMapper6 tạo SparePart6(); gán id, partCode, partName, description, unit, technicalSpecifications, unitPrice, stockQuantity, image; tiếp tục tạo PartBrand6()/Category6(), gán id/tên và setBrand(...)/setCat(...).

202. RowMapper6.part trả SparePart6 về SparePartDAO6.findById; phương thức DAO trả đối tượng này về UsageMapper6.part.

203. UsageMapper6.part gán setSparePart(...) bằng đối tượng vừa nhận.

204. UsageMapper6.part gọi RowMapper6.technician(con, id); helper gọi employee(con, id).

205. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo Technician6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

206. employee trả Employee6 về technician; technician kiểm tra đúng subtype Technician6 rồi trả tham chiếu về UsageMapper6.part.

207. UsageMapper6.part gán setTech(...) bằng Technician6 vừa nhận.

208. UsageMapper6.part gọi RowMapper6.order(con, id). order truy vấn tblRepairOrder6 theo id, tạo RepairOrder6() và gán id, repairOrderCode, createTime, completionTime, vehicleCondition, repairDescription, repairResult, repairStatus.

209. RowMapper6.order gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

210. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

211. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.order.

212. RowMapper6.order gán cus; tạo VehicleDAO6(con) mượn cùng connection và gọi findById(vehicleId).

213. VehicleDAO6.findById gọi RowMapper6.vehicle(con, rs) với dòng xe vừa truy vấn.

214. RowMapper6.vehicle tạo Vehicle6(); gán id, vehicleCode, licensePlate, vehicleModel, currentMileage và manufactureYear; getObject(..., Integer.class) giữ được năm NULL.

215. RowMapper6.vehicle gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

216. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

217. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.vehicle.

218. RowMapper6.vehicle gán cus bằng Customer6 vừa nhận; truy vấn tblVehicleBrand6 theo vehicleBrandId, tạo VehicleBrand6(), gán id/name và setBrand(brand).

219. RowMapper6.vehicle trả Vehicle6 đã có cus và brand về VehicleDAO6.findById.

220. VehicleDAO6.findById trả xe về RowMapper6.order; order gán setVehicle(...).

221. RowMapper6.order gọi RowMapper6.saleStaff(con, id); helper gọi employee(con, id).

222. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo SaleStaff6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

223. employee trả Employee6 về saleStaff; saleStaff kiểm tra đúng subtype SaleStaff6 rồi trả tham chiếu về RowMapper6.order.

224. RowMapper6.order gán ss. Với các phiếu được chọn có appointmentId NULL theo tiền điều kiện, order trả RepairOrder6 về UsageMapper6.part; serviceUsage và partUsage vẫn là hai mảng rỗng được khởi tạo trong entity.

225. UsageMapper6.part gán setOrder(...) và trả PartUsage6 về PartUsageDAO6.byOrder; DAO thêm đối tượng này vào ArrayList cục bộ của chính DAO. Các bước ánh xạ vừa nêu được lặp cho từng dòng ResultSet trước khi DAO chuyển sang mảng.

226. PartUsageDAO6.byOrder chuyển ArrayList của DAO thành PartUsage6[] và trả mảng về InvoiceDAO6.getInvoiceDetail.

227. InvoiceDAO6.getInvoiceDetail gán o.setPartUsage(mảng vừa nhận), i.setOrder(o), đọc paymentId và SELECT tblPayment6 WHERE id=?.

228. InvoiceDAO6.getInvoiceDetail tạo Payment6(), gán id,paymentAmount,paymentTime,paymentMethod,status,transactionReference từ dòng payment; số tiền đọc BigDecimal rồi doubleValue.

229. InvoiceDAO6.getInvoiceDetail gọi RowMapper6.saleStaff(con, id); helper gọi employee(con, id).

230. RowMapper6.employee truy vấn tblMember6 JOIN tblEmployee6 theo memberId; tạo SaleStaff6() theo jobTitle. employee gọi member(rs,e) để gán hồ sơ và Account6; member hoàn tất void, employee tiếp tục setEmployeeCode(...) và setJobTitle(...).

231. employee trả Employee6 về saleStaff; saleStaff kiểm tra đúng subtype SaleStaff6 rồi trả tham chiếu về InvoiceDAO6.getInvoiceDetail.

232. InvoiceDAO6.getInvoiceDetail gán payment.ss bằng SaleStaff6 vừa nhận, gán i.payment; payment là PAID nên DAO so sánh tổng và số tiền thanh toán bằng BigDecimal đọc trực tiếp từ SQL, kết quả bằng nhau trong happy case.

233. InvoiceDAO6.getInvoiceDetail trả Invoice6 đầy đủ về InvoiceServlet6.doGet; connection sở hữu được đóng sau try-with-resources.

234. InvoiceServlet6.doGet đặt invoice vào request; sau khi đã tải hóa đơn, gọi Web6.reportFilter(req), kiểm tra itemType và tạo backUrl về /service-part với itemType/itemCode, ngày,statisticType,page,usagePage.

235. InvoiceServlet6.doGet gọi Web6.view(req, resp, "manager/InvoiceDetailView6"); Web6 gán ctx/businessZone và forward cùng request đến /WEB-INF/views/manager/InvoiceDetailView6.jsp.

236. InvoiceDetailView6.jsp hiển thị HD0001, ngày lập, phiếu sửa chữa, khách hàng/xe, nhân viên bán hàng order.ss, đủ 2 dòng dịch vụ và 2 dòng phụ tùng theo giá lịch sử, tổng 3.600.000 VNĐ; phần payment thể hiện trạng thái/phương thức/thời điểm/số tiền, nhân viên thu tiền payment.ss và mã giao dịch. Nút quay lại giữ ngữ cảnh chi tiết; không có phần rating.

