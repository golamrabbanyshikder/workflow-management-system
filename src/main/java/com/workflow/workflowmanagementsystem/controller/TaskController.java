package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.entity.Task;
import com.workflow.workflowmanagementsystem.entity.Task.TaskPriority;
import com.workflow.workflowmanagementsystem.entity.Task.TaskStatus;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.Workflow;
import com.workflow.workflowmanagementsystem.service.TaskService;
import com.workflow.workflowmanagementsystem.service.UserService;
import com.workflow.workflowmanagementsystem.service.WorkflowService;
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
    private UserRepository userRepository;
    
    // Get current authenticated user
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
    
    // List all tasks
    @GetMapping
    public String listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) Long workflowId,
            @RequestParam(required = false) String search,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> tasks;
        
        if (search != null && !search.trim().isEmpty()) {
            tasks = taskService.searchTasks(search, pageable);
        } else {
            TaskStatus statusEnum = null;
            if (status != null && !status.isEmpty()) {
                statusEnum = TaskStatus.valueOf(status.toUpperCase());
            }
            
            TaskPriority priorityEnum = null;
            if (priority != null && !priority.isEmpty()) {
                priorityEnum = TaskPriority.valueOf(priority.toUpperCase());
            }
            
            tasks = taskService.getTasksWithFilters(statusEnum, priorityEnum, assignedToId, workflowId, pageable);
        }
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tasks.getTotalPages());
        model.addAttribute("totalItems", tasks.getTotalElements());
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("assignedToId", assignedToId);
        model.addAttribute("workflowId", workflowId);
        model.addAttribute("search", search);
        model.addAttribute("statusOptions", TaskStatus.values());
        model.addAttribute("priorityOptions", TaskPriority.values());
        model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
        
        return "task/list";
    }
    
    // Show create task form
    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Long workflowId, Model model) {
        Task task = new Task();
        if (workflowId != null) {
            Optional<Workflow> workflow = workflowService.getWorkflowById(workflowId);
            workflow.ifPresent(task::setWorkflow);
        }
        
        model.addAttribute("task", task);
        model.addAttribute("statusOptions", TaskStatus.values());
        model.addAttribute("priorityOptions", TaskPriority.values());
        model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
        return "task/create";
    }
    
    // Process create task form
    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute Task task,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest request) {
        
        if (result.hasErrors()) {
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            return "task/create";
        }
        
        try {
            User currentUser = getCurrentUser();
            task.setCreatedBy(currentUser);
            Task savedTask = taskService.createTask(task, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Task created successfully!");
            return "redirect:/tasks";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "Entity not found: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            return "task/create";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid input: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            return "task/create";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            return "task/create";
        }
    }
    
    // Show edit task form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Task> task = taskService.getTaskById(id);
        
        if (task.isPresent()) {
            model.addAttribute("task", task.get());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
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
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            return "task/edit";
        }
        
        try {
            User currentUser = getCurrentUser();
            Task updatedTask = taskService.updateTask(id, task, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Task updated successfully!");
            return "redirect:/tasks";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "Task not found: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            return "task/edit";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid input: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            return "task/edit";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("statusOptions", TaskStatus.values());
            model.addAttribute("priorityOptions", TaskPriority.values());
            model.addAttribute("workflows", workflowService.getAllWorkflows(PageRequest.of(0, 100)).getContent());
            return "task/edit";
        }
    }
    
    // View task details
    @GetMapping("/view/{id}")
    public String viewTask(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Task> task = taskService.getTaskById(id);
        
        if (task.isPresent()) {
            model.addAttribute("task", task.get());
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
            Model model) {
        
        User currentUser = getCurrentUser();
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
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + id));
    }
    
    // Get tasks by status (API)
    @GetMapping("/api/status/{status}")
    @ResponseBody
    public List<Task> getTasksByStatus(@PathVariable TaskStatus status) {
        return taskService.getTasksByStatus(status);
    }
    
    // Get tasks by priority (API)
    @GetMapping("/api/priority/{priority}")
    @ResponseBody
    public List<Task> getTasksByPriority(@PathVariable TaskPriority priority) {
        return taskService.getTasksByPriority(priority);
    }
    
    // Get tasks assigned to user (API)
    @GetMapping("/api/assigned/{userId}")
    @ResponseBody
    public List<Task> getTasksByAssignedUser(@PathVariable Long userId) {
        return taskService.getTasksByAssignedUser(userId);
    }
    
    // Get overdue tasks (API)
    @GetMapping("/api/overdue")
    @ResponseBody
    public List<Task> getOverdueTasks() {
        return taskService.getOverdueTasks();
    }
    
    // Get tasks due within next 7 days (API)
    @GetMapping("/api/due-soon")
    @ResponseBody
    public List<Task> getTasksDueSoon() {
        return taskService.getTasksDueWithinDays(7);
    }
    
    // Get task statistics (API)
    @GetMapping("/api/statistics")
    @ResponseBody
    public Object getTaskStatistics() {
        return taskService.getTaskStatistics();
    }
}