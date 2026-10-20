package com.waste.management.servlet;

import com.waste.management.dao.AreaDAO;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.Worker;
import com.waste.management.service.WorkerService;
import com.waste.management.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "WorkerServlet", urlPatterns = {
        "/workers",
        "/workers/create",
        "/workers/edit",
        "/workers/status"
})
public class WorkerServlet extends BaseServlet {

    private WorkerService workerService;
    private AreaDAO areaDAO;

    @Override
    public void init() {
        this.workerService = new WorkerService();
        this.areaDAO = new AreaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/workers/create":
                showCreateForm(request, response);
                break;
            case "/workers/edit":
                showEditForm(request, response);
                break;
            case "/workers":
            default:
                listWorkers(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/workers/create":
                handleCreate(request, response);
                break;
            case "/workers/edit":
                handleEdit(request, response);
                break;
            case "/workers/status":
                handleStatusChange(request, response);
                break;
            default:
                redirect(request, response, "/workers");
                break;
        }
    }

    private void listWorkers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("workers", workerService.getAllWorkers());
        forward(request, response, "workers/list");
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("areas", areaDAO.findAll());
        forward(request, response, "workers/form");
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        Optional<Worker> workerOpt = workerService.getWorkerById(id);
        if (!workerOpt.isPresent()) {
            setFlashError(request, "Worker not found.");
            redirect(request, response, "/workers");
            return;
        }
        request.setAttribute("worker", workerOpt.get());
        request.setAttribute("areas", areaDAO.findAll());
        forward(request, response, "workers/form");
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Worker worker = new Worker();
            worker.setWorkerCode(request.getParameter("workerCode"));
            worker.setFullName(request.getParameter("fullName"));
            worker.setPhone(request.getParameter("phone"));
            worker.setAreaId(ValidationUtil.parseLong(request.getParameter("areaId"), "Area"));
            worker.setStatus("ACTIVE");
            worker.setAvailabilityStatus("AVAILABLE");

            workerService.createWorker(worker);
            setFlashSuccess(request, "Worker " + worker.getFullName() + " registered successfully.");
            redirect(request, response, "/workers");
        } catch (ValidationException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("areas", areaDAO.findAll());
            forward(request, response, "workers/form");
        }
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        try {
            Worker worker = workerService.getWorkerById(id)
                    .orElseThrow(() -> new ValidationException("Worker not found."));

            worker.setWorkerCode(request.getParameter("workerCode"));
            worker.setFullName(request.getParameter("fullName"));
            worker.setPhone(request.getParameter("phone"));
            worker.setAreaId(ValidationUtil.parseLong(request.getParameter("areaId"), "Area"));
            worker.setStatus(request.getParameter("status"));
            worker.setAvailabilityStatus(request.getParameter("availabilityStatus"));

            workerService.updateWorker(worker);
            setFlashSuccess(request, "Worker " + worker.getFullName() + " updated successfully.");
            redirect(request, response, "/workers");
        } catch (ValidationException e) {
            request.setAttribute("errorMessage", e.getMessage());
            showEditForm(request, response);
        }
    }

    private void handleStatusChange(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        String status = request.getParameter("availabilityStatus");
        workerService.updateAvailability(id, status);
        setFlashSuccess(request, "Worker availability updated.");
        redirect(request, response, "/workers");
    }
}
