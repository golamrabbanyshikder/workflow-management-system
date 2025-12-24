package com.workflow.workflowmanagementsystem.dto;

import com.workflow.workflowmanagementsystem.entity.Task.TaskPriority;
import com.workflow.workflowmanagementsystem.entity.Task.TaskStatus;

public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long assignedToId;
    private String assignedToName;
    private Long workflowId;
    private String workflowName;
    private Long workflowStatusLayerId;
    private String workflowStatusLayerName;
    private Long createdById;
    private String createdByName;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime dueDate;
    private Integer estimatedHours;
    private Integer actualHours;
    private Long teamId;
    private String teamName;
    private int teamMemberCount;
    
    public TaskDto() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    
    // Add method to check if task is completed based on workflow status layer
    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }
    
    // Add method to check if task is in progress based on workflow status layer
    public boolean isInProgress() {
        return status == TaskStatus.IN_PROGRESS;
    }
    
    // Add method to check if task is on hold based on workflow status layer
    public boolean isOnHold() {
        return status == TaskStatus.ON_HOLD;
    }
    
    // Add method to check if task is pending based on workflow status layer
    public boolean isPending() {
        return status == TaskStatus.PENDING;
    }
    
    // Add method to check if task is cancelled based on workflow status layer
    public boolean isCancelled() {
        return status == TaskStatus.CANCELLED;
    }
    
    public TaskPriority getPriority() {
        return priority;
    }
    
    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }
    
    public Long getAssignedToId() {
        return assignedToId;
    }
    
    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }
    
    public String getAssignedToName() {
        return assignedToName;
    }
    
    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }
    
    public Long getWorkflowId() {
        return workflowId;
    }
    
    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }
    
    public String getWorkflowName() {
        return workflowName;
    }
    
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }
    
    public Long getWorkflowStatusLayerId() {
        return workflowStatusLayerId;
    }
    
    public void setWorkflowStatusLayerId(Long workflowStatusLayerId) {
        this.workflowStatusLayerId = workflowStatusLayerId;
    }
    
    public String getWorkflowStatusLayerName() {
        return workflowStatusLayerName;
    }
    
    public void setWorkflowStatusLayerName(String workflowStatusLayerName) {
        this.workflowStatusLayerName = workflowStatusLayerName;
    }
    
    public Long getCreatedById() {
        return createdById;
    }
    
    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public java.time.LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(java.time.LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public Integer getEstimatedHours() {
        return estimatedHours;
    }
    
    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }
    
    public Integer getActualHours() {
        return actualHours;
    }
    
    public void setActualHours(Integer actualHours) {
        this.actualHours = actualHours;
    }
    
    public Long getTeamId() {
        return teamId;
    }
    
    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    public int getTeamMemberCount() {
        return teamMemberCount;
    }
    
    public void setTeamMemberCount(int teamMemberCount) {
        this.teamMemberCount = teamMemberCount;
    }
}