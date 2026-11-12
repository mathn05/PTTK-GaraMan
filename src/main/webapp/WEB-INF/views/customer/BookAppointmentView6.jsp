<%@ page pageEncoding="UTF-8" %>
<%@ include file="../user/tags.jspf" %>
<c:set var="pageTitle" value="Đặt lịch hẹn" />
<%@ include file="../user/top.jspf" %>
<h1>ĐẶT LỊCH HẸN SỬA CHỮA</h1>
<form action="${ctx}/booking" method="post" id="bookingForm">
  <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="context" value="${context}">
  <div class="two-column booking-grid">
    <section><h2 class="bar">1. NGÀY VÀ GIỜ HẸN</h2>
      <label for="appointmentDate">Ngày hẹn <span class="required">*</span></label>
      <input id="appointmentDate" name="appointmentDate" type="date" value="${appointmentDate}" min="${minDate}" max="${maxDate}" required>
      <button class="button secondary small" type="submit" formmethod="get" name="action" value="slots" formaction="${ctx}/booking" formnovalidate data-load-slots>Xem khung giờ</button>
      <fieldset class="slot-fieldset"><legend>Khung giờ hẹn <span class="required">*</span> <span class="muted">(chọn một giờ còn trống)</span></legend>
        <div class="slot-grid">
          <c:forEach var="slot" items="${availableSlots}"><label class="slot"><input type="radio" name="appointmentSlot" value="${slot.id}" ${selectedSlot == slot.id ? 'checked' : ''} required><span><fmt:formatDate value="${slot.startTime}" pattern="HH:mm" /></span></label></c:forEach>
        </div>
        <c:if test="${empty availableSlots}"><p class="empty">Ngày này không còn khung giờ có thể đặt. Vui lòng chọn ngày khác.</p></c:if>
      </fieldset>
      <div class="info-box"><strong>THÔNG TIN GARA</strong><br>Chọn khung giờ còn trống được hệ thống hiển thị theo lịch làm việc hiện tại.<br>Khung giờ được giữ trong 3 phút ở bước xác nhận.</div>
    </section>
    <section><h2 class="bar">2. THÔNG TIN KHÁCH HÀNG</h2>
      <label for="fullName">Họ và tên</label><input id="fullName" name="fullName" value="<c:out value='${cus.fullName}' />" readonly>
      <label for="phone">Số điện thoại</label><input id="phone" name="phone" value="<c:out value='${cus.phone}' />" readonly>
      <label for="email">Email</label><input id="email" name="email" value="<c:out value='${cus.email}' />" readonly>
      <label for="vehicle">Xe cần sửa chữa / bảo dưỡng <span class="required">*</span></label>
      <select id="vehicle" name="vehicle" required><option value="">Chọn xe đã lưu</option><c:forEach var="v" items="${vehicleList}"><option value="${v.id}" ${selectedVehicle == v.id ? 'selected' : ''}><c:out value="${v.brand.name} ${v.vehicleModel} — ${v.licensePlate}" /></option></c:forEach></select>
      <button class="button secondary full" type="submit" name="action" value="addVehicle" formnovalidate>＋ THÊM XE MỚI</button>
      <label for="requestDescription">Nội dung yêu cầu sửa chữa <span class="required">*</span></label>
      <textarea id="requestDescription" name="requestDescription" rows="4" maxlength="255" required placeholder="Nhập mô tả lỗi / dịch vụ mong muốn..."><c:out value="${requestDescription}" /></textarea>
    </section>
  </div>
  <div class="actions"><a class="button secondary" href="${ctx}/booking?action=home">← QUAY LẠI</a><button class="button" type="submit" name="action" value="hold">XÁC NHẬN →</button></div>
</form>
<%@ include file="../user/bottom.jspf" %>
