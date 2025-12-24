-- Reset database sequences to avoid primary key conflicts
-- This script should be run after loading sample data to reset sequences

-- Reset user sequence to start after the highest existing user ID
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 0) FROM users)) FROM users;

-- Reset department sequence to start after the highest existing department ID  
SELECT setval('departments_id_seq', (SELECT COALESCE(MAX(id), 0) FROM departments)) FROM departments;

-- Reset team sequence to start after the highest existing team ID
SELECT setval('teams_id_seq', (SELECT COALESCE(MAX(id), 0) FROM teams)) FROM teams;

-- Reset role sequence to start after the highest existing role ID
SELECT setval('roles_id_seq', (SELECT COALESCE(MAX(id), 0) FROM roles)) FROM roles;

-- Reset workflow sequence to start after the highest existing workflow ID
SELECT setval('workflows_id_seq', (SELECT COALESCE(MAX(id), 0) FROM workflows)) FROM workflows;

-- Reset task sequence to start after the highest existing task ID
SELECT setval('tasks_id_seq', (SELECT COALESCE(MAX(id), 0) FROM tasks)) FROM tasks;

-- Reset workflow_status_layer sequence to start after the highest existing workflow_status_layer ID
SELECT setval('workflow_status_layers_id_seq', (SELECT COALESCE(MAX(id), 0) FROM workflow_status_layers)) FROM workflow_status_layers;

-- Reset comment sequence to start after the highest existing comment ID
SELECT setval('comments_id_seq', (SELECT COALESCE(MAX(id), 0) FROM comments)) FROM comments;

-- Reset audit_log sequence to start after the highest existing audit_log ID
SELECT setval('audit_logs_id_seq', (SELECT COALESCE(MAX(id), 0) FROM audit_logs)) FROM audit_logs;

-- Reset role_permission sequence (if it exists)
-- This table might not have a sequence if it's not auto-increment
-- SELECT setval('role_permissions_id_seq', (SELECT COALESCE(MAX(id), 0) FROM role_permissions)) FROM role_permissions;

-- Reset user_role sequence (if it exists)
-- This table might not have a sequence if it's not auto-increment
-- SELECT setval('user_roles_id_seq', (SELECT COALESCE(MAX(id), 0) FROM user_roles)) FROM user_roles;

SELECT 'Database sequences reset successfully!' as message;