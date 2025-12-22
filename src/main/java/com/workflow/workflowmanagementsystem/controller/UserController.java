package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.entity.Role;
import com.workflow.workflowmanagementsystem.entity.Team;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.UserRole;
import com.workflow.workflowmanagementsystem.service.UserService;
import com.workflow.workflowmanagementsystem.service.TeamService;
import com.workflow.workflowmanagementsystem.Repository.RoleRepository;
import com.workflow.workflowmanagementsystem.Repository.TeamRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRoleRepository;
import com.workflow.workflowmanagementsystem.util.RoleUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private UserRoleRepository userRoleRepository ;

    @Autowired
    private RoleUtil roleUtil;

    @GetMapping
    public String listUsers(Model model, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = roleUtil.getCurrentUser(userRepository);
        roleUtil.setUserRolesInSession(currentUser,userRoleRepository, request);
        
        model.addAttribute("users", userRepository.findAll());
        return "user/list";
    }

    @GetMapping("/assign-roles")
    public String showAssignRoleForm(Model model, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = roleUtil.getCurrentUser(userRepository);
        roleUtil.setUserRolesInSession(currentUser,userRoleRepository, request);
        
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", roleRepository.findAll());
        return "user/assign-roles";
    }

    @PostMapping("/assign-roles")
    public String assignRole(@RequestParam Long userId, 
                             @RequestParam Long roleId,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            
            // Check if user already has this role (considering active status)
            boolean hasRole = user.getUserRoles().stream()
                    .filter(ur -> ur.isActive() == null || ur.isActive())
                    .anyMatch(ur -> ur.getRole().getId().equals(roleId));
            
            if (!hasRole) {
                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(role);
                user.getUserRoles().add(userRole);
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("success", "Role assigned successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User already has this role!");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users/assign-roles";
    }

    @GetMapping("/remove-role/{userId}/{roleId}")
    public String removeRole(@PathVariable Long userId, 
                           @PathVariable Long roleId,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.getUserRoles().removeIf(ur -> ur.getRole().getId().equals(roleId));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Role removed successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users/assign-roles";
    }
}

@Controller
@RequestMapping("/admin/teams")
class TeamAssignmentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository ;

    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private TeamService teamService;
    @Autowired
    private RoleUtil roleUtil;

    @GetMapping("/assign-users")
    public String showAssignUserForm(Model model, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = roleUtil.getCurrentUser(userRepository);
        roleUtil.setUserRolesInSession(currentUser,userRoleRepository, request);
        
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("teams", teamRepository.findAll());
        return "team/assign-users";
    }

    @GetMapping("/assign-users/{id}")
    public String showAssignUserFormForTeam(@PathVariable Long id, Model model, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = roleUtil.getCurrentUser(userRepository);
        roleUtil.setUserRolesInSession(currentUser,userRoleRepository, request);
        
        Team team = teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
        // Ensure the members collection is initialized
        if (team.getMembers() == null) {
            team.setMembers(new java.util.ArrayList<>());
        }
        model.addAttribute("team", team);
        model.addAttribute("availableUsers", userRepository.findAll());
        return "team/assign-users";
    }

    @PostMapping("/add-member/{teamId}")
    public String assignUserToTeam(@PathVariable Long teamId,
                                  @RequestParam Long userId,
                                  RedirectAttributes redirectAttributes) {
        try {
            teamService.addMemberToTeam(teamId, userId);
            redirectAttributes.addFlashAttribute("success", "User assigned to team successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams/assign-users/" + teamId;
    }

    @PostMapping("/remove-member/{teamId}")
    public String removeUserFromTeam(@PathVariable Long teamId,
                                   @RequestParam Long userId,
                                   RedirectAttributes redirectAttributes) {
        try {
            teamService.removeMemberFromTeam(teamId, userId);
            redirectAttributes.addFlashAttribute("success", "User removed from team successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams/assign-users/" + teamId;
    }
}