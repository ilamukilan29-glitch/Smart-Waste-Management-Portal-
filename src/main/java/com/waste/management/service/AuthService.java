package com.waste.management.service;

import com.waste.management.dao.UserDAO;
import com.waste.management.exception.AuthenticationException;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.Role;
import com.waste.management.model.User;
import com.waste.management.util.PasswordUtil;
import com.waste.management.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO;

    public AuthService() {
        this(new UserDAO());
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates user with username and plain password using BCrypt.
     */
    public User login(String username, String plainPassword) {
        ValidationUtil.requireNonEmpty(username, "Username");
        ValidationUtil.requireNonEmpty(plainPassword, "Password");

        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (!userOpt.isPresent()) {
            LOGGER.warn("Login failed: User '{}' not found", username);
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            LOGGER.warn("Login failed: User '{}' account is inactive", username);
            throw new AuthenticationException("Your account has been deactivated. Please contact an administrator.");
        }

        if (!PasswordUtil.checkPassword(plainPassword, user.getPasswordHash())) {
            LOGGER.warn("Login failed: Password mismatch for user '{}'", username);
            throw new AuthenticationException("Invalid username or password.");
        }

        LOGGER.info("User '{}' ({}) logged in successfully", user.getUsername(), user.getRole());
        return user;
    }

    /**
     * Validates that the user has one of the allowed roles.
     */
    public boolean hasRole(User user, Role... allowedRoles) {
        if (user == null || user.getRole() == null || allowedRoles == null) {
            return false;
        }
        return Arrays.stream(allowedRoles)
                .anyMatch(role -> role == user.getRole());
    }

    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        ValidationUtil.requireNonEmpty(currentPassword, "Current Password");
        ValidationUtil.requireNonEmpty(newPassword, "New Password");

        if (newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters long.");
        }

        User user = userDAO.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found."));

        if (!PasswordUtil.checkPassword(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password does not match.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, newHash);
    }
}
