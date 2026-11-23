<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Hồ sơ cá nhân | Web Homework Day 4</title>
    <c:url value="/assets/styles.css" var="stylesUrl" /><link rel="stylesheet" href="${stylesUrl}">
</head>
<body>
<main class="container py-4 py-lg-5" style="max-width: 920px">
    <section class="card border-0 shadow-sm rounded-4 p-3 p-md-5">
        <div class="d-flex align-items-center gap-3 mb-4">
            <c:choose>
                <c:when test="${not empty user.profileImageUrl}">
                    <img src="${fn:escapeXml(user.profileImageUrl)}" alt="Ảnh đại diện"
                         class="rounded-circle border" width="88" height="88" style="object-fit: cover">
                </c:when>
                <c:otherwise>
                    <div class="rounded-circle bg-success-subtle text-success d-flex align-items-center justify-content-center fw-bold fs-2"
                         style="width: 88px; height: 88px" aria-hidden="true">
                        <c:out value="${fn:substring(user.fullName, 0, 1)}" />
                    </div>
                </c:otherwise>
            </c:choose>
            <div>
                <p class="text-uppercase fw-bold text-success small mb-1">Tài khoản của bạn</p>
                <h1 class="h2 mb-1">Hồ sơ cá nhân</h1>
                <p class="text-secondary mb-0">Tên đăng nhập: <c:out value="${user.username}" /></p>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert"><c:out value="${error}" /></div>
        </c:if>
        <c:if test="${not empty message}">
            <div class="alert alert-success" role="status"><c:out value="${message}" /></div>
        </c:if>

        <section class="border rounded-3 p-3 p-md-4 mb-4">
            <h2 class="h4">Thông tin cá nhân</h2>
            <p class="text-secondary">Cập nhật họ tên, số điện thoại và ảnh đại diện.</p>
            <c:url value="/profile" var="profileUrl" />
            <form action="${profileUrl}" method="post" enctype="multipart/form-data"
                  class="needs-validation" novalidate>
                <input type="hidden" name="action" value="updateProfile">
                <div class="mb-3">
                    <label class="form-label fw-semibold" for="fullName">Họ và tên</label>
                    <input class="form-control" id="fullName" name="fullName" type="text" required
                           maxlength="120" autocomplete="name" value="${fn:escapeXml(profileFullName)}">
                    <div class="invalid-feedback">Nhập họ tên, tối đa 120 ký tự.</div>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-semibold" for="phone">Số điện thoại</label>
                    <input class="form-control" id="phone" name="phone" type="tel" maxlength="32"
                           pattern="[0-9+(). -]{7,32}" autocomplete="tel"
                           value="${fn:escapeXml(profilePhone)}">
                    <div class="form-text">Có thể để trống; nếu nhập, số điện thoại cần có 7–15 chữ số.</div>
                    <div class="invalid-feedback">Số điện thoại chưa đúng định dạng.</div>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-semibold" for="avatar">Ảnh đại diện</label>
                    <input class="form-control" id="avatar" name="avatar" type="file"
                           accept="image/jpeg,image/png,image/webp,image/gif">
                    <div class="form-text">JPG, PNG, WebP hoặc GIF; tối đa 5 MB. Ảnh được tải lên Cloudinary.</div>
                </div>
                <button class="btn btn-success" type="submit">Lưu hồ sơ</button>
            </form>
        </section>

        <section class="border rounded-3 p-3 p-md-4">
            <h2 class="h4">Đổi email</h2>
            <p class="text-secondary">Email chỉ được đổi sau khi nhập OTP gửi đến địa chỉ email mới.</p>
            <div class="mb-3"><span class="fw-semibold">Email hiện tại:</span>
                <c:out value="${user.email}" />
            </div>
            <c:if test="${not empty user.pendingEmail}">
                <div class="alert alert-info" role="status">
                    Đang chờ xác nhận email mới: <strong><c:out value="${user.pendingEmail}" /></strong>
                </div>
            </c:if>
            <form action="${profileUrl}" method="post" class="needs-validation mb-4" novalidate>
                <input type="hidden" name="action" value="requestEmailChange">
                <div class="mb-3">
                    <label class="form-label fw-semibold" for="newEmail">Email mới</label>
                    <input class="form-control" id="newEmail" name="newEmail" type="email" required
                           maxlength="255" autocomplete="email" value="${fn:escapeXml(newEmail)}">
                    <div class="invalid-feedback">Nhập một địa chỉ email hợp lệ.</div>
                </div>
                <button class="btn btn-outline-success" type="submit">Gửi OTP đến email mới</button>
            </form>

            <c:if test="${not empty user.pendingEmail}">
                <form action="${profileUrl}" method="post" class="needs-validation" novalidate>
                    <input type="hidden" name="action" value="confirmEmailChange">
                    <div class="mb-3">
                        <label class="form-label fw-semibold" for="code">Mã OTP 6 chữ số</label>
                        <input class="form-control" id="code" name="code" type="text" required
                               inputmode="numeric" autocomplete="one-time-code" maxlength="6"
                               pattern="[0-9]{6}">
                        <div class="form-text">Mã có hiệu lực 10 phút và chỉ sử dụng được một lần.</div>
                        <div class="invalid-feedback">Nhập OTP gồm đúng 6 chữ số.</div>
                    </div>
                    <button class="btn btn-success" type="submit">Xác nhận email mới</button>
                </form>
            </c:if>
        </section>
    </section>
</main>
</body>
</html>
