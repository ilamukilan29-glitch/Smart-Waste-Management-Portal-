<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Operational Reports & Analytics" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
            <div>
                <h4 class="fw-bold mb-1 text-dark">Municipal Operational Reports</h4>
                <p class="text-muted small">Analytics on waste collections, volume weights, worker performance, and fleet output.</p>
            </div>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/reports/export/csv?startDate=${startDate}&endDate=${endDate}&areaId=${selectedAreaId}"
                   class="btn btn-outline-success btn-sm shadow-sm">
                    <i class="bi bi-file-earmark-spreadsheet me-1"></i> Export CSV
                </a>
                <a href="${pageContext.request.contextPath}/reports/print?startDate=${startDate}&endDate=${endDate}&areaId=${selectedAreaId}"
                   target="_blank" class="btn btn-outline-secondary btn-sm shadow-sm">
                    <i class="bi bi-printer me-1"></i> Print / Save PDF
                </a>
            </div>
        </div>

        <!-- Filter Toolbar -->
        <div class="card card-municipal mb-4">
            <div class="card-body p-3">
                <form action="${pageContext.request.contextPath}/reports" method="GET" class="row g-2 align-items-center">
                    <div class="col-12 col-md-3">
                        <label class="form-label small text-muted mb-1">From Date</label>
                        <input type="date" name="startDate" class="form-control form-control-sm" value="${startDate}">
                    </div>
                    <div class="col-12 col-md-3">
                        <label class="form-label small text-muted mb-1">To Date</label>
                        <input type="date" name="endDate" class="form-control form-control-sm" value="${endDate}">
                    </div>
                    <div class="col-12 col-md-4">
                        <label class="form-label small text-muted mb-1">Municipal Area</label>
                        <select name="areaId" class="form-select form-select-sm">
                            <option value="">All Municipal Areas</option>
                            <c:forEach var="area" items="${areas}">
                                <option value="${area.id}" ${selectedAreaId == area.id ? 'selected' : ''}>
                                    <c:out value="${area.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-12 col-md-2 align-self-end">
                        <button type="submit" class="btn btn-sm btn-primary w-100">
                            <i class="bi bi-filter me-1"></i> Apply Filters
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- KPI Metrics Row -->
        <div class="row g-3 mb-4">
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card">
                    <div class="metric-label">Total Collections</div>
                    <div class="metric-val text-dark">${summary.totalCollections}</div>
                    <div class="small text-muted mt-1">Dispatched in date range</div>
                </div>
            </div>

            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card border-success">
                    <div class="metric-label text-success fw-bold">Completed Tasks</div>
                    <div class="metric-val text-success">${summary.completedCollections}</div>
                    <div class="small text-muted mt-1">Successfully collected tours</div>
                </div>
            </div>

            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card border-warning">
                    <div class="metric-label text-warning-emphasis fw-bold">Pending / Active</div>
                    <div class="metric-val text-dark">${summary.pendingCollections}</div>
                    <div class="small text-muted mt-1">Awaiting completion</div>
                </div>
            </div>

            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card">
                    <div class="metric-label">Total Waste Collected</div>
                    <div class="metric-val text-primary">
                        <fmt:formatNumber value="${summary.totalWasteCollectedKg}" maxFractionDigits="1"/> kg
                    </div>
                    <div class="small text-muted mt-1">Measured at disposal depot</div>
                </div>
            </div>
        </div>

        <!-- Chart Breakdown -->
        <div class="card card-municipal mb-4">
            <div class="card-header">
                <span class="fw-bold"><i class="bi bi-pie-chart-fill text-success me-2"></i>Waste Collected by Category (kg)</span>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty summary.wasteCollectedByType}">
                        <div class="text-center text-muted py-4">No completed collection records in this date range.</div>
                    </c:when>
                    <c:otherwise>
                        <div class="row align-items-center">
                            <div class="col-md-6" style="height: 250px;">
                                <canvas id="reportWasteChart"></canvas>
                            </div>
                            <div class="col-md-6">
                                <ul class="list-group list-group-flush">
                                    <c:forEach var="entry" items="${summary.wasteCollectedByType}">
                                        <li class="list-group-item d-flex justify-content-between align-items-center px-0">
                                            <span><c:out value="${entry.key}"/></span>
                                            <span class="fw-bold text-dark"><fmt:formatNumber value="${entry.value}" maxFractionDigits="1"/> kg</span>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Worker Performance Table -->
        <div class="card card-municipal">
            <div class="card-header">
                <span class="fw-bold"><i class="bi bi-people-fill text-primary me-2"></i>Personnel Collection Performance</span>
            </div>
            <div class="table-responsive">
                <table class="table table-municipal table-hover mb-0">
                    <thead>
                    <tr>
                        <th>Worker Code</th>
                        <th>Personnel Name</th>
                        <th>Contact</th>
                        <th>Assigned Area</th>
                        <th>Assigned Tasks</th>
                        <th>Completed</th>
                        <th>Pending</th>
                        <th>Waste Collected (kg)</th>
                        <th>Completion Rate</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty summary.workerPerformances}">
                            <tr>
                                <td colspan="9" class="text-center py-4 text-muted">No worker activity recorded.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="w" items="${summary.workerPerformances}">
                                <tr>
                                    <td class="fw-bold text-success"><c:out value="${w.workerCode}"/></td>
                                    <td class="fw-semibold text-dark"><c:out value="${w.fullName}"/></td>
                                    <td><c:out value="${w.phone}"/></td>
                                    <td><span class="badge bg-light text-dark border"><c:out value="${w.areaName}"/></span></td>
                                    <td>${w.totalAssignedTasks}</td>
                                    <td class="fw-bold text-success">${w.completedTasks}</td>
                                    <td class="text-warning-emphasis">${w.pendingTasks}</td>
                                    <td class="fw-semibold"><fmt:formatNumber value="${w.totalCollectedKg}" maxFractionDigits="1"/> kg</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <div class="progress flex-grow-1" style="height: 6px;">
                                                <div class="progress-bar bg-success" style="width: ${w.completionRate}%;"></div>
                                            </div>
                                            <span class="small fw-bold">${w.completionRate}%</span>
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

<script>
document.addEventListener("DOMContentLoaded", function() {
    <c:if test="${not empty summary.wasteCollectedByType}">
        const labels = [];
        const data = [];
        <c:forEach var="entry" items="${summary.wasteCollectedByType}">
            labels.push("${entry.key}");
            data.push(${entry.value});
        </c:forEach>

        const ctx = document.getElementById("reportWasteChart");
        if (ctx) {
            new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: labels,
                    datasets: [{
                        data: data,
                        backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#8b5cf6', '#64748b']
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom' } }
                }
            });
        }
    </c:if>
});
</script>

<jsp:include page="../common/footer.jsp"/>
