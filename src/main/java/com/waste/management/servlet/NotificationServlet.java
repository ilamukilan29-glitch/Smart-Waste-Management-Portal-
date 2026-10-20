package com.waste.management.servlet;

import com.waste.management.model.Notification;
import com.waste.management.service.NotificationService;
import com.waste.management.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "NotificationServlet", urlPatterns = {
        "/notifications",
        "/notifications/read",
        "/notifications/read-all"
})
public class NotificationServlet extends BaseServlet {

    private NotificationService notificationService;

    @Override
    public void init() {
        this.notificationService = new NotificationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Notification> notifications = notificationService.getAllNotifications(50);
        request.setAttribute("notifications", notifications);
        forward(request, response, "notifications/list");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String path = request.getServletPath();

        if ("/notifications/read-all".equals(path)) {
            notificationService.markAllAsRead();
            setFlashSuccess(request, "All notifications marked as read.");
        } else if ("/notifications/read".equals(path)) {
            long id = ValidationUtil.parseLongOrDefault(request.getParameter("id"), 0L);
            notificationService.markAsRead(id);
            setFlashSuccess(request, "Notification marked as read.");
        }

        redirect(request, response, "/notifications");
    }
}
