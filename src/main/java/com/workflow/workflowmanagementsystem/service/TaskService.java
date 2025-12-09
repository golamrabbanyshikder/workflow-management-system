package com.workflow.workflowmanagementsystem.service;

import com.workflow.workflowmanagementsystem.Repository.AuditLogRepository;
import com.workflow.workflowmanagementsystem.Repository.TaskRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.Repository.WorkflowRepository;
import com.workflow.workflowmanagementsystem.entity.AuditLog;
import com.workflow.workflowmanagementsystem.entity.Task;
import com.workflow.workflowmanagementsystem.entity.Task.TaskPriority;
import com.workflow.workflowmanagementsystem.entity.Task.TaskStatus;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.Workflow;
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
        
        Task savedTask = taskRepository.save(task);
        
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
                existingTask.getTitle(), existingTask.getStatus(), existingTask.getPriority(),
                existingTask.getAssignedTo() != null ? existingTask.getAssignedTo().getUsername() : "Unassigned");
        
        // Update fields
        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setStatus(taskDetails.getStatus());
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
        
        Task updatedTask = taskRepository.save(existingTask);
        
        // Log the update
        String newValues = String.format("Title: %s, Status: %s, Priority: %s, AssignedTo: %s",
                updatedTask.getTitle(), updatedTask.getStatus(), updatedTask.getPriority(),
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
    
    // Get tasks by status
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
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
        
        Task updatedTask = taskRepository.save(task);
        
        // Log the assignment
        String description = String.format("Assigned task '%s' to %s", task.getTitle(), assignedTo.getUsername());
        if (previousAssignee != null) {
            description += String.format(" (reassigned from %s)", previousAssignee.getUsername());
        }
        
        logAuditAction(AuditLog.ActionType.ASSIGN, "Task", taskId, description, assignedBy);
        
        return updatedTask;
    }
    
    // Change task status
    public Task changeTaskStatus(Long id, TaskStatus newStatus, Long changedByUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + id));
        
        User changedBy = userRepository.findById(changedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + changedByUserId));
        
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);
        
        Task updatedTask = taskRepository.save(task);
        
        // Log the status change
        logAuditAction(AuditLog.ActionType.UPDATE, "Task", id, 
                      "Changed status from " + oldStatus + " to " + newStatus + " for task: " + task.getTitle(), 
                      changedBy, oldStatus.toString(), newStatus.toString());
        
        return updatedTask;
    }
    
    // Complete task
    public Task completeTask(Long id, Integer actualHours, Long completedByUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + id));
        
        User completedBy = userRepository.findById(completedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + completedByUserId));
        
        task.setStatus(TaskStatus.COMPLETED);
        if (actualHours != null) {
            task.setActualHours(actualHours);
        }
        
        Task completedTask = taskRepository.save(task);
        
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
}