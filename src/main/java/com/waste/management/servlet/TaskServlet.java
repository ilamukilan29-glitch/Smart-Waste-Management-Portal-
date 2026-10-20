package com.waste.management.servlet;

import com.waste.management.dao.CollectionPlanDAO;
import com.waste.management.dto.RoutePlanResultDTO;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.CollectionPlan;
import com.waste.management.model.CollectionTask;
import com.waste.management.model.User;
import com.waste.management.service.TaskSchedulingService;
import com.waste.management.util.DateTimeUtil;
import com.waste.management.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "TaskServlet", urlPatterns = {
        "/tasks",
        "/tasks/assign",
        "/tasks/details",
        "/tasks/start",
        "/tasks/complete"
})
public class TaskServlet extends BaseServlet {

    private TaskSchedulingService taskService;
    private CollectionPlanDAO planDAO;

    @Override
    public void init() {
        this.taskService = new TaskSchedulingService();
        this.planDAO = new CollectionPlanDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/tasks/details":
                showTaskDetails(request, response);
                break;
            case "/tasks":
            default:
                listTasks(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/tasks/assign":
                handleAssignTask(request, response);
                break;
            case "/tasks/start":
                handleStartTask(request, response);
                break;
            case "/tasks/complete":
                handleCompleteTask(request, response);
                break;
            default:
                redirect(request, response, "/tasks");
                break;
        }
    }

    private void listTasks(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<CollectionTask> tasks = taskService.getAllTasks();
        request.setAttribute("tasks", tasks);
        forward(request, response, "tasks/list");
    }

    private void showTaskDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
        Optional<CollectionTask> taskOpt = taskService.getTaskById(id);

        if (!taskOpt.isPresent()) {
            setFlashError(request, "Task not found.");
            redirect(request, response, "/tasks");
            return;
        }

        CollectionTask task = taskOpt.get();
        request.setAttribute("task", task);

        // Fetch corresponding plan and stops
        Optional<CollectionPlan> planOpt = planDAO.findById(task.getCollectionPlanId());
        planOpt.ifPresent(plan -> request.setAttribute("plan", plan));

        forward(request, response, "tasks/details");
    }

    private void handleAssignTask(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            RoutePlanResultDTO activeResult = (RoutePlanResultDTO) request.getSession().getAttribute("ACTIVE_ROUTE_RESULT");
            if (activeResult == null) {
                throw new ValidationException("No active route plan in session. Please generate a route first.");
            }

            Long workerId = ValidationUtil.parseLong(request.getParameter("workerId"), "Worker");
            Long vehicleId = ValidationUtil.parseLong(request.getParameter("vehicleId"), "Vehicle");
            String dateParam = request.getParameter("plannedDate");
            LocalDate plannedDate = DateTimeUtil.parseDate(dateParam).orElse(LocalDate.now());

            User user = getLoggedInUser(request);
            Long userId = (user != null) ? user.getId() : 1L;

            CollectionTask task = taskService.schedulePlanAndTask(activeResult, workerId, vehicleId, plannedDate, userId);

            // Clear session plan
            request.getSession().removeAttribute("ACTIVE_ROUTE_RESULT");

            setFlashSuccess(request, "Collection Task " + task.getTaskCode() + " scheduled and assigned successfully!");
            redirect(request, response, "/tasks/details?id=" + task.getId());
        } catch (ValidationException e) {
            setFlashError(request, e.getMessage());
            redirect(request, response, "/routes/plan");
        }
    }

    private void handleStartTask(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long taskId = ValidationUtil.parseLongOrDefault(request.getParameter("taskId"), 0L);
        User user = getLoggedInUser(request);
        boolean isAdminOrSup = isSupervisor(request);

        try {
            taskService.startTask(taskId, user.getId(), isAdminOrSup);
            setFlashSuccess(request, "Task started! Status is now IN PROGRESS.");
        } catch (Exception e) {
            setFlashError(request, e.getMessage());
        }

        redirect(request, response, "/tasks/details?id=" + taskId);
    }

    private void handleCompleteTask(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long taskId = ValidationUtil.parseLongOrDefault(request.getParameter("taskId"), 0L);
        double collectedWeight = ValidationUtil.parseDoubleOrDefault(request.getParameter("collectedWeightKg"), 0.0);
        String remarks = request.getParameter("remarks");

        User user = getLoggedInUser(request);
        boolean isAdminOrSup = isSupervisor(request);

        try {
            taskService.completeTask(taskId, collectedWeight, remarks, user.getId(), isAdminOrSup);
            setFlashSuccess(request, "Task completed successfully! Collected weight recorded and bins reset.");
        } catch (Exception e) {
            setFlashError(request, e.getMessage());
        }

        redirect(request, response, "/tasks/details?id=" + taskId);
    }
}
