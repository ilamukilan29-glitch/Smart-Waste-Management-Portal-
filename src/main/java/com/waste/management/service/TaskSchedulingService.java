package com.waste.management.service;

import com.waste.management.dao.*;
import com.waste.management.dto.RoutePlanResultDTO;
import com.waste.management.dto.RouteStopDTO;
import com.waste.management.exception.AuthorizationException;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.*;
import com.waste.management.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskSchedulingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskSchedulingService.class);

    private final CollectionPlanDAO planDAO;
    private final CollectionTaskDAO taskDAO;
    private final WorkerDAO workerDAO;
    private final VehicleDAO vehicleDAO;
    private final WasteBinDAO binDAO;

    public TaskSchedulingService() {
        this(new CollectionPlanDAO(), new CollectionTaskDAO(), new WorkerDAO(), new VehicleDAO(), new WasteBinDAO());
    }

    public TaskSchedulingService(CollectionPlanDAO planDAO, CollectionTaskDAO taskDAO, WorkerDAO workerDAO, VehicleDAO vehicleDAO, WasteBinDAO binDAO) {
        this.planDAO = planDAO;
        this.taskDAO = taskDAO;
        this.workerDAO = workerDAO;
        this.vehicleDAO = vehicleDAO;
        this.binDAO = binDAO;
    }

    /**
     * Saves an optimized route plan and schedules an assigned operational task.
     */
    public CollectionTask schedulePlanAndTask(RoutePlanResultDTO routeResult, Long workerId, Long vehicleId,
                                            LocalDate plannedDate, Long createdByUserId) {
        if (routeResult == null || routeResult.getStops() == null || routeResult.getStops().isEmpty()) {
            throw new ValidationException("Cannot schedule task: Route plan is empty.");
        }

        // Validate worker
        Worker worker = workerDAO.findById(workerId)
                .orElseThrow(() -> new ValidationException("Worker not found: ID " + workerId));
        if (!worker.isAvailable()) {
            throw new ValidationException("Worker " + worker.getFullName() + " is currently " + worker.getAvailabilityStatus() + ".");
        }

        // Validate vehicle
        Vehicle vehicle = vehicleDAO.findById(vehicleId)
                .orElseThrow(() -> new ValidationException("Vehicle not found: ID " + vehicleId));
        if (!vehicle.isAvailable()) {
            throw new ValidationException("Vehicle " + vehicle.getRegistrationNumber() + " is currently " + vehicle.getAvailabilityStatus() + ".");
        }

        // Capacity check
        if (routeResult.getTotalWasteEstimatedKg() > vehicle.getCapacityKg()) {
            throw new ValidationException(String.format("Route planned waste (%.1f kg) exceeds vehicle capacity (%.1f kg).",
                    routeResult.getTotalWasteEstimatedKg(), vehicle.getCapacityKg()));
        }

        String timestampStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String planCode = "PLAN-" + timestampStr;
        String taskCode = "TASK-" + timestampStr;

        // 1. Create CollectionPlan
        CollectionPlan plan = new CollectionPlan();
        plan.setPlanCode(planCode);
        plan.setAreaId(routeResult.getAreaId());
        plan.setDepotId(routeResult.getDepotId());
        plan.setVehicleId(vehicleId);
        plan.setWorkerId(workerId);
        plan.setTotalDistanceKm(routeResult.getTotalDistanceKm());
        plan.setEstimatedDurationMinutes(routeResult.getTotalDurationMinutes());
        plan.setTotalWasteEstimatedKg(routeResult.getTotalWasteEstimatedKg());
        plan.setStatus(TaskStatus.ASSIGNED);
        plan.setPlannedCollectionDate(plannedDate != null ? plannedDate : LocalDate.now());
        plan.setCreatedByUserId(createdByUserId);

        // Map DTO stops to entity stops
        List<RouteStop> entityStops = new ArrayList<>();
        for (RouteStopDTO dto : routeResult.getStops()) {
            RouteStop stop = new RouteStop();
            stop.setStopOrder(dto.getStopOrder());
            stop.setStopType(dto.getStopType());
            stop.setWasteBinId(dto.getWasteBinId());
            stop.setLocationLabel(dto.getLocationLabel());
            stop.setDistanceFromPrevKm(dto.getDistanceFromPrevKm());
            stop.setCumulativeDistanceKm(dto.getCumulativeDistanceKm());
            stop.setExpectedWasteKg(dto.getExpectedWasteKg());
            stop.setStatus("PENDING");
            entityStops.add(stop);
        }

        // Save plan transactionally
        CollectionPlan savedPlan = planDAO.save(plan, entityStops);

        // 2. Create CollectionTask
        CollectionTask task = new CollectionTask();
        task.setTaskCode(taskCode);
        task.setCollectionPlanId(savedPlan.getId());
        task.setWorkerId(workerId);
        task.setVehicleId(vehicleId);
        task.setPlannedDate(plan.getPlannedCollectionDate());
        task.setStatus(TaskStatus.ASSIGNED);

        CollectionTask savedTask = taskDAO.save(task);

        // 3. Mark worker ON_DUTY and vehicle ON_ROUTE
        workerDAO.updateAvailability(workerId, "ON_DUTY");
        vehicleDAO.updateAvailability(vehicleId, "ON_ROUTE");

        LOGGER.info("Task scheduled successfully: {} for Plan: {}", taskCode, planCode);
        return savedTask;
    }

    /**
     * Starts an assigned task (worker transition to IN_PROGRESS).
     */
    public boolean startTask(Long taskId, Long workerUserId, boolean isAdminOrSupervisor) {
        CollectionTask task = taskDAO.findById(taskId)
                .orElseThrow(() -> new ValidationException("Task not found: ID " + taskId));

        // Authorization check: only assigned worker or supervisor/admin can start
        if (!isAdminOrSupervisor) {
            Optional<Worker> workerOpt = workerDAO.findByUserId(workerUserId);
            if (!workerOpt.isPresent() || !workerOpt.get().getId().equals(task.getWorkerId())) {
                throw new AuthorizationException("You are not authorized to start this task.");
            }
        }

        if (task.getStatus() != TaskStatus.ASSIGNED) {
            throw new ValidationException("Cannot start task in status: " + task.getStatus());
        }

        planDAO.updateStatus(task.getCollectionPlanId(), TaskStatus.IN_PROGRESS);
        return taskDAO.updateStatus(taskId, TaskStatus.IN_PROGRESS);
    }

    /**
     * Completes a collection task with measured waste quantity and remarks.
     * Executes atomic multi-table transaction.
     */
    public boolean completeTask(Long taskId, double collectedWeightKg, String remarks,
                                Long workerUserId, boolean isAdminOrSupervisor) {
        ValidationUtil.validatePositiveWeight(collectedWeightKg);

        CollectionTask task = taskDAO.findById(taskId)
                .orElseThrow(() -> new ValidationException("Task not found: ID " + taskId));

        // Authorization check
        if (!isAdminOrSupervisor) {
            Optional<Worker> workerOpt = workerDAO.findByUserId(workerUserId);
            if (!workerOpt.isPresent() || !workerOpt.get().getId().equals(task.getWorkerId())) {
                throw new AuthorizationException("You are not authorized to complete this task.");
            }
        }

        if (task.getStatus() != TaskStatus.IN_PROGRESS && task.getStatus() != TaskStatus.ASSIGNED) {
            throw new ValidationException("Cannot complete task in status: " + task.getStatus());
        }

        // Fetch route stops to create collection records
        CollectionPlan plan = planDAO.findById(task.getCollectionPlanId())
                .orElseThrow(() -> new ValidationException("Plan not found for task"));

        List<CollectionRecord> records = new ArrayList<>();
        for (RouteStop stop : plan.getStops()) {
            if ("BIN".equalsIgnoreCase(stop.getStopType()) && stop.getWasteBinId() != null) {
                Optional<WasteBin> binOpt = binDAO.findById(stop.getWasteBinId());
                double fillBefore = binOpt.map(WasteBin::getCurrentFillPercentage).orElse(85.0);

                CollectionRecord rec = new CollectionRecord();
                rec.setTaskId(taskId);
                rec.setWasteBinId(stop.getWasteBinId());
                rec.setFillPercentageBefore(fillBefore);
                rec.setFillPercentageAfter(0.0);
                rec.setWeightCollectedKg(stop.getExpectedWasteKg());
                rec.setRemarks("Successfully collected by " + task.getWorkerName());
                records.add(rec);
            }
        }

        return taskDAO.completeTask(taskId, collectedWeightKg, remarks, records);
    }

    public List<CollectionTask> getAllTasks() {
        return taskDAO.findAll();
    }

    public Optional<CollectionTask> getTaskById(Long id) {
        return taskDAO.findById(id);
    }

    public List<CollectionTask> getTasksForWorker(Long workerId) {
        return taskDAO.findByWorkerId(workerId);
    }
}
