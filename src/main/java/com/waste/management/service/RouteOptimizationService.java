package com.waste.management.service;

import com.waste.management.dao.DepotDAO;
import com.waste.management.dao.LocationDistanceDAO;
import com.waste.management.dao.VehicleDAO;
import com.waste.management.dao.WasteBinDAO;
import com.waste.management.dto.RoutePlanResultDTO;
import com.waste.management.dto.RouteStopDTO;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.Depot;
import com.waste.management.model.Vehicle;
import com.waste.management.model.WasteBin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Route Optimization Service implementing the Nearest Neighbor Heuristic
 * with Vehicle Capacity Constraints and Priority-Aware Candidate Selection.
 *
 * Algorithm Workflow:
 * 1. Initialize route at Depot (Stop 0).
 * 2. Track unvisited eligible candidate bins, cumulative vehicle load, and cumulative distance.
 * 3. At each step, search remaining unvisited bins for the candidate that:
 *    a) Does not exceed remaining vehicle capacity.
 *    b) Has the minimum cost = distance_km - (priorityWeight * priorityBiasFactor).
 * 4. Add the chosen bin as the next route stop. Update current location, load, and distance.
 * 5. Repeat until no more candidate bins can fit or all eligible bins are visited.
 * 6. Return from the last visited bin back to the Depot (Stop N+1, completing round trip).
 * 7. Calculate total distance and estimated travel time using average truck speed.
 */
public class RouteOptimizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteOptimizationService.class);

    // Operational parameters
    private double averageTruckSpeedKmh = 25.0; // Typical urban municipal truck speed (km/h)
    private int serviceTimePerBinMinutes = 5;    // Time spent servicing/emptying each bin
    private double priorityBiasFactor = 0.5;    // Km equivalent discount for higher priority bins

    private final LocationDistanceDAO distanceDAO;
    private final DepotDAO depotDAO;
    private final WasteBinDAO binDAO;
    private final VehicleDAO vehicleDAO;

    // Optional in-memory distance matrix for standalone testing without database
    private Map<String, Double> inMemoryDistances = null;

    public RouteOptimizationService() {
        this.distanceDAO = new LocationDistanceDAO();
        this.depotDAO = new DepotDAO();
        this.binDAO = new WasteBinDAO();
        this.vehicleDAO = new VehicleDAO();
    }

    /**
     * Constructor for unit testing without database dependencies.
     */
    public RouteOptimizationService(LocationDistanceDAO distanceDAO, DepotDAO depotDAO, WasteBinDAO binDAO, VehicleDAO vehicleDAO) {
        this.distanceDAO = distanceDAO;
        this.depotDAO = depotDAO;
        this.binDAO = binDAO;
        this.vehicleDAO = vehicleDAO;
    }

    public void setInMemoryDistances(Map<String, Double> distances) {
        this.inMemoryDistances = distances;
    }

    /**
     * Generates an optimized collection route for the specified depot, vehicle, and candidate bins.
     *
     * @param depotId depot ID where route begins and ends
     * @param vehicleId vehicle ID assigned to the route
     * @param candidateBins list of waste bins to consider for collection
     * @return RoutePlanResultDTO with ordered stops, metrics, and capacity tracking
     */
    public RoutePlanResultDTO generateRoutePlan(Long depotId, Long vehicleId, List<WasteBin> candidateBins) {
        if (candidateBins == null || candidateBins.isEmpty()) {
            throw new ValidationException("Cannot generate route plan: No candidate bins provided.");
        }

        Depot depot = depotDAO.findById(depotId)
                .orElseThrow(() -> new ValidationException("Depot with ID " + depotId + " not found."));

        Vehicle vehicle = vehicleDAO.findById(vehicleId)
                .orElseThrow(() -> new ValidationException("Vehicle with ID " + vehicleId + " not found."));

        if (!vehicle.isAvailable()) {
            throw new ValidationException("Selected vehicle " + vehicle.getRegistrationNumber() + " is currently unavailable.");
        }

        return executeNearestNeighbor(depot, vehicle, candidateBins);
    }

    /**
     * Pure algorithmic execution of Nearest Neighbor.
     * Can be invoked directly in unit tests without touching database lookups.
     */
    public RoutePlanResultDTO executeNearestNeighbor(Depot depot, Vehicle vehicle, List<WasteBin> candidateBins) {
        RoutePlanResultDTO result = new RoutePlanResultDTO();
        result.setAreaId(depot.getAreaId());
        result.setAreaName(depot.getAreaName());
        result.setDepotId(depot.getId());
        result.setDepotName(depot.getName());
        result.setVehicleId(vehicle.getId());
        result.setVehicleRegistration(vehicle.getRegistrationNumber());
        result.setVehicleCapacityKg(vehicle.getCapacityKg());

        List<RouteStopDTO> stops = new ArrayList<>();
        List<WasteBin> unvisited = new ArrayList<>(candidateBins);
        List<Long> unvisitedBinIds = new ArrayList<>();

        double cumulativeDistance = 0.0;
        double cumulativeLoad = 0.0;
        int stopOrder = 1;

        // Stop 1: Start at Depot
        RouteStopDTO depotStart = new RouteStopDTO();
        depotStart.setStopOrder(stopOrder++);
        depotStart.setStopType("DEPOT_START");
        depotStart.setLocationLabel("Depot Start: " + depot.getName());
        depotStart.setAddress(depot.getAddress());
        depotStart.setLatitude(depot.getLatitude());
        depotStart.setLongitude(depot.getLongitude());
        depotStart.setDistanceFromPrevKm(0.0);
        depotStart.setCumulativeDistanceKm(0.0);
        depotStart.setCumulativeLoadKg(0.0);
        depotStart.setEstimatedLegMinutes(0);
        stops.add(depotStart);

        String currentType = "DEPOT";
        Long currentId = depot.getId();

        // Greedy Nearest Neighbor Loop
        while (!unvisited.isEmpty()) {
            WasteBin bestCandidate = null;
            double bestCost = Double.MAX_VALUE;
            double bestDistance = 0.0;

            for (WasteBin candidate : unvisited) {
                double estimatedBinWaste = candidate.getEstimatedWasteKg();

                // Check capacity constraint
                if (cumulativeLoad + estimatedBinWaste > vehicle.getCapacityKg()) {
                    continue; // Skip bin if it would exceed vehicle capacity
                }

                // Lookup distance from current location to candidate bin
                double dist = lookupDistance(currentType, currentId, "BIN", candidate.getId());

                // Priority tie-breaker: bias cost by priority weight
                int priorityWeight = candidate.getPriorityLevel().getWeight();
                double cost = dist - (priorityWeight * priorityBiasFactor);

                if (cost < bestCost) {
                    bestCost = cost;
                    bestDistance = dist;
                    bestCandidate = candidate;
                }
            }

            // If no candidate fits within capacity, stop collection loop
            if (bestCandidate == null) {
                break;
            }

            // Visit best candidate
            unvisited.remove(bestCandidate);
            cumulativeDistance += bestDistance;
            double wasteKg = bestCandidate.getEstimatedWasteKg();
            cumulativeLoad += wasteKg;

            int legMinutes = calculateLegMinutes(bestDistance) + serviceTimePerBinMinutes;

            RouteStopDTO binStop = new RouteStopDTO();
            binStop.setStopOrder(stopOrder++);
            binStop.setStopType("BIN");
            binStop.setWasteBinId(bestCandidate.getId());
            binStop.setBinCode(bestCandidate.getBinCode());
            binStop.setLocationLabel("Bin: " + bestCandidate.getBinCode() + " (" + bestCandidate.getLocationName() + ")");
            binStop.setAddress(bestCandidate.getAddress());
            binStop.setLatitude(bestCandidate.getLatitude());
            binStop.setLongitude(bestCandidate.getLongitude());
            binStop.setFillPercentage(bestCandidate.getCurrentFillPercentage());
            binStop.setPriorityLevel(bestCandidate.getPriorityLevel());
            binStop.setExpectedWasteKg(wasteKg);
            binStop.setCumulativeLoadKg(cumulativeLoad);
            binStop.setDistanceFromPrevKm(bestDistance);
            binStop.setCumulativeDistanceKm(cumulativeDistance);
            binStop.setEstimatedLegMinutes(legMinutes);
            stops.add(binStop);

            // Advance current position
            currentType = "BIN";
            currentId = bestCandidate.getId();
        }

        // Remaining unvisited bins (due to vehicle capacity)
        for (WasteBin remaining : unvisited) {
            unvisitedBinIds.add(remaining.getId());
        }
        result.setUnvisitedBinIds(unvisitedBinIds);
        result.setCapacityExceeded(!unvisited.isEmpty());

        // Final Leg: Return from last stop to Depot (Completing round trip)
        double returnDistance = lookupDistance(currentType, currentId, "DEPOT", depot.getId());
        cumulativeDistance += returnDistance;
        int returnLegMinutes = calculateLegMinutes(returnDistance);

        RouteStopDTO depotEnd = new RouteStopDTO();
        depotEnd.setStopOrder(stopOrder);
        depotEnd.setStopType("DEPOT_END");
        depotEnd.setLocationLabel("Depot Return: " + depot.getName());
        depotEnd.setAddress(depot.getAddress());
        depotEnd.setLatitude(depot.getLatitude());
        depotEnd.setLongitude(depot.getLongitude());
        depotEnd.setDistanceFromPrevKm(returnDistance);
        depotEnd.setCumulativeDistanceKm(cumulativeDistance);
        depotEnd.setCumulativeLoadKg(cumulativeLoad);
        depotEnd.setEstimatedLegMinutes(returnLegMinutes);
        stops.add(depotEnd);

        // Compute total duration in minutes
        int totalMinutes = stops.stream().mapToInt(RouteStopDTO::getEstimatedLegMinutes).sum();

        result.setStops(stops);
        result.setTotalDistanceKm(Math.round(cumulativeDistance * 100.0) / 100.0);
        result.setTotalDurationMinutes(totalMinutes);
        result.setTotalWasteEstimatedKg(Math.round(cumulativeLoad * 100.0) / 100.0);

        return result;
    }

    /**
     * Looks up distance between two entities.
     * Checks in-memory test map first if set, then queries LocationDistanceDAO.
     * Throws ValidationException if distance is missing (never invents distances).
     */
    public double lookupDistance(String fromType, Long fromId, String toType, Long toId) {
        if (fromType.equalsIgnoreCase(toType) && fromId.equals(toId)) {
            return 0.0;
        }

        if (inMemoryDistances != null) {
            String key1 = fromType + "_" + fromId + "-" + toType + "_" + toId;
            String key2 = toType + "_" + toId + "-" + fromType + "_" + fromId;
            if (inMemoryDistances.containsKey(key1)) {
                return inMemoryDistances.get(key1);
            }
            if (inMemoryDistances.containsKey(key2)) {
                return inMemoryDistances.get(key2);
            }
        }

        if (distanceDAO != null) {
            Optional<Double> dist = distanceDAO.getDistance(fromType, fromId, toType, toId);
            if (dist.isPresent()) {
                return dist.get();
            }
        }

        throw new ValidationException(String.format(
                "Missing road distance configuration in database table 'location_distances' between %s ID %d and %s ID %d. Cannot compute deterministic route.",
                fromType, fromId, toType, toId));
    }

    private int calculateLegMinutes(double distanceKm) {
        if (distanceKm <= 0.0) return 0;
        double travelHours = distanceKm / averageTruckSpeedKmh;
        return (int) Math.ceil(travelHours * 60.0);
    }

    // Parameter getters and setters
    public double getAverageTruckSpeedKmh() {
        return averageTruckSpeedKmh;
    }

    public void setAverageTruckSpeedKmh(double averageTruckSpeedKmh) {
        this.averageTruckSpeedKmh = averageTruckSpeedKmh;
    }

    public int getServiceTimePerBinMinutes() {
        return serviceTimePerBinMinutes;
    }

    public void setServiceTimePerBinMinutes(int serviceTimePerBinMinutes) {
        this.serviceTimePerBinMinutes = serviceTimePerBinMinutes;
    }
}
