<%@ page pageEncoding="UTF-8" %>
<%@ include file="../user/tags.jspf" %>
<c:set var="pageTitle" value="Kết quả thống kê doanh thu" />
<%@ include file="../user/top.jspf" %>
<h1 class="bar light">KẾT QUẢ THỐNG KÊ DỊCH VỤ / PHỤ TÙNG</h1>
<div class="report-caption"><span><c:out value="${startDate}" /> → <c:out value="${endDate}" /></span><span>Loại: ${statisticType == 'SERVICE' ? 'Dịch vụ' : statisticType == 'PART' ? 'Phụ tùng' : 'Tất cả'} · Hóa đơn đã thanh toán</span></div>
<div class="table-wrap"><table id="revenueTable"><thead><tr><th>Mã DV/PT</th><th>Tên dịch vụ / phụ tùng</th><th class="numeric">Số lần sử dụng</th><th class="numeric">Tổng doanh thu (VNĐ)</th></tr></thead><tbody>
  <c:forEach var="r" items="${revenueStats}"><c:url var="detailUrl" value="/service-part"><c:param name="itemType" value="${r.itemType}" /><c:param name="itemCode" value="${r.itemCode}" /><c:param name="startDate" value="${startDate}" /><c:param name="endDate" value="${endDate}" /><c:param name="statisticType" value="${statisticType}" /><c:param name="page" value="${currentPage}" /></c:url>
    <tr data-item-type="${r.itemType}" data-item-code="<c:out value='${r.itemCode}' />"><td><a class="item-link" href="<c:out value='${detailUrl}' />"><c:out value="${r.itemCode}" /> ↗</a><small class="type-label">${r.itemType == 'SERVICE' ? 'Dịch vụ' : 'Phụ tùng'}</small></td><td><c:out value="${r.itemName}" /></td><td class="numeric">${r.usageCount}</td><td class="numeric money"><fmt:formatNumber value="${r.totalRevenue}" minFractionDigits="0" maxFractionDigits="2" /></td></tr>
  </c:forEach>
  <c:if test="${empty revenueStats}"><tr><td colspan="4" class="empty">Không có dữ liệu trong khoảng thời gian đã chọn.</td></tr></c:if>
</tbody><tfoot><tr><td colspan="3">Tổng doanh thu toàn bộ kết quả</td><td class="numeric money" id="totalRevenue" data-value="${totalRevenue}"><fmt:formatNumber value="${totalRevenue}" minFractionDigits="0" maxFractionDigits="2" /> VNĐ</td></tr></tfoot></table></div>
<%@ include file="../user/pagination.jspf" %>
<a class="button secondary" href="<c:out value='${backUrl}' />">← Quay lại bộ lọc</a>
<%@ include file="../user/bottom.jspf" %>
