package com.waste.management.servlet;

import com.waste.management.service.PriorityEngineService;
import com.waste.management.service.RouteOptimizationService;
import com.waste.management.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "SettingsServlet", urlPatterns = {"/settings"})
public class SettingsServlet extends BaseServlet {

    private PriorityEngineService priorityEngine;
    private RouteOptimizationService routeOptimizer;

    @Override
    public void init() {
        this.priorityEngine = new PriorityEngineService();
        this.routeOptimizer = new RouteOptimizationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("criticalThreshold", priorityEngine.getCriticalThreshold());
        request.setAttribute("highThreshold", priorityEngine.getHighThreshold());
        request.setAttribute("mediumThreshold", priorityEngine.getMediumThreshold());
        request.setAttribute("truckSpeed", routeOptimizer.getAverageTruckSpeedKmh());
        request.setAttribute("serviceTime", routeOptimizer.getServiceTimePerBinMinutes());
        forward(request, response, "settings/index");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            double crit = ValidationUtil.parseDouble(request.getParameter("criticalThreshold"), "Critical Threshold");
            double high = ValidationUtil.parseDouble(request.getParameter("highThreshold"), "High Threshold");
            double med = ValidationUtil.parseDouble(request.getParameter("mediumThreshold"), "Medium Threshold");
            double speed = ValidationUtil.parseDouble(request.getParameter("truckSpeed"), "Average Truck Speed");
            int serviceTime = (int) ValidationUtil.parseLong(request.getParameter("serviceTime"), "Service Time");

            if (crit <= high || high <= med) {
                setFlashError(request, "Thresholds must satisfy: Critical > High > Medium.");
            } else {
                priorityEngine.setCriticalThreshold(crit);
                priorityEngine.setHighThreshold(high);
                priorityEngine.setMediumThreshold(med);
                routeOptimizer.setAverageTruckSpeedKmh(speed);
                routeOptimizer.setServiceTimePerBinMinutes(serviceTime);
                setFlashSuccess(request, "System settings updated successfully.");
            }
        } catch (Exception e) {
            setFlashError(request, "Failed to update settings: " + e.getMessage());
        }

        redirect(request, response, "/settings");
    }
}
