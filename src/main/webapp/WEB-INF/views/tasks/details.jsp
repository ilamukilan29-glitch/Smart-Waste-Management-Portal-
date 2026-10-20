<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Task Details - ${task.taskCode}" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <a href="${pageContext.request.contextPath}/tasks" class="text-decoration-none text-muted small">
                    <i class="bi bi-arrow-left me-1"></i> Back to Collection Tasks
                </a>
                <h4 class="fw-bold mb-1 mt-2 text-dark">Collection Task: <c:out value="${task.taskCode}"/></h4>
                <p class="text-muted small">Assigned to: <strong><c:out value="${task.workerName}"/></strong> &bull; Vehicle: <strong><c:out value="${task.vehicleRegistration}"/></strong></p>
            </div>
            <div class="d-flex gap-2">
                <span class="badge ${task.status == 'COMPLETED' ? 'bg-success' : (task.status == 'IN_PROGRESS' ? 'bg-warning text-dark' : 'bg-primary')} fs-6">
                    <c:out value="${task.status.displayName}"/>
                </span>

                <c:if test="${task.status == 'ASSIGNED'}">
                    <form action="${pageContext.request.contextPath}/tasks/start" method="POST" class="d-inline">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="taskId" value="${task.id}">
                        <button type="submit" class="btn btn-warning btn-sm">
                            <i class="bi bi-play-circle-fill me-1"></i> Start Task
                        </button>
                    </form>
                </c:if>

                <c:if test="${task.status == 'IN_PROGRESS' || task.status == 'ASSIGNED'}">
                    <button type="button" class="btn btn-success btn-sm shadow-sm" data-bs-toggle="modal" data-bs-target="#completeModal">
                        <i class="bi bi-check2-circle me-1"></i> Record & Complete Task
                    </button>
                </c:if>
            </div>
        </div>

        <!-- Task Metadata Cards -->
        <div class="row g-3 mb-4">
            <div class="col-12 col-md-3">
                <div class="metric-card">
                    <div class="metric-label">Scheduled Date</div>
                    <div class="fw-bold text-dark fs-5 mt-1"><c:out value="${task.plannedDate}"/></div>
                </div>
            </div>
            <div class="col-12 col-md-3">
                <div class="metric-card">
                    <div class="metric-label">Total Tour Distance</div>
                    <div class="fw-bold text-success fs-5 mt-1">
                        <fmt:formatNumber value="${plan != null ? plan.totalDistanceKm : 0}" maxFractionDigits="2"/> km
                    </div>
                </div>
            </div>
            <div class="col-12 col-md-3">
                <div class="metric-card">
                    <div class="metric-label">Collected Waste Weight</div>
                    <div class="fw-bold text-dark fs-5 mt-1">
                        <c:choose>
                            <c:when test="${task.status == 'COMPLETED'}">
                                <fmt:formatNumber value="${task.collectedWeightKg}" maxFractionDigits="1"/> kg
                            </c:when>
                            <c:otherwise>
                                Est. <fmt:formatNumber value="${plan != null ? plan.totalWasteEstimatedKg : 0}" maxFractionDigits="1"/> kg
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            <div class="col-12 col-md-3">
                <div class="metric-card">
                    <div class="metric-label">Execution Timestamps</div>
                    <div class="small text-muted mt-1">
                        Start: <c:out value="${task.actualStartAt != null ? task.actualStartAt : 'Pending'}"/><br>
                        End: <c:out value="${task.actualCompletedAt != null ? task.actualCompletedAt : 'Pending'}"/>
                    </div>
                </div>
            </div>
        </div>

        <c:if test="${not empty task.remarks}">
            <div class="alert alert-light border shadow-sm mb-4">
                <strong>Worker Remarks:</strong> <c:out value="${task.remarks}"/>
            </div>
        </c:if>

        <!-- Route Stops for this Task -->
        <div class="card card-municipal mb-4">
            <div class="card-header bg-light">
                <span class="fw-bold text-dark"><i class="bi bi-geo-alt-fill text-danger me-2"></i>Route Stops in Task Tour</span>
            </div>
            <div class="table-responsive">
                <table class="table table-municipal mb-0">
                    <thead>
                    <tr>
                        <th style="width: 70px;">Stop #</th>
                        <th>Type</th>
                        <th>Location & Identifier</th>
                        <th>Leg Distance</th>
                        <th>Cumulative Distance</th>
                        <th>Expected Waste</th>
                        <th class="text-end">Stop Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="stop" items="${plan.stops}">
                        <tr>
                            <td class="text-center fw-bold">${stop.stopOrder}</td>
                            <td><span class="badge ${stop.stopType == 'BIN' ? 'bg-primary' : 'bg-dark'}"><c:out value="${stop.stopType}"/></span></td>
                            <td class="fw-semibold text-dark"><c:out value="${stop.locationLabel}"/></td>
                            <td>+<fmt:formatNumber value="${stop.distanceFromPrevKm}" maxFractionDigits="2"/> km</td>
                            <td class="fw-bold text-success"><fmt:formatNumber value="${stop.cumulativeDistanceKm}" maxFractionDigits="2"/> km</td>
                            <td>
                                <c:choose>
                                    <c:when test="${stop.stopType == 'BIN'}"><fmt:formatNumber value="${stop.expectedWasteKg}" maxFractionDigits="1"/> kg</c:when>
                                    <c:otherwise>&mdash;</c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end">
                                <span class="badge ${stop.status == 'VISITED' ? 'bg-success' : 'bg-light text-dark border'}">
                                    <c:out value="${stop.status}"/>
                                </span>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Complete Task Modal -->
        <div class="modal fade" id="completeModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <form action="${pageContext.request.contextPath}/tasks/complete" method="POST">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="taskId" value="${task.id}">

                        <div class="modal-header bg-success-subtle">
                            <h6 class="modal-title fw-bold text-success-emphasis">
                                <i class="bi bi-check2-circle me-1"></i> Complete Collection Task: ${task.taskCode}
                            </h6>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body p-4">
                            <p class="small text-muted mb-3">
                                Marking this task completed will transactionally reset all visited bin fill levels to 0.0%, record actual collection weight, and release worker/vehicle availability back to AVAILABLE.
                            </p>

                            <div class="mb-3">
                                <label for="collectedWeightKg" class="form-label small fw-semibold">Actual Collected Weight (kg) <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="number" step="0.1" min="0" class="form-control" id="collectedWeightKg" name="collectedWeightKg"
                                           value="<fmt:formatNumber value='${plan != null ? plan.totalWasteEstimatedKg : 0}' maxFractionDigits='1'/>" required>
                                    <span class="input-group-text">kg</span>
                                </div>
                                <div class="form-text small">Enter the total weight weighed at the disposal facility.</div>
                            </div>

                            <div class="mb-3">
                                <label for="remarks" class="form-label small fw-semibold">Worker Remarks / Field Notes</label>
                                <textarea class="form-control" id="remarks" name="remarks" rows="3"
                                          placeholder="e.g. Route completed smoothly. All bins emptied and sanitized."></textarea>
                            </div>
                        </div>
                        <div class="modal-footer p-3">
                            <button type="button" class="btn btn-light border" data-bs-dismiss="modal">Cancel</button>
                            <button type="submit" class="btn btn-success px-4">
                                <i class="bi bi-check-lg me-1"></i> Submit & Finish Task
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

    </main>

<jsp:include page="../common/footer.jsp"/>
