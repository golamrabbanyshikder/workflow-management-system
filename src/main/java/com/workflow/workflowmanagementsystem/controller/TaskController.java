package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRoleRepository;
import com.workflow.workflowmanagementsystem.dto.TaskDto;
import com.workflow.workflowmanagementsystem.dto.WorkflowDto;
import com.workflow.workflowmanagementsystem.dto.WorkflowStatusLayerDto;
import com.workflow.workflowmanagementsystem.entity.Task;
import com.workflow.workflowmanagementsystem.entity.Task.TaskPriority;
import com.workflow.workflowmanagementsystem.entity.Task.TaskStatus;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.Workflow;
import com.workflow.workflowmanagementsystem.entity.WorkflowStatusLayer;
import com.workflow.workflowmanagementsystem.service.TaskService;
import com.workflow.workflowmanagementsystem.service.UserService;
import com.workflow.workflowmanagementsystem.service.WorkflowService;
import com.workflow.workflowmanagementsystem.service.DepartmentService;
import com.workflow.workflowmanagementsystem.util.RoleUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.persistence.EntityNotFoundException;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    // Get current authenticated user
    private User getCurrentUser() {
        return RoleUtil.getCurrentUser(userRepository);
    }
    
    // List all tasks
    @GetMapping
    public String listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) Long workflowId,
            @RequestParam(required = false) Long workflowStatusLayerId,
            @RequestParam(required = false) String search,
            Model model,
            HttpServletRequest request) {
        
        // Set user roles in session
        User currentUser = getCurrentUser();
        RoleUtil.setUserRolesInSession(currentUser, userRoleRepository, request);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> tasks;
        
        if (search != null && !search.trim().isEmpty()) {
            tasks = taskService.searchTasks(search, pageable);
        } else {
            TaskPriority priorityEnum = null;
            if (priority != null && !priority.isEmpty()) {
                priorityEnum = TaskPriority.valueOf(priority.toUpperCase());
            }
            
            // Use workflow status layer filtering if provided, otherwise use regular filtering
            if (workflowStatusLayerId != null) {
                tasks = taskService.getTasksWithWorkflowStatusLayerFilter(workflowStatusLayerId, priorityEnum, assignedToId, pageable);
            } else {
                tasks = taskService.getTasksWithFilters(null, priorityEnum, assignedToId, workflowId, pageable);
            }
        }
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tasks.getTotalPages());
        model.addAttribute("totalItems", tasks.getTotalElements());
        model.addAttribute("priority", priority);
        model.addAttribute("assignedToId", assignedToId);
        model.addAttribute("workflowId", workflowId);
        model.addAttribute("workflowStatusLayerId", workflowStatusLayerId);
        model.addAttribute("search", search);
        model.addAttribute("priorityOptions", TaskPriority.values());
        model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
        
        // Add status layers for the selected workflow if any
        if (workflowId != null) {
            model.addAttribute("statusLayers", workflowService.getStatusLayersForWorkflow(workflowId));
        }
        
        // Add current workflow status layer if any
        if (workflowStatusLayerId != null) {
            model.addAttribute("selectedWorkflowStatusLayerId", workflowStatusLayerId);
        }
        
        return "task/list";
    }
    
    // Show create task form
    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Long workflowId, Model model, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = getCurrentUser();
        RoleUtil.setUserRolesInSession(currentUser, userRoleRepository, request);
        Task task = new Task();
        List<WorkflowStatusLayer> statusLayers = null;
        
        if (workflowId != null) {
            Optional<Workflow> workflow = workflowService.getWorkflowById(workflowId);
            workflow.ifPresent(task::setWorkflow);
            // Get status layers for the selected workflow
            statusLayers = workflowService.getStatusLayersForWorkflow(workflowId);
        }
        
        model.addAttribute("task", task);
        model.addAttribute("statusOptions", TaskStatus.values());
        model.addAttribute("priorityOptions", TaskPriority.values());
        model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
        model.addAttribute("users", userService.getAllUsers(PageRequest.of(0, 100)).getContent());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("statusLayers", statusLayers);
        return "task/create";
    }
    
    // Process create task form
    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute Task task,
                           BindingResult result,
                           @RequestParam(required = false) Long workflowStatusLayerId,
                           Model model,
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest request) {
        
        if (result.hasErrors()) {
            List<WorkflowStatusLayer> statusLayers = task.getWorkflow() != null ?
                    workflowService.getStatusLayersForWorkflow(task.getWorkflow().getId()) : null;
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            model.addAttribute("users", userService.getAllUsers(PageRequest.of(0, 100)).getContent());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statusLayers", statusLayers);
            return "task/create";
        }
        
        try {
            User currentUser = getCurrentUser();
            task.setCreatedBy(currentUser);
            
            Task savedTask;
            if (workflowStatusLayerId != null) {
                // Create task with workflow status layer
                savedTask = taskService.createTaskWithWorkflowStatus(task, workflowStatusLayerId, currentUser.getId());
            } else {
                // Create task with default status
                savedTask = taskService.createTask(task, currentUser.getId());
            }
            
            redirectAttributes.addFlashAttribute("success", "Task created successfully!");
            return "redirect:/tasks";
        } catch (EntityNotFoundException e) {
            List<WorkflowStatusLayer> statusLayers = task.getWorkflow() != null ?
                    workflowService.getStatusLayersForWorkflow(task.getWorkflow().getId()) : null;
            model.addAttribute("error", "Entity not found: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            model.addAttribute("users", userService.getAllUsers(PageRequest.of(0, 100)).getContent());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statusLayers", statusLayers);
            return "task/create";
        } catch (IllegalArgumentException e) {
            List<WorkflowStatusLayer> statusLayers = task.getWorkflow() != null ?
                    workflowService.getStatusLayersForWorkflow(task.getWorkflow().getId()) : null;
            model.addAttribute("error", "Invalid input: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            model.addAttribute("users", userService.getAllUsers(PageRequest.of(0, 100)).getContent());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statusLayers", statusLayers);
            return "task/create";
        } catch (Exception e) {
            List<WorkflowStatusLayer> statusLayers = task.getWorkflow() != null ?
                    workflowService.getStatusLayersForWorkflow(task.getWorkflow().getId()) : null;
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            model.addAttribute("users", userService.getAllUsers(PageRequest.of(0, 100)).getContent());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statusLayers", statusLayers);
            return "task/create";
        }
    }
    
    // Show edit task form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = getCurrentUser();
        RoleUtil.setUserRolesInSession(currentUser, userRoleRepository, request);
        Optional<Task> taskOpt = taskService.getTaskById(id);
        
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            model.addAttribute("task", task);
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            model.addAttribute("users", userService.getAllUsers(PageRequest.of(0, 100)).getContent());
            model.addAttribute("departments", departmentService.getAllDepartments());
            
            // Add workflow status layers for the current workflow
            if (task.getWorkflow() != null) {
                List<WorkflowStatusLayer> statusLayers = workflowService.getStatusLayersForWorkflow(task.getWorkflow().getId());
                model.addAttribute("statusLayers", statusLayers);
            }
            
            return "task/edit";
        } else {
            redirectAttributes.addFlashAttribute("error", "Task not found!");
            return "redirect:/tasks";
        }
    }
    
    // Process edit task form
    @PostMapping("/edit/{id}")
    public String updateTask(@PathVariable Long id,
                           @Valid @ModelAttribute Task task,
                           BindingResult result,
                           @RequestParam(required = false) Long workflowStatusLayerId,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            model.addAttribute("users", userService.getAllUsers(PageRequest.of(0, 100)).getContent());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "task/edit";
        }
        
        try {
            User currentUser = getCurrentUser();
            Task updatedTask;
            
            if (workflowStatusLayerId != null) {
                // Update task with workflow status layer
                updatedTask = taskService.changeTaskWorkflowStatus(id, workflowStatusLayerId, currentUser.getId());
                // Also update other task fields
                Task existingTask = taskService.getTaskById(id).orElseThrow(() ->
                    new EntityNotFoundException("Task not found with ID: " + id));
                existingTask.setTitle(task.getTitle());
                existingTask.setDescription(task.getDescription());
                existingTask.setPriority(task.getPriority());
                existingTask.setDueDate(task.getDueDate());
                existingTask.setEstimatedHours(task.getEstimatedHours());
                existingTask.setActualHours(task.getActualHours());
                
                if (task.getAssignedTo() != null && task.getAssignedTo().getId() != null) {
                    User assignedTo = userService.getUserById(task.getAssignedTo().getId())
                            .orElseThrow(() -> new EntityNotFoundException("Assigned user not found"));
                    existingTask.setAssignedTo(assignedTo);
                }
                
                updatedTask = taskService.updateTask(id, existingTask, currentUser.getId());
            } else {
                // Update task normally
                updatedTask = taskService.updateTask(id, task, currentUser.getId());
            }
            
            redirectAttributes.addFlashAttribute("success", "Task updated successfully!");
            return "redirect:/tasks";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "Task not found: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            model.addAttribute("users", userService.getAllUsers(PageRequest.of(0, 100)).getContent());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "task/edit";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid input: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            model.addAttribute("users", userService.getAllUsers(PageRequest.of(0, 100)).getContent());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "task/edit";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            model.addAttribute("users", userService.getAllUsers(PageRequest.of(0, 100)).getContent());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "task/edit";
        }
    }
    
    // View task details
    @GetMapping("/view/{id}")
    public String viewTask(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = getCurrentUser();
        RoleUtil.setUserRolesInSession(currentUser, userRoleRepository, request);
        Optional<Task> task = taskService.getTaskById(id);
        
        if (task.isPresent()) {
            model.addAttribute("task", task.get());
            model.addAttribute("users", userService.getAllUsers(PageRequest.of(0, 100)).getContent());
            return "task/view";
        } else {
            redirectAttributes.addFlashAttribute("error", "Task not found!");
            return "redirect:/tasks";
        }
    }
    
    // Delete task
    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            taskService.deleteTask(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Task deleted successfully!");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Task not found: " + e.getMessage());
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this task: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/tasks";
    }
    
    // Assign task to user
    @PostMapping("/assign/{id}")
    public String assignTask(@PathVariable Long id,
                           @RequestParam Long assignedToId,
                           RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            taskService.assignTask(id, assignedToId, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Task assigned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks";
    }
    
    // Change task status
    @PostMapping("/status/{id}")
    public String changeStatus(@PathVariable Long id,
                           @RequestParam TaskStatus status,
                           RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            taskService.changeTaskStatus(id, status, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Task status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks";
    }
    
    // Complete task
    @PostMapping("/complete/{id}")
    public String completeTask(@PathVariable Long id,
                           @RequestParam(required = false) Integer actualHours,
                           RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            taskService.completeTask(id, actualHours, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Task completed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks";
    }
    
    // My Tasks page
    @GetMapping("/my-tasks")
    public String myTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            Model model,
            HttpServletRequest request) {
        
        User currentUser = getCurrentUser();
        // Set user roles in session
        RoleUtil.setUserRolesInSession(currentUser, userRoleRepository, request);
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        
        TaskStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            statusEnum = TaskStatus.valueOf(status.toUpperCase());
        }
        
        TaskPriority priorityEnum = null;
        if (priority != null && !priority.isEmpty()) {
            priorityEnum = TaskPriority.valueOf(priority.toUpperCase());
        }
        
        Page<Task> tasks = taskService.getTasksForUser(currentUser.getId(), statusEnum, priorityEnum, pageable);
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tasks.getTotalPages());
        model.addAttribute("totalItems", tasks.getTotalElements());
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("statusOptions", TaskStatus.values());
        model.addAttribute("priorityOptions", TaskPriority.values());
        
        return "task/my-tasks";
    }
    
    // REST API endpoints
    
    // Get task by ID (API)
    @GetMapping("/api/{id}")
    @ResponseBody
    public TaskDto getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + id));
        return convertToTaskDto(task);
    }
    
    // Get tasks by status (API)
    @GetMapping("/api/status/{status}")
    @ResponseBody
    public List<TaskDto> getTasksByStatus(@PathVariable TaskStatus status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        return tasks.stream()
                .map(this::convertToTaskDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Get tasks by priority (API)
    @GetMapping("/api/priority/{priority}")
    @ResponseBody
    public List<TaskDto> getTasksByPriority(@PathVariable TaskPriority priority) {
        List<Task> tasks = taskService.getTasksByPriority(priority);
        return tasks.stream()
                .map(this::convertToTaskDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Get tasks assigned to user (API)
    @GetMapping("/api/assigned/{userId}")
    @ResponseBody
    public List<TaskDto> getTasksByAssignedUser(@PathVariable Long userId) {
        List<Task> tasks = taskService.getTasksByAssignedUser(userId);
        return tasks.stream()
                .map(this::convertToTaskDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Get overdue tasks (API)
    @GetMapping("/api/overdue")
    @ResponseBody
    public List<TaskDto> getOverdueTasks() {
        List<Task> tasks = taskService.getOverdueTasks();
        return tasks.stream()
                .map(this::convertToTaskDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Get tasks due within next 7 days (API)
    @GetMapping("/api/due-soon")
    @ResponseBody
    public List<TaskDto> getTasksDueSoon() {
        List<Task> tasks = taskService.getTasksDueWithinDays(7);
        return tasks.stream()
                .map(this::convertToTaskDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Get task statistics (API)
    @GetMapping("/api/statistics")
    @ResponseBody
    public Object getTaskStatistics() {
        return taskService.getTaskStatistics();
    }
    
    // Change task workflow status layer
    @PostMapping("/workflow-status/{id}")
    public String changeTaskWorkflowStatus(@PathVariable Long id,
                                     @RequestParam Long workflowStatusLayerId,
                                     RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            taskService.changeTaskWorkflowStatus(id, workflowStatusLayerId, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Task workflow status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks";
    }
    
    // Get next workflow status layers for a task (API)
    @GetMapping("/api/next-status-layers/{taskId}")
    @ResponseBody
    public List<WorkflowStatusLayerDto> getNextWorkflowStatusLayers(@PathVariable Long taskId) {
        List<WorkflowStatusLayer> statusLayers = taskService.getNextWorkflowStatusLayers(taskId);
        return statusLayers.stream()
                .map(this::convertToWorkflowStatusLayerDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Get tasks by workflow status layer (API)
    @GetMapping("/api/workflow-status-layer/{workflowStatusLayerId}")
    @ResponseBody
    public List<TaskDto> getTasksByWorkflowStatusLayer(@PathVariable Long workflowStatusLayerId) {
        List<Task> tasks = taskService.getTasksByWorkflowStatusLayer(workflowStatusLayerId);
        return tasks.stream()
                .map(this::convertToTaskDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // API endpoint to get workflows by department (for dynamic loading)
    @GetMapping("/api/workflows-by-department/{departmentId}")
    @ResponseBody
    public List<WorkflowDto> getWorkflowsByDepartment(@PathVariable Long departmentId) {
        List<Workflow> workflows = workflowService.getWorkflowsByDepartment(departmentId);
        return workflows.stream()
                .map(this::convertToWorkflowDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // API endpoint to get status layers by workflow (for dynamic loading)
    @GetMapping("/api/status-layers-by-workflow/{workflowId}")
    @ResponseBody
    public List<WorkflowStatusLayerDto> getStatusLayersByWorkflow(@PathVariable Long workflowId) {
        List<WorkflowStatusLayer> statusLayers = workflowService.getStatusLayersForWorkflow(workflowId);
        return statusLayers.stream()
                .map(this::convertToWorkflowStatusLayerDto)
                .collect(java.util.stream.Collectors.toList());
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
    
    // Helper method to convert Task to TaskDto
    private TaskDto convertToTaskDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        // Status is now derived from workflowStatusLayer
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setAssignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null);
        dto.setAssignedToName(task.getAssignedTo() != null ?
                (task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName()) : null);
        dto.setWorkflowId(task.getWorkflow() != null ? task.getWorkflow().getId() : null);
        dto.setWorkflowName(task.getWorkflow() != null ? task.getWorkflow().getName() : null);
        dto.setWorkflowStatusLayerId(task.getWorkflowStatusLayer() != null ? task.getWorkflowStatusLayer().getId() : null);
        dto.setWorkflowStatusLayerName(task.getWorkflowStatusLayer() != null ? task.getWorkflowStatusLayer().getName() : null);
        dto.setCreatedById(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null);
        dto.setCreatedByName(task.getCreatedBy() != null ?
                (task.getCreatedBy().getFirstName() + " " + task.getCreatedBy().getLastName()) : null);
        dto.setCreatedAt(task.getCreatedAt());
        dto.setDueDate(task.getDueDate());
        dto.setEstimatedHours(task.getEstimatedHours());
        dto.setActualHours(task.getActualHours());
        
        // Add team information
        if (task.getAssignedTo() != null && task.getAssignedTo().getTeam() != null) {
            dto.setTeamId(task.getAssignedTo().getTeam().getId());
            dto.setTeamName(task.getAssignedTo().getTeam().getName());
            dto.setTeamMemberCount(task.getAssignedTo().getTeam().getMemberCount());
        }
        
        return dto;
    }
}