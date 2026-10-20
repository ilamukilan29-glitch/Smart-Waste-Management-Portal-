<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="System Alerts & Notifications" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h4 class="fw-bold mb-1 text-dark">System Alerts & Operational Notices</h4>
                <p class="text-muted small">Real-time alerts for critical bins (&ge;90%), unassigned collection plans, and fleet status.</p>
            </div>
            <div>
                <form action="${pageContext.request.contextPath}/notifications/read-all" method="POST" class="d-inline">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <button type="submit" class="btn btn-outline-secondary btn-sm shadow-sm"
                            ${empty notifications ? 'disabled' : ''}>
                        <i class="bi bi-check2-all me-1"></i> Mark All as Read
                    </button>
                </form>
            </div>
        </div>

        <div class="card card-municipal">
            <div class="card-header">
                <span class="fw-bold">Recent Notifications (${notifications.size()})</span>
            </div>
            <div class="list-group list-group-flush">
                <c:choose>
                    <c:when test="${empty notifications}">
                        <div class="text-center py-5 text-muted">
                            <i class="bi bi-bell-slash fs-2"></i>
                            <p class="mt-2 mb-0">No active alerts. All municipal operations normal.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="n" items="${notifications}">
                            <div class="list-group-item p-3 ${n.read ? 'bg-light text-muted' : ''}">
                                <div class="d-flex justify-content-between align-items-start">
                                    <div class="d-flex align-items-start gap-3">
                                        <div class="mt-1">
                                            <span class="badge ${n.alertType == 'CRITICAL' ? 'bg-danger' : (n.alertType == 'WARNING' ? 'bg-warning text-dark' : 'bg-primary')} p-2">
                                                <i class="bi ${n.alertType == 'CRITICAL' ? 'bi-exclamation-octagon-fill' : (n.alertType == 'WARNING' ? 'bi-exclamation-triangle-fill' : 'bi-info-circle-fill')}"></i>
                                            </span>
                                        </div>
                                        <div>
                                            <h6 class="fw-bold mb-1 ${n.read ? 'text-muted' : 'text-dark'}">
                                                <c:out value="${n.title}"/>
                                            </h6>
                                            <p class="mb-1 small"><c:out value="${n.message}"/></p>
                                            <span class="small text-muted"><i class="bi bi-clock me-1"></i><c:out value="${n.createdAt}"/></span>
                                        </div>
                                    </div>
                                    <div>
                                        <c:if test="${!n.read}">
                                            <form action="${pageContext.request.contextPath}/notifications/read" method="POST" class="d-inline">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="id" value="${n.id}">
                                                <button type="submit" class="btn btn-sm btn-outline-success py-0 px-2" title="Mark as read">
                                                    <i class="bi bi-check"></i>
                                                </button>
                                            </form>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </main>

<jsp:include page="../common/footer.jsp"/>
