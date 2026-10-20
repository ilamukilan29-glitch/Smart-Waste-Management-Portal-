<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Collection Workforce" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h4 class="fw-bold mb-1 text-dark">Collection Personnel Management</h4>
                <p class="text-muted small">Manage municipal workers, area assignments, and on-duty availability.</p>
            </div>
            <div>
                <c:if test="${sessionScope.LOGGED_IN_USER.role == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/workers/create" class="btn btn-success btn-sm shadow-sm">
                        <i class="bi bi-person-plus-fill me-1"></i> Register New Worker
                    </a>
                </c:if>
            </div>
        </div>

        <div class="card card-municipal">
            <div class="card-header">
                <span class="fw-bold">Registered Personnel (${workers.size()} workers)</span>
            </div>
            <div class="table-responsive">
                <table class="table table-municipal table-hover mb-0">
                    <thead>
                    <tr>
                        <th>Worker Code</th>
                        <th>Full Name</th>
                        <th>Contact Phone</th>
                        <th>Assigned Area</th>
                        <th>User Account</th>
                        <th>Availability</th>
                        <th>Status</th>
                        <th class="text-end">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty workers}">
                            <tr>
                                <td colspan="8" class="text-center py-5 text-muted">
                                    <i class="bi bi-people fs-2"></i>
                                    <p class="mt-2 mb-0">No workers registered in database.</p>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="w" items="${workers}">
                                <tr>
                                    <td class="fw-bold text-success"><c:out value="${w.workerCode}"/></td>
                                    <td class="fw-semibold text-dark"><c:out value="${w.fullName}"/></td>
                                    <td><c:out value="${w.phone}"/></td>
                                    <td><span class="badge bg-light text-dark border"><c:out value="${w.areaName}"/></span></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty w.username}">
                                                <code>@<c:out value="${w.username}"/></code>
                                            </c:when>
                                            <c:otherwise><span class="text-muted small">None linked</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="badge ${w.availabilityStatus == 'AVAILABLE' ? 'bg-success-subtle text-success border border-success' : (w.availabilityStatus == 'ON_DUTY' ? 'bg-warning-subtle text-warning-emphasis border border-warning' : 'bg-secondary')}">
                                            <c:out value="${w.availabilityStatus}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="badge ${w.status == 'ACTIVE' ? 'bg-success' : 'bg-secondary'}"><c:out value="${w.status}"/></span>
                                    </td>
                                    <td class="text-end">
                                        <c:if test="${sessionScope.LOGGED_IN_USER.role == 'ADMIN'}">
                                            <div class="btn-group btn-group-sm">
                                                <a href="${pageContext.request.contextPath}/workers/edit?id=${w.id}" class="btn btn-outline-primary" title="Edit Worker">
                                                    <i class="bi bi-pencil"></i>
                                                </a>
                                                <!-- Quick Availability Toggle Modal -->
                                                <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#availModal${w.id}">
                                                    <i class="bi bi-clock"></i>
                                                </button>
                                            </div>

                                            <!-- Modal -->
                                            <div class="modal fade text-start" id="availModal${w.id}" tabindex="-1" aria-hidden="true">
                                                <div class="modal-dialog modal-dialog-centered modal-sm">
                                                    <div class="modal-content">
                                                        <form action="${pageContext.request.contextPath}/workers/status" method="POST">
                                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                            <input type="hidden" name="id" value="${w.id}">
                                                            <div class="modal-header">
                                                                <h6 class="modal-title fw-bold">Update Status: ${w.fullName}</h6>
                                                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                            </div>
                                                            <div class="modal-body">
                                                                <label class="form-label small text-muted">Availability Status</label>
                                                                <select name="availabilityStatus" class="form-select form-select-sm">
                                                                    <option value="AVAILABLE" ${w.availabilityStatus == 'AVAILABLE' ? 'selected' : ''}>AVAILABLE</option>
                                                                    <option value="ON_DUTY" ${w.availabilityStatus == 'ON_DUTY' ? 'selected' : ''}>ON_DUTY</option>
                                                                    <option value="ON_LEAVE" ${w.availabilityStatus == 'ON_LEAVE' ? 'selected' : ''}>ON_LEAVE</option>
                                                                </select>
                                                            </div>
                                                            <div class="modal-footer p-2">
                                                                <button type="button" class="btn btn-sm btn-light border" data-bs-dismiss="modal">Cancel</button>
                                                                <button type="submit" class="btn btn-sm btn-success">Update</button>
                                                            </div>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:if>
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
