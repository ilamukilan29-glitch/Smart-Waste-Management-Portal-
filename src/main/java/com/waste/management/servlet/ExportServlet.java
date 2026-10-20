package com.waste.management.servlet;

import com.waste.management.dto.ReportSummaryDTO;
import com.waste.management.dto.WorkerPerformanceDTO;
import com.waste.management.service.ReportService;
import com.waste.management.util.CsvExportUtil;
import com.waste.management.util.DateTimeUtil;
import com.waste.management.util.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "ExportServlet", urlPatterns = {"/reports/export/csv"})
public class ExportServlet extends BaseServlet {

    private ReportService reportService;

    @Override
    public void init() {
        this.reportService = new ReportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String startStr = request.getParameter("startDate");
        String endStr = request.getParameter("endDate");
        String areaParam = request.getParameter("areaId");

        LocalDate startDate = DateTimeUtil.parseDate(startStr).orElse(LocalDate.now().minusDays(30));
        LocalDate endDate = DateTimeUtil.parseDate(endStr).orElse(LocalDate.now());
        Long areaId = (areaParam != null && !areaParam.trim().isEmpty()) ?
                ValidationUtil.parseLongOrDefault(areaParam, 0L) : null;
        if (areaId != null && areaId == 0L) areaId = null;

        ReportSummaryDTO summary = reportService.generateReport(startDate, endDate, areaId);

        String filename = "waste_report_" + startDate + "_to_" + endDate + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        List<String> headers = Arrays.asList(
                "Worker Code",
                "Full Name",
                "Phone",
                "Assigned Area",
                "Assigned Tasks",
                "Completed Tasks",
                "Pending Tasks",
                "Total Waste Collected (kg)",
                "Completion Rate (%)"
        );

        List<List<String>> rows = new ArrayList<>();
        for (WorkerPerformanceDTO w : summary.getWorkerPerformances()) {
            rows.add(Arrays.asList(
                    w.getWorkerCode(),
                    w.getFullName(),
                    w.getPhone(),
                    w.getAreaName() != null ? w.getAreaName() : "N/A",
                    String.valueOf(w.getTotalAssignedTasks()),
                    String.valueOf(w.getCompletedTasks()),
                    String.valueOf(w.getPendingTasks()),
                    String.format("%.1f", w.getTotalCollectedKg()),
                    String.format("%.1f", w.getCompletionRate())
            ));
        }

        try (PrintWriter writer = response.getWriter()) {
            CsvExportUtil.writeCsv(writer, headers, rows);
        }
    }
}
