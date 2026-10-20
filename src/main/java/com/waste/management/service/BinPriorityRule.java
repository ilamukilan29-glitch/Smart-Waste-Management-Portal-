package com.waste.management.service;

import com.waste.management.model.WasteBin;

/**
 * Custom Java 8 Functional Interface for bin collection priority evaluation.
 * Demonstrates the @FunctionalInterface annotation and lambda compatibility.
 */
@FunctionalInterface
public interface BinPriorityRule {

    /**
     * Evaluates whether a waste bin satisfies a specific collection rule or threshold.
     *
     * @param bin the waste bin being evaluated
     * @param threshold the percentage or numerical threshold
     * @return true if the rule criteria are met, false otherwise
     */
    boolean matches(WasteBin bin, double threshold);

    /**
     * Default method demonstrating Java 8 interface default methods.
     * Inverts the priority rule condition.
     */
    default BinPriorityRule negateRule() {
        return (bin, threshold) -> !this.matches(bin, threshold);
    }
}
