<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html><html lang="vi"><head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${video.title}"/> | Video</title>
    <c:url value="/assets/styles.css" var="stylesUrl"/><link rel="stylesheet" href="${stylesUrl}">
</head><body>
<main class="container narrow">
    <article class="card">
        <p class="eyebrow">Video của <c:out value="${video.user.fullName}"/></p>
        <h1><c:out value="${video.title}"/></h1>
        <video class="video-player" controls playsinline preload="metadata" poster="${fn:escapeXml(video.poster)}">
            <c:url value="/video/play" var="playUrl"><c:param name="id" value="${video.videoId}"/></c:url>
            <source src="${playUrl}" type="video/mp4">Trình duyệt không hỗ trợ phát video MP4.
        </video>
        <p class="muted video-description"><c:out value="${video.description}"/></p>
        <div class="actions"><c:url value="/video" var="listUrl"/><a class="button secondary" href="${listUrl}">Quay lại danh sách</a>
            <c:if test="${sessionScope.account != null and sessionScope.account.id eq video.user.id}"><c:url value="/video/edit" var="editUrl"><c:param name="id" value="${video.videoId}"/></c:url><a class="button" href="${editUrl}">Sửa video</a></c:if>
        </div>
    </article>
</main></body></html>
