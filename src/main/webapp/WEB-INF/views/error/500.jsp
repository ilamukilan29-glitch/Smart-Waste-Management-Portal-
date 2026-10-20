<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>500 Internal Server Error</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
</head>
<body class="bg-light d-flex align-items-center justify-content-center min-vh-100 p-3">
<div class="text-center" style="max-width: 550px;">
    <div class="text-danger display-1 fw-bold mb-3"><i class="bi bi-slash-circle-fill"></i></div>
    <h2 class="fw-bold text-dark mb-2">500 - Internal Server Error</h2>
    <p class="text-muted mb-4">An unexpected system error occurred while processing your request. Sensitive debug traces have been securely logged to server diagnostics.</p>
    <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary px-4">
        <i class="bi bi-house-door-fill me-1"></i> Return to Dashboard
    </a>
</div>
</body>
</html>
