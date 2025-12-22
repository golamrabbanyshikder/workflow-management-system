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
                .filter(userRole -> userRole.isActive() == null || userRole.isActive())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<UserRole> getUserRolesByUserId(Long userId) {
        return userRoleRepository.findByIdUserId(userId);
    }
    
    public List<UserRole> getActiveUserRolesByUserId(Long userId) {
        return userRoleRepository.findByIdUserId(userId).stream()
                .filter(userRole -> userRole.isActive() == null || userRole.isActive())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<UserRole> getUserRolesByRoleId(Long roleId) {
        return userRoleRepository.findByIdRoleId(roleId);
    }
    
    public List<UserRole> getActiveUserRolesByRoleId(Long roleId) {
        return userRoleRepository.findByIdRoleId(roleId).stream()
                .filter(userRole -> userRole.isActive() == null || userRole.isActive())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<UserRole> getUserRolesByDepartmentId(Long departmentId) {
        return userRoleRepository.findByDepartmentId(departmentId);
    }
    
    public List<UserRole> getActiveUserRolesByDepartmentId(Long departmentId) {
        return userRoleRepository.findByDepartmentId(departmentId).stream()
                .filter(userRole -> userRole.isActive() == null || userRole.isActive())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<UserRole> getUserRolesByTeamId(Long teamId) {
        return userRoleRepository.findByTeamId(teamId);
    }
    
    public List<UserRole> getActiveUserRolesByTeamId(Long teamId) {
        return userRoleRepository.findByTeamId(teamId).stream()
                .filter(userRole -> userRole.isActive() == null || userRole.isActive())
                .collect(java.util.stream.Collectors.toList());
    }
    
    public Optional<UserRole> getUserRole(Long userId, Long roleId) {
        return userRoleRepository.findByIdUserId(userId).stream()
                .filter(ur -> ur.getRole().getId().equals(roleId))
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .findFirst();
    }
    
    public Optional<UserRole> getUserRoleByUserDepartmentTeam(Long userId, Long departmentId, Long teamId) {
        return userRoleRepository.findByIdUserId(userId).stream()
                .filter(ur -> {
                    boolean deptMatch = (departmentId == null) ||
                            (ur.getDepartment() != null && ur.getDepartment().getId().equals(departmentId));
                    boolean teamMatch = (teamId == null) ||
                            (ur.getTeam() != null && ur.getTeam().getId().equals(teamId));
                    return deptMatch && teamMatch;
                })
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .findFirst();
    }
    
    public Long getUserCountByRoleId(Long roleId) {
        return userRoleRepository.findByIdRoleId(roleId).stream()
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .count();
    }
    
    public Long getUserCountByDepartmentId(Long departmentId) {
        return userRoleRepository.findByDepartmentId(departmentId).stream()
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .count();
    }
    
    public Long getUserCountByTeamId(Long teamId) {
        return userRoleRepository.findByTeamId(teamId).stream()
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .count();
    }
    
    public List<Long> getUserIdsByRoleId(Long roleId) {
        return userRoleRepository.findByIdRoleId(roleId).stream()
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .map(ur -> ur.getUser().getId())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
    
    public List<Long> getUserIdsByDepartmentId(Long departmentId) {
        return userRoleRepository.findByDepartmentId(departmentId).stream()
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .map(ur -> ur.getUser().getId())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
    
    public List<Long> getUserIdsByTeamId(Long teamId) {
        return userRoleRepository.findByTeamId(teamId).stream()
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .map(ur -> ur.getUser().getId())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
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
        Optional<UserRole> existingUserRole = userRoleRepository.findByIdUserId(userId).stream()
                .filter(ur -> ur.getRole().getId().equals(roleId))
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .findFirst();
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
        Optional<UserRole> existingUserRole = userRoleRepository.findByIdUserId(userId).stream()
                .filter(ur -> {
                    boolean deptMatch = ur.getDepartment() != null && ur.getDepartment().getId().equals(departmentId);
                    boolean teamMatch = ur.getTeam() != null && ur.getTeam().getId().equals(teamId);
                    return deptMatch && teamMatch;
                })
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .findFirst();
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
        UserRole userRoleToUpdate = userRoleRepository.findByIdUserId(userId).stream()
                .filter(ur -> ur.getRole().getId().equals(roleId))
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .findFirst()
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
        
        userRoleToUpdate.setRole(newRole);
        userRoleToUpdate.setDepartment(department);
        userRoleToUpdate.setTeam(team);
        userRoleToUpdate.setAssignedBy(assignedBy);
        userRoleToUpdate.setAssignedAt(LocalDateTime.now());
        
        // Update user's team if team is assigned
        User user = userRoleToUpdate.getUser();
        if (team != null) {
            user.setTeam(team);
            userRepository.save(user);
        }
        
        return userRoleRepository.save(userRoleToUpdate);
    }
    
    public UserRole updateUserRoleStatus(Long userId, Long roleId, Boolean active) {
        UserRole userRoleToUpdate = userRoleRepository.findByIdUserId(userId).stream()
                .filter(ur -> ur.getRole().getId().equals(roleId))
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User role assignment not found"));
        
        userRoleToUpdate.setActive(active);
        
        return userRoleRepository.save(userRoleToUpdate);
    }

    public void removeRoleFromUser(Long userId, Long roleId) {
        UserRole userRoleToUpdate = userRoleRepository.findByIdUserId(userId).stream()
                .filter(ur -> ur.getRole().getId().equals(roleId))
                .filter(ur -> ur.isActive() == null || ur.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User role assignment not found"));
        
        userRoleToUpdate.setActive(false);
        userRoleRepository.save(userRoleToUpdate);
    }
    
    public void removeUserFromTeam(Long userId, Long teamId) {
        List<UserRole> userRoles = userRoleRepository.findByIdUserId(userId).stream()
                .filter(userRole -> userRole.isActive() == null || userRole.isActive())
                .collect(java.util.stream.Collectors.toList());
        
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
        List<UserRole> userRoles = userRoleRepository.findByIdUserId(userId).stream()
                .filter(userRole -> userRole.isActive() == null || userRole.isActive())
                .collect(java.util.stream.Collectors.toList());
        
        for (UserRole userRole : userRoles) {
            if (userRole.getDepartment() != null && userRole.getDepartment().getId().equals(departmentId)) {
                userRole.setActive(false);
                userRoleRepository.save(userRole);
            }
        }
    }
    
    public void transferUserToNewTeam(Long userId, Long oldTeamId, Long newTeamId, Long assignedBy) {
        List<UserRole> userRoles = userRoleRepository.findByIdUserId(userId).stream()
                .filter(userRole -> userRole.isActive() == null || userRole.isActive())
                .collect(java.util.stream.Collectors.toList());
        
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