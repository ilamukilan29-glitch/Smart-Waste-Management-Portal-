<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>403 Forbidden - Access Denied</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
</head>
<body class="bg-light d-flex align-items-center justify-content-center min-vh-100 p-3">
<div class="text-center" style="max-width: 500px;">
    <div class="text-danger display-1 fw-bold mb-3"><i class="bi bi-shield-lock-fill"></i></div>
    <h2 class="fw-bold text-dark mb-2">403 - Access Forbidden</h2>
    <p class="text-muted mb-4">You do not have the required role permissions to access this municipal operational resource.</p>
    <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary px-4">
        <i class="bi bi-house-door-fill me-1"></i> Return to Dashboard
    </a>
</div>
</body>
</html>
