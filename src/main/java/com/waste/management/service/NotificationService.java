package com.waste.management.service;

import com.waste.management.dao.NotificationDAO;
import com.waste.management.dao.WasteBinDAO;
import com.waste.management.model.Notification;
import com.waste.management.model.WasteBin;

import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO;
    private final WasteBinDAO binDAO;

    public NotificationService() {
        this(new NotificationDAO(), new WasteBinDAO());
    }

    public NotificationService(NotificationDAO notificationDAO, WasteBinDAO binDAO) {
        this.notificationDAO = notificationDAO;
        this.binDAO = binDAO;
    }

    public List<Notification> getAllNotifications(int limit) {
        return notificationDAO.findAll(limit);
    }

    public List<Notification> getUnreadNotifications() {
        return notificationDAO.findUnread();
    }

    public boolean markAsRead(Long id) {
        return notificationDAO.markAsRead(id);
    }

    public boolean markAllAsRead() {
        return notificationDAO.markAllAsRead();
    }

    public void triggerCriticalBinAlert(WasteBin bin) {
        if (bin.getCurrentFillPercentage() >= 90.0) {
            Notification notif = new Notification(
                    "Critical Overflow Risk: Bin " + bin.getBinCode(),
                    "Bin " + bin.getBinCode() + " located at " + bin.getLocationName() + " has reached " + bin.getCurrentFillPercentage() + "% capacity. Immediate collection required.",
                    "CRITICAL",
                    "BIN",
                    bin.getId()
            );
            notificationDAO.save(notif);
        }
    }
}
