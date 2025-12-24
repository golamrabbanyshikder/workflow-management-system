/*
package com.workflow.workflowmanagementsystem.component;

import com.workflow.workflowmanagementsystem.Repository.DepartmentRepository;
import com.workflow.workflowmanagementsystem.Repository.RoleRepository;
import com.workflow.workflowmanagementsystem.Repository.TeamRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize data if tables are completely empty
        // This prevents conflicts with existing sample data
        if (roleRepository.count() == 0 &&
            departmentRepository.count() == 0 &&
            teamRepository.count() == 0 &&
            userRepository.count() == 0) {
            
            System.out.println("Initializing sample data...");
            initializeRoles();
            initializeDepartments();
            initializeTeams();
            initializeUsers();
            System.out.println("Sample data initialization completed");
        } else {
            System.out.println("Database already contains data. Skipping sample data initialization.");
        }
    }

    private void initializeRoles() {
        // Create roles only if they don't exist
        createRoleIfNotExists("ROLE_ADMIN", "System administrator with full access to all features", 1);
        createRoleIfNotExists("ROLE_MANAGER", "Department manager with access to team management features", 2);
        createRoleIfNotExists("ROLE_TEAM_LEAD", "Team leader with access to task assignment and team coordination", 3);
        createRoleIfNotExists("ROLE_EMPLOYEE", "Regular employee with access to assigned tasks and basic features", 4);
        createRoleIfNotExists("ROLE_HR_SPECIALIST", "HR specialist with access to employee management features", 3);
        createRoleIfNotExists("ROLE_FINANCE_ANALYST", "Finance analyst with access to financial reports and budget management", 3);
        
        System.out.println("Role initialization completed");
    }
    
    private void createRoleIfNotExists(String name, String description, int level) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            role.setRoleLevel(level);
            role.setActive(true);
            roleRepository.save(role);
            System.out.println("Created role: " + name);
        }
    }

    private void initializeDepartments() {
        // Create departments only if they don't exist
        createDepartmentIfNotExists("Information Technology", "IT and Software Development");
        createDepartmentIfNotExists("Human Resources", "HR and Administration");
        createDepartmentIfNotExists("Finance", "Finance and Accounting");
        createDepartmentIfNotExists("Marketing", "Marketing and Sales");
        
        System.out.println("Department initialization completed");
    }
    
    private void createDepartmentIfNotExists(String name, String description) {
        if (!departmentRepository.existsByName(name)) {
            Department department = new Department();
            department.setName(name);
            department.setDescription(description);
            department.setCreatedAt(LocalDateTime.now());
            departmentRepository.save(department);
            System.out.println("Created department: " + name);
        }
    }

    private void initializeTeams() {
        Department itDept = departmentRepository.findByName("Information Technology").orElse(null);
        Department hrDept = departmentRepository.findByName("Human Resources").orElse(null);
        Department financeDept = departmentRepository.findByName("Finance").orElse(null);
        Department marketingDept = departmentRepository.findByName("Marketing").orElse(null);

        // Create teams only if they don't exist
        if (itDept != null) {
            createTeamIfNotExists("Development Team", "Software Development Team", itDept);
            createTeamIfNotExists("QA Team", "Quality Assurance Team", itDept);
        }

        if (hrDept != null) {
            createTeamIfNotExists("HR Team", "Human Resources Team", hrDept);
        }

        if (financeDept != null) {
            createTeamIfNotExists("Finance Team", "Finance and Accounting Team", financeDept);
        }

        if (marketingDept != null) {
            createTeamIfNotExists("Marketing Team", "Marketing and Sales Team", marketingDept);
        }

        System.out.println("Team initialization completed");
    }
    
    private void createTeamIfNotExists(String name, String description, Department department) {
        if (!teamRepository.findByName(name).isPresent()) {
            Team team = new Team();
            team.setName(name);
            team.setDescription(description);
            team.setDepartment(department);
            team.setCreatedAt(LocalDateTime.now());
            teamRepository.save(team);
            System.out.println("Created team: " + name);
        }
    }

    private void initializeUsers() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
        Role managerRole = roleRepository.findByName("ROLE_MANAGER").orElse(null);
        Role teamLeadRole = roleRepository.findByName("ROLE_TEAM_LEAD").orElse(null);
        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE").orElse(null);

        Department itDept = departmentRepository.findByName("Information Technology").orElse(null);
        Department hrDept = departmentRepository.findByName("Human Resources").orElse(null);

        Team devTeam = teamRepository.findByName("Development Team").orElse(null);
        Team qaTeam = teamRepository.findByName("QA Team").orElse(null);
        Team hrTeam = teamRepository.findByName("HR Team").orElse(null);

        // Create Admin user only if it doesn't exist
        if (!userRepository.existsByUsername("admin") && adminRole != null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@workflow.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setEnabled(true);
            admin.setCreatedAt(LocalDateTime.now());

            UserRole adminUserRole = new UserRole();
            adminUserRole.setUser(admin);
            adminUserRole.setRole(adminRole);
            admin.getUserRoles().add(adminUserRole);

            userRepository.save(admin);
            System.out.println("Created admin user");
        }

        // Create Department Head only if it doesn't exist
        if (!userRepository.existsByUsername("ithead") && managerRole != null && itDept != null) {
            User itHead = new User();
            itHead.setUsername("ithead");
            itHead.setEmail("ithead@workflow.com");
            itHead.setPassword(passwordEncoder.encode("head123"));
            itHead.setFirstName("IT");
            itHead.setLastName("Head");
            itHead.setEnabled(true);
            itHead.setCreatedAt(LocalDateTime.now());

            UserRole itHeadRole = new UserRole();
            itHeadRole.setUser(itHead);
            itHeadRole.setRole(managerRole);
            itHeadRole.setDepartment(itDept);
            itHead.getUserRoles().add(itHeadRole);

            userRepository.save(itHead);
            System.out.println("Created IT head user");
        }

        // Create Team Lead only if it doesn't exist
        if (!userRepository.existsByUsername("devlead") && teamLeadRole != null && devTeam != null) {
            User devLead = new User();
            devLead.setUsername("devlead");
            devLead.setEmail("devlead@workflow.com");
            devLead.setPassword(passwordEncoder.encode("lead123"));
            devLead.setFirstName("Development");
            devLead.setLastName("Lead");
            devLead.setEnabled(true);
            devLead.setCreatedAt(LocalDateTime.now());

            UserRole devLeadRole = new UserRole();
            devLeadRole.setUser(devLead);
            devLeadRole.setRole(teamLeadRole);
            devLeadRole.setDepartment(itDept);
            devLeadRole.setTeam(devTeam);
            devLead.getUserRoles().add(devLeadRole);

            userRepository.save(devLead);
            System.out.println("Created dev lead user");
        }

        // Create Employees only if they don't exist
        if (!userRepository.existsByUsername("developer1") && employeeRole != null && devTeam != null) {
            User dev1 = new User();
            dev1.setUsername("developer1");
            dev1.setEmail("dev1@workflow.com");
            dev1.setPassword(passwordEncoder.encode("dev123"));
            dev1.setFirstName("Developer");
            dev1.setLastName("One");
            dev1.setEnabled(true);
            dev1.setCreatedAt(LocalDateTime.now());

            UserRole dev1Role = new UserRole();
            dev1Role.setUser(dev1);
            dev1Role.setRole(employeeRole);
            dev1Role.setDepartment(itDept);
            dev1Role.setTeam(devTeam);
            dev1.getUserRoles().add(dev1Role);

            userRepository.save(dev1);
            System.out.println("Created developer1 user");
        }

        if (!userRepository.existsByUsername("qa1") && employeeRole != null && qaTeam != null) {
            User qa1 = new User();
            qa1.setUsername("qa1");
            qa1.setEmail("qa1@workflow.com");
            qa1.setPassword(passwordEncoder.encode("qa123"));
            qa1.setFirstName("QA");
            qa1.setLastName("One");
            qa1.setEnabled(true);
            qa1.setCreatedAt(LocalDateTime.now());

            UserRole qa1Role = new UserRole();
            qa1Role.setUser(qa1);
            qa1Role.setRole(employeeRole);
            qa1Role.setDepartment(itDept);
            qa1Role.setTeam(qaTeam);
            qa1.getUserRoles().add(qa1Role);

            userRepository.save(qa1);
            System.out.println("Created qa1 user");
        }

        System.out.println("User initialization completed");
    }
}
*/
