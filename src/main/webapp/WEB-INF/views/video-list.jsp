<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html><html lang="vi"><head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Video | Web Homework Day 3</title>
    <c:url value="/assets/styles.css" var="stylesUrl"/><link rel="stylesheet" href="${stylesUrl}">
</head><body>
<main class="container">
    <section class="card">
        <p class="eyebrow">Thư viện công khai</p>
        <c:choose><c:when test="${not empty userId}"><h1>Video của người dùng #<c:out value="${userId}"/></h1></c:when><c:otherwise><h1>Tất cả video</h1></c:otherwise></c:choose>
        <p class="muted">Có <c:out value="${totalCount}"/> video. Mỗi trang hiển thị 6 video.</p>
        <c:if test="${sessionScope.account != null}"><c:url value="/video/manage" var="manageUrl"/><a class="button secondary" href="${manageUrl}">Quản lý video của tôi</a></c:if>
    </section>
    <section class="section-block">
        <c:choose>
            <c:when test="${not empty videos}">
                <div class="video-grid">
                    <c:forEach items="${videos}" var="video">
                        <article class="video-card">
                            <c:if test="${not empty video.poster}"><img class="video-poster" src="${fn:escapeXml(video.poster)}" alt="Ảnh bìa ${fn:escapeXml(video.title)}" loading="lazy"></c:if>
                            <div class="video-card-body">
                                <c:url value="/video/detail" var="detailUrl"><c:param name="id" value="${video.videoId}"/></c:url>
                                <c:url value="/video" var="userVideosUrl"><c:param name="userId" value="${video.user.id}"/></c:url>
                                <h2><a href="${detailUrl}"><c:out value="${video.title}"/></a></h2>
                                <p class="muted">Đăng bởi <a href="${userVideosUrl}"><c:out value="${video.user.fullName}"/></a></p>
                                <p class="muted clamp-text"><c:out value="${video.description}"/></p>
                                <a href="${detailUrl}">Xem chi tiết và phát video →</a>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise><div class="card empty">Chưa có video được đăng.</div></c:otherwise>
        </c:choose>
        <c:if test="${totalPages gt 1}">
            <nav class="pagination" aria-label="Phân trang video">
                <c:if test="${currentPage gt 1}">
                    <c:choose><c:when test="${not empty userId}"><c:url value="/video" var="previousUrl"><c:param name="page" value="${currentPage - 1}"/><c:param name="userId" value="${userId}"/></c:url></c:when><c:otherwise><c:url value="/video" var="previousUrl"><c:param name="page" value="${currentPage - 1}"/></c:url></c:otherwise></c:choose>
                    <a class="button secondary" href="${previousUrl}">← Trước</a>
                </c:if>
                <span>Trang <c:out value="${currentPage}"/> / <c:out value="${totalPages}"/></span>
                <c:if test="${currentPage lt totalPages}">
                    <c:choose><c:when test="${not empty userId}"><c:url value="/video" var="nextUrl"><c:param name="page" value="${currentPage + 1}"/><c:param name="userId" value="${userId}"/></c:url></c:when><c:otherwise><c:url value="/video" var="nextUrl"><c:param name="page" value="${currentPage + 1}"/></c:url></c:otherwise></c:choose>
                    <a class="button secondary" href="${nextUrl}">Sau →</a>
                </c:if>
            </nav>
        </c:if>
    </section>
</main></body></html>
