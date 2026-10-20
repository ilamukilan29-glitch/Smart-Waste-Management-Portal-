package com.waste.management.servlet;

import com.google.gson.Gson;
import com.waste.management.filter.AuthenticationFilter;
import com.waste.management.model.Role;
import com.waste.management.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Base controller providing shared dispatching, JSON serialization,
 * session user retrieval, and flash messaging utilities.
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final Gson GSON = new Gson();

    protected User getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (User) session.getAttribute(AuthenticationFilter.SESSION_USER_KEY) : null;
    }

    protected boolean isAdmin(HttpServletRequest request) {
        User user = getLoggedInUser(request);
        return user != null && user.getRole() == Role.ADMIN;
    }

    protected boolean isSupervisor(HttpServletRequest request) {
        User user = getLoggedInUser(request);
        return user != null && (user.getRole() == Role.SUPERVISOR || user.getRole() == Role.ADMIN);
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String viewName)
            throws ServletException, IOException {
        String fullPath = "/WEB-INF/views/" + viewName + ".jsp";
        request.getRequestDispatcher(fullPath).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void setFlashSuccess(HttpServletRequest request, String message) {
        request.getSession().setAttribute("FLASH_SUCCESS", message);
    }

    protected void setFlashError(HttpServletRequest request, String message) {
        request.getSession().setAttribute("FLASH_ERROR", message);
    }

    protected void setFlashWarning(HttpServletRequest request, String message) {
        request.getSession().setAttribute("FLASH_WARNING", message);
    }

    protected void sendJson(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(GSON.toJson(data));
            out.flush();
        }
    }
}
