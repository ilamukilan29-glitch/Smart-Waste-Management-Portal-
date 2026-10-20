# Project Demonstration Script (Viva Presentation Walkthrough)

This step-by-step demonstration script walks evaluators and examiners through the core end-to-end workflows of the **Smart Waste Management and Collection Optimization System**.

---

## Preparation & Test Credentials
- **Application URL**: `http://localhost:8080/smart-waste-management/`
- **Administrator**: `admin` / `Admin@123`
- **Supervisor**: `supervisor` / `Super@123`
- **Collection Worker**: `worker1` / `Worker@123`

---

## Workflow 1: Administrator Login & Real-Time Operational Dashboard
1. Open the browser and navigate to `/login`.
2. Enter username `admin` and password `Admin@123`. Click **Sign In**.
3. **What to Explain to Examiners**:
   - The dashboard retrieves metrics live from the database via Java 8 `CompletableFuture` (concurrently fetching bins, tasks, workers, and vehicles).
   - Point out the **Critical Bins (&ge; 90%)** badge card and **Bins Requiring Collection (&ge; 80%)** counter.
   - Point out the dynamic **Chart.js** graphs: Bin Priority Breakdown (Doughnut) and Bins by Waste Type (Bar).

---

## Workflow 2: Bin Inventory & Sensor Fill Simulation
1. Click **Waste Bins** on the sidebar.
2. Filter by Area (e.g. *Central Commercial Zone*) or Waste Type (e.g. *Organic*).
3. Point out the animated **Fill Level Progress Bars** (red pulsing gradient for critical bins &ge; 90%).
4. Click **Fill Simulation** on the top right.
5. In the simulation page, explain:
   - *"In a live municipal deployment, fill levels originate from ultrasonic IoT sensors. For our academic demonstration, this module deterministically simulates waste generation."*
6. Select *Central Commercial Zone*, set Max Increment to `25%`, and click **Run Accumulation Step**.
7. Observe that fill levels increase, and notifications/critical alerts are automatically triggered for bins crossing 90%.

---

## Workflow 3: Collection Priority Engine
1. Click **Priority Queue** on the sidebar.
2. Select *Central Commercial Zone*.
3. **What to Explain to Examiners**:
   - Show how bins are categorized into **Critical (&ge;90%)**, **High (&ge;80%)**, and **Medium (&ge;60%)**.
   - Show that bins already assigned to active tours are flagged with **Task Scheduled** to prevent duplicate dispatches.
   - Explain that this queue is powered by our custom Java 8 `@FunctionalInterface BinPriorityRule` and Java 8 Stream filters.

---

## Workflow 4: Route Optimization (Nearest Neighbor Heuristic)
1. Click **Route Planner** on the sidebar.
2. Step 1: Select *Central Commercial Zone*.
3. Step 2: Select starting depot (*Central Municipal Garage*).
4. Step 3: Select available vehicle (*TN-01-WM-1001 Compactor Truck*).
5. Step 4: Click **Select All Eligible** or pick candidate bins.
6. Click **Generate Nearest Neighbor Route &rarr;**.
7. **What to Explain to Examiners**:
   - The system executes our pure Java 8 `RouteOptimizationService`.
   - Show the ordered stops table:
     - Stop 1: Base Depot Start ($0.0\text{ km}$).
     - Stop 2..$N$: Successive nearest unvisited bins based on road distances in `location_distances`.
     - Final Stop: Base Depot Return, completing the round trip.
   - Show total computed distance, estimated travel time (using $25\text{ km/h}$ average truck speed), and cumulative waste load against truck carrying capacity.

---

## Workflow 5: Task Assignment & Dispatch
1. On the route review page, under **Assign Operational Task to Worker**, select **Rajesh Kumar (WRK-001)**.
2. Click **Confirm & Dispatch Task**.
3. Observe success alert: *"Collection Task TASK-... scheduled and assigned successfully!"*
4. Point out that the vehicle availability is now marked **ON_ROUTE** and the worker is marked **ON_DUTY**.

---

## Workflow 6: Worker Mobile Field Portal Execution
1. Click the top-right profile icon and select **Sign Out**.
2. Sign in as the field worker: username `worker1`, password `Worker@123`.
3. The system automatically redirects the worker to `/worker/tasks`.
4. Locate the newly assigned task. Click **Start** (transitions task to `IN_PROGRESS`).
5. Click **Complete**.
6. In the modal, input measured collected weight (e.g. `210.5 kg`) and field remarks: *"All bins emptied and sanitized."*
7. Click **Submit & Finish Task**.

---

## Workflow 7: Multi-Table Transaction Verification
1. Sign back in as `admin`.
2. Go to **Waste Bins**: Observe that all bins visited on that completed tour have had their fill levels **reset back to 0.0%** and their `last_collection_at` updated.
3. Go to **Fleet / Vehicles**: Observe that truck `TN-01-WM-1001` is automatically released back to **AVAILABLE**.
4. Go to **Workers**: Observe worker `Rajesh Kumar` is released back to **AVAILABLE**.
5. Explain to examiners: *"All these updates were executed in a single atomic JDBC transaction (`conn.setAutoCommit(false)`), guaranteeing zero partial state corruptions."*

---

## Workflow 8: Operational Reports & CSV Export
1. Click **Operational Reports** on the sidebar.
2. Filter date range and select an area.
3. Observe summary KPIs: Total Collections, Completed Tasks, and Total Waste Collected.
4. Review the **Personnel Performance Table** showing assigned tasks, completed tasks, and completion rate.
5. Click **Export CSV**: Open the generated file in Excel to demonstrate RFC-4180 compliant CSV export.
6. Click **Print / Save PDF**: Show the clean, print-optimized document view.
