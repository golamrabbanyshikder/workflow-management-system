package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.Repository.UserRoleRepository;
import com.workflow.workflowmanagementsystem.entity.Department;
import com.workflow.workflowmanagementsystem.entity.Team;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.service.DepartmentService;
import com.workflow.workflowmanagementsystem.service.TeamService;
import com.workflow.workflowmanagementsystem.service.UserService;
import com.workflow.workflowmanagementsystem.util.RoleUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;

    @GetMapping
    public String listTeams(Model model, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = RoleUtil.getCurrentUser(userRepository);
        RoleUtil.setUserRolesInSession(currentUser, userRoleRepository, request);
        
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "team/list";
    }
    
    @GetMapping("/active")
    public String listActiveTeams(Model model) {
        model.addAttribute("teams", teamService.getActiveTeams());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "team/list";
    }
    
    @GetMapping("/department/{departmentId}")
    public String listTeamsByDepartment(@PathVariable Long departmentId, Model model) {
        Department department = departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        model.addAttribute("teams", teamService.getTeamsByDepartment(departmentId));
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("selectedDepartment", department);
        return "team/list";
    }
    
    @GetMapping("/search")
    public String searchTeams(@RequestParam(required = false) String name,
                             @RequestParam(required = false) Long departmentId,
                             Model model) {
        List<Team> teams;
        if (name != null && !name.trim().isEmpty()) {
            teams = teamService.searchTeams(name, departmentId);
        } else if (departmentId != null) {
            teams = teamService.getTeamsByDepartment(departmentId);
        } else {
            teams = teamService.getAllTeams();
        }
        model.addAttribute("teams", teams);
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("searchName", name);
        model.addAttribute("searchDepartmentId", departmentId);
        return "team/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = RoleUtil.getCurrentUser(userRepository);
        RoleUtil.setUserRolesInSession(currentUser, userRoleRepository, request);
        
        model.addAttribute("team", new Team());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("users", userService.getAllUsers());
        return "team/create";
    }

    @PostMapping("/create")
    public String createTeam(@ModelAttribute Team team,
                           @RequestParam Long departmentId,
                           @RequestParam(required = false) Long teamLeadId,
                           RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.getDepartmentById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            team.setDepartment(department);
            
            if (teamLeadId != null) {
                teamService.createTeamWithLead(team, teamLeadId);
            } else {
                teamService.createTeam(team);
            }
            
            redirectAttributes.addFlashAttribute("success", "Team created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = RoleUtil.getCurrentUser(userRepository);
        RoleUtil.setUserRolesInSession(currentUser, userRoleRepository, request);
        
        Team team = teamService.getTeamById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        model.addAttribute("team", team);
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("users", userService.getAllUsers());
        return "team/edit";
    }

    @PostMapping("/update/{id}")
    public String updateTeam(@PathVariable Long id,
                           @ModelAttribute Team team,
                           @RequestParam Long departmentId,
                           @RequestParam(required = false) Long teamLeadId,
                           RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.getDepartmentById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            team.setDepartment(department);
            
            if (teamLeadId != null) {
                team.setTeamLeadId(teamLeadId);
            }
            
            teamService.updateTeam(id, team);
            redirectAttributes.addFlashAttribute("success", "Team updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams";
    }
    
    @PostMapping("/status/{id}")
    public String updateTeamStatus(@PathVariable Long id,
                                 @RequestParam Boolean active,
                                 RedirectAttributes redirectAttributes) {
        try {
            teamService.updateTeamStatus(id, active);
            String status = active ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", "Team " + status + " successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams";
    }
    
    @PostMapping("/assign-lead/{teamId}")
    public String assignTeamLead(@PathVariable Long teamId,
                               @RequestParam Long teamLeadId,
                               RedirectAttributes redirectAttributes) {
        try {
            teamService.assignTeamLead(teamId, teamLeadId);
            redirectAttributes.addFlashAttribute("success", "Team lead assigned successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams";
    }
    
    
    @GetMapping("/view/{id}")
    public String viewTeam(@PathVariable Long id, Model model, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = RoleUtil.getCurrentUser(userRepository);
        RoleUtil.setUserRolesInSession(currentUser, userRoleRepository, request);
        
        Team team = teamService.getTeamById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        Long memberCount = teamService.getMemberCount(id);
        List<User> availableUsers = userService.getAllUsers();
        
        model.addAttribute("team", team);
        model.addAttribute("memberCount", memberCount);
        model.addAttribute("availableUsers", availableUsers);
        return "team/view";
    }
    

    @GetMapping("/delete/{id}")
    public String deleteTeam(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teamService.deleteTeam(id);
            redirectAttributes.addFlashAttribute("success", "Team deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams";
    }
}