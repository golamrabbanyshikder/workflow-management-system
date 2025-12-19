package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.entity.Role;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.service.RoleService;
import com.workflow.workflowmanagementsystem.service.UserRoleService;
import com.workflow.workflowmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public String listRoles(Model model) {
        model.addAttribute("roles", roleService.getAllRoles());
        model.addAttribute("availablePermissions", getAvailablePermissions());
        model.addAttribute("role", new Role()); // Add empty role object for the create modal form
        return "role/list";
    }
    
    @GetMapping("/active")
    public String listActiveRoles(Model model) {
        model.addAttribute("roles", roleService.getActiveRoles());
        model.addAttribute("availablePermissions", getAvailablePermissions());
        model.addAttribute("role", new Role()); // Add empty role object for create modal form
        return "role/list";
    }
    
    @GetMapping("/search")
    public String searchRoles(@RequestParam(required = false) String name,
                            @RequestParam(required = false) Boolean active,
                            @RequestParam(required = false) String sortBy,
                            @RequestParam(required = false) String roleType,
                            @RequestParam(required = false) String permission,
                            @RequestParam(required = false) Integer level,
                            Model model) {
        List<Role> roles;
        if (name != null && !name.trim().isEmpty()) {
            roles = roleService.searchRoles(name, active);
        } else if (active != null) {
            roles = roleService.getActiveRoles();
            if (!active) {
                roles = roleService.getAllRoles().stream()
                        .filter(role -> !role.isActive())
                        .collect(java.util.stream.Collectors.toList());
            }
        } else {
            roles = roleService.getAllRoles();
        }
        model.addAttribute("roles", roles);
        model.addAttribute("searchName", name);
        model.addAttribute("searchActive", active);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("searchType", roleType);
        model.addAttribute("selectedPermission", permission);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("availablePermissions", getAvailablePermissions());
        model.addAttribute("role", new Role()); // Add empty role object for create modal form
        return "role/list";
    }
    
    @GetMapping("/level/{level}")
    public String listRolesByLevel(@PathVariable Integer level, Model model) {
        model.addAttribute("roles", roleService.getRolesByLevelOrLower(level));
        model.addAttribute("selectedLevel", level);
        model.addAttribute("availablePermissions", getAvailablePermissions());
        model.addAttribute("role", new Role()); // Add empty role object for create modal form
        return "role/list";
    }
    
    @GetMapping("/permission/{permission}")
    public String listRolesByPermission(@PathVariable String permission, Model model) {
        model.addAttribute("roles", roleService.getRolesWithPermission(permission));
        model.addAttribute("selectedPermission", permission);
        model.addAttribute("availablePermissions", getAvailablePermissions());
        model.addAttribute("role", new Role()); // Add empty role object for create modal form
        return "role/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("role", new Role());
        model.addAttribute("availablePermissions", getAvailablePermissions());
        return "role/create";
    }

    @PostMapping("/create")
    public String createRole(@ModelAttribute Role role,
                           @RequestParam(required = false) List<String> permissions,
                           RedirectAttributes redirectAttributes) {
        try {
            if (permissions != null && !permissions.isEmpty()) {
                roleService.createRoleWithPermissions(role, permissions);
            } else {
                roleService.createRole(role);
            }
            redirectAttributes.addFlashAttribute("success", "Role created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        model.addAttribute("role", role);
        model.addAttribute("availablePermissions", getAvailablePermissions());
        return "role/edit";
    }

    @PostMapping("/update/{id}")
    public String updateRole(@PathVariable Long id,
                           @ModelAttribute Role role,
                           @RequestParam(required = false) List<String> permissions,
                           RedirectAttributes redirectAttributes) {
        try {
            roleService.updateRole(id, role);
            
            if (permissions != null) {
                roleService.updateRolePermissions(id, permissions);
            }
            
            redirectAttributes.addFlashAttribute("success", "Role updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }
    
    @PostMapping("/status/{id}")
    public String updateRoleStatus(@PathVariable Long id,
                                @RequestParam Boolean active,
                                RedirectAttributes redirectAttributes) {
        try {
            roleService.updateRoleStatus(id, active);
            String status = active ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", "Role " + status + " successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }
    
    @PostMapping("/add-permission/{id}")
    public String addPermissionToRole(@PathVariable Long id,
                                   @RequestParam String permission,
                                   RedirectAttributes redirectAttributes) {
        try {
            roleService.addPermissionToRole(id, permission);
            redirectAttributes.addFlashAttribute("success", "Permission added to role successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles/edit/" + id;
    }
    
    @PostMapping("/remove-permission/{id}")
    public String removePermissionFromRole(@PathVariable Long id,
                                      @RequestParam String permission,
                                      RedirectAttributes redirectAttributes) {
        try {
            roleService.removePermissionFromRole(id, permission);
            redirectAttributes.addFlashAttribute("success", "Permission removed from role successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles/edit/" + id;
    }
    
    @GetMapping("/view/{id}")
    public String viewRole(@PathVariable Long id, Model model) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        Long userCount = roleService.getUserCount(id);
        List<User> assignedUsers = userRoleService.getUserIdsByRoleId(id).stream()
                .map(userId -> userService.getUserById(userId).orElse(null))
                .filter(user -> user != null)
                .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("role", role);
        model.addAttribute("userCount", userCount);
        model.addAttribute("assignedUsers", assignedUsers);
        model.addAttribute("availablePermissions", getAvailablePermissions());
        return "role/view";
    }
    
    @GetMapping("/assign-users/{id}")
    public String showAssignUsersForm(@PathVariable Long id, Model model) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        List<User> availableUsers = userService.getActiveUsers();
        
        model.addAttribute("role", role);
        model.addAttribute("availableUsers", availableUsers);
        return "role/assign-users";
    }
    
    @PostMapping("/assign-user/{roleId}")
    public String assignUserToRole(@PathVariable Long roleId,
                                 @RequestParam Long userId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) Long teamId,
                                 RedirectAttributes redirectAttributes) {
        try {
            // For simplicity, using current user ID as assignedBy (in real app, get from security context)
            userRoleService.assignRoleToUser(userId, roleId, departmentId, teamId, 1L);
            redirectAttributes.addFlashAttribute("success", "User assigned to role successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles/view/" + roleId;
    }
    
    @PostMapping("/remove-user/{roleId}")
    public String removeUserFromRole(@PathVariable Long roleId,
                                   @RequestParam Long userId,
                                   RedirectAttributes redirectAttributes) {
        try {
            userRoleService.removeRoleFromUser(userId, roleId);
            redirectAttributes.addFlashAttribute("success", "User removed from role successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles/view/" + roleId;
    }

    @GetMapping("/delete/{id}")
    public String deleteRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roleService.deleteRole(id);
            redirectAttributes.addFlashAttribute("success", "Role deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }
    
    @PostMapping("/initialize-default")
    public String initializeDefaultRoles(RedirectAttributes redirectAttributes) {
        try {
            roleService.initializeDefaultRoles();
            redirectAttributes.addFlashAttribute("success", "Default roles initialized successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }
    
    private List<String> getAvailablePermissions() {
        return Arrays.asList(
            "USER_CREATE", "USER_READ", "USER_UPDATE", "USER_DELETE",
            "ROLE_CREATE", "ROLE_READ", "ROLE_UPDATE", "ROLE_DELETE",
            "DEPARTMENT_CREATE", "DEPARTMENT_READ", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE",
            "TEAM_CREATE", "TEAM_READ", "TEAM_UPDATE", "TEAM_DELETE", "TEAM_MANAGE",
            "WORKFLOW_CREATE", "WORKFLOW_READ", "WORKFLOW_UPDATE", "WORKFLOW_DELETE",
            "TASK_CREATE", "TASK_READ", "TASK_UPDATE", "TASK_DELETE", "TASK_MANAGE",
            "REPORT_READ", "SYSTEM_ADMIN"
        );
    }
}