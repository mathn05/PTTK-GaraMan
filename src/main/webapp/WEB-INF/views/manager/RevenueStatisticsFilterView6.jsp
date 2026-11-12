<%@ page pageEncoding="UTF-8" %>
<%@ include file="../user/tags.jspf" %>
<c:set var="pageTitle" value="Bộ lọc thống kê doanh thu" />
<%@ include file="../user/top.jspf" %>
<section class="report-panel narrow">
  <h1 class="bar light">BỘ LỌC THỐNG KÊ DOANH THU</h1>
  <form action="${ctx}/revenue" method="get" class="filter-form">
    <input type="hidden" name="action" value="list">
    <div class="form-row">
      <label for="startDate">Ngày bắt đầu <span class="required">*</span></label>
      <input id="startDate" name="startDate" type="date" value="<c:out value='${startDate}' />" required>
    </div>
    <div class="form-row">
      <label for="endDate">Ngày kết thúc <span class="required">*</span></label>
      <input id="endDate" name="endDate" type="date" value="<c:out value='${endDate}' />" required>
    </div>
    <div class="form-row">
      <label for="statisticType">Loại thống kê</label>
      <select id="statisticType" name="statisticType">
        <option value="ALL" ${statisticType == 'ALL' ? 'selected' : ''}>Tất cả</option>
        <option value="SERVICE" ${statisticType == 'SERVICE' ? 'selected' : ''}>Dịch vụ</option>
        <option value="PART" ${statisticType == 'PART' ? 'selected' : ''}>Phụ tùng</option>
      </select>
    </div>
    <p class="muted">Doanh thu theo ngày phát hành hóa đơn đã thanh toán, bao gồm cả hai ngày đã chọn.</p>
    <div class="actions">
      <a class="button secondary" href="${ctx}/revenue?action=menu">← Quay lại</a>
      <button class="button" type="submit" name="btnConfirm">THỐNG KÊ →</button>
    </div>
  </form>
</section>
<%@ include file="../user/bottom.jspf" %>
