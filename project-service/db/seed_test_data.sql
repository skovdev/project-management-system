-- =============================================================
-- Seed test data for project-service
-- Database : project_management_system_project_service_db
-- Tables   : project_management_system_project
--
-- Cross-service reference (logical, no DB-level FK):
--   user_id → user-service: project_management_system_user.id
--
-- user_id values MUST match the UUIDs seeded in user-service.
--
-- project_status enum: PLANNING | IN_PROGRESS | COMPLETED | ON_HOLD | CANCELLED
--
-- Run is idempotent: ON CONFLICT (id) DO NOTHING
-- =============================================================

-- ---------------------------------------------------------------
-- 1. Projects  (covers all five status values for broad scenario coverage)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_project
    (id, title, description, project_status, start_date, end_date, user_id, deleted)
VALUES
    -- Owned by john.doe (b...0002)
    ('c0000000-0000-0000-0000-000000000001',
     'E-Commerce Platform Redesign',
     'Full redesign of the customer-facing shop: new UI, updated checkout flow, and product recommendation engine.',
     'IN_PROGRESS',
     '2026-01-15 09:00:00',
     '2026-06-30 18:00:00',
     'b0000000-0000-0000-0000-000000000002',
     false),

    -- Owned by jane.smith (b...0003)
    ('c0000000-0000-0000-0000-000000000002',
     'Mobile Banking Application',
     'Cross-platform mobile app for retail banking customers, covering account management, transfers, and notifications.',
     'PLANNING',
     '2026-03-01 09:00:00',
     '2026-12-31 18:00:00',
     'b0000000-0000-0000-0000-000000000003',
     false),

    -- Owned by bob.jones (b...0004)
    ('c0000000-0000-0000-0000-000000000003',
     'HR Management System v2',
     'Second-generation HR platform with automated onboarding, leave management, and performance review modules.',
     'COMPLETED',
     '2025-06-01 09:00:00',
     '2026-01-31 18:00:00',
     'b0000000-0000-0000-0000-000000000004',
     false),

    -- Owned by admin (b...0001)
    ('c0000000-0000-0000-0000-000000000004',
     'Data Analytics Dashboard',
     'Centralised BI dashboard aggregating KPIs from sales, support, and operations for executive reporting.',
     'ON_HOLD',
     '2025-09-01 09:00:00',
     '2026-08-31 18:00:00',
     'b0000000-0000-0000-0000-000000000001',
     false),

    -- Owned by john.doe (b...0002) – cancelled legacy project
    ('c0000000-0000-0000-0000-000000000005',
     'Legacy CRM Migration',
     'Migration of the old on-premise CRM to a cloud-native SaaS solution. Project was cancelled after vendor change.',
     'CANCELLED',
     '2025-01-10 09:00:00',
     '2025-07-31 18:00:00',
     'b0000000-0000-0000-0000-000000000002',
     false)
ON CONFLICT (id) DO NOTHING;
