<%@ page pageEncoding="UTF-8" %>
<%@ include file="../shared/tags.jspf" %>
<c:set var="pageTitle" value="Xem báo cáo" />
<%@ include file="../shared/top.jspf" %>
<section class="report-panel narrow"><h1 class="panel-title">XEM BÁO CÁO</h1><div class="menu-options">
  <a class="button teal" href="${ctx}/revenue?action=filter">Thống kê dịch vụ / phụ tùng theo doanh thu →</a>
  <button class="button teal" disabled>Thống kê khách hàng · Chưa triển khai</button>
</div><a class="button secondary" href="${ctx}/revenue?action=home">← Quay lại</a></section>
<%@ include file="../shared/bottom.jspf" %>
