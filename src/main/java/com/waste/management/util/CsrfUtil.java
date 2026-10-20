package com.waste.management.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cross-Site Request Forgery (CSRF) protection utility.
 * Generates and validates cryptographic anti-CSRF tokens bound to user sessions.
 */
public class CsrfUtil {

    public static final String CSRF_TOKEN_SESSION_ATTR = "CSRF_TOKEN";
    public static final String CSRF_TOKEN_PARAM = "csrfToken";
    public static final String CSRF_TOKEN_HEADER = "X-CSRF-Token";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Retrieves the existing CSRF token for this session or generates a new one.
     */
    public static String getToken(HttpSession session) {
        if (session == null) {
            return "";
        }
        String token = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
        if (token == null || token.trim().isEmpty()) {
            token = generateNewToken();
            session.setAttribute(CSRF_TOKEN_SESSION_ATTR, token);
        }
        return token;
    }

    /**
     * Generates a new cryptographically secure random token string.
     */
    public static String generateNewToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Validates the CSRF token submitted in the request against the session token.
     */
    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }

        String requestToken = request.getParameter(CSRF_TOKEN_PARAM);
        if (requestToken == null || requestToken.trim().isEmpty()) {
            requestToken = request.getHeader(CSRF_TOKEN_HEADER);
        }

        return sessionToken.equals(requestToken);
    }
}
