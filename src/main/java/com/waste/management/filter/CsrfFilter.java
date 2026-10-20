package com.waste.management.filter;

import com.waste.management.util.CsrfUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "CsrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String method = request.getMethod();
        String path = request.getRequestURI().substring(request.getContextPath().length());

        // Only enforce CSRF verification on state-changing requests (POST, PUT, DELETE)
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            // Whitelist login endpoint (initial session establishment) and simulation toggle if needed
            if (!path.equals("/login")) {
                if (!CsrfUtil.isValid(request)) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "CSRF token validation failed or token expired. Please refresh the page and try again.");
                    return;
                }
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}
