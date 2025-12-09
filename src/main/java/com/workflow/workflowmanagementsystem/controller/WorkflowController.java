package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.entity.Department;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.Workflow;
import com.workflow.workflowmanagementsystem.entity.Workflow.WorkflowStatus;
import com.workflow.workflowmanagementsystem.service.DepartmentService;
import com.workflow.workflowmanagementsystem.service.WorkflowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/workflows")
public class WorkflowController {
    
    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private DepartmentService departmentService;
    
    // Get current authenticated user
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // This is a simplified approach - in production, you'd want to load the full User entity
        String username = auth.getName();
        // For now, we'll return a simple user with username
        // In a real implementation, you'd load from database
        User user = new User();
        user.setUsername(username);
        user.setId(1L); // This should be loaded from database
        return user;
    }
    
    // List all workflows
    @GetMapping
    public String listWorkflows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Workflow> workflows;
        
        if (search != null && !search.trim().isEmpty()) {
            workflows = workflowService.searchWorkflows(search, pageable);
        } else {
            WorkflowStatus statusEnum = null;
            if (status != null && !status.isEmpty()) {
                statusEnum = WorkflowStatus.valueOf(status.toUpperCase());
            }
            workflows = workflowService.getWorkflowsWithFilters(statusEnum, departmentId, isActive, pageable);
        }
        
        model.addAttribute("workflows", workflows);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", workflows.getTotalPages());
        model.addAttribute("totalItems", workflows.getTotalElements());
        model.addAttribute("status", status);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("isActive", isActive);
        model.addAttribute("search", search);
        model.addAttribute("statusOptions", WorkflowStatus.values());
        model.addAttribute("departments", departmentService.getAllDepartments());
        
        return "workflow/list";
    }
    
    // Show create workflow form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("workflow", new Workflow());
        model.addAttribute("statusOptions", WorkflowStatus.values());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "workflow/create";
    }
    
    // Process create workflow form
    @PostMapping("/create")
    public String createWorkflow(@Valid @ModelAttribute Workflow workflow,
                               BindingResult result, 
                               Model model,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        
        if (result.hasErrors()) {
            model.addAttribute("statusOptions", WorkflowStatus.values());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "workflow/create";
        }
        
        try {
            User currentUser = getCurrentUser();
            workflow.setCreatedBy(currentUser);
            Workflow savedWorkflow = workflowService.createWorkflow(workflow, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Workflow created successfully!");
            return "redirect:/workflows";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("statusOptions", WorkflowStatus.values());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "workflow/create";
        }
    }
    
    // Show edit workflow form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Workflow> workflow = workflowService.getWorkflowById(id);
        
        if (workflow.isPresent()) {
            model.addAttribute("workflow", workflow.get());
            model.addAttribute("statusOptions", WorkflowStatus.values());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "workflow/edit";
        } else {
            redirectAttributes.addFlashAttribute("error", "Workflow not found!");
            return "redirect:/workflows";
        }
    }
    
    // Process edit workflow form
    @PostMapping("/edit/{id}")
    public String updateWorkflow(@PathVariable Long id,
                               @Valid @ModelAttribute Workflow workflow,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("statusOptions", WorkflowStatus.values());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "workflow/edit";
        }
        
        try {
            User currentUser = getCurrentUser();
            Workflow updatedWorkflow = workflowService.updateWorkflow(id, workflow, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Workflow updated successfully!");
            return "redirect:/workflows";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("statusOptions", WorkflowStatus.values());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "workflow/edit";
        }
    }
    
    // View workflow details
    @GetMapping("/view/{id}")
    public String viewWorkflow(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Workflow> workflow = workflowService.getWorkflowById(id);
        
        if (workflow.isPresent()) {
            model.addAttribute("workflow", workflow.get());
            return "workflow/view";
        } else {
            redirectAttributes.addFlashAttribute("error", "Workflow not found!");
            return "redirect:/workflows";
        }
    }
    
    // Delete workflow
    @PostMapping("/delete/{id}")
    public String deleteWorkflow(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            workflowService.deleteWorkflow(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Workflow deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/workflows";
    }
    
    // Change workflow status
    @PostMapping("/status/{id}")
    public String changeStatus(@PathVariable Long id,
                             @RequestParam WorkflowStatus status,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            workflowService.changeWorkflowStatus(id, status, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Workflow status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/workflows";
    }
    
    // REST API endpoints
    
    // Get workflow by ID (API)
    @GetMapping("/api/{id}")
    @ResponseBody
    public Workflow getWorkflowById(@PathVariable Long id) {
        return workflowService.getWorkflowById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found with ID: " + id));
    }
    
    // Get workflows by status (API)
    @GetMapping("/api/status/{status}")
    @ResponseBody
    public List<Workflow> getWorkflowsByStatus(@PathVariable WorkflowStatus status) {
        return workflowService.getWorkflowsByStatus(status);
    }
    
    // Get workflows by department (API)
    @GetMapping("/api/department/{departmentId}")
    @ResponseBody
    public List<Workflow> getWorkflowsByDepartment(@PathVariable Long departmentId) {
        return workflowService.getWorkflowsByDepartment(departmentId);
    }
    
    // Get active workflows (API)
    @GetMapping("/api/active")
    @ResponseBody
    public List<Workflow> getActiveWorkflows() {
        return workflowService.getActiveWorkflows();
    }
    
    // Get workflow statistics (API)
    @GetMapping("/api/statistics")
    @ResponseBody
    public Object getWorkflowStatistics() {
        return workflowService.getWorkflowStatistics();
    }
}