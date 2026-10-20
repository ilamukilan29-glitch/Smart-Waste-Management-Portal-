/**
 * Smart Waste Management System - Frontend Interactivity
 */

document.addEventListener("DOMContentLoaded", function () {
    // 1. Auto-dismiss flash alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 5000);
    });

    // 2. Select All Checkbox Handler for Route Planner
    const selectAllCheckbox = document.getElementById("selectAllBins");
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener("change", function () {
            const checkboxes = document.querySelectorAll(".bin-checkbox");
            checkboxes.forEach(cb => cb.checked = selectAllCheckbox.checked);
            updateSelectedSummary();
        });

        const binCheckboxes = document.querySelectorAll(".bin-checkbox");
        binCheckboxes.forEach(cb => {
            cb.addEventListener("change", updateSelectedSummary);
        });
    }
});

function updateSelectedSummary() {
    const checked = document.querySelectorAll(".bin-checkbox:checked");
    const countBadge = document.getElementById("selectedCountBadge");
    if (countBadge) {
        countBadge.innerText = checked.length;
    }
    const submitBtn = document.getElementById("generateRouteBtn");
    if (submitBtn) {
        submitBtn.disabled = checked.length === 0;
    }
}

/**
 * Chart.js Helper: Render Doughnut Chart for Waste Bins by Priority
 */
function initPriorityChart(canvasId, labels, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: [
                    '#ef4444', // Critical
                    '#f59e0b', // High
                    '#0ea5e9', // Medium
                    '#10b981'  // Low
                ],
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        boxWidth: 12,
                        padding: 15,
                        font: { size: 12 }
                    }
                }
            },
            cutout: '70%'
        }
    });
}

/**
 * Chart.js Helper: Render Bar Chart for Bins by Waste Type
 */
function initWasteTypeChart(canvasId, labels, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Registered Bins',
                data: data,
                backgroundColor: '#10b981',
                borderRadius: 6,
                maxBarThickness: 40
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0 }
                },
                x: {
                    grid: { display: false }
                }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
}
