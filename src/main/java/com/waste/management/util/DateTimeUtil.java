package com.waste.management.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Utility utilizing Java 8 Date and Time API (JSR-310)
 * Demonstrates modern, immutable, thread-safe date/time handling.
 */
public class DateTimeUtil {

    public static final DateTimeFormatter DISPLAY_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter SQL_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDateTime(LocalDateTime ldt) {
        return ldt != null ? ldt.format(DISPLAY_DATETIME) : "N/A";
    }

    public static String formatDate(LocalDate ld) {
        return ld != null ? ld.format(DISPLAY_DATE) : "N/A";
    }

    public static Optional<LocalDate> parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(text.trim(), DISPLAY_DATE));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    public static Optional<LocalDateTime> parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDateTime.parse(text.trim(), SQL_DATETIME));
        } catch (DateTimeParseException e) {
            try {
                return Optional.of(LocalDateTime.parse(text.trim(), DISPLAY_DATETIME));
            } catch (DateTimeParseException e2) {
                return Optional.empty();
            }
        }
    }

    /**
     * Calculates the duration in human-readable format between two LocalDateTime instances
     * using Java 8 Duration.
     */
    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return "N/A";
        }
        Duration duration = Duration.between(start, end);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        if (hours > 0) {
            return String.format("%d hr %d min", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }
}
