package com.waste.management.servlet;

import com.waste.management.dto.DashboardMetricsDTO;
import com.waste.management.model.Role;
import com.waste.management.model.User;
import com.waste.management.service.ReportService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard", "/api/dashboard"})
public class DashboardServlet extends BaseServlet {

    private ReportService reportService;

    @Override
    public void init() {
        this.reportService = new ReportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getLoggedInUser(request);
        if (user != null && user.getRole() == Role.WORKER) {
            redirect(request, response, "/worker/tasks");
            return;
        }

        DashboardMetricsDTO metrics = reportService.getDashboardMetrics();

        if ("/api/dashboard".equals(request.getServletPath()) ||
            "application/json".equalsIgnoreCase(request.getHeader("Accept"))) {
            sendJson(response, metrics);
            return;
        }

        request.setAttribute("metrics", metrics);
        forward(request, response, "dashboard/index");
    }
}
