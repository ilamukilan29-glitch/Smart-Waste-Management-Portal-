package com.waste.management.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password hashing and verification utility.
 * Demonstrates secure credential handling in Java 8.
 */
public class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    /**
     * Hashes a plain-text password using BCrypt with a secure salt.
     *
     * @param plainPassword plain-text password
     * @return salted BCrypt hash string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies that a plain-text password matches a stored BCrypt hash.
     *
     * @param plainPassword candidate plain-text password
     * @param hashedPassword stored hashed password
     * @return true if password matches, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        String adminHash = hashPassword("Admin@123");
        String superHash = hashPassword("Super@123");
        String workerHash = hashPassword("Worker@123");

        System.out.println("ADMIN_HASH=" + adminHash);
        System.out.println("SUPER_HASH=" + superHash);
        System.out.println("WORKER_HASH=" + workerHash);
    }
}
