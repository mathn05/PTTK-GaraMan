<%@ page pageEncoding="UTF-8" %>
<%@ include file="../user/tags.jspf" %>
<c:set var="pageTitle" value="Thêm xe mới" />
<%@ include file="../user/top.jspf" %>
<section class="narrow">
  <h1>THÊM XE MỚI</h1>
  <p class="center muted">Nhập thông tin xe để lưu và chọn trong biểu mẫu đặt lịch hẹn.</p>
  <h2 class="bar light">THÔNG TIN PHƯƠNG TIỆN</h2>
  <form action="${ctx}/vehicle" method="post">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="context" value="${context}">
    <div class="form-row">
      <label for="vehicleBrand">Hãng xe <span class="required">*</span></label>
      <select id="vehicleBrand" name="vehicleBrand" required>
        <option value="">Chọn hãng xe</option>
        <c:forEach var="b" items="${vehicleBrandList}">
          <option value="${b.id}" ${selectedBrandId == b.id ? 'selected' : ''}><c:out value="${b.name}" /></option>
        </c:forEach>
      </select>
    </div>
    <div class="form-row">
      <label for="vehicleModel">Dòng xe <span class="required">*</span></label>
      <input id="vehicleModel" name="vehicleModel" maxlength="255" value="<c:out value='${vehicleModel}' />" placeholder="Nhập dòng xe" required>
    </div>
    <div class="form-row">
      <label for="licensePlate">Biển số xe <span class="required">*</span></label>
      <input id="licensePlate" name="licensePlate" maxlength="30" value="<c:out value='${licensePlate}' />" placeholder="Ví dụ: 30A-123.45" required>
    </div>
    <div class="form-row">
      <label for="manufactureYear">Năm sản xuất</label>
      <input id="manufactureYear" name="manufactureYear" type="number" min="1886" max="${maxYear}" value="<c:out value='${manufactureYear}' />" placeholder="Ví dụ: 2022">
    </div>
    <p class="muted">(*) Thông tin bắt buộc</p>
    <div class="actions">
      <a class="button secondary" href="${ctx}/booking?context=${context}">← Quay lại</a>
      <button class="button" type="submit">Lưu thông tin xe</button>
    </div>
  </form>
</section>
<%@ include file="../user/bottom.jspf" %>
