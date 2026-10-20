package com.waste.management.listener;

import com.waste.management.service.ReportService;
import com.waste.management.util.DBConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Application Lifecycle Listener.
 * Manages database connection pool and thread executor initialization and shutdown.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Smart Waste Management Application starting up...");
        try {
            DBConnectionUtil.init();
            LOGGER.info("Database connection pool initialized.");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize database pool on startup: {}", e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Smart Waste Management Application shutting down...");
        try {
            ReportService.shutdownExecutor();
            LOGGER.info("Report executor shut down.");
        } catch (Exception e) {
            LOGGER.warn("Error shutting down report executor: {}", e.getMessage());
        }

        try {
            DBConnectionUtil.shutdown();
            LOGGER.info("Database pool shut down cleanly.");
        } catch (Exception e) {
            LOGGER.warn("Error shutting down database pool: {}", e.getMessage());
        }
    }
}
