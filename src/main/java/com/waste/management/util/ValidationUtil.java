package com.waste.management.util;

import com.waste.management.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Server-side input validation utility.
 * Protects against bad input, out-of-range metrics, and injection attacks.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+\\-\\s()]{7,20}$");

    public static void requireNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required.");
        }
    }

    public static void validateFillPercentage(double fillPercentage) throws ValidationException {
        if (fillPercentage < 0.0 || fillPercentage > 100.0) {
            throw new ValidationException("Fill percentage must be between 0.0 and 100.0%. Provided: " + fillPercentage);
        }
    }

    public static void validatePositiveCapacity(double capacity) throws ValidationException {
        if (capacity <= 0.0) {
            throw new ValidationException("Capacity must be a positive number greater than 0. Provided: " + capacity);
        }
    }

    public static void validatePositiveWeight(double weight) throws ValidationException {
        if (weight < 0.0) {
            throw new ValidationException("Collected weight cannot be negative. Provided: " + weight);
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        requireNonEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    public static void validatePhone(String phone) throws ValidationException {
        requireNonEmpty(phone, "Phone number");
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new ValidationException("Invalid phone number format: " + phone);
        }
    }

    public static long parseLong(String val, String fieldName) throws ValidationException {
        requireNonEmpty(val, fieldName);
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid integer.");
        }
    }

    public static double parseDouble(String val, String fieldName) throws ValidationException {
        requireNonEmpty(val, fieldName);
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid number.");
        }
    }

    public static double parseDoubleOrDefault(String val, double defaultValue) {
        if (val == null || val.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long parseLongOrDefault(String val, long defaultValue) {
        if (val == null || val.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
