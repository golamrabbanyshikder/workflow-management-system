package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.entity.Department;
import com.workflow.workflowmanagementsystem.entity.Team;
import com.workflow.workflowmanagementsystem.service.DepartmentService;
import com.workflow.workflowmanagementsystem.service.TeamService;
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

    @GetMapping
    public String listTeams(Model model) {
        model.addAttribute("teams", teamService.getAllTeams());
        return "team/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("team", new Team());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "team/create";
    }

    @PostMapping("/create")
    public String createTeam(@ModelAttribute Team team, @RequestParam Long departmentId, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.getDepartmentById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            team.setDepartment(department);
            teamService.createTeam(team);
            redirectAttributes.addFlashAttribute("success", "Team created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Team team = teamService.getTeamById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        model.addAttribute("team", team);
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "team/edit";
    }

    @PostMapping("/update/{id}")
    public String updateTeam(@PathVariable Long id, @ModelAttribute Team team, @RequestParam Long departmentId, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.getDepartmentById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            team.setDepartment(department);
            teamService.updateTeam(id, team);
            redirectAttributes.addFlashAttribute("success", "Team updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams";
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