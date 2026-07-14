-- =============================================================
-- Seed test data for organization-service
-- Database : project_management_system_organization_service_db
-- Tables   : project_management_system_organization
--            project_management_system_organization_member
--
-- Cross-service reference (logical, no DB-level FK):
--   owner_id (organization) / user_id (organization_member)
--     → auth-service: project_management_system_auth_user.id (authUserId from JWT)
--
-- owner_id / user_id values MUST match the UUIDs seeded in auth-service:
--   admin      = a0000000-0000-0000-0000-000000000001
--   john.doe   = a0000000-0000-0000-0000-000000000002
--   jane.smith = a0000000-0000-0000-0000-000000000003
--   bob.jones  = a0000000-0000-0000-0000-000000000004
--
-- Every organization has exactly one OWNER member, matching the
-- "at least one OWNER" invariant enforced by the service layer.
--
-- Run is idempotent: ON CONFLICT (id) DO NOTHING
-- =============================================================

-- ---------------------------------------------------------------
-- 1. Organizations
-- ---------------------------------------------------------------
INSERT INTO project_management_system_organization
    (id, name, description, owner_id, deleted)
VALUES
    -- Owned by admin
    ('cc000000-0000-0000-0000-000000000001',
     'Acme Corp',
     'Primary organization for the Acme product suite.',
     'a0000000-0000-0000-0000-000000000001',
     false),

    -- Owned by john.doe
    ('cc000000-0000-0000-0000-000000000002',
     'Globex Inc',
     'Globex engineering division, covering platform and infrastructure teams.',
     'a0000000-0000-0000-0000-000000000002',
     false),

    -- Owned by jane.smith
    ('cc000000-0000-0000-0000-000000000003',
     'Initech',
     'Initech customer solutions organization.',
     'a0000000-0000-0000-0000-000000000003',
     false)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 2. Organization members
-- ---------------------------------------------------------------
INSERT INTO project_management_system_organization_member
    (id, organization_id, user_id, role, deleted)
VALUES
    -- Acme Corp (cc...0001)
    ('dd000000-0000-0000-0000-000000000001',
     'cc000000-0000-0000-0000-000000000001',
     'a0000000-0000-0000-0000-000000000001',
     'OWNER', false),

    ('dd000000-0000-0000-0000-000000000002',
     'cc000000-0000-0000-0000-000000000001',
     'a0000000-0000-0000-0000-000000000002',
     'ADMIN', false),

    ('dd000000-0000-0000-0000-000000000003',
     'cc000000-0000-0000-0000-000000000001',
     'a0000000-0000-0000-0000-000000000004',
     'MEMBER', false),

    -- Globex Inc (cc...0002)
    ('dd000000-0000-0000-0000-000000000004',
     'cc000000-0000-0000-0000-000000000002',
     'a0000000-0000-0000-0000-000000000002',
     'OWNER', false),

    ('dd000000-0000-0000-0000-000000000005',
     'cc000000-0000-0000-0000-000000000002',
     'a0000000-0000-0000-0000-000000000003',
     'MEMBER', false),

    -- Initech (cc...0003)
    ('dd000000-0000-0000-0000-000000000006',
     'cc000000-0000-0000-0000-000000000003',
     'a0000000-0000-0000-0000-000000000003',
     'OWNER', false),

    ('dd000000-0000-0000-0000-000000000007',
     'cc000000-0000-0000-0000-000000000003',
     'a0000000-0000-0000-0000-000000000001',
     'ADMIN', false),

    ('dd000000-0000-0000-0000-000000000008',
     'cc000000-0000-0000-0000-000000000003',
     'a0000000-0000-0000-0000-000000000004',
     'MEMBER', false)
ON CONFLICT (id) DO NOTHING;
