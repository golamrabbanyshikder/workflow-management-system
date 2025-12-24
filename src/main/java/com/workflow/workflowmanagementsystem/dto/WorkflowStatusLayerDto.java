package com.workflow.workflowmanagementsystem.dto;

public class WorkflowStatusLayerDto {
    private Long id;
    private String name;
    private String description;
    private Integer order;
    private Boolean isFinal;
    private String color;
    private Long workflowId;
    
    public WorkflowStatusLayerDto() {}
    
    public WorkflowStatusLayerDto(Long id, String name, String description, Integer order, Boolean isFinal, String color, Long workflowId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.order = order;
        this.isFinal = isFinal;
        this.color = color;
        this.workflowId = workflowId;
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
    
    public Integer getOrder() {
        return order;
    }
    
    public void setOrder(Integer order) {
        this.order = order;
    }
    
    public Boolean getIsFinal() {
        return isFinal;
    }
    
    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public Long getWorkflowId() {
        return workflowId;
    }
    
    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }
}