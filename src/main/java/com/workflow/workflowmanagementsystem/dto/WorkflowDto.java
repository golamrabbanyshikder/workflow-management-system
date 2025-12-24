package com.workflow.workflowmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class WorkflowDto {
    private Long id;
    private String name;
    private String description;
    private String status;
    private Boolean isActive;
    
    public WorkflowDto() {}
    
    public WorkflowDto(Long id, String name, String description, String status, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.isActive = isActive;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}