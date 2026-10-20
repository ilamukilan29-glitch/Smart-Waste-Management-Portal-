package com.waste.management.service;

import com.waste.management.dao.WorkerDAO;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.Worker;
import com.waste.management.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class WorkerService {

    private final WorkerDAO workerDAO;

    public WorkerService() {
        this(new WorkerDAO());
    }

    public WorkerService(WorkerDAO workerDAO) {
        this.workerDAO = workerDAO;
    }

    public List<Worker> getAllWorkers() {
        return workerDAO.findAll();
    }

    public Optional<Worker> getWorkerById(Long id) {
        return workerDAO.findById(id);
    }

    public Optional<Worker> getWorkerByUserId(Long userId) {
        return workerDAO.findByUserId(userId);
    }

    public List<Worker> getAvailableWorkers(Long areaId) {
        return workerDAO.findAvailableWorkers(areaId);
    }

    public Worker createWorker(Worker worker) {
        validateWorker(worker);
        return workerDAO.save(worker);
    }

    public boolean updateWorker(Worker worker) {
        if (worker.getId() == null || worker.getId() <= 0) {
            throw new ValidationException("Invalid worker ID for update.");
        }
        validateWorker(worker);
        return workerDAO.update(worker);
    }

    public boolean updateAvailability(Long workerId, String availabilityStatus) {
        return workerDAO.updateAvailability(workerId, availabilityStatus);
    }

    private void validateWorker(Worker worker) {
        ValidationUtil.requireNonEmpty(worker.getWorkerCode(), "Worker Code");
        ValidationUtil.requireNonEmpty(worker.getFullName(), "Full Name");
        ValidationUtil.validatePhone(worker.getPhone());
        if (worker.getAreaId() == null || worker.getAreaId() <= 0) {
            throw new ValidationException("Area is required.");
        }
    }
}
