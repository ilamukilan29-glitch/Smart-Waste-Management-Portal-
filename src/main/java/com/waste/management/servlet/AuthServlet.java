package com.waste.management.servlet;

import com.waste.management.exception.AuthenticationException;
import com.waste.management.exception.ValidationException;
import com.waste.management.filter.AuthenticationFilter;
import com.waste.management.model.Role;
import com.waste.management.model.User;
import com.waste.management.service.AuthService;
import com.waste.management.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/logout"})
public class AuthServlet extends BaseServlet {

    private AuthService authService;

    @Override
    public void init() {
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/logout".equals(path)) {
            handleLogout(request, response);
            return;
        }

        // Already logged in? Redirect to appropriate dashboard
        User user = getLoggedInUser(request);
        if (user != null) {
            if (user.getRole() == Role.WORKER) {
                redirect(request, response, "/worker/tasks");
            } else {
                redirect(request, response, "/dashboard");
            }
            return;
        }

        forward(request, response, "auth/login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        if ("/logout".equals(path)) {
            handleLogout(request, response);
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String redirectTarget = request.getParameter("redirect");

        try {
            User user = authService.login(username, password);

            // Session fixation protection: invalidate old session and create fresh session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute(AuthenticationFilter.SESSION_USER_KEY, user);
            CsrfUtil.getToken(newSession); // initialize CSRF token for the new session

            if (redirectTarget != null && !redirectTarget.trim().isEmpty() && !redirectTarget.contains("://")) {
                response.sendRedirect(request.getContextPath() + redirectTarget);
            } else {
                if (user.getRole() == Role.WORKER) {
                    redirect(request, response, "/worker/tasks");
                } else {
                    redirect(request, response, "/dashboard");
                }
            }
        } catch (AuthenticationException | ValidationException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("username", username);
            forward(request, response, "auth/login");
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login?loggedOut=true");
    }
}
