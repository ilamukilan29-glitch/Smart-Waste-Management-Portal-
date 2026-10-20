<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Municipal Operations Dashboard" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <!-- Top Header & Actions -->
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
            <div>
                <h4 class="fw-bold mb-1 text-dark">Real-Time Operational Dashboard</h4>
                <p class="text-muted mb-0 small">Live municipal waste metrics, priority alerts, and collection status.</p>
            </div>
            <div class="d-flex gap-2">
                <c:if test="${sessionScope.LOGGED_IN_USER.role == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/simulation" class="btn btn-outline-secondary btn-sm shadow-sm">
                        <i class="bi bi-cpu me-1"></i> Simulate Bins
                    </a>
                    <a href="${pageContext.request.contextPath}/routes/plan" class="btn btn-success btn-sm shadow-sm">
                        <i class="bi bi-map-fill me-1"></i> Plan Route
                    </a>
                    <a href="${pageContext.request.contextPath}/bins/create" class="btn btn-outline-success btn-sm shadow-sm">
                        <i class="bi bi-plus-lg me-1"></i> Register Bin
                    </a>
                </c:if>
            </div>
        </div>

        <!-- Metric KPI Cards Row 1 -->
        <div class="row g-3 mb-4">
            <!-- Total Bins -->
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="metric-label">Registered Bins</div>
                            <div class="metric-val text-dark">${metrics.totalBins}</div>
                        </div>
                        <div class="metric-icon-box bg-light text-primary">
                            <i class="bi bi-trash3-fill"></i>
                        </div>
                    </div>
                    <div class="small text-muted mt-2">Monitored municipal bins</div>
                </div>
            </div>

            <!-- Critical Bins -->
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card border-danger">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="metric-label text-danger fw-bold">Critical Bins (&ge; 90%)</div>
                            <div class="metric-val text-danger">${metrics.criticalBins}</div>
                        </div>
                        <div class="metric-icon-box bg-danger-subtle text-danger">
                            <i class="bi bi-exclamation-triangle-fill"></i>
                        </div>
                    </div>
                    <div class="small text-danger mt-2">
                        <c:choose>
                            <c:when test="${metrics.criticalBins > 0}">
                                <a href="${pageContext.request.contextPath}/priority" class="text-danger fw-bold text-decoration-none">
                                    Immediate dispatch needed &rarr;
                                </a>
                            </c:when>
                            <c:otherwise>All bins within safe thresholds</c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Bins Requiring Collection -->
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card border-warning">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="metric-label text-warning-emphasis fw-bold">Eligible for Collection</div>
                            <div class="metric-val text-dark">${metrics.binsRequiringCollection}</div>
                        </div>
                        <div class="metric-icon-box bg-warning-subtle text-warning">
                            <i class="bi bi-hourglass-split"></i>
                        </div>
                    </div>
                    <div class="small text-muted mt-2">&ge; 80% fill capacity threshold</div>
                </div>
            </div>

            <!-- Pending Tasks -->
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="metric-card">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="metric-label">Pending / Active Tasks</div>
                            <div class="metric-val text-primary">${metrics.pendingTasks}</div>
                        </div>
                        <div class="metric-icon-box bg-primary-subtle text-primary">
                            <i class="bi bi-clipboard-data"></i>
                        </div>
                    </div>
                    <div class="small text-muted mt-2">${metrics.collectionsCompletedToday} completed today</div>
                </div>
            </div>
        </div>

        <!-- Metric KPI Cards Row 2 -->
        <div class="row g-3 mb-4">
            <!-- Available Workers -->
            <div class="col-12 col-sm-6 col-xl-4">
                <div class="metric-card">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="metric-label">Available Workers</div>
                            <div class="metric-val text-success">${metrics.availableWorkers}</div>
                        </div>
                        <div class="metric-icon-box bg-success-subtle text-success">
                            <i class="bi bi-person-check-fill"></i>
                        </div>
                    </div>
                    <div class="small text-muted mt-2">Ready for route assignment</div>
                </div>
            </div>

            <!-- Available Vehicles -->
            <div class="col-12 col-sm-6 col-xl-4">
                <div class="metric-card">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="metric-label">Available Fleet Vehicles</div>
                            <div class="metric-val text-info">${metrics.availableVehicles}</div>
                        </div>
                        <div class="metric-icon-box bg-info-subtle text-info">
                            <i class="bi bi-truck-front-fill"></i>
                        </div>
                    </div>
                    <div class="small text-muted mt-2">Depot ready collection trucks</div>
                </div>
            </div>

            <!-- Completion Rate -->
            <div class="col-12 col-sm-6 col-xl-4">
                <div class="metric-card">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="metric-label">Collection Completion Rate</div>
                            <div class="metric-val text-primary">${metrics.completionPercentage}%</div>
                        </div>
                        <div class="metric-icon-box bg-light text-primary">
                            <i class="bi bi-percent"></i>
                        </div>
                    </div>
                    <div class="small text-muted mt-2">Historical assigned task efficiency</div>
                </div>
            </div>
        </div>

        <!-- Visual Analytics Row (Charts) -->
        <div class="row g-3 mb-4">
            <!-- Priority Distribution Doughnut Chart -->
            <div class="col-12 col-lg-5">
                <div class="card card-municipal h-100">
                    <div class="card-header">
                        <span class="fw-bold"><i class="bi bi-pie-chart-fill text-success me-2"></i>Bin Priority Breakdown</span>
                    </div>
                    <div class="card-body d-flex flex-column justify-content-center" style="min-height: 280px;">
                        <c:choose>
                            <c:when test="${metrics.totalBins == 0}">
                                <div class="text-center text-muted py-5">
                                    <i class="bi bi-inbox fs-1"></i>
                                    <p class="mt-2 mb-0">No waste bins registered in database.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div style="height: 240px; position: relative;">
                                    <canvas id="priorityChart"></canvas>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Waste Types Distribution Bar Chart -->
            <div class="col-12 col-lg-7">
                <div class="card card-municipal h-100">
                    <div class="card-header">
                        <span class="fw-bold"><i class="bi bi-bar-chart-fill text-primary me-2"></i>Bins by Waste Type</span>
                    </div>
                    <div class="card-body" style="min-height: 280px;">
                        <c:choose>
                            <c:when test="${metrics.totalBins == 0}">
                                <div class="text-center text-muted py-5">
                                    <i class="bi bi-inbox fs-1"></i>
                                    <p class="mt-2 mb-0">No waste bins registered in database.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div style="height: 240px; position: relative;">
                                    <canvas id="wasteTypeChart"></canvas>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>

        <!-- Operational Quick Links / Shortcuts -->
        <div class="card card-municipal">
            <div class="card-header">
                <span class="fw-bold"><i class="bi bi-lightning-charge-fill text-warning me-2"></i>Municipal Workflow Guidance</span>
            </div>
            <div class="card-body">
                <div class="row g-3">
                    <div class="col-md-4">
                        <div class="p-3 border rounded-3 bg-light h-100">
                            <h6 class="fw-bold text-dark"><i class="bi bi-1-circle-fill text-success me-2"></i>1. Check Priorities</h6>
                            <p class="small text-muted mb-2">Review bins categorized as Critical (&ge;90%) and High (&ge;80%) in the collection queue.</p>
                            <a href="${pageContext.request.contextPath}/priority" class="btn btn-sm btn-outline-success">View Priority Queue &rarr;</a>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="p-3 border rounded-3 bg-light h-100">
                            <h6 class="fw-bold text-dark"><i class="bi bi-2-circle-fill text-success me-2"></i>2. Optimize Route</h6>
                            <p class="small text-muted mb-2">Select eligible bins and run Nearest Neighbor routing with road distance matrix.</p>
                            <a href="${pageContext.request.contextPath}/routes/plan" class="btn btn-sm btn-outline-primary">Run Route Planner &rarr;</a>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="p-3 border rounded-3 bg-light h-100">
                            <h6 class="fw-bold text-dark"><i class="bi bi-3-circle-fill text-success me-2"></i>3. Assign & Track</h6>
                            <p class="small text-muted mb-2">Assign route plan to available truck and worker. Worker records collected weight.</p>
                            <a href="${pageContext.request.contextPath}/tasks" class="btn btn-sm btn-outline-dark">Track Tasks &rarr;</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </main>

<script>
document.addEventListener("DOMContentLoaded", function() {
    <c:if test="${metrics.totalBins > 0}">
        // Priority Doughnut Chart
        const priorityLabels = ['Critical', 'High', 'Medium', 'Low'];
        const priorityData = [
            ${metrics.binsByPriority['Critical'] != null ? metrics.binsByPriority['Critical'] : 0},
            ${metrics.binsByPriority['High'] != null ? metrics.binsByPriority['High'] : 0},
            ${metrics.binsByPriority['Medium'] != null ? metrics.binsByPriority['Medium'] : 0},
            ${metrics.binsByPriority['Low'] != null ? metrics.binsByPriority['Low'] : 0}
        ];
        initPriorityChart('priorityChart', priorityLabels, priorityData);

        // Waste Types Bar Chart
        const wasteLabels = [];
        const wasteData = [];
        <c:forEach var="entry" items="${metrics.binsByWasteType}">
            wasteLabels.push("${entry.key}");
            wasteData.push(${entry.value});
        </c:forEach>
        initWasteTypeChart('wasteTypeChart', wasteLabels, wasteData);
    </c:if>
});
</script>

<jsp:include page="../common/footer.jsp"/>
