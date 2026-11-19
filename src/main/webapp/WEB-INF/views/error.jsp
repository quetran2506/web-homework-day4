<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="vi">
<head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Có lỗi</title>
    <c:url value="/assets/styles.css" var="stylesUrl" /><link rel="stylesheet" href="${stylesUrl}"></head>
<body>
<main class="container narrow"><section class="card">
    <p class="eyebrow">Có lỗi</p><h1>Không hoàn tất được thao tác</h1>
    <div class="alert" role="alert"><c:out value="${error}" /></div>
    <c:url value="/admin/category/list" var="listUrl" /><a class="button" href="${listUrl}">Quay lại danh mục</a>
</section></main></body>
</html>
