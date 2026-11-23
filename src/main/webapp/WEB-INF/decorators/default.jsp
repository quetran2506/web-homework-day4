<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><sitemesh:write property="title" /> | Web Homework Day 4</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.9/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-yEPZ7jP9S0QKiUQ0m3jjsO4ProIW7wvR9gXkI7JOGa2XVsw53Yke2bsdkBbjmmiX"
          crossorigin="anonymous">
    <sitemesh:write property="head" />
</head>
<body class="d-flex min-vh-100 flex-column bg-body-tertiary">
    <c:url value="/home" var="homeUrl" />
    <c:url value="/video" var="videoUrl" />
    <c:url value="/admin/category/list" var="categoryUrl" />
    <c:url value="/login" var="loginUrl" />
    <c:url value="/register" var="registerUrl" />
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
        <div class="container-fluid px-3 px-lg-5">
            <a class="navbar-brand fw-bold" href="${homeUrl}">Web Homework</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNavigation"
                    aria-controls="mainNavigation" aria-expanded="false" aria-label="Mở điều hướng">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="mainNavigation">
                <ul class="navbar-nav ms-auto align-items-lg-center gap-lg-2">
                    <li class="nav-item"><a class="nav-link" href="${homeUrl}">Trang chủ</a></li>
                    <li class="nav-item"><a class="nav-link" href="${categoryUrl}">Danh mục</a></li>
                    <li class="nav-item"><a class="nav-link" href="${videoUrl}">Video</a></li>
                    <c:choose>
                        <c:when test="${sessionScope.account != null}">
                            <li class="nav-item"><c:url value="/profile" var="profileUrl" /><a class="nav-link" href="${profileUrl}">Hồ sơ</a></li>
                            <li class="nav-item"><c:url value="/video/manage" var="manageVideosUrl" /><a class="nav-link" href="${manageVideosUrl}">Quản lý video</a></li>
                            <li class="nav-item"><span class="navbar-text px-lg-2 text-nowrap">Xin ch&#224;o, <c:out value="${sessionScope.account.fullName}" /></span></li>
                            <li class="nav-item">
                                <c:url value="/logout" var="logoutUrl" />
                                <form action="${logoutUrl}" method="post" class="d-inline">
                                    <button class="btn btn-outline-light btn-sm ms-lg-2" type="submit">Đăng xuất</button>
                                </form>
                            </li>
                        </c:when>
                        <c:otherwise>
                            <li class="nav-item"><a class="nav-link" href="${loginUrl}">Đăng nhập</a></li>
                            <li class="nav-item"><a class="btn btn-success btn-sm ms-lg-2" href="${registerUrl}">Đăng ký</a></li>
                        </c:otherwise>
                    </c:choose>
                </ul>
            </div>
        </div>
    </nav>
    <div class="flex-grow-1 py-4 py-lg-5">
        <sitemesh:write property="body" />
    </div>
    <footer class="border-top bg-white py-3 mt-auto">
        <div class="container text-center text-secondary small">Bài tập Lập trình Web · Homework Day 4</div>
    </footer>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.9/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-WyOxWvTyr1QaMPfiPJSv76iReVthorl5uWlFtZPyK0IE0hB0Gzo9y5Bp9mYanr8Bu"
            crossorigin="anonymous"></script>
    <c:url value="/assets/js/form-validation.js" var="validationJs" />
    <script src="${validationJs}" defer></script>
</body>
</html>
