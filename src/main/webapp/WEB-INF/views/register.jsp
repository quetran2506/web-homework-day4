<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng ký | Web Homework</title>
    <c:url value="/assets/styles.css" var="stylesUrl" /><link rel="stylesheet" href="${stylesUrl}">
</head>
<body>
<main class="container narrow">
    <section class="card">
        <p class="eyebrow">Bài 1 · Tạo tài khoản</p>
        <h1>Đăng ký</h1>
        <p class="muted">Mật khẩu được băm trước khi lưu. Sau khi đăng ký, nhập mã OTP trong email để kích hoạt tài khoản.</p>
        <c:if test="${not empty error}"><div class="alert" role="alert"><c:out value="${error}" /></div></c:if>
        <c:url value="/register" var="registerUrl" />
        <form action="${registerUrl}" method="post" class="needs-validation" novalidate>
            <div class="field"><label for="fullName">Họ và tên</label><input id="fullName" name="fullName" type="text" required maxlength="120" autocomplete="name" value="${fn:escapeXml(param.fullName)}" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div></div>
            <div class="field"><label for="email">Email</label><input id="email" name="email" type="email" required maxlength="255" autocomplete="email" value="${fn:escapeXml(param.email)}" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div></div>
            <div class="field"><label for="username">Tên đăng nhập</label><input id="username" name="username" type="text" required minlength="3" maxlength="64" pattern="[A-Za-z0-9_.-]{3,64}" title="Dùng 3-64 ký tự chữ, số, dấu chấm, gạch dưới hoặc gạch ngang." autocomplete="username" value="${fn:escapeXml(param.username)}" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div></div>
            <div class="field"><label for="phone">Số điện thoại (không bắt buộc)</label><input id="phone" name="phone" type="tel" maxlength="32" pattern="[0-9+(). -]{7,32}" title="Nhập số điện thoại có 7-15 chữ số, có thể kèm dấu +, ngoặc, dấu chấm hoặc khoảng trắng." autocomplete="tel" value="${fn:escapeXml(param.phone)}" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div></div>
            <div class="field"><label for="password">Mật khẩu</label><input id="password" name="password" type="password" required minlength="8" maxlength="128" autocomplete="new-password" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div></div>
            <div class="field"><label for="confirmPassword">Nhập lại mật khẩu</label><input id="confirmPassword" name="confirmPassword" type="password" data-match-password="password" required minlength="8" maxlength="128" autocomplete="new-password" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div></div>
            <div class="actions"><button class="button" type="submit">Tạo tài khoản</button><a class="button secondary" href="<c:url value='/login' />">Quay lại đăng nhập</a></div>
        </form>
    </section>
</main>
</body>
</html>
