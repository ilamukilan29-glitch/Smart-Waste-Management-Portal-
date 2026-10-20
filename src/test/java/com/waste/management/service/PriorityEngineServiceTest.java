package com.waste.management.service;

import com.waste.management.dao.WasteBinDAO;
import com.waste.management.dto.BinPriorityDTO;
import com.waste.management.model.PriorityLevel;
import com.waste.management.model.WasteBin;
import com.waste.management.model.WasteType;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class PriorityEngineServiceTest {

    private PriorityEngineService priorityEngine;

    @Before
    public void setUp() {
        // Test priority engine independently with stubbed DAO
        priorityEngine = new PriorityEngineService();
    }

    @Test
    public void testCriticalPriorityClassification() {
        WasteBin bin = new WasteBin();
        bin.setStatus("ACTIVE");
        bin.setCurrentFillPercentage(92.5);

        PriorityLevel priority = priorityEngine.determinePriority(bin);
        assertEquals("Fill >= 90% must be CRITICAL", PriorityLevel.CRITICAL, priority);
    }

    @Test
    public void testHighPriorityClassification() {
        WasteBin bin = new WasteBin();
        bin.setStatus("ACTIVE");
        bin.setCurrentFillPercentage(84.0);

        PriorityLevel priority = priorityEngine.determinePriority(bin);
        assertEquals("Fill 80-89.9% must be HIGH", PriorityLevel.HIGH, priority);
    }

    @Test
    public void testMediumPriorityClassification() {
        WasteBin bin = new WasteBin();
        bin.setStatus("ACTIVE");
        bin.setCurrentFillPercentage(65.0);

        PriorityLevel priority = priorityEngine.determinePriority(bin);
        assertEquals("Fill 60-79.9% must be MEDIUM", PriorityLevel.MEDIUM, priority);
    }

    @Test
    public void testLowPriorityClassification() {
        WasteBin bin = new WasteBin();
        bin.setStatus("ACTIVE");
        bin.setCurrentFillPercentage(45.0);

        PriorityLevel priority = priorityEngine.determinePriority(bin);
        assertEquals("Fill < 60% must be LOW", PriorityLevel.LOW, priority);
    }

    @Test
    public void testInactiveBinPriority() {
        WasteBin bin = new WasteBin();
        bin.setStatus("INACTIVE");
        bin.setCurrentFillPercentage(98.0);

        PriorityLevel priority = priorityEngine.determinePriority(bin);
        assertEquals("Inactive bins must not receive collection priority", PriorityLevel.LOW, priority);
    }

    @Test
    public void testFunctionalInterfaceNegation() {
        BinPriorityRule rule = (b, threshold) -> b.getCurrentFillPercentage() >= threshold;
        BinPriorityRule inverted = rule.negateRule();

        WasteBin bin = new WasteBin();
        bin.setCurrentFillPercentage(95.0);

        assertTrue(rule.matches(bin, 90.0));
        assertFalse(inverted.matches(bin, 90.0));
    }

    @Test
    public void testPriorityGroupingWithStreams() {
        List<BinPriorityDTO> dtoList = new ArrayList<>();

        BinPriorityDTO b1 = new BinPriorityDTO();
        b1.setBinId(1L);
        b1.setPriorityLevel(PriorityLevel.CRITICAL);
        dtoList.add(b1);

        BinPriorityDTO b2 = new BinPriorityDTO();
        b2.setBinId(2L);
        b2.setPriorityLevel(PriorityLevel.CRITICAL);
        dtoList.add(b2);

        BinPriorityDTO b3 = new BinPriorityDTO();
        b3.setBinId(3L);
        b3.setPriorityLevel(PriorityLevel.HIGH);
        dtoList.add(b3);

        Map<PriorityLevel, List<BinPriorityDTO>> grouped = priorityEngine.groupBinsByPriority(dtoList);
        assertEquals(2, grouped.get(PriorityLevel.CRITICAL).size());
        assertEquals(1, grouped.get(PriorityLevel.HIGH).size());
        assertNull(grouped.get(PriorityLevel.LOW));
    }
}
