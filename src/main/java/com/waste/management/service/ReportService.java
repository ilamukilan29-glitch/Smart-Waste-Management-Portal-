package com.waste.management.service;

import com.waste.management.dao.*;
import com.waste.management.dto.DashboardMetricsDTO;
import com.waste.management.dto.ReportSummaryDTO;
import com.waste.management.dto.WorkerPerformanceDTO;
import com.waste.management.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Report & Analytics Service.
 * Demonstrates Java 8 CompletableFuture for asynchronous, non-blocking data aggregation,
 * Java 8 Streams, Collectors (groupingBy, summingDouble, counting), and Date/Time API.
 */
public class ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);

    private final WasteBinDAO binDAO;
    private final CollectionTaskDAO taskDAO;
    private final CollectionRecordDAO recordDAO;
    private final WorkerDAO workerDAO;
    private final VehicleDAO vehicleDAO;
    private final CollectionPlanDAO planDAO;

    // Managed ExecutorService for asynchronous CompletableFuture processing
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4, new ThreadFactory() {
        private int count = 1;
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "WasteReportWorker-" + (count++));
            t.setDaemon(true);
            return t;
        }
    });

    public ReportService() {
        this(new WasteBinDAO(), new CollectionTaskDAO(), new CollectionRecordDAO(),
             new WorkerDAO(), new VehicleDAO(), new CollectionPlanDAO());
    }

    public ReportService(WasteBinDAO binDAO, CollectionTaskDAO taskDAO, CollectionRecordDAO recordDAO,
                         WorkerDAO workerDAO, VehicleDAO vehicleDAO, CollectionPlanDAO planDAO) {
        this.binDAO = binDAO;
        this.taskDAO = taskDAO;
        this.recordDAO = recordDAO;
        this.workerDAO = workerDAO;
        this.vehicleDAO = vehicleDAO;
        this.planDAO = planDAO;
    }

    /**
     * Aggregates live Dashboard KPIs using Java 8 CompletableFuture.
     * Concurrently queries bins, tasks, workers, and vehicles, then joins results.
     */
    public DashboardMetricsDTO getDashboardMetrics() {
        DashboardMetricsDTO metrics = new DashboardMetricsDTO();

        // 1. Asynchronously fetch bins
        CompletableFuture<List<WasteBin>> binsFuture = CompletableFuture.supplyAsync(
                () -> binDAO.findAll(null, null, null, null), EXECUTOR);

        // 2. Asynchronously fetch tasks
        CompletableFuture<List<CollectionTask>> tasksFuture = CompletableFuture.supplyAsync(
                taskDAO::findAll, EXECUTOR);

        // 3. Asynchronously fetch workers
        CompletableFuture<List<Worker>> workersFuture = CompletableFuture.supplyAsync(
                workerDAO::findAll, EXECUTOR);

        // 4. Asynchronously fetch vehicles
        CompletableFuture<List<Vehicle>> vehiclesFuture = CompletableFuture.supplyAsync(
                vehicleDAO::findAll, EXECUTOR);

        // Wait for all asynchronous computations to finish
        CompletableFuture.allOf(binsFuture, tasksFuture, workersFuture, vehiclesFuture).join();

        try {
            List<WasteBin> bins = binsFuture.get();
            List<CollectionTask> tasks = tasksFuture.get();
            List<Worker> workers = workersFuture.get();
            List<Vehicle> vehicles = vehiclesFuture.get();

            // Total registered bins
            metrics.setTotalBins(bins.size());

            // Java 8 Streams: Critical and collection-requiring counts
            long critical = bins.stream()
                    .filter(WasteBin::isActive)
                    .filter(b -> b.getCurrentFillPercentage() >= 90.0)
                    .count();
            metrics.setCriticalBins(critical);

            long requiring = bins.stream()
                    .filter(WasteBin::isActive)
                    .filter(b -> b.getCurrentFillPercentage() >= 80.0)
                    .count();
            metrics.setBinsRequiringCollection(requiring);

            // Bins grouped by waste type using Collectors.groupingBy and Collectors.counting
            Map<String, Long> wasteTypeCounts = bins.stream()
                    .collect(Collectors.groupingBy(b -> b.getWasteType().getDisplayName(), Collectors.counting()));
            metrics.setBinsByWasteType(wasteTypeCounts);

            // Bins grouped by priority
            Map<String, Long> priorityCounts = bins.stream()
                    .filter(WasteBin::isActive)
                    .collect(Collectors.groupingBy(b -> b.getPriorityLevel().getDisplayName(), Collectors.counting()));
            metrics.setBinsByPriority(priorityCounts);

            // Tasks metrics
            long pending = tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.PENDING || t.getStatus() == TaskStatus.ASSIGNED)
                    .count();
            metrics.setPendingTasks(pending);

            LocalDate today = LocalDate.now();
            long completedToday = tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                    .filter(t -> t.getActualCompletedAt() != null && t.getActualCompletedAt().toLocalDate().isEqual(today))
                    .count();
            metrics.setCollectionsCompletedToday(completedToday);

            // Tasks by status map
            Map<String, Long> statusCounts = tasks.stream()
                    .collect(Collectors.groupingBy(t -> t.getStatus().getDisplayName(), Collectors.counting()));
            metrics.setTasksByStatus(statusCounts);

            // Overall completion percentage
            long totalTasks = tasks.size();
            long totalCompleted = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            double completionRate = totalTasks > 0 ? ((double) totalCompleted / totalTasks) * 100.0 : 0.0;
            metrics.setCompletionPercentage(Math.round(completionRate * 10.0) / 10.0);

            // Fleet and Personnel availability using Stream filter & count
            long availableWorkers = workers.stream().filter(Worker::isAvailable).count();
            metrics.setAvailableWorkers(availableWorkers);

            long availableVehicles = vehicles.stream().filter(Vehicle::isAvailable).count();
            metrics.setAvailableVehicles(availableVehicles);

        } catch (Exception e) {
            LOGGER.error("Error computing dashboard metrics asynchronously", e);
        }

        return metrics;
    }

    /**
     * Generates a comprehensive ReportSummaryDTO for a date range and optional area filter.
     */
    public ReportSummaryDTO generateReport(LocalDate startDate, LocalDate endDate, Long areaId) {
        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.setStartDate(startDate);
        summary.setEndDate(endDate);
        summary.setAreaFilterId(areaId);

        List<CollectionRecord> records = recordDAO.findByDateRange(startDate, endDate, areaId);
        List<CollectionTask> allTasks = taskDAO.findAll();
        List<Worker> allWorkers = workerDAO.findAll();

        // Filter tasks in date range
        List<CollectionTask> tasksInRange = allTasks.stream()
                .filter(t -> {
                    LocalDate pDate = t.getPlannedDate();
                    if (pDate == null) return false;
                    boolean afterStart = (startDate == null || !pDate.isBefore(startDate));
                    boolean beforeEnd = (endDate == null || !pDate.isAfter(endDate));
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());

        summary.setTotalCollections(tasksInRange.size());
        summary.setCompletedCollections(tasksInRange.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count());
        summary.setPendingCollections(tasksInRange.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED).count());

        // Java 8 Collectors.summingDouble for total collected waste
        double totalWaste = records.stream()
                .collect(Collectors.summingDouble(CollectionRecord::getWeightCollectedKg));
        summary.setTotalWasteCollectedKg(Math.round(totalWaste * 10.0) / 10.0);

        // Waste collected by waste type using Stream grouping and summing
        Map<String, Double> wasteByType = new HashMap<>();
        for (CollectionRecord r : records) {
            Optional<WasteBin> bOpt = binDAO.findById(r.getWasteBinId());
            String typeName = bOpt.map(b -> b.getWasteType().getDisplayName()).orElse("General Municipal Waste");
            wasteByType.merge(typeName, r.getWeightCollectedKg(), (v1, v2) -> v1 + v2);
        }
        summary.setWasteCollectedByType(wasteByType);

        // Worker performance aggregation using Streams
        List<WorkerPerformanceDTO> workerMetrics = allWorkers.stream()
                .map(worker -> {
                    WorkerPerformanceDTO dto = new WorkerPerformanceDTO();
                    dto.setWorkerId(worker.getId());
                    dto.setWorkerCode(worker.getWorkerCode());
                    dto.setFullName(worker.getFullName());
                    dto.setPhone(worker.getPhone());
                    dto.setAreaName(worker.getAreaName());

                    List<CollectionTask> workerTasks = tasksInRange.stream()
                            .filter(t -> t.getWorkerId().equals(worker.getId()))
                            .collect(Collectors.toList());

                    long assigned = workerTasks.size();
                    long done = workerTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
                    double weight = workerTasks.stream()
                            .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                            .mapToDouble(CollectionTask::getCollectedWeightKg)
                            .sum();

                    dto.setTotalAssignedTasks(assigned);
                    dto.setCompletedTasks(done);
                    dto.setPendingTasks(assigned - done);
                    dto.setTotalCollectedKg(Math.round(weight * 10.0) / 10.0);
                    dto.setCompletionRate(assigned > 0 ? Math.round(((double) done / assigned) * 1000.0) / 10.0 : 0.0);

                    return dto;
                })
                .sorted(Comparator.comparing(WorkerPerformanceDTO::getCompletedTasks).reversed())
                .collect(Collectors.toList());

        summary.setWorkerPerformances(workerMetrics);
        return summary;
    }

    /**
     * Clean shutdown of the thread pool executor.
     */
    public static void shutdownExecutor() {
        try {
            EXECUTOR.shutdown();
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
