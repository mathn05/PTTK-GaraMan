<%@ page pageEncoding="UTF-8" %>
<%@ include file="../shared/tags.jspf" %>
<c:set var="pageTitle" value="Đăng nhập" />
<%@ include file="../shared/top.jspf" %>
<section class="login-card">
  <div class="section-label">GARAMAN AUTO SERVICE</div>
  <h1>ĐĂNG NHẬP</h1>
  <p class="muted">Chào mừng bạn đến với GaraMan.</p>
  <form action="${ctx}/login" method="post" class="stack">
    <input type="hidden" name="action" value="login"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <label for="username">Tên đăng nhập <span class="required">*</span></label>
    <input id="username" name="username" autocomplete="username" maxlength="50" value="<c:out value='${param.username}' />" required autofocus>
    <label for="password">Mật khẩu <span class="required">*</span></label>
    <input id="password" name="password" type="password" autocomplete="current-password" maxlength="1024" required>
    <button class="button" type="submit" name="btnLogin">Đăng nhập →</button>
  </form>
</section>
<%@ include file="../shared/bottom.jspf" %>
