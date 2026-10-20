<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header class="app-navbar no-print">
    <div class="d-flex align-items-center gap-3">
        <h5 class="mb-0 fw-bold text-dark"><c:out value="${pageTitle != null ? pageTitle : 'Operations Overview'}"/></h5>
    </div>

    <div class="d-flex align-items-center gap-3">
        <div class="text-end d-none d-md-block">
            <div class="fw-semibold text-dark"><c:out value="${sessionScope.LOGGED_IN_USER.fullName}"/></div>
            <div class="text-muted" style="font-size: 0.78rem;">
                <span class="badge ${sessionScope.LOGGED_IN_USER.role == 'ADMIN' ? 'bg-danger' : (sessionScope.LOGGED_IN_USER.role == 'SUPERVISOR' ? 'bg-primary' : 'bg-success')}">
                    <c:out value="${sessionScope.LOGGED_IN_USER.role.displayName}"/>
                </span>
            </div>
        </div>

        <div class="dropdown">
            <button class="btn btn-light rounded-circle p-2 shadow-sm" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-person-circle fs-5 text-secondary"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0">
                <li class="px-3 py-2 border-bottom">
                    <div class="fw-bold"><c:out value="${sessionScope.LOGGED_IN_USER.fullName}"/></div>
                    <div class="small text-muted">@<c:out value="${sessionScope.LOGGED_IN_USER.username}"/></div>
                </li>
                <li>
                    <a class="dropdown-item text-danger mt-1" href="${pageContext.request.contextPath}/logout">
                        <i class="bi bi-box-arrow-right me-2"></i> Sign Out
                    </a>
                </li>
            </ul>
        </div>
    </div>
</header>
