<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập | Web Homework</title>
    <c:url value="/assets/styles.css" var="stylesUrl" /><link rel="stylesheet" href="${stylesUrl}">
</head>
<body>
<main class="container narrow">
    <section class="card">
        <p class="eyebrow">Bài 1 · Đăng nhập</p>
        <h1>Đăng nhập</h1>
        <p class="muted">Mặc định dùng Session cho phiên hiện tại. Chọn Cookie để tự đăng nhập lại trong 30 ngày.</p>
        <c:if test="${not empty error}"><div class="alert" role="alert"><c:out value="${error}" /></div></c:if>
        <c:if test="${param.registered eq '1'}"><div class="alert success">Đăng ký thành công. Bạn có thể đăng nhập.</div></c:if>
        <c:if test="${param.loggedOut eq '1'}"><div class="alert success">Bạn đã đăng xuất.</div></c:if>
        <c:if test="${param.activated eq '1'}"><div class="alert success">Tài khoản đã được kích hoạt. Bạn có thể đăng nhập.</div></c:if>
        <c:if test="${param.passwordReset eq '1'}"><div class="alert success">Mật khẩu đã được đổi. Hãy đăng nhập lại.</div></c:if>
        <c:url value="/login" var="loginUrl" />
        <form action="${loginUrl}" method="post" class="needs-validation" novalidate>
            <c:if test="${param.next eq 'category'}"><input type="hidden" name="next" value="category"></c:if>
            <div class="field">
                <label for="username">Tên đăng nhập</label>
                <input id="username" type="text" name="username" autocomplete="username" required minlength="3" maxlength="64" pattern="[A-Za-z0-9_.-]{3,64}" title="Dùng 3-64 ký tự chữ, số, dấu chấm, gạch dưới hoặc gạch ngang." value="${fn:escapeXml(param.username)}" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div>
            </div>
            <div class="field">
                <label for="password">Mật khẩu</label>
                <input id="password" type="password" name="password" autocomplete="current-password" required minlength="8" maxlength="128" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div>
            </div>
            <div class="check-row">
                <input id="remember" type="checkbox" name="remember">
                <label for="remember">Ghi nhớ đăng nhập bằng Cookie trong 30 ngày trên thiết bị này.</label>
            </div>
            <button class="button" type="submit">Đăng nhập</button>
        </form>
        <c:if test="${not empty activationEmail}">
            <c:url value="/activation" var="activateUrl"><c:param name="email" value="${activationEmail}" /></c:url>
            <p class="footer-note"><a href="${activateUrl}">Mở trang nhập OTP kích hoạt tài khoản</a></p>
        </c:if>
        <p class="footer-note"><c:url value="/forgot-password" var="forgotUrl" /><a href="${forgotUrl}">Quên mật khẩu?</a></p>
        <p class="footer-note">Chưa có tài khoản? <c:url value="/register" var="registerUrl" /><a href="${registerUrl}">Đăng ký</a></p>
    </section>
</main>
</body>
</html>
