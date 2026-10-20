<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Municipal Waste Collection Report - Print Version</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { font-size: 12px; color: #000; background: #fff; padding: 20px; }
        .table th, .table td { padding: 6px 8px; font-size: 11px; }
        @media print {
            .no-print { display: none !important; }
        }
    </style>
</head>
<body>

<div class="no-print mb-3 text-end">
    <button onclick="window.print()" class="btn btn-primary btn-sm">Print / Save as PDF</button>
    <button onclick="window.close()" class="btn btn-secondary btn-sm">Close Window</button>
</div>

<div class="border-bottom pb-3 mb-4 d-flex justify-content-between align-items-center">
    <div>
        <h4 class="fw-bold mb-0">MUNICIPAL SMART WASTE COLLECTION REPORT</h4>
        <div class="text-muted">Operations & Routing Optimization Audit Document</div>
    </div>
    <div class="text-end">
        <div><strong>Period:</strong> <c:out value="${summary.startDate}"/> to <c:out value="${summary.endDate}"/></div>
        <div><strong>Generated:</strong> <%= java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) %></div>
    </div>
</div>

<div class="row g-3 mb-4">
    <div class="col-3">
        <div class="p-2 border rounded">
            <div class="text-muted small">Total Collections</div>
            <div class="fs-5 fw-bold">${summary.totalCollections}</div>
        </div>
    </div>
    <div class="col-3">
        <div class="p-2 border rounded">
            <div class="text-muted small">Completed Tasks</div>
            <div class="fs-5 fw-bold text-success">${summary.completedCollections}</div>
        </div>
    </div>
    <div class="col-3">
        <div class="p-2 border rounded">
            <div class="text-muted small">Pending Tasks</div>
            <div class="fs-5 fw-bold text-warning-emphasis">${summary.pendingCollections}</div>
        </div>
    </div>
    <div class="col-3">
        <div class="p-2 border rounded">
            <div class="text-muted small">Total Waste Collected</div>
            <div class="fs-5 fw-bold text-primary"><fmt:formatNumber value="${summary.totalWasteCollectedKg}" maxFractionDigits="1"/> kg</div>
        </div>
    </div>
</div>

<h6 class="fw-bold border-bottom pb-1 mb-2">Personnel Performance Audit</h6>
<table class="table table-bordered table-sm mb-4">
    <thead class="table-light">
    <tr>
        <th>Worker Code</th>
        <th>Full Name</th>
        <th>Phone</th>
        <th>Area</th>
        <th>Assigned</th>
        <th>Completed</th>
        <th>Total Waste (kg)</th>
        <th>Completion %</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="w" items="${summary.workerPerformances}">
        <tr>
            <td><c:out value="${w.workerCode}"/></td>
            <td><c:out value="${w.fullName}"/></td>
            <td><c:out value="${w.phone}"/></td>
            <td><c:out value="${w.areaName}"/></td>
            <td>${w.totalAssignedTasks}</td>
            <td>${w.completedTasks}</td>
            <td><fmt:formatNumber value="${w.totalCollectedKg}" maxFractionDigits="1"/> kg</td>
            <td>${w.completionRate}%</td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<div class="mt-5 pt-4 d-flex justify-content-between text-muted small">
    <div>Prepared by: Municipal Systems Administrator</div>
    <div>Authorized Municipal Signature: ___________________________</div>
</div>

</body>
</html>
