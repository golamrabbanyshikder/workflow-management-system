package com.workflow.workflowmanagementsystem.util;

import com.workflow.workflowmanagementsystem.entity.Task;
import com.workflow.workflowmanagementsystem.entity.WorkflowStatusLayer;

/**
 * Utility class for handling TaskStatus transitions based on WorkflowStatusLayer
 */
public class TaskStatusUtil {
    
    /**
     * Determines the TaskStatus based on WorkflowStatusLayer
     * @param workflowStatusLayer The workflow status layer
     * @return The corresponding TaskStatus
     */
    public static Task.TaskStatus getStatusFromWorkflowStatusLayer(WorkflowStatusLayer workflowStatusLayer) {
        if (workflowStatusLayer == null) {
            return Task.TaskStatus.PENDING;
        }
        
        // Check if the status layer is final (completed)
        if (workflowStatusLayer.getIsFinal()) {
            return Task.TaskStatus.COMPLETED;
        }
        
        // Check status layer name for specific status indicators
        String layerName = workflowStatusLayer.getName().toLowerCase();
        
        if (layerName.contains("hold") || layerName.contains("pause")) {
            return Task.TaskStatus.ON_HOLD;
        } else if (layerName.contains("cancel")) {
            return Task.TaskStatus.CANCELLED;
        } else if (layerName.contains("progress") || layerName.contains("active") || layerName.contains("work")) {
            return Task.TaskStatus.IN_PROGRESS;
        } else {
            // Default to PENDING for any other non-final status
            return Task.TaskStatus.PENDING;
        }
    }
    
    /**
     * Checks if a task is completed based on its workflow status layer
     * @param task The task to check
     * @return true if the task is completed
     */
    public static boolean isTaskCompleted(Task task) {
        return task.getStatus() == Task.TaskStatus.COMPLETED;
    }
    
    /**
     * Checks if a task is in progress based on its workflow status layer
     * @param task The task to check
     * @return true if the task is in progress
     */
    public static boolean isTaskInProgress(Task task) {
        return task.getStatus() == Task.TaskStatus.IN_PROGRESS;
    }
    
    /**
     * Checks if a task is on hold based on its workflow status layer
     * @param task The task to check
     * @return true if the task is on hold
     */
    public static boolean isTaskOnHold(Task task) {
        return task.getStatus() == Task.TaskStatus.ON_HOLD;
    }
    
    /**
     * Checks if a task is pending based on its workflow status layer
     * @param task The task to check
     * @return true if the task is pending
     */
    public static boolean isTaskPending(Task task) {
        return task.getStatus() == Task.TaskStatus.PENDING;
    }
    
    /**
     * Checks if a task is cancelled based on its workflow status layer
     * @param task The task to check
     * @return true if the task is cancelled
     */
    public static boolean isTaskCancelled(Task task) {
        return task.getStatus() == Task.TaskStatus.CANCELLED;
    }
    
    /**
     * Determines if a task is overdue (not completed and past due date)
     * @param task The task to check
     * @return true if the task is overdue
     */
    public static boolean isTaskOverdue(Task task) {
        return task.getDueDate() != null && 
               task.getDueDate().isBefore(java.time.LocalDateTime.now()) && 
               !isTaskCompleted(task);
    }
    
    /**
     * Gets the CSS class for a task status badge
     * @param task The task
     * @return The CSS class string
     */
    public static String getStatusBadgeClass(Task task) {
        switch (task.getStatus()) {
            case COMPLETED:
                return "bg-success";
            case IN_PROGRESS:
                return "bg-primary";
            case PENDING:
                return "bg-warning";
            case ON_HOLD:
                return "bg-info";
            case CANCELLED:
                return "bg-danger";
            default:
                return "bg-secondary";
        }
    }
    
    /**
     * Gets the icon class for a task status
     * @param task The task
     * @return The icon class string
     */
    public static String getStatusIconClass(Task task) {
        switch (task.getStatus()) {
            case COMPLETED:
                return "fas fa-check-circle text-success";
            case IN_PROGRESS:
                return "fas fa-spinner text-primary";
            case PENDING:
                return "fas fa-clock text-warning";
            case ON_HOLD:
                return "fas fa-pause-circle text-info";
            case CANCELLED:
                return "fas fa-times-circle text-danger";
            default:
                return "fas fa-question-circle text-secondary";
        }
    }
    
    /**
     * Gets the progress percentage for a task status
     * @param task The task
     * @return The progress percentage (0-100)
     */
    public static int getStatusProgressPercentage(Task task) {
        switch (task.getStatus()) {
            case COMPLETED:
                return 100;
            case IN_PROGRESS:
                return 60;
            case ON_HOLD:
                return 40;
            case PENDING:
                return 25;
            case CANCELLED:
                return 0;
            default:
                return 0;
        }
    }
}