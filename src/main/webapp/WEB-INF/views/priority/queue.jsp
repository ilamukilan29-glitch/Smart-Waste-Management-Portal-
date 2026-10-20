<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Collection Priority Queue" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
            <div>
                <h4 class="fw-bold mb-1 text-dark">Collection Priority Queue</h4>
                <p class="text-muted small">Bins prioritized by Java 8 functional rules based on fill percentage and overflow risk.</p>
            </div>
            <div>
                <c:if test="${not empty selectedAreaId}">
                    <a href="${pageContext.request.contextPath}/routes/plan?areaId=${selectedAreaId}" class="btn btn-success btn-sm shadow-sm">
                        <i class="bi bi-map-fill me-1"></i> Generate Route for this Area
                    </a>
                </c:if>
            </div>
        </div>

        <!-- Priority Threshold Legend -->
        <div class="row g-2 mb-4">
            <div class="col-6 col-md-3">
                <div class="p-2 border rounded-3 bg-white d-flex align-items-center gap-2">
                    <span class="badge badge-priority-critical">&ge; ${criticalThreshold}%</span>
                    <span class="small fw-semibold text-danger">Critical Overflow</span>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="p-2 border rounded-3 bg-white d-flex align-items-center gap-2">
                    <span class="badge badge-priority-high">&ge; ${highThreshold}%</span>
                    <span class="small fw-semibold text-warning-emphasis">High Urgency</span>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="p-2 border rounded-3 bg-white d-flex align-items-center gap-2">
                    <span class="badge badge-priority-medium">&ge; ${mediumThreshold}%</span>
                    <span class="small fw-semibold text-primary">Medium Priority</span>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="p-2 border rounded-3 bg-white d-flex align-items-center gap-2">
                    <span class="badge badge-priority-low">&lt; ${mediumThreshold}%</span>
                    <span class="small fw-semibold text-success">Low (Monitoring)</span>
                </div>
            </div>
        </div>

        <!-- Area & Low Priority Filter Form -->
        <div class="card card-municipal mb-4">
            <div class="card-body p-3">
                <form action="${pageContext.request.contextPath}/priority" method="GET" class="row g-2 align-items-center">
                    <div class="col-12 col-md-4">
                        <select name="areaId" class="form-select form-select-sm" onchange="this.form.submit()">
                            <option value="">All Municipal Areas</option>
                            <c:forEach var="area" items="${areas}">
                                <option value="${area.id}" ${selectedAreaId == area.id ? 'selected' : ''}>
                                    <c:out value="${area.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-12 col-md-4">
                        <div class="form-check form-switch small">
                            <input class="form-check-input" type="checkbox" id="includeLow" name="includeLow" value="true"
                                   ${includeLow ? 'checked' : ''} onchange="this.form.submit()">
                            <label class="form-check-label" for="includeLow">Include low-priority monitoring bins (&lt; 60%)</label>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <!-- Priority Queue Table -->
        <div class="card card-municipal">
            <div class="card-header">
                <span class="fw-bold">Prioritized Bins (${prioritizedBins.size()} candidate bins)</span>
            </div>
            <div class="table-responsive">
                <table class="table table-municipal table-hover mb-0">
                    <thead>
                    <tr>
                        <th>Priority</th>
                        <th>Bin Code</th>
                        <th>Area & Location</th>
                        <th>Waste Type</th>
                        <th style="min-width: 160px;">Fill Level</th>
                        <th>Active Task</th>
                        <th>Priority Reason</th>
                        <th class="text-end">Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty prioritizedBins}">
                            <tr>
                                <td colspan="8" class="text-center py-5 text-muted">
                                    <i class="bi bi-check2-circle fs-2 text-success"></i>
                                    <p class="mt-2 mb-0">No bins currently requiring collection in this scope.</p>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="item" items="${prioritizedBins}">
                                <tr>
                                    <td>
                                        <span class="badge ${item.priorityLevel == 'CRITICAL' ? 'badge-priority-critical' : (item.priorityLevel == 'HIGH' ? 'badge-priority-high' : (item.priorityLevel == 'MEDIUM' ? 'badge-priority-medium' : 'badge-priority-low'))}">
                                            <c:out value="${item.priorityLevel.displayName}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/bins/details?id=${item.binId}" class="fw-bold text-success text-decoration-none">
                                            <c:out value="${item.binCode}"/>
                                        </a>
                                    </td>
                                    <td>
                                        <div class="fw-semibold text-dark"><c:out value="${item.locationName}"/></div>
                                        <div class="small text-muted"><c:out value="${item.areaName}"/></div>
                                    </td>
                                    <td>
                                        <span class="badge bg-light text-dark border"><c:out value="${item.wasteType.displayName}"/></span>
                                    </td>
                                    <td>
                                        <div class="d-flex justify-content-between small fw-bold mb-1">
                                            <span><fmt:formatNumber value="${item.currentFillPercentage}" maxFractionDigits="1"/>%</span>
                                        </div>
                                        <div class="fill-progress-container">
                                            <div class="fill-progress-bar ${item.currentFillPercentage >= 90 ? 'fill-bar-critical' : (item.currentFillPercentage >= 80 ? 'fill-bar-high' : (item.currentFillPercentage >= 60 ? 'fill-bar-medium' : 'fill-bar-low'))}"
                                                 style="width: ${item.currentFillPercentage}%;"></div>
                                        </div>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${item.hasActiveTask}">
                                                <span class="badge bg-warning-subtle text-warning-emphasis border border-warning">
                                                    <i class="bi bi-clock-history me-1"></i> Task Scheduled
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-success-subtle text-success border border-success">
                                                    <i class="bi bi-check-lg me-1"></i> Ready for Route
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="small text-muted"><c:out value="${item.priorityReason}"/></span>
                                    </td>
                                    <td class="text-end">
                                        <c:if test="${!item.hasActiveTask}">
                                            <a href="${pageContext.request.contextPath}/routes/plan?areaId=${item.areaId}" class="btn btn-sm btn-outline-success">
                                                Plan Route
                                            </a>
                                        </c:if>
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
