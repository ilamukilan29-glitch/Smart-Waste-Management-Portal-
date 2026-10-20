<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Bin Details - ${bin.binCode}" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <a href="${pageContext.request.contextPath}/bins" class="text-decoration-none text-muted small">
                    <i class="bi bi-arrow-left me-1"></i> Back to Waste Bins
                </a>
                <h4 class="fw-bold mb-1 mt-2 text-dark">Waste Bin: <c:out value="${bin.binCode}"/></h4>
                <p class="text-muted small"><c:out value="${bin.locationName}"/> &bull; <c:out value="${bin.areaName}"/></p>
            </div>
            <div class="d-flex gap-2">
                <c:if test="${sessionScope.LOGGED_IN_USER.role == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/bins/edit?id=${bin.id}" class="btn btn-outline-primary btn-sm">
                        <i class="bi bi-pencil me-1"></i> Edit Details
                    </a>
                </c:if>
            </div>
        </div>

        <div class="row g-4">
            <!-- Left Column: Specs and Visual Gauge -->
            <div class="col-12 col-lg-6">
                <div class="card card-municipal h-100">
                    <div class="card-header">
                        <span class="fw-bold"><i class="bi bi-speedometer2 text-success me-2"></i>Live Monitoring Gauge</span>
                    </div>
                    <div class="card-body p-4 text-center">
                        <div class="mb-4">
                            <span class="display-3 fw-bold ${bin.currentFillPercentage >= 90 ? 'text-danger' : (bin.currentFillPercentage >= 80 ? 'text-warning' : (bin.currentFillPercentage >= 60 ? 'text-info' : 'text-success'))}">
                                <fmt:formatNumber value="${bin.currentFillPercentage}" maxFractionDigits="1"/>%
                            </span>
                            <div class="text-muted small">Current Fill Capacity</div>
                        </div>

                        <div class="fill-progress-container mb-3" style="height: 16px;">
                            <div class="fill-progress-bar ${bin.currentFillPercentage >= 90 ? 'fill-bar-critical' : (bin.currentFillPercentage >= 80 ? 'fill-bar-high' : (bin.currentFillPercentage >= 60 ? 'fill-bar-medium' : 'fill-bar-low'))}"
                                 style="width: ${bin.currentFillPercentage}%;"></div>
                        </div>

                        <div class="d-flex justify-content-between small text-muted mb-4">
                            <span>0 kg (Empty)</span>
                            <span><strong><fmt:formatNumber value="${bin.estimatedWasteKg}" maxFractionDigits="1"/> kg</strong> accumulated</span>
                            <span><fmt:formatNumber value="${bin.capacityKg}" maxFractionDigits="1"/> kg (Full)</span>
                        </div>

                        <div class="p-3 bg-light rounded-3 text-start mb-4 border">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <span class="text-muted small">Engine Priority Classification:</span>
                                <span class="badge ${bin.priorityLevel == 'CRITICAL' ? 'badge-priority-critical' : (bin.priorityLevel == 'HIGH' ? 'badge-priority-high' : (bin.priorityLevel == 'MEDIUM' ? 'badge-priority-medium' : 'badge-priority-low'))}">
                                    <c:out value="${bin.priorityLevel.displayName}"/>
                                </span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <span class="text-muted small">Last Collection:</span>
                                <span class="fw-semibold text-dark">
                                    <c:out value="${bin.lastCollectionAt != null ? bin.lastCollectionAt : 'Never / Pending'}"/>
                                </span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center">
                                <span class="text-muted small">Last Status Update:</span>
                                <span class="fw-semibold text-dark"><c:out value="${bin.lastReportedAt}"/></span>
                            </div>
                        </div>

                        <!-- Manual Fill Update Form -->
                        <form action="${pageContext.request.contextPath}/bins/update-fill" method="POST" class="row g-2 justify-content-center">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="id" value="${bin.id}">
                            <div class="col-8 col-sm-6">
                                <div class="input-group input-group-sm">
                                    <input type="number" step="0.1" min="0" max="100" name="fillPercentage"
                                           class="form-control" value="${bin.currentFillPercentage}" required>
                                    <span class="input-group-text">%</span>
                                </div>
                            </div>
                            <div class="col-auto">
                                <button type="submit" class="btn btn-sm btn-outline-success">Update Fill</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Right Column: Location & Specifications -->
            <div class="col-12 col-lg-6">
                <div class="card card-municipal h-100">
                    <div class="card-header">
                        <span class="fw-bold"><i class="bi bi-geo-alt-fill text-primary me-2"></i>Location & Specifications</span>
                    </div>
                    <div class="card-body p-4">
                        <table class="table table-sm mb-4">
                            <tbody>
                            <tr>
                                <th class="text-muted fw-normal" style="width: 40%;">Bin Code</th>
                                <td class="fw-bold"><c:out value="${bin.binCode}"/></td>
                            </tr>
                            <tr>
                                <th class="text-muted fw-normal">Location Name</th>
                                <td class="fw-semibold"><c:out value="${bin.locationName}"/></td>
                            </tr>
                            <tr>
                                <th class="text-muted fw-normal">Street Address</th>
                                <td><c:out value="${bin.address}"/></td>
                            </tr>
                            <tr>
                                <th class="text-muted fw-normal">Municipal Area</th>
                                <td><span class="badge bg-light text-dark border"><c:out value="${bin.areaName}"/></span></td>
                            </tr>
                            <tr>
                                <th class="text-muted fw-normal">Waste Category</th>
                                <td><span class="badge bg-light text-dark border"><c:out value="${bin.wasteType.displayName}"/></span></td>
                            </tr>
                            <tr>
                                <th class="text-muted fw-normal">Maximum Capacity</th>
                                <td class="fw-semibold"><fmt:formatNumber value="${bin.capacityKg}" maxFractionDigits="1"/> kg</td>
                            </tr>
                            <tr>
                                <th class="text-muted fw-normal">GPS Coordinates</th>
                                <td class="font-monospace small"><c:out value="${bin.latitude}"/>, <c:out value="${bin.longitude}"/></td>
                            </tr>
                            <tr>
                                <th class="text-muted fw-normal">Operational Status</th>
                                <td>
                                    <span class="badge ${bin.active ? 'bg-success' : 'bg-secondary'}"><c:out value="${bin.status}"/></span>
                                </td>
                            </tr>
                            </tbody>
                        </table>

                        <div class="p-3 bg-light rounded-3 border">
                            <h6 class="fw-bold small mb-2"><i class="bi bi-diagram-3-fill text-success me-1"></i>Routing Eligibility Status</h6>
                            <c:choose>
                                <c:when test="${bin.currentFillPercentage >= 60 && bin.active}">
                                    <div class="text-success small fw-semibold">
                                        <i class="bi bi-check-circle-fill me-1"></i> This bin is currently eligible for route optimization.
                                    </div>
                                    <div class="mt-2">
                                        <a href="${pageContext.request.contextPath}/routes/plan?areaId=${bin.areaId}" class="btn btn-sm btn-success">
                                            Include in Route Plan &rarr;
                                        </a>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="text-muted small">
                                        <i class="bi bi-info-circle me-1"></i> Fill level is below collection threshold (60%) or bin is inactive.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </main>

<jsp:include page="../common/footer.jsp"/>
