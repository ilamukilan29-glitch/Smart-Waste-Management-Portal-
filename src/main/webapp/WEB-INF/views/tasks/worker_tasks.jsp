<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Worker Task Portal" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h4 class="fw-bold mb-1 text-dark">My Assigned Collection Tours</h4>
                <p class="text-muted small">
                    <c:choose>
                        <c:when test="${not empty worker}">
                            Logged in as Worker: <strong><c:out value="${worker.fullName}"/></strong> (<c:out value="${worker.workerCode}"/>) &bull; Area: <c:out value="${worker.areaName}"/>
                        </c:when>
                        <c:otherwise>Collection Field Operations Portal</c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>

        <c:if test="${unlinkedWorker}">
            <div class="alert alert-info border shadow-sm mb-4">
                <i class="bi bi-info-circle-fill me-2"></i> You are logged in with administrative privileges. To view assigned field tasks as a worker, sign in using worker credentials (e.g. <code>worker1</code> / <code>Worker@123</code>).
            </div>
        </c:if>

        <div class="card card-municipal">
            <div class="card-header bg-light">
                <span class="fw-bold text-dark"><i class="bi bi-clipboard2-check-fill text-success me-2"></i>Assigned Tasks List (${tasks.size()})</span>
            </div>
            <div class="table-responsive">
                <table class="table table-municipal table-hover mb-0">
                    <thead>
                    <tr>
                        <th>Task Code</th>
                        <th>Assigned Vehicle</th>
                        <th>Scheduled Date</th>
                        <th>Status</th>
                        <th>Started At</th>
                        <th>Collected Weight</th>
                        <th class="text-end">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty tasks}">
                            <tr>
                                <td colspan="7" class="text-center py-5 text-muted">
                                    <i class="bi bi-calendar-check fs-2 text-success"></i>
                                    <p class="mt-2 mb-0">No collection tasks currently assigned to you. All clear!</p>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="task" items="${tasks}">
                                <tr>
                                    <td class="fw-bold text-success">
                                        <a href="${pageContext.request.contextPath}/tasks/details?id=${task.id}" class="text-success text-decoration-none">
                                            <c:out value="${task.taskCode}"/>
                                        </a>
                                    </td>
                                    <td><span class="badge bg-light text-dark border"><c:out value="${task.vehicleRegistration}"/></span></td>
                                    <td><c:out value="${task.plannedDate}"/></td>
                                    <td>
                                        <span class="badge ${task.status == 'COMPLETED' ? 'bg-success' : (task.status == 'IN_PROGRESS' ? 'bg-warning text-dark' : 'bg-primary')}">
                                            <c:out value="${task.status.displayName}"/>
                                        </span>
                                    </td>
                                    <td class="small text-muted"><c:out value="${task.actualStartAt != null ? task.actualStartAt : 'Not Started'}"/></td>
                                    <td class="fw-semibold">
                                        <c:choose>
                                            <c:when test="${task.status == 'COMPLETED'}">
                                                <fmt:formatNumber value="${task.collectedWeightKg}" maxFractionDigits="1"/> kg
                                            </c:when>
                                            <c:otherwise>&mdash;</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end">
                                        <div class="btn-group btn-group-sm">
                                            <c:if test="${task.status == 'ASSIGNED'}">
                                                <form action="${pageContext.request.contextPath}/tasks/start" method="POST" class="d-inline">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                    <input type="hidden" name="taskId" value="${task.id}">
                                                    <button type="submit" class="btn btn-warning">
                                                        <i class="bi bi-play-fill"></i> Start
                                                    </button>
                                                </form>
                                            </c:if>
                                            <c:if test="${task.status == 'IN_PROGRESS' || task.status == 'ASSIGNED'}">
                                                <a href="${pageContext.request.contextPath}/tasks/details?id=${task.id}" class="btn btn-success">
                                                    <i class="bi bi-check2"></i> Complete
                                                </a>
                                            </c:if>
                                            <a href="${pageContext.request.contextPath}/tasks/details?id=${task.id}" class="btn btn-outline-info">
                                                <i class="bi bi-eye"></i> Stops
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

    </main>

<jsp:include page="../common/footer.jsp"/>
