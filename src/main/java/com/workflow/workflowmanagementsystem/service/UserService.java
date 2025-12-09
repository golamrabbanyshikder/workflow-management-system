package com.workflow.workflowmanagementsystem.service;



import com.workflow.workflowmanagementsystem.Repository.DepartmentRepository;
import com.workflow.workflowmanagementsystem.Repository.RoleRepository;
import com.workflow.workflowmanagementsystem.Repository.TeamRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.dto.RegistrationDto;
import com.workflow.workflowmanagementsystem.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User registerUser(RegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEnabled(true);

        // Assign role based on registration logic
        Role role = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Department department = departmentRepository.findById(registrationDto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Team team = teamRepository.findById(registrationDto.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setDepartment(department);
        userRole.setTeam(team);

        user.getUserRoles().add(userRole);

        return userRepository.save(user);
    }

    public void createCEOUser() {
        if (!userRepository.existsByUsername("ceo")) {
            User ceo = new User();
            ceo.setUsername("ceo");
            ceo.setEmail("ceo@xyz.com");
            ceo.setPassword(passwordEncoder.encode("ceo123"));
            ceo.setFirstName("CEO");
            ceo.setLastName("Admin");
            ceo.setEnabled(true);

            Role ceoRole = roleRepository.findByName("ROLE_CEO")
                    .orElseThrow(() -> new RuntimeException("CEO role not found"));

            UserRole userRole = new UserRole();
            userRole.setUser(ceo);
            userRole.setRole(ceoRole);
            // CEO might not be assigned to specific department/team
            ceo.getUserRoles().add(userRole);

            userRepository.save(ceo);
        }
    }
}