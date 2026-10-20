<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<aside class="app-sidebar no-print">
    <a href="${pageContext.request.contextPath}/dashboard" class="sidebar-brand">
        <div class="brand-icon">
            <i class="bi bi-recycle"></i>
        </div>
        <div>
            <div>EcoWaste Ops</div>
            <div style="font-size: 0.68rem; font-weight: 400; color: #94a3b8;">Municipal System</div>
        </div>
    </a>

    <div class="sidebar-nav">
        <c:if test="${sessionScope.LOGGED_IN_USER.role != 'WORKER'}">
            <div class="nav-section-title">Core Operations</div>
            <a href="${pageContext.request.contextPath}/dashboard"
               class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/dashboard') ? 'active' : ''}">
                <i class="bi bi-grid-1x2-fill"></i> Dashboard
            </a>

            <a href="${pageContext.request.contextPath}/bins"
               class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/bins') ? 'active' : ''}">
                <i class="bi bi-trash3-fill"></i> Waste Bins
            </a>

            <a href="${pageContext.request.contextPath}/priority"
               class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/priority') ? 'active' : ''}">
                <i class="bi bi-exclamation-diamond-fill"></i> Priority Queue
            </a>

            <div class="nav-section-title">Optimization & Routing</div>
            <a href="${pageContext.request.contextPath}/routes/plan"
               class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/routes/plan') ? 'active' : ''}">
                <i class="bi bi-map-fill"></i> Route Planner
            </a>

            <a href="${pageContext.request.contextPath}/routes"
               class="sidebar-link ${pageContext.request.servletPath == '/routes' || fn:startsWith(pageContext.request.servletPath, '/routes/details') ? 'active' : ''}">
                <i class="bi bi-signpost-split-fill"></i> Collection Plans
            </a>

            <a href="${pageContext.request.contextPath}/tasks"
               class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/tasks') ? 'active' : ''}">
                <i class="bi bi-check2-square"></i> Collection Tasks
            </a>

            <div class="nav-section-title">Fleet & Workforce</div>
            <a href="${pageContext.request.contextPath}/workers"
               class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/workers') ? 'active' : ''}">
                <i class="bi bi-people-fill"></i> Workers
            </a>

            <a href="${pageContext.request.contextPath}/vehicles"
               class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/vehicles') ? 'active' : ''}">
                <i class="bi bi-truck"></i> Vehicles
            </a>

            <div class="nav-section-title">Reports & Alerts</div>
            <a href="${pageContext.request.contextPath}/reports"
               class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/reports') ? 'active' : ''}">
                <i class="bi bi-bar-chart-line-fill"></i> Operational Reports
            </a>

            <a href="${pageContext.request.contextPath}/notifications"
               class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/notifications') ? 'active' : ''}">
                <i class="bi bi-bell-fill"></i> Alerts & Notices
            </a>

            <c:if test="${sessionScope.LOGGED_IN_USER.role == 'ADMIN'}">
                <div class="nav-section-title">System & Simulation</div>
                <a href="${pageContext.request.contextPath}/simulation"
                   class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/simulation') ? 'active' : ''}">
                    <i class="bi bi-cpu-fill"></i> Fill Simulation
                </a>

                <a href="${pageContext.request.contextPath}/settings"
                   class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/settings') ? 'active' : ''}">
                    <i class="bi bi-sliders"></i> Engine Rules
                </a>
            </c:if>
        </c:if>

        <c:if test="${sessionScope.LOGGED_IN_USER.role == 'WORKER'}">
            <div class="nav-section-title">Worker Portal</div>
            <a href="${pageContext.request.contextPath}/worker/tasks"
               class="sidebar-link ${fn:startsWith(pageContext.request.servletPath, '/worker') ? 'active' : ''}">
                <i class="bi bi-clipboard2-check-fill"></i> My Assigned Tasks
            </a>
        </c:if>
    </div>
</aside>
