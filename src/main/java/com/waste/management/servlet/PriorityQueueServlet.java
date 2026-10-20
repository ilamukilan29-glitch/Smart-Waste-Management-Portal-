package com.waste.management.servlet;

import com.waste.management.dao.AreaDAO;
import com.waste.management.dto.BinPriorityDTO;
import com.waste.management.service.PriorityEngineService;
import com.waste.management.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "PriorityQueueServlet", urlPatterns = {"/priority"})
public class PriorityQueueServlet extends BaseServlet {

    private PriorityEngineService priorityEngine;
    private AreaDAO areaDAO;

    @Override
    public void init() {
        this.priorityEngine = new PriorityEngineService();
        this.areaDAO = new AreaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String areaParam = request.getParameter("areaId");
        Long areaId = (areaParam != null && !areaParam.trim().isEmpty()) ?
                ValidationUtil.parseLongOrDefault(areaParam, 0L) : null;
        if (areaId != null && areaId == 0L) areaId = null;

        boolean includeLow = "true".equalsIgnoreCase(request.getParameter("includeLow"));

        List<BinPriorityDTO> prioritizedBins = priorityEngine.getPrioritizedBins(areaId, includeLow);

        request.setAttribute("prioritizedBins", prioritizedBins);
        request.setAttribute("areas", areaDAO.findAll());
        request.setAttribute("selectedAreaId", areaId);
        request.setAttribute("includeLow", includeLow);
        request.setAttribute("criticalThreshold", priorityEngine.getCriticalThreshold());
        request.setAttribute("highThreshold", priorityEngine.getHighThreshold());
        request.setAttribute("mediumThreshold", priorityEngine.getMediumThreshold());

        forward(request, response, "priority/queue");
    }
}
