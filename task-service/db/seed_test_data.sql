-- =============================================================
-- Seed test data for task-service
-- Database : project_management_system_task_service_db
-- Tables   : project_management_system_task
--            project_management_system_task_comment
--
-- Cross-service references (logical, no DB-level FKs):
--   project_id → project-service: project_management_system_project.id
--   user_id    → auth-service:    project_management_system_auth_user.id (authUserId from JWT)
--   author_id  → auth-service:    project_management_system_auth_user.id (authUserId from JWT)
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
     'a0000000-0000-0000-0000-000000000002',
     false),

    -- Mobile Banking Application (c...002), assigned to jane.smith (b...0003)
    ('d0000000-0000-0000-0000-000000000002',
     'Design Mobile UI Mockups',
     'Create high-fidelity Figma mockups for account overview, transaction history, and fund transfer screens.',
     'TODO', 'MEDIUM',
     true,
     'c0000000-0000-0000-0000-000000000002',
     'a0000000-0000-0000-0000-000000000003',
     false),

    -- HR Management System v2 (c...003), assigned to bob.jones (b...0004)
    ('d0000000-0000-0000-0000-000000000003',
     'Migrate Employee Records to New Schema',
     'Extract, transform, and load all employee records from the legacy HR database into the v2 schema with data validation.',
     'DONE', 'LOW',
     false,
     'c0000000-0000-0000-0000-000000000003',
     'a0000000-0000-0000-0000-000000000004',
     false),

    -- Data Analytics Dashboard (c...004), assigned to admin (b...0001)
    ('d0000000-0000-0000-0000-000000000004',
     'Configure BI Report Templates',
     'Set up executive report templates in the BI tool: monthly KPI summary, sales funnel, and support SLA compliance.',
     'ON_HOLD', 'CRITICAL',
     false,
     'c0000000-0000-0000-0000-000000000004',
     'a0000000-0000-0000-0000-000000000001',
     false)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 2. Task comments  (a few comments per active task)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_task_comment
    (id, content, task_id, author_id, created_at, updated_at, deleted)
VALUES
    ('bb000000-0000-0000-0000-000000000001',
     'Started with the GET /products endpoint. Using Spring Data pagination — should be ready for review by end of week.',
     'd0000000-0000-0000-0000-000000000001',
     'a0000000-0000-0000-0000-000000000002',
     '2026-01-17 09:30:00+00', '2026-01-17 09:30:00+00', false),

    ('bb000000-0000-0000-0000-000000000002',
     'Make sure to add OpenAPI docs for each endpoint. The frontend team needs the spec before they can start integration.',
     'd0000000-0000-0000-0000-000000000001',
     'a0000000-0000-0000-0000-000000000001',
     '2026-01-18 11:00:00+00', '2026-01-18 11:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000003',
     'Added category filtering and search. PR up for review — please take a look when you get a chance.',
     'd0000000-0000-0000-0000-000000000001',
     'a0000000-0000-0000-0000-000000000002',
     '2026-01-22 14:45:00+00', '2026-01-22 14:45:00+00', false),

    ('bb000000-0000-0000-0000-000000000004',
     'Kicked off the account overview screen in Figma. Sharing the draft link with the team for early feedback.',
     'd0000000-0000-0000-0000-000000000002',
     'a0000000-0000-0000-0000-000000000003',
     '2026-03-03 10:00:00+00', '2026-03-03 10:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000005',
     'The transaction history screen looks good. Can we add a filter panel on the right side for date range and amount?',
     'd0000000-0000-0000-0000-000000000002',
     'a0000000-0000-0000-0000-000000000001',
     '2026-03-05 15:20:00+00', '2026-03-05 15:20:00+00', false)

ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 3. Additional tasks  (4 per new project, 20 projects = 80 tasks)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_task
    (id, title, description,
     task_status, task_priority,
     active, project_id, user_id, deleted)
VALUES
    -- DevOps Infrastructure Modernization (c006, admin)
    ('d0000000-0000-0000-0000-000000000005',
     'Set Up Kubernetes Cluster',
     'Provision a production-grade K8s cluster on AWS EKS with multi-AZ node groups, autoscaling, and RBAC configuration.',
     'IN_PROGRESS', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000006',
     'Configure GitOps Pipeline with ArgoCD',
     'Install ArgoCD, define ApplicationSet manifests, and migrate three existing services to GitOps-managed deployments.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000007',
     'Centralise Logging with OpenSearch',
     'Deploy OpenSearch cluster, configure Fluent Bit DaemonSet, and migrate from the legacy ELK stack.',
     'IN_PROGRESS', 'CRITICAL', true,
     'c0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000008',
     'Define Resource Quotas and Network Policies',
     'Establish namespace-level resource quotas and Kubernetes NetworkPolicy rules to enforce least-privilege pod traffic.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000004', false),

    -- Customer Support Portal (c007, admin)
    ('d0000000-0000-0000-0000-000000000009',
     'Design Portal Information Architecture',
     'Map user journeys, define site structure, and produce lo-fi wireframes for ticket creation, live chat, and FAQ flows.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000010',
     'Set Up Ticketing Backend',
     'Implement ticket entity, service layer, and REST API with SLA tracking and priority routing.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000011',
     'Integrate Live Chat Service',
     'Evaluate and integrate a live chat provider (Intercom or Crisp) via webhook relay into the portal backend.',
     'TODO', 'LOW', true,
     'c0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000012',
     'Build AI FAQ Knowledge Base',
     'Index documentation with a vector store and expose a semantic search API to power FAQ suggestions in the support portal.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000004', false),

    -- API Gateway Consolidation (c008, john.doe)
    ('d0000000-0000-0000-0000-000000000013',
     'Audit Existing Gateway Configurations',
     'Document all route rules, auth middleware, and rate-limit settings from the three legacy gateway instances.',
     'IN_PROGRESS', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000008', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000014',
     'Provision Managed Gateway Infrastructure',
     'Deploy Kong Gateway on Kubernetes with Helm, configure Postgres backend, and verify HA readiness.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000008', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000015',
     'Migrate Authentication Middleware',
     'Port JWT validation plugins and API-key management from the legacy gateways to Kong consumer groups.',
     'IN_PROGRESS', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000008', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000016',
     'Implement Rate-Limiting and Circuit-Breaker Policies',
     'Define rate-limit tiers per consumer group and configure Resilience4j circuit breakers on all upstream services.',
     'TODO', 'CRITICAL', true,
     'c0000000-0000-0000-0000-000000000008', 'a0000000-0000-0000-0000-000000000001', false),

    -- Inventory Management System (c009, john.doe — COMPLETED)
    ('d0000000-0000-0000-0000-000000000017',
     'Implement Barcode Scanner Integration',
     'Completed integration of Zebra TC21 handheld scanners via MQTT bridge to the inventory service.',
     'DONE', 'HIGH', false,
     'c0000000-0000-0000-0000-000000000009', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000018',
     'Build Automatic Reorder Trigger',
     'Implemented rule engine that fires purchase-order events when stock falls below configurable thresholds.',
     'DONE', 'MEDIUM', false,
     'c0000000-0000-0000-0000-000000000009', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000019',
     'Supplier EDI Integration',
     'Connected to top-5 suppliers via AS2 EDI; mapped 850/855/856 transactions to internal order lifecycle events.',
     'DONE', 'LOW', false,
     'c0000000-0000-0000-0000-000000000009', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000020',
     'Develop Stock Reporting Dashboard',
     'Delivered Grafana-based dashboard covering turnover rate, dead stock, and shrinkage by warehouse zone.',
     'DONE', 'HIGH', false,
     'c0000000-0000-0000-0000-000000000009', 'a0000000-0000-0000-0000-000000000001', false),

    -- IoT Fleet Monitoring Platform (c010, jane.smith)
    ('d0000000-0000-0000-0000-000000000021',
     'Ingest Telematics Data Stream',
     'Set up Kafka topic for vehicle telemetry, define Avro schema, and implement consumer that persists readings to TimescaleDB.',
     'IN_PROGRESS', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000010', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000022',
     'Build GPS Live Map View',
     'Integrate Mapbox GL JS to render vehicle positions updated every 5 seconds from the WebSocket push endpoint.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000010', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000023',
     'Implement Driver Behaviour Scoring',
     'Compute harsh-braking, cornering, and speeding scores from telemetry events using a configurable threshold engine.',
     'IN_PROGRESS', 'CRITICAL', true,
     'c0000000-0000-0000-0000-000000000010', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000024',
     'Fuel Consumption Anomaly Alerting',
     'Detect statistically significant deviations in per-vehicle fuel consumption and trigger alerts via notification-service.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000010', 'a0000000-0000-0000-0000-000000000002', false),

    -- Healthcare Patient Portal (c011, jane.smith — PLANNING)
    ('d0000000-0000-0000-0000-000000000025',
     'HIPAA Gap Analysis',
     'Conduct a formal gap analysis against HIPAA Security Rule controls and produce a prioritised remediation backlog.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000011', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000026',
     'Patient Authentication Flow',
     'Design and implement MFA-backed patient login with identity verification meeting NIST 800-63B AAL2 requirements.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000011', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000027',
     'Lab Results Viewer',
     'Build a read-only lab result viewer with HL7 FHIR R4 integration from the hospital Laboratory Information System.',
     'TODO', 'LOW', true,
     'c0000000-0000-0000-0000-000000000011', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000028',
     'Prescription Refill Request API',
     'Implement refill request flow with pharmacy routing logic and real-time status tracking visible to patients.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000011', 'a0000000-0000-0000-0000-000000000002', false),

    -- Supply Chain Optimization Tool (c012, bob.jones)
    ('d0000000-0000-0000-0000-000000000029',
     'Demand Forecasting Model Integration',
     'Integrate pre-trained LSTM demand forecasting model via REST API and wire predictions into replenishment planning.',
     'IN_PROGRESS', 'CRITICAL', true,
     'c0000000-0000-0000-0000-000000000012', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000030',
     'Supplier Risk Scoring Module',
     'Aggregate delivery SLA history, financial health data, and geopolitical risk indices into per-supplier risk scores.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000012', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000031',
     'Logistics Cost Calculator',
     'Build cost optimisation engine comparing route options, carrier rates, and customs duty profiles for outbound shipments.',
     'IN_PROGRESS', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000012', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000032',
     'Executive Supply Chain Dashboard',
     'Deliver a Recharts-based dashboard showing end-to-end supply chain KPIs and on-time-in-full metrics for C-level reporting.',
     'TODO', 'LOW', true,
     'c0000000-0000-0000-0000-000000000012', 'a0000000-0000-0000-0000-000000000003', false),

    -- Employee Self-Service Portal (c013, bob.jones — PLANNING)
    ('d0000000-0000-0000-0000-000000000033',
     'Leave Request Workflow',
     'Design multi-step approval workflow for leave requests with manager delegation and calendar conflict detection.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000013', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000034',
     'Payslip PDF Generation',
     'Generate monthly payslip PDFs from payroll data using JasperReports and store in S3 for employee self-service download.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000013', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000035',
     'Personal Details Update Flow',
     'Allow employees to update contact info, bank details, and emergency contacts with a manager confirmation step.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000013', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000036',
     'Company Policy Document Repository',
     'Build searchable policy document library with version tracking and mandatory per-document acknowledgement tracking.',
     'TODO', 'LOW', true,
     'c0000000-0000-0000-0000-000000000013', 'a0000000-0000-0000-0000-000000000003', false),

    -- AI-Powered Content Moderation (c014, admin)
    ('d0000000-0000-0000-0000-000000000037',
     'Train Multi-Label Classification Model',
     'Fine-tune a DistilBERT checkpoint on the internal moderation dataset covering 12 policy violation categories.',
     'IN_PROGRESS', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000014', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000038',
     'Build Async Moderation Pipeline',
     'Implement Kafka consumer that routes content to the ML scoring service and writes verdicts back to the audit table.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000014', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000039',
     'Human Review Queue Interface',
     'Build a React-based review dashboard where moderators handle low-confidence model predictions with keyboard shortcuts.',
     'IN_PROGRESS', 'BLOCKER', true,
     'c0000000-0000-0000-0000-000000000014', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000040',
     'Metrics and Bias Monitoring',
     'Set up MLflow experiment tracking and a Grafana dashboard for precision/recall drift and demographic parity metrics.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000014', 'a0000000-0000-0000-0000-000000000004', false),

    -- Real-Time Analytics Engine (c015, admin — PLANNING)
    ('d0000000-0000-0000-0000-000000000041',
     'Design Stream Processing Topology',
     'Define Kafka Streams topology for sessionisation, windowed aggregation, and late-arrival event handling.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000015', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000042',
     'Evaluate Flink vs Kafka Streams',
     'Benchmark both frameworks against throughput and latency requirements; produce ADR with recommendation.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000015', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000043',
     'Implement Backpressure Handling',
     'Design and test backpressure propagation to prevent consumer lag from cascading into the upstream producer.',
     'TODO', 'LOW', true,
     'c0000000-0000-0000-0000-000000000015', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000044',
     'Build Sub-Second Dashboard Prototype',
     'Deliver a proof-of-concept dashboard consuming from the stream engine with end-to-end latency under 1 second.',
     'TODO', 'CRITICAL', true,
     'c0000000-0000-0000-0000-000000000015', 'a0000000-0000-0000-0000-000000000004', false),

    -- Cybersecurity Audit Platform (c016, john.doe)
    ('d0000000-0000-0000-0000-000000000045',
     'Ingest SIEM Logs into Audit Store',
     'Configure Filebeat-to-Kafka pipeline for SIEM log ingestion; normalise events to Elastic Common Schema (ECS).',
     'IN_PROGRESS', 'BLOCKER', true,
     'c0000000-0000-0000-0000-000000000016', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000046',
     'CVE Feed Synchronisation',
     'Schedule daily NVD CVE feed pulls, parse affected CPE entries, and cross-reference against deployed service versions.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000016', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000047',
     'Compliance Report Generator',
     'Implement templated PDF report generation for SOC 2 Type II, PCI-DSS, and ISO 27001 evidence packages.',
     'IN_PROGRESS', 'CRITICAL', true,
     'c0000000-0000-0000-0000-000000000016', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000048',
     'Vulnerability Ticket Auto-Creation',
     'Auto-create prioritised remediation tickets from new critical/high CVE matches and assign to the owning engineering team.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000016', 'a0000000-0000-0000-0000-000000000001', false),

    -- Cloud Cost Optimisation Dashboard (c017, john.doe — PLANNING)
    ('d0000000-0000-0000-0000-000000000049',
     'Multi-Cloud Cost Data Aggregation',
     'Pull cost and usage reports from AWS, Azure, and GCP into a unified Parquet data lake partitioned by account and service.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000017', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000050',
     'Anomaly Detection on Cloud Spend',
     'Implement statistical spend anomaly detection using ARIMA baseline to alert on unexpected cost spikes within 1 hour.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000017', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000051',
     'Reserved-Instance Recommendation Engine',
     'Analyse usage patterns and recommend optimal RI coverage per service targeting 30 % reduction in compute spend.',
     'TODO', 'LOW', true,
     'c0000000-0000-0000-0000-000000000017', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000052',
     'Team Budget Alert System',
     'Allow finance to set per-team monthly budgets; trigger Slack and email alerts at 80 % and 100 % spend thresholds.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000017', 'a0000000-0000-0000-0000-000000000001', false),

    -- Marketing Automation Suite (c018, jane.smith)
    ('d0000000-0000-0000-0000-000000000053',
     'Build Campaign Orchestration Engine',
     'Implement a directed-graph workflow engine where marketers define multi-step drip campaigns with conditional branching.',
     'IN_PROGRESS', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000018', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000054',
     'A/B Test Framework',
     'Build variant assignment, metric collection, and statistical significance testing for email subject lines and CTAs.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000018', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000055',
     'Personalisation Rules Engine',
     'Implement audience segmentation and content personalisation based on user behaviour, demographics, and purchase history.',
     'IN_PROGRESS', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000018', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000056',
     'Attribution Reporting Dashboard',
     'Build first-touch, last-touch, and linear attribution models and expose them in the campaign analytics dashboard.',
     'TODO', 'LOW', true,
     'c0000000-0000-0000-0000-000000000018', 'a0000000-0000-0000-0000-000000000002', false),

    -- Customer Loyalty Program (c019, jane.smith — COMPLETED)
    ('d0000000-0000-0000-0000-000000000057',
     'Points Accrual Engine',
     'Delivered configurable rule engine mapping purchase events to points awards with double-points promotional support.',
     'DONE', 'HIGH', false,
     'c0000000-0000-0000-0000-000000000019', 'a0000000-0000-0000-0000-000000000003', false),

    ('d0000000-0000-0000-0000-000000000058',
     'Tier Management System',
     'Implemented Bronze/Silver/Gold/Platinum tier thresholds with automatic upgrade/downgrade on rolling 12-month spend.',
     'DONE', 'MEDIUM', false,
     'c0000000-0000-0000-0000-000000000019', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000059',
     'Reward Catalogue API',
     'Built REST API for the reward catalogue with stock management, eligibility filtering, and points redemption flow.',
     'DONE', 'LOW', false,
     'c0000000-0000-0000-0000-000000000019', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000060',
     'Partner Redemption Integration',
     'Integrated two airline and one hotel partner redemption APIs with points-to-currency conversion and confirmation webhooks.',
     'DONE', 'MEDIUM', false,
     'c0000000-0000-0000-0000-000000000019', 'a0000000-0000-0000-0000-000000000002', false),

    -- Fraud Detection System (c020, bob.jones)
    ('d0000000-0000-0000-0000-000000000061',
     'Feature Engineering Pipeline',
     'Build real-time feature extraction from the transaction stream: velocity checks, amount z-score, device fingerprint, and geolocation deviation.',
     'IN_PROGRESS', 'BLOCKER', true,
     'c0000000-0000-0000-0000-000000000020', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000062',
     'Model Serving Infrastructure',
     'Deploy XGBoost scoring model behind a low-latency REST API with P99 < 20 ms SLA using Triton Inference Server.',
     'TODO', 'CRITICAL', true,
     'c0000000-0000-0000-0000-000000000020', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000063',
     'Alert Triage Dashboard',
     'Build an analyst dashboard showing triggered fraud alerts with transaction context, historical patterns, and one-click case creation.',
     'IN_PROGRESS', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000020', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000064',
     'Adaptive Threshold Tuning',
     'Implement feedback loop where analyst decisions retrain threshold parameters weekly via the MLflow model registry.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000020', 'a0000000-0000-0000-0000-000000000001', false),

    -- Payment Gateway Integration (c021, bob.jones — PLANNING)
    ('d0000000-0000-0000-0000-000000000065',
     'PCI-DSS Scope Assessment',
     'Define the cardholder data environment scope, identify all data flows, and produce a network segmentation diagram.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000021', 'a0000000-0000-0000-0000-000000000004', false),

    ('d0000000-0000-0000-0000-000000000066',
     'Stripe Integration',
     'Implement Stripe PaymentIntent flow with 3DS2, webhook signature validation, and idempotency key management.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000021', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000067',
     'PayPal and Adyen Integration',
     'Implement PayPal Orders API and Adyen drop-in payments with a unified normalised result model across providers.',
     'TODO', 'LOW', true,
     'c0000000-0000-0000-0000-000000000021', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000068',
     'Tokenisation Vault Design',
     'Design PCI-compliant tokenisation vault using HashiCorp Vault Transit secrets engine to eliminate PANs from application storage.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000021', 'a0000000-0000-0000-0000-000000000001', false),

    -- Knowledge Management Platform (c022, admin)
    ('d0000000-0000-0000-0000-000000000069',
     'Implement Semantic Search with Elasticsearch',
     'Configure dense vector search using the ELSER model to support natural-language document queries across the knowledge base.',
     'IN_PROGRESS', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000022', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000070',
     'Document Version Control System',
     'Build version history with diff viewer, rollback capability, and change authorship tracking per document section.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000022', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000071',
     'RBAC Permission Model',
     'Implement space-level and page-level permissions with inheritance, group membership, and guest access controls.',
     'IN_PROGRESS', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000022', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000072',
     'Full-Text Index Sync Pipeline',
     'Set up incremental Debezium CDC pipeline to keep the Elasticsearch index in sync with the PostgreSQL document store.',
     'TODO', 'LOW', true,
     'c0000000-0000-0000-0000-000000000022', 'a0000000-0000-0000-0000-000000000002', false),

    -- Remote Work Collaboration Tool (c023, admin — ON_HOLD)
    ('d0000000-0000-0000-0000-000000000073',
     'Video Room WebRTC Infrastructure',
     'Planned implementation of a self-hosted Jitsi-based WebRTC room service — on hold pending infrastructure cost review.',
     'ON_HOLD', 'HIGH', false,
     'c0000000-0000-0000-0000-000000000023', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000074',
     'Threaded Discussion Board',
     'Planned thread-and-reply model with rich text editing, @mentions, and emoji reactions — deferred pending go/no-go decision.',
     'ON_HOLD', 'MEDIUM', false,
     'c0000000-0000-0000-0000-000000000023', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000075',
     'Shared Whiteboard Feature',
     'Planned real-time collaborative canvas using the tldraw library — blocked on open-source licensing review.',
     'ON_HOLD', 'LOW', false,
     'c0000000-0000-0000-0000-000000000023', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000076',
     'Google Calendar Integration',
     'Planned OAuth2 calendar sync for scheduling video rooms directly from discussion threads — on hold with the project.',
     'ON_HOLD', 'MEDIUM', false,
     'c0000000-0000-0000-0000-000000000023', 'a0000000-0000-0000-0000-000000000002', false),

    -- ERP System Migration (c024, john.doe)
    ('d0000000-0000-0000-0000-000000000077',
     'SAP Data Extraction and Transformation',
     'Extract and transform master data (GL accounts, cost centres, vendors, customers) from ECC using the LTMC migration cockpit.',
     'IN_PROGRESS', 'BLOCKER', true,
     'c0000000-0000-0000-0000-000000000024', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000078',
     'Cutover Runbook Preparation',
     'Document step-by-step cutover plan with rollback checkpoints, team assignments, and go/no-go decision criteria per phase.',
     'TODO', 'HIGH', true,
     'c0000000-0000-0000-0000-000000000024', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000079',
     'Integration Testing in S/4HANA Sandbox',
     'Execute integration test cycles for procure-to-pay and order-to-cash processes in the S/4HANA sandbox environment.',
     'IN_PROGRESS', 'CRITICAL', true,
     'c0000000-0000-0000-0000-000000000024', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000080',
     'End-User Training Delivery',
     'Deliver role-based S/4HANA Fiori training to 250 finance and procurement users across three global regions.',
     'TODO', 'MEDIUM', true,
     'c0000000-0000-0000-0000-000000000024', 'a0000000-0000-0000-0000-000000000003', false),

    -- Data Warehouse Modernisation (c025, john.doe — CANCELLED)
    ('d0000000-0000-0000-0000-000000000081',
     'Teradata Schema Inventory',
     'Catalogued all 1,200 tables, identified 340 active models, and documented full lineage before project cancellation.',
     'CANCELLED', 'HIGH', false,
     'c0000000-0000-0000-0000-000000000025', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000082',
     'Snowflake Account Setup',
     'Provisioned Snowflake Enterprise trial and defined virtual warehouse sizing — cancelled before full validation completed.',
     'CANCELLED', 'MEDIUM', false,
     'c0000000-0000-0000-0000-000000000025', 'a0000000-0000-0000-0000-000000000001', false),

    ('d0000000-0000-0000-0000-000000000083',
     'dbt Model Rewrite Spike',
     'Began rewriting 40 % of SQL transforms as dbt models before the project was deprioritised and formally cancelled.',
     'CANCELLED', 'LOW', false,
     'c0000000-0000-0000-0000-000000000025', 'a0000000-0000-0000-0000-000000000002', false),

    ('d0000000-0000-0000-0000-000000000084',
     'Historical Data Migration Proof of Concept',
     'Loaded 6 months of historical data into Snowflake staging area to validate ETL performance — work cancelled mid-execution.',
     'CANCELLED', 'HIGH', false,
     'c0000000-0000-0000-0000-000000000025', 'a0000000-0000-0000-0000-000000000003', false)

ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------
-- 4. Additional task comments  (4 per IN_PROGRESS project = 40 comments)
-- ---------------------------------------------------------------
INSERT INTO project_management_system_task_comment
    (id, content, task_id, author_id, created_at, updated_at, deleted)
VALUES
    -- c006 / DevOps Infrastructure Modernization
    ('bb000000-0000-0000-0000-000000000006',
     'EKS cluster is up with 3 node groups. Still tuning HPA thresholds — will share the config PR by tomorrow.',
     'd0000000-0000-0000-0000-000000000005',
     'a0000000-0000-0000-0000-000000000001',
     '2026-02-05 10:00:00+00', '2026-02-05 10:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000007',
     'RBAC roles look good but we need to add the cluster-admin binding for the CI service account before the pipeline can run.',
     'd0000000-0000-0000-0000-000000000005',
     'a0000000-0000-0000-0000-000000000002',
     '2026-02-06 14:30:00+00', '2026-02-06 14:30:00+00', false),

    ('bb000000-0000-0000-0000-000000000008',
     'OpenSearch is deployed and Fluent Bit is shipping logs. Index retention policy set to 30 days — let me know if that needs adjusting.',
     'd0000000-0000-0000-0000-000000000007',
     'a0000000-0000-0000-0000-000000000003',
     '2026-02-10 09:15:00+00', '2026-02-10 09:15:00+00', false),

    ('bb000000-0000-0000-0000-000000000009',
     'Found some gaps in the dashboard for auth-service traces. Can we add a correlation ID field to the log schema?',
     'd0000000-0000-0000-0000-000000000007',
     'a0000000-0000-0000-0000-000000000001',
     '2026-02-11 16:00:00+00', '2026-02-11 16:00:00+00', false),

    -- c008 / API Gateway Consolidation
    ('bb000000-0000-0000-0000-000000000010',
     'Completed audit of gateway-1 and gateway-2. gateway-3 has 4 undocumented routes — will sort those out today.',
     'd0000000-0000-0000-0000-000000000013',
     'a0000000-0000-0000-0000-000000000002',
     '2026-01-22 10:30:00+00', '2026-01-22 10:30:00+00', false),

    ('bb000000-0000-0000-0000-000000000011',
     'The undocumented routes were leftover from the payments v1 pilot. I''ve added them to the migration backlog.',
     'd0000000-0000-0000-0000-000000000013',
     'a0000000-0000-0000-0000-000000000004',
     '2026-01-23 09:00:00+00', '2026-01-23 09:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000012',
     'JWT plugin is migrated and tested against 5 services. API-key consumers still need mapping — should be done by Friday.',
     'd0000000-0000-0000-0000-000000000015',
     'a0000000-0000-0000-0000-000000000004',
     '2026-02-03 11:00:00+00', '2026-02-03 11:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000013',
     'Heads up: the payments service uses a non-standard HS512 key — we need a dedicated Kong plugin for that.',
     'd0000000-0000-0000-0000-000000000015',
     'a0000000-0000-0000-0000-000000000002',
     '2026-02-04 13:45:00+00', '2026-02-04 13:45:00+00', false),

    -- c010 / IoT Fleet Monitoring Platform
    ('bb000000-0000-0000-0000-000000000014',
     'Kafka topic created and Avro schema registered in Schema Registry. Consumer persisting at ~12 k events/s without lag.',
     'd0000000-0000-0000-0000-000000000021',
     'a0000000-0000-0000-0000-000000000003',
     '2026-01-15 09:45:00+00', '2026-01-15 09:45:00+00', false),

    ('bb000000-0000-0000-0000-000000000015',
     'TimescaleDB hypertable chunk interval set to 1 day. Seeing good compression ratios already — query latency is under 5 ms.',
     'd0000000-0000-0000-0000-000000000021',
     'a0000000-0000-0000-0000-000000000004',
     '2026-01-18 15:00:00+00', '2026-01-18 15:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000016',
     'Harsh-braking detector works well. Need to calibrate the cornering threshold — currently flags too many false positives on tight roundabouts.',
     'd0000000-0000-0000-0000-000000000023',
     'a0000000-0000-0000-0000-000000000001',
     '2026-01-25 12:30:00+00', '2026-01-25 12:30:00+00', false),

    ('bb000000-0000-0000-0000-000000000017',
     'Tested against a 200-vehicle subset. Scores look sensible. I''ll share the calibration spreadsheet for sign-off before prod rollout.',
     'd0000000-0000-0000-0000-000000000023',
     'a0000000-0000-0000-0000-000000000003',
     '2026-01-27 08:00:00+00', '2026-01-27 08:00:00+00', false),

    -- c012 / Supply Chain Optimization Tool
    ('bb000000-0000-0000-0000-000000000018',
     'LSTM API is integrated. Forecast accuracy on the validation set is 87 % MAPE — better than the 80 % target.',
     'd0000000-0000-0000-0000-000000000029',
     'a0000000-0000-0000-0000-000000000004',
     '2026-02-20 10:00:00+00', '2026-02-20 10:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000019',
     'Replenishment planning is consuming the forecasts. I noticed lead times are missing for 3 suppliers — can procurement fill those in?',
     'd0000000-0000-0000-0000-000000000029',
     'a0000000-0000-0000-0000-000000000001',
     '2026-02-22 14:00:00+00', '2026-02-22 14:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000020',
     'Route optimiser picks FedEx Ground over DHL for 60 % of lanes — saving approx. $18 k/month. Showing it to the logistics team tomorrow.',
     'd0000000-0000-0000-0000-000000000031',
     'a0000000-0000-0000-0000-000000000002',
     '2026-03-01 11:30:00+00', '2026-03-01 11:30:00+00', false),

    ('bb000000-0000-0000-0000-000000000021',
     'Customs duty profiles are incomplete for APAC lanes. I''ll pull the HS code mappings from the tariff API this week.',
     'd0000000-0000-0000-0000-000000000031',
     'a0000000-0000-0000-0000-000000000004',
     '2026-03-02 09:00:00+00', '2026-03-02 09:00:00+00', false),

    -- c014 / AI-Powered Content Moderation
    ('bb000000-0000-0000-0000-000000000022',
     'First fine-tuning run complete. Macro F1 at 0.81 across 12 categories — hate-speech class is the weakest at 0.74.',
     'd0000000-0000-0000-0000-000000000037',
     'a0000000-0000-0000-0000-000000000001',
     '2026-03-10 10:00:00+00', '2026-03-10 10:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000023',
     'Can we augment the hate-speech training set with the synthetic data pipeline? That class only has 2k examples right now.',
     'd0000000-0000-0000-0000-000000000037',
     'a0000000-0000-0000-0000-000000000002',
     '2026-03-11 14:00:00+00', '2026-03-11 14:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000024',
     'Review queue UI is functional. Keyboard shortcuts for approve/reject are working. Starting UAT with the moderation team next Monday.',
     'd0000000-0000-0000-0000-000000000039',
     'a0000000-0000-0000-0000-000000000003',
     '2026-03-15 16:30:00+00', '2026-03-15 16:30:00+00', false),

    ('bb000000-0000-0000-0000-000000000025',
     'UAT feedback: moderators want a bulk-approve option for obvious low-risk items. Should be a quick addition.',
     'd0000000-0000-0000-0000-000000000039',
     'a0000000-0000-0000-0000-000000000001',
     '2026-03-18 09:00:00+00', '2026-03-18 09:00:00+00', false),

    -- c016 / Cybersecurity Audit Platform
    ('bb000000-0000-0000-0000-000000000026',
     'Filebeat-to-Kafka pipeline is live for 3 SIEM sources. Still onboarding the endpoint detection logs — ETA tomorrow.',
     'd0000000-0000-0000-0000-000000000045',
     'a0000000-0000-0000-0000-000000000002',
     '2026-01-10 10:30:00+00', '2026-01-10 10:30:00+00', false),

    ('bb000000-0000-0000-0000-000000000027',
     'ECS normalisation is inconsistent for Windows event logs — the event.code field is sometimes null. Raised a bug.',
     'd0000000-0000-0000-0000-000000000045',
     'a0000000-0000-0000-0000-000000000003',
     '2026-01-12 15:00:00+00', '2026-01-12 15:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000028',
     'SOC 2 report template is done and approved by the compliance lead. Working on the PCI-DSS section now.',
     'd0000000-0000-0000-0000-000000000047',
     'a0000000-0000-0000-0000-000000000004',
     '2026-01-20 11:00:00+00', '2026-01-20 11:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000029',
     'PCI-DSS section needs the QSA sign-off before we can include it. Can you chase that contact this week?',
     'd0000000-0000-0000-0000-000000000047',
     'a0000000-0000-0000-0000-000000000002',
     '2026-01-21 09:00:00+00', '2026-01-21 09:00:00+00', false),

    -- c018 / Marketing Automation Suite
    ('bb000000-0000-0000-0000-000000000030',
     'Campaign engine can now execute 5-step drip sequences. Branching on email open/click events is working in staging.',
     'd0000000-0000-0000-0000-000000000053',
     'a0000000-0000-0000-0000-000000000003',
     '2026-02-10 10:00:00+00', '2026-02-10 10:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000031',
     'The campaign engine needs a pause/resume capability for when compliance holds a campaign during review windows.',
     'd0000000-0000-0000-0000-000000000053',
     'a0000000-0000-0000-0000-000000000004',
     '2026-02-12 14:00:00+00', '2026-02-12 14:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000032',
     'Personalisation rules are matching correctly against 94 % of test profiles. The edge case is users with no purchase history.',
     'd0000000-0000-0000-0000-000000000055',
     'a0000000-0000-0000-0000-000000000001',
     '2026-02-20 09:00:00+00', '2026-02-20 09:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000033',
     'For users with no history, let''s fall back to the top-selling items in their region. That covers 95 % of the cold-start cases.',
     'd0000000-0000-0000-0000-000000000055',
     'a0000000-0000-0000-0000-000000000003',
     '2026-02-21 11:30:00+00', '2026-02-21 11:30:00+00', false),

    -- c020 / Fraud Detection System
    ('bb000000-0000-0000-0000-000000000034',
     'Feature extraction running at 8 k events/s with P99 latency of 4 ms. Velocity check window is 5-minute rolling.',
     'd0000000-0000-0000-0000-000000000061',
     'a0000000-0000-0000-0000-000000000004',
     '2026-01-20 10:00:00+00', '2026-01-20 10:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000035',
     'Geolocation deviation feature needs a lookup cache — hitting the geo API directly is adding 12 ms average. I''ll add Redis caching.',
     'd0000000-0000-0000-0000-000000000061',
     'a0000000-0000-0000-0000-000000000001',
     '2026-01-22 15:30:00+00', '2026-01-22 15:30:00+00', false),

    ('bb000000-0000-0000-0000-000000000036',
     'Triage dashboard MVP is live. Analysts can see last 24 h alerts with merchant category and past 30-day transaction history.',
     'd0000000-0000-0000-0000-000000000063',
     'a0000000-0000-0000-0000-000000000002',
     '2026-02-05 09:00:00+00', '2026-02-05 09:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000037',
     'Analysts are requesting risk score breakdown per feature — they want to understand why the model flagged a specific transaction.',
     'd0000000-0000-0000-0000-000000000063',
     'a0000000-0000-0000-0000-000000000004',
     '2026-02-07 13:00:00+00', '2026-02-07 13:00:00+00', false),

    -- c022 / Knowledge Management Platform
    ('bb000000-0000-0000-0000-000000000038',
     'ELSER model loaded and semantic search is returning good results for natural-language queries. BM25 hybrid mode helps edge cases.',
     'd0000000-0000-0000-0000-000000000069',
     'a0000000-0000-0000-0000-000000000001',
     '2026-02-15 10:30:00+00', '2026-02-15 10:30:00+00', false),

    ('bb000000-0000-0000-0000-000000000039',
     'Search latency is 120 ms P99 — within budget. But re-indexing during peak hours causes a 40 % spike. Need to schedule off-peak.',
     'd0000000-0000-0000-0000-000000000069',
     'a0000000-0000-0000-0000-000000000002',
     '2026-02-17 14:00:00+00', '2026-02-17 14:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000040',
     'Space-level permissions are implemented. Page-level inheritance is working; the only gap is guest access which needs product sign-off.',
     'd0000000-0000-0000-0000-000000000071',
     'a0000000-0000-0000-0000-000000000001',
     '2026-02-25 09:00:00+00', '2026-02-25 09:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000041',
     'Guest access model is approved. Guests get read-only to public spaces only. I''ll implement and add tests this week.',
     'd0000000-0000-0000-0000-000000000071',
     'a0000000-0000-0000-0000-000000000001',
     '2026-02-26 11:00:00+00', '2026-02-26 11:00:00+00', false),

    -- c024 / ERP System Migration
    ('bb000000-0000-0000-0000-000000000042',
     'GL accounts and cost centres extracted — 99.8 % record match rate. Vendor master has 340 duplicate records to clean before load.',
     'd0000000-0000-0000-0000-000000000077',
     'a0000000-0000-0000-0000-000000000002',
     '2026-01-15 10:00:00+00', '2026-01-15 10:00:00+00', false),

    ('bb000000-0000-0000-0000-000000000043',
     'Vendor deduplication rules are agreed with procurement. Running the cleanse job tonight — should be clear for another load attempt tomorrow.',
     'd0000000-0000-0000-0000-000000000077',
     'a0000000-0000-0000-0000-000000000001',
     '2026-01-16 14:30:00+00', '2026-01-16 14:30:00+00', false),

    ('bb000000-0000-0000-0000-000000000044',
     'P2P cycle 1 passed with 2 defects: GR/IR clearing and down-payment posting. Both are config issues, not data issues.',
     'd0000000-0000-0000-0000-000000000079',
     'a0000000-0000-0000-0000-000000000002',
     '2026-02-08 09:30:00+00', '2026-02-08 09:30:00+00', false),

    ('bb000000-0000-0000-0000-000000000045',
     'GR/IR config fix is applied. Cycle 2 is scheduled for next Tuesday. O2C still has an open point on credit management activation.',
     'd0000000-0000-0000-0000-000000000079',
     'a0000000-0000-0000-0000-000000000002',
     '2026-02-10 11:00:00+00', '2026-02-10 11:00:00+00', false)

ON CONFLICT (id) DO NOTHING;
