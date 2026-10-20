<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Waste Bins Management" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
            <div>
                <h4 class="fw-bold mb-1 text-dark">Waste Bins Inventory</h4>
                <p class="text-muted mb-0 small">Monitor manually reported fill levels, locations, and capacity.</p>
            </div>
            <div class="d-flex gap-2">
                <c:if test="${sessionScope.LOGGED_IN_USER.role == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/simulation" class="btn btn-outline-secondary btn-sm shadow-sm">
                        <i class="bi bi-cpu me-1"></i> Fill Simulation
                    </a>
                    <a href="${pageContext.request.contextPath}/bins/create" class="btn btn-success btn-sm shadow-sm">
                        <i class="bi bi-plus-lg me-1"></i> Add New Bin
                    </a>
                </c:if>
            </div>
        </div>

        <!-- Filter & Search Toolbar -->
        <div class="card card-municipal mb-4">
            <div class="card-body p-3">
                <form action="${pageContext.request.contextPath}/bins" method="GET" class="row g-2 align-items-center">
                    <!-- Search Input -->
                    <div class="col-12 col-md-4">
                        <div class="input-group input-group-sm">
                            <span class="input-group-text bg-light text-muted"><i class="bi bi-search"></i></span>
                            <input type="text" class="form-control" name="search" placeholder="Search bin code, location..."
                                   value="<c:out value='${search}'/>">
                        </div>
                    </div>

                    <!-- Area Filter -->
                    <div class="col-6 col-md-3">
                        <select name="areaId" class="form-select form-select-sm">
                            <option value="">All Municipal Areas</option>
                            <c:forEach var="area" items="${areas}">
                                <option value="${area.id}" ${selectedAreaId == area.id ? 'selected' : ''}>
                                    <c:out value="${area.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <!-- Waste Type Filter -->
                    <div class="col-6 col-md-2">
                        <select name="wasteType" class="form-select form-select-sm">
                            <option value="">All Waste Types</option>
                            <c:forEach var="type" items="${wasteTypes}">
                                <option value="${type.name()}" ${selectedWasteType == type ? 'selected' : ''}>
                                    <c:out value="${type.displayName}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <!-- Status Filter -->
                    <div class="col-6 col-md-2">
                        <select name="status" class="form-select form-select-sm">
                            <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>Active Bins</option>
                            <option value="INACTIVE" ${selectedStatus == 'INACTIVE' ? 'selected' : ''}>Inactive Bins</option>
                            <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                        </select>
                    </div>

                    <!-- Filter & Reset Buttons -->
                    <div class="col-6 col-md-1 d-flex gap-1">
                        <button type="submit" class="btn btn-sm btn-primary w-100">Filter</button>
                        <a href="${pageContext.request.contextPath}/bins" class="btn btn-sm btn-light border text-muted" title="Reset Filters">
                            <i class="bi bi-arrow-counterclockwise"></i>
                        </a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Bins Data Table -->
        <div class="card card-municipal">
            <div class="card-header">
                <span class="fw-bold">Bins Inventory (${bins.size()} items)</span>
            </div>
            <div class="table-responsive">
                <table class="table table-municipal table-hover mb-0">
                    <thead>
                    <tr>
                        <th>Bin Code</th>
                        <th>Location & Area</th>
                        <th>Waste Type</th>
                        <th>Capacity</th>
                        <th style="min-width: 180px;">Fill Level</th>
                        <th>Priority</th>
                        <th>Status</th>
                        <th class="text-end">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty bins}">
                            <tr>
                                <td colspan="8" class="text-center py-5 text-muted">
                                    <i class="bi bi-inbox fs-2"></i>
                                    <p class="mt-2 mb-0">No waste bins found matching your filter criteria.</p>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="bin" items="${bins}">
                                <tr>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/bins/details?id=${bin.id}" class="fw-bold text-success text-decoration-none">
                                            <c:out value="${bin.binCode}"/>
                                        </a>
                                    </td>
                                    <td>
                                        <div class="fw-semibold text-dark"><c:out value="${bin.locationName}"/></div>
                                        <div class="small text-muted"><c:out value="${bin.areaName}"/> &bull; <c:out value="${bin.address}"/></div>
                                    </td>
                                    <td>
                                        <span class="badge bg-light text-dark border">
                                            <c:out value="${bin.wasteType.displayName}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="fw-semibold"><fmt:formatNumber value="${bin.capacityKg}" maxFractionDigits="1"/> kg</span>
                                    </td>
                                    <td>
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="fw-bold"><fmt:formatNumber value="${bin.currentFillPercentage}" maxFractionDigits="1"/>%</span>
                                            <span class="text-muted"><fmt:formatNumber value="${bin.estimatedWasteKg}" maxFractionDigits="1"/> kg</span>
                                        </div>
                                        <div class="fill-progress-container">
                                            <div class="fill-progress-bar ${bin.currentFillPercentage >= 90 ? 'fill-bar-critical' : (bin.currentFillPercentage >= 80 ? 'fill-bar-high' : (bin.currentFillPercentage >= 60 ? 'fill-bar-medium' : 'fill-bar-low'))}"
                                                 style="width: ${bin.currentFillPercentage}%;"></div>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge ${bin.priorityLevel == 'CRITICAL' ? 'badge-priority-critical' : (bin.priorityLevel == 'HIGH' ? 'badge-priority-high' : (bin.priorityLevel == 'MEDIUM' ? 'badge-priority-medium' : 'badge-priority-low'))}">
                                            <c:out value="${bin.priorityLevel.displayName}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="badge ${bin.active ? 'bg-success-subtle text-success' : 'bg-secondary'}">
                                            <c:out value="${bin.status}"/>
                                        </span>
                                    </td>
                                    <td class="text-end">
                                        <div class="btn-group btn-group-sm">
                                            <!-- Quick Fill Update Modal Trigger -->
                                            <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal"
                                                    data-bs-target="#updateFillModal${bin.id}" title="Update Fill Level">
                                                <i class="bi bi-speedometer2"></i>
                                            </button>
                                            <a href="${pageContext.request.contextPath}/bins/details?id=${bin.id}" class="btn btn-outline-info" title="View Details">
                                                <i class="bi bi-eye"></i>
                                            </a>
                                            <c:if test="${sessionScope.LOGGED_IN_USER.role == 'ADMIN'}">
                                                <a href="${pageContext.request.contextPath}/bins/edit?id=${bin.id}" class="btn btn-outline-primary" title="Edit Bin">
                                                    <i class="bi bi-pencil"></i>
                                                </a>
                                                <form action="${pageContext.request.contextPath}/bins/delete" method="POST" class="d-inline"
                                                      onsubmit="return confirm('Are you sure you want to deactivate bin ${bin.binCode}?');">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                    <input type="hidden" name="id" value="${bin.id}">
                                                    <button type="submit" class="btn btn-outline-danger" title="Deactivate Bin">
                                                        <i class="bi bi-trash"></i>
                                                    </button>
                                                </form>
                                            </c:if>
                                        </div>

                                        <!-- Update Fill Modal -->
                                        <div class="modal fade text-start" id="updateFillModal${bin.id}" tabindex="-1" aria-hidden="true">
                                            <div class="modal-dialog modal-dialog-centered modal-sm">
                                                <div class="modal-content">
                                                    <form action="${pageContext.request.contextPath}/bins/update-fill" method="POST">
                                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                        <input type="hidden" name="id" value="${bin.id}">
                                                        <div class="modal-header">
                                                            <h6 class="modal-title fw-bold">Update Fill Level: ${bin.binCode}</h6>
                                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                        </div>
                                                        <div class="modal-body">
                                                            <label class="form-label small text-muted">Current Fill Percentage (%)</label>
                                                            <div class="input-group">
                                                                <input type="number" step="0.1" min="0" max="100" name="fillPercentage"
                                                                       class="form-control" value="${bin.currentFillPercentage}" required>
                                                                <span class="input-group-text">%</span>
                                                            </div>
                                                            <div class="form-text small">Enter value between 0.0 and 100.0%.</div>
                                                        </div>
                                                        <div class="modal-footer p-2">
                                                            <button type="button" class="btn btn-sm btn-light border" data-bs-dismiss="modal">Cancel</button>
                                                            <button type="submit" class="btn btn-sm btn-success">Save Update</button>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
                                        <!-- End Modal -->
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
