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
        initializeRoles();
        initializeDepartments();
        initializeTeams();
        initializeUsers();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            Role ceoRole = new Role();
            ceoRole.setName("ROLE_CEO");
            roleRepository.save(ceoRole);

            Role deptHeadRole = new Role();
            deptHeadRole.setName("ROLE_DEPARTMENT_HEAD");
            roleRepository.save(deptHeadRole);

            Role teamLeadRole = new Role();
            teamLeadRole.setName("ROLE_TEAM_LEAD");
            roleRepository.save(teamLeadRole);

            Role employeeRole = new Role();
            employeeRole.setName("ROLE_EMPLOYEE");
            roleRepository.save(employeeRole);

            System.out.println("Initialized roles");
        }
    }

    private void initializeDepartments() {
        if (departmentRepository.count() == 0) {
            Department itDept = new Department();
            itDept.setName("Information Technology");
            itDept.setDescription("IT and Software Development");
            itDept.setCreatedAt(LocalDateTime.now());
            departmentRepository.save(itDept);

            Department hrDept = new Department();
            hrDept.setName("Human Resources");
            hrDept.setDescription("HR and Administration");
            hrDept.setCreatedAt(LocalDateTime.now());
            departmentRepository.save(hrDept);

            Department financeDept = new Department();
            financeDept.setName("Finance");
            financeDept.setDescription("Finance and Accounting");
            financeDept.setCreatedAt(LocalDateTime.now());
            departmentRepository.save(financeDept);

            Department marketingDept = new Department();
            marketingDept.setName("Marketing");
            marketingDept.setDescription("Marketing and Sales");
            marketingDept.setCreatedAt(LocalDateTime.now());
            departmentRepository.save(marketingDept);

            System.out.println("Initialized departments");
        }
    }

    private void initializeTeams() {
        if (teamRepository.count() == 0) {
            Department itDept = departmentRepository.findByName("Information Technology").orElse(null);
            Department hrDept = departmentRepository.findByName("Human Resources").orElse(null);
            Department financeDept = departmentRepository.findByName("Finance").orElse(null);
            Department marketingDept = departmentRepository.findByName("Marketing").orElse(null);

            if (itDept != null) {
                Team devTeam = new Team();
                devTeam.setName("Development Team");
                devTeam.setDescription("Software Development Team");
                devTeam.setDepartment(itDept);
                devTeam.setCreatedAt(LocalDateTime.now());
                teamRepository.save(devTeam);

                Team qaTeam = new Team();
                qaTeam.setName("QA Team");
                qaTeam.setDescription("Quality Assurance Team");
                qaTeam.setDepartment(itDept);
                qaTeam.setCreatedAt(LocalDateTime.now());
                teamRepository.save(qaTeam);
            }

            if (hrDept != null) {
                Team hrTeam = new Team();
                hrTeam.setName("HR Team");
                hrTeam.setDescription("Human Resources Team");
                hrTeam.setDepartment(hrDept);
                hrTeam.setCreatedAt(LocalDateTime.now());
                teamRepository.save(hrTeam);
            }

            if (financeDept != null) {
                Team financeTeam = new Team();
                financeTeam.setName("Finance Team");
                financeTeam.setDescription("Finance and Accounting Team");
                financeTeam.setDepartment(financeDept);
                financeTeam.setCreatedAt(LocalDateTime.now());
                teamRepository.save(financeTeam);
            }

            if (marketingDept != null) {
                Team marketingTeam = new Team();
                marketingTeam.setName("Marketing Team");
                marketingTeam.setDescription("Marketing and Sales Team");
                marketingTeam.setDepartment(marketingDept);
                marketingTeam.setCreatedAt(LocalDateTime.now());
                teamRepository.save(marketingTeam);
            }

            System.out.println("Initialized teams");
        }
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            Role ceoRole = roleRepository.findByName("ROLE_CEO").orElse(null);
            Role deptHeadRole = roleRepository.findByName("ROLE_DEPARTMENT_HEAD").orElse(null);
            Role teamLeadRole = roleRepository.findByName("ROLE_TEAM_LEAD").orElse(null);
            Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE").orElse(null);

            Department itDept = departmentRepository.findByName("Information Technology").orElse(null);
            Department hrDept = departmentRepository.findByName("Human Resources").orElse(null);

            Team devTeam = teamRepository.findByName("Development Team").orElse(null);
            Team qaTeam = teamRepository.findByName("QA Team").orElse(null);
            Team hrTeam = teamRepository.findByName("HR Team").orElse(null);

            // Create CEO user
            if (ceoRole != null) {
                User ceo = new User();
                ceo.setUsername("ceo");
                ceo.setEmail("ceo@workflow.com");
                ceo.setPassword(passwordEncoder.encode("ceo123"));
                ceo.setFirstName("Chief");
                ceo.setLastName("Executive");
                ceo.setEnabled(true);
                ceo.setCreatedAt(LocalDateTime.now());

                UserRole ceoUserRole = new UserRole();
                ceoUserRole.setUser(ceo);
                ceoUserRole.setRole(ceoRole);
                ceo.getUserRoles().add(ceoUserRole);

                userRepository.save(ceo);
            }

            // Create Department Head
            if (deptHeadRole != null && itDept != null) {
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
                itHeadRole.setRole(deptHeadRole);
                itHeadRole.setDepartment(itDept);
                itHead.getUserRoles().add(itHeadRole);

                userRepository.save(itHead);
            }

            // Create Team Leads
            if (teamLeadRole != null && devTeam != null) {
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
            }

            // Create Employees
            if (employeeRole != null && devTeam != null) {
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
            }

            if (employeeRole != null && qaTeam != null) {
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
            }

            System.out.println("Initialized users");
        }
    }
}
