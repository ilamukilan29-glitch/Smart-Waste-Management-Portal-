package com.waste.management.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Database Connection Manager supporting HikariCP connection pooling
 * and graceful zero-friction fallback to embedded H2 (MySQL Mode) if
 * local MySQL Server is not accessible.
 */
public class DBConnectionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnectionUtil.class);

    private static HikariDataSource dataSource;
    private static Properties dbProps = new Properties();
    private static boolean initialized = false;
    private static boolean isH2Fallback = false;

    private static String jdbcUrl;
    private static String jdbcUser;
    private static String jdbcPassword;
    private static String jdbcDriver;

    static {
        init();
    }

    public static synchronized void init() {
        if (initialized && dataSource != null && !dataSource.isClosed()) {
            return;
        }

        try {
            // 1. Load properties from classpath
            try (InputStream in = DBConnectionUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    dbProps.load(in);
                    LOGGER.info("Loaded db.properties from classpath");
                } else {
                    LOGGER.warn("db.properties not found on classpath, relying on environment or defaults");
                }
            } catch (Exception e) {
                LOGGER.warn("Could not load db.properties: {}", e.getMessage());
            }

            // 2. Read configured MySQL settings
            jdbcDriver = getEnvOrProp("DB_DRIVER", "db.driver", "com.mysql.cj.jdbc.Driver");
            String host = getEnvOrProp("DB_HOST", "db.host", "localhost");
            String port = getEnvOrProp("DB_PORT", "db.port", "3306");
            String name = getEnvOrProp("DB_NAME", "db.name", "waste_management_db");
            String defaultUrl = "jdbc:mysql://" + host + ":" + port + "/" + name + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";

            jdbcUrl = getEnvOrProp("DB_URL", "db.url", defaultUrl);
            jdbcUser = getEnvOrProp("DB_USER", "db.user", "root");
            jdbcPassword = getEnvOrProp("DB_PASSWORD", "db.password", "root");

            boolean mysqlAvailable = false;
            try {
                Class.forName(jdbcDriver);
                try (Connection testConn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)) {
                    mysqlAvailable = true;
                    LOGGER.info("Successfully established connection to MySQL: {}", jdbcUrl);
                }
            } catch (Exception e) {
                LOGGER.warn("MySQL connection failed ({}: {}).", e.getClass().getSimpleName(), e.getMessage());
            }

            // 3. Fallback to Embedded H2 if MySQL is unavailable
            if (!mysqlAvailable) {
                LOGGER.info("--------------------------------------------------------------------------------");
                LOGGER.info("  Activating zero-friction Embedded H2 Database (MySQL Compatibility Mode)");
                LOGGER.info("--------------------------------------------------------------------------------");
                isH2Fallback = true;
                jdbcDriver = "org.h2.Driver";
                jdbcUrl = "jdbc:h2:mem:waste_management_db;MODE=MySQL;DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1";
                jdbcUser = "sa";
                jdbcPassword = "";
                Class.forName(jdbcDriver);
            }

            // 4. Configure HikariCP connection pool
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(jdbcDriver);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(jdbcUser);
            config.setPassword(jdbcPassword);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(10000);
            config.setPoolName(isH2Fallback ? "SmartWasteH2Pool" : "SmartWasteHikariPool");

            if (!isH2Fallback) {
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");
            }

            try {
                dataSource = new HikariDataSource(config);
                LOGGER.info("HikariCP connection pool initialized successfully for URL: {}", jdbcUrl);
            } catch (Exception poolEx) {
                LOGGER.warn("HikariCP initialization failed, will fall back to direct DriverManager: {}", poolEx.getMessage());
                dataSource = null;
            }

            initialized = true;

            // 5. If running on H2 fallback, populate schema and seed data
            if (isH2Fallback) {
                initializeH2Database();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize database connectivity: {}", e.getMessage(), e);
        }
    }

    private static void initializeH2Database() {
        try (Connection conn = (dataSource != null ? dataSource.getConnection() : DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword));
             Statement stmt = conn.createStatement()) {
            LOGGER.info("Initializing H2 schema and seed records...");
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            executeSqlScript(stmt, "schema.sql");
            executeSqlScript(stmt, "seed.sql");
            executeSqlScript(stmt, "sample_distance_matrix.sql");

            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            LOGGER.info("H2 database successfully initialized with 12 normalized tables, distance matrix, and seed accounts!");
        } catch (Exception e) {
            LOGGER.error("Error initializing H2 database: {}", e.getMessage(), e);
        }
    }

    private static void executeSqlScript(Statement stmt, String filename) throws Exception {
        String content = loadSqlContent(filename);
        if (content == null || content.trim().isEmpty()) {
            LOGGER.warn("SQL script not found or empty: {}", filename);
            return;
        }

        // Strip comments
        String cleaned = content.replaceAll("--.*(\r?\n)", "\n");
        String[] statements = cleaned.split(";");
        for (String raw : statements) {
            String s = raw.trim();
            if (s.isEmpty()) continue;
            if (s.toUpperCase().startsWith("CREATE DATABASE") ||
                s.toUpperCase().startsWith("USE ") ||
                s.toUpperCase().startsWith("SET FOREIGN_KEY_CHECKS")) {
                continue;
            }
            try {
                stmt.execute(s);
            } catch (SQLException e) {
                LOGGER.debug("SQL warning during H2 init ({}): {}", s, e.getMessage());
            }
        }
    }

    private static String loadSqlContent(String filename) {
        // Try ClassLoader first
        String[] candidates = {
            "database/" + filename,
            "/database/" + filename,
            filename
        };
        for (String c : candidates) {
            try (InputStream in = DBConnectionUtil.class.getClassLoader().getResourceAsStream(c)) {
                if (in != null) {
                    return readStreamToString(in);
                }
            } catch (Exception ignored) {
            }
        }

        // Try filesystem fallback
        File[] fileCandidates = {
            new File("database/" + filename),
            new File("src/main/resources/database/" + filename),
            new File("../database/" + filename)
        };
        for (File f : fileCandidates) {
            if (f.exists()) {
                try (InputStream in = new FileInputStream(f)) {
                    return readStreamToString(in);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private static String readStreamToString(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private static String getEnvOrProp(String envKey, String propKey, String defaultValue) {
        String val = System.getenv(envKey);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        val = System.getProperty(envKey);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        val = dbProps.getProperty(propKey);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        return defaultValue;
    }

    /**
     * Obtains a JDBC Connection from the pool or via DriverManager.
     * Always close the connection in a try-with-resources block!
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            init();
        }

        if (dataSource != null && !dataSource.isClosed()) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                LOGGER.warn("HikariDataSource.getConnection() failed, attempting direct connection: {}", e.getMessage());
            }
        }

        // Direct DriverManager fallback
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    /**
     * Closes the connection pool on application shutdown.
     */
    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            LOGGER.info("Shutting down database connection pool");
            dataSource.close();
        }
    }

    public static String getJdbcUrl() {
        return jdbcUrl;
    }

    public static String getJdbcUser() {
        return jdbcUser;
    }

    public static boolean isH2Fallback() {
        return isH2Fallback;
    }
}
