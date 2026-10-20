package com.waste.management.servlet;

import com.waste.management.dao.AreaDAO;
import com.waste.management.dao.DepotDAO;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.Vehicle;
import com.waste.management.service.VehicleService;
import com.waste.management.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "VehicleServlet", urlPatterns = {
        "/vehicles",
        "/vehicles/create",
        "/vehicles/edit",
        "/vehicles/status"
})
public class VehicleServlet extends BaseServlet {

    private VehicleService vehicleService;
    private AreaDAO areaDAO;
    private DepotDAO depotDAO;

    @Override
    public void init() {
        this.vehicleService = new VehicleService();
        this.areaDAO = new AreaDAO();
        this.depotDAO = new DepotDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/vehicles/create":
                showCreateForm(request, response);
                break;
            case "/vehicles/edit":
                showEditForm(request, response);
                break;
            case "/vehicles":
            default:
                listVehicles(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/vehicles/create":
                handleCreate(request, response);
                break;
            case "/vehicles/edit":
                handleEdit(request, response);
                break;
            case "/vehicles/status":
                handleStatusChange(request, response);
                break;
            default:
                redirect(request, response, "/vehicles");
                break;
        }
    }

    private void listVehicles(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("vehicles", vehicleService.getAllVehicles());
        forward(request, response, "vehicles/list");
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("areas", areaDAO.findAll());
        request.setAttribute("depots", depotDAO.findAll());
        forward(request, response, "vehicles/form");
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        Optional<Vehicle> vOpt = vehicleService.getVehicleById(id);
        if (!vOpt.isPresent()) {
            setFlashError(request, "Vehicle not found.");
            redirect(request, response, "/vehicles");
            return;
        }
        request.setAttribute("vehicle", vOpt.get());
        request.setAttribute("areas", areaDAO.findAll());
        request.setAttribute("depots", depotDAO.findAll());
        forward(request, response, "vehicles/form");
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setRegistrationNumber(request.getParameter("registrationNumber"));
            vehicle.setVehicleType(request.getParameter("vehicleType"));
            vehicle.setCapacityKg(ValidationUtil.parseDouble(request.getParameter("capacityKg"), "Capacity"));
            vehicle.setAreaId(ValidationUtil.parseLong(request.getParameter("areaId"), "Area"));
            vehicle.setDepotId(ValidationUtil.parseLong(request.getParameter("depotId"), "Depot"));
            vehicle.setStatus("ACTIVE");
            vehicle.setAvailabilityStatus("AVAILABLE");

            vehicleService.createVehicle(vehicle);
            setFlashSuccess(request, "Vehicle " + vehicle.getRegistrationNumber() + " registered successfully.");
            redirect(request, response, "/vehicles");
        } catch (ValidationException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("areas", areaDAO.findAll());
            request.setAttribute("depots", depotDAO.findAll());
            forward(request, response, "vehicles/form");
        }
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        try {
            Vehicle vehicle = vehicleService.getVehicleById(id)
                    .orElseThrow(() -> new ValidationException("Vehicle not found."));

            vehicle.setRegistrationNumber(request.getParameter("registrationNumber"));
            vehicle.setVehicleType(request.getParameter("vehicleType"));
            vehicle.setCapacityKg(ValidationUtil.parseDouble(request.getParameter("capacityKg"), "Capacity"));
            vehicle.setAreaId(ValidationUtil.parseLong(request.getParameter("areaId"), "Area"));
            vehicle.setDepotId(ValidationUtil.parseLong(request.getParameter("depotId"), "Depot"));
            vehicle.setStatus(request.getParameter("status"));
            vehicle.setAvailabilityStatus(request.getParameter("availabilityStatus"));

            vehicleService.updateVehicle(vehicle);
            setFlashSuccess(request, "Vehicle " + vehicle.getRegistrationNumber() + " updated successfully.");
            redirect(request, response, "/vehicles");
        } catch (ValidationException e) {
            request.setAttribute("errorMessage", e.getMessage());
            showEditForm(request, response);
        }
    }

    private void handleStatusChange(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        String status = request.getParameter("availabilityStatus");
        vehicleService.updateAvailability(id, status);
        setFlashSuccess(request, "Vehicle availability updated.");
        redirect(request, response, "/vehicles");
    }
}
