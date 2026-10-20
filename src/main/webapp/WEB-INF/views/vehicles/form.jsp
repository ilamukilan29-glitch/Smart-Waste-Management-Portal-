<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isEdit" value="${vehicle != null && vehicle.id != null}"/>
<c:set var="pageTitle" value="${isEdit ? 'Edit Vehicle' : 'Register Vehicle'}" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="mb-4">
            <a href="${pageContext.request.contextPath}/vehicles" class="text-decoration-none text-muted small">
                <i class="bi bi-arrow-left me-1"></i> Back to Fleet List
            </a>
            <h4 class="fw-bold mb-1 mt-2 text-dark">${isEdit ? 'Edit Vehicle Profile' : 'Register Collection Vehicle'}</h4>
            <p class="text-muted small">Configure registration number, type, carrying capacity, and base depot.</p>
        </div>

        <div class="row">
            <div class="col-12 col-md-8">
                <div class="card card-municipal">
                    <div class="card-header">
                        <span class="fw-bold"><i class="bi bi-truck text-success me-2"></i>Vehicle Attributes</span>
                    </div>
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/vehicles/${isEdit ? 'edit' : 'create'}" method="POST">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <c:if test="${isEdit}">
                                <input type="hidden" name="id" value="${vehicle.id}">
                            </c:if>

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label for="registrationNumber" class="form-label small fw-semibold">Registration Number <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="registrationNumber" name="registrationNumber"
                                           placeholder="e.g. TN-01-AB-1234" required
                                           value="<c:out value='${vehicle != null ? vehicle.registrationNumber : \"\"}'/>">
                                </div>

                                <div class="col-md-6">
                                    <label for="vehicleType" class="form-label small fw-semibold">Vehicle Type <span class="text-danger">*</span></label>
                                    <select id="vehicleType" name="vehicleType" class="form-select" required>
                                        <option value="COMPACTOR_TRUCK" ${vehicle != null && vehicle.vehicleType == 'COMPACTOR_TRUCK' ? 'selected' : ''}>COMPACTOR_TRUCK (High Volume)</option>
                                        <option value="TIPPER_TRUCK" ${vehicle != null && vehicle.vehicleType == 'TIPPER_TRUCK' ? 'selected' : ''}>TIPPER_TRUCK (Standard)</option>
                                        <option value="ELECTRIC_VAN" ${vehicle != null && vehicle.vehicleType == 'ELECTRIC_VAN' ? 'selected' : ''}>ELECTRIC_VAN (Narrow Streets)</option>
                                        <option value="MINI_TRUCK" ${vehicle != null && vehicle.vehicleType == 'MINI_TRUCK' ? 'selected' : ''}>MINI_TRUCK (Rapid Dispatch)</option>
                                    </select>
                                </div>

                                <div class="col-md-6">
                                    <label for="capacityKg" class="form-label small fw-semibold">Carrying Capacity (kg) <span class="text-danger">*</span></label>
                                    <div class="input-group">
                                        <input type="number" step="10" min="100" class="form-control" id="capacityKg" name="capacityKg"
                                               placeholder="e.g. 1500" required
                                               value="<c:out value='${vehicle != null ? vehicle.capacityKg : \"1500\"}'/>">
                                        <span class="input-group-text">kg</span>
                                    </div>
                                    <div class="form-text small">Maximum load allowed during route optimization.</div>
                                </div>

                                <div class="col-md-6">
                                    <label for="areaId" class="form-label small fw-semibold">Assigned Municipal Area <span class="text-danger">*</span></label>
                                    <select id="areaId" name="areaId" class="form-select" required>
                                        <option value="">Select Area...</option>
                                        <c:forEach var="area" items="${areas}">
                                            <option value="${area.id}" ${(vehicle != null && vehicle.areaId == area.id) ? 'selected' : ''}>
                                                <c:out value="${area.name}"/>
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="col-md-6">
                                    <label for="depotId" class="form-label small fw-semibold">Base Depot <span class="text-danger">*</span></label>
                                    <select id="depotId" name="depotId" class="form-select" required>
                                        <option value="">Select Depot...</option>
                                        <c:forEach var="depot" items="${depots}">
                                            <option value="${depot.id}" ${(vehicle != null && vehicle.depotId == depot.id) ? 'selected' : ''}>
                                                <c:out value="${depot.name}"/>
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <c:if test="${isEdit}">
                                    <div class="col-md-6">
                                        <label for="availabilityStatus" class="form-label small fw-semibold">Availability</label>
                                        <select id="availabilityStatus" name="availabilityStatus" class="form-select">
                                            <option value="AVAILABLE" ${vehicle.availabilityStatus == 'AVAILABLE' ? 'selected' : ''}>AVAILABLE</option>
                                            <option value="ON_ROUTE" ${vehicle.availabilityStatus == 'ON_ROUTE' ? 'selected' : ''}>ON_ROUTE</option>
                                            <option value="UNAVAILABLE" ${vehicle.availabilityStatus == 'UNAVAILABLE' ? 'selected' : ''}>UNAVAILABLE</option>
                                        </select>
                                    </div>

                                    <div class="col-md-6">
                                        <label for="status" class="form-label small fw-semibold">Fleet Status</label>
                                        <select id="status" name="status" class="form-select">
                                            <option value="ACTIVE" ${vehicle.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                            <option value="MAINTENANCE" ${vehicle.status == 'MAINTENANCE' ? 'selected' : ''}>MAINTENANCE</option>
                                            <option value="RETIRED" ${vehicle.status == 'RETIRED' ? 'selected' : ''}>RETIRED</option>
                                        </select>
                                    </div>
                                </c:if>
                            </div>

                            <hr class="my-4">

                            <div class="d-flex justify-content-end gap-2">
                                <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-light border">Cancel</a>
                                <button type="submit" class="btn btn-success px-4">
                                    <i class="bi bi-check-lg me-1"></i> ${isEdit ? 'Update Vehicle' : 'Register Vehicle'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

    </main>

<jsp:include page="../common/footer.jsp"/>
