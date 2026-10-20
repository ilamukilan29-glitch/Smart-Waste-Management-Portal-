-- =============================================================================
-- SMART WASTE MANAGEMENT AND COLLECTION OPTIMIZATION SYSTEM
-- Database Schema (MySQL 8.x compatible, strictly normalized relational design)
-- Database Name: waste_management_db
-- =============================================================================

CREATE DATABASE IF NOT EXISTS waste_management_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE waste_management_db;

-- Disable foreign key checks during creation/reset
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Users Table (System Authentication & RBAC)
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS collection_records;
DROP TABLE IF EXISTS collection_tasks;
DROP TABLE IF EXISTS route_stops;
DROP TABLE IF EXISTS collection_plans;
DROP TABLE IF EXISTS location_distances;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS workers;
DROP TABLE IF EXISTS waste_bins;
DROP TABLE IF EXISTS depots;
DROP TABLE IF EXISTS areas;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    role ENUM('ADMIN', 'WORKER', 'SUPERVISOR') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- 2. Areas Table (Municipal Administrative Zones)
CREATE TABLE areas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    city VARCHAR(100) NOT NULL DEFAULT 'Metro City',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Depots Table (Base stations / starting & ending points for collection trucks)
CREATE TABLE depots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    area_id BIGINT NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    contact_phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 4. Waste Bins Table (Smart Waste Monitoring Bins)
CREATE TABLE waste_bins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bin_code VARCHAR(30) NOT NULL UNIQUE,
    area_id BIGINT NOT NULL,
    location_name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    waste_type ENUM('ORGANIC', 'RECYCLABLE', 'PLASTIC', 'PAPER', 'GENERAL') NOT NULL DEFAULT 'GENERAL',
    capacity_kg DECIMAL(8, 2) NOT NULL,
    current_fill_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') NOT NULL DEFAULT 'ACTIVE',
    last_collection_at DATETIME NULL,
    last_reported_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE RESTRICT,
    INDEX idx_bin_area (area_id),
    INDEX idx_bin_fill (current_fill_percentage),
    INDEX idx_bin_status (status)
) ENGINE=InnoDB;

-- 5. Workers Table (Collection personnel)
CREATE TABLE workers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL UNIQUE,
    worker_code VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    area_id BIGINT NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    availability_status ENUM('AVAILABLE', 'ON_DUTY', 'ON_LEAVE') NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE RESTRICT,
    INDEX idx_worker_area (area_id),
    INDEX idx_worker_avail (availability_status)
) ENGINE=InnoDB;

-- 6. Vehicles Table (Collection Fleet)
CREATE TABLE vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_number VARCHAR(30) NOT NULL UNIQUE,
    vehicle_type ENUM('COMPACTOR_TRUCK', 'TIPPER_TRUCK', 'ELECTRIC_VAN', 'MINI_TRUCK') NOT NULL,
    capacity_kg DECIMAL(8, 2) NOT NULL,
    area_id BIGINT NOT NULL,
    depot_id BIGINT NOT NULL,
    status ENUM('ACTIVE', 'MAINTENANCE', 'RETIRED') NOT NULL DEFAULT 'ACTIVE',
    availability_status ENUM('AVAILABLE', 'ON_ROUTE', 'UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE RESTRICT,
    FOREIGN KEY (depot_id) REFERENCES depots(id) ON DELETE RESTRICT,
    INDEX idx_vehicle_avail (availability_status),
    INDEX idx_vehicle_area (area_id)
) ENGINE=InnoDB;

-- 7. Location Distances (Distance Matrix between depots and bins)
CREATE TABLE location_distances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_type ENUM('DEPOT', 'BIN') NOT NULL,
    from_id BIGINT NOT NULL,
    to_type ENUM('DEPOT', 'BIN') NOT NULL,
    to_id BIGINT NOT NULL,
    distance_km DECIMAL(6, 2) NOT NULL,
    estimated_time_minutes INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_route_pair (from_type, from_id, to_type, to_id),
    INDEX idx_dist_lookup (from_type, from_id, to_type, to_id)
) ENGINE=InnoDB;

-- 8. Collection Plans (Generated route optimization plans)
CREATE TABLE collection_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_code VARCHAR(40) NOT NULL UNIQUE,
    area_id BIGINT NOT NULL,
    depot_id BIGINT NOT NULL,
    vehicle_id BIGINT NULL,
    worker_id BIGINT NULL,
    total_distance_km DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    estimated_duration_minutes INT NOT NULL DEFAULT 0,
    total_waste_estimated_kg DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    status ENUM('DRAFT', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
    planned_collection_date DATE NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE RESTRICT,
    FOREIGN KEY (depot_id) REFERENCES depots(id) ON DELETE RESTRICT,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL,
    FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_plan_status (status),
    INDEX idx_plan_date (planned_collection_date)
) ENGINE=InnoDB;

-- 9. Route Stops (Sequential stops in an optimized collection plan)
CREATE TABLE route_stops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    collection_plan_id BIGINT NOT NULL,
    stop_order INT NOT NULL,
    stop_type ENUM('DEPOT_START', 'BIN', 'DEPOT_END') NOT NULL,
    waste_bin_id BIGINT NULL,
    location_label VARCHAR(150) NOT NULL,
    distance_from_prev_km DECIMAL(6, 2) NOT NULL DEFAULT 0.00,
    cumulative_distance_km DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    expected_waste_kg DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    status ENUM('PENDING', 'VISITED', 'SKIPPED') NOT NULL DEFAULT 'PENDING',
    arrival_time DATETIME NULL,
    departure_time DATETIME NULL,
    FOREIGN KEY (collection_plan_id) REFERENCES collection_plans(id) ON DELETE CASCADE,
    FOREIGN KEY (waste_bin_id) REFERENCES waste_bins(id) ON DELETE SET NULL,
    INDEX idx_stop_plan (collection_plan_id),
    INDEX idx_stop_bin (waste_bin_id)
) ENGINE=InnoDB;

-- 10. Collection Tasks (Assigned operational tasks tracked for completion)
CREATE TABLE collection_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_code VARCHAR(40) NOT NULL UNIQUE,
    collection_plan_id BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    planned_date DATE NOT NULL,
    actual_start_at DATETIME NULL,
    actual_completed_at DATETIME NULL,
    collected_weight_kg DECIMAL(8, 2) DEFAULT 0.00,
    remarks TEXT NULL,
    status ENUM('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ASSIGNED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (collection_plan_id) REFERENCES collection_plans(id) ON DELETE RESTRICT,
    FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE RESTRICT,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE RESTRICT,
    INDEX idx_task_worker (worker_id),
    INDEX idx_task_status (status),
    INDEX idx_task_date (planned_date)
) ENGINE=InnoDB;

-- 11. Collection Records (Historical audit log per bin collection)
CREATE TABLE collection_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    waste_bin_id BIGINT NOT NULL,
    collected_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fill_percentage_before DECIMAL(5, 2) NOT NULL,
    fill_percentage_after DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    weight_collected_kg DECIMAL(8, 2) NOT NULL,
    remarks VARCHAR(255) NULL,
    FOREIGN KEY (task_id) REFERENCES collection_tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (waste_bin_id) REFERENCES waste_bins(id) ON DELETE RESTRICT,
    INDEX idx_record_bin (waste_bin_id),
    INDEX idx_record_date (collected_at)
) ENGINE=InnoDB;

-- 12. Notifications (In-app operational alerts for municipal administrators and supervisors)
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    alert_type ENUM('CRITICAL', 'WARNING', 'INFO', 'SUCCESS') NOT NULL DEFAULT 'INFO',
    related_entity_type VARCHAR(50) NULL,
    related_entity_id BIGINT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notif_read (is_read),
    INDEX idx_notif_created (created_at)
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;
