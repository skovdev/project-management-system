-- =============================================================
-- Seed test data for project-service
-- Database : project_management_system_project_service_db
-- Tables   : project_management_system_project
--
-- Cross-service reference (logical, no DB-level FK):
--   user_id → auth-service: project_management_system_auth_user.id (authUserId from JWT)
--
-- user_id values MUST match the UUIDs seeded in auth-service.
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
    -- Owned by john.doe (a...0002)
    ('c0000000-0000-0000-0000-000000000001',
     'E-Commerce Platform Redesign',
     'Full redesign of the customer-facing shop: new UI, updated checkout flow, and product recommendation engine.',
     'IN_PROGRESS',
     '2026-01-15 09:00:00',
     '2026-06-30 18:00:00',
     'a0000000-0000-0000-0000-000000000002',
     false),

    -- Owned by jane.smith (a...0003)
    ('c0000000-0000-0000-0000-000000000002',
     'Mobile Banking Application',
     'Cross-platform mobile app for retail banking customers, covering account management, transfers, and notifications.',
     'PLANNING',
     '2026-03-01 09:00:00',
     '2026-12-31 18:00:00',
     'a0000000-0000-0000-0000-000000000003',
     false),

    -- Owned by bob.jones (a...0004)
    ('c0000000-0000-0000-0000-000000000003',
     'HR Management System v2',
     'Second-generation HR platform with automated onboarding, leave management, and performance review modules.',
     'COMPLETED',
     '2025-06-01 09:00:00',
     '2026-01-31 18:00:00',
     'a0000000-0000-0000-0000-000000000004',
     false),

    -- Owned by admin (a...0001)
    ('c0000000-0000-0000-0000-000000000004',
     'Data Analytics Dashboard',
     'Centralised BI dashboard aggregating KPIs from sales, support, and operations for executive reporting.',
     'ON_HOLD',
     '2025-09-01 09:00:00',
     '2026-08-31 18:00:00',
     'a0000000-0000-0000-0000-000000000001',
     false),

    -- Owned by john.doe (a...0002) – cancelled legacy project
    ('c0000000-0000-0000-0000-000000000005',
     'Legacy CRM Migration',
     'Migration of the old on-premise CRM to a cloud-native SaaS solution. Project was cancelled after vendor change.',
     'CANCELLED',
     '2025-01-10 09:00:00',
     '2025-07-31 18:00:00',
     'a0000000-0000-0000-0000-000000000002',
     false)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 2. Additional projects (2 per user, 10 users = 20 more projects)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_project
    (id, title, description, project_status, start_date, end_date, user_id, deleted)
VALUES
    -- admin (a...0001)
    ('c0000000-0000-0000-0000-000000000006',
     'DevOps Infrastructure Modernization',
     'Migrate all services to Kubernetes, introduce GitOps pipelines, and consolidate observability tooling across environments.',
     'IN_PROGRESS',
     '2026-02-01 09:00:00', '2026-09-30 18:00:00',
     'a0000000-0000-0000-0000-000000000001', false),

    ('c0000000-0000-0000-0000-000000000007',
     'Customer Support Portal',
     'Self-service support portal with ticket management, live chat, and an AI-powered FAQ knowledge base.',
     'PLANNING',
     '2026-04-01 09:00:00', '2026-12-31 18:00:00',
     'a0000000-0000-0000-0000-000000000001', false),

    -- john.doe (a...0002)
    ('c0000000-0000-0000-0000-000000000008',
     'API Gateway Consolidation',
     'Replace three separate gateway instances with a single managed gateway layer providing unified auth, rate-limiting, and routing.',
     'IN_PROGRESS',
     '2026-01-20 09:00:00', '2026-07-31 18:00:00',
     'a0000000-0000-0000-0000-000000000002', false),

    ('c0000000-0000-0000-0000-000000000009',
     'Inventory Management System',
     'Warehouse inventory tracking with barcode scanning, automatic reorder triggers, and supplier integration via EDI.',
     'COMPLETED',
     '2025-03-01 09:00:00', '2025-12-31 18:00:00',
     'a0000000-0000-0000-0000-000000000002', false),

    -- jane.smith (a...0003)
    ('c0000000-0000-0000-0000-000000000010',
     'IoT Fleet Monitoring Platform',
     'Real-time telematics dashboard for a fleet of 500+ vehicles including GPS tracking, fuel consumption, and driver behaviour scoring.',
     'IN_PROGRESS',
     '2026-01-10 09:00:00', '2026-10-31 18:00:00',
     'a0000000-0000-0000-0000-000000000003', false),

    ('c0000000-0000-0000-0000-000000000011',
     'Healthcare Patient Portal',
     'HIPAA-compliant portal for patients to view appointments, access lab results, request prescription refills, and message providers.',
     'PLANNING',
     '2026-05-01 09:00:00', '2027-02-28 18:00:00',
     'a0000000-0000-0000-0000-000000000003', false),

    -- bob.jones (a...0004)
    ('c0000000-0000-0000-0000-000000000012',
     'Supply Chain Optimization Tool',
     'End-to-end supply chain visibility platform with demand forecasting, supplier risk scoring, and logistics cost optimisation.',
     'IN_PROGRESS',
     '2026-02-15 09:00:00', '2026-11-30 18:00:00',
     'a0000000-0000-0000-0000-000000000004', false),

    ('c0000000-0000-0000-0000-000000000013',
     'Employee Self-Service Portal',
     'HR self-service portal enabling employees to manage leave requests, view payslips, update personal details, and access company policies.',
     'PLANNING',
     '2026-04-15 09:00:00', '2026-12-31 18:00:00',
     'a0000000-0000-0000-0000-000000000004', false),

    -- admin (a...0001, continued)
    ('c0000000-0000-0000-0000-000000000014',
     'AI-Powered Content Moderation',
     'Automated moderation pipeline using ML models to classify user-generated content for policy violations with human review fallback.',
     'IN_PROGRESS',
     '2026-03-01 09:00:00', '2026-10-31 18:00:00',
     'a0000000-0000-0000-0000-000000000001', false),

    ('c0000000-0000-0000-0000-000000000015',
     'Real-Time Analytics Engine',
     'Stream processing engine built on Kafka Streams and Flink to deliver sub-second dashboards for operational metrics.',
     'PLANNING',
     '2026-05-15 09:00:00', '2027-01-31 18:00:00',
     'a0000000-0000-0000-0000-000000000001', false),

    -- john.doe (a...0002, continued)
    ('c0000000-0000-0000-0000-000000000016',
     'Cybersecurity Audit Platform',
     'Centralised audit trail and vulnerability management platform integrating SIEM logs, CVE feeds, and compliance reporting.',
     'IN_PROGRESS',
     '2026-01-05 09:00:00', '2026-08-31 18:00:00',
     'a0000000-0000-0000-0000-000000000002', false),

    ('c0000000-0000-0000-0000-000000000017',
     'Cloud Cost Optimisation Dashboard',
     'Multi-cloud spend analytics with anomaly detection, reserved-instance recommendations, and team-level budget alerting.',
     'PLANNING',
     '2026-04-01 09:00:00', '2026-12-31 18:00:00',
     'a0000000-0000-0000-0000-000000000002', false),

    -- jane.smith (a...0003, continued)
    ('c0000000-0000-0000-0000-000000000018',
     'Marketing Automation Suite',
     'Multi-channel campaign orchestration platform with A/B testing, personalisation engine, and attribution reporting.',
     'IN_PROGRESS',
     '2026-02-01 09:00:00', '2026-09-30 18:00:00',
     'a0000000-0000-0000-0000-000000000003', false),

    ('c0000000-0000-0000-0000-000000000019',
     'Customer Loyalty Program',
     'Points-based loyalty system with tier management, reward catalogue, and partner redemption integrations.',
     'COMPLETED',
     '2025-04-01 09:00:00', '2026-01-31 18:00:00',
     'a0000000-0000-0000-0000-000000000003', false),

    -- bob.jones (a...0004, continued)
    ('c0000000-0000-0000-0000-000000000020',
     'Fraud Detection System',
     'Real-time transaction scoring engine using ensemble ML models to flag suspicious activity with adaptive thresholds per merchant category.',
     'IN_PROGRESS',
     '2026-01-15 09:00:00', '2026-09-30 18:00:00',
     'a0000000-0000-0000-0000-000000000004', false),

    ('c0000000-0000-0000-0000-000000000021',
     'Payment Gateway Integration',
     'Unified payment abstraction layer supporting Stripe, PayPal, and Adyen with PCI-DSS compliant tokenisation and reconciliation.',
     'PLANNING',
     '2026-05-01 09:00:00', '2027-01-31 18:00:00',
     'a0000000-0000-0000-0000-000000000004', false),

    -- admin (a...0001, continued)
    ('c0000000-0000-0000-0000-000000000022',
     'Knowledge Management Platform',
     'Internal wiki and document management system with semantic search, version control, and role-based access control.',
     'IN_PROGRESS',
     '2026-02-10 09:00:00', '2026-10-31 18:00:00',
     'a0000000-0000-0000-0000-000000000001', false),

    ('c0000000-0000-0000-0000-000000000023',
     'Remote Work Collaboration Tool',
     'Async-first collaboration workspace combining video rooms, threaded discussions, shared whiteboards, and calendar integration.',
     'ON_HOLD',
     '2025-10-01 09:00:00', '2026-06-30 18:00:00',
     'a0000000-0000-0000-0000-000000000001', false),

    -- john.doe (a...0002, continued)
    ('c0000000-0000-0000-0000-000000000024',
     'ERP System Migration',
     'Phased migration from SAP ECC to S/4HANA covering finance, procurement, and manufacturing modules with zero-downtime cutover.',
     'IN_PROGRESS',
     '2026-01-01 09:00:00', '2027-06-30 18:00:00',
     'a0000000-0000-0000-0000-000000000002', false),

    ('c0000000-0000-0000-0000-000000000025',
     'Data Warehouse Modernisation',
     'Deprecate the on-premise Teradata DW in favour of Snowflake; includes historical data migration and dbt model rewrite.',
     'CANCELLED',
     '2025-06-01 09:00:00', '2026-03-31 18:00:00',
     'a0000000-0000-0000-0000-000000000002', false)

ON CONFLICT (id) DO NOTHING;
