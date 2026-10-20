<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Collection Fleet Vehicles" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h4 class="fw-bold mb-1 text-dark">Municipal Collection Fleet</h4>
                <p class="text-muted small">Manage waste trucks, carrying capacity constraints, and depot assignments.</p>
            </div>
            <div>
                <c:if test="${sessionScope.LOGGED_IN_USER.role == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/vehicles/create" class="btn btn-success btn-sm shadow-sm">
                        <i class="bi bi-truck me-1"></i> Register New Vehicle
                    </a>
                </c:if>
            </div>
        </div>

        <div class="card card-municipal">
            <div class="card-header">
                <span class="fw-bold">Fleet Roster (${vehicles.size()} vehicles)</span>
            </div>
            <div class="table-responsive">
                <table class="table table-municipal table-hover mb-0">
                    <thead>
                    <tr>
                        <th>Registration No.</th>
                        <th>Vehicle Type</th>
                        <th>Maximum Capacity</th>
                        <th>Assigned Area</th>
                        <th>Base Depot</th>
                        <th>Availability</th>
                        <th>Status</th>
                        <th class="text-end">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty vehicles}">
                            <tr>
                                <td colspan="8" class="text-center py-5 text-muted">
                                    <i class="bi bi-truck fs-2"></i>
                                    <p class="mt-2 mb-0">No vehicles registered in fleet.</p>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="v" items="${vehicles}">
                                <tr>
                                    <td class="fw-bold text-success"><c:out value="${v.registrationNumber}"/></td>
                                    <td><span class="badge bg-light text-dark border"><c:out value="${v.vehicleType}"/></span></td>
                                    <td class="fw-semibold"><fmt:formatNumber value="${v.capacityKg}" maxFractionDigits="1"/> kg</td>
                                    <td><c:out value="${v.areaName}"/></td>
                                    <td><c:out value="${v.depotName}"/></td>
                                    <td>
                                        <span class="badge ${v.availabilityStatus == 'AVAILABLE' ? 'bg-success-subtle text-success border border-success' : (v.availabilityStatus == 'ON_ROUTE' ? 'bg-warning-subtle text-warning-emphasis border border-warning' : 'bg-secondary')}">
                                            <c:out value="${v.availabilityStatus}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="badge ${v.status == 'ACTIVE' ? 'bg-success' : 'bg-secondary'}"><c:out value="${v.status}"/></span>
                                    </td>
                                    <td class="text-end">
                                        <c:if test="${sessionScope.LOGGED_IN_USER.role == 'ADMIN'}">
                                            <div class="btn-group btn-group-sm">
                                                <a href="${pageContext.request.contextPath}/vehicles/edit?id=${v.id}" class="btn btn-outline-primary" title="Edit Vehicle">
                                                    <i class="bi bi-pencil"></i>
                                                </a>
                                                <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#vAvailModal${v.id}">
                                                    <i class="bi bi-clock"></i>
                                                </button>
                                            </div>

                                            <!-- Modal -->
                                            <div class="modal fade text-start" id="vAvailModal${v.id}" tabindex="-1" aria-hidden="true">
                                                <div class="modal-dialog modal-dialog-centered modal-sm">
                                                    <div class="modal-content">
                                                        <form action="${pageContext.request.contextPath}/vehicles/status" method="POST">
                                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                            <input type="hidden" name="id" value="${v.id}">
                                                            <div class="modal-header">
                                                                <h6 class="modal-title fw-bold">Update Status: ${v.registrationNumber}</h6>
                                                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                            </div>
                                                            <div class="modal-body">
                                                                <label class="form-label small text-muted">Availability Status</label>
                                                                <select name="availabilityStatus" class="form-select form-select-sm">
                                                                    <option value="AVAILABLE" ${v.availabilityStatus == 'AVAILABLE' ? 'selected' : ''}>AVAILABLE</option>
                                                                    <option value="ON_ROUTE" ${v.availabilityStatus == 'ON_ROUTE' ? 'selected' : ''}>ON_ROUTE</option>
                                                                    <option value="UNAVAILABLE" ${v.availabilityStatus == 'UNAVAILABLE' ? 'selected' : ''}>UNAVAILABLE</option>
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
