-- =============================================================================
-- SMART WASTE MANAGEMENT AND COLLECTION OPTIMIZATION SYSTEM
-- Seed Data Script
-- =============================================================================

USE waste_management_db;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE notifications;
TRUNCATE TABLE collection_records;
TRUNCATE TABLE collection_tasks;
TRUNCATE TABLE route_stops;
TRUNCATE TABLE collection_plans;
TRUNCATE TABLE location_distances;
TRUNCATE TABLE vehicles;
TRUNCATE TABLE workers;
TRUNCATE TABLE waste_bins;
TRUNCATE TABLE depots;
TRUNCATE TABLE areas;
TRUNCATE TABLE users;

-- 1. Users (Passwords: admin -> Admin@123, supervisor -> Super@123, worker1/2/3 -> Worker@123)
INSERT INTO users (id, username, password_hash, full_name, email, phone, role, status) VALUES
(1, 'admin',      '$2a$12$gu18CjvVcJ5WsQHWwGWigu.uD.Zbc.is6wv342bxDVPQ/oItgCxG6', 'Chief Administrator', 'admin@ecowaste.gov',      '+91 98400 11223', 'ADMIN',      'ACTIVE'),
(2, 'supervisor', '$2a$12$HW36gz1UkHP8LrR2dQWJUuNUoOiiRvuz1CPbWZQCLLHtVo6hYX.0u', 'Anand Verma',         'supervisor@ecowaste.gov', '+91 98400 22334', 'SUPERVISOR', 'ACTIVE'),
(3, 'worker1',    '$2a$12$gEta0pFz5ML9iSI1OhJYoeNBFe.V77ERGHzsr.wfgEyWZ74Oo.i4W', 'Rajesh Kumar',        'rajesh@ecowaste.gov',     '+91 98400 33445', 'WORKER',     'ACTIVE'),
(4, 'worker2',    '$2a$12$gEta0pFz5ML9iSI1OhJYoeNBFe.V77ERGHzsr.wfgEyWZ74Oo.i4W', 'Suresh Patel',        'suresh@ecowaste.gov',     '+91 98400 44556', 'WORKER',     'ACTIVE'),
(5, 'worker3',    '$2a$12$gEta0pFz5ML9iSI1OhJYoeNBFe.V77ERGHzsr.wfgEyWZ74Oo.i4W', 'Amit Sharma',         'amit@ecowaste.gov',       '+91 98400 55667', 'WORKER',     'ACTIVE');

-- 2. Areas
INSERT INTO areas (id, code, name, description, city) VALUES
(1, 'CENTRAL',  'Central Commercial Zone', 'High-density retail, administrative offices, and markets', 'Metro City'),
(2, 'NORTH',    'North Residential Sector', 'Medium-density residential colonies and academic campuses', 'Metro City'),
(3, 'TECHPARK', 'East Technology Corridor', 'IT parks, corporate complexes, and institutional facilities', 'Metro City');

-- 3. Depots
INSERT INTO depots (id, area_id, code, name, address, latitude, longitude, contact_phone) VALUES
(1, 1, 'DEPOT-C1', 'Central Municipal Garage', '100 Municipal Way, Central Zone', 13.082700, 80.270700, '+91 44 2530 0101'),
(2, 2, 'DEPOT-N1', 'North Sanitation Yard',    '24 Ring Road, North Sector',       13.125000, 80.250000, '+91 44 2530 0202');

-- 4. Workers
INSERT INTO workers (id, user_id, worker_code, full_name, phone, area_id, status, availability_status) VALUES
(1, 3, 'WRK-001', 'Rajesh Kumar', '+91 98400 33445', 1, 'ACTIVE', 'AVAILABLE'),
(2, 4, 'WRK-002', 'Suresh Patel', '+91 98400 44556', 1, 'ACTIVE', 'AVAILABLE'),
(3, 5, 'WRK-003', 'Amit Sharma',  '+91 98400 55667', 2, 'ACTIVE', 'AVAILABLE');

-- 5. Vehicles
INSERT INTO vehicles (id, registration_number, vehicle_type, capacity_kg, area_id, depot_id, status, availability_status) VALUES
(1, 'TN-01-WM-1001', 'COMPACTOR_TRUCK', 1800.00, 1, 1, 'ACTIVE', 'AVAILABLE'),
(2, 'TN-01-WM-1002', 'TIPPER_TRUCK',    1200.00, 1, 1, 'ACTIVE', 'AVAILABLE'),
(3, 'TN-02-WM-2001', 'ELECTRIC_VAN',     600.00, 2, 2, 'ACTIVE', 'AVAILABLE'),
(4, 'TN-01-WM-1003', 'MINI_TRUCK',       800.00, 1, 1, 'ACTIVE', 'UNAVAILABLE'); -- Demonstrates unavailable vehicle

-- 6. Waste Bins
-- Demonstrates: Critical (>=90%), High (80-89.9%), Medium (60-79.9%), and Low (<60%) bins
INSERT INTO waste_bins (id, bin_code, area_id, location_name, address, latitude, longitude, waste_type, capacity_kg, current_fill_percentage, status, last_collection_at) VALUES
(1, 'BIN-C-01', 1, 'Central Market Gate 1',        '15 Market Street',       13.085000, 80.273000, 'ORGANIC',    120.00, 95.00, 'ACTIVE', '2026-10-18 10:00:00'),
(2, 'BIN-C-02', 1, 'Town Hall Plaza',             '1 Civic Center Ave',     13.081500, 80.268000, 'RECYCLABLE', 100.00, 85.00, 'ACTIVE', '2026-10-17 11:30:00'),
(3, 'BIN-C-03', 1, 'Metro Rail Station Exit A',    '45 Station Link Road',   13.078000, 80.275000, 'PLASTIC',    100.00, 75.00, 'ACTIVE', '2026-10-19 16:45:00'),
(4, 'BIN-C-04', 1, 'General Hospital Entrance',    '88 Healthcare Boulevard', 13.089000, 80.265000, 'GENERAL',    150.00, 65.00, 'ACTIVE', '2026-10-18 14:00:00'),
(5, 'BIN-C-05', 1, 'Public Library Square',       '12 Heritage Lane',       13.075000, 80.262000, 'PAPER',       80.00, 35.00, 'ACTIVE', CURRENT_TIMESTAMP),
(6, 'BIN-C-06', 1, 'City Park North End',          '3 Greenway Boulevard',   13.092000, 80.278000, 'ORGANIC',    100.00, 20.00, 'ACTIVE', CURRENT_TIMESTAMP),
(7, 'BIN-N-01', 2, 'North Sector High School',     '50 Scholar Way',         13.128000, 80.248000, 'PAPER',      100.00, 92.00, 'ACTIVE', '2026-10-16 09:15:00'),
(8, 'BIN-N-02', 2, 'Community Sports Complex',     '10 Stadium Road',        13.122000, 80.255000, 'GENERAL',    120.00, 82.00, 'ACTIVE', '2026-10-17 15:30:00'),
(9, 'BIN-N-03', 2, 'Botanical Garden Pavilion',    '8 Floral Park Road',     13.131000, 80.259000, 'ORGANIC',    100.00, 45.00, 'ACTIVE', CURRENT_TIMESTAMP);

-- 7. Notifications
INSERT INTO notifications (id, title, message, alert_type, related_entity_type, related_entity_id, is_read) VALUES
(1, 'Critical Overflow Alert: BIN-C-01', 'Central Market Gate 1 has reached 95% fill capacity. Priority collection required.', 'CRITICAL', 'BIN', 1, FALSE),
(2, 'Critical Overflow Alert: BIN-N-01', 'North Sector High School has reached 92% fill capacity. Priority collection required.', 'CRITICAL', 'BIN', 7, FALSE),
(3, 'Vehicle Maintenance Notification',  'Vehicle TN-01-WM-1003 is currently unavailable for route dispatch.', 'WARNING', 'VEHICLE', 4, TRUE);

-- 8. Historical Completed Task & Records (To demonstrate reports and history)
INSERT INTO collection_plans (id, plan_code, area_id, depot_id, vehicle_id, worker_id, total_distance_km, estimated_duration_minutes, total_waste_estimated_kg, status, planned_collection_date, created_by_user_id) VALUES
(1, 'PLAN-HIST-001', 1, 1, 1, 1, 8.40, 45, 185.00, 'COMPLETED', '2026-10-19', 1);

INSERT INTO route_stops (collection_plan_id, stop_order, stop_type, waste_bin_id, location_label, distance_from_prev_km, cumulative_distance_km, expected_waste_kg, status) VALUES
(1, 1, 'DEPOT_START', NULL, 'Depot Start: Central Municipal Garage', 0.00, 0.00, 0.00, 'VISITED'),
(1, 2, 'BIN', 5, 'Bin: BIN-C-05 (Public Library Square)', 2.10, 2.10, 80.00, 'VISITED'),
(1, 3, 'BIN', 6, 'Bin: BIN-C-06 (City Park North End)', 3.50, 5.60, 105.00, 'VISITED'),
(1, 4, 'DEPOT_END', NULL, 'Depot Return: Central Municipal Garage', 2.80, 8.40, 185.00, 'VISITED');

INSERT INTO collection_tasks (id, task_code, collection_plan_id, worker_id, vehicle_id, planned_date, actual_start_at, actual_completed_at, collected_weight_kg, remarks, status) VALUES
(1, 'TASK-HIST-001', 1, 1, 1, '2026-10-19',
 '2026-10-19 14:00:00', '2026-10-19 15:00:00', 192.50, 'All bins emptied successfully and sanitization completed.', 'COMPLETED');

INSERT INTO collection_records (id, task_id, waste_bin_id, collected_at, fill_percentage_before, fill_percentage_after, weight_collected_kg, remarks) VALUES
(1, 1, 5, '2026-10-19 14:20:00', 95.00, 0.00, 82.50, 'Emptied without incident'),
(2, 1, 6, '2026-10-19 14:45:00', 90.00, 0.00, 110.00, 'Heavy organic waste collected');

SET FOREIGN_KEY_CHECKS = 1;
