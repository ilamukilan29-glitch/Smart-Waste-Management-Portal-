<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Waste Generation Simulation" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="mb-4">
            <h4 class="fw-bold mb-1 text-dark">Waste Accumulation Simulation Engine</h4>
            <p class="text-muted small">Simulate periodic waste generation and sensor fill reports for demonstration and viva review.</p>
        </div>

        <div class="alert alert-info border d-flex align-items-start mb-4 shadow-sm">
            <i class="bi bi-info-circle-fill fs-4 text-primary me-3 mt-1"></i>
            <div>
                <h6 class="fw-bold mb-1">Academic Simulation Notice</h6>
                <p class="mb-0 small text-muted">
                    This module provides deterministic software simulation of waste accumulation across municipal bins.
                    In production, these updates would originate from ultrasonic level sensors or municipal field workers.
                    Use this page during project reviews to simulate changing bin fill levels, trigger critical alerts, and observe route plan re-computations.
                </p>
            </div>
        </div>

        <div class="row g-4">
            <!-- Simulation Action 1: Accumulate Waste -->
            <div class="col-12 col-md-6">
                <div class="card card-municipal h-100">
                    <div class="card-header bg-success-subtle">
                        <span class="fw-bold text-success-emphasis"><i class="bi bi-graph-up-arrow me-2"></i>Simulate Waste Accumulation</span>
                    </div>
                    <div class="card-body p-4">
                        <p class="small text-muted mb-3">
                            Increments the current fill percentage of active bins by a randomized amount, simulating elapsed time and community waste deposit.
                        </p>

                        <form action="${pageContext.request.contextPath}/simulation" method="POST">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="action" value="accumulate">

                            <div class="mb-3">
                                <label for="areaId" class="form-label small fw-semibold">Target Area</label>
                                <select id="areaId" name="areaId" class="form-select form-select-sm">
                                    <option value="">All Municipal Areas</option>
                                    <c:forEach var="area" items="${areas}">
                                        <option value="${area.id}"><c:out value="${area.name}"/></option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="mb-4">
                                <label for="maxIncrement" class="form-label small fw-semibold">Max Percentage Increment (%)</label>
                                <div class="input-group input-group-sm">
                                    <input type="number" step="1" min="5" max="50" class="form-control" id="maxIncrement" name="maxIncrement" value="25">
                                    <span class="input-group-text">%</span>
                                </div>
                                <div class="form-text small">Each bin will increase between +5.0% and the specified maximum (capped at 100%).</div>
                            </div>

                            <button type="submit" class="btn btn-success w-100">
                                <i class="bi bi-play-fill me-1"></i> Run Accumulation Step
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Simulation Action 2: Reset All Bins -->
            <div class="col-12 col-md-6">
                <div class="card card-municipal h-100">
                    <div class="card-header bg-light">
                        <span class="fw-bold text-dark"><i class="bi bi-arrow-counterclockwise me-2"></i>Reset Fill Levels</span>
                    </div>
                    <div class="card-body p-4">
                        <p class="small text-muted mb-3">
                            Resets all waste bins in the selected area back to 0.0% fill level. Useful for clearing the system before beginning a new viva demonstration cycle.
                        </p>

                        <form action="${pageContext.request.contextPath}/simulation" method="POST"
                              onsubmit="return confirm('Are you sure you want to reset all bin fill levels to 0%?');">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="action" value="reset_all">

                            <div class="mb-4">
                                <label for="resetAreaId" class="form-label small fw-semibold">Target Area</label>
                                <select id="resetAreaId" name="areaId" class="form-select form-select-sm">
                                    <option value="">All Municipal Areas</option>
                                    <c:forEach var="area" items="${areas}">
                                        <option value="${area.id}"><c:out value="${area.name}"/></option>
                                    </c:forEach>
                                </select>
                            </div>

                            <button type="submit" class="btn btn-outline-danger w-100">
                                <i class="bi bi-trash me-1"></i> Reset All Bins to 0%
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>

    </main>

<jsp:include page="../common/footer.jsp"/>
