-- =============================================================
-- Seed test data for auth-service
-- Database : project_management_system_auth_service_db
-- Tables   : project_management_system_auth_user
--            project_management_system_auth_role
--            project_management_system_auth_permission
--
-- Insertion order (respects FK auth_role.auth_user_id → auth_user.id
-- and auth_permission.auth_user_id → auth_user.id):
--   1. auth_user
--   2. auth_role
--   3. auth_permission
--
-- Plaintext passwords (BCrypt-10):
--   admin      → Admin123!
--   john.doe   → UserPass1!
--   jane.smith → UserPass2!
--   bob.jones  → UserPass3!
--
-- Run is idempotent: ON CONFLICT (id) DO NOTHING
-- =============================================================

-- ---------------------------------------------------------------
-- 1. Auth users
-- ---------------------------------------------------------------
INSERT INTO project_management_system_auth_user (id, username, password, deleted)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'admin',
     '$2a$10$VdRAz5hid6/AftQ8/bgrceTz1XYRz0Rv4cyUZaQZxytYBSLS47Bee', false),

    ('a0000000-0000-0000-0000-000000000002', 'john.doe',
     '$2a$10$DbYTLbMO/aow.OJ8aDRKjegu2j8nXKRofLDdFMiY9R4LgMhkJ8/D2', false),

    ('a0000000-0000-0000-0000-000000000003', 'jane.smith',
     '$2a$10$kN7oNjkOiLaoOeijvv0Au.GR8WgGbOrTk4wM./Z.We3DurHo4bc3y', false),

    ('a0000000-0000-0000-0000-000000000004', 'bob.jones',
     '$2a$10$QVAoerCbp72OZfm0a3rMne0CDccVEvvOHcG8BvEjQJraSWS.odIyi', false)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 2. Auth roles  (ROLE_USER assigned to every user)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_auth_role (id, authority, auth_user_id, deleted)
VALUES
    ('e0000000-0000-0000-0000-000000000001', 'USER',
     'a0000000-0000-0000-0000-000000000001', false),

    ('e0000000-0000-0000-0000-000000000002', 'USER',
     'a0000000-0000-0000-0000-000000000002', false),

    ('e0000000-0000-0000-0000-000000000003', 'USER',
     'a0000000-0000-0000-0000-000000000003', false),

    ('e0000000-0000-0000-0000-000000000004', 'USER',
     'a0000000-0000-0000-0000-000000000004', false)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 3. Auth permissions  (READ_ALL assigned to every user)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_auth_permission (id, permission, auth_user_id, deleted)
VALUES
    ('f0000000-0000-0000-0000-000000000001', 'READ_ALL',
     'a0000000-0000-0000-0000-000000000001', false),

    ('f0000000-0000-0000-0000-000000000002', 'READ_ALL',
     'a0000000-0000-0000-0000-000000000002', false),

    ('f0000000-0000-0000-0000-000000000003', 'READ_ALL',
     'a0000000-0000-0000-0000-000000000003', false),

    ('f0000000-0000-0000-0000-000000000004', 'READ_ALL',
     'a0000000-0000-0000-0000-000000000004', false)
ON CONFLICT (id) DO NOTHING;
