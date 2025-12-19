package com.workflow.workflowmanagementsystem.service;

import com.workflow.workflowmanagementsystem.Repository.DepartmentRepository;
import com.workflow.workflowmanagementsystem.Repository.RoleRepository;
import com.workflow.workflowmanagementsystem.Repository.TeamRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRoleRepository;
import com.workflow.workflowmanagementsystem.entity.Department;
import com.workflow.workflowmanagementsystem.entity.Role;
import com.workflow.workflowmanagementsystem.entity.Team;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserRoleService {

    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private TeamRepository teamRepository;

    public List<UserRole> getAllUserRoles() {
        return userRoleRepository.findAll();
    }
    
    public List<UserRole> getActiveUserRoles() {
        return userRoleRepository.findAll().stream()
                .filter(UserRole::isActive)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<UserRole> getUserRolesByUserId(Long userId) {
        return userRoleRepository.findByUserId(userId);
    }
    
    public List<UserRole> getActiveUserRolesByUserId(Long userId) {
        return userRoleRepository.findByUserIdAndActive(userId, true);
    }

    public List<UserRole> getUserRolesByRoleId(Long roleId) {
        return userRoleRepository.findByRoleId(roleId);
    }
    
    public List<UserRole> getActiveUserRolesByRoleId(Long roleId) {
        return userRoleRepository.findByRoleIdAndActive(roleId, true);
    }

    public List<UserRole> getUserRolesByDepartmentId(Long departmentId) {
        return userRoleRepository.findByDepartmentId(departmentId);
    }
    
    public List<UserRole> getActiveUserRolesByDepartmentId(Long departmentId) {
        return userRoleRepository.findByDepartmentIdAndActive(departmentId, true);
    }

    public List<UserRole> getUserRolesByTeamId(Long teamId) {
        return userRoleRepository.findByTeamId(teamId);
    }
    
    public List<UserRole> getActiveUserRolesByTeamId(Long teamId) {
        return userRoleRepository.findByTeamIdAndActive(teamId, true);
    }
    
    public Optional<UserRole> getUserRole(Long userId, Long roleId) {
        return userRoleRepository.findActiveUserRole(userId, roleId);
    }
    
    public Optional<UserRole> getUserRoleByUserDepartmentTeam(Long userId, Long departmentId, Long teamId) {
        return userRoleRepository.findActiveUserRoleByUserDepartmentTeam(userId, departmentId, teamId);
    }
    
    public Long getUserCountByRoleId(Long roleId) {
        return userRoleRepository.countActiveUsersByRoleId(roleId);
    }
    
    public Long getUserCountByDepartmentId(Long departmentId) {
        return userRoleRepository.countActiveUsersByDepartmentId(departmentId);
    }
    
    public Long getUserCountByTeamId(Long teamId) {
        return userRoleRepository.countActiveUsersByTeamId(teamId);
    }
    
    public List<Long> getUserIdsByRoleId(Long roleId) {
        return userRoleRepository.findActiveUserIdsByRoleId(roleId);
    }
    
    public List<Long> getUserIdsByDepartmentId(Long departmentId) {
        return userRoleRepository.findActiveUserIdsByDepartmentId(departmentId);
    }
    
    public List<Long> getUserIdsByTeamId(Long teamId) {
        return userRoleRepository.findActiveUserIdsByTeamId(teamId);
    }

    public UserRole assignRoleToUser(Long userId, Long roleId, Long departmentId, Long teamId, Long assignedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        
        Department department = null;
        if (departmentId != null) {
            department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        }
        
        Team team = null;
        if (teamId != null) {
            team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        }
        
        // Check if user already has this role assignment
        Optional<UserRole> existingUserRole = userRoleRepository.findActiveUserRole(userId, roleId);
        if (existingUserRole.isPresent()) {
            throw new RuntimeException("User already has this role assigned");
        }
        
        UserRole userRole = new UserRole(user, role, department, team);
        userRole.setAssignedBy(assignedBy);
        userRole.setAssignedAt(LocalDateTime.now());
        
        return userRoleRepository.save(userRole);
    }
    
    public UserRole assignRoleToUserInContext(Long userId, Long roleId, Long departmentId, Long teamId, Long assignedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        
        // Check if user already has this role in the same context
        Optional<UserRole> existingUserRole = userRoleRepository.findActiveUserRoleByUserDepartmentTeam(userId, departmentId, teamId);
        if (existingUserRole.isPresent()) {
            throw new RuntimeException("User already has a role assigned in this department and team context");
        }
        
        UserRole userRole = new UserRole(user, role, department, team);
        userRole.setAssignedBy(assignedBy);
        userRole.setAssignedAt(LocalDateTime.now());
        
        // Update user's team if team is assigned
        if (team != null) {
            user.setTeam(team);
            userRepository.save(user);
        }
        
        return userRoleRepository.save(userRole);
    }

    public UserRole updateUserRole(Long userId, Long roleId, Long newRoleId, Long departmentId, Long teamId, Long assignedBy) {
        UserRole userRole = userRoleRepository.findActiveUserRole(userId, roleId)
                .orElseThrow(() -> new RuntimeException("User role assignment not found"));
        
        Role newRole = roleRepository.findById(newRoleId)
                .orElseThrow(() -> new RuntimeException("New role not found with id: " + newRoleId));
        
        Department department = null;
        if (departmentId != null) {
            department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        }
        
        Team team = null;
        if (teamId != null) {
            team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        }
        
        userRole.setRole(newRole);
        userRole.setDepartment(department);
        userRole.setTeam(team);
        userRole.setAssignedBy(assignedBy);
        userRole.setAssignedAt(LocalDateTime.now());
        
        // Update user's team if team is assigned
        User user = userRole.getUser();
        if (team != null) {
            user.setTeam(team);
            userRepository.save(user);
        }
        
        return userRoleRepository.save(userRole);
    }
    
    public UserRole updateUserRoleStatus(Long userId, Long roleId, Boolean active) {
        UserRole userRole = userRoleRepository.findActiveUserRole(userId, roleId)
                .orElseThrow(() -> new RuntimeException("User role assignment not found"));
        
        userRole.setActive(active);
        
        return userRoleRepository.save(userRole);
    }

    public void removeRoleFromUser(Long userId, Long roleId) {
        UserRole userRole = userRoleRepository.findActiveUserRole(userId, roleId)
                .orElseThrow(() -> new RuntimeException("User role assignment not found"));
        
        userRole.setActive(false);
        userRoleRepository.save(userRole);
    }
    
    public void removeUserFromTeam(Long userId, Long teamId) {
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndActive(userId, true);
        
        for (UserRole userRole : userRoles) {
            if (userRole.getTeam() != null && userRole.getTeam().getId().equals(teamId)) {
                userRole.setActive(false);
                userRoleRepository.save(userRole);
            }
        }
        
        // Update user's team
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setTeam(null);
        userRepository.save(user);
    }
    
    public void removeUserFromDepartment(Long userId, Long departmentId) {
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndActive(userId, true);
        
        for (UserRole userRole : userRoles) {
            if (userRole.getDepartment() != null && userRole.getDepartment().getId().equals(departmentId)) {
                userRole.setActive(false);
                userRoleRepository.save(userRole);
            }
        }
    }
    
    public void transferUserToNewTeam(Long userId, Long oldTeamId, Long newTeamId, Long assignedBy) {
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndActive(userId, true);
        
        for (UserRole userRole : userRoles) {
            if (userRole.getTeam() != null && userRole.getTeam().getId().equals(oldTeamId)) {
                Team newTeam = teamRepository.findById(newTeamId)
                        .orElseThrow(() -> new RuntimeException("New team not found with id: " + newTeamId));
                
                userRole.setTeam(newTeam);
                userRole.setAssignedBy(assignedBy);
                userRole.setAssignedAt(LocalDateTime.now());
                userRoleRepository.save(userRole);
            }
        }
        
        // Update user's team
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Team newTeam = teamRepository.findById(newTeamId)
                .orElseThrow(() -> new RuntimeException("New team not found with id: " + newTeamId));
        user.setTeam(newTeam);
        userRepository.save(user);
    }
}