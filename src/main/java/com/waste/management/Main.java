package com.waste.management;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/**
 * Embedded Apache Tomcat 9 Launcher for direct localhost execution.
 * Mounts src/main/webapp and classes from target/classes to provide
 * instantaneous zero-configuration local server startup on port 8080.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portStr = System.getenv("PORT");
        if (portStr != null && !portStr.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        // Configure temporary base directory
        File baseDir = new File("target/tomcat");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(baseDir.getAbsolutePath());

        // Initialize HTTP Connector
        tomcat.getConnector();

        // Mount Web Application
        File webappDir = new File("src/main/webapp");
        if (!webappDir.exists()) {
            throw new IllegalStateException("Webapp directory does not exist: " + webappDir.getAbsolutePath());
        }

        Context ctx = tomcat.addWebapp("/smart-waste-management", webappDir.getAbsolutePath());
        ctx.setParentClassLoader(Main.class.getClassLoader());

        // Map compiled classes so servlets, filters, and listeners are found
        File additionWebInfClasses = new File("target/classes");
        if (additionWebInfClasses.exists()) {
            WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }

        // Start Tomcat engine
        tomcat.start();

        System.out.println();
        System.out.println("===============================================================================");
        System.out.println("  ECOWASTE - SMART WASTE MANAGEMENT AND COLLECTION OPTIMIZATION SYSTEM");
        System.out.println("  Embedded Apache Tomcat 9.0 Server Successfully Started!");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("  Web URL:      http://localhost:" + port + "/smart-waste-management/");
        System.out.println("  Login Page:   http://localhost:" + port + "/smart-waste-management/login");
        System.out.println("  Dashboard:    http://localhost:" + port + "/smart-waste-management/dashboard");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("  Default Academic Test Credentials:");
        System.out.println("  - Administrator: username: admin      | password: Admin@123");
        System.out.println("  - Supervisor:    username: supervisor | password: Super@123");
        System.out.println("  - Worker:        username: worker1    | password: Worker@123");
        System.out.println("===============================================================================");
        System.out.println();

        tomcat.getServer().await();
    }
}
