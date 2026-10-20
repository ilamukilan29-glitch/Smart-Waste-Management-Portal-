# Comprehensive Viva Questions & Answers Guide

This document prepares students for academic reviews and oral examinations covering Java 8, Servlets, JSP, JDBC, Database Design, MVC Architecture, and Route Optimization.

---

## Section 1: Java 8 Technologies

### Q1: What is a Functional Interface, and how did you use it in your project?
**Answer**:
A Functional Interface is an interface containing exactly one abstract method (SAM - Single Abstract Method). It can be marked with the `@FunctionalInterface` annotation and can be implemented using a lambda expression or method reference.
In our project, we implemented `com.waste.management.service.BinPriorityRule`:
```java
@FunctionalInterface
public interface BinPriorityRule {
    boolean matches(WasteBin bin, double threshold);
    default BinPriorityRule negateRule() {
        return (bin, threshold) -> !this.matches(bin, threshold);
    }
}
```
This allows priority rules to be expressed as clean lambdas: `(bin, threshold) -> bin.isActive() && bin.getCurrentFillPercentage() >= threshold`.

### Q2: What are the key advantages of the Stream API over traditional `for` loops?
**Answer**:
1. **Declarative Style**: Focuses on *what* to achieve rather than *how* to iterate.
2. **Pipelining and Laziness**: Intermediate operations (`filter()`, `map()`) are lazy and execute only when a terminal operation (`collect()`, `count()`) is invoked.
3. **Internal Iteration**: The JVM manages the iteration internals, facilitating optimization and potential parallelization (`parallelStream()`).
4. **Composability**: Allows chaining filtering, transformation, sorting, and reduction in a single readable pipeline.

### Q3: How does `CompletableFuture` work in your dashboard reporting?
**Answer**:
`CompletableFuture` provides non-blocking, asynchronous computation. In `ReportService.getDashboardMetrics()`, we execute independent queries for Bins, Tasks, Workers, and Fleet Vehicles concurrently on a managed thread pool:
```java
CompletableFuture<List<WasteBin>> binsFuture = CompletableFuture.supplyAsync(binDAO::findAll, EXECUTOR);
CompletableFuture<List<Worker>> workersFuture = CompletableFuture.supplyAsync(workerDAO::findAll, EXECUTOR);
CompletableFuture.allOf(binsFuture, workersFuture).join();
```
Instead of waiting sequentially for 4 separate SQL executions, they execute in parallel, reducing dashboard load latency.

### Q4: Why is `Optional<T>` preferred over returning `null`?
**Answer**:
Returning `null` often causes `NullPointerException` if the caller forgets to perform a null check. `Optional<T>` makes the potential absence of a value explicit in the method signature (e.g. `Optional<User> findByUsername(String username)`). It forces the caller to consciously handle both cases using methods like `.isPresent()`, `.map()`, or `.orElseThrow()`.

### Q5: Why did Java 8 introduce the new Date and Time API (JSR-310)?
**Answer**:
1. **Immutability and Thread Safety**: Classes like `LocalDate`, `LocalDateTime`, and `Duration` are immutable and thread-safe, unlike legacy `java.util.Date` and `Calendar` which were mutable.
2. **Clarity**: Separation between Date (`LocalDate`), Time (`LocalTime`), and combined DateTime (`LocalDateTime`).
3. **Fluent Arithmetic**: Built-in support for duration math: `Duration.between(startTime, endTime)`.

---

## Section 2: Servlets, JSP & Web Architecture

### Q6: Explain the MVC architecture implemented in this project.
**Answer**:
- **Model**: Domain entities (`WasteBin`, `CollectionTask`, `Vehicle`) and DTOs representing municipal data.
- **View**: JSP pages in `WEB-INF/views/` (e.g. `dashboard/index.jsp`, `bins/list.jsp`) responsible exclusively for HTML rendering using JSTL and EL. Zero SQL or business logic is placed in JSPs.
- **Controller**: Java Servlets (`DashboardServlet`, `BinServlet`, `TaskServlet`) that intercept incoming HTTP requests, invoke backend Service classes, and dispatch results to JSP views.

### Q7: What is the purpose of Filters in Java web applications, and which filters did you build?
**Answer**:
Filters intercept requests and responses before they reach servlets. We built 4 filters:
1. `CharacterEncodingFilter`: Enforces UTF-8 character encoding on all requests/responses.
2. `AuthenticationFilter`: Checks for active user session; redirects unauthenticated requests to `/login`.
3. `AuthorizationFilter`: Server-side role-based access control (RBAC); prevents unauthorized workers from accessing admin configuration pages.
4. `CsrfFilter`: Validates cryptographic CSRF tokens on mutating requests (`POST`, `PUT`, `DELETE`).

### Q8: How does session fixation protection work in your authentication flow?
**Answer**:
When an unauthenticated user arrives, they have an initial session. Upon successful login in `AuthServlet`, the existing session is invalidated immediately via `session.invalidate()`, and a brand new session is created with `request.getSession(true)`. This prevents attackers from hijacking an authenticated session using a known pre-login session ID.

---

## Section 3: JDBC & Database Design

### Q9: Why is `PreparedStatement` safer and faster than `Statement`?
**Answer**:
1. **Security (SQL Injection)**: `PreparedStatement` treats user input strictly as parameters/data, never as executable SQL tokens.
2. **Performance**: Pre-compiles SQL queries in the database. When executed repeatedly with different parameters, the database reuses the execution plan from cache.

### Q10: How did you implement transactional rollback during task completion?
**Answer**:
In `CollectionTaskDAO.completeTask()`, updating the task involves multiple related tables:
1. Updating task status to `COMPLETED`.
2. Updating collection plan status to `COMPLETED`.
3. Inserting audit entries in `collection_records`.
4. Resetting bin fill levels to `0.0%` in `waste_bins`.
5. Releasing worker and vehicle back to `AVAILABLE`.

We disable autocommit (`conn.setAutoCommit(false)`). If all steps succeed, `conn.commit()` commits the transaction. If any exception occurs, `conn.rollback()` restores the initial database state, preventing partial or corrupted updates.

### Q11: What is Connection Pooling, and why did you use HikariCP?
**Answer**:
Creating a new physical JDBC connection to MySQL requires a TCP handshake, authentication, and resource allocation (costing 50–200ms per request). A connection pool maintains a pool of pre-established, reusable connections. We used **HikariCP**, known as the fastest and most lightweight connection pool for Java 8.

---

## Section 4: Route Optimization & Nearest Neighbor Algorithm

### Q12: Explain how the Nearest Neighbor algorithm works in your Route Planner.
**Answer**:
1. The route starts at the designated municipal depot ($v_0$).
2. The algorithm searches all remaining unvisited eligible bins in the area.
3. For each candidate bin, it verifies that adding the bin will not violate the vehicle's capacity limit.
4. It selects the candidate bin with the lowest cost: $\text{Distance} - (\text{Priority Weight} \times \beta)$.
5. That bin is added to the route tour as the next stop, and cumulative distance and load are updated.
6. The process repeats until all candidate bins are visited or the truck is full.
7. Finally, a return leg is computed from the last bin back to the depot, completing the round trip.

### Q13: What is the time complexity of Nearest Neighbor, and how does it compare to an exact TSP solver?
**Answer**:
- Nearest Neighbor has a polynomial time complexity of $\mathcal{O}(n^2)$, where $n$ is the number of candidate bins. At step 1 it checks $n$ bins, at step 2 it checks $n-1$, and so on ($\frac{n(n-1)}{2} = \mathcal{O}(n^2)$).
- An exact TSP/CVRP solver requires exponential time $\mathcal{O}(2^n \cdot n^2)$ using Held-Karp dynamic programming or $\mathcal{O}(n!)$ brute force. For 50 bins, an exact solver would take billions of years, whereas Nearest Neighbor computes in under 2 milliseconds.

### Q14: Is Nearest Neighbor guaranteed to produce the optimal shortest route?
**Answer**:
No. Nearest Neighbor is a **greedy heuristic**. It makes locally optimal decisions at each stop, which can sometimes lead to longer final return legs (known as "greedy traps"). However, in real-world municipal logistics with capacity constraints and dynamic priority updates, it provides an excellent balance of computational speed and practical route quality.
