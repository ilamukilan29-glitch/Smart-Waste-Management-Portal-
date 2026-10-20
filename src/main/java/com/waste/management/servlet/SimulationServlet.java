package com.waste.management.servlet;

import com.waste.management.dao.AreaDAO;
import com.waste.management.service.BinService;
import com.waste.management.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "SimulationServlet", urlPatterns = {"/simulation"})
public class SimulationServlet extends BaseServlet {

    private BinService binService;
    private AreaDAO areaDAO;

    @Override
    public void init() {
        this.binService = new BinService();
        this.areaDAO = new AreaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("areas", areaDAO.findAll());
        forward(request, response, "bins/simulation");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        String areaParam = request.getParameter("areaId");
        Long areaId = (areaParam != null && !areaParam.trim().isEmpty()) ?
                ValidationUtil.parseLongOrDefault(areaParam, 0L) : null;
        if (areaId != null && areaId == 0L) areaId = null;

        if ("accumulate".equals(action)) {
            double maxInc = ValidationUtil.parseDoubleOrDefault(request.getParameter("maxIncrement"), 25.0);
            int count = binService.simulateWasteAccumulation(areaId, maxInc);
            setFlashSuccess(request, "Simulation executed: Updated fill levels for " + count + " active bins.");
        } else if ("reset_all".equals(action)) {
            binService.getBins(null, areaId, null, "ACTIVE").forEach(b -> binService.resetFillLevel(b.getId(), 0.0));
            setFlashSuccess(request, "Simulation reset: All bins in the selected scope reset to 0% fill level.");
        }

        redirect(request, response, "/bins");
    }
}
