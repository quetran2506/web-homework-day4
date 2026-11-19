<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="editing" value="${formAction eq 'edit'}" />
<c:set var="externalImage" value="${not empty category.icon and (fn:startsWith(category.icon, 'http://') or fn:startsWith(category.icon, 'https://'))}" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:choose><c:when test="${editing}">Sửa</c:when><c:otherwise>Thêm</c:otherwise></c:choose> danh mục</title>
    <c:url value="/assets/styles.css" var="stylesUrl" /><link rel="stylesheet" href="${stylesUrl}">
</head>
<body>
<main class="container narrow">
    <section class="card">
        <p class="eyebrow">Yêu cầu 2 · Category</p>
        <h1><c:choose><c:when test="${editing}">Sửa danh mục</c:when><c:otherwise>Thêm danh mục</c:otherwise></c:choose></h1>
        <c:if test="${not empty error}"><div class="alert" role="alert"><c:out value="${error}" /></div></c:if>
        <c:choose>
            <c:when test="${editing}"><c:url value="/admin/category/edit" var="formUrl" /></c:when>
            <c:otherwise><c:url value="/admin/category/add" var="formUrl" /></c:otherwise>
        </c:choose>
        <form action="${formUrl}" method="post" enctype="multipart/form-data" class="needs-validation" novalidate>
            <c:if test="${editing}"><input type="hidden" name="id" value="${category.id}"></c:if>
            <div class="field">
                <label for="name">Tên danh mục</label>
                <input id="name" type="text" name="name" required maxlength="255" value="${fn:escapeXml(category.name)}" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div>
            </div>
            <div class="field">
                <label for="imageUrl">Link ảnh đại diện (tùy chọn)</label>
                <input id="imageUrl" type="url" name="imageUrl" maxlength="500" pattern="https?://.+" placeholder="https://..."
                       value="${externalImage ? fn:escapeXml(category.icon) : ''}" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div>
            </div>
            <div class="field">
                <label for="icon">Hoặc tải ảnh PNG, JPG, GIF, WebP (tối đa 3 MB)</label>
                <input id="icon" type="file" name="icon" accept="image/png,image/jpeg,image/gif,image/webp" data-max-bytes="3145728" class="form-control"><div class="invalid-feedback">Vui lòng kiểm tra thông tin ở trường này.</div>
                <c:if test="${editing and not empty category.icon}">
                    <p class="muted">Ảnh hiện tại</p>
                    <c:choose>
                        <c:when test="${fn:startsWith(category.icon, 'http://') or fn:startsWith(category.icon, 'https://')}"><c:set var="currentImageUrl" value="${category.icon}" /></c:when>
                        <c:otherwise><c:url value="/media/${category.icon}" var="currentImageUrl" /></c:otherwise>
                    </c:choose>
                    <img class="thumb" src="${currentImageUrl}" alt="Ảnh hiện tại">
                    <p class="footer-note">Để giữ ảnh hiện tại, để trống link và không chọn tệp mới.</p>
                </c:if>
            </div>
            <div class="field">
                <span class="field-label">Trạng thái</span>
                <label><input type="radio" name="status" value="1" ${category.status eq 0 ? '' : 'checked'}> Hoạt động</label>
                <label><input type="radio" name="status" value="0" ${category.status eq 0 ? 'checked' : ''}> Khóa</label>
            </div>
            <div class="actions">
                <button class="button" type="submit"><c:choose><c:when test="${editing}">Lưu thay đổi</c:when><c:otherwise>Thêm danh mục</c:otherwise></c:choose></button>
                <c:url value="/admin/category/list" var="listUrl" /><a class="button secondary" href="${listUrl}">Quay lại</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>
