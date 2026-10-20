package com.waste.management.service;

import com.waste.management.dao.WasteBinDAO;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.WasteBin;
import com.waste.management.model.WasteType;
import com.waste.management.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

public class BinService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinService.class);
    private final WasteBinDAO binDAO;
    private static final SecureRandom RANDOM = new SecureRandom();

    public BinService() {
        this(new WasteBinDAO());
    }

    public BinService(WasteBinDAO binDAO) {
        this.binDAO = binDAO;
    }

    public List<WasteBin> getBins(String search, Long areaId, WasteType wasteType, String status) {
        return binDAO.findAll(search, areaId, wasteType, status);
    }

    public Optional<WasteBin> getBinById(Long id) {
        return binDAO.findById(id);
    }

    public WasteBin createBin(WasteBin bin) {
        validateBin(bin);

        // Check unique code
        if (binDAO.findByBinCode(bin.getBinCode()).isPresent()) {
            throw new ValidationException("Bin code '" + bin.getBinCode() + "' is already registered.");
        }

        return binDAO.save(bin);
    }

    public boolean updateBin(WasteBin bin) {
        if (bin.getId() == null || bin.getId() <= 0) {
            throw new ValidationException("Invalid bin ID for update.");
        }
        validateBin(bin);

        Optional<WasteBin> existing = binDAO.findByBinCode(bin.getBinCode());
        if (existing.isPresent() && !existing.get().getId().equals(bin.getId())) {
            throw new ValidationException("Bin code '" + bin.getBinCode() + "' is already used by another bin.");
        }

        return binDAO.update(bin);
    }

    public boolean updateFillLevel(Long binId, double fillPercentage) {
        ValidationUtil.validateFillPercentage(fillPercentage);
        return binDAO.updateFillLevel(binId, fillPercentage);
    }

    public boolean deleteBin(Long binId) {
        return binDAO.deleteOrDeactivate(binId);
    }

    /**
     * Simulation Feature:
     * Simulates periodic waste generation by increasing the fill levels of active bins.
     * Clearly labeled as a software simulation for academic demonstration.
     *
     * @param areaId optional area filter, null for all areas
     * @param incrementRangeMax maximum percentage increment (e.g. 25.0)
     * @return number of bins simulated
     */
    public int simulateWasteAccumulation(Long areaId, double incrementRangeMax) {
        List<WasteBin> bins = binDAO.findAll(null, areaId, null, "ACTIVE");
        int updatedCount = 0;

        for (WasteBin bin : bins) {
            // Add a random increment between 5.0% and incrementRangeMax
            double increment = 5.0 + (RANDOM.nextDouble() * (incrementRangeMax - 5.0));
            double newFill = Math.min(100.0, Math.round((bin.getCurrentFillPercentage() + increment) * 10.0) / 10.0);

            if (binDAO.updateFillLevel(bin.getId(), newFill)) {
                updatedCount++;
            }
        }

        LOGGER.info("Simulation completed: Updated fill levels for {} bins.", updatedCount);
        return updatedCount;
    }

    /**
     * Resets a bin's fill level to a specific percentage (e.g., 0% or custom value).
     */
    public boolean resetFillLevel(Long binId, double targetFillPercentage) {
        ValidationUtil.validateFillPercentage(targetFillPercentage);
        return binDAO.updateFillLevel(binId, targetFillPercentage);
    }

    private void validateBin(WasteBin bin) {
        ValidationUtil.requireNonEmpty(bin.getBinCode(), "Bin Code");
        ValidationUtil.requireNonEmpty(bin.getLocationName(), "Location Name");
        ValidationUtil.requireNonEmpty(bin.getAddress(), "Address");
        if (bin.getAreaId() == null || bin.getAreaId() <= 0) {
            throw new ValidationException("Area is required.");
        }
        if (bin.getWasteType() == null) {
            throw new ValidationException("Waste type is required.");
        }
        ValidationUtil.validatePositiveCapacity(bin.getCapacityKg());
        ValidationUtil.validateFillPercentage(bin.getCurrentFillPercentage());
    }
}
