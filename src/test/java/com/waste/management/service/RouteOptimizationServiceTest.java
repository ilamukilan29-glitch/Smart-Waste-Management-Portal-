package com.waste.management.service;

import com.waste.management.dto.RoutePlanResultDTO;
import com.waste.management.dto.RouteStopDTO;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.Depot;
import com.waste.management.model.Vehicle;
import com.waste.management.model.WasteBin;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RouteOptimizationServiceTest {

    private RouteOptimizationService routeOptimizer;
    private Depot testDepot;
    private Vehicle testVehicle;
    private Map<String, Double> distanceMatrix;

    @Before
    public void setUp() {
        routeOptimizer = new RouteOptimizationService(null, null, null, null);

        // Setup test Depot
        testDepot = new Depot();
        testDepot.setId(1L);
        testDepot.setAreaId(1L);
        testDepot.setName("Test Base Depot");
        testDepot.setAddress("100 Municipal Way");

        // Setup test Vehicle (Capacity 2000 kg)
        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setRegistrationNumber("TN-01-TEST");
        testVehicle.setVehicleType("COMPACTOR_TRUCK");
        testVehicle.setCapacityKg(2000.0);
        testVehicle.setStatus("ACTIVE");
        testVehicle.setAvailabilityStatus("AVAILABLE");

        // Setup Known Symmetric Road Distance Matrix
        distanceMatrix = new HashMap<>();
        // Depot <-> Bins
        distanceMatrix.put("DEPOT_1-BIN_1", 1.0);
        distanceMatrix.put("DEPOT_1-BIN_2", 3.0);
        distanceMatrix.put("DEPOT_1-BIN_3", 5.0);
        // Inter-Bin Distances
        distanceMatrix.put("BIN_1-BIN_2", 1.5);
        distanceMatrix.put("BIN_1-BIN_3", 4.0);
        distanceMatrix.put("BIN_2-BIN_3", 1.2);

        routeOptimizer.setInMemoryDistances(distanceMatrix);
    }

    private WasteBin createTestBin(Long id, String code, double capacityKg, double fillPercentage) {
        WasteBin bin = new WasteBin();
        bin.setId(id);
        bin.setBinCode(code);
        bin.setLocationName("Location " + code);
        bin.setCapacityKg(capacityKg);
        bin.setCurrentFillPercentage(fillPercentage);
        bin.setStatus("ACTIVE");
        return bin;
    }

    @Test
    public void testNearestNeighborRouteSequenceAndDistance() {
        WasteBin b1 = createTestBin(1L, "BIN-1", 100.0, 80.0); // 80 kg
        WasteBin b2 = createTestBin(2L, "BIN-2", 100.0, 80.0); // 80 kg
        WasteBin b3 = createTestBin(3L, "BIN-3", 100.0, 80.0); // 80 kg

        List<WasteBin> candidates = Arrays.asList(b3, b1, b2); // Unordered input

        RoutePlanResultDTO result = routeOptimizer.executeNearestNeighbor(testDepot, testVehicle, candidates);

        assertNotNull(result);
        List<RouteStopDTO> stops = result.getStops();

        // Must have 5 stops: Depot Start -> Bin 1 -> Bin 2 -> Bin 3 -> Depot End
        assertEquals("Route must consist of 5 stops (Depot Start + 3 Bins + Depot End)", 5, stops.size());

        assertEquals("Stop 1 must be DEPOT_START", "DEPOT_START", stops.get(0).getStopType());
        assertEquals("Stop 2 must be nearest to Depot: BIN-1 (1.0 km)", (Long) 1L, stops.get(1).getWasteBinId());
        assertEquals("Stop 3 must be nearest to BIN-1: BIN-2 (1.5 km)", (Long) 2L, stops.get(2).getWasteBinId());
        assertEquals("Stop 4 must be nearest to BIN-2: BIN-3 (1.2 km)", (Long) 3L, stops.get(3).getWasteBinId());
        assertEquals("Stop 5 must be DEPOT_END (return leg 5.0 km)", "DEPOT_END", stops.get(4).getStopType());

        // Expected distance: 1.0 (Depot->B1) + 1.5 (B1->B2) + 1.2 (B2->B3) + 5.0 (B3->Depot) = 8.7 km
        assertEquals("Total distance must match exact matrix sum of 8.70 km", 8.70, result.getTotalDistanceKm(), 0.01);
        assertEquals("Total collected waste must be 240.0 kg", 240.0, result.getTotalWasteEstimatedKg(), 0.01);
        assertFalse("Vehicle capacity should not be exceeded", result.isCapacityExceeded());
    }

    @Test
    public void testVehicleCapacityConstraintEnforcement() {
        // Vehicle with small capacity of 100 kg
        Vehicle smallVehicle = new Vehicle();
        smallVehicle.setId(2L);
        smallVehicle.setRegistrationNumber("MINI-01");
        smallVehicle.setCapacityKg(100.0); // Only 100 kg limit
        smallVehicle.setStatus("ACTIVE");
        smallVehicle.setAvailabilityStatus("AVAILABLE");

        WasteBin b1 = createTestBin(1L, "BIN-1", 100.0, 80.0); // 80 kg
        WasteBin b2 = createTestBin(2L, "BIN-2", 100.0, 80.0); // 80 kg (would total 160 kg > 100 kg)

        RoutePlanResultDTO result = routeOptimizer.executeNearestNeighbor(testDepot, smallVehicle, Arrays.asList(b1, b2));

        // Should only visit B1 and then return to depot, leaving B2 unvisited
        assertEquals("Should visit Depot Start + BIN-1 + Depot End (3 stops)", 3, result.getStops().size());
        assertEquals((Long) 1L, result.getStops().get(1).getWasteBinId());
        assertTrue("Capacity exceeded flag should be true", result.isCapacityExceeded());
        assertTrue("Unvisited bins must contain BIN-2", result.getUnvisitedBinIds().contains(2L));
    }

    @Test(expected = ValidationException.class)
    public void testMissingDistanceHandling() {
        // Query distance for an unconfigured pair (BIN 99)
        routeOptimizer.lookupDistance("DEPOT", 1L, "BIN", 99L);
    }
}
