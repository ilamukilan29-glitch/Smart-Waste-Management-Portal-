<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Collection Route Planner" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="mb-4">
            <h4 class="fw-bold mb-1 text-dark">Collection Route Optimization Engine</h4>
            <p class="text-muted small">Generates deterministic collection tours using the Nearest Neighbor heuristic with vehicle capacity constraints.</p>
        </div>

        <!-- Academic Viva Notice Banner -->
        <div class="alert alert-light border shadow-sm d-flex align-items-start mb-4">
            <i class="bi bi-lightbulb-fill text-warning fs-4 me-3 mt-1"></i>
            <div class="small text-muted">
                <strong class="text-dark">Algorithm Note (Academic Defense):</strong>
                This planner implements the <strong>Nearest Neighbor Heuristic</strong> for the Capacitated Vehicle Routing Problem (CVRP).
                Starting at the depot, the algorithm iteratively visits the closest unvisited bin that fits in the truck's capacity, biased by priority level, and returns to the depot.
                It executes in polynomial time $O(n^2)$. As a greedy heuristic, it finds an efficient practical route but does not mathematically guarantee a global optimum.
            </div>
        </div>

        <div class="card card-municipal mb-4">
            <div class="card-header bg-light">
                <span class="fw-bold text-dark"><i class="bi bi-1-circle-fill text-success me-2"></i>Step 1: Select Municipal Area</span>
            </div>
            <div class="card-body p-3">
                <form action="${pageContext.request.contextPath}/routes/plan" method="GET" class="row g-2 align-items-center">
                    <div class="col-12 col-md-6">
                        <select name="areaId" class="form-select form-select-sm" onchange="this.form.submit()">
                            <option value="">-- Choose Municipal Area to Load Bins & Fleet --</option>
                            <c:forEach var="area" items="${areas}">
                                <option value="${area.id}" ${selectedAreaId == area.id ? 'selected' : ''}>
                                    <c:out value="${area.name}"/> (<c:out value="${area.code}"/>)
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-auto">
                        <span class="small text-muted">Selecting an area filters available depots, trucks, and candidate bins.</span>
                    </div>
                </form>
            </div>
        </div>

        <c:if test="${not empty selectedAreaId}">
            <form action="${pageContext.request.contextPath}/routes/plan" method="POST">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="areaId" value="${selectedAreaId}">

                <div class="row g-4 mb-4">
                    <!-- Depot Selection -->
                    <div class="col-12 col-md-6">
                        <div class="card card-municipal h-100">
                            <div class="card-header">
                                <span class="fw-bold"><i class="bi bi-geo-alt-fill text-danger me-2"></i>Step 2: Starting & Ending Depot</span>
                            </div>
                            <div class="card-body p-3">
                                <c:choose>
                                    <c:when test="${empty depots}">
                                        <div class="alert alert-warning small mb-0">No depots registered for this area.</div>
                                    </c:when>
                                    <c:otherwise>
                                        <label for="depotId" class="form-label small fw-semibold">Base Municipal Depot <span class="text-danger">*</span></label>
                                        <select id="depotId" name="depotId" class="form-select" required>
                                            <c:forEach var="depot" items="${depots}">
                                                <option value="${depot.id}"><c:out value="${depot.name}"/> - <c:out value="${depot.address}"/></option>
                                            </c:forEach>
                                        </select>
                                        <div class="form-text small">Route initiates and concludes at this station (round trip).</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <!-- Vehicle Selection -->
                    <div class="col-12 col-md-6">
                        <div class="card card-municipal h-100">
                            <div class="card-header">
                                <span class="fw-bold"><i class="bi bi-truck text-primary me-2"></i>Step 3: Assign Available Vehicle</span>
                            </div>
                            <div class="card-body p-3">
                                <c:choose>
                                    <c:when test="${empty vehicles}">
                                        <div class="alert alert-warning small mb-0">No vehicles currently AVAILABLE in this area.</div>
                                    </c:when>
                                    <c:otherwise>
                                        <label for="vehicleId" class="form-label small fw-semibold">Available Collection Fleet <span class="text-danger">*</span></label>
                                        <select id="vehicleId" name="vehicleId" class="form-select" required>
                                            <c:forEach var="veh" items="${vehicles}">
                                                <option value="${veh.id}">
                                                    <c:out value="${veh.registrationNumber}"/> (<c:out value="${veh.vehicleType}"/>) - Capacity: <fmt:formatNumber value="${veh.capacityKg}" maxFractionDigits="0"/> kg
                                                </option>
                                            </c:forEach>
                                        </select>
                                        <div class="form-text small">Optimizer enforces this carrying capacity constraint.</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Step 4: Candidate Bins Selection Table -->
                <div class="card card-municipal mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <div>
                            <span class="fw-bold"><i class="bi bi-trash3-fill text-success me-2"></i>Step 4: Select Bins for Collection</span>
                            <span class="badge bg-light text-dark border ms-2"><span id="selectedCountBadge">0</span> bins selected</span>
                        </div>
                        <div>
                            <div class="form-check form-check-inline small mb-0">
                                <input class="form-check-input" type="checkbox" id="selectAllBins">
                                <label class="form-check-label fw-semibold" for="selectAllBins">Select All Eligible</label>
                            </div>
                        </div>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-municipal table-hover mb-0">
                            <thead>
                            <tr>
                                <th style="width: 40px;"></th>
                                <th>Bin Code</th>
                                <th>Location</th>
                                <th>Waste Type</th>
                                <th>Capacity</th>
                                <th style="min-width: 140px;">Fill Level</th>
                                <th>Priority</th>
                                <th>Estimated Waste</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:choose>
                                <c:when test="${empty eligibleBins}">
                                    <tr>
                                        <td colspan="8" class="text-center py-5 text-muted">
                                            <i class="bi bi-inbox fs-2"></i>
                                            <p class="mt-2 mb-0">No unassigned bins with fill level &ge; 60% found in this area.</p>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="bin" items="${eligibleBins}">
                                        <tr>
                                            <td>
                                                <input type="checkbox" name="binIds" value="${bin.id}" class="form-check-input bin-checkbox"
                                                       ${bin.currentFillPercentage >= 80 ? 'checked' : ''}>
                                            </td>
                                            <td class="fw-bold text-success"><c:out value="${bin.binCode}"/></td>
                                            <td>
                                                <div class="fw-semibold text-dark"><c:out value="${bin.locationName}"/></div>
                                                <div class="small text-muted"><c:out value="${bin.address}"/></div>
                                            </td>
                                            <td><span class="badge bg-light text-dark border"><c:out value="${bin.wasteType.displayName}"/></span></td>
                                            <td><fmt:formatNumber value="${bin.capacityKg}" maxFractionDigits="1"/> kg</td>
                                            <td>
                                                <div class="small fw-bold mb-1"><fmt:formatNumber value="${bin.currentFillPercentage}" maxFractionDigits="1"/>%</div>
                                                <div class="fill-progress-container">
                                                    <div class="fill-progress-bar ${bin.currentFillPercentage >= 90 ? 'fill-bar-critical' : (bin.currentFillPercentage >= 80 ? 'fill-bar-high' : 'fill-bar-medium')}"
                                                         style="width: ${bin.currentFillPercentage}%;"></div>
                                                </div>
                                            </td>
                                            <td>
                                                <span class="badge ${bin.priorityLevel == 'CRITICAL' ? 'badge-priority-critical' : (bin.priorityLevel == 'HIGH' ? 'badge-priority-high' : 'badge-priority-medium')}">
                                                    <c:out value="${bin.priorityLevel.displayName}"/>
                                                </span>
                                            </td>
                                            <td class="fw-bold text-dark"><fmt:formatNumber value="${bin.estimatedWasteKg}" maxFractionDigits="1"/> kg</td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>
                    <div class="card-footer bg-white p-3 text-end">
                        <button type="submit" id="generateRouteBtn" class="btn btn-success px-4 shadow-sm"
                                ${empty eligibleBins || empty depots || empty vehicles ? 'disabled' : ''}>
                            <i class="bi bi-lightning-charge-fill me-1"></i> Generate Nearest Neighbor Route &rarr;
                        </button>
                    </div>
                </div>
            </form>
        </c:if>

    </main>

<script>
document.addEventListener("DOMContentLoaded", function() {
    updateSelectedSummary();
});
</script>

<jsp:include page="../common/footer.jsp"/>
