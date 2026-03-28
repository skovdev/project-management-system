-- =============================================================
-- Seed test data for task-service
-- Database : project_management_system_task_service_db
-- Tables   : project_management_system_task
--
-- Cross-service references (logical, no DB-level FKs):
--   project_id → project-service: project_management_system_project.id
--   user_id    → user-service:    project_management_system_user.id
--
-- UNIQUE constraints on the table:
--   • project_id  – each project may have at most ONE task
--   • user_id     – each user may be assigned to at most ONE task
--
-- task_status   enum: TODO | IN_PROGRESS | DONE | ON_HOLD | CANCELLED
-- task_priority enum: LOW  | MEDIUM      | HIGH | CRITICAL | BLOCKER
--
-- Run is idempotent: ON CONFLICT (id) DO NOTHING
-- =============================================================

-- ---------------------------------------------------------------
-- 1. Tasks  (one per project, one per user — respects unique constraints)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_task
    (id, title, description,
     task_status, task_priority,
     active, project_id, user_id, deleted)
VALUES
    -- E-Commerce Platform Redesign (c...001), assigned to john.doe (b...0002)
    ('d0000000-0000-0000-0000-000000000001',
     'Implement Product Catalog API',
     'Design and implement REST endpoints for the product catalogue: CRUD operations, pagination, search, and category filtering.',
     'IN_PROGRESS', 'HIGH',
     true,
     'c0000000-0000-0000-0000-000000000001',
     'b0000000-0000-0000-0000-000000000002',
     false),

    -- Mobile Banking Application (c...002), assigned to jane.smith (b...0003)
    ('d0000000-0000-0000-0000-000000000002',
     'Design Mobile UI Mockups',
     'Create high-fidelity Figma mockups for account overview, transaction history, and fund transfer screens.',
     'TODO', 'MEDIUM',
     true,
     'c0000000-0000-0000-0000-000000000002',
     'b0000000-0000-0000-0000-000000000003',
     false),

    -- HR Management System v2 (c...003), assigned to bob.jones (b...0004)
    ('d0000000-0000-0000-0000-000000000003',
     'Migrate Employee Records to New Schema',
     'Extract, transform, and load all employee records from the legacy HR database into the v2 schema with data validation.',
     'DONE', 'LOW',
     false,
     'c0000000-0000-0000-0000-000000000003',
     'b0000000-0000-0000-0000-000000000004',
     false),

    -- Data Analytics Dashboard (c...004), assigned to admin (b...0001)
    ('d0000000-0000-0000-0000-000000000004',
     'Configure BI Report Templates',
     'Set up executive report templates in the BI tool: monthly KPI summary, sales funnel, and support SLA compliance.',
     'ON_HOLD', 'CRITICAL',
     false,
     'c0000000-0000-0000-0000-000000000004',
     'b0000000-0000-0000-0000-000000000001',
     false)
ON CONFLICT (id) DO NOTHING;
