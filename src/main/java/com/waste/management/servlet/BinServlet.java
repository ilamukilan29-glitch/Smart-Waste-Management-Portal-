package com.waste.management.servlet;

import com.waste.management.dao.AreaDAO;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.WasteBin;
import com.waste.management.model.WasteType;
import com.waste.management.service.BinService;
import com.waste.management.service.NotificationService;
import com.waste.management.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "BinServlet", urlPatterns = {
        "/bins",
        "/bins/create",
        "/bins/edit",
        "/bins/details",
        "/bins/delete",
        "/bins/update-fill"
})
public class BinServlet extends BaseServlet {

    private BinService binService;
    private AreaDAO areaDAO;
    private NotificationService notificationService;

    @Override
    public void init() {
        this.binService = new BinService();
        this.areaDAO = new AreaDAO();
        this.notificationService = new NotificationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/bins/create":
                showCreateForm(request, response);
                break;
            case "/bins/edit":
                showEditForm(request, response);
                break;
            case "/bins/details":
                showBinDetails(request, response);
                break;
            case "/bins":
            default:
                listBins(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/bins/create":
                handleCreate(request, response);
                break;
            case "/bins/edit":
                handleEdit(request, response);
                break;
            case "/bins/delete":
                handleDelete(request, response);
                break;
            case "/bins/update-fill":
                handleUpdateFill(request, response);
                break;
            default:
                redirect(request, response, "/bins");
                break;
        }
    }

    private void listBins(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String search = request.getParameter("search");
        String areaParam = request.getParameter("areaId");
        String typeParam = request.getParameter("wasteType");
        String status = request.getParameter("status");

        Long areaId = (areaParam != null && !areaParam.trim().isEmpty()) ? Long.parseLong(areaParam.trim()) : null;
        WasteType wasteType = (typeParam != null && !typeParam.trim().isEmpty()) ? WasteType.valueOf(typeParam.trim()) : null;

        List<WasteBin> bins = binService.getBins(search, areaId, wasteType, status);

        request.setAttribute("bins", bins);
        request.setAttribute("areas", areaDAO.findAll());
        request.setAttribute("wasteTypes", WasteType.values());
        request.setAttribute("search", search);
        request.setAttribute("selectedAreaId", areaId);
        request.setAttribute("selectedWasteType", wasteType);
        request.setAttribute("selectedStatus", status);

        forward(request, response, "bins/list");
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("areas", areaDAO.findAll());
        request.setAttribute("wasteTypes", WasteType.values());
        forward(request, response, "bins/form");
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        Optional<WasteBin> binOpt = binService.getBinById(id);
        if (!binOpt.isPresent()) {
            setFlashError(request, "Bin not found.");
            redirect(request, response, "/bins");
            return;
        }
        request.setAttribute("bin", binOpt.get());
        request.setAttribute("areas", areaDAO.findAll());
        request.setAttribute("wasteTypes", WasteType.values());
        forward(request, response, "bins/form");
    }

    private void showBinDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        Optional<WasteBin> binOpt = binService.getBinById(id);
        if (!binOpt.isPresent()) {
            setFlashError(request, "Bin not found.");
            redirect(request, response, "/bins");
            return;
        }
        request.setAttribute("bin", binOpt.get());
        forward(request, response, "bins/details");
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            WasteBin bin = new WasteBin();
            bin.setBinCode(request.getParameter("binCode"));
            bin.setAreaId(ValidationUtil.parseLong(request.getParameter("areaId"), "Area"));
            bin.setLocationName(request.getParameter("locationName"));
            bin.setAddress(request.getParameter("address"));
            bin.setLatitude(ValidationUtil.parseDoubleOrDefault(request.getParameter("latitude"), 13.0827));
            bin.setLongitude(ValidationUtil.parseDoubleOrDefault(request.getParameter("longitude"), 80.2707));
            bin.setWasteType(WasteType.valueOf(request.getParameter("wasteType")));
            bin.setCapacityKg(ValidationUtil.parseDouble(request.getParameter("capacityKg"), "Capacity"));
            bin.setCurrentFillPercentage(ValidationUtil.parseDoubleOrDefault(request.getParameter("currentFillPercentage"), 0.0));
            bin.setStatus("ACTIVE");

            binService.createBin(bin);
            notificationService.triggerCriticalBinAlert(bin);

            setFlashSuccess(request, "Waste Bin " + bin.getBinCode() + " registered successfully.");
            redirect(request, response, "/bins");
        } catch (ValidationException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("areas", areaDAO.findAll());
            request.setAttribute("wasteTypes", WasteType.values());
            forward(request, response, "bins/form");
        }
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        try {
            WasteBin bin = binService.getBinById(id)
                    .orElseThrow(() -> new ValidationException("Bin not found."));

            bin.setBinCode(request.getParameter("binCode"));
            bin.setAreaId(ValidationUtil.parseLong(request.getParameter("areaId"), "Area"));
            bin.setLocationName(request.getParameter("locationName"));
            bin.setAddress(request.getParameter("address"));
            bin.setLatitude(ValidationUtil.parseDoubleOrDefault(request.getParameter("latitude"), bin.getLatitude()));
            bin.setLongitude(ValidationUtil.parseDoubleOrDefault(request.getParameter("longitude"), bin.getLongitude()));
            bin.setWasteType(WasteType.valueOf(request.getParameter("wasteType")));
            bin.setCapacityKg(ValidationUtil.parseDouble(request.getParameter("capacityKg"), "Capacity"));
            bin.setCurrentFillPercentage(ValidationUtil.parseDoubleOrDefault(request.getParameter("currentFillPercentage"), bin.getCurrentFillPercentage()));
            bin.setStatus(request.getParameter("status"));

            binService.updateBin(bin);
            notificationService.triggerCriticalBinAlert(bin);

            setFlashSuccess(request, "Waste Bin " + bin.getBinCode() + " updated successfully.");
            redirect(request, response, "/bins");
        } catch (ValidationException e) {
            request.setAttribute("errorMessage", e.getMessage());
            showEditForm(request, response);
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        if (binService.deleteBin(id)) {
            setFlashSuccess(request, "Bin deactivated successfully.");
        } else {
            setFlashError(request, "Failed to deactivate bin.");
        }
        redirect(request, response, "/bins");
    }

    private void handleUpdateFill(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        double fill = ValidationUtil.parseDoubleOrDefault(request.getParameter("fillPercentage"), -1.0);

        try {
            binService.updateFillLevel(id, fill);
            binService.getBinById(id).ifPresent(notificationService::triggerCriticalBinAlert);
            setFlashSuccess(request, "Fill level updated successfully to " + fill + "%.");
        } catch (ValidationException e) {
            setFlashError(request, e.getMessage());
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            response.sendRedirect(referer);
        } else {
            redirect(request, response, "/bins");
        }
    }
}
