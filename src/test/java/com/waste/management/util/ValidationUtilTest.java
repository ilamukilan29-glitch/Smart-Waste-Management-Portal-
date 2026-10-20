package com.waste.management.util;

import com.waste.management.exception.ValidationException;
import org.junit.Test;

import static org.junit.Assert.*;

public class ValidationUtilTest {

    @Test
    public void testValidFillPercentage() {
        ValidationUtil.validateFillPercentage(0.0);
        ValidationUtil.validateFillPercentage(50.0);
        ValidationUtil.validateFillPercentage(100.0);
    }

    @Test(expected = ValidationException.class)
    public void testNegativeFillPercentageThrows() {
        ValidationUtil.validateFillPercentage(-1.0);
    }

    @Test(expected = ValidationException.class)
    public void testExcessiveFillPercentageThrows() {
        ValidationUtil.validateFillPercentage(100.1);
    }

    @Test
    public void testPositiveCapacityValidation() {
        ValidationUtil.validatePositiveCapacity(10.0);
        ValidationUtil.validatePositiveCapacity(1000.0);
    }

    @Test(expected = ValidationException.class)
    public void testZeroCapacityThrows() {
        ValidationUtil.validatePositiveCapacity(0.0);
    }

    @Test
    public void testEmailValidation() {
        ValidationUtil.validateEmail("admin@ecowaste.gov");
        ValidationUtil.validateEmail("worker.zone1@waste.org");
    }

    @Test(expected = ValidationException.class)
    public void testInvalidEmailThrows() {
        ValidationUtil.validateEmail("not-an-email");
    }

    @Test
    public void testParseDoubleOrDefault() {
        assertEquals(55.5, ValidationUtil.parseDoubleOrDefault("55.5", 0.0), 0.001);
        assertEquals(10.0, ValidationUtil.parseDoubleOrDefault("invalid", 10.0), 0.001);
        assertEquals(20.0, ValidationUtil.parseDoubleOrDefault("", 20.0), 0.001);
        assertEquals(30.0, ValidationUtil.parseDoubleOrDefault(null, 30.0), 0.001);
    }
}
