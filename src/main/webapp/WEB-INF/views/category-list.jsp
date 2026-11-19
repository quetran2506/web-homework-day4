<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh mục | Web Homework</title>
    <c:url value="/assets/styles.css" var="stylesUrl" /><link rel="stylesheet" href="${stylesUrl}">
</head>
<body>
<main class="container">
    <section class="card">
        <p class="eyebrow">Yêu cầu 2 · Servlet + JPA</p>
        <h1>Quản lý danh mục</h1>
        <p class="muted">Danh sách Category được truy vấn bằng JPQL và phân trang qua JPA.</p>
        <c:if test="${param.message eq 'created'}"><div class="alert success">Đã thêm danh mục.</div></c:if>
        <c:if test="${param.message eq 'updated'}"><div class="alert success">Đã cập nhật danh mục.</div></c:if>
        <c:if test="${param.message eq 'deleted'}"><div class="alert success">Đã xóa danh mục.</div></c:if>
        <div class="toolbar">
            <c:url value="/admin/category/list" var="listUrl" />
            <form class="search" action="${listUrl}" method="get">
                <input type="search" name="q" placeholder="Tìm theo tên danh mục" value="${fn:escapeXml(keyword)}">
                <button class="button secondary" type="submit">Tìm</button>
            </form>
            <c:url value="/admin/category/add" var="addUrl" /><a class="button" href="${addUrl}">+ Thêm danh mục</a>
        </div>
        <div class="table-wrap">
            <table>
                <thead><tr><th>Mã</th><th>Ảnh</th><th>Tên danh mục</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty categories}"><tr><td class="empty" colspan="5">Chưa có danh mục phù hợp.</td></tr></c:when>
                    <c:otherwise>
                        <c:forEach items="${categories}" var="category">
                            <tr>
                                <td><c:out value="${category.id}" /></td>
                                <td>
                                    <c:if test="${not empty category.icon}">
                                        <c:choose>
                                            <c:when test="${fn:startsWith(category.icon, 'http://') or fn:startsWith(category.icon, 'https://')}"><c:set var="imageUrl" value="${category.icon}" /></c:when>
                                            <c:otherwise><c:url value="/media/${category.icon}" var="imageUrl" /></c:otherwise>
                                        </c:choose>
                                        <img class="thumb" src="${imageUrl}" alt="Ảnh ${fn:escapeXml(category.name)}">
                                    </c:if>
                                    <c:if test="${empty category.icon}"><span class="muted">Chưa có ảnh</span></c:if>
                                </td>
                                <td><c:out value="${category.name}" /></td>
                                <td><c:choose><c:when test="${category.status eq 1}">Hoạt động</c:when><c:otherwise>Khóa</c:otherwise></c:choose></td>
                                <td>
                                    <c:url value="/admin/category/edit" var="editUrl"><c:param name="id" value="${category.id}" /></c:url>
                                    <a href="${editUrl}">Sửa</a>
                                    <span class="muted"> · </span>
                                    <c:url value="/admin/category/delete" var="deleteUrl" />
                                    <form class="inline-form" action="${deleteUrl}" method="post" onsubmit="return confirm('Xóa danh mục này?');">
                                        <input type="hidden" name="id" value="${category.id}">
                                        <button class="link-button" style="color:#b42318" type="submit">Xóa</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
        <c:if test="${totalPages gt 1}">
            <nav class="pagination" aria-label="Phân trang">
                <c:if test="${currentPage gt 1}">
                    <c:url value="/admin/category/list" var="previousUrl"><c:param name="q" value="${keyword}"/><c:param name="page" value="${currentPage - 1}"/></c:url>
                    <a class="button secondary" href="${previousUrl}">← Trước</a>
                </c:if>
                <span class="muted">Trang <c:out value="${currentPage}" /> / <c:out value="${totalPages}" /> · <c:out value="${totalCount}" /> danh mục</span>
                <c:if test="${currentPage lt totalPages}">
                    <c:url value="/admin/category/list" var="nextUrl"><c:param name="q" value="${keyword}"/><c:param name="page" value="${currentPage + 1}"/></c:url>
                    <a class="button secondary" href="${nextUrl}">Sau →</a>
                </c:if>
            </nav>
        </c:if>
    </section>
</main>
</body>
</html>
