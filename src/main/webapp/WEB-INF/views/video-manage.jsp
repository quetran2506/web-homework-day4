<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html><html lang="vi"><head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý video | Web Homework Day 3</title>
    <c:url value="/assets/styles.css" var="stylesUrl"/><link rel="stylesheet" href="${stylesUrl}">
</head><body>
<main class="container"><section class="card">
    <p class="eyebrow">Kênh của tôi</p><h1>Quản lý video</h1>
    <c:if test="${param.message eq 'created'}"><div class="alert success">Đã thêm video.</div></c:if>
    <c:if test="${param.message eq 'updated'}"><div class="alert success">Đã cập nhật video.</div></c:if>
    <c:if test="${param.message eq 'deleted'}"><div class="alert success">Đã xóa video.</div></c:if>
    <c:url value="/video/add" var="addUrl"/><c:url value="/video/delete" var="deleteUrl"/><a class="button" href="${addUrl}">Thêm video</a>
    <div class="table-wrap section-block"><table><thead><tr><th>Tên video</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
        <c:forEach items="${videos}" var="video"><tr>
            <td><c:url value="/video/detail" var="detailUrl"><c:param name="id" value="${video.videoId}"/></c:url><a href="${detailUrl}"><c:out value="${video.title}"/></a></td>
            <td><c:choose><c:when test="${video.active eq 1}">Công khai</c:when><c:otherwise>Ẩn</c:otherwise></c:choose></td>
            <td class="row-actions"><c:url value="/video/edit" var="editUrl"><c:param name="id" value="${video.videoId}"/></c:url><a href="${editUrl}">Sửa</a>
                <form action="${deleteUrl}" method="post" onsubmit="return confirm('Xóa video này?')"><input type="hidden" name="id" value="${fn:escapeXml(video.videoId)}"/><button class="link-button danger-text" type="submit">Xóa</button></form></td>
        </tr></c:forEach>
    </tbody></table></div>
    <c:if test="${empty videos}"><div class="empty">Bạn chưa đăng video nào.</div></c:if>
</section></main></body></html>
