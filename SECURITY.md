# Security Architecture and Controls Guide

This document details the security controls implemented in the **Smart Waste Management and Collection Optimization System**, as well as guidelines for production hardening.

---

## 1. Password Hashing (BCrypt)
- **Library**: `org.mindrot:jbcrypt:0.4`
- **Algorithm**: Blowfish-based cryptographic hashing with cost factor $12$ ($2^{12} = 4096$ iterations).
- **Salt Generation**: Automatically generates a cryptographically strong, random 128-bit salt per user.
- **Resistance**: Immune to precomputed rainbow table attacks and GPU brute-force attacks due to high memory and compute cost.
- **Implementation**: See [`PasswordUtil.java`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/main/java/com/waste/management/util/PasswordUtil.java).

---

## 2. Server-Side Role-Based Access Control (RBAC)
- **Design Principle**: Authorization must never rely solely on hiding buttons in the UI.
- **Filter Enforced**: [`AuthorizationFilter.java`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/main/java/com/waste/management/filter/AuthorizationFilter.java) intercepts every request.
- **Roles**:
  - `ADMIN`: Full access to bin creation/deletion, workforce management, fleet management, simulation, and threshold settings.
  - `SUPERVISOR`: Read-only access to operational dashboards, route plans, priority queues, and audit reports.
  - `WORKER`: Strictly restricted to viewing assigned field tasks and submitting collected weight reports. Forbidden from accessing administrative and configuration endpoints.
- **Failure Response**: Returns `403 Forbidden` (`WEB-INF/views/error/403.jsp`).

---

## 3. SQL Injection Prevention
- **Mechanism**: Every dynamic database operation is executed exclusively through `java.sql.PreparedStatement`.
- **Implementation**: Zero string concatenation in SQL queries (`"SELECT ... WHERE id = " + id` is strictly banned). Parameters are passed using type-safe setters (`stmt.setString()`, `stmt.setLong()`, `stmt.setDouble()`).
- **Database User**: Prepared statements use parameterized binary protocols in MySQL Connector/J.

---

## 4. Cross-Site Request Forgery (CSRF) Defense
- **Implementation**: [`CsrfFilter.java`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/main/java/com/waste/management/filter/CsrfFilter.java) and [`CsrfUtil.java`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/main/java/com/waste/management/util/CsrfUtil.java).
- **Token Characteristics**: Cryptographically random 256-bit token generated via `SecureRandom` and bound to the user's active session.
- **Enforcement**: Validated on every state-changing HTTP request (`POST`, `PUT`, `DELETE`). Requests with missing or mismatched tokens are rejected with `400 Bad Request`.

---

## 5. Session Fixation and Session Management
- **Fixation Protection**: When a user successfully authenticates in [`AuthServlet.java`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/main/java/com/waste/management/servlet/AuthServlet.java), the pre-authentication session is destroyed immediately via `session.invalidate()`, and a fresh session with a new `JSESSIONID` is created.
- **Timeout**: Set to 30 minutes in `web.xml`.
- **Cookie Security**: Configured with `<http-only>true</http-only>` to mitigate session hijacking via XSS.

---

## 6. Cross-Site Scripting (XSS) Prevention
- **Output Escaping**: All dynamic user-supplied data in JSP views is rendered using JSTL `<c:out value="..."/>`, which automatically HTML-escapes special characters (`<`, `>`, `&`, `"`, `'`).
- **Input Sanitization**: Strings are validated and trimmed on arrival in [`ValidationUtil.java`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/main/java/com/waste/management/util/ValidationUtil.java).

---

## 7. Error Handling & Information Disclosure
- **Global Error Handling**: Custom error pages configured in `web.xml` for `403`, `404`, and `500`.
- **Stack Trace Suppression**: Raw exception stack traces and database internal error messages are logged to server diagnostics via SLF4J, but never exposed to end-user browsers.

---

## 8. Multi-Table Transaction Integrity
- **Transactional Rollback**: Multi-table operations (such as completing a collection task which simultaneously updates tasks, route stops, collection records, bin fill levels, and worker/vehicle availability) use explicit database transactions (`conn.setAutoCommit(false)`).
- **Rollback Guarantee**: In case of any mid-transaction failure, `conn.rollback()` executes, preventing orphan or corrupted partial updates.

---

## 9. Production Deployment Hardening Checklist
Before deploying to public municipal networks:
1. **SSL/TLS (HTTPS)**: Enable TLSv1.3 with valid certificates on Apache Tomcat or reverse proxy (Nginx). Set `<secure>true</secure>` in `web.xml` cookie configuration.
2. **Environment Credentials**: Set database passwords via system environment variables (`DB_PASSWORD`) rather than plaintext properties files.
3. **Database Principle of Least Privilege**: Create a dedicated MySQL user `waste_app` with `SELECT`, `INSERT`, `UPDATE`, `DELETE` grants only on `waste_management_db`.
4. **WAF & Rate Limiting**: Deploy rate limiting on `/login` to mitigate credential stuffing.
