package com.waste.management.service;

import com.waste.management.dao.WasteBinDAO;
import com.waste.management.dto.BinPriorityDTO;
import com.waste.management.model.PriorityLevel;
import com.waste.management.model.WasteBin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reusable Collection Priority Engine utilizing Java 8 Functional Interfaces,
 * Streams, Lambdas, and Method References to evaluate and prioritize municipal waste bins.
 */
public class PriorityEngineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriorityEngineService.class);

    // Configurable thresholds with default academic values
    private double criticalThreshold = 90.0;
    private double highThreshold = 80.0;
    private double mediumThreshold = 60.0;

    private final WasteBinDAO wasteBinDAO;

    // Java 8 Functional Interface instances implemented with Lambda expressions
    private final BinPriorityRule criticalRule = (bin, threshold) ->
            bin.isActive() && bin.getCurrentFillPercentage() >= threshold;

    private final BinPriorityRule highRule = (bin, threshold) ->
            bin.isActive() && bin.getCurrentFillPercentage() >= threshold && bin.getCurrentFillPercentage() < criticalThreshold;

    private final BinPriorityRule mediumRule = (bin, threshold) ->
            bin.isActive() && bin.getCurrentFillPercentage() >= threshold && bin.getCurrentFillPercentage() < highThreshold;

    public PriorityEngineService() {
        this(new WasteBinDAO());
    }

    public PriorityEngineService(WasteBinDAO wasteBinDAO) {
        this.wasteBinDAO = wasteBinDAO;
    }

    /**
     * Determines priority level for an individual bin using functional rules.
     */
    public PriorityLevel determinePriority(WasteBin bin) {
        if (bin == null || !bin.isActive()) {
            return PriorityLevel.LOW;
        }

        if (criticalRule.matches(bin, criticalThreshold)) {
            return PriorityLevel.CRITICAL;
        } else if (highRule.matches(bin, highThreshold)) {
            return PriorityLevel.HIGH;
        } else if (mediumRule.matches(bin, mediumThreshold)) {
            return PriorityLevel.MEDIUM;
        } else {
            return PriorityLevel.LOW;
        }
    }

    /**
     * Generates a prioritized queue of bins eligible for collection.
     * Uses Java 8 Stream API: filter, map, sorted with Comparator.
     * Automatically filters out bins that currently have active collection tasks to prevent duplicates.
     */
    public List<BinPriorityDTO> getPrioritizedBins(Long areaId, boolean includeLowPriority) {
        List<WasteBin> allBins = wasteBinDAO.findAll(null, areaId, null, "ACTIVE");

        // Java 8 Stream pipeline demonstrating lambda, method references, and sorting
        return allBins.stream()
                .filter(WasteBin::isActive)
                .map(bin -> {
                    boolean hasActive = wasteBinDAO.hasActiveTask(bin.getId());
                    PriorityLevel level = determinePriority(bin);
                    String reason = level == PriorityLevel.CRITICAL ? "Fill level >= " + criticalThreshold + "% (CRITICAL OVERFLOW RISK)" :
                                    level == PriorityLevel.HIGH ? "Fill level >= " + highThreshold + "% (High Collection Urgency)" :
                                    level == PriorityLevel.MEDIUM ? "Fill level >= " + mediumThreshold + "% (Standard Collection Threshold)" :
                                    "Routine Monitoring";

                    return BinPriorityDTO.fromEntity(bin, hasActive, reason);
                })
                .filter(dto -> includeLowPriority || dto.getPriorityLevel() != PriorityLevel.LOW)
                // Sort by priority level weight (descending), then fill percentage (descending)
                .sorted(Comparator
                        .comparing((BinPriorityDTO dto) -> dto.getPriorityLevel().getWeight()).reversed()
                        .thenComparing(Comparator.comparing(BinPriorityDTO::getCurrentFillPercentage).reversed())
                )
                .collect(Collectors.toList());
    }

    /**
     * Filters bins strictly eligible for route planning in a specific area:
     * - Must be active
     * - Fill level >= mediumThreshold (60%)
     * - Must NOT have an active collection task
     */
    public List<WasteBin> getBinsEligibleForRoute(Long areaId) {
        List<WasteBin> bins = wasteBinDAO.findAll(null, areaId, null, "ACTIVE");

        return bins.stream()
                .filter(WasteBin::isActive)
                .filter(b -> b.getCurrentFillPercentage() >= mediumThreshold)
                .filter(b -> !wasteBinDAO.hasActiveTask(b.getId()))
                .sorted(Comparator.comparing(WasteBin::getCurrentFillPercentage).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Groups prioritized bins by priority level using Java 8 Collectors.groupingBy.
     */
    public Map<PriorityLevel, List<BinPriorityDTO>> groupBinsByPriority(List<BinPriorityDTO> bins) {
        return bins.stream()
                .collect(Collectors.groupingBy(BinPriorityDTO::getPriorityLevel));
    }

    // Threshold Getters and Setters
    public double getCriticalThreshold() {
        return criticalThreshold;
    }

    public void setCriticalThreshold(double criticalThreshold) {
        this.criticalThreshold = criticalThreshold;
    }

    public double getHighThreshold() {
        return highThreshold;
    }

    public void setHighThreshold(double highThreshold) {
        this.highThreshold = highThreshold;
    }

    public double getMediumThreshold() {
        return mediumThreshold;
    }

    public void setMediumThreshold(double mediumThreshold) {
        this.mediumThreshold = mediumThreshold;
    }
}
