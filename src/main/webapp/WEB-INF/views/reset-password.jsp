<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html><html lang="vi"><head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đặt lại mật khẩu | Web Homework</title>
    <c:url value="/assets/styles.css" var="stylesUrl"/><link rel="stylesheet" href="${stylesUrl}">
</head><body>
<main class="container narrow"><section class="card">
    <p class="eyebrow">Khôi phục tài khoản</p><h1>Đặt mật khẩu mới</h1>
    <p class="muted">Nhập OTP trong email và đặt mật khẩu mới. Mã chỉ sử dụng một lần.</p>
    <c:if test="${not empty error}"><div class="alert"><c:out value="${error}"/></div></c:if>
    <c:url value="/reset-password" var="resetUrl"/>
    <form action="${resetUrl}" method="post" class="needs-validation" novalidate>
        <div class="field"><label for="email">Email</label><input id="email" name="email" type="email" required maxlength="255" value="${fn:escapeXml(email)}" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div></div>
        <div class="field"><label for="code">Mã OTP</label><input id="code" name="code" type="text" inputmode="numeric" pattern="[0-9]{6}" minlength="6" maxlength="6" autocomplete="one-time-code" required class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div></div>
        <div class="field"><label for="password">Mật khẩu mới</label><input id="password" name="password" type="password" minlength="8" maxlength="128" autocomplete="new-password" required class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div></div>
        <div class="field"><label for="confirmPassword">Nhập lại mật khẩu mới</label><input id="confirmPassword" name="confirmPassword" type="password" data-match-password="password" minlength="8" maxlength="128" autocomplete="new-password" required class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div></div>
        <button class="button" type="submit">Đổi mật khẩu</button>
    </form>
</section></main></body></html>
