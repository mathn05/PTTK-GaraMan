# Scenario V3 — Module 1: Khách hàng đặt lịch hẹn trực tuyến

Bản hiện trạng ngày 12/11/2026, snapshot SHA-256 ab485f1be8360b8dc1e1b1c9d9f0151c052f145c0ca8a0c4dc2adff642fe0ffb; không phải luồng đề xuất sửa. Bảng bằng chứng từng bước nằm trong [truy vết](../review/02_Truy_vet_Scenario_V3.md).

Tiền điều kiện: đã đăng nhập CUSTOMER và đang ở CustomerHomeView6.jsp; hồ sơ/xe thuộc khách tồn tại. DB có slot khả dụng trong lịch cấu hình. Khách chọn ngày khác ngày mặc định, nhập mô tả hợp lệ, giữ rồi xác nhận trong 3 phút; đồng hồ trình duyệt đồng bộ đủ để nút xác nhận còn hoạt động. Luồng chính dùng xe đã có. Thêm xe, chỉnh sửa, hết hạn và các nhánh lỗi được review riêng, không chen vào happy case.

Mỗi HTTP request trong luồng đi qua AppFilter6 rồi AuthFilter6 trước handler theo mapping trong web.xml. Container dispatch vào handler; không có self-call doGet/doPost. Tên lớp không kèm package trong các bước; package đầy đủ có trong inventory. Constructor tạo tham chiếu; setter trả void. Nhóm ánh xạ “cho từng dòng” được lặp trong vòng while của DAO cho mọi dòng kết quả; entity không tự truy vấn hoặc tự tạo danh sách.

1. Tại CustomerHomeView6.jsp của khách hàng đã đăng nhập, khách hàng chọn “Đặt lịch hẹn”.

2. CustomerHomeView6.jsp gửi GET /booking với cookie session; không gửi mã khách hàng hay đối tượng Java.

3. AppFilter6.doFilter thiết lập UTF-8/header/session csrfToken; AuthFilter6.doFilter kiểm tra currentUser, currentRole=CUSTOMER và chuyển chuỗi xử lý đến route /booking.

4. BookingServlet6 tiếp nhận yêu cầu tại doGet(req,resp) do container dispatch; gọi Web6.context(req,true).

5. Web6.context gọi contexts(session) để lấy/tạo Map bookingContexts; tạo UUID, khởi tạo BookingContext6(id), đặt appointmentDate là ngày mai theo Config6.zone(), lưu c vào map của session và trả c về doGet.

6. BookingServlet6.doGet đồng bộ trên c và gọi helper form(req,resp,c).

7. BookingServlet6.form gọi Web6.customer(req); Web6 lấy currentUser từ session, xác định Customer6 và lấy customerCode.

8. Web6.customer tạo CustomerDAO6() và gọi getCustomerInfo(customerCode).

9. CustomerDAO6.getCustomerInfo truy vấn tblCustomer6 JOIN tblMember6 với customerCode = ?.

10. CustomerDAO6.getCustomerInfo gọi RowMapper6.customer(rs) để ánh xạ dòng hồ sơ khách hàng.

11. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

12. RowMapper6 trả Customer6 đã gán dữ liệu về CustomerDAO6.getCustomerInfo.

13. CustomerDAO6 trả Customer6 về Web6.customer; DAO sở hữu connection được đóng sau try-with-resources.

14. Web6.customer trả hồ sơ Customer6 mới đọc từ DB về BookingServlet6.form.

15. BookingServlet6.form đặt context, cus, appointmentDate (String), selectedVehicle, selectedSlot, requestDescription, draftAppointment, minDate/maxDate vào request; tạo VehicleDAO6() và SlotDAO6().

16. BookingServlet6.form gọi vehicles.getVehiclesOfCustomer(cus.getId()); VehicleDAO6 SELECT xe theo customerId, ORDER BY id và chuẩn bị ArrayList<Vehicle6> cục bộ.

17. VehicleDAO6.getVehiclesOfCustomer gọi RowMapper6.vehicle(con, rs) với dòng xe vừa truy vấn.

18. RowMapper6.vehicle tạo Vehicle6(); gán id, vehicleCode, licensePlate, vehicleModel, currentMileage và manufactureYear; getObject(..., Integer.class) giữ được năm NULL.

19. RowMapper6.vehicle gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

20. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

21. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.vehicle.

22. RowMapper6.vehicle gán cus bằng Customer6 vừa nhận; truy vấn tblVehicleBrand6 theo vehicleBrandId, tạo VehicleBrand6(), gán id/name và setBrand(brand).

23. RowMapper6.vehicle trả Vehicle6 đã có cus và brand về VehicleDAO6.getVehiclesOfCustomer.

24. VehicleDAO6 thêm từng xe được mapper trả về vào rows, lặp nhóm ánh xạ cho mỗi dòng, chuyển rows thành Vehicle6[] và trả về form. form đặt vehicleList.

25. BookingServlet6.form gọi slots.getAvailableSlots(c.appointmentDate). SlotDAO6 lọc [đầu ngày, đầu ngày kế tiếp), startTime>NOW(6), AVAILABLE hoặc HELD hết hạn, không có activeSlotId; ORDER BY startTime,id.

26. SlotDAO6.getAvailableSlots gọi RowMapper6.slot(rs) cho từng dòng khung giờ.

27. RowMapper6.slot tạo AppointmentSlot6(); gán id, startTime, holdCode, holdExpiresAt, status rồi trả tham chiếu slot về SlotDAO6.getAvailableSlots.

28. SlotDAO6 thêm từng slot vào ArrayList cục bộ, chuyển thành AppointmentSlot6[] và trả về form; form đặt availableSlots, đóng các connection sở hữu khi kết thúc try-with-resources.

29. BookingServlet6.form gọi Web6.view(req, resp, "customer/BookAppointmentView6"); Web6 gán ctx/businessZone và forward cùng request đến /WEB-INF/views/customer/BookAppointmentView6.jsp.

30. BookAppointmentView6.jsp hiển thị họ tên, điện thoại, email readonly; danh sách xe, ngày mặc định, radio slot, ô requestDescription và nút xác nhận.

31. Khách hàng chọn ngày hẹn hợp lệ khác ngày mặc định.

32. app.js gọi loadSlots, tạo URL GET /booking?action=slots&context=...&appointmentDate=... và fetch với Accept:text/html, cookie session cùng origin.

33. Qua hai filter, container dispatch vào BookingServlet6.doGet; handler lấy lại c bằng Web6.context(req,true), đồng bộ c, đọc ngày bằng Web6.date và gọi form(req,resp,c).

34. BookingServlet6.form gọi Web6.customer(req); Web6 lấy currentUser từ session, xác định Customer6 và lấy customerCode.

35. Web6.customer tạo CustomerDAO6() và gọi getCustomerInfo(customerCode).

36. CustomerDAO6.getCustomerInfo truy vấn tblCustomer6 JOIN tblMember6 với customerCode = ?.

37. CustomerDAO6.getCustomerInfo gọi RowMapper6.customer(rs) để ánh xạ dòng hồ sơ khách hàng.

38. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

39. RowMapper6 trả Customer6 đã gán dữ liệu về CustomerDAO6.getCustomerInfo.

40. CustomerDAO6 trả Customer6 về Web6.customer; DAO sở hữu connection được đóng sau try-with-resources.

41. Web6.customer trả hồ sơ Customer6 mới đọc từ DB về BookingServlet6.form.

42. BookingServlet6.form đặt context, cus, appointmentDate (String), selectedVehicle, selectedSlot, requestDescription, draftAppointment, minDate/maxDate vào request; tạo VehicleDAO6() và SlotDAO6().

43. BookingServlet6.form gọi vehicles.getVehiclesOfCustomer(cus.getId()); VehicleDAO6 SELECT xe theo customerId, ORDER BY id và chuẩn bị ArrayList<Vehicle6> cục bộ.

44. VehicleDAO6.getVehiclesOfCustomer gọi RowMapper6.vehicle(con, rs) với dòng xe vừa truy vấn.

45. RowMapper6.vehicle tạo Vehicle6(); gán id, vehicleCode, licensePlate, vehicleModel, currentMileage và manufactureYear; getObject(..., Integer.class) giữ được năm NULL.

46. RowMapper6.vehicle gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

47. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

48. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.vehicle.

49. RowMapper6.vehicle gán cus bằng Customer6 vừa nhận; truy vấn tblVehicleBrand6 theo vehicleBrandId, tạo VehicleBrand6(), gán id/name và setBrand(brand).

50. RowMapper6.vehicle trả Vehicle6 đã có cus và brand về VehicleDAO6.getVehiclesOfCustomer.

51. VehicleDAO6 thêm từng xe được mapper trả về vào rows, lặp nhóm ánh xạ cho mỗi dòng, chuyển rows thành Vehicle6[] và trả về form. form đặt vehicleList.

52. BookingServlet6.form gọi slots.getAvailableSlots(c.appointmentDate). SlotDAO6 lọc [đầu ngày, đầu ngày kế tiếp), startTime>NOW(6), AVAILABLE hoặc HELD hết hạn, không có activeSlotId; ORDER BY startTime,id.

53. SlotDAO6.getAvailableSlots gọi RowMapper6.slot(rs) cho từng dòng khung giờ.

54. RowMapper6.slot tạo AppointmentSlot6(); gán id, startTime, holdCode, holdExpiresAt, status rồi trả tham chiếu slot về SlotDAO6.getAvailableSlots.

55. SlotDAO6 thêm từng slot vào ArrayList cục bộ, chuyển thành AppointmentSlot6[] và trả về form; form đặt availableSlots, đóng các connection sở hữu khi kết thúc try-with-resources.

56. BookingServlet6.form gọi Web6.view(req, resp, "customer/BookAppointmentView6"); Web6 gán ctx/businessZone và forward cùng request đến /WEB-INF/views/customer/BookAppointmentView6.jsp.

57. BookAppointmentView6.jsp render toàn bộ HTML form với availableSlots theo ngày mới; phản hồi fetch là HTML đầy đủ.

58. app.js dùng DOMParser đọc HTML và replaceWith .slot-fieldset của request mới nhất; control xe/mô tả đang nhập trong DOM được giữ lại.

59. Khách hàng chọn appointmentSlot, chọn vehicle, kiểm tra liên hệ readonly, nhập requestDescription hợp lệ và bấm nút action=hold.

60. BookAppointmentView6.jsp submit POST /booking với csrfToken, context, appointmentDate, appointmentSlot, vehicle, requestDescription, action=hold.

61. AppFilter6 kiểm tra CSRF, AuthFilter6 kiểm tra CUSTOMER; container dispatch BookingServlet6.doPost. Handler lấy context(req,false), đồng bộ c, đọc ngày/mô tả/id xe/id slot và kiểm tra nội tuyến action, khoảng ngày, trường bắt buộc, độ dài mô tả.

62. BookingServlet6.doPost gọi Web6.customer(req); Web6 lấy currentUser từ session, xác định Customer6 và lấy customerCode.

63. Web6.customer tạo CustomerDAO6() và gọi getCustomerInfo(customerCode).

64. CustomerDAO6.getCustomerInfo truy vấn tblCustomer6 JOIN tblMember6 với customerCode = ?.

65. CustomerDAO6.getCustomerInfo gọi RowMapper6.customer(rs) để ánh xạ dòng hồ sơ khách hàng.

66. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

67. RowMapper6 trả Customer6 đã gán dữ liệu về CustomerDAO6.getCustomerInfo.

68. CustomerDAO6 trả Customer6 về Web6.customer; DAO sở hữu connection được đóng sau try-with-resources.

69. Web6.customer trả hồ sơ Customer6 mới đọc từ DB về BookingServlet6.doPost.

70. BookingServlet6.doPost tạo VehicleDAO6() và gọi findById(c.vehicleId); DAO SELECT tblVehicle6 WHERE id=?.

71. VehicleDAO6.findById gọi RowMapper6.vehicle(con, rs) với dòng xe vừa truy vấn.

72. RowMapper6.vehicle tạo Vehicle6(); gán id, vehicleCode, licensePlate, vehicleModel, currentMileage và manufactureYear; getObject(..., Integer.class) giữ được năm NULL.

73. RowMapper6.vehicle gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

74. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

75. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.vehicle.

76. RowMapper6.vehicle gán cus bằng Customer6 vừa nhận; truy vấn tblVehicleBrand6 theo vehicleBrandId, tạo VehicleBrand6(), gán id/name và setBrand(brand).

77. RowMapper6.vehicle trả Vehicle6 đã có cus và brand về VehicleDAO6.findById.

78. VehicleDAO6.findById trả selected về doPost; doPost kiểm tra selected.cus.id bằng customer.id lấy từ session/DB.

79. BookingServlet6.doPost tạo SlotDAO6() và gọi findById(c.slotId); DAO SELECT tblAppointmentSlot6 WHERE id=?.

80. SlotDAO6.findById gọi RowMapper6.slot(rs) với dòng khung giờ.

81. RowMapper6.slot tạo AppointmentSlot6(); gán id, startTime, holdCode, holdExpiresAt, status rồi trả tham chiếu slot về SlotDAO6.findById.

82. SlotDAO6.findById trả slotInfo về doPost; doPost dùng DateUtil6.localDate(slotInfo.startTime) để xác định slot cùng c.appointmentDate.

83. BookingServlet6.doPost gọi slots.holdSlot(c.slotId).

84. SlotDAO6.holdSlot tạo UUID token, UPDATE có điều kiện đặt HELD/holdCode/holdExpiresAt=NOW(6)+3 phút. Happy case affectedRows=1 trên connection autocommit; DAO SELECT lại theo cả id và token vừa tạo.

85. SlotDAO6.holdSlot gọi RowMapper6.slot(rs) với dòng khung giờ.

86. RowMapper6.slot tạo AppointmentSlot6(); gán id, startTime, holdCode, holdExpiresAt, status rồi trả tham chiếu slot về SlotDAO6.holdSlot.

87. SlotDAO6.holdSlot trả held về doPost; doPost gán c.holdCode và c.holdExpiresAt từ held.

88. BookingServlet6.doPost tạo Appointment6(); gọi setCus(customer), setVehicle(selected), setSlot(held), setRequestDescription(c.requestDescription). Setter trả void; doPost giữ tham chiếu a và gán c.draft=a trong context thuộc session.

89. BookingServlet6.doPost gọi confirm(req,resp,c); helper đặt context, appointment=c.draft, completed=false, holdCode, holdExpiresAt, holdExpiresAtMillis vào request.

90. BookingServlet6.confirm gọi Web6.view(req, resp, "customer/ConfirmView6"); Web6 gán ctx/businessZone và forward cùng request đến /WEB-INF/views/customer/ConfirmView6.jsp.

91. ConfirmView6.jsp hiển thị lịch tạm: ngày/giờ, khách hàng, liên hệ, xe, mô tả, hạn giữ chỗ và nút “Xác nhận đặt lịch”; app.js cập nhật countdown từ holdExpiresAtMillis.

92. Khách hàng kiểm tra thông tin và bấm “Xác nhận đặt lịch” trong hạn giữ chỗ.

93. ConfirmView6.jsp submit POST /appointment với csrfToken, context; Appointment6 và token giữ chỗ vẫn được giữ phía server.

94. Qua AppFilter6 và AuthFilter6, container dispatch AppointmentServlet6.doPost. Handler lấy Web6.context(req,false), đồng bộ c, xác định completed chưa có, draft/holdCode/slotId đang tồn tại.

95. AppointmentServlet6.doPost gọi Web6.customer(req); Web6 lấy currentUser từ session, xác định Customer6 và lấy customerCode.

96. Web6.customer tạo CustomerDAO6() và gọi getCustomerInfo(customerCode).

97. CustomerDAO6.getCustomerInfo truy vấn tblCustomer6 JOIN tblMember6 với customerCode = ?.

98. CustomerDAO6.getCustomerInfo gọi RowMapper6.customer(rs) để ánh xạ dòng hồ sơ khách hàng.

99. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

100. RowMapper6 trả Customer6 đã gán dữ liệu về CustomerDAO6.getCustomerInfo.

101. CustomerDAO6 trả Customer6 về Web6.customer; DAO sở hữu connection được đóng sau try-with-resources.

102. Web6.customer trả hồ sơ Customer6 mới đọc từ DB về AppointmentServlet6.doPost.

103. AppointmentServlet6.doPost so sánh khách hàng vừa xác minh với c.draft.cus.id; tạo SlotDAO6() và gọi validateHold(c.slotId,c.holdCode).

104. SlotDAO6.validateHold SELECT theo id, HELD, đúng token, holdExpiresAt>NOW(6) và startTime>NOW(6); trả true về AppointmentServlet6.doPost.

105. AppointmentServlet6.doPost tạo AppointmentDAO6() sở hữu connection rồi gọi createAppointment(c.draft,c.holdCode).

106. AppointmentDAO6.createAppointment kiểm tra quan hệ/mô tả rồi setAutoCommit(false) trên connection sở hữu để bắt đầu transaction.

107. AppointmentDAO6.createAppointment tạo SlotDAO6(con) mượn cùng connection và gọi consumeHold(appointment.slot.id,holdCode).

108. SlotDAO6.consumeHold kiểm tra autocommit=false, gọi changeHold(slotId,holdCode,true). changeHold UPDATE HELD→BOOKED, xóa token/hạn với điều kiện token đúng và hạn/giờ còn hiệu lực; khóa dòng giữ đến cuối transaction.

109. changeHold trả true về consumeHold; consumeHold trả true về AppointmentDAO6.createAppointment. Cập nhật slot vẫn chưa commit.

110. AppointmentDAO6.createAppointment SELECT xe theo id/customerId FOR SHARE để xác minh quyền sở hữu ngay trong transaction.

111. AppointmentDAO6.createAppointment tạo CodeSequenceDAO6(con), gọi nextCode("LH"). DAO phụ SELECT lastValue FOR UPDATE, tăng bằng Math.addExact, UPDATE bộ đếm và trả mã LH + số đệm ít nhất 4 chữ số về createAppointment.

112. AppointmentDAO6.createAppointment INSERT tblAppointment6: mã vừa cấp, mô tả, NOW(6), PENDING, saleStaffId=NULL, vehicleId/appointmentSlotId/customerId; nhận generated key id.

113. AppointmentDAO6.createAppointment gọi RowMapper6.appointment(con,id). Mapper SELECT lịch vừa INSERT, tạo Appointment6(), gán id, appointmentCode, createAt, requestDescription và status.

114. RowMapper6.appointment gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

115. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

116. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.appointment.

117. RowMapper6.appointment gán cus, tạo VehicleDAO6(con), gọi findById(vehicleId).

118. VehicleDAO6.findById gọi RowMapper6.vehicle(con, rs) với dòng xe vừa truy vấn.

119. RowMapper6.vehicle tạo Vehicle6(); gán id, vehicleCode, licensePlate, vehicleModel, currentMileage và manufactureYear; getObject(..., Integer.class) giữ được năm NULL.

120. RowMapper6.vehicle gọi RowMapper6.customer(con, id) để truy vấn tblMember6 JOIN tblCustomer6 theo memberId.

121. Trong customer(rs), RowMapper6 tạo Customer6 bằng Customer6(); gọi member(rs, c) để gán id, fullName, phone, email, address. member tạo Account6(), gán id, username, password, profileImage, status rồi setAcc(a); member hoàn tất void. customer tiếp tục setCustomerCode(...).

122. RowMapper6 trả Customer6 đã gán dữ liệu từ customer(rs) về customer(con, id), rồi về RowMapper6.vehicle.

123. RowMapper6.vehicle gán cus bằng Customer6 vừa nhận; truy vấn tblVehicleBrand6 theo vehicleBrandId, tạo VehicleBrand6(), gán id/name và setBrand(brand).

124. RowMapper6.vehicle trả Vehicle6 đã có cus và brand về VehicleDAO6.findById.

125. VehicleDAO6.findById trả xe về RowMapper6.appointment; mapper setVehicle(...), tạo SlotDAO6(con), gọi findById(appointmentSlotId).

126. SlotDAO6.findById gọi RowMapper6.slot(rs) với dòng khung giờ.

127. RowMapper6.slot tạo AppointmentSlot6(); gán id, startTime, holdCode, holdExpiresAt, status rồi trả tham chiếu slot về SlotDAO6.findById.

128. SlotDAO6.findById trả slot BOOKED đã xóa token/hạn về RowMapper6.appointment. Mapper setSlot(...), gọi saleStaff(con,0); helper trả null ngay do saleStaffId NULL. Mapper setSs(null) và trả saved về AppointmentDAO6.createAppointment.

129. AppointmentDAO6.createAppointment commit thành công, khôi phục autocommit trong finally và trả saved về AppointmentServlet6.doPost; lịch hẹn và tiêu thụ chỗ đã được commit nguyên tử.

130. AppointmentServlet6.doPost gán c.completed=saved, xóa holdCode/holdExpiresAt/draft, giữ completed trong session context và sendRedirect /booking?action=confirm&context=....

131. Trình duyệt gửi GET theo redirect. Qua hai filter, container dispatch BookingServlet6.doGet; handler lấy c bằng context(req,true), đồng bộ c và gọi confirm(req,resp,c).

132. BookingServlet6.confirm đặt appointment=c.completed, completed=true, context và các thuộc tính giữ chỗ đã xóa vào request.

133. BookingServlet6.confirm gọi Web6.view(req, resp, "customer/ConfirmView6"); Web6 gán ctx/businessZone và forward cùng request đến /WEB-INF/views/customer/ConfirmView6.jsp.

134. ConfirmView6.jsp hiển thị “Đặt lịch hẹn thành công”, mã lịch, thời điểm tạo, trạng thái “Chờ tiếp nhận” từ PENDING và khách hàng/xe/ngày giờ/mô tả; chính trang xác nhận là trang kết quả thành công.

