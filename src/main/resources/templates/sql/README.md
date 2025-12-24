# Sample Data for Workflow Management System

This directory contains SQL scripts for populating the Workflow Management System with sample data for testing and demonstration purposes.

## Files

- `schema.sql` - Database schema definition (if exists)
- `sample-data.sql` - Comprehensive sample data for all entities

## Sample Data Overview

The `sample-data.sql` script creates a realistic dataset with:

### Departments (5)
1. Information Technology
2. Human Resources
3. Finance
4. Marketing
5. Operations

### Roles (6)
1. **ADMIN** - System administrator with full access
2. **MANAGER** - Department manager with team management access
3. **TEAM_LEAD** - Team leader with task assignment capabilities
4. **EMPLOYEE** - Regular employee with basic task access
5. **HR_SPECIALIST** - HR specialist with employee management access
6. **FINANCE_ANALYST** - Finance analyst with financial reporting access

### Teams (10)
- 2 teams per department, each with an assigned team lead
- Teams include Development, Infrastructure, Recruitment, Payroll, Accounting, Budget, Digital Marketing, Brand Management, Process Improvement, and Quality Assurance

### Users (13)
- 1 System Administrator
- 12 regular users distributed across all departments and teams
- All users have the default password: `password123` (BCrypt encoded)

### Workflows (5)
1. **Software Development Lifecycle** - Complete development process
2. **Employee Onboarding** - New employee onboarding process
3. **Budget Approval Process** - Budget planning and approval workflow
4. **Marketing Campaign Management** - Marketing campaign creation and management
5. **Process Improvement Request** - Process improvement workflow

### Workflow Status Layers (28)
- 4-6 status layers per workflow
- Each layer has a specific order, color, and completion status
- Final layers are marked as completion points

### Tasks (24)
- Realistic tasks distributed across all workflows
- Various priorities (LOW, MEDIUM, HIGH, URGENT)
- Assigned to appropriate users with due dates and time estimates

### Comments (18)
- Sample comments on various tasks showing collaboration
- Timestamps to show realistic interaction patterns

### Audit Logs (15)
- System activity tracking for important actions
- Includes user logins, task assignments, and data modifications

### Role Permissions
- Comprehensive permission matrix for each role
- Follows principle of least privilege

## Usage Instructions

### Option 1: Direct SQL Execution
1. Connect to your PostgreSQL database
2. Execute the script: `psql -U username -d database_name -f sample-data.sql`

### Option 2: Database Tool
1. Open your preferred database management tool (pgAdmin, DBeaver, etc.)
2. Connect to your database
3. Open and execute the `sample-data.sql` file

### Option 3: Spring Boot Application
If you want to load this data automatically when your application starts:

1. Add this to your `application.properties`:
```properties
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:templates/sql/sample-data.sql
spring.sql.init.continue-on-error=true
```

2. Or create a `DataInitializer` component to execute the script on startup

## Important Notes

### Password Security
All user accounts use the password `password123` which is BCrypt encoded as:
```
$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
```

**IMPORTANT**: Change these passwords in a production environment!

### Foreign Key Constraints
The script handles foreign key dependencies by:
1. Temporarily disabling foreign key checks using PostgreSQL's session_replication_role
2. Using TRUNCATE with CASCADE to clear existing data
3. Inserting data in the correct order
4. Re-enabling foreign key checks

### Auto-increment Reset
The script resets all sequence counters using TRUNCATE with RESTART IDENTITY for clean testing.

### Sequence Reset for Existing Data
If you have existing data with hardcoded IDs and want to enable auto-increment for new records:

1. Run the sample-data.sql script first to load your data
2. Then run the reset-sequences.sql script to reset all database sequences

```bash
# Load sample data
psql -U username -d database_name -f sample-data.sql

# Reset sequences to avoid conflicts
psql -U username -d database_name -f reset-sequences.sql
```

This ensures that:
- All your existing sample data is preserved
- Database sequences start after the highest existing ID
- New records (like user registrations) won't conflict with existing primary keys

### Data Relationships
The sample data maintains proper relationships:
- Users are assigned to teams and departments
- Team leads are properly designated
- Tasks are assigned to appropriate users
- Workflows are associated with departments
- Comments are linked to tasks and users
- Audit logs track system activities

## Testing Scenarios

This sample data supports testing:

1. **User Authentication**: Login with any user using password `password123`
2. **Role-based Access**: Different access levels based on user roles
3. **Task Management**: Complete task lifecycle from creation to completion
4. **Workflow Navigation**: Moving tasks through workflow status layers
5. **Team Collaboration**: Comments and task assignments
6. **Reporting**: Generate reports with meaningful data
7. **Audit Trail**: Review system activity logs

## Sample Login Credentials

| Username | Password | Role | Department |
|----------|----------|------|------------|
| admin | password123 | ADMIN | System |
| john.smith | password123 | TEAM_LEAD | IT |
| sarah.jones | password123 | EMPLOYEE | IT |
| mike.wilson | password123 | TEAM_LEAD | IT |
| lisa.brown | password123 | TEAM_LEAD | HR |
| david.miller | password123 | HR_SPECIALIST | HR |
| emily.davis | password123 | EMPLOYEE | HR |
| robert.taylor | password123 | TEAM_LEAD | Finance |
| jennifer.anderson | password123 | FINANCE_ANALYST | Finance |
| chris.thomas | password123 | TEAM_LEAD | Marketing |
| amanda.jackson | password123 | EMPLOYEE | Marketing |
| kevin.white | password123 | TEAM_LEAD | Operations |
| michelle.harris | password123 | EMPLOYEE | Operations |

## Customization

To modify the sample data:

1. **Add more data**: Insert additional rows following the existing patterns
2. **Modify existing data**: Update the values in the INSERT statements
3. **Change relationships**: Update foreign key references to match your needs
4. **Adjust permissions**: Modify the role_permissions table

## Troubleshooting

### Common Issues

1. **Foreign Key Errors**: Ensure you're using a clean database or the script properly clears existing data
2. **Encoding Issues**: Make sure your database connection uses UTF-8 encoding
3. **Permission Errors**: Ensure your database user has INSERT, UPDATE, DELETE privileges
4. **Auto-increment Conflicts**: The script resets counters, but if you have existing data with high IDs, you may need to adjust

### Data Integrity Verification

After running the script, verify data integrity:
```sql
-- Check all tables have data
SELECT 'departments' as table_name, COUNT(*) as record_count FROM departments
UNION ALL
SELECT 'roles', COUNT(*) FROM roles
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'teams', COUNT(*) FROM teams
UNION ALL
SELECT 'workflows', COUNT(*) FROM workflows
UNION ALL
SELECT 'tasks', COUNT(*) FROM tasks
UNION ALL
SELECT 'comments', COUNT(*) FROM comments
UNION ALL
SELECT 'audit_logs', COUNT(*) FROM audit_logs;

-- Check foreign key relationships
SELECT u.username, t.name as team_name, d.name as department_name
FROM users u
LEFT JOIN teams t ON u.team_id = t.id
LEFT JOIN departments d ON t.department_id = d.id
WHERE u.id <= 5;
```

## Production Considerations

This sample data is intended for development and testing only. For production:

1. **Remove or replace** all sample data
2. **Change all default passwords**
3. **Review and adjust** role permissions
4. **Set up proper user accounts** for your organization
5. **Configure appropriate audit logging**
6. **Implement proper data validation**