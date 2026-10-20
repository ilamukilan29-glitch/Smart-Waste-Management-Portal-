package com.waste.management.util;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

public class DatabaseSchemaTest {

    @Test
    public void testH2SchemaInitialization() throws Exception {
        Class.forName("org.h2.Driver");
        String h2Url = "jdbc:h2:mem:test_waste_db;MODE=MySQL;DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(h2Url, "sa", "");
             Statement stmt = conn.createStatement()) {

            // Disable foreign key checks
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            // Execute schema
            executeSqlFile(stmt, new File("database/schema.sql"));

            // Execute seed
            executeSqlFile(stmt, new File("database/seed.sql"));

            // Execute distance matrix
            executeSqlFile(stmt, new File("database/sample_distance_matrix.sql"));

            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");

            // Verify a table
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                assertTrue(rs.next());
                int count = rs.getInt(1);
                System.out.println("User count in H2: " + count);
                assertTrue(count >= 5);
            }
        }
    }

    private void executeSqlFile(Statement stmt, File file) throws Exception {
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file.getAbsolutePath());
        }
        String sql = new String(Files.readAllBytes(file.toPath()), "UTF-8");

        // Remove comments
        sql = sql.replaceAll("--.*(\r?\n)", "\n");

        // Split by semicolon
        String[] statements = sql.split(";");
        for (String rawStmt : statements) {
            String s = rawStmt.trim();
            if (s.isEmpty()) continue;
            if (s.toUpperCase().startsWith("CREATE DATABASE") ||
                s.toUpperCase().startsWith("USE ") ||
                s.toUpperCase().startsWith("SET FOREIGN_KEY_CHECKS")) {
                continue;
            }
            try {
                stmt.execute(s);
            } catch (Exception ex) {
                System.err.println("Failed statement: " + s);
                throw ex;
            }
        }
    }
}
