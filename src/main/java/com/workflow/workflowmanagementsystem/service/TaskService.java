package com.workflow.workflowmanagementsystem.service;

import com.workflow.workflowmanagementsystem.Repository.AuditLogRepository;
import com.workflow.workflowmanagementsystem.Repository.TaskRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.Repository.WorkflowRepository;
import com.workflow.workflowmanagementsystem.Repository.WorkflowStatusLayerRepository;
import com.workflow.workflowmanagementsystem.entity.AuditLog;
import com.workflow.workflowmanagementsystem.entity.Task;
import com.workflow.workflowmanagementsystem.entity.Task.TaskPriority;
import com.workflow.workflowmanagementsystem.entity.Task.TaskStatus;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.Workflow;
import com.workflow.workflowmanagementsystem.entity.WorkflowStatusLayer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private WorkflowStatusLayerRepository workflowStatusLayerRepository;
    
    // Create a new task
    public Task createTask(Task task, Long createdByUserId) {
        // Validate workflow exists
        Workflow workflow = workflowRepository.findById(task.getWorkflow().getId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with ID: " + task.getWorkflow().getId()));
        
        // Validate creator
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + createdByUserId));
        
        // Validate assigned user if provided
        if (task.getAssignedTo() != null && task.getAssignedTo().getId() != null) {
            User assignedTo = userRepository.findById(task.getAssignedTo().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Assigned user not found with ID: " + task.getAssignedTo().getId()));
            task.setAssignedTo(assignedTo);
        }
        
        // Check for duplicate task title in workflow
        if (taskRepository.existsByTitleIgnoreCaseAndWorkflowId(task.getTitle(), workflow.getId())) {
            throw new IllegalArgumentException("Task with title '" + task.getTitle() + "' already exists in this workflow");
        }
        
        task.setWorkflow(workflow);
        task.setCreatedBy(createdBy);
        
        // Set default workflow status layer if not provided
        if (task.getWorkflowStatusLayer() == null) {
            WorkflowStatusLayer firstStatusLayer = workflow.getFirstStatusLayer();
            if (firstStatusLayer != null) {
                task.setWorkflowStatusLayer(firstStatusLayer);
            }
        }
        
        Task savedTask = this.taskRepository.save(task);
        
        // Log the creation
        logAuditAction(AuditLog.ActionType.CREATE, "Task", savedTask.getId(),
                      "Created task: " + savedTask.getTitle() + " in workflow: " + workflow.getName(), createdBy);
        
        return savedTask;
    }
    
    // Update an existing task
    public Task updateTask(Long id, Task taskDetails, Long updatedByUserId) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + id));
        
        User updatedBy = userRepository.findById(updatedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + updatedByUserId));
        
        // Check title uniqueness if title is being changed
        if (!existingTask.getTitle().equalsIgnoreCase(taskDetails.getTitle()) &&
            taskRepository.existsByTitleIgnoreCaseAndWorkflowIdAndIdNot(
                taskDetails.getTitle(), existingTask.getWorkflow().getId(), id)) {
            throw new IllegalArgumentException("Task with title '" + taskDetails.getTitle() + "' already exists in this workflow");
        }
        
        // Store old values for audit
        String oldValues = String.format("Title: %s, Status: %s, Priority: %s, AssignedTo: %s",
                existingTask.getTitle(),
                existingTask.getWorkflowStatusLayer() != null ? existingTask.getWorkflowStatusLayer().getName() : "None",
                existingTask.getPriority(),
                existingTask.getAssignedTo() != null ? existingTask.getAssignedTo().getUsername() : "Unassigned");
        
        // Update fields
        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        // Status is now derived from workflowStatusLayer, not set directly
        existingTask.setPriority(taskDetails.getPriority());
        existingTask.setDueDate(taskDetails.getDueDate());
        existingTask.setEstimatedHours(taskDetails.getEstimatedHours());
        existingTask.setActualHours(taskDetails.getActualHours());
        
        // Update assigned user if provided
        if (taskDetails.getAssignedTo() != null && taskDetails.getAssignedTo().getId() != null) {
            User assignedTo = userRepository.findById(taskDetails.getAssignedTo().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Assigned user not found with ID: " + taskDetails.getAssignedTo().getId()));
            existingTask.setAssignedTo(assignedTo);
        }
        
        Task updatedTask = this.taskRepository.save(existingTask);
        
        // Log the update
        String newValues = String.format("Title: %s, Status: %s, Priority: %s, AssignedTo: %s",
                updatedTask.getTitle(),
                updatedTask.getWorkflowStatusLayer() != null ? updatedTask.getWorkflowStatusLayer().getName() : "None",
                updatedTask.getPriority(),
                updatedTask.getAssignedTo() != null ? updatedTask.getAssignedTo().getUsername() : "Unassigned");
        
        logAuditAction(AuditLog.ActionType.UPDATE, "Task", updatedTask.getId(), 
                      "Updated task: " + updatedTask.getTitle(), updatedBy, oldValues, newValues);
        
        return updatedTask;
    }
    
    // Get task by ID
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
    
    // Get all tasks with pagination
    public Page<Task> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }
    
    // Get tasks by status - method removed as status is now derived from workflowStatusLayer
    public List<Task> getTasksByStatus(TaskStatus status) {
        // This method is deprecated as status is now derived from workflowStatusLayer
        // Return empty list to maintain compatibility
        return new java.util.ArrayList<>();
    }
    
    // Get tasks by priority
    public List<Task> getTasksByPriority(TaskPriority priority) {
        return taskRepository.findByPriority(priority);
    }
    
    // Get tasks assigned to user
    public List<Task> getTasksByAssignedUser(Long assignedToId) {
        return taskRepository.findByAssignedToId(assignedToId);
    }
    
    // Get tasks by workflow
    public List<Task> getTasksByWorkflow(Long workflowId) {
        return taskRepository.findByWorkflowId(workflowId);
    }
    
    // Get tasks created by user
    public List<Task> getTasksByCreator(Long createdBy) {
        return taskRepository.findByCreatedById(createdBy);
    }
    
    // Get overdue tasks
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDateTime.now());
    }
    
    // Get tasks due within next N days
    public List<Task> getTasksDueWithinDays(int days) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(days);
        return taskRepository.findTasksDueWithinDateRange(startDate, endDate);
    }
    
    // Search tasks
    public Page<Task> searchTasks(String searchTerm, Pageable pageable) {
        return taskRepository.searchTasks(searchTerm, pageable);
    }
    
    // Get tasks with filters
    public Page<Task> getTasksWithFilters(TaskStatus status, TaskPriority priority, Long assignedToId, 
                                       Long workflowId, Pageable pageable) {
        return taskRepository.findTasksWithFilters(status, priority, assignedToId, workflowId, pageable);
    }
    
    // Delete task
    public void deleteTask(Long id, Long deletedByUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + id));
        
        User deletedBy = userRepository.findById(deletedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + deletedByUserId));
        
        taskRepository.delete(task);
        
        // Log the deletion
        logAuditAction(AuditLog.ActionType.DELETE, "Task", id, 
                      "Deleted task: " + task.getTitle(), deletedBy);
    }
    
    // Assign task to user
    public Task assignTask(Long taskId, Long assignedToId, Long assignedByUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));
        
        User assignedTo = userRepository.findById(assignedToId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + assignedToId));
        
        User assignedBy = userRepository.findById(assignedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + assignedByUserId));
        
        User previousAssignee = task.getAssignedTo();
        task.setAssignedTo(assignedTo);
        
        Task updatedTask = this.taskRepository.save(task);
        
        // Log the assignment
        String description = String.format("Assigned task '%s' to %s", task.getTitle(), assignedTo.getUsername());
        if (previousAssignee != null) {
            description += String.format(" (reassigned from %s)", previousAssignee.getUsername());
        }
        
        logAuditAction(AuditLog.ActionType.ASSIGN, "Task", taskId, description, assignedBy);
        
        return updatedTask;
    }
    
    // Change task status (kept for compatibility but redirects to workflow status change)
    public Task changeTaskStatus(Long id, TaskStatus newStatus, Long changedByUserId) {
        // This method is kept for compatibility but should use workflow status layers instead
        // For now, find a workflow status layer that matches the desired status
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + id));
        
        List<WorkflowStatusLayer> statusLayers = workflowStatusLayerRepository
                .findByWorkflowIdOrderByOrderAsc(task.getWorkflow().getId());
        
        WorkflowStatusLayer targetLayer = null;
        if (newStatus == TaskStatus.COMPLETED) {
            targetLayer = statusLayers.stream()
                    .filter(WorkflowStatusLayer::getIsFinal)
                    .findFirst()
                    .orElse(null);
        } else if (newStatus == TaskStatus.ON_HOLD) {
            targetLayer = statusLayers.stream()
                    .filter(layer -> !layer.getIsFinal() &&
                            (layer.getName().toLowerCase().contains("hold") || layer.getName().toLowerCase().contains("pause")))
                    .findFirst()
                    .orElse(null);
        } else if (newStatus == TaskStatus.IN_PROGRESS) {
            targetLayer = statusLayers.stream()
                    .filter(layer -> !layer.getIsFinal() &&
                            (layer.getName().toLowerCase().contains("progress") || layer.getName().toLowerCase().contains("active")))
                    .findFirst()
                    .orElse(null);
        } else {
            // Find first non-final status layer for PENDING
            targetLayer = statusLayers.stream()
                    .filter(layer -> !layer.getIsFinal())
                    .findFirst()
                    .orElse(null);
        }
        
        if (targetLayer != null) {
            return changeTaskWorkflowStatus(id, targetLayer.getId(), changedByUserId);
        }
        
        return task;
    }
    
    // Complete task
    public Task completeTask(Long id, Integer actualHours, Long completedByUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + id));
        
        User completedBy = userRepository.findById(completedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + completedByUserId));
        
        // Find final status layer in the workflow
        List<WorkflowStatusLayer> statusLayers = workflowStatusLayerRepository
                .findByWorkflowIdOrderByOrderAsc(task.getWorkflow().getId());
        
        WorkflowStatusLayer finalStatusLayer = statusLayers.stream()
                .filter(WorkflowStatusLayer::getIsFinal)
                .findFirst()
                .orElse(null);
        
        if (finalStatusLayer != null) {
            task.setWorkflowStatusLayer(finalStatusLayer);
        }
        
        if (actualHours != null) {
            task.setActualHours(actualHours);
        }
        
        Task completedTask = this.taskRepository.save(task);
        
        // Log the completion
        logAuditAction(AuditLog.ActionType.COMPLETE, "Task", id,
                      "Completed task: " + task.getTitle(), completedBy);
        
        return completedTask;
    }
    
    // Get task statistics
    public Map<String, Object> getTaskStatistics() {
        List<Object[]> statusStats = taskRepository.countTasksByStatus();
        List<Object[]> priorityStats = taskRepository.countTasksByPriority();
        List<Object[]> userStats = taskRepository.countTasksByAssignedUser();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("statusStatistics", statusStats);
        stats.put("priorityStatistics", priorityStats);
        stats.put("userStatistics", userStats);
        return stats;
    }
    
    // Get tasks for a user with filters
    public Page<Task> getTasksForUser(Long userId, TaskStatus status, TaskPriority priority, Pageable pageable) {
        return taskRepository.findTasksForUser(userId, status, priority, pageable);
    }
    
    // Get workflow task statistics
    public Object[] getWorkflowTaskStatistics(Long workflowId) {
        return taskRepository.getTaskStatisticsByWorkflow(workflowId);
    }
    
    // Helper method to log audit actions
    private void logAuditAction(AuditLog.ActionType actionType, String entityType, Long entityId, 
                               String description, User user) {
        logAuditAction(actionType, entityType, entityId, description, user, null, null);
    }
    
    private void logAuditAction(AuditLog.ActionType actionType, String entityType, Long entityId, 
                               String description, User user, String oldValues, String newValues) {
        AuditLog auditLog = new AuditLog(actionType, entityType, entityId, description, user);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLogRepository.save(auditLog);
    }
    
    // Dynamic Workflow Status Methods
    
    /**
     * Create a new task with workflow status layer
     */
    public Task createTaskWithWorkflowStatus(Task task, Long workflowStatusLayerId, Long createdByUserId) {
        // Validate workflow exists
        Workflow workflow = workflowRepository.findById(task.getWorkflow().getId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with ID: " + task.getWorkflow().getId()));
        
        // Validate workflow status layer exists
        WorkflowStatusLayer workflowStatusLayer = workflowStatusLayerRepository.findById(workflowStatusLayerId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow status layer not found with ID: " + workflowStatusLayerId));
        
        // Validate that the status layer belongs to the workflow
        if (!workflowStatusLayer.getWorkflow().getId().equals(workflow.getId())) {
            throw new IllegalArgumentException("Status layer does not belong to the specified workflow");
        }
        
        // Validate creator
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + createdByUserId));
        
        // Validate assigned user if provided
        if (task.getAssignedTo() != null && task.getAssignedTo().getId() != null) {
            User assignedTo = userRepository.findById(task.getAssignedTo().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Assigned user not found with ID: " + task.getAssignedTo().getId()));
            task.setAssignedTo(assignedTo);
        }
        
        // Check for duplicate task title in workflow
        if (taskRepository.existsByTitleIgnoreCaseAndWorkflowId(task.getTitle(), workflow.getId())) {
            throw new IllegalArgumentException("Task with title '" + task.getTitle() + "' already exists in this workflow");
        }
        
        task.setWorkflow(workflow);
        task.setWorkflowStatusLayer(workflowStatusLayer);
        task.setCreatedBy(createdBy);
        
        // Status is now derived from workflowStatusLayer, no need to set it directly
        // The getStatus() method will derive the correct status based on the workflowStatusLayer
        
        Task savedTask = this.taskRepository.save(task);
        
        // Log the creation
        logAuditAction(AuditLog.ActionType.CREATE, "Task", savedTask.getId(),
                      "Created task: " + savedTask.getTitle() + " with status: " + workflowStatusLayer.getName() +
                      " in workflow: " + workflow.getName(), createdBy);
        
        return savedTask;
    }
    
    /**
     * Change task workflow status layer
     */
    public Task changeTaskWorkflowStatus(Long taskId, Long newWorkflowStatusLayerId, Long changedByUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));
        
        User changedBy = userRepository.findById(changedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + changedByUserId));
        
        WorkflowStatusLayer newWorkflowStatusLayer = workflowStatusLayerRepository.findById(newWorkflowStatusLayerId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow status layer not found with ID: " + newWorkflowStatusLayerId));
        
        // Validate that the status layer belongs to the task's workflow
        if (!newWorkflowStatusLayer.getWorkflow().getId().equals(task.getWorkflow().getId())) {
            throw new IllegalArgumentException("Status layer does not belong to the task's workflow");
        }
        
        WorkflowStatusLayer oldWorkflowStatusLayer = task.getWorkflowStatusLayer();
        task.setWorkflowStatusLayer(newWorkflowStatusLayer);
        
        // completedAt is now automatically managed by setWorkflowStatusLayer method
        
        Task updatedTask = this.taskRepository.save(task);
        
        // Log the status change
        String description = String.format("Changed workflow status from %s to %s for task: %s",
                oldWorkflowStatusLayer != null ? oldWorkflowStatusLayer.getName() : "None",
                newWorkflowStatusLayer.getName(), task.getTitle());
        
        logAuditAction(AuditLog.ActionType.UPDATE, "Task", taskId, description, changedBy,
                      oldWorkflowStatusLayer != null ? oldWorkflowStatusLayer.getName() : "None",
                      newWorkflowStatusLayer.getName());
        
        return updatedTask;
    }
    
    /**
     * Get next available workflow status layers for a task
     */
    public List<WorkflowStatusLayer> getNextWorkflowStatusLayers(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));
        
        if (task.getWorkflowStatusLayer() == null) {
            // Return all status layers for the workflow if task has no current status
            return workflowStatusLayerRepository.findByWorkflowIdOrderByOrderAsc(task.getWorkflow().getId());
        }
        
        // Return status layers with higher order than current
        return workflowStatusLayerRepository.findByWorkflowIdOrderByOrderAsc(task.getWorkflow().getId())
                .stream()
                .filter(statusLayer -> statusLayer.getOrder() > task.getWorkflowStatusLayer().getOrder())
                .toList();
    }
    
    /**
     * Get tasks by workflow status layer
     */
    public List<Task> getTasksByWorkflowStatusLayer(Long workflowStatusLayerId) {
        return taskRepository.findByWorkflowStatusLayerId(workflowStatusLayerId);
    }
    
    /**
     * Get tasks with workflow status layer filter
     */
    public Page<Task> getTasksWithWorkflowStatusLayerFilter(Long workflowStatusLayerId, TaskPriority priority,
                                                           Long assignedToId, Pageable pageable) {
        return taskRepository.findTasksWithWorkflowStatusLayerFilter(workflowStatusLayerId, priority, assignedToId, pageable);
    }
}