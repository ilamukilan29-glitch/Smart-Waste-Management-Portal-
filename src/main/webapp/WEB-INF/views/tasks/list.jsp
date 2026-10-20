<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Collection Tasks" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h4 class="fw-bold mb-1 text-dark">Collection Operational Tasks</h4>
                <p class="text-muted small">Track dispatched collection tours, real-time worker execution, and recorded waste weights.</p>
            </div>
            <div>
                <c:if test="${sessionScope.LOGGED_IN_USER.role == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/routes/plan" class="btn btn-success btn-sm shadow-sm">
                        <i class="bi bi-plus-lg me-1"></i> Dispatch New Task
                    </a>
                </c:if>
            </div>
        </div>

        <div class="card card-municipal">
            <div class="card-header">
                <span class="fw-bold">All Collection Tasks (${tasks.size()} records)</span>
            </div>
            <div class="table-responsive">
                <table class="table table-municipal table-hover mb-0">
                    <thead>
                    <tr>
                        <th>Task Code</th>
                        <th>Plan Ref</th>
                        <th>Assigned Worker</th>
                        <th>Fleet Vehicle</th>
                        <th>Scheduled Date</th>
                        <th>Started At</th>
                        <th>Completed At</th>
                        <th>Collected Waste</th>
                        <th>Status</th>
                        <th class="text-end">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty tasks}">
                            <tr>
                                <td colspan="10" class="text-center py-5 text-muted">
                                    <i class="bi bi-clipboard2-check fs-2"></i>
                                    <p class="mt-2 mb-0">No collection tasks assigned yet.</p>
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
                                    <td>
                                        <a href="${pageContext.request.contextPath}/routes/details?id=${task.collectionPlanId}" class="text-muted text-decoration-none small">
                                            <c:out value="${task.planCode}"/>
                                        </a>
                                    </td>
                                    <td class="fw-semibold text-dark"><c:out value="${task.workerName}"/></td>
                                    <td><span class="badge bg-light text-dark border"><c:out value="${task.vehicleRegistration}"/></span></td>
                                    <td><c:out value="${task.plannedDate}"/></td>
                                    <td class="small text-muted"><c:out value="${task.actualStartAt != null ? task.actualStartAt : '&mdash;'}" escapeXml="false"/></td>
                                    <td class="small text-muted"><c:out value="${task.actualCompletedAt != null ? task.actualCompletedAt : '&mdash;'}" escapeXml="false"/></td>
                                    <td class="fw-bold text-dark">
                                        <c:choose>
                                            <c:when test="${task.status == 'COMPLETED'}">
                                                <fmt:formatNumber value="${task.collectedWeightKg}" maxFractionDigits="1"/> kg
                                            </c:when>
                                            <c:otherwise>&mdash;</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="badge ${task.status == 'COMPLETED' ? 'bg-success' : (task.status == 'IN_PROGRESS' ? 'bg-warning text-dark' : (task.status == 'ASSIGNED' ? 'bg-primary' : 'bg-secondary'))}">
                                            <c:out value="${task.status.displayName}"/>
                                        </span>
                                    </td>
                                    <td class="text-end">
                                        <a href="${pageContext.request.contextPath}/tasks/details?id=${task.id}" class="btn btn-sm btn-outline-info">
                                            <i class="bi bi-eye"></i> Details
                                        </a>
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
