<%@ page pageEncoding="UTF-8" %>
<%@ include file="../user/tags.jspf" %>
<c:set var="pageTitle" value="Xác nhận lịch hẹn" />
<%@ include file="../user/top.jspf" %>
<section class="narrow">
  <c:choose>
    <c:when test="${completed}">
      <div class="success-banner">
        <h1>ĐẶT LỊCH HẸN THÀNH CÔNG</h1>
        <p>Mã lịch hẹn: <strong id="appointmentCode"><c:out value="${appointment.appointmentCode}" /></strong> · <span class="badge">Chờ tiếp nhận</span></p>
      </div>
    </c:when>
    <c:otherwise>
      <h1>XÁC NHẬN LỊCH HẸN</h1>
    </c:otherwise>
  </c:choose>
  <h2 class="bar">CHI TIẾT YÊU CẦU ĐẶT LỊCH</h2>
  <dl class="details">
    <dt>Ngày hẹn</dt>
    <dd><fmt:formatDate value="${appointment.slot.startTime}" pattern="dd/MM/yyyy" /></dd>
    <dt>Khung giờ hẹn</dt>
    <dd><fmt:formatDate value="${appointment.slot.startTime}" pattern="HH:mm" /></dd>
    <dt>Khách hàng</dt>
    <dd><c:out value="${appointment.cus.fullName}" /></dd>
    <dt>Số điện thoại</dt>
    <dd><c:out value="${appointment.cus.phone}" /></dd>
    <dt>Email</dt>
    <dd><c:out value="${appointment.cus.email}" default="Chưa có" /></dd>
    <dt>Xe</dt>
    <dd><c:out value="${appointment.vehicle.brand.name} ${appointment.vehicle.vehicleModel} — ${appointment.vehicle.licensePlate}" /></dd>
    <dt>Nội dung yêu cầu</dt>
    <dd class="multiline"><c:out value="${appointment.requestDescription}" /></dd>
    <c:if test="${completed}">
      <dt>Thời điểm tạo</dt>
      <dd><fmt:formatDate value="${appointment.createAt}" pattern="dd/MM/yyyy HH:mm:ss" /></dd>
      <dt>Trạng thái</dt>
      <dd>Chờ tiếp nhận</dd>
    </c:if>
  </dl>
  <c:choose>
    <c:when test="${completed}">
      <div class="center">
        <a class="button" href="${ctx}/booking?action=home">VỀ TRANG CHỦ</a>
      </div>
    </c:when>
    <c:otherwise>
      <div class="notice" id="holdTimer" data-remaining="${holdRemainingMillis}">
        Khung giờ đang được giữ đến <strong><fmt:formatDate value="${holdExpiresAt}" pattern="HH:mm:ss dd/MM/yyyy" /></strong>. Còn <strong data-countdown>03:00</strong> để xác nhận.
      </div>
      <div class="actions">
        <form action="${ctx}/booking" method="post">
          <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
          <input type="hidden" name="context" value="${context}">
          <input type="hidden" name="action" value="edit"><button class="button secondary" type="submit">← CHỈNH SỬA</button>
        </form>
        <form action="${ctx}/appointment" method="post">
          <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
          <input type="hidden" name="context" value="${context}"><button class="button" id="confirmBooking" type="submit">XÁC NHẬN ĐẶT LỊCH →</button>
        </form>
      </div>
    </c:otherwise>
  </c:choose>
</section>
<%@ include file="../user/bottom.jspf" %>
