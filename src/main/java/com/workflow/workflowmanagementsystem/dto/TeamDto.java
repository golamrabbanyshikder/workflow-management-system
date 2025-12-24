package com.workflow.workflowmanagementsystem.dto;

public class TeamDto {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private Long departmentId;
    private String departmentName;
    private int memberCount;
    
    public TeamDto() {}
    
    public TeamDto(Long id, String name, String description, Boolean active, Long departmentId, String departmentName, int memberCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.memberCount = memberCount;
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
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public int getMemberCount() {
        return memberCount;
    }
    
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
}