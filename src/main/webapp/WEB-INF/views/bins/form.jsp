<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isEdit" value="${bin != null && bin.id != null}"/>
<c:set var="pageTitle" value="${isEdit ? 'Edit Waste Bin' : 'Register Waste Bin'}" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="mb-4">
            <a href="${pageContext.request.contextPath}/bins" class="text-decoration-none text-muted small">
                <i class="bi bi-arrow-left me-1"></i> Back to Waste Bins
            </a>
            <h4 class="fw-bold mb-1 mt-2 text-dark">${isEdit ? 'Edit Waste Bin' : 'Register New Waste Bin'}</h4>
            <p class="text-muted small">Specify bin details, geographic location coordinates, and volume capacity.</p>
        </div>

        <div class="row">
            <div class="col-12 col-lg-8">
                <div class="card card-municipal">
                    <div class="card-header">
                        <span class="fw-bold"><i class="bi bi-trash3-fill text-success me-2"></i>Bin Attributes</span>
                    </div>
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/bins/${isEdit ? 'edit' : 'create'}" method="POST">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <c:if test="${isEdit}">
                                <input type="hidden" name="id" value="${bin.id}">
                            </c:if>

                            <div class="row g-3">
                                <!-- Bin Code -->
                                <div class="col-md-6">
                                    <label for="binCode" class="form-label small fw-semibold">Bin Code / Identifier <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="binCode" name="binCode"
                                           placeholder="e.g. BIN-MZ-001" required
                                           value="<c:out value='${bin != null ? bin.binCode : \"\"}'/>">
                                    <div class="form-text small">Unique municipal serial code.</div>
                                </div>

                                <!-- Area Selection -->
                                <div class="col-md-6">
                                    <label for="areaId" class="form-label small fw-semibold">Municipal Area <span class="text-danger">*</span></label>
                                    <select id="areaId" name="areaId" class="form-select" required>
                                        <option value="">Select Municipal Area...</option>
                                        <c:forEach var="area" items="${areas}">
                                            <option value="${area.id}" ${(bin != null && bin.areaId == area.id) ? 'selected' : ''}>
                                                <c:out value="${area.name}"/> (<c:out value="${area.code}"/>)
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <!-- Location Name -->
                                <div class="col-md-6">
                                    <label for="locationName" class="form-label small fw-semibold">Location / Landmark <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="locationName" name="locationName"
                                           placeholder="e.g. Central Market Gate 1" required
                                           value="<c:out value='${bin != null ? bin.locationName : \"\"}'/>">
                                </div>

                                <!-- Address -->
                                <div class="col-md-6">
                                    <label for="address" class="form-label small fw-semibold">Street Address <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="address" name="address"
                                           placeholder="e.g. 42 Commercial Road" required
                                           value="<c:out value='${bin != null ? bin.address : \"\"}'/>">
                                </div>

                                <!-- Coordinates -->
                                <div class="col-md-6">
                                    <label for="latitude" class="form-label small fw-semibold">Latitude</label>
                                    <input type="number" step="0.000001" class="form-control" id="latitude" name="latitude"
                                           placeholder="13.082700"
                                           value="<c:out value='${bin != null ? bin.latitude : \"13.082700\"}'/>">
                                </div>
                                <div class="col-md-6">
                                    <label for="longitude" class="form-label small fw-semibold">Longitude</label>
                                    <input type="number" step="0.000001" class="form-control" id="longitude" name="longitude"
                                           placeholder="80.270700"
                                           value="<c:out value='${bin != null ? bin.longitude : \"80.270700\"}'/>">
                                </div>

                                <!-- Waste Type -->
                                <div class="col-md-4">
                                    <label for="wasteType" class="form-label small fw-semibold">Waste Category <span class="text-danger">*</span></label>
                                    <select id="wasteType" name="wasteType" class="form-select" required>
                                        <c:forEach var="type" items="${wasteTypes}">
                                            <option value="${type.name()}" ${(bin != null && bin.wasteType == type) ? 'selected' : ''}>
                                                <c:out value="${type.displayName}"/>
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <!-- Capacity (kg) -->
                                <div class="col-md-4">
                                    <label for="capacityKg" class="form-label small fw-semibold">Capacity (kg) <span class="text-danger">*</span></label>
                                    <div class="input-group">
                                        <input type="number" step="0.1" min="1" class="form-control" id="capacityKg" name="capacityKg"
                                               placeholder="100.0" required
                                               value="<c:out value='${bin != null ? bin.capacityKg : \"100.0\"}'/>">
                                        <span class="input-group-text">kg</span>
                                    </div>
                                </div>

                                <!-- Fill Percentage -->
                                <div class="col-md-4">
                                    <label for="currentFillPercentage" class="form-label small fw-semibold">Initial Fill Level (%)</label>
                                    <div class="input-group">
                                        <input type="number" step="0.1" min="0" max="100" class="form-control" id="currentFillPercentage" name="currentFillPercentage"
                                               placeholder="0.0"
                                               value="<c:out value='${bin != null ? bin.currentFillPercentage : \"0.0\"}'/>">
                                        <span class="input-group-text">%</span>
                                    </div>
                                </div>

                                <c:if test="${isEdit}">
                                    <!-- Status -->
                                    <div class="col-md-6">
                                        <label for="status" class="form-label small fw-semibold">Operational Status</label>
                                        <select id="status" name="status" class="form-select">
                                            <option value="ACTIVE" ${bin.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                            <option value="MAINTENANCE" ${bin.status == 'MAINTENANCE' ? 'selected' : ''}>MAINTENANCE</option>
                                            <option value="INACTIVE" ${bin.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                        </select>
                                    </div>
                                </c:if>
                            </div>

                            <hr class="my-4">

                            <div class="d-flex justify-content-end gap-2">
                                <a href="${pageContext.request.contextPath}/bins" class="btn btn-light border">Cancel</a>
                                <button type="submit" class="btn btn-success px-4">
                                    <i class="bi bi-check-lg me-1"></i> ${isEdit ? 'Update Bin' : 'Register Bin'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Helpful Hints Card -->
            <div class="col-12 col-lg-4">
                <div class="card card-municipal">
                    <div class="card-header">
                        <span class="fw-bold"><i class="bi bi-info-circle text-primary me-2"></i>Priority Rules Info</span>
                    </div>
                    <div class="card-body small text-muted">
                        <p>Fill percentage directly drives the priority engine:</p>
                        <ul class="ps-3">
                            <li><strong class="text-danger">&ge; 90% - Critical:</strong> Immediate route dispatch candidate.</li>
                            <li><strong class="text-warning-emphasis">&ge; 80% - High:</strong> Urgent pickup to prevent spillover.</li>
                            <li><strong class="text-info">&ge; 60% - Medium:</strong> Eligible for routine planned collection.</li>
                            <li><strong class="text-success">&lt; 60% - Low:</strong> Safe fill level; collection deferred to avoid empty trips.</li>
                        </ul>
                        <div class="alert alert-light border small mt-3 mb-0">
                            <i class="bi bi-lightbulb text-warning me-1"></i> Coordinates (Lat/Long) are used by the nearest neighbor distance matrix calculation.
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </main>

<jsp:include page="../common/footer.jsp"/>
