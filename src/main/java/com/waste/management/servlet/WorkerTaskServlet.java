package com.waste.management.servlet;

import com.waste.management.model.CollectionTask;
import com.waste.management.model.User;
import com.waste.management.model.Worker;
import com.waste.management.service.TaskSchedulingService;
import com.waste.management.service.WorkerService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "WorkerTaskServlet", urlPatterns = {"/worker/tasks", "/worker"})
public class WorkerTaskServlet extends BaseServlet {

    private TaskSchedulingService taskService;
    private WorkerService workerService;

    @Override
    public void init() {
        this.taskService = new TaskSchedulingService();
        this.workerService = new WorkerService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getLoggedInUser(request);
        if (user == null) {
            redirect(request, response, "/login");
            return;
        }

        // Find worker profile linked to this user
        Optional<Worker> workerOpt = workerService.getWorkerByUserId(user.getId());
        List<CollectionTask> tasks;

        if (workerOpt.isPresent()) {
            Worker worker = workerOpt.get();
            request.setAttribute("worker", worker);
            tasks = taskService.getTasksForWorker(worker.getId());
        } else {
            // If admin or supervisor viewing worker portal or unlinked worker
            tasks = Collections.emptyList();
            request.setAttribute("unlinkedWorker", true);
        }

        request.setAttribute("tasks", tasks);
        forward(request, response, "tasks/worker_tasks");
    }
}
