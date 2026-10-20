<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In - Smart Waste Management System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <style>
        body {
            background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1.5rem;
        }
        .login-card {
            max-width: 440px;
            width: 100%;
            background: #ffffff;
            border-radius: var(--radius-lg);
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.2), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            overflow: hidden;
        }
        .login-header {
            background: linear-gradient(135deg, #059669, #10b981);
            color: #ffffff;
            padding: 2.25rem 2rem;
            text-align: center;
        }
    </style>
</head>
<body>

<div class="login-card">
    <div class="login-header">
        <div class="d-inline-flex align-items-center justify-content-center bg-white text-success rounded-circle mb-3" style="width: 56px; height: 56px; font-size: 1.75rem;">
            <i class="bi bi-recycle"></i>
        </div>
        <h4 class="fw-bold mb-1">EcoWaste Management</h4>
        <p class="mb-0 text-white-50 small">Municipal Operations & Optimization System</p>
    </div>

    <div class="p-4 p-md-4">
        <c:if test="${param.loggedOut == 'true'}">
            <div class="alert alert-info py-2 small d-flex align-items-center">
                <i class="bi bi-info-circle-fill me-2"></i> You have been successfully signed out.
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger py-2 small d-flex align-items-center">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="POST">
            <input type="hidden" name="redirect" value="<c:out value='${param.redirect}'/>">

            <div class="mb-3">
                <label for="username" class="form-label small fw-semibold text-secondary">Username</label>
                <div class="input-group">
                    <span class="input-group-text bg-light text-muted"><i class="bi bi-person"></i></span>
                    <input type="text" class="form-control" id="username" name="username"
                           value="<c:out value='${username != null ? username : \"admin\"}'/>" required autofocus>
                </div>
            </div>

            <div class="mb-4">
                <label for="password" class="form-label small fw-semibold text-secondary">Password</label>
                <div class="input-group">
                    <span class="input-group-text bg-light text-muted"><i class="bi bi-lock"></i></span>
                    <input type="password" class="form-control" id="password" name="password"
                           value="Admin@123" required>
                </div>
            </div>

            <button type="submit" class="btn btn-success w-100 py-2 fw-semibold shadow-sm">
                <i class="bi bi-box-arrow-in-right me-1"></i> Sign In to Portal
            </button>
        </form>

        <div class="mt-4 p-3 bg-light rounded-3 border">
            <div class="fw-bold text-dark small mb-1"><i class="bi bi-key-fill text-success me-1"></i> Academic Demo Credentials:</div>
            <ul class="list-unstyled mb-0 small text-muted">
                <li>&bull; <strong>Administrator:</strong> <code>admin</code> / <code>Admin@123</code></li>
                <li>&bull; <strong>Supervisor:</strong> <code>supervisor</code> / <code>Super@123</code></li>
                <li>&bull; <strong>Collection Worker:</strong> <code>worker1</code> / <code>Worker@123</code></li>
            </ul>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
