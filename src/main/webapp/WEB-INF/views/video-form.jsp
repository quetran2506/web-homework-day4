<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html><html lang="vi"><head>
    <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:choose><c:when test="${formAction eq 'edit'}">Sửa video</c:when><c:otherwise>Thêm video</c:otherwise></c:choose> | Web Homework</title>
    <c:url value="/assets/styles.css" var="stylesUrl"/><link rel="stylesheet" href="${stylesUrl}">
</head><body>
<main class="container narrow"><section class="card">
    <p class="eyebrow">Quản lý video</p><h1><c:choose><c:when test="${formAction eq 'edit'}">Sửa video</c:when><c:otherwise>Thêm video mới</c:otherwise></c:choose></h1>
    <p class="muted">Chỉ nhận MP4 tối đa 500 MB. File video được lưu trong thư mục uploads/videos.</p>
    <c:if test="${not empty error}"><div class="alert"><c:out value="${error}"/></div></c:if>
    <c:choose><c:when test="${formAction eq 'edit'}"><c:url value="/video/edit" var="formUrl"><c:param name="id" value="${video.videoId}"/></c:url></c:when><c:otherwise><c:url value="/video/add" var="formUrl"/></c:otherwise></c:choose>
    <form action="${formUrl}" method="post" enctype="multipart/form-data" class="needs-validation" novalidate>
        <div class="field"><label for="title">Tên video</label><input id="title" class="form-control" name="title" type="text" required maxlength="500" value="${fn:escapeXml(video.title)}"><div class="invalid-feedback">Nhập tên video, tối đa 500 ký tự.</div></div>
        <div class="field"><label for="description">Mô tả</label><textarea id="description" class="form-control" name="description" rows="5" maxlength="10000"><c:out value="${video.description}"/></textarea><div class="invalid-feedback">Mô tả tối đa 10.000 ký tự.</div></div>
        <div class="field"><label for="poster">Link ảnh bìa (không bắt buộc)</label><input id="poster" class="form-control" name="poster" type="url" maxlength="500" pattern="https?://.+" value="${fn:escapeXml(video.poster)}"><div class="invalid-feedback">Link ảnh phải là URL HTTP hoặc HTTPS hợp lệ.</div></div>
        <div class="field"><label for="videoUpload">File MP4 <c:if test="${formAction eq 'edit'}">(để trống để giữ file hiện tại)</c:if></label><input id="videoUpload" class="form-control" name="videoUpload" type="file" accept="video/mp4,.mp4" data-max-bytes="524288000" <c:if test="${formAction eq 'add'}">required</c:if>><div class="invalid-feedback">Chọn tệp MP4 không quá 500 MB.</div></div>
        <c:if test="${formAction eq 'edit'}"><p class="muted">File hiện tại: <c:out value="${video.videoFile}"/></p></c:if>
        <div class="actions"><button class="button" type="submit">Lưu video</button><c:url value="/video/manage" var="manageUrl"/><a class="button secondary" href="${manageUrl}">Hủy</a></div>
    </form>
</section></main></body></html>
