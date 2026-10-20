package com.waste.management.filter;

import com.waste.management.model.User;
import com.waste.management.util.CsrfUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "AuthenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter implements Filter {

    public static final String SESSION_USER_KEY = "LOGGED_IN_USER";

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        // Whitelist static assets and public authentication pages
        if (path.startsWith("/assets/") ||
            path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/images/") ||
            path.equals("/login") ||
            path.equals("/logout")) {

            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute(SESSION_USER_KEY) : null;

        if (user == null) {
            // Unauthenticated user -> redirect to login page with return URL
            String redirectUrl = request.getContextPath() + "/login";
            if (!path.equals("/") && !path.isEmpty()) {
                redirectUrl += "?redirect=" + response.encodeURL(path);
            }
            response.sendRedirect(redirectUrl);
            return;
        }

        // Attach CSRF token to request attribute so JSP forms can easily embed it
        if (session != null) {
            request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        }

        // Allow authenticated user to proceed
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}
