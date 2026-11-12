<%@ page pageEncoding="UTF-8" %>
<%@ include file="../user/tags.jspf" %>
<c:set var="pageTitle" value="Chi tiết hóa đơn" />
<%@ include file="../user/top.jspf" %>
<h1 class="bar light center">HÓA ĐƠN THANH TOÁN CHI TIẾT</h1>
<div class="invoice-meta">
  <p><strong>Mã hóa đơn:</strong> <c:out value="${invoice.invoiceCode}" /></p><p><strong>Ngày phát hành:</strong> <fmt:formatDate value="${invoice.issueDate}" pattern="dd/MM/yyyy HH:mm:ss" /></p><p><strong>Phiếu:</strong> <c:out value="${invoice.order.repairOrderCode}" /></p>
  <p><strong>Khách hàng:</strong> <c:out value="${invoice.order.cus.fullName}" /></p><p><strong>Biển số:</strong> <c:out value="${invoice.order.vehicle.licensePlate}" /></p><p><strong>Xe:</strong> <c:out value="${invoice.order.vehicle.brand.name} ${invoice.order.vehicle.vehicleModel}" /></p>
  <p id="saleStaff"><strong>Nhân viên bán hàng:</strong> <c:out value="${invoice.order.ss.fullName}" default="Chưa phân công" /> <c:out value="${invoice.order.ss.employeeCode}" /></p><p><strong>Điện thoại khách:</strong> <c:out value="${invoice.order.cus.phone}" /></p><p><strong>Hoàn thành:</strong> <fmt:formatDate value="${invoice.order.completionTime}" pattern="dd/MM/yyyy HH:mm" /></p>
</div>
<h2 class="bar light">CHI TIẾT DỊCH VỤ / PHỤ TÙNG SỬ DỤNG</h2>
<div class="table-wrap"><table id="invoiceLines"><thead><tr><th>Mã</th><th>Dịch vụ / phụ tùng</th><th>Đơn vị</th><th class="numeric">SL / đơn giá (VNĐ)</th><th class="numeric">Thành tiền (VNĐ)</th></tr></thead><tbody>
  <c:forEach var="u" items="${invoice.order.serviceUsage}"><tr data-type="SERVICE"><td><c:out value="${u.service.serviceCode}" /></td><td><c:out value="${u.service.serviceName}" /></td><td>Lần</td><td class="numeric">1 × <fmt:formatNumber value="${u.saleUnitPrice}" maxFractionDigits="2" /></td><td class="numeric"><fmt:formatNumber value="${u.saleUnitPrice}" maxFractionDigits="2" /></td></tr></c:forEach>
  <c:forEach var="u" items="${invoice.order.partUsage}"><tr data-type="PART"><td><c:out value="${u.sparePart.partCode}" /></td><td><c:out value="${u.sparePart.partName}" /></td><td><c:out value="${u.sparePart.unit}" /></td><td class="numeric">${u.usedQuantity} × <fmt:formatNumber value="${u.saleUnitPrice}" maxFractionDigits="2" /></td><td class="numeric"><fmt:formatNumber value="${u.lineAmount}" maxFractionDigits="2" /></td></tr></c:forEach>
</tbody><tfoot><tr><td colspan="4">TỔNG THANH TOÁN</td><td id="invoiceTotal" class="numeric money" data-value="${invoice.totalAmount}"><fmt:formatNumber value="${invoice.totalAmount}" maxFractionDigits="2" /> VNĐ</td></tr></tfoot></table></div>
<section class="payment-summary">
<c:choose><c:when test="${not empty invoice.payment}">
  <p><strong>Trạng thái:</strong> <span class="paid">Đã thanh toán</span></p>
  <p><strong>Hình thức thanh toán:</strong> ${invoice.payment.paymentMethod == 'BANK_TRANSFER' ? 'Chuyển khoản' : invoice.payment.paymentMethod == 'CASH' ? 'Tiền mặt' : ''}<c:if test="${invoice.payment.paymentMethod != 'BANK_TRANSFER' && invoice.payment.paymentMethod != 'CASH'}"><c:out value="${invoice.payment.paymentMethod}" /></c:if></p>
  <p><strong>Ngày thanh toán:</strong> <fmt:formatDate value="${invoice.payment.paymentTime}" pattern="dd/MM/yyyy HH:mm:ss" /></p><p><strong>Số tiền:</strong> <fmt:formatNumber value="${invoice.payment.paymentAmount}" maxFractionDigits="2" /> VNĐ</p>
  <p id="cashier"><strong>Nhân viên thu tiền:</strong> <c:out value="${invoice.payment.ss.fullName}" /></p><p><strong>Mã giao dịch:</strong> <c:out value="${invoice.payment.transactionReference}" /></p>
</c:when><c:otherwise><p>Hóa đơn chưa thanh toán.</p></c:otherwise></c:choose>
</section>
<a class="button secondary" href="<c:out value='${backUrl}' />">← Quay lại chi tiết</a>
<%@ include file="../user/bottom.jspf" %>
