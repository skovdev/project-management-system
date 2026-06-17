-- =============================================================
-- Seed test data for notification-service
-- Database : project_management_system_notification_service_db
-- Table    : project_management_system_notification
--
-- Cross-service reference (logical, no DB-level FK):
--   user_id → auth-service: project_management_system_auth_user.id (authUserId from JWT)
--
-- user_id values MUST match the UUIDs seeded in auth-service.
--
-- notification_type enum: WELCOME | PROJECT_CREATED | TASK_CREATED
--
-- Run is idempotent: ON CONFLICT (id) DO NOTHING
-- =============================================================

-- ---------------------------------------------------------------
-- 1. WELCOME notifications  (one per user on account creation)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_notification
    (id, user_id, notification_type, title, message, read, deleted, created_at)
VALUES
    ('aa000000-0000-0000-0000-000000000001',
     'a0000000-0000-0000-0000-000000000001',
     'WELCOME',
     'Welcome to Project Management System',
     'Hi Admin, your account has been created successfully. Start by creating a project or exploring the dashboard.',
     true, false,
     '2026-01-01 08:00:00+00'),

    ('aa000000-0000-0000-0000-000000000002',
     'a0000000-0000-0000-0000-000000000002',
     'WELCOME',
     'Welcome to Project Management System',
     'Hi John, your account has been created successfully. Start by creating a project or exploring the dashboard.',
     true, false,
     '2026-01-01 08:05:00+00'),

    ('aa000000-0000-0000-0000-000000000003',
     'a0000000-0000-0000-0000-000000000003',
     'WELCOME',
     'Welcome to Project Management System',
     'Hi Jane, your account has been created successfully. Start by creating a project or exploring the dashboard.',
     false, false,
     '2026-01-01 08:10:00+00'),

    ('aa000000-0000-0000-0000-000000000004',
     'a0000000-0000-0000-0000-000000000004',
     'WELCOME',
     'Welcome to Project Management System',
     'Hi Bob, your account has been created successfully. Start by creating a project or exploring the dashboard.',
     false, false,
     '2026-01-01 08:15:00+00')

ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 2. PROJECT_CREATED notifications  (one per project owner)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_notification
    (id, user_id, notification_type, title, message, read, deleted, created_at)
VALUES
    ('aa000000-0000-0000-0000-000000000005',
     'a0000000-0000-0000-0000-000000000002',
     'PROJECT_CREATED',
     'Project Created: E-Commerce Platform Redesign',
     'Your project "E-Commerce Platform Redesign" has been created and is now IN_PROGRESS.',
     true, false,
     '2026-01-15 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000006',
     'a0000000-0000-0000-0000-000000000003',
     'PROJECT_CREATED',
     'Project Created: Mobile Banking Application',
     'Your project "Mobile Banking Application" has been created and is now in PLANNING.',
     true, false,
     '2026-03-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000007',
     'a0000000-0000-0000-0000-000000000004',
     'PROJECT_CREATED',
     'Project Created: HR Management System v2',
     'Your project "HR Management System v2" has been created and is now in PLANNING.',
     true, false,
     '2025-06-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000008',
     'a0000000-0000-0000-0000-000000000001',
     'PROJECT_CREATED',
     'Project Created: Data Analytics Dashboard',
     'Your project "Data Analytics Dashboard" has been created and is now in PLANNING.',
     false, false,
     '2025-09-01 09:05:00+00')

ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 3. TASK_CREATED notifications  (one per task assignee)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_notification
    (id, user_id, notification_type, title, message, read, deleted, created_at)
VALUES
    ('aa000000-0000-0000-0000-000000000009',
     'a0000000-0000-0000-0000-000000000002',
     'TASK_CREATED',
     'New Task Assigned: Implement Product Catalog API',
     'You have been assigned a new task "Implement Product Catalog API" with HIGH priority in project "E-Commerce Platform Redesign".',
     true, false,
     '2026-01-16 10:00:00+00'),

    ('aa000000-0000-0000-0000-000000000010',
     'a0000000-0000-0000-0000-000000000003',
     'TASK_CREATED',
     'New Task Assigned: Design Mobile UI Mockups',
     'You have been assigned a new task "Design Mobile UI Mockups" with MEDIUM priority in project "Mobile Banking Application".',
     false, false,
     '2026-03-02 10:00:00+00'),

    ('aa000000-0000-0000-0000-000000000011',
     'a0000000-0000-0000-0000-000000000004',
     'TASK_CREATED',
     'New Task Assigned: Migrate Employee Records to New Schema',
     'You have been assigned a new task "Migrate Employee Records to New Schema" with LOW priority in project "HR Management System v2".',
     true, false,
     '2025-06-02 10:00:00+00'),

    ('aa000000-0000-0000-0000-000000000012',
     'a0000000-0000-0000-0000-000000000001',
     'TASK_CREATED',
     'New Task Assigned: Configure BI Report Templates',
     'You have been assigned a new task "Configure BI Report Templates" with CRITICAL priority in project "Data Analytics Dashboard".',
     false, false,
     '2025-09-02 10:00:00+00')

ON CONFLICT (id) DO NOTHING;


-- ---------------------------------------------------------------
-- 5. PROJECT_CREATED notifications for 20 new projects (aa019–aa038)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_notification
    (id, user_id, notification_type, title, message, read, deleted, created_at)
VALUES
    ('aa000000-0000-0000-0000-000000000019',
     'a0000000-0000-0000-0000-000000000001', 'PROJECT_CREATED',
     'Project Created: DevOps Infrastructure Modernization',
     'Your project "DevOps Infrastructure Modernization" has been created and is now IN_PROGRESS.',
     true, false, '2026-02-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000020',
     'a0000000-0000-0000-0000-000000000001', 'PROJECT_CREATED',
     'Project Created: Customer Support Portal',
     'Your project "Customer Support Portal" has been created and is now in PLANNING.',
     false, false, '2026-04-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000021',
     'a0000000-0000-0000-0000-000000000002', 'PROJECT_CREATED',
     'Project Created: API Gateway Consolidation',
     'Your project "API Gateway Consolidation" has been created and is now IN_PROGRESS.',
     true, false, '2026-01-20 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000022',
     'a0000000-0000-0000-0000-000000000002', 'PROJECT_CREATED',
     'Project Created: Inventory Management System',
     'Your project "Inventory Management System" has been created and is now in PLANNING.',
     true, false, '2025-03-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000023',
     'a0000000-0000-0000-0000-000000000003', 'PROJECT_CREATED',
     'Project Created: IoT Fleet Monitoring Platform',
     'Your project "IoT Fleet Monitoring Platform" has been created and is now IN_PROGRESS.',
     true, false, '2026-01-10 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000024',
     'a0000000-0000-0000-0000-000000000003', 'PROJECT_CREATED',
     'Project Created: Healthcare Patient Portal',
     'Your project "Healthcare Patient Portal" has been created and is now in PLANNING.',
     false, false, '2026-05-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000025',
     'a0000000-0000-0000-0000-000000000004', 'PROJECT_CREATED',
     'Project Created: Supply Chain Optimization Tool',
     'Your project "Supply Chain Optimization Tool" has been created and is now IN_PROGRESS.',
     true, false, '2026-02-15 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000026',
     'a0000000-0000-0000-0000-000000000004', 'PROJECT_CREATED',
     'Project Created: Employee Self-Service Portal',
     'Your project "Employee Self-Service Portal" has been created and is now in PLANNING.',
     false, false, '2026-04-15 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000027',
     'a0000000-0000-0000-0000-000000000001', 'PROJECT_CREATED',
     'Project Created: AI-Powered Content Moderation',
     'Your project "AI-Powered Content Moderation" has been created and is now IN_PROGRESS.',
     true, false, '2026-03-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000028',
     'a0000000-0000-0000-0000-000000000001', 'PROJECT_CREATED',
     'Project Created: Real-Time Analytics Engine',
     'Your project "Real-Time Analytics Engine" has been created and is now in PLANNING.',
     false, false, '2026-05-15 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000029',
     'a0000000-0000-0000-0000-000000000002', 'PROJECT_CREATED',
     'Project Created: Cybersecurity Audit Platform',
     'Your project "Cybersecurity Audit Platform" has been created and is now IN_PROGRESS.',
     true, false, '2026-01-05 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000030',
     'a0000000-0000-0000-0000-000000000002', 'PROJECT_CREATED',
     'Project Created: Cloud Cost Optimisation Dashboard',
     'Your project "Cloud Cost Optimisation Dashboard" has been created and is now in PLANNING.',
     false, false, '2026-04-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000031',
     'a0000000-0000-0000-0000-000000000003', 'PROJECT_CREATED',
     'Project Created: Marketing Automation Suite',
     'Your project "Marketing Automation Suite" has been created and is now IN_PROGRESS.',
     true, false, '2026-02-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000032',
     'a0000000-0000-0000-0000-000000000003', 'PROJECT_CREATED',
     'Project Created: Customer Loyalty Program',
     'Your project "Customer Loyalty Program" has been created and is now in PLANNING.',
     true, false, '2025-04-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000033',
     'a0000000-0000-0000-0000-000000000004', 'PROJECT_CREATED',
     'Project Created: Fraud Detection System',
     'Your project "Fraud Detection System" has been created and is now IN_PROGRESS.',
     true, false, '2026-01-15 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000034',
     'a0000000-0000-0000-0000-000000000004', 'PROJECT_CREATED',
     'Project Created: Payment Gateway Integration',
     'Your project "Payment Gateway Integration" has been created and is now in PLANNING.',
     false, false, '2026-05-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000035',
     'a0000000-0000-0000-0000-000000000001', 'PROJECT_CREATED',
     'Project Created: Knowledge Management Platform',
     'Your project "Knowledge Management Platform" has been created and is now IN_PROGRESS.',
     true, false, '2026-02-10 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000036',
     'a0000000-0000-0000-0000-000000000001', 'PROJECT_CREATED',
     'Project Created: Remote Work Collaboration Tool',
     'Your project "Remote Work Collaboration Tool" has been created and is now in PLANNING.',
     true, false, '2025-10-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000037',
     'a0000000-0000-0000-0000-000000000002', 'PROJECT_CREATED',
     'Project Created: ERP System Migration',
     'Your project "ERP System Migration" has been created and is now IN_PROGRESS.',
     true, false, '2026-01-01 09:05:00+00'),

    ('aa000000-0000-0000-0000-000000000038',
     'a0000000-0000-0000-0000-000000000002', 'PROJECT_CREATED',
     'Project Created: Data Warehouse Modernisation',
     'Your project "Data Warehouse Modernisation" has been created and is now in PLANNING.',
     true, false, '2025-06-01 09:05:00+00')

ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 6. TASK_CREATED notifications for 80 new tasks (aa039–aa118)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_notification
    (id, user_id, notification_type, title, message, read, deleted, created_at)
VALUES
    -- c006 tasks (d005-d008)
    ('aa000000-0000-0000-0000-000000000039', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Set Up Kubernetes Cluster',
     'You have been assigned "Set Up Kubernetes Cluster" with HIGH priority in project "DevOps Infrastructure Modernization".',
     true, false, '2026-02-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000040', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Configure GitOps Pipeline with ArgoCD',
     'You have been assigned "Configure GitOps Pipeline with ArgoCD" with MEDIUM priority in project "DevOps Infrastructure Modernization".',
     false, false, '2026-02-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000041', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Centralise Logging with OpenSearch',
     'You have been assigned "Centralise Logging with OpenSearch" with CRITICAL priority in project "DevOps Infrastructure Modernization".',
     true, false, '2026-02-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000042', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Define Resource Quotas and Network Policies',
     'You have been assigned "Define Resource Quotas and Network Policies" with HIGH priority in project "DevOps Infrastructure Modernization".',
     false, false, '2026-02-02 10:03:00+00'),

    -- c007 tasks (d009-d012)
    ('aa000000-0000-0000-0000-000000000043', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Design Portal Information Architecture',
     'You have been assigned "Design Portal Information Architecture" with HIGH priority in project "Customer Support Portal".',
     false, false, '2026-04-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000044', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Set Up Ticketing Backend',
     'You have been assigned "Set Up Ticketing Backend" with MEDIUM priority in project "Customer Support Portal".',
     false, false, '2026-04-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000045', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Integrate Live Chat Service',
     'You have been assigned "Integrate Live Chat Service" with LOW priority in project "Customer Support Portal".',
     false, false, '2026-04-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000046', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Build AI FAQ Knowledge Base',
     'You have been assigned "Build AI FAQ Knowledge Base" with MEDIUM priority in project "Customer Support Portal".',
     false, false, '2026-04-02 10:03:00+00'),

    -- c008 tasks (d013-d016)
    ('aa000000-0000-0000-0000-000000000047', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Audit Existing Gateway Configurations',
     'You have been assigned "Audit Existing Gateway Configurations" with HIGH priority in project "API Gateway Consolidation".',
     true, false, '2026-01-21 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000048', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Provision Managed Gateway Infrastructure',
     'You have been assigned "Provision Managed Gateway Infrastructure" with MEDIUM priority in project "API Gateway Consolidation".',
     false, false, '2026-01-21 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000049', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Migrate Authentication Middleware',
     'You have been assigned "Migrate Authentication Middleware" with HIGH priority in project "API Gateway Consolidation".',
     true, false, '2026-01-21 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000050', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Implement Rate-Limiting and Circuit-Breaker Policies',
     'You have been assigned "Implement Rate-Limiting and Circuit-Breaker Policies" with CRITICAL priority in project "API Gateway Consolidation".',
     false, false, '2026-01-21 10:03:00+00'),

    -- c009 tasks (d017-d020)
    ('aa000000-0000-0000-0000-000000000051', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Implement Barcode Scanner Integration',
     'You have been assigned "Implement Barcode Scanner Integration" with HIGH priority in project "Inventory Management System".',
     true, false, '2025-03-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000052', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Build Automatic Reorder Trigger',
     'You have been assigned "Build Automatic Reorder Trigger" with MEDIUM priority in project "Inventory Management System".',
     true, false, '2025-03-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000053', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Supplier EDI Integration',
     'You have been assigned "Supplier EDI Integration" with LOW priority in project "Inventory Management System".',
     true, false, '2025-03-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000054', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Develop Stock Reporting Dashboard',
     'You have been assigned "Develop Stock Reporting Dashboard" with HIGH priority in project "Inventory Management System".',
     true, false, '2025-03-02 10:03:00+00'),

    -- c010 tasks (d021-d024)
    ('aa000000-0000-0000-0000-000000000055', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Ingest Telematics Data Stream',
     'You have been assigned "Ingest Telematics Data Stream" with HIGH priority in project "IoT Fleet Monitoring Platform".',
     true, false, '2026-01-11 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000056', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Build GPS Live Map View',
     'You have been assigned "Build GPS Live Map View" with MEDIUM priority in project "IoT Fleet Monitoring Platform".',
     false, false, '2026-01-11 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000057', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Implement Driver Behaviour Scoring',
     'You have been assigned "Implement Driver Behaviour Scoring" with CRITICAL priority in project "IoT Fleet Monitoring Platform".',
     true, false, '2026-01-11 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000058', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Fuel Consumption Anomaly Alerting',
     'You have been assigned "Fuel Consumption Anomaly Alerting" with MEDIUM priority in project "IoT Fleet Monitoring Platform".',
     false, false, '2026-01-11 10:03:00+00'),

    -- c011 tasks (d025-d028)
    ('aa000000-0000-0000-0000-000000000059', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: HIPAA Gap Analysis',
     'You have been assigned "HIPAA Gap Analysis" with HIGH priority in project "Healthcare Patient Portal".',
     false, false, '2026-05-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000060', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Patient Authentication Flow',
     'You have been assigned "Patient Authentication Flow" with MEDIUM priority in project "Healthcare Patient Portal".',
     false, false, '2026-05-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000061', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Lab Results Viewer',
     'You have been assigned "Lab Results Viewer" with LOW priority in project "Healthcare Patient Portal".',
     false, false, '2026-05-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000062', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Prescription Refill Request API',
     'You have been assigned "Prescription Refill Request API" with HIGH priority in project "Healthcare Patient Portal".',
     false, false, '2026-05-02 10:03:00+00'),

    -- c012 tasks (d029-d032)
    ('aa000000-0000-0000-0000-000000000063', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Demand Forecasting Model Integration',
     'You have been assigned "Demand Forecasting Model Integration" with CRITICAL priority in project "Supply Chain Optimization Tool".',
     true, false, '2026-02-16 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000064', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Supplier Risk Scoring Module',
     'You have been assigned "Supplier Risk Scoring Module" with HIGH priority in project "Supply Chain Optimization Tool".',
     false, false, '2026-02-16 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000065', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Logistics Cost Calculator',
     'You have been assigned "Logistics Cost Calculator" with MEDIUM priority in project "Supply Chain Optimization Tool".',
     true, false, '2026-02-16 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000066', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Executive Supply Chain Dashboard',
     'You have been assigned "Executive Supply Chain Dashboard" with LOW priority in project "Supply Chain Optimization Tool".',
     false, false, '2026-02-16 10:03:00+00'),

    -- c013 tasks (d033-d036)
    ('aa000000-0000-0000-0000-000000000067', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Leave Request Workflow',
     'You have been assigned "Leave Request Workflow" with HIGH priority in project "Employee Self-Service Portal".',
     false, false, '2026-04-16 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000068', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Payslip PDF Generation',
     'You have been assigned "Payslip PDF Generation" with MEDIUM priority in project "Employee Self-Service Portal".',
     false, false, '2026-04-16 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000069', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Personal Details Update Flow',
     'You have been assigned "Personal Details Update Flow" with MEDIUM priority in project "Employee Self-Service Portal".',
     false, false, '2026-04-16 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000070', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Company Policy Document Repository',
     'You have been assigned "Company Policy Document Repository" with LOW priority in project "Employee Self-Service Portal".',
     false, false, '2026-04-16 10:03:00+00'),

    -- c014 tasks (d037-d040)
    ('aa000000-0000-0000-0000-000000000071', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Train Multi-Label Classification Model',
     'You have been assigned "Train Multi-Label Classification Model" with HIGH priority in project "AI-Powered Content Moderation".',
     true, false, '2026-03-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000072', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Build Async Moderation Pipeline',
     'You have been assigned "Build Async Moderation Pipeline" with MEDIUM priority in project "AI-Powered Content Moderation".',
     false, false, '2026-03-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000073', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Human Review Queue Interface',
     'You have been assigned "Human Review Queue Interface" with BLOCKER priority in project "AI-Powered Content Moderation".',
     true, false, '2026-03-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000074', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Metrics and Bias Monitoring',
     'You have been assigned "Metrics and Bias Monitoring" with HIGH priority in project "AI-Powered Content Moderation".',
     false, false, '2026-03-02 10:03:00+00'),

    -- c015 tasks (d041-d044)
    ('aa000000-0000-0000-0000-000000000075', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Design Stream Processing Topology',
     'You have been assigned "Design Stream Processing Topology" with HIGH priority in project "Real-Time Analytics Engine".',
     false, false, '2026-05-16 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000076', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Evaluate Flink vs Kafka Streams',
     'You have been assigned "Evaluate Flink vs Kafka Streams" with MEDIUM priority in project "Real-Time Analytics Engine".',
     false, false, '2026-05-16 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000077', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Implement Backpressure Handling',
     'You have been assigned "Implement Backpressure Handling" with LOW priority in project "Real-Time Analytics Engine".',
     false, false, '2026-05-16 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000078', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Build Sub-Second Dashboard Prototype',
     'You have been assigned "Build Sub-Second Dashboard Prototype" with CRITICAL priority in project "Real-Time Analytics Engine".',
     false, false, '2026-05-16 10:03:00+00'),

    -- c016 tasks (d045-d048)
    ('aa000000-0000-0000-0000-000000000079', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Ingest SIEM Logs into Audit Store',
     'You have been assigned "Ingest SIEM Logs into Audit Store" with BLOCKER priority in project "Cybersecurity Audit Platform".',
     true, false, '2026-01-06 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000080', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: CVE Feed Synchronisation',
     'You have been assigned "CVE Feed Synchronisation" with HIGH priority in project "Cybersecurity Audit Platform".',
     false, false, '2026-01-06 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000081', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Compliance Report Generator',
     'You have been assigned "Compliance Report Generator" with CRITICAL priority in project "Cybersecurity Audit Platform".',
     true, false, '2026-01-06 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000082', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Vulnerability Ticket Auto-Creation',
     'You have been assigned "Vulnerability Ticket Auto-Creation" with MEDIUM priority in project "Cybersecurity Audit Platform".',
     false, false, '2026-01-06 10:03:00+00'),

    -- c017 tasks (d049-d052)
    ('aa000000-0000-0000-0000-000000000083', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Multi-Cloud Cost Data Aggregation',
     'You have been assigned "Multi-Cloud Cost Data Aggregation" with HIGH priority in project "Cloud Cost Optimisation Dashboard".',
     false, false, '2026-04-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000084', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Anomaly Detection on Cloud Spend',
     'You have been assigned "Anomaly Detection on Cloud Spend" with MEDIUM priority in project "Cloud Cost Optimisation Dashboard".',
     false, false, '2026-04-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000085', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Reserved-Instance Recommendation Engine',
     'You have been assigned "Reserved-Instance Recommendation Engine" with LOW priority in project "Cloud Cost Optimisation Dashboard".',
     false, false, '2026-04-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000086', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Team Budget Alert System',
     'You have been assigned "Team Budget Alert System" with MEDIUM priority in project "Cloud Cost Optimisation Dashboard".',
     false, false, '2026-04-02 10:03:00+00'),

    -- c018 tasks (d053-d056)
    ('aa000000-0000-0000-0000-000000000087', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Build Campaign Orchestration Engine',
     'You have been assigned "Build Campaign Orchestration Engine" with HIGH priority in project "Marketing Automation Suite".',
     true, false, '2026-02-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000088', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: A/B Test Framework',
     'You have been assigned "A/B Test Framework" with MEDIUM priority in project "Marketing Automation Suite".',
     false, false, '2026-02-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000089', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Personalisation Rules Engine',
     'You have been assigned "Personalisation Rules Engine" with HIGH priority in project "Marketing Automation Suite".',
     true, false, '2026-02-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000090', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Attribution Reporting Dashboard',
     'You have been assigned "Attribution Reporting Dashboard" with LOW priority in project "Marketing Automation Suite".',
     false, false, '2026-02-02 10:03:00+00'),

    -- c019 tasks (d057-d060)
    ('aa000000-0000-0000-0000-000000000091', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Points Accrual Engine',
     'You have been assigned "Points Accrual Engine" with HIGH priority in project "Customer Loyalty Program".',
     true, false, '2025-04-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000092', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Tier Management System',
     'You have been assigned "Tier Management System" with MEDIUM priority in project "Customer Loyalty Program".',
     true, false, '2025-04-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000093', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Reward Catalogue API',
     'You have been assigned "Reward Catalogue API" with LOW priority in project "Customer Loyalty Program".',
     true, false, '2025-04-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000094', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Partner Redemption Integration',
     'You have been assigned "Partner Redemption Integration" with MEDIUM priority in project "Customer Loyalty Program".',
     true, false, '2025-04-02 10:03:00+00'),

    -- c020 tasks (d061-d064)
    ('aa000000-0000-0000-0000-000000000095', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: Feature Engineering Pipeline',
     'You have been assigned "Feature Engineering Pipeline" with BLOCKER priority in project "Fraud Detection System".',
     true, false, '2026-01-16 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000096', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Model Serving Infrastructure',
     'You have been assigned "Model Serving Infrastructure" with CRITICAL priority in project "Fraud Detection System".',
     false, false, '2026-01-16 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000097', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Alert Triage Dashboard',
     'You have been assigned "Alert Triage Dashboard" with HIGH priority in project "Fraud Detection System".',
     true, false, '2026-01-16 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000098', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Adaptive Threshold Tuning',
     'You have been assigned "Adaptive Threshold Tuning" with MEDIUM priority in project "Fraud Detection System".',
     false, false, '2026-01-16 10:03:00+00'),

    -- c021 tasks (d065-d068)
    ('aa000000-0000-0000-0000-000000000099', 'a0000000-0000-0000-0000-000000000004', 'TASK_CREATED',
     'New Task Assigned: PCI-DSS Scope Assessment',
     'You have been assigned "PCI-DSS Scope Assessment" with HIGH priority in project "Payment Gateway Integration".',
     false, false, '2026-05-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000100', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Stripe Integration',
     'You have been assigned "Stripe Integration" with MEDIUM priority in project "Payment Gateway Integration".',
     false, false, '2026-05-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000101', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: PayPal and Adyen Integration',
     'You have been assigned "PayPal and Adyen Integration" with LOW priority in project "Payment Gateway Integration".',
     false, false, '2026-05-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000102', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Tokenisation Vault Design',
     'You have been assigned "Tokenisation Vault Design" with HIGH priority in project "Payment Gateway Integration".',
     false, false, '2026-05-02 10:03:00+00'),

    -- c022 tasks (d069-d072)
    ('aa000000-0000-0000-0000-000000000103', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Implement Semantic Search with Elasticsearch',
     'You have been assigned "Implement Semantic Search with Elasticsearch" with HIGH priority in project "Knowledge Management Platform".',
     true, false, '2026-02-11 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000104', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Document Version Control System',
     'You have been assigned "Document Version Control System" with MEDIUM priority in project "Knowledge Management Platform".',
     false, false, '2026-02-11 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000105', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: RBAC Permission Model',
     'You have been assigned "RBAC Permission Model" with MEDIUM priority in project "Knowledge Management Platform".',
     true, false, '2026-02-11 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000106', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Full-Text Index Sync Pipeline',
     'You have been assigned "Full-Text Index Sync Pipeline" with LOW priority in project "Knowledge Management Platform".',
     false, false, '2026-02-11 10:03:00+00'),

    -- c023 tasks (d073-d076)
    ('aa000000-0000-0000-0000-000000000107', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Video Room WebRTC Infrastructure',
     'You have been assigned "Video Room WebRTC Infrastructure" with HIGH priority in project "Remote Work Collaboration Tool".',
     true, false, '2025-10-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000108', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Threaded Discussion Board',
     'You have been assigned "Threaded Discussion Board" with MEDIUM priority in project "Remote Work Collaboration Tool".',
     true, false, '2025-10-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000109', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Shared Whiteboard Feature',
     'You have been assigned "Shared Whiteboard Feature" with LOW priority in project "Remote Work Collaboration Tool".',
     true, false, '2025-10-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000110', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Google Calendar Integration',
     'You have been assigned "Google Calendar Integration" with MEDIUM priority in project "Remote Work Collaboration Tool".',
     true, false, '2025-10-02 10:03:00+00'),

    -- c024 tasks (d077-d080)
    ('aa000000-0000-0000-0000-000000000111', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: SAP Data Extraction and Transformation',
     'You have been assigned "SAP Data Extraction and Transformation" with BLOCKER priority in project "ERP System Migration".',
     true, false, '2026-01-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000112', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Cutover Runbook Preparation',
     'You have been assigned "Cutover Runbook Preparation" with HIGH priority in project "ERP System Migration".',
     false, false, '2026-01-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000113', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Integration Testing in S/4HANA Sandbox',
     'You have been assigned "Integration Testing in S/4HANA Sandbox" with CRITICAL priority in project "ERP System Migration".',
     true, false, '2026-01-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000114', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: End-User Training Delivery',
     'You have been assigned "End-User Training Delivery" with MEDIUM priority in project "ERP System Migration".',
     false, false, '2026-01-02 10:03:00+00'),

    -- c025 tasks (d081-d084)
    ('aa000000-0000-0000-0000-000000000115', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: Teradata Schema Inventory',
     'You have been assigned "Teradata Schema Inventory" with HIGH priority in project "Data Warehouse Modernisation".',
     true, false, '2025-06-02 10:00:00+00'),
    ('aa000000-0000-0000-0000-000000000116', 'a0000000-0000-0000-0000-000000000001', 'TASK_CREATED',
     'New Task Assigned: Snowflake Account Setup',
     'You have been assigned "Snowflake Account Setup" with MEDIUM priority in project "Data Warehouse Modernisation".',
     true, false, '2025-06-02 10:01:00+00'),
    ('aa000000-0000-0000-0000-000000000117', 'a0000000-0000-0000-0000-000000000002', 'TASK_CREATED',
     'New Task Assigned: dbt Model Rewrite Spike',
     'You have been assigned "dbt Model Rewrite Spike" with LOW priority in project "Data Warehouse Modernisation".',
     true, false, '2025-06-02 10:02:00+00'),
    ('aa000000-0000-0000-0000-000000000118', 'a0000000-0000-0000-0000-000000000003', 'TASK_CREATED',
     'New Task Assigned: Historical Data Migration Proof of Concept',
     'You have been assigned "Historical Data Migration Proof of Concept" with HIGH priority in project "Data Warehouse Modernisation".',
     true, false, '2025-06-02 10:03:00+00')

ON CONFLICT (id) DO NOTHING;
