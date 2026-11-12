<%@ page pageEncoding="UTF-8" %>
<%@ include file="user/tags.jspf" %>
<c:set var="pageTitle" value="Không thể xử lý yêu cầu" />
<%@ include file="user/top.jspf" %>
<section class="narrow center"><h1>Yêu cầu chưa được thực hiện</h1><p class="muted">Mã phản hồi: <c:out value="${errorStatus}" /></p><a class="button" href="${ctx}/login">Về trang chủ</a></section>
<%@ include file="user/bottom.jspf" %>
