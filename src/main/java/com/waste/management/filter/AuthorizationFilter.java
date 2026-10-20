package com.waste.management.filter;

import com.waste.management.model.Role;
import com.waste.management.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "AuthorizationFilter", urlPatterns = "/*")
public class AuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        // Skip static resources and auth endpoints
        if (path.startsWith("/assets/") || path.equals("/login") || path.equals("/logout")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute(AuthenticationFilter.SESSION_USER_KEY) : null;

        if (user == null) {
            // AuthenticationFilter will handle redirect
            chain.doFilter(req, res);
            return;
        }

        Role role = user.getRole();

        // 1. Admin-Only Endpoints
        boolean isAdminEndpoint = path.startsWith("/workers/create") ||
                                  path.startsWith("/workers/edit") ||
                                  path.startsWith("/vehicles/create") ||
                                  path.startsWith("/vehicles/edit") ||
                                  path.startsWith("/bins/create") ||
                                  path.startsWith("/bins/edit") ||
                                  path.startsWith("/bins/delete") ||
                                  path.startsWith("/routes/plan") ||
                                  path.startsWith("/tasks/assign") ||
                                  path.startsWith("/simulation") ||
                                  path.startsWith("/settings");

        if (isAdminEndpoint && role != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Administrator role required.");
            return;
        }

        // 2. Supervisor / Admin Endpoints (Workers cannot modify vehicles/workers/route plans)
        boolean isSupervisorOrAdminEndpoint = path.startsWith("/workers") ||
                                              path.startsWith("/vehicles") ||
                                              path.startsWith("/routes") ||
                                              path.startsWith("/priority") ||
                                              path.startsWith("/reports");

        if (isSupervisorOrAdminEndpoint && role == Role.WORKER) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Worker role does not have permission for this section.");
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}
