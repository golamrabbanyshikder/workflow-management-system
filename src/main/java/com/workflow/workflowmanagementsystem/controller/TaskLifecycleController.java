package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.Repository.TaskRepository;
import com.workflow.workflowmanagementsystem.entity.Task;
import com.workflow.workflowmanagementsystem.entity.WorkflowStatusLayer;
import com.workflow.workflowmanagementsystem.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class TaskLifecycleController {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private WorkflowService workflowService;
    
    @GetMapping("/task/lifecycle")
    public String taskLifecycle(Model model) {
        // Get all tasks
        List<Task> allTasks = taskRepository.findAll();
        
        // Get all workflow status layers for the lifecycle view
        List<WorkflowStatusLayer> allStatusLayers = workflowService.getAllStatusLayers();
        
        // Calculate statistics using workflow status layers
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalTasks", (long) allTasks.size());
        
        // Count tasks by workflow status layers
        if (allStatusLayers != null && !allStatusLayers.isEmpty()) {
            for (WorkflowStatusLayer statusLayer : allStatusLayers) {
                String layerName = statusLayer.getName();
                long count = allTasks.stream()
                        .filter(task -> task.getWorkflowStatusLayer() != null &&
                                       task.getWorkflowStatusLayer().getId().equals(statusLayer.getId()))
                        .count();
                stats.put(layerName.replace(" ", "_") + "Tasks", count);
            }
        }
        
        // Count tasks by derived status for backward compatibility
        stats.put("completedTasks", allTasks.stream().filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED).count());
        stats.put("inProgressTasks", allTasks.stream().filter(task -> task.getStatus() == Task.TaskStatus.IN_PROGRESS).count());
        stats.put("pendingTasks", allTasks.stream().filter(task -> task.getStatus() == Task.TaskStatus.PENDING).count());
        stats.put("onHoldTasks", allTasks.stream().filter(task -> task.getStatus() == Task.TaskStatus.ON_HOLD).count());
        stats.put("cancelledTasks", allTasks.stream().filter(task -> task.getStatus() == Task.TaskStatus.CANCELLED).count());
        
        model.addAttribute("statusLayers", allStatusLayers);
        model.addAttribute("stats", stats);
        model.addAttribute("totalTasks", stats.get("totalTasks"));
        model.addAttribute("completedTasks", stats.get("completedTasks"));
        model.addAttribute("inProgressTasks", stats.get("inProgressTasks"));
        model.addAttribute("overdueTasks", allTasks.stream()
                .filter(task -> task.getDueDate() != null &&
                               task.getDueDate().isBefore(java.time.LocalDateTime.now()) &&
                               task.getStatus() != Task.TaskStatus.COMPLETED)
                .count());
        
        return "task/lifecycle";
    }
    
    @GetMapping("/task/lifecycle/{id}")
    public String individualTaskLifecycle(@PathVariable Long id, Model model) {
        // Get specific task
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));
        
        // Get all tasks for statistics
        List<Task> allTasks = taskRepository.findAll();
        
        // Get workflow status layers for the specific task's workflow
        List<WorkflowStatusLayer> workflowStatusLayers = workflowService.getStatusLayersForWorkflow(task.getWorkflow().getId());
        
        // Calculate statistics using workflow status layers
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalTasks", (long) allTasks.size());
        
        // Count tasks by workflow status layers for this specific workflow
        if (workflowStatusLayers != null && !workflowStatusLayers.isEmpty()) {
            for (WorkflowStatusLayer statusLayer : workflowStatusLayers) {
                String layerName = statusLayer.getName();
                long count = allTasks.stream()
                        .filter(t -> t.getWorkflowStatusLayer() != null &&
                                       t.getWorkflowStatusLayer().getId().equals(statusLayer.getId()))
                        .count();
                stats.put(layerName.replace(" ", "_") + "Tasks", count);
            }
        }
        
        // Count tasks by derived status for backward compatibility
        stats.put("completedTasks", allTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count());
        stats.put("inProgressTasks", allTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS).count());
        stats.put("pendingTasks", allTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.PENDING).count());
        stats.put("onHoldTasks", allTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.ON_HOLD).count());
        stats.put("cancelledTasks", allTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.CANCELLED).count());
        
        model.addAttribute("task", task);
        model.addAttribute("statusLayers", workflowStatusLayers);
        model.addAttribute("stats", stats);
        model.addAttribute("totalTasks", stats.get("totalTasks"));
        model.addAttribute("completedTasks", stats.get("completedTasks"));
        model.addAttribute("inProgressTasks", stats.get("inProgressTasks"));
        model.addAttribute("overdueTasks", allTasks.stream()
                .filter(t -> t.getDueDate() != null &&
                               t.getDueDate().isBefore(java.time.LocalDateTime.now()) &&
                               t.getStatus() != Task.TaskStatus.COMPLETED)
                .count());
        
        return "task/individual-lifecycle";
    }
}