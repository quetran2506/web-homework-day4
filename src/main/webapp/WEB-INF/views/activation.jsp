<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html><html lang="vi"><head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kích hoạt tài khoản | Web Homework</title>
    <c:url value="/assets/styles.css" var="stylesUrl"/><link rel="stylesheet" href="${stylesUrl}">
</head><body>
<main class="container narrow"><section class="card">
    <p class="eyebrow">Xác minh email</p><h1>Kích hoạt tài khoản</h1>
    <p class="muted">Nhập mã OTP 6 chữ số đã gửi đến email. Mã có hiệu lực 10 phút.</p>
    <c:if test="${not empty error}"><div class="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty message}"><div class="alert success"><c:out value="${message}"/></div></c:if>
    <c:if test="${empty email and empty error}"><div class="alert">Nhập email đã dùng khi đăng ký.</div></c:if>
    <c:url value="/activation" var="activationUrl"/>
    <form action="${activationUrl}" method="post">
        <div class="field"><label for="email">Email</label><input id="email" name="email" type="email" required maxlength="255" autocomplete="email" value="${fn:escapeXml(email)}"></div>
        <div class="field"><label for="code">Mã OTP</label><input id="code" name="code" type="text" inputmode="numeric" pattern="[0-9]{6}" minlength="6" maxlength="6" autocomplete="one-time-code"></div>
        <div class="actions">
            <button class="button" type="submit" name="action" value="verify">Xác minh</button>
            <button class="button secondary" type="submit" name="action" value="resend" formnovalidate>Gửi lại OTP</button>
        </div>
    </form>
</section></main></body></html>