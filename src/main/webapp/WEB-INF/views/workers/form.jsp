<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isEdit" value="${worker != null && worker.id != null}"/>
<c:set var="pageTitle" value="${isEdit ? 'Edit Worker' : 'Register Worker'}" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="mb-4">
            <a href="${pageContext.request.contextPath}/workers" class="text-decoration-none text-muted small">
                <i class="bi bi-arrow-left me-1"></i> Back to Workers List
            </a>
            <h4 class="fw-bold mb-1 mt-2 text-dark">${isEdit ? 'Edit Worker Profile' : 'Register Collection Worker'}</h4>
            <p class="text-muted small">Specify personnel identification, phone number, and designated area.</p>
        </div>

        <div class="row">
            <div class="col-12 col-md-8">
                <div class="card card-municipal">
                    <div class="card-header">
                        <span class="fw-bold"><i class="bi bi-person-fill text-success me-2"></i>Worker Profile</span>
                    </div>
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/workers/${isEdit ? 'edit' : 'create'}" method="POST">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <c:if test="${isEdit}">
                                <input type="hidden" name="id" value="${worker.id}">
                            </c:if>

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label for="workerCode" class="form-label small fw-semibold">Worker ID / Code <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="workerCode" name="workerCode"
                                           placeholder="e.g. WRK-101" required
                                           value="<c:out value='${worker != null ? worker.workerCode : \"\"}'/>">
                                </div>

                                <div class="col-md-6">
                                    <label for="fullName" class="form-label small fw-semibold">Full Legal Name <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="fullName" name="fullName"
                                           placeholder="e.g. Rajesh Kumar" required
                                           value="<c:out value='${worker != null ? worker.fullName : \"\"}'/>">
                                </div>

                                <div class="col-md-6">
                                    <label for="phone" class="form-label small fw-semibold">Contact Phone <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="phone" name="phone"
                                           placeholder="e.g. +91 98765 43210" required
                                           value="<c:out value='${worker != null ? worker.phone : \"\"}'/>">
                                </div>

                                <div class="col-md-6">
                                    <label for="areaId" class="form-label small fw-semibold">Assigned Municipal Area <span class="text-danger">*</span></label>
                                    <select id="areaId" name="areaId" class="form-select" required>
                                        <option value="">Select Area...</option>
                                        <c:forEach var="area" items="${areas}">
                                            <option value="${area.id}" ${(worker != null && worker.areaId == area.id) ? 'selected' : ''}>
                                                <c:out value="${area.name}"/>
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <c:if test="${isEdit}">
                                    <div class="col-md-6">
                                        <label for="availabilityStatus" class="form-label small fw-semibold">Availability Status</label>
                                        <select id="availabilityStatus" name="availabilityStatus" class="form-select">
                                            <option value="AVAILABLE" ${worker.availabilityStatus == 'AVAILABLE' ? 'selected' : ''}>AVAILABLE</option>
                                            <option value="ON_DUTY" ${worker.availabilityStatus == 'ON_DUTY' ? 'selected' : ''}>ON_DUTY</option>
                                            <option value="ON_LEAVE" ${worker.availabilityStatus == 'ON_LEAVE' ? 'selected' : ''}>ON_LEAVE</option>
                                        </select>
                                    </div>

                                    <div class="col-md-6">
                                        <label for="status" class="form-label small fw-semibold">Account Status</label>
                                        <select id="status" name="status" class="form-select">
                                            <option value="ACTIVE" ${worker.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                            <option value="INACTIVE" ${worker.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                        </select>
                                    </div>
                                </c:if>
                            </div>

                            <hr class="my-4">

                            <div class="d-flex justify-content-end gap-2">
                                <a href="${pageContext.request.contextPath}/workers" class="btn btn-light border">Cancel</a>
                                <button type="submit" class="btn btn-success px-4">
                                    <i class="bi bi-check-lg me-1"></i> ${isEdit ? 'Update Worker' : 'Register Worker'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

    </main>

<jsp:include page="../common/footer.jsp"/>
