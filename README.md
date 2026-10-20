# Smart Waste Management and Collection Optimization System

> **Academic Capstone Project in Java 8 and Advanced Java Technologies**  
> Designed for B.Tech Artificial Intelligence and Data Science / Computer Science Engineering students.

[![Java 8](https://img.shields.io/badge/Java-8-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Build-Maven%203.9-blue.svg)](https://maven.apache.org/)
[![Tomcat 9](https://img.shields.io/badge/Server-Apache%20Tomcat%209-yellow.svg)](https://tomcat.apache.org/)
[![MySQL 8](https://img.shields.io/badge/Database-MySQL%208.x-blue.svg)](https://www.mysql.com/)
[![Tests](https://img.shields.io/badge/Tests-18%20Passed-brightgreen.svg)]()

---

## 1. Project Overview

The **Smart Waste Management and Collection Optimization System** is a production-grade, web-based municipal operations platform. It enables city administrators, supervisors, and field collection workers to:
- Monitor bin fill percentages and track overflow risks.
- Prioritize collections using dynamic **Java 8 Functional Rules**.
- Compute deterministic, capacity-constrained collection routes using the **Nearest Neighbor Heuristic** on a realistic road distance matrix.
- Dispatch collection tours to available workers and vehicles.
- Log actual collected waste quantities and update bin levels transactionally.
- Generate operational reports and export RFC-4180 compliant CSV audits.

> **Academic Note**: This system is a software simulation. Bin fill levels simulate ultrasonic IoT telemetry through a built-in simulation engine and manual updates. All dashboard metrics, route distances, and reports originate from normalized relational database records.

---

## 2. Mandatory Technology Stack

| Component | Technology | Version / Specification |
|---|---|---|
| **Language** | Java | Java 8 (`-source 1.8 -target 1.8`) |
| **Web Tier** | Java Servlets & JSP | Servlet API 4.0 (`javax.servlet.*`), JSTL 1.2 |
| **Persistence** | JDBC with PreparedStatement | MySQL Connector/J 8.0.33, HikariCP 3.4.5 Pool |
| **Application Server** | Apache Tomcat | Tomcat 9.0.x |
| **Database** | MySQL | MySQL 8.x (`waste_management_db`) |
| **Build Tool** | Apache Maven | Maven 3.6+ (WAR packaging) |
| **Security** | jBCrypt | BCrypt cost factor 12 |
| **Frontend** | HTML5, CSS3, JS, Bootstrap 5 | Bootstrap 5.3, Bootstrap Icons, Chart.js 4.4 |
| **JSON API** | Google Gson | Gson 2.10.1 |

---

## 3. Architecture & Package Structure

The system enforces strict **Model-View-Controller (MVC)** separation:

```
com.waste.management
├── model          # Domain entity models (WasteBin, Worker, Vehicle, CollectionPlan, etc.)
├── dao            # JDBC Data Access Objects with PreparedStatement & transactions
├── service        # Core business logic (PriorityEngine, RouteOptimization, TaskScheduling)
├── servlet        # HTTP controllers (DashboardServlet, BinServlet, RoutePlannerServlet)
├── filter         # Security, Authentication, Authorization, CSRF, and Encoding filters
├── util           # DBConnectionUtil, PasswordUtil, CsrfUtil, DateTimeUtil, CsvExportUtil
├── dto            # Data Transfer Objects (DashboardMetricsDTO, RoutePlanResultDTO)
├── exception      # Application-specific hierarchy (WasteManagementException, etc.)
└── listener       # AppContextListener for datasource and executor lifecycle management
```

---

## 4. Default Seed Credentials

The database seed script initializes three role accounts:

| Role | Username | Plain Password | Access Permissions |
|---|---|---|---|
| **Administrator** | `admin` | `Admin@123` | Full system access: Bins CRUD, Fleet, Workers, Routing, Simulation, Settings |
| **Supervisor** | `supervisor` | `Super@123` | Operational access: View dashboards, plans, tasks, reports, priority queue |
| **Collection Worker** | `worker1` | `Worker@123` | Worker portal: View assigned tasks, start collection, record weight and remarks |

---

## 5. Quick Start & Installation Guide

### Prerequisites
- Java Development Kit (JDK 8 or JDK 11/17/21 configured for 1.8 target)
- Apache Maven 3.6+
- MySQL Server 8.0+
- Apache Tomcat 9.0+

### Step 1: Database Initialization
Open a terminal in the project root and execute the SQL initialization scripts:

```bash
# 1. Create database schema and tables
mysql -u root -p < database/schema.sql

# 2. Seed initial users, areas, depots, workers, vehicles, and bins
mysql -u root -p < database/seed.sql

# 3. Populate sample road network distance matrix
mysql -u root -p < database/sample_distance_matrix.sql
```

### Step 2: Database Configuration
Verify or edit database credentials in [`src/main/resources/db.properties`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/main/resources/db.properties):
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.host=localhost
db.port=3306
db.name=waste_management_db
db.user=root
db.password=root
```
*(Alternatively, configure environment variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`)*.

### Step 3: Run Automated Unit Tests
Execute the independent test suite without launching Tomcat:
```bash
mvn test
```
*Expected Result*: All 18 automated tests pass with 0 failures.

### Step 4: Build and Package WAR
Package the application into a deployable Web Application Archive:
```bash
mvn clean package
```
The compiled WAR file is generated at:
```
target/smart-waste-management.war
```

### Step 5: Deploy to Apache Tomcat 9
1. Copy `target/smart-waste-management.war` to your Tomcat deployment directory:
   ```bash
   cp target/smart-waste-management.war $CATALINA_HOME/webapps/
   ```
2. Start Tomcat:
   - **Windows**: `$CATALINA_HOME\bin\startup.bat`
   - **Linux/Mac**: `$CATALINA_HOME/bin/startup.sh`
3. Access the application in your browser:
   ```
   http://localhost:8080/smart-waste-management/
   ```

---

## 6. Route Optimization Algorithm Overview

- **Algorithm**: Nearest Neighbor Heuristic for the Capacitated Vehicle Routing Problem (CVRP).
- **Time Complexity**: $\mathcal{O}(n^2)$ polynomial time.
- **Formulation**:
  1. Begins at the selected municipal Depot.
  2. Evaluates all unvisited candidate bins in the area.
  3. Verifies that candidate waste load fits in the vehicle's capacity ($L + q_i \le Q_{\max}$).
  4. Greedily selects the closest candidate bin with priority tie-breaking.
  5. Repeats until all bins are collected or the vehicle is full.
  6. Computes the return leg back to the Depot (completing the closed tour).
  7. Computes travel time using $25\text{ km/h}$ average urban speed + $5\text{ min}$ service time per stop.
- For complete math, complexity, and worked examples, see [`docs/ROUTE_ALGORITHM.md`](file:///d:/Hackathons/Java%208%20Capstone%20Project/docs/ROUTE_ALGORITHM.md).

---

## 7. Documentation Index

- [`JAVA8_FEATURE_MAPPING.md`](file:///d:/Hackathons/Java%208%20Capstone%20Project/JAVA8_FEATURE_MAPPING.md): Detailed inventory of all Java 8 features (Lambdas, Streams, Functional Interfaces, CompletableFuture, Optional, Date/Time).
- [`SECURITY.md`](file:///d:/Hackathons/Java%208%20Capstone%20Project/SECURITY.md): BCrypt hashing, RBAC authorization, CSRF protection, session fixation, and XSS defense.
- [`docs/ER_DIAGRAM.md`](file:///d:/Hackathons/Java%208%20Capstone%20Project/docs/ER_DIAGRAM.md): Mermaid relational schema diagram and 3NF normalization analysis.
- [`docs/ROUTE_ALGORITHM.md`](file:///d:/Hackathons/Java%208%20Capstone%20Project/docs/ROUTE_ALGORITHM.md): CVRP mathematical formulation and step-by-step nearest neighbor math.
- [`docs/TESTING.md`](file:///d:/Hackathons/Java%208%20Capstone%20Project/docs/TESTING.md): Test suite execution instructions and coverage matrix.
- [`docs/DEMO_SCRIPT.md`](file:///d:/Hackathons/Java%208%20Capstone%20Project/docs/DEMO_SCRIPT.md): Step-by-step viva presentation script walking through all 10 workflows.
- [`docs/VIVA_QA.md`](file:///d:/Hackathons/Java%208%20Capstone%20Project/docs/VIVA_QA.md): Oral examination questions and answers for viva defense.
