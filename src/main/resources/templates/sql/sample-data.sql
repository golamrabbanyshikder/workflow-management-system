-- Sample Data Insert Script for Workflow Management System
-- This script inserts sample data for all entities in the system
-- Compatible with PostgreSQL

-- Disable foreign key checks temporarily (for PostgreSQL)
SET session_replication_role = replica;

-- Clear existing data (in reverse order of dependencies)
TRUNCATE TABLE audit_logs RESTART IDENTITY CASCADE;
TRUNCATE TABLE comments RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_roles RESTART IDENTITY CASCADE;
TRUNCATE TABLE role_permissions RESTART IDENTITY CASCADE;
TRUNCATE TABLE tasks RESTART IDENTITY CASCADE;
TRUNCATE TABLE workflow_status_layers RESTART IDENTITY CASCADE;
TRUNCATE TABLE workflows RESTART IDENTITY CASCADE;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;
TRUNCATE TABLE teams RESTART IDENTITY CASCADE;
TRUNCATE TABLE departments RESTART IDENTITY CASCADE;
TRUNCATE TABLE roles RESTART IDENTITY CASCADE;

-- Insert Departments
INSERT INTO departments (id, name, description, created_at) VALUES
                                                                (1, 'Information Technology', 'Manages all technology infrastructure and software development', NOW()),
                                                                (2, 'Human Resources', 'Handles employee relations, recruitment, and HR policies', NOW()),
                                                                (3, 'Finance', 'Manages financial planning, budgeting, and accounting', NOW()),
                                                                (4, 'Marketing', 'Handles marketing campaigns, branding, and customer acquisition', NOW()),
                                                                (5, 'Operations', 'Manages day-to-day operations and process improvements', NOW());

-- Insert Roles
INSERT INTO roles (id, name, description, role_level, is_active, created_at) VALUES
                                                                                 (1, 'ROLE_CEO', 'System administrator with full access to all features', 1, true, NOW()),
                                                                                 (2, 'ROLE_DEPARTMENT_HEAD', 'Department manager with access to team management features', 2, true, NOW()),
                                                                                 (3, 'ROLE_TEAM_LEAD', 'Team leader with access to task assignment and team coordination', 3, true, NOW()),
                                                                                 (4, 'ROLE_EMPLOYEE', 'Regular employee with access to assigned tasks and basic features', 4, true, NOW()),
                                                                                 (5, 'ROLE_HR_SPECIALIST', 'HR specialist with access to employee management features', 3, true, NOW()),
                                                                                 (6, 'ROLE_FINANCE_ANALYST', 'Finance analyst with access to financial reports and budget management', 3, true, NOW());

-- Insert Role Permissions
INSERT INTO role_permissions (role_id, permission) VALUES
-- Admin permissions
(1, 'USER_CREATE'),
(1, 'USER_READ'),
(1, 'USER_UPDATE'),
(1, 'USER_DELETE'),
(1, 'ROLE_CREATE'),
(1, 'ROLE_READ'),
(1, 'ROLE_UPDATE'),
(1, 'ROLE_DELETE'),
(1, 'DEPARTMENT_CREATE'),
(1, 'DEPARTMENT_READ'),
(1, 'DEPARTMENT_UPDATE'),
(1, 'DEPARTMENT_DELETE'),
(1, 'TEAM_CREATE'),
(1, 'TEAM_READ'),
(1, 'TEAM_UPDATE'),
(1, 'TEAM_DELETE'),
(1, 'WORKFLOW_CREATE'),
(1, 'WORKFLOW_READ'),
(1, 'WORKFLOW_UPDATE'),
(1, 'WORKFLOW_DELETE'),
(1, 'TASK_CREATE'),
(1, 'TASK_READ'),
(1, 'TASK_UPDATE'),
(1, 'TASK_DELETE'),
(1, 'REPORT_READ'),
(1, 'REPORT_EXPORT'),
(1, 'AUDIT_READ'),

-- Manager permissions
(2, 'USER_READ'),
(2, 'USER_UPDATE'),
(2, 'ROLE_READ'),
(2, 'DEPARTMENT_READ'),
(2, 'DEPARTMENT_UPDATE'),
(2, 'TEAM_CREATE'),
(2, 'TEAM_READ'),
(2, 'TEAM_UPDATE'),
(2, 'WORKFLOW_CREATE'),
(2, 'WORKFLOW_READ'),
(2, 'WORKFLOW_UPDATE'),
(2, 'TASK_CREATE'),
(2, 'TASK_READ'),
(2, 'TASK_UPDATE'),
(2, 'TASK_DELETE'),
(2, 'REPORT_READ'),
(2, 'REPORT_EXPORT'),

-- Team Lead permissions
(3, 'USER_READ'),
(3, 'ROLE_READ'),
(3, 'TEAM_READ'),
(3, 'TEAM_UPDATE'),
(3, 'WORKFLOW_READ'),
(3, 'TASK_CREATE'),
(3, 'TASK_READ'),
(3, 'TASK_UPDATE'),
(3, 'TASK_DELETE'),
(3, 'REPORT_READ'),

-- Employee permissions
(4, 'TASK_READ'),
(4, 'TASK_UPDATE'),
(4, 'WORKFLOW_READ'),

-- HR Specialist permissions
(5, 'USER_CREATE'),
(5, 'USER_READ'),
(5, 'USER_UPDATE'),
(5, 'ROLE_READ'),
(5, 'DEPARTMENT_READ'),
(5, 'TEAM_READ'),
(5, 'REPORT_READ'),

-- Finance Analyst permissions
(6, 'WORKFLOW_READ'),
(6, 'TASK_READ'),
(6, 'REPORT_READ'),
(6, 'REPORT_EXPORT');

-- Insert Teams
INSERT INTO teams (id, name, description, department_id, team_lead_id, is_active, created_at) VALUES
                                                                                                  (1, 'Development Team', 'Software development and maintenance team', 1, NULL, true, NOW()),
                                                                                                  (2, 'Infrastructure Team', 'IT infrastructure and system administration', 1, NULL, true, NOW()),
                                                                                                  (3, 'Recruitment Team', 'Employee recruitment and onboarding', 2, NULL, true, NOW()),
                                                                                                  (4, 'Payroll Team', 'Payroll processing and compensation management', 2, NULL, true, NOW()),
                                                                                                  (5, 'Accounting Team', 'Financial accounting and reporting', 3, NULL, true, NOW()),
                                                                                                  (6, 'Budget Team', 'Budget planning and financial analysis', 3, NULL, true, NOW()),
                                                                                                  (7, 'Digital Marketing Team', 'Digital marketing and social media', 4, NULL, true, NOW()),
                                                                                                  (8, 'Brand Management Team', 'Brand strategy and corporate identity', 4, NULL, true, NOW()),
                                                                                                  (9, 'Process Improvement Team', 'Business process optimization', 5, NULL, true, NOW()),
                                                                                                  (10, 'Quality Assurance Team', 'Quality control and compliance', 5, NULL, true, NOW());

-- Insert Users (passwords are BCrypt encoded for "password123")
INSERT INTO users (id, username, email, password, first_name, last_name, enabled, team_id, created_at) VALUES
                                                                                                           (1, 'admin', 'admin@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'System', 'Administrator', true, NULL, NOW()),
                                                                                                           (2, 'john.smith', 'john.smith@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Smith', true, 1, NOW()),
                                                                                                           (3, 'sarah.jones', 'sarah.jones@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Sarah', 'Jones', true, 1, NOW()),
                                                                                                           (4, 'mike.wilson', 'mike.wilson@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Mike', 'Wilson', true, 2, NOW()),
                                                                                                           (5, 'lisa.brown', 'lisa.brown@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Lisa', 'Brown', true, 3, NOW()),
                                                                                                           (6, 'david.miller', 'david.miller@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'David', 'Miller', true, 3, NOW()),
                                                                                                           (7, 'emily.davis', 'emily.davis@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Emily', 'Davis', true, 4, NOW()),
                                                                                                           (8, 'robert.taylor', 'robert.taylor@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Robert', 'Taylor', true, 5, NOW()),
                                                                                                           (9, 'jennifer.anderson', 'jennifer.anderson@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jennifer', 'Anderson', true, 6, NOW()),
                                                                                                           (10, 'chris.thomas', 'chris.thomas@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Chris', 'Thomas', true, 7, NOW()),
                                                                                                           (11, 'amanda.jackson', 'amanda.jackson@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Amanda', 'Jackson', true, 8, NOW()),
                                                                                                           (12, 'kevin.white', 'kevin.white@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Kevin', 'White', true, 9, NOW()),
                                                                                                           (13, 'michelle.harris', 'michelle.harris@workflow.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Michelle', 'Harris', true, 10, NOW());

-- Update team lead IDs
UPDATE teams SET team_lead_id = 2 WHERE id = 1;  -- John Smith leads Development Team
UPDATE teams SET team_lead_id = 4 WHERE id = 2;  -- Mike Wilson leads Infrastructure Team
UPDATE teams SET team_lead_id = 5 WHERE id = 3;  -- Lisa Brown leads Recruitment Team
UPDATE teams SET team_lead_id = 7 WHERE id = 4;  -- Emily Davis leads Payroll Team
UPDATE teams SET team_lead_id = 8 WHERE id = 5;  -- Robert Taylor leads Accounting Team
UPDATE teams SET team_lead_id = 9 WHERE id = 6;  -- Jennifer Anderson leads Budget Team
UPDATE teams SET team_lead_id = 10 WHERE id = 7; -- Chris Thomas leads Digital Marketing Team
UPDATE teams SET team_lead_id = 11 WHERE id = 8; -- Amanda Jackson leads Brand Management Team
UPDATE teams SET team_lead_id = 12 WHERE id = 9; -- Kevin White leads Process Improvement Team
UPDATE teams SET team_lead_id = 13 WHERE id = 10; -- Michelle Harris leads Quality Assurance Team

-- Insert UserRoles (many-to-many relationship between users and roles)
INSERT INTO user_roles (user_id, role_id, department_id, team_id, is_active, assigned_by, assigned_at, created_at) VALUES
-- Admin user
(1, 1, NULL, NULL, true, 1, NOW(), NOW()),

-- IT Department users
(2, 3, 1, 1, true, 1, NOW(), NOW()),  -- John Smith - Team Lead
(3, 4, 1, 1, true, 2, NOW(), NOW()),  -- Sarah Jones - Employee
(4, 3, 1, 2, true, 1, NOW(), NOW()),  -- Mike Wilson - Team Lead

-- HR Department users
(5, 3, 2, 3, true, 1, NOW(), NOW()),  -- Lisa Brown - Team Lead
(6, 5, 2, 3, true, 5, NOW(), NOW()),  -- David Miller - HR Specialist
(7, 4, 2, 4, true, 1, NOW(), NOW()),  -- Emily Davis - Employee

-- Finance Department users
(8, 3, 3, 5, true, 1, NOW(), NOW()),  -- Robert Taylor - Team Lead
(9, 6, 3, 6, true, 8, NOW(), NOW()),  -- Jennifer Anderson - Finance Analyst

-- Marketing Department users
(10, 3, 4, 7, true, 1, NOW(), NOW()), -- Chris Thomas - Team Lead
(11, 4, 4, 8, true, 10, NOW(), NOW()), -- Amanda Jackson - Employee

-- Operations Department users
(12, 3, 5, 9, true, 1, NOW(), NOW()), -- Kevin White - Team Lead
(13, 4, 5, 10, true, 12, NOW(), NOW()); -- Michelle Harris - Employee

-- Insert Workflows
INSERT INTO workflows (id, name, description, status, created_by, department_id, is_active, created_at) VALUES
                                                                                                            (1, 'Software Development Lifecycle', 'Complete software development workflow from requirements to deployment', 'ACTIVE', 1, 1, true, NOW()),
                                                                                                            (2, 'Employee Onboarding', 'Process for onboarding new employees', 'ACTIVE', 5, 2, true, NOW()),
                                                                                                            (3, 'Budget Approval Process', 'Workflow for budget planning and approval', 'ACTIVE', 8, 3, true, NOW()),
                                                                                                            (4, 'Marketing Campaign Management', 'Process for creating and managing marketing campaigns', 'ACTIVE', 10, 4, true, NOW()),
                                                                                                            (5, 'Process Improvement Request', 'Workflow for submitting and approving process improvement suggestions', 'ACTIVE', 12, 5, true, NOW());

-- Insert Workflow Status Layers
INSERT INTO workflow_status_layers (id, name, description, status_order, is_final, color, workflow_id, created_at) VALUES
-- Software Development Workflow Status Layers
(1, 'Requirements Gathering', 'Collect and document project requirements', 1, false, '#007bff', 1, NOW()),
(2, 'Design', 'Create system design and architecture', 2, false, '#17a2b8', 1, NOW()),
(3, 'Development', 'Write and implement code', 3, false, '#ffc107', 1, NOW()),
(4, 'Testing', 'Perform quality assurance testing', 4, false, '#fd7e14', 1, NOW()),
(5, 'Deployment', 'Deploy to production environment', 5, false, '#20c997', 1, NOW()),
(6, 'Completed', 'Project successfully completed', 6, true, '#28a745', 1, NOW()),

-- Employee Onboarding Workflow Status Layers
(7, 'Application Received', 'Job application received and reviewed', 1, false, '#007bff', 2, NOW()),
(8, 'Interview Scheduled', 'Interview process initiated', 2, false, '#17a2b8', 2, NOW()),
(9, 'Background Check', 'Background verification in progress', 3, false, '#ffc107', 2, NOW()),
(10, 'Offer Extended', 'Job offer sent to candidate', 4, false, '#fd7e14', 2, NOW()),
(11, 'Onboarding Started', 'Employee onboarding process initiated', 5, false, '#20c997', 2, NOW()),
(12, 'Onboarding Complete', 'Employee successfully onboarded', 6, true, '#28a745', 2, NOW()),

-- Budget Approval Workflow Status Layers
(13, 'Budget Proposal', 'Initial budget proposal submitted', 1, false, '#007bff', 3, NOW()),
(14, 'Department Review', 'Budget reviewed by department head', 2, false, '#17a2b8', 3, NOW()),
(15, 'Finance Review', 'Budget reviewed by finance team', 3, false, '#ffc107', 3, NOW()),
(16, 'Executive Approval', 'Final approval from executive team', 4, false, '#fd7e14', 3, NOW()),
(17, 'Budget Approved', 'Budget approved and implemented', 5, true, '#28a745', 3, NOW()),

-- Marketing Campaign Workflow Status Layers
(18, 'Campaign Concept', 'Initial campaign concept developed', 1, false, '#007bff', 4, NOW()),
(19, 'Creative Development', 'Creative assets and content developed', 2, false, '#17a2b8', 4, NOW()),
(20, 'Campaign Launch', 'Campaign launched to target audience', 3, false, '#ffc107', 4, NOW()),
(21, 'Performance Monitoring', 'Campaign performance tracked and analyzed', 4, false, '#fd7e14', 4, NOW()),
(22, 'Campaign Complete', 'Campaign successfully completed', 5, true, '#28a745', 4, NOW()),

-- Process Improvement Workflow Status Layers
(23, 'Improvement Suggestion', 'Process improvement idea submitted', 1, false, '#007bff', 5, NOW()),
(24, 'Feasibility Analysis', 'Analysis of improvement feasibility', 2, false, '#17a2b8', 5, NOW()),
(25, 'Implementation Planning', 'Detailed implementation plan created', 3, false, '#ffc107', 5, NOW()),
(26, 'Implementation', 'Process changes implemented', 4, false, '#fd7e14', 5, NOW()),
(27, 'Review and Refine', 'Implementation reviewed and refined', 5, false, '#20c997', 5, NOW()),
(28, 'Improvement Complete', 'Process improvement successfully completed', 6, true, '#28a745', 5, NOW());

-- Insert Tasks
INSERT INTO tasks (id, title, description, workflow_status_layer_id, priority, workflow_id, assigned_to, created_by, due_date, estimated_hours, created_at) VALUES
-- Software Development Tasks
(1, 'Gather requirements for new feature', 'Collect and document all requirements for the new customer portal feature', 1, 'HIGH', 1, 2, 3, NOW() + INTERVAL '7 days', 8, NOW()),
(2, 'Create system architecture design', 'Design the overall system architecture for the customer portal', 2, 'HIGH', 1, 2, 3, NOW() + INTERVAL '10 days', 12, NOW()),
(3, 'Implement user authentication module', 'Develop the user authentication and authorization system', 3, 'MEDIUM', 1, 3, 2, NOW() + INTERVAL '14 days', 16, NOW()),
(4, 'Develop customer dashboard', 'Create the main customer dashboard interface', 3, 'MEDIUM', 1, 3, 2, NOW() + INTERVAL '16 days', 20, NOW()),
(5, 'Perform integration testing', 'Test all system components integration', 4, 'MEDIUM', 1, 3, 2, NOW() + INTERVAL '20 days', 10, NOW()),
(6, 'Deploy to production server', 'Deploy the completed application to production', 5, 'HIGH', 1, 4, 2, NOW() + INTERVAL '22 days', 4, NOW()),

-- Employee Onboarding Tasks
(7, 'Review job applications', 'Review and screen received job applications', 7, 'MEDIUM', 2, 5, 6, NOW() + INTERVAL '3 days', 6, NOW()),
(8, 'Schedule candidate interviews', 'Schedule interviews with qualified candidates', 8, 'MEDIUM', 2, 6, 5, NOW() + INTERVAL '5 days', 4, NOW()),
(9, 'Conduct background checks', 'Perform background verification for selected candidates', 9, 'HIGH', 2, 5, 6, NOW() + INTERVAL '7 days', 8, NOW()),
(10, 'Prepare employment contract', 'Create and prepare employment contract documents', 10, 'HIGH', 2, 7, 5, NOW() + INTERVAL '8 days', 3, NOW()),
(11, 'Set up employee workstation', 'Prepare workstation and system access for new employee', 11, 'MEDIUM', 2, 7, 5, NOW() + INTERVAL '10 days', 5, NOW()),

-- Budget Approval Tasks
(12, 'Prepare Q1 budget proposal', 'Create detailed budget proposal for Q1 2024', 13, 'HIGH', 3, 8, 9, NOW() + INTERVAL '5 days', 10, NOW()),
(13, 'Review departmental budgets', 'Review all departmental budget submissions', 14, 'MEDIUM', 3, 9, 8, NOW() + INTERVAL '8 days', 6, NOW()),
(14, 'Analyze financial feasibility', 'Analyze financial feasibility of budget proposals', 15, 'HIGH', 3, 9, 8, NOW() + INTERVAL '10 days', 8, NOW()),
(15, 'Prepare executive summary', 'Create executive summary for final approval', 16, 'HIGH', 3, 8, 9, NOW() + INTERVAL '12 days', 4, NOW()),

-- Marketing Campaign Tasks
(16, 'Develop campaign concept', 'Create concept for spring marketing campaign', 18, 'MEDIUM', 4, 10, 11, NOW() + INTERVAL '4 days', 6, NOW()),
(17, 'Design marketing materials', 'Create visual assets for marketing campaign', 19, 'MEDIUM', 4, 11, 10, NOW() + INTERVAL '7 days', 12, NOW()),
(18, 'Launch social media campaign', 'Execute social media marketing campaign', 20, 'HIGH', 4, 10, 11, NOW() + INTERVAL '10 days', 8, NOW()),
(19, 'Monitor campaign performance', 'Track and analyze campaign metrics', 21, 'MEDIUM', 4, 11, 10, NOW() + INTERVAL '15 days', 10, NOW()),

-- Process Improvement Tasks
(20, 'Submit process improvement idea', 'Document and submit improvement suggestion', 23, 'LOW', 5, 12, 13, NOW() + INTERVAL '2 days', 3, NOW()),
(21, 'Analyze current process', 'Analyze existing process for improvement opportunities', 24, 'MEDIUM', 5, 13, 12, NOW() + INTERVAL '5 days', 8, NOW()),
(22, 'Create implementation plan', 'Develop detailed plan for process changes', 25, 'MEDIUM', 5, 12, 13, NOW() + INTERVAL '8 days', 6, NOW()),
(23, 'Implement process changes', 'Execute planned process improvements', 26, 'HIGH', 5, 13, 12, NOW() + INTERVAL '12 days', 15, NOW()),
(24, 'Monitor improvement results', 'Track and measure improvement effectiveness', 27, 'MEDIUM', 5, 12, 13, NOW() + INTERVAL '15 days', 8, NOW());

-- Insert Comments
INSERT INTO comments (id, content, task_id, user_id, is_edited, created_at) VALUES
(1, 'Please ensure all requirements are documented with clear acceptance criteria', 1, 2, false, NOW() + INTERVAL '1 hour'),
(2, 'I have completed the requirements gathering phase. All stakeholders have provided their input.', 1, 3, false, NOW() + INTERVAL '2 hours'),
(3, 'The system architecture design is ready for review. Please check the attached diagrams.', 2, 3, false, NOW() + INTERVAL '1 day'),
(4, 'Architecture looks good. Let''s proceed with development.', 2, 2, false, NOW() + INTERVAL '1 day 2 hours'),
(5, 'Authentication module is 50% complete. Expected to finish by end of week.', 3, 3, false, NOW() + INTERVAL '3 days'),
(6, 'Need to integrate with the existing user database. Working on API endpoints.', 3, 3, false, NOW() + INTERVAL '4 days'),
(7, 'Customer dashboard mockups are ready for approval.', 4, 3, false, NOW() + INTERVAL '5 days'),
(8, 'Approved! Please proceed with implementation.', 4, 2, false, NOW() + INTERVAL '5 days 3 hours'),
(9, 'All candidates have been screened. Shortlisted 5 applicants for interviews.', 7, 6, false, NOW() + INTERVAL '1 hour'),
(10, 'Interviews scheduled for next week. All candidates have been notified.', 8, 5, false, NOW() + INTERVAL '2 days'),
(11, 'Background checks completed for all selected candidates.', 9, 6, false, NOW() + INTERVAL '3 days'),
(12, 'Employment contracts prepared for 3 candidates.', 10, 7, false, NOW() + INTERVAL '4 days'),
(13, 'Q1 budget proposal includes all departmental requests and projected revenue.', 12, 8, false, NOW() + INTERVAL '2 hours'),
(14, 'Budget review complete. Minor adjustments needed in marketing allocation.', 14, 9, false, NOW() + INTERVAL '4 days'),
(15, 'Spring campaign concept focuses on sustainability and eco-friendly messaging.', 16, 10, false, NOW() + INTERVAL '1 hour'),
(16, 'Marketing materials are vibrant and engaging. Great work!', 17, 11, false, NOW() + INTERVAL '3 days'),
(17, 'Process improvement idea targets reducing approval time by 30%.', 20, 12, false, NOW() + INTERVAL '1 hour'),
(18, 'Current process analysis shows significant bottlenecks in the approval workflow.', 21, 13, false, NOW() + INTERVAL '2 days');

-- Insert Audit Logs
INSERT INTO audit_logs (id, action_type, entity_type, entity_id, description, user_id, ip_address, user_agent, created_at) VALUES
                                                                                                                               (1, 'CREATE', 'User', 1, 'Created admin user account', 1, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW()),
                                                                                                                               (2, 'CREATE', 'Department', 1, 'Created Information Technology department', 1, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '1 minute'),
                                                                                                                               (3, 'CREATE', 'Role', 1, 'Created ADMIN role with full permissions', 1, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '2 minutes'),
                                                                                                                               (4, 'CREATE', 'Team', 1, 'Created Development Team', 2, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '5 minutes'),
                                                                                                                               (5, 'ASSIGN', 'UserRole', 1, 'Assigned TEAM_LEAD role to John Smith', 1, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '6 minutes'),
                                                                                                                               (6, 'CREATE', 'Workflow', 1, 'Created Software Development Lifecycle workflow', 1, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '10 minutes'),
                                                                                                                               (7, 'CREATE', 'Task', 1, 'Created task: Gather requirements for new feature', 2, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '15 minutes'),
                                                                                                                               (8, 'ASSIGN', 'Task', 1, 'Assigned task to Sarah Jones', 2, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '16 minutes'),
                                                                                                                               (9, 'UPDATE', 'Task', 1, 'Updated task status to In Progress', 3, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '1 hour'),
                                                                                                                               (10, 'CREATE', 'Comment', 1, 'Added comment to task: Gather requirements for new feature', 2, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '1 hour 1 minute'),
                                                                                                                               (11, 'LOGIN', 'User', 2, 'User John Smith logged in', 2, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '2 hours'),
                                                                                                                               (12, 'VIEW', 'Task', 1, 'Viewed task details: Gather requirements for new feature', 2, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '2 hours 5 minutes'),
                                                                                                                               (13, 'EXPORT', 'Report', 1, 'Exported task status report', 2, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '3 hours'),
                                                                                                                               (14, 'COMPLETE', 'Task', 1, 'Marked task as completed: Gather requirements for new feature', 3, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() + INTERVAL '4 hours'),
                                                                                                                               (15, 'CREATE', 'AuditLog', 1, 'System audit log initialized', 1, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW());

-- Re-enable foreign key checks
SET session_replication_role = DEFAULT;

-- Summary of inserted data
SELECT 'Data insertion completed successfully!' as message;
SELECT COUNT(*) as departments_count FROM departments;
SELECT COUNT(*) as roles_count FROM roles;
SELECT COUNT(*) as role_permissions_count FROM role_permissions;
SELECT COUNT(*) as teams_count FROM teams;
SELECT COUNT(*) as users_count FROM users;
SELECT COUNT(*) as user_roles_count FROM user_roles;
SELECT COUNT(*) as workflows_count FROM workflows;
SELECT COUNT(*) as workflow_status_layers_count FROM workflow_status_layers;
SELECT COUNT(*) as tasks_count FROM tasks;
SELECT COUNT(*) as comments_count FROM comments;
SELECT COUNT(*) as audit_logs_count FROM audit_logs;