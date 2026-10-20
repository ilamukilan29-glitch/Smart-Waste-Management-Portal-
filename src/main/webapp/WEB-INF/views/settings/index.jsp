<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Priority & Engine Settings" scope="request"/>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/sidebar.jsp"/>

<div class="app-main">
    <jsp:include page="../common/navbar.jsp"/>

    <main class="app-content">
        <jsp:include page="../common/alerts.jsp"/>

        <div class="mb-4">
            <h4 class="fw-bold mb-1 text-dark">Engine Rules & Routing Parameters</h4>
            <p class="text-muted small">Configure priority classification thresholds, truck speeds, and route planning constraints.</p>
        </div>

        <div class="row">
            <div class="col-12 col-md-8">
                <div class="card card-municipal">
                    <div class="card-header bg-light">
                        <span class="fw-bold text-dark"><i class="bi bi-sliders text-success me-2"></i>Priority Engine Configuration</span>
                    </div>
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/settings" method="POST">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">

                            <h6 class="fw-bold text-dark mb-3">1. Collection Priority Thresholds</h6>
                            <div class="row g-3 mb-4">
                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-danger">Critical Threshold (%)</label>
                                    <div class="input-group input-group-sm">
                                        <input type="number" step="0.5" min="70" max="99" name="criticalThreshold" class="form-control"
                                               value="${criticalThreshold}" required>
                                        <span class="input-group-text">%</span>
                                    </div>
                                    <div class="form-text small">Default: 90.0%</div>
                                </div>

                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-warning-emphasis">High Priority Threshold (%)</label>
                                    <div class="input-group input-group-sm">
                                        <input type="number" step="0.5" min="50" max="89" name="highThreshold" class="form-control"
                                               value="${highThreshold}" required>
                                        <span class="input-group-text">%</span>
                                    </div>
                                    <div class="form-text small">Default: 80.0%</div>
                                </div>

                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-primary">Medium Priority Threshold (%)</label>
                                    <div class="input-group input-group-sm">
                                        <input type="number" step="0.5" min="30" max="79" name="mediumThreshold" class="form-control"
                                               value="${mediumThreshold}" required>
                                        <span class="input-group-text">%</span>
                                    </div>
                                    <div class="form-text small">Default: 60.0%</div>
                                </div>
                            </div>

                            <hr class="my-4">

                            <h6 class="fw-bold text-dark mb-3">2. Route Planning & Fleet Parameters</h6>
                            <div class="row g-3 mb-4">
                                <div class="col-md-6">
                                    <label class="form-label small fw-semibold">Average Truck Speed (km/h)</label>
                                    <div class="input-group input-group-sm">
                                        <input type="number" step="1" min="10" max="60" name="truckSpeed" class="form-control"
                                               value="${truckSpeed}" required>
                                        <span class="input-group-text">km/h</span>
                                    </div>
                                    <div class="form-text small">Urban municipal average speed for travel time estimation.</div>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label small fw-semibold">Service Stop Time (minutes/bin)</label>
                                    <div class="input-group input-group-sm">
                                        <input type="number" step="1" min="1" max="30" name="serviceTime" class="form-control"
                                               value="${serviceTime}" required>
                                        <span class="input-group-text">min</span>
                                    </div>
                                    <div class="form-text small">Mechanical loading and emptying duration per stop.</div>
                                </div>
                            </div>

                            <div class="d-flex justify-content-end">
                                <button type="submit" class="btn btn-success px-4 shadow-sm">
                                    <i class="bi bi-save me-1"></i> Save Configuration
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-12 col-md-4">
                <div class="card card-municipal">
                    <div class="card-header bg-light">
                        <span class="fw-bold"><i class="bi bi-info-circle text-primary me-2"></i>Academic Rule Reference</span>
                    </div>
                    <div class="card-body small text-muted">
                        <p>These settings directly parameterize the Java 8 <code>PriorityEngineService</code> and <code>RouteOptimizationService</code>.</p>
                        <p class="mb-0">Changes take effect dynamically in memory for all subsequent route calculations and priority evaluations.</p>
                    </div>
                </div>
            </div>
        </div>

    </main>

<jsp:include page="../common/footer.jsp"/>
