<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="isNewPlan" value="${routeResult != null}"/>
<c:set var="pageTitle" value="${isNewPlan ? 'Optimized Route Plan Review' : 'Collection Plan Details'}" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <a href="${pageContext.request.contextPath}/routes" class="text-decoration-none text-muted small">
                    <i class="bi bi-arrow-left me-1"></i> Back to Collection Plans
                </a>
                <h4 class="fw-bold mb-1 mt-2 text-dark">
                    <c:choose>
                        <c:when test="${isNewPlan}">Optimized Route Plan (Generated via Nearest Neighbor)</c:when>
                        <c:otherwise>Collection Plan: <c:out value="${plan.planCode}"/></c:otherwise>
                    </c:choose>
                </h4>
                <p class="text-muted small">
                    <c:choose>
                        <c:when test="${isNewPlan}">Review stops and assign an operational worker to dispatch.</c:when>
                        <c:otherwise>Area: <c:out value="${plan.areaName}"/> &bull; Depot: <c:out value="${plan.depotName}"/></c:otherwise>
                    </c:choose>
                </p>
            </div>
            <div>
                <c:if test="${!isNewPlan}">
                    <span class="badge ${plan.status == 'COMPLETED' ? 'bg-success' : (plan.status == 'IN_PROGRESS' ? 'bg-warning text-dark' : 'bg-primary')} fs-6">
                        <c:out value="${plan.status.displayName}"/>
                    </span>
                </c:if>
            </div>
        </div>

        <!-- Metrics Row -->
        <div class="row g-3 mb-4">
            <!-- Total Distance -->
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card">
                    <div class="metric-label">Total Tour Distance</div>
                    <div class="metric-val text-success">
                        <fmt:formatNumber value="${isNewPlan ? routeResult.totalDistanceKm : plan.totalDistanceKm}" maxFractionDigits="2"/> km
                    </div>
                    <div class="small text-muted mt-1">Computed via Distance Matrix</div>
                </div>
            </div>

            <!-- Estimated Duration -->
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card">
                    <div class="metric-label">Estimated Travel Time</div>
                    <div class="metric-val text-primary">
                        ${isNewPlan ? routeResult.totalDurationMinutes : plan.estimatedDurationMinutes} min
                    </div>
                    <div class="small text-muted mt-1">Includes bin service stop times</div>
                </div>
            </div>

            <!-- Total Waste Load -->
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card">
                    <div class="metric-label">Estimated Collected Waste</div>
                    <div class="metric-val text-dark">
                        <fmt:formatNumber value="${isNewPlan ? routeResult.totalWasteEstimatedKg : plan.totalWasteEstimatedKg}" maxFractionDigits="1"/> kg
                    </div>
                    <div class="small text-muted mt-1">Total expected truck load</div>
                </div>
            </div>

            <!-- Vehicle / Capacity -->
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card">
                    <div class="metric-label">Vehicle Assigned</div>
                    <div class="fw-bold text-dark fs-5 mt-1">
                        <c:out value="${isNewPlan ? routeResult.vehicleRegistration : plan.vehicleRegistration}"/>
                    </div>
                    <div class="small text-muted mt-1">
                        Capacity: <fmt:formatNumber value="${isNewPlan ? routeResult.vehicleCapacityKg : 0}" maxFractionDigits="0"/> kg
                    </div>
                </div>
            </div>
        </div>

        <!-- Capacity Warning if exceeded -->
        <c:if test="${isNewPlan && routeResult.capacityExceeded}">
            <div class="alert alert-warning border d-flex align-items-center mb-4">
                <i class="bi bi-exclamation-triangle-fill fs-4 me-3 text-warning"></i>
                <div>
                    <strong>Vehicle Capacity Limit Reached:</strong> Some requested candidate bins were excluded from this tour because adding them would exceed the vehicle's maximum carrying capacity.
                </div>
            </div>
        </c:if>

        <!-- Ordered Route Stops Table -->
        <div class="card card-municipal mb-4">
            <div class="card-header bg-light">
                <span class="fw-bold text-dark"><i class="bi bi-list-ol text-success me-2"></i>Ordered Route Stops (Sequential Tour)</span>
            </div>
            <div class="table-responsive">
                <table class="table table-municipal table-hover mb-0">
                    <thead>
                    <tr>
                        <th style="width: 70px;">Stop #</th>
                        <th>Type</th>
                        <th>Location & Identifier</th>
                        <th>Leg Distance</th>
                        <th>Cumulative Distance</th>
                        <th>Expected Waste</th>
                        <th>Cumulative Load</th>
                        <th class="text-end">Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:set var="stopsList" value="${isNewPlan ? routeResult.stops : plan.stops}"/>
                    <c:forEach var="stop" items="${stopsList}">
                        <tr class="${stop.stopType == 'DEPOT_START' || stop.stopType == 'DEPOT_END' ? 'table-light fw-semibold' : ''}">
                            <td class="text-center">
                                <span class="badge ${stop.stopType == 'DEPOT_START' || stop.stopType == 'DEPOT_END' ? 'bg-dark' : 'bg-success'} rounded-circle p-2">
                                    ${stop.stopOrder}
                                </span>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${stop.stopType == 'DEPOT_START'}">
                                        <span class="badge bg-secondary">DEPOT START</span>
                                    </c:when>
                                    <c:when test="${stop.stopType == 'DEPOT_END'}">
                                        <span class="badge bg-secondary">DEPOT RETURN</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-primary">WASTE BIN</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div class="fw-semibold text-dark"><c:out value="${stop.locationLabel}"/></div>
                                <c:if test="${not empty stop.address}">
                                    <div class="small text-muted"><c:out value="${stop.address}"/></div>
                                </c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${stop.stopOrder == 1}">0.0 km</c:when>
                                    <c:otherwise>+<fmt:formatNumber value="${stop.distanceFromPrevKm}" maxFractionDigits="2"/> km</c:otherwise>
                                </c:choose>
                            </td>
                            <td class="fw-bold text-success">
                                <fmt:formatNumber value="${stop.cumulativeDistanceKm}" maxFractionDigits="2"/> km
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${stop.stopType == 'BIN'}">
                                        <fmt:formatNumber value="${stop.expectedWasteKg}" maxFractionDigits="1"/> kg
                                    </c:when>
                                    <c:otherwise>&mdash;</c:otherwise>
                                </c:choose>
                            </td>
                            <td class="fw-semibold">
                                <fmt:formatNumber value="${stop.cumulativeLoadKg}" maxFractionDigits="1"/> kg
                            </td>
                            <td class="text-end">
                                <span class="badge ${stop.status == 'VISITED' ? 'bg-success' : 'bg-light text-dark border'}">
                                    <c:out value="${stop.status != null ? stop.status : 'PLANNED'}"/>
                                </span>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Task Assignment Form (Shown when previewing a newly generated plan) -->
        <c:if test="${isNewPlan}">
            <div class="card card-municipal">
                <div class="card-header bg-success-subtle">
                    <span class="fw-bold text-success-emphasis"><i class="bi bi-person-check-fill me-2"></i>Assign Operational Task to Worker</span>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/tasks/assign" method="POST">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="vehicleId" value="${routeResult.vehicleId}">

                        <div class="row g-3 align-items-end">
                            <div class="col-md-5">
                                <label for="workerId" class="form-label small fw-semibold">Select Available Worker <span class="text-danger">*</span></label>
                                <select id="workerId" name="workerId" class="form-select" required>
                                    <option value="">-- Choose Available Worker --</option>
                                    <c:forEach var="worker" items="${workers}">
                                        <option value="${worker.id}">
                                            <c:out value="${worker.fullName}"/> (<c:out value="${worker.workerCode}"/>) - <c:out value="${worker.phone}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-4">
                                <label for="plannedDate" class="form-label small fw-semibold">Scheduled Date <span class="text-danger">*</span></label>
                                <input type="date" class="form-control" id="plannedDate" name="plannedDate"
                                       value="<%= java.time.LocalDate.now().toString() %>" required>
                            </div>

                            <div class="col-md-3">
                                <button type="submit" class="btn btn-success w-100 shadow-sm"
                                        ${empty workers ? 'disabled' : ''}>
                                    <i class="bi bi-check2-circle me-1"></i> Confirm & Dispatch Task
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </c:if>

    </main>

<jsp:include page="../common/footer.jsp"/>
