-- =============================================================
-- Seed test data for user-service
-- Database : project_management_system_user_service_db
-- Tables   : project_management_system_user
--
-- Cross-service reference (logical, no DB-level FK):
--   auth_user_id → auth-service: project_management_system_auth_user.id
--
-- auth_user_id values MUST match the UUIDs seeded in auth-service.
--
-- Run is idempotent: ON CONFLICT (id) DO NOTHING
-- =============================================================

-- ---------------------------------------------------------------
-- 1. Users
-- ---------------------------------------------------------------
INSERT INTO project_management_system_user
    (id, first_name, last_name, email, auth_user_id, deleted)
VALUES
    ('b0000000-0000-0000-0000-000000000001',
     'Admin', 'User',
     'admin@pms.local',
     'a0000000-0000-0000-0000-000000000001',
     false),

    ('b0000000-0000-0000-0000-000000000002',
     'John', 'Doe',
     'john.doe@pms.local',
     'a0000000-0000-0000-0000-000000000002',
     false),

    ('b0000000-0000-0000-0000-000000000003',
     'Jane', 'Smith',
     'jane.smith@pms.local',
     'a0000000-0000-0000-0000-000000000003',
     false),

    ('b0000000-0000-0000-0000-000000000004',
     'Bob', 'Jones',
     'bob.jones@pms.local',
     'a0000000-0000-0000-0000-000000000004',
     false)
ON CONFLICT (id) DO NOTHING;
