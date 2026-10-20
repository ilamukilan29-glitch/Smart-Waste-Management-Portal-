package com.waste.management.servlet;

import com.waste.management.dao.AreaDAO;
import com.waste.management.dao.CollectionPlanDAO;
import com.waste.management.dao.DepotDAO;
import com.waste.management.dao.WasteBinDAO;
import com.waste.management.dto.RoutePlanResultDTO;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.CollectionPlan;
import com.waste.management.model.Depot;
import com.waste.management.model.Vehicle;
import com.waste.management.model.WasteBin;
import com.waste.management.service.PriorityEngineService;
import com.waste.management.service.RouteOptimizationService;
import com.waste.management.service.VehicleService;
import com.waste.management.service.WorkerService;
import com.waste.management.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet(name = "RoutePlannerServlet", urlPatterns = {
        "/routes",
        "/routes/plan",
        "/routes/details"
})
public class RoutePlannerServlet extends BaseServlet {

    private RouteOptimizationService routeOptimizer;
    private PriorityEngineService priorityEngine;
    private AreaDAO areaDAO;
    private DepotDAO depotDAO;
    private VehicleService vehicleService;
    private WorkerService workerService;
    private WasteBinDAO binDAO;
    private CollectionPlanDAO planDAO;

    @Override
    public void init() {
        this.routeOptimizer = new RouteOptimizationService();
        this.priorityEngine = new PriorityEngineService();
        this.areaDAO = new AreaDAO();
        this.depotDAO = new DepotDAO();
        this.vehicleService = new VehicleService();
        this.workerService = new WorkerService();
        this.binDAO = new WasteBinDAO();
        this.planDAO = new CollectionPlanDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/routes/plan":
                showPlannerForm(request, response);
                break;
            case "/routes/details":
                showPlanDetails(request, response);
                break;
            case "/routes":
            default:
                listPlans(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/routes/plan".equals(path)) {
            generatePlan(request, response);
        } else {
            redirect(request, response, "/routes");
        }
    }

    private void listPlans(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<CollectionPlan> plans = planDAO.findAll();
        request.setAttribute("plans", plans);
        forward(request, response, "route/plans");
    }

    private void showPlannerForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String areaParam = request.getParameter("areaId");
        Long areaId = (areaParam != null && !areaParam.trim().isEmpty()) ?
                ValidationUtil.parseLongOrDefault(areaParam, 0L) : null;
        if (areaId != null && areaId == 0L) areaId = null;

        request.setAttribute("areas", areaDAO.findAll());
        request.setAttribute("selectedAreaId", areaId);

        if (areaId != null) {
            List<Depot> depots = depotDAO.findByAreaId(areaId);
            List<Vehicle> vehicles = vehicleService.getAvailableVehicles(areaId);
            List<WasteBin> eligibleBins = priorityEngine.getBinsEligibleForRoute(areaId);

            request.setAttribute("depots", depots);
            request.setAttribute("vehicles", vehicles);
            request.setAttribute("eligibleBins", eligibleBins);
            request.setAttribute("workers", workerService.getAvailableWorkers(areaId));
        }

        forward(request, response, "route/planner");
    }

    private void generatePlan(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long areaId = ValidationUtil.parseLong(request.getParameter("areaId"), "Area");
            Long depotId = ValidationUtil.parseLong(request.getParameter("depotId"), "Depot");
            Long vehicleId = ValidationUtil.parseLong(request.getParameter("vehicleId"), "Vehicle");
            String[] selectedBinIds = request.getParameterValues("binIds");

            if (selectedBinIds == null || selectedBinIds.length == 0) {
                throw new ValidationException("Please select at least one waste bin for collection.");
            }

            List<WasteBin> candidateBins = Arrays.stream(selectedBinIds)
                    .map(Long::parseLong)
                    .map(binDAO::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            RoutePlanResultDTO routeResult = routeOptimizer.generateRoutePlan(depotId, vehicleId, candidateBins);

            // Store routeResult in session so admin can review and confirm assignment
            request.getSession().setAttribute("ACTIVE_ROUTE_RESULT", routeResult);

            request.setAttribute("routeResult", routeResult);
            request.setAttribute("workers", workerService.getAvailableWorkers(areaId));
            request.setAttribute("areaId", areaId);

            forward(request, response, "route/details");
        } catch (ValidationException e) {
            setFlashError(request, e.getMessage());
            showPlannerForm(request, response);
        }
    }

    private void showPlanDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        Optional<CollectionPlan> planOpt = planDAO.findById(id);

        if (!planOpt.isPresent()) {
            setFlashError(request, "Collection plan not found.");
            redirect(request, response, "/routes");
            return;
        }

        request.setAttribute("plan", planOpt.get());
        forward(request, response, "route/details");
    }
}
