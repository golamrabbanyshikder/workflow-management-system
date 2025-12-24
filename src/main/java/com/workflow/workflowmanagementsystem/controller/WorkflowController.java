package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.dto.WorkflowDto;
import com.workflow.workflowmanagementsystem.dto.WorkflowStatusLayerDto;
import com.workflow.workflowmanagementsystem.entity.Department;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.Workflow;
import com.workflow.workflowmanagementsystem.entity.WorkflowStatusLayer;
import com.workflow.workflowmanagementsystem.entity.Workflow.WorkflowStatus;
import com.workflow.workflowmanagementsystem.service.DepartmentService;
import com.workflow.workflowmanagementsystem.service.WorkflowService;
import com.workflow.workflowmanagementsystem.util.RoleUtil;

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
    
    @Autowired
    private UserRepository userRepository;
    
    // Get current authenticated user
    private User getCurrentUser() {
        return RoleUtil.getCurrentUser(userRepository);
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
    public WorkflowDto getWorkflowById(@PathVariable Long id) {
        Workflow workflow = workflowService.getWorkflowById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found with ID: " + id));
        return convertToWorkflowDto(workflow);
    }
    
    // Get workflows by status (API)
    @GetMapping("/api/status/{status}")
    @ResponseBody
    public List<WorkflowDto> getWorkflowsByStatus(@PathVariable WorkflowStatus status) {
        List<Workflow> workflows = workflowService.getWorkflowsByStatus(status);
        return workflows.stream()
                .map(this::convertToWorkflowDto)
                .collect(Collectors.toList());
    }
    
    // Get workflows by department (API)
    @GetMapping("/api/department/{departmentId}")
    @ResponseBody
    public List<WorkflowDto> getWorkflowsByDepartment(@PathVariable Long departmentId) {
        List<Workflow> workflows = workflowService.getWorkflowsByDepartment(departmentId);
        return workflows.stream()
                .map(this::convertToWorkflowDto)
                .collect(Collectors.toList());
    }
    
    // Get active workflows (API)
    @GetMapping("/api/active")
    @ResponseBody
    public List<WorkflowDto> getActiveWorkflows() {
        List<Workflow> workflows = workflowService.getActiveWorkflows();
        return workflows.stream()
                .map(this::convertToWorkflowDto)
                .collect(Collectors.toList());
    }
    
    // Get workflow statistics (API)
    @GetMapping("/api/statistics")
    @ResponseBody
    public Object getWorkflowStatistics() {
        return workflowService.getWorkflowStatistics();
    }
    
    // Workflow Status Layer Management
    
    // Show workflow status layers
    @GetMapping("/status-layers/{workflowId}")
    public String showStatusLayers(@PathVariable Long workflowId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Workflow> workflow = workflowService.getWorkflowById(workflowId);
        
        if (workflow.isPresent()) {
            model.addAttribute("workflow", workflow.get());
            model.addAttribute("statusLayers", workflowService.getStatusLayersForWorkflow(workflowId));
            return "workflow/status-layers";
        } else {
            redirectAttributes.addFlashAttribute("error", "Workflow not found!");
            return "redirect:/workflows";
        }
    }
    
    // Show add status layer form
    @GetMapping("/status-layers/add/{workflowId}")
    public String showAddStatusLayerForm(@PathVariable Long workflowId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Workflow> workflow = workflowService.getWorkflowById(workflowId);
        
        if (workflow.isPresent()) {
            model.addAttribute("workflow", workflow.get());
            model.addAttribute("statusLayer", new WorkflowStatusLayer());
            return "workflow/add-status-layer";
        } else {
            redirectAttributes.addFlashAttribute("error", "Workflow not found!");
            return "redirect:/workflows";
        }
    }
    
    // Process add status layer form
    @PostMapping("/status-layers/add/{workflowId}")
    public String addStatusLayer(@PathVariable Long workflowId,
                                @Valid @ModelAttribute WorkflowStatusLayer statusLayer,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            Optional<Workflow> workflow = workflowService.getWorkflowById(workflowId);
            workflow.ifPresent(w -> model.addAttribute("workflow", w));
            model.addAttribute("statusLayer", statusLayer);
            return "workflow/add-status-layer";
        }
        
        try {
            User currentUser = getCurrentUser();
            workflowService.addStatusLayerToWorkflow(workflowId, statusLayer, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Status layer added successfully!");
            return "redirect:/workflows/status-layers/" + workflowId;
        } catch (Exception e) {
            Optional<Workflow> workflow = workflowService.getWorkflowById(workflowId);
            workflow.ifPresent(w -> model.addAttribute("workflow", w));
            model.addAttribute("statusLayer", statusLayer);
            model.addAttribute("error", e.getMessage());
            return "workflow/add-status-layer";
        }
    }
    
    // Show edit status layer form
    @GetMapping("/status-layers/edit/{id}")
    public String showEditStatusLayerForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        // We need to find the status layer and its workflow
        // For simplicity, we'll assume the status layer exists and get its workflow
        // In a real implementation, you might want to add a method to WorkflowService to get status layer by ID
        return "workflow/edit-status-layer";
    }
    
    // Process edit status layer form
    @PostMapping("/status-layers/edit/{id}")
    public String editStatusLayer(@PathVariable Long id,
                               @Valid @ModelAttribute WorkflowStatusLayer statusLayer,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("statusLayer", statusLayer);
            model.addAttribute("error", "Please fix the errors below");
            return "workflow/edit-status-layer";
        }
        
        try {
            User currentUser = getCurrentUser();
            workflowService.updateStatusLayer(id, statusLayer, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Status layer updated successfully!");
            // Redirect back to the workflow's status layers page
            return "redirect:/workflows/status-layers/" + statusLayer.getWorkflow().getId();
        } catch (Exception e) {
            model.addAttribute("statusLayer", statusLayer);
            model.addAttribute("error", e.getMessage());
            return "workflow/edit-status-layer";
        }
    }
    
    // Delete status layer
    @PostMapping("/status-layers/delete/{id}")
    public String deleteStatusLayer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            // Get the status layer to find its workflow for redirect
            // We need to get the status layer by ID first, then get its workflow
            // This is a temporary fix - you should implement a proper method in WorkflowService
            List<WorkflowStatusLayer> allStatusLayers = workflowService.getAllStatusLayers();
            WorkflowStatusLayer statusLayer = allStatusLayers.stream()
                    .filter(sl -> sl.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            
            if (statusLayer != null) {
                workflowService.deleteStatusLayer(id, currentUser.getId());
                redirectAttributes.addFlashAttribute("success", "Status layer deleted successfully!");
                return "redirect:/workflows/status-layers/" + statusLayer.getWorkflow().getId();
            } else {
                redirectAttributes.addFlashAttribute("error", "Status layer not found!");
                return "redirect:/workflows";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/workflows";
        }
    }
    
    // API endpoint to get workflow status layers
    @GetMapping("/api/status-layers/{workflowId}")
    @ResponseBody
    public List<WorkflowStatusLayerDto> getWorkflowStatusLayers(@PathVariable Long workflowId) {
        List<WorkflowStatusLayer> statusLayers = workflowService.getStatusLayersForWorkflow(workflowId);
        return statusLayers.stream()
                .map(this::convertToWorkflowStatusLayerDto)
                .collect(Collectors.toList());
    }
    
    // Helper method to convert Workflow to WorkflowDto
    private WorkflowDto convertToWorkflowDto(Workflow workflow) {
        WorkflowDto dto = new WorkflowDto();
        dto.setId(workflow.getId());
        dto.setName(workflow.getName());
        dto.setDescription(workflow.getDescription());
        dto.setStatus(workflow.getStatus() != null ? workflow.getStatus().name() : null);
        dto.setIsActive(workflow.getIsActive());
        return dto;
    }
    
    // Helper method to convert WorkflowStatusLayer to WorkflowStatusLayerDto
    private WorkflowStatusLayerDto convertToWorkflowStatusLayerDto(WorkflowStatusLayer statusLayer) {
        WorkflowStatusLayerDto dto = new WorkflowStatusLayerDto();
        dto.setId(statusLayer.getId());
        dto.setName(statusLayer.getName());
        dto.setDescription(statusLayer.getDescription());
        dto.setOrder(statusLayer.getOrder());
        dto.setIsFinal(statusLayer.getIsFinal());
        dto.setColor(statusLayer.getColor());
        dto.setWorkflowId(statusLayer.getWorkflow() != null ? statusLayer.getWorkflow().getId() : null);
        return dto;
    }
}