# Entity Relationship (ER) Diagram & Relational Schema Guide

This document presents the normalized relational database architecture for the **Smart Waste Management and Collection Optimization System** (`waste_management_db`).

---

## 1. Mermaid Entity-Relationship Diagram

```mermaid
erDiagram
    USERS ||--o{ WORKERS : "has profile"
    USERS ||--o{ COLLECTION_PLANS : "creates"
    AREAS ||--o{ DEPOTS : "contains"
    AREAS ||--o{ WASTE_BINS : "locates"
    AREAS ||--o{ WORKERS : "assigns"
    AREAS ||--o{ VEHICLES : "assigns"
    AREAS ||--o{ COLLECTION_PLANS : "scopes"

    DEPOTS ||--o{ VEHICLES : "bases"
    DEPOTS ||--o{ COLLECTION_PLANS : "originates"

    COLLECTION_PLANS ||--o{ ROUTE_STOPS : "sequences"
    COLLECTION_PLANS ||--o| COLLECTION_TASKS : "triggers"
    COLLECTION_PLANS }o--|| VEHICLES : "uses"
    COLLECTION_PLANS }o--|| WORKERS : "assigns"

    WASTE_BINS ||--o{ ROUTE_STOPS : "visited at"
    WASTE_BINS ||--o{ COLLECTION_RECORDS : "records"

    WORKERS ||--o{ COLLECTION_TASKS : "executes"
    VEHICLES ||--o{ COLLECTION_TASKS : "deployed in"
    COLLECTION_TASKS ||--o{ COLLECTION_RECORDS : "logs"

    USERS {
        bigint id PK
        varchar username UK
        varchar password_hash
        varchar full_name
        varchar email UK
        varchar phone
        enum role "ADMIN, WORKER, SUPERVISOR"
        enum status "ACTIVE, INACTIVE"
        timestamp created_at
        timestamp updated_at
    }

    AREAS {
        bigint id PK
        varchar code UK
        varchar name
        varchar description
        varchar city
        timestamp created_at
    }

    DEPOTS {
        bigint id PK
        bigint area_id FK
        varchar code UK
        varchar name
        varchar address
        decimal latitude
        decimal longitude
        varchar contact_phone
        timestamp created_at
    }

    WASTE_BINS {
        bigint id PK
        varchar bin_code UK
        bigint area_id FK
        varchar location_name
        varchar address
        decimal latitude
        decimal longitude
        enum waste_type "ORGANIC, RECYCLABLE, PLASTIC, PAPER, GENERAL"
        decimal capacity_kg
        decimal current_fill_percentage
        enum status "ACTIVE, INACTIVE, MAINTENANCE"
        datetime last_collection_at
        datetime last_reported_at
        timestamp created_at
        timestamp updated_at
    }

    WORKERS {
        bigint id PK
        bigint user_id FK
        varchar worker_code UK
        varchar full_name
        varchar phone
        bigint area_id FK
        enum status "ACTIVE, INACTIVE"
        enum availability_status "AVAILABLE, ON_DUTY, ON_LEAVE"
        timestamp created_at
        timestamp updated_at
    }

    VEHICLES {
        bigint id PK
        varchar registration_number UK
        enum vehicle_type "COMPACTOR_TRUCK, TIPPER_TRUCK, ELECTRIC_VAN, MINI_TRUCK"
        decimal capacity_kg
        bigint area_id FK
        bigint depot_id FK
        enum status "ACTIVE, MAINTENANCE, RETIRED"
        enum availability_status "AVAILABLE, ON_ROUTE, UNAVAILABLE"
        timestamp created_at
        timestamp updated_at
    }

    LOCATION_DISTANCES {
        bigint id PK
        enum from_type "DEPOT, BIN"
        bigint from_id
        enum to_type "DEPOT, BIN"
        bigint to_id
        decimal distance_km
        int estimated_time_minutes
        timestamp created_at
    }

    COLLECTION_PLANS {
        bigint id PK
        varchar plan_code UK
        bigint area_id FK
        bigint depot_id FK
        bigint vehicle_id FK
        bigint worker_id FK
        decimal total_distance_km
        int estimated_duration_minutes
        decimal total_waste_estimated_kg
        enum status "DRAFT, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED"
        date planned_collection_date
        bigint created_by_user_id FK
        timestamp created_at
        timestamp updated_at
    }

    ROUTE_STOPS {
        bigint id PK
        bigint collection_plan_id FK
        int stop_order
        enum stop_type "DEPOT_START, BIN, DEPOT_END"
        bigint waste_bin_id FK
        varchar location_label
        decimal distance_from_prev_km
        decimal cumulative_distance_km
        decimal expected_waste_kg
        enum status "PENDING, VISITED, SKIPPED"
        datetime arrival_time
        datetime departure_time
    }

    COLLECTION_TASKS {
        bigint id PK
        varchar task_code UK
        bigint collection_plan_id FK
        bigint worker_id FK
        bigint vehicle_id FK
        date planned_date
        datetime actual_start_at
        datetime actual_completed_at
        decimal collected_weight_kg
        text remarks
        enum status "PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED"
        timestamp created_at
        timestamp updated_at
    }

    COLLECTION_RECORDS {
        bigint id PK
        bigint task_id FK
        bigint waste_bin_id FK
        datetime collected_at
        decimal fill_percentage_before
        decimal fill_percentage_after
        decimal weight_collected_kg
        varchar remarks
    }

    NOTIFICATIONS {
        bigint id PK
        varchar title
        text message
        enum alert_type "CRITICAL, WARNING, INFO, SUCCESS"
        varchar related_entity_type
        bigint related_entity_id
        boolean is_read
        timestamp created_at
    }
```

---

## 2. Relational Normalization Analysis

### First Normal Form (1NF)
- All columns contain atomic, non-decomposable values (e.g. single strings, single decimals).
- No repeating groups or arrays stored in single attributes.
- Each table defines a strict primary key.

### Second Normal Form (2NF)
- The schema is in 1NF.
- Every non-key attribute is fully functionally dependent on the primary key (no partial dependencies on composite keys).
- Route stops decompose individual tour waypoints into child records linked to parent collection plans.

### Third Normal Form (3NF / Boyce-Codd Normal Form)
- The schema is in 2NF.
- There are no transitive dependencies: non-key attributes depend only on the primary key, not on other non-key attributes.
- Distances are maintained in a dedicated lookup matrix table `location_distances`, decoupled from bin metadata.

---

## 3. Referential Integrity & Deletion Rules
- **Safe Soft Deletions**: Inactive bins, workers, and vehicles are flagged with `status = 'INACTIVE'` or `status = 'MAINTENANCE'` rather than `DELETE FROM ...`. This strictly preserves historical audit trails for past collection routes, fuel records, and worker tonnage statistics.
- **Foreign Key Constraints**: Foreign keys are enforced at the database engine level with InnoDB (`ON DELETE RESTRICT` on core references and `ON DELETE CASCADE` on tightly coupled child stops).
