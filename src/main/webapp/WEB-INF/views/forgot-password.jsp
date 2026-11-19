<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html><html lang="vi"><head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quên mật khẩu | Web Homework</title>
    <c:url value="/assets/styles.css" var="stylesUrl"/><link rel="stylesheet" href="${stylesUrl}">
</head><body>
<main class="container narrow"><section class="card">
    <p class="eyebrow">Khôi phục tài khoản</p><h1>Quên mật khẩu</h1>
    <p class="muted">Nhập email đã kích hoạt. Mã OTP sẽ có hiệu lực trong 10 phút.</p>
    <c:if test="${not empty error}"><div class="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty message}"><div class="alert success"><c:out value="${message}"/></div></c:if>
    <c:url value="/forgot-password" var="forgotUrl"/>
    <form action="${forgotUrl}" method="post" class="needs-validation" novalidate>
        <div class="field"><label for="email">Email</label><input id="email" name="email" type="email" required maxlength="255" autocomplete="email" value="${fn:escapeXml(email)}" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div></div>
        <button class="button" type="submit">Gửi mã OTP</button>
    </form>
    <c:if test="${sent}">
        <p class="footer-note"><c:url value="/reset-password" var="resetUrl"><c:param name="email" value="${email}"/></c:url><a href="${resetUrl}">Nhập OTP và đặt mật khẩu mới</a></p>
    </c:if>
</section></main></body></html>
