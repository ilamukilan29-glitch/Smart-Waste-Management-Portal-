-- =============================================================================
-- SMART WASTE MANAGEMENT AND COLLECTION OPTIMIZATION SYSTEM
-- Sample Road Network Distance Matrix (Distance between Depots and Bins)
-- Distances in Kilometers (km), Estimated Travel Time in Minutes (min)
-- =============================================================================

USE waste_management_db;

-- Clear previous distances
DELETE FROM location_distances;

-- =============================================================================
-- ZONE 1: Central Commercial Zone Distances (Depot 1 & Bins 1, 2, 3, 4, 5, 6)
-- =============================================================================

-- From Depot 1 (Central Municipal Garage) to Central Bins
INSERT INTO location_distances (from_type, from_id, to_type, to_id, distance_km, estimated_time_minutes) VALUES
('DEPOT', 1, 'BIN', 1, 1.80, 5),  -- Depot 1 <-> BIN-C-01 (Central Market)
('DEPOT', 1, 'BIN', 2, 1.20, 4),  -- Depot 1 <-> BIN-C-02 (Town Hall Plaza)
('DEPOT', 1, 'BIN', 3, 2.50, 7),  -- Depot 1 <-> BIN-C-03 (Metro Rail Station)
('DEPOT', 1, 'BIN', 4, 2.10, 6),  -- Depot 1 <-> BIN-C-04 (General Hospital)
('DEPOT', 1, 'BIN', 5, 2.80, 8),  -- Depot 1 <-> BIN-C-05 (Public Library)
('DEPOT', 1, 'BIN', 6, 3.40, 10); -- Depot 1 <-> BIN-C-06 (City Park)

-- Inter-bin distances within Central Zone
-- Bin 1 (Central Market) to others
INSERT INTO location_distances (from_type, from_id, to_type, to_id, distance_km, estimated_time_minutes) VALUES
('BIN', 1, 'BIN', 2, 1.40, 4),
('BIN', 1, 'BIN', 3, 1.90, 5),
('BIN', 1, 'BIN', 4, 1.10, 3),
('BIN', 1, 'BIN', 5, 2.60, 7),
('BIN', 1, 'BIN', 6, 1.50, 4);

-- Bin 2 (Town Hall Plaza) to others
INSERT INTO location_distances (from_type, from_id, to_type, to_id, distance_km, estimated_time_minutes) VALUES
('BIN', 2, 'BIN', 3, 2.10, 6),
('BIN', 2, 'BIN', 4, 1.60, 5),
('BIN', 2, 'BIN', 5, 1.30, 4),
('BIN', 2, 'BIN', 6, 2.90, 8);

-- Bin 3 (Metro Rail Station) to others
INSERT INTO location_distances (from_type, from_id, to_type, to_id, distance_km, estimated_time_minutes) VALUES
('BIN', 3, 'BIN', 4, 2.40, 7),
('BIN', 3, 'BIN', 5, 1.70, 5),
('BIN', 3, 'BIN', 6, 2.00, 5);

-- Bin 4 (General Hospital) to others
INSERT INTO location_distances (from_type, from_id, to_type, to_id, distance_km, estimated_time_minutes) VALUES
('BIN', 4, 'BIN', 5, 3.10, 9),
('BIN', 4, 'BIN', 6, 1.80, 5);

-- Bin 5 (Public Library) to others
INSERT INTO location_distances (from_type, from_id, to_type, to_id, distance_km, estimated_time_minutes) VALUES
('BIN', 5, 'BIN', 6, 3.80, 11);

-- =============================================================================
-- ZONE 2: North Residential Sector Distances (Depot 2 & Bins 7, 8, 9)
-- =============================================================================

-- From Depot 2 (North Sanitation Yard) to North Bins
INSERT INTO location_distances (from_type, from_id, to_type, to_id, distance_km, estimated_time_minutes) VALUES
('DEPOT', 2, 'BIN', 7, 1.50, 4),  -- Depot 2 <-> BIN-N-01 (High School)
('DEPOT', 2, 'BIN', 8, 1.90, 5),  -- Depot 2 <-> BIN-N-02 (Sports Complex)
('DEPOT', 2, 'BIN', 9, 2.40, 7);  -- Depot 2 <-> BIN-N-03 (Botanical Garden)

-- Inter-bin distances within North Zone
INSERT INTO location_distances (from_type, from_id, to_type, to_id, distance_km, estimated_time_minutes) VALUES
('BIN', 7, 'BIN', 8, 1.30, 4),
('BIN', 7, 'BIN', 9, 1.80, 5),
('BIN', 8, 'BIN', 9, 1.60, 5);
