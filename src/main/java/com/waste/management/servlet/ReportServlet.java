package com.waste.management.servlet;

import com.waste.management.dao.AreaDAO;
import com.waste.management.dto.ReportSummaryDTO;
import com.waste.management.service.ReportService;
import com.waste.management.util.DateTimeUtil;
import com.waste.management.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet(name = "ReportServlet", urlPatterns = {"/reports", "/reports/print"})
public class ReportServlet extends BaseServlet {

    private ReportService reportService;
    private AreaDAO areaDAO;

    @Override
    public void init() {
        this.reportService = new ReportService();
        this.areaDAO = new AreaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String startStr = request.getParameter("startDate");
        String endStr = request.getParameter("endDate");
        String areaParam = request.getParameter("areaId");

        LocalDate startDate = DateTimeUtil.parseDate(startStr).orElse(LocalDate.now().minusDays(30));
        LocalDate endDate = DateTimeUtil.parseDate(endStr).orElse(LocalDate.now());
        Long areaId = (areaParam != null && !areaParam.trim().isEmpty()) ?
                ValidationUtil.parseLongOrDefault(areaParam, 0L) : null;
        if (areaId != null && areaId == 0L) areaId = null;

        ReportSummaryDTO summary = reportService.generateReport(startDate, endDate, areaId);

        request.setAttribute("summary", summary);
        request.setAttribute("areas", areaDAO.findAll());
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("selectedAreaId", areaId);

        if ("/reports/print".equals(request.getServletPath())) {
            forward(request, response, "reports/print");
        } else {
            forward(request, response, "reports/index");
        }
    }
}
