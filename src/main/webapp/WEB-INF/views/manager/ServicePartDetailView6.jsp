<%@ page pageEncoding="UTF-8" %>
<%@ include file="../user/tags.jspf" %>
<c:set var="pageTitle" value="Chi tiết dịch vụ / phụ tùng" />
<%@ include file="../user/top.jspf" %>
<c:choose><c:when test="${itemType == 'SERVICE'}">
  <h1 class="bar light">THÔNG TIN CHI TIẾT DỊCH VỤ</h1>
  <dl class="details item-details"><dt>Mã dịch vụ</dt><dd><c:out value="${service.serviceCode}" /></dd><dt>Tên dịch vụ</dt><dd><c:out value="${service.serviceName}" /></dd><dt>Thông tin / mô tả</dt><dd><c:out value="${service.description}" /></dd><dt>Giá dịch vụ hiện tại</dt><dd><fmt:formatNumber value="${service.unitPrice}" maxFractionDigits="2" /> VNĐ</dd><dt>Số lần sử dụng toàn thời gian</dt><dd id="lifetimeUsageCount">${lifetimeUsageCount} lần</dd></dl>
  <h2 class="bar light">DANH SÁCH CÁC LẦN SỬ DỤNG DỊCH VỤ</h2>
  <div class="table-wrap"><table id="usageTable"><thead><tr><th>STT</th><th>Mã phiếu sửa chữa</th><th>Khách hàng</th><th>Biển số xe</th><th>Ngày hoàn thành</th><th>Thao tác</th></tr></thead><tbody>
    <c:forEach var="u" items="${serviceUsageList}" varStatus="s"><tr><td>${(currentPage - 1) * 20 + s.count}</td><td><c:out value="${u.order.repairOrderCode}" /></td><td><c:out value="${u.order.cus.fullName}" /></td><td><c:out value="${u.order.vehicle.licensePlate}" /></td><td><fmt:formatDate value="${u.order.completionTime}" pattern="dd/MM/yyyy HH:mm" /></td><td><a class="button small" href="<c:out value='${invoiceBaseUrl}' />&amp;usageId=${u.id}">Xem hóa đơn</a></td></tr></c:forEach>
    <c:if test="${empty serviceUsageList}"><tr><td class="empty" colspan="6">Không có lần sử dụng đã thanh toán trong khoảng thống kê.</td></tr></c:if>
  </tbody></table></div>
</c:when><c:otherwise>
  <h1 class="bar light">THÔNG TIN CHI TIẾT PHỤ TÙNG</h1>
  <div class="part-summary"><dl class="details item-details"><dt>Mã phụ tùng</dt><dd><c:out value="${sparePart.partCode}" /></dd><dt>Loại phụ tùng</dt><dd><c:out value="${sparePart.cat.categoryName}" /></dd><dt>Tên phụ tùng</dt><dd><c:out value="${sparePart.partName}" /></dd><dt>Thương hiệu</dt><dd><c:out value="${sparePart.brand.name}" /></dd><dt>Mô tả</dt><dd><c:out value="${sparePart.description}" /></dd><dt>Đơn vị</dt><dd><c:out value="${sparePart.unit}" /></dd><dt>Giá phụ tùng / đơn vị hiện tại</dt><dd><fmt:formatNumber value="${sparePart.unitPrice}" maxFractionDigits="2" /> VNĐ / <c:out value="${sparePart.unit}" /></dd><dt>Thông số kỹ thuật</dt><dd><c:out value="${sparePart.technicalSpecifications}" /></dd><dt>Số lần sử dụng toàn thời gian</dt><dd id="lifetimeUsageCount">${lifetimeUsageCount} lần</dd><dt>Tồn kho</dt><dd><c:out value="${sparePart.stockQuantity} ${sparePart.unit}" /></dd></dl><c:if test="${not empty partImageUrl}"><figure><img class="part-image" src="<c:out value='${partImageUrl}' />" alt="<c:out value='${sparePart.partName}' />"><figcaption>Hình minh họa phụ tùng</figcaption></figure></c:if></div>
  <h2 class="bar light">DANH SÁCH LẦN SỬ DỤNG PHỤ TÙNG</h2>
  <div class="table-wrap"><table id="usageTable"><thead><tr><th>STT</th><th>Mã phiếu sửa chữa</th><th>Khách hàng</th><th>Biển số xe</th><th>Ngày hoàn thành</th><th>Thao tác</th></tr></thead><tbody>
    <c:forEach var="u" items="${partUsageList}" varStatus="s"><tr><td>${(currentPage - 1) * 20 + s.count}</td><td><c:out value="${u.order.repairOrderCode}" /></td><td><c:out value="${u.order.cus.fullName}" /></td><td><c:out value="${u.order.vehicle.licensePlate}" /></td><td><fmt:formatDate value="${u.order.completionTime}" pattern="dd/MM/yyyy HH:mm" /></td><td><a class="button small" href="<c:out value='${invoiceBaseUrl}' />&amp;usageId=${u.id}">Xem hóa đơn</a></td></tr></c:forEach>
    <c:if test="${empty partUsageList}"><tr><td class="empty" colspan="6">Không có lần sử dụng đã thanh toán trong khoảng thống kê.</td></tr></c:if>
  </tbody></table></div>
</c:otherwise></c:choose>
<p class="muted">Khoảng thống kê: <c:out value="${startDate}" /> → <c:out value="${endDate}" /> · Chỉ hóa đơn đã thanh toán.</p>
<%@ include file="../user/pagination.jspf" %>
<a class="button secondary" href="<c:out value='${backUrl}' />">← Quay lại danh sách</a>
<%@ include file="../user/bottom.jspf" %>
