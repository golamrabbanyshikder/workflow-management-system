package com.workflow.workflowmanagementsystem.service;

import com.workflow.workflowmanagementsystem.Repository.RoleRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRoleRepository;
import com.workflow.workflowmanagementsystem.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public List<Role> getActiveRoles() {
        return roleRepository.findByActive(true);
    }

    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }
    
    public List<Role> searchRoles(String name, Boolean active) {
        return roleRepository.findByNameContainingAndActive(name, active);
    }
    
    public List<Role> getRolesByLevel(Integer roleLevel) {
        return roleRepository.findByRoleLevel(roleLevel);
    }
    
    public List<Role> getRolesByLevelOrLower(Integer maxLevel) {
        return roleRepository.findByRoleLevelLessThanEqual(maxLevel);
    }
    
    public List<Role> getRolesWithPermission(String permission) {
        return roleRepository.findRolesWithPermission(permission);
    }
    
    public Long getUserCount(Long roleId) {
        return roleRepository.countActiveUsersByRoleId(roleId);
    }

    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new RuntimeException("Role with name '" + role.getName() + "' already exists");
        }
        
        if (role.getRoleLevel() == null) {
            role.setRoleLevel(1); // Default role level
        }
        
        role.setActive(true);
        role.setCreatedAt(LocalDateTime.now());
        return roleRepository.save(role);
    }
    
    public Role createRoleWithPermissions(Role role, List<String> permissions) {
        if (roleRepository.existsByName(role.getName())) {
            throw new RuntimeException("Role with name '" + role.getName() + "' already exists");
        }
        
        if (role.getRoleLevel() == null) {
            role.setRoleLevel(1); // Default role level
        }
        
        role.setActive(true);
        role.setCreatedAt(LocalDateTime.now());
        
        if (permissions != null && !permissions.isEmpty()) {
            role.setPermissions(permissions);
        }
        
        return roleRepository.save(role);
    }

    public Role updateRole(Long id, Role roleDetails) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // Check if name is being changed and if new name already exists
        if (!role.getName().equals(roleDetails.getName()) &&
                roleRepository.existsByName(roleDetails.getName())) {
            throw new RuntimeException("Role with name '" + roleDetails.getName() + "' already exists");
        }

        role.setName(roleDetails.getName());
        role.setDescription(roleDetails.getDescription());
        role.setRoleLevel(roleDetails.getRoleLevel());
        role.setUpdatedAt(LocalDateTime.now());
        
        return roleRepository.save(role);
    }
    
    public Role updateRolePermissions(Long id, List<String> permissions) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        
        role.setPermissions(permissions);
        role.setUpdatedAt(LocalDateTime.now());
        
        return roleRepository.save(role);
    }
    
    public Role addPermissionToRole(Long id, String permission) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        
        role.addPermission(permission);
        role.setUpdatedAt(LocalDateTime.now());
        
        return roleRepository.save(role);
    }
    
    public Role removePermissionFromRole(Long id, String permission) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        
        role.removePermission(permission);
        role.setUpdatedAt(LocalDateTime.now());
        
        return roleRepository.save(role);
    }
    
    public Role updateRoleStatus(Long id, Boolean active) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        
        role.setActive(active);
        role.setUpdatedAt(LocalDateTime.now());
        
        return roleRepository.save(role);
    }

    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        
        // Check if role is assigned to any active users
        Long activeUserCount = userRoleRepository.countActiveUsersByRoleId(id);
        if (activeUserCount > 0) {
            throw new RuntimeException("Cannot delete role. It is assigned to " + activeUserCount + " active users. Please deactivate the role instead.");
        }
        
        roleRepository.delete(role);
    }
    
    // Initialize default roles with permissions
    public void initializeDefaultRoles() {
        if (roleRepository.count() == 0) {
            // Admin role
            Role adminRole = new Role("ADMIN", "System Administrator", 10);
            adminRole.setPermissions(Arrays.asList(
                "USER_CREATE", "USER_READ", "USER_UPDATE", "USER_DELETE",
                "ROLE_CREATE", "ROLE_READ", "ROLE_UPDATE", "ROLE_DELETE",
                "DEPARTMENT_CREATE", "DEPARTMENT_READ", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE",
                "TEAM_CREATE", "TEAM_READ", "TEAM_UPDATE", "TEAM_DELETE",
                "WORKFLOW_CREATE", "WORKFLOW_READ", "WORKFLOW_UPDATE", "WORKFLOW_DELETE",
                "TASK_CREATE", "TASK_READ", "TASK_UPDATE", "TASK_DELETE",
                "REPORT_READ", "SYSTEM_ADMIN"
            ));
            roleRepository.save(adminRole);
            
            // Manager role
            Role managerRole = new Role("MANAGER", "Department Manager", 7);
            managerRole.setPermissions(Arrays.asList(
                "USER_READ", "USER_UPDATE",
                "ROLE_READ",
                "DEPARTMENT_READ", "DEPARTMENT_UPDATE",
                "TEAM_CREATE", "TEAM_READ", "TEAM_UPDATE",
                "WORKFLOW_CREATE", "WORKFLOW_READ", "WORKFLOW_UPDATE",
                "TASK_CREATE", "TASK_READ", "TASK_UPDATE", "TASK_DELETE",
                "REPORT_READ", "TEAM_MANAGE"
            ));
            roleRepository.save(managerRole);
            
            // Team Lead role
            Role teamLeadRole = new Role("TEAM_LEAD", "Team Leader", 5);
            teamLeadRole.setPermissions(Arrays.asList(
                "USER_READ",
                "ROLE_READ",
                "DEPARTMENT_READ",
                "TEAM_READ", "TEAM_UPDATE",
                "WORKFLOW_READ", "WORKFLOW_UPDATE",
                "TASK_CREATE", "TASK_READ", "TASK_UPDATE", "TASK_DELETE",
                "REPORT_READ", "TASK_MANAGE"
            ));
            roleRepository.save(teamLeadRole);
            
            // Employee role
            Role employeeRole = new Role("EMPLOYEE", "Regular Employee", 1);
            employeeRole.setPermissions(Arrays.asList(
                "USER_READ",
                "ROLE_READ",
                "DEPARTMENT_READ",
                "TEAM_READ",
                "WORKFLOW_READ",
                "TASK_READ", "TASK_UPDATE",
                "REPORT_READ"
            ));
            roleRepository.save(employeeRole);
        }
    }
}
