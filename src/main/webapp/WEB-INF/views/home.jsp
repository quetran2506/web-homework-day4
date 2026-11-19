<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html><html lang="vi"><head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Trang chủ | Web Homework Day 4</title>
    <c:url value="/assets/styles.css" var="stylesUrl"/><link rel="stylesheet" href="${stylesUrl}">
</head><body>
<main class="container">
    <section class="card intro-card">
        <p class="eyebrow">Lập trình Web · Java MVC · JPA</p>
        <c:choose>
            <c:when test="${sessionScope.account != null}"><h1>Chào <c:out value="${sessionScope.account.fullName}"/>!</h1></c:when>
            <c:otherwise><h1>Thư viện video</h1></c:otherwise>
        </c:choose>
        <p class="muted">Tài khoản có xác minh OTP qua email, đăng nhập bằng mật khẩu đã băm và quản lý video cá nhân.</p>
        <div class="actions">
            <c:url value="/video" var="allVideosUrl"/><a class="button" href="${allVideosUrl}">Xem tất cả video</a>
            <c:if test="${sessionScope.account != null}"><c:url value="/video/manage" var="manageUrl"/><a class="button secondary" href="${manageUrl}">Quản lý video của tôi</a></c:if>
        </div>
    </section>
    <section class="section-block">
        <div class="section-heading"><div><p class="eyebrow">Mới nhất</p><h2>10 video gần đây</h2></div></div>
        <c:if test="${not empty videoError}"><div class="alert"><c:out value="${videoError}"/></div></c:if>
        <div class="video-grid">
            <c:forEach items="${latestVideos}" var="video">
                <article class="video-card">
                    <c:if test="${not empty video.poster}"><img class="video-poster" src="${fn:escapeXml(video.poster)}" alt="Ảnh bìa ${fn:escapeXml(video.title)}" loading="lazy"></c:if>
                    <div class="video-card-body">
                        <c:url value="/video/detail" var="detailUrl"><c:param name="id" value="${video.videoId}"/></c:url>
                        <h3><a href="${detailUrl}"><c:out value="${video.title}"/></a></h3>
                        <p class="muted">Đăng bởi <c:out value="${video.user.fullName}"/></p>
                        <p class="muted clamp-text"><c:out value="${video.description}"/></p>
                        <a href="${detailUrl}">Xem chi tiết và phát video →</a>
                    </div>
                </article>
            </c:forEach>
        </div>
        <c:if test="${empty latestVideos and empty videoError}"><div class="card empty">Chưa có video nào. Hãy đăng nhập và tải video đầu tiên.</div></c:if>
    </section>
    <section class="grid">
        <article class="feature"><p class="eyebrow">Tài khoản</p><h2>OTP qua email</h2><p class="muted">Kích hoạt tài khoản và khôi phục mật khẩu bằng mã OTP.</p></article>
        <article class="feature"><p class="eyebrow">Video</p><h2>CRUD và phân trang</h2><p class="muted">Tải MP4, quản lý video của bạn và xem danh sách 6 video mỗi trang.</p></article>
        <article class="feature"><p class="eyebrow">Category</p><h2>CRUD bằng JPA</h2><p class="muted">Tiếp tục các chức năng danh mục ở bài tập trước.</p><c:url value="/admin/category/list" var="categoryUrl"/><a href="${categoryUrl}">Mở danh mục →</a></article>
    </section>
</main></body></html>
