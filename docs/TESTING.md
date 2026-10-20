# Automated Testing and Verification Guide

This document details the automated unit and integration test suite implemented for the **Smart Waste Management and Collection Optimization System**.

---

## 1. Running the Automated Test Suite

To run all unit and integration tests from the command line:

```bash
mvn test
```

### Verified Test Execution Result:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.waste.management.service.PriorityEngineServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.126 s
[INFO] Running com.waste.management.service.RouteOptimizationServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s
[INFO] Running com.waste.management.util.ValidationUtilTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s
[INFO] 
[INFO] Results:
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 2. Test Suite Architecture and Coverage Matrix

| Test Class | Focus Area | Specific Test Cases Covered |
|---|---|---|
| [`PriorityEngineServiceTest`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/test/java/com/waste/management/service/PriorityEngineServiceTest.java) | Priority Classification & Functional Rules | - `testCriticalPriorityClassification` (fill $\ge 90\% \rightarrow$ CRITICAL)<br>- `testHighPriorityClassification` (fill $80-89.9\% \rightarrow$ HIGH)<br>- `testMediumPriorityClassification` (fill $60-79.9\% \rightarrow$ MEDIUM)<br>- `testLowPriorityClassification` (fill $< 60\% \rightarrow$ LOW)<br>- `testInactiveBinPriority` (inactive bins receive LOW priority)<br>- `testFunctionalInterfaceNegation` (Java 8 default method `.negateRule()`)<br>- `testPriorityGroupingWithStreams` (Java 8 `Collectors.groupingBy`) |
| [`RouteOptimizationServiceTest`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/test/java/com/waste/management/service/RouteOptimizationServiceTest.java) | Nearest Neighbor Algorithm & Capacity Limits | - `testNearestNeighborRouteSequenceAndDistance` (verifies exact stop order $D_0 \rightarrow B_1 \rightarrow B_2 \rightarrow B_3 \rightarrow D_0$ and distance calculation of $8.70\text{ km}$)<br>- `testVehicleCapacityConstraintEnforcement` (verifies truncation when truck capacity is reached)<br>- `testMissingDistanceHandling` (verifies that missing road matrix links throw `ValidationException`) |
| [`ValidationUtilTest`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/test/java/com/waste/management/util/ValidationUtilTest.java) | Server-Side Input Sanitization | - `testValidFillPercentage` (allows $0.0\%$, $50.0\%$, $100.0\%$)<br>- `testNegativeFillPercentageThrows` (rejects $< 0.0\%$)<br>- `testExcessiveFillPercentageThrows` (rejects $> 100.0\%$)<br>- `testPositiveCapacityValidation` (requires capacity $> 0$)<br>- `testZeroCapacityThrows`<br>- `testEmailValidation` (valid email regex)<br>- `testInvalidEmailThrows`<br>- `testParseDoubleOrDefault` (safe numeric parsing with fallbacks) |

---

## 3. Standalone Route Testing (Viva Defense)

A key academic requirement is that the **Route Optimization Service can be executed and tested independently without launching Apache Tomcat or configuring a live MySQL instance**.

This is demonstrated directly in [`RouteOptimizationServiceTest.java`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/test/java/com/waste/management/service/RouteOptimizationServiceTest.java):
1. In-memory symmetric distance matrix is injected via `routeOptimizer.setInMemoryDistances(...)`.
2. Mock Depot and Vehicle entities are passed directly into `executeNearestNeighbor(...)`.
3. The method returns `RoutePlanResultDTO` containing ordered stops, leg distances, and duration.
4. JUnit assertions verify mathematical correctness with zero external network or server dependencies.
