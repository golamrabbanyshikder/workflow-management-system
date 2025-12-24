package com.workflow.workflowmanagementsystem.dto;

import java.util.List;

public class RoleDto {
    private Long id;
    private String name;
    private String description;
    private Integer roleLevel;
    private Boolean active;
    private List<String> permissions;
    private Long userCount;
    
    public RoleDto() {}
    
    public RoleDto(Long id, String name, String description, Integer roleLevel, Boolean active, List<String> permissions, Long userCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.roleLevel = roleLevel;
        this.active = active;
        this.permissions = permissions;
        this.userCount = userCount;
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
    
    public Integer getRoleLevel() {
        return roleLevel;
    }
    
    public void setRoleLevel(Integer roleLevel) {
        this.roleLevel = roleLevel;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public List<String> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
    
    public Long getUserCount() {
        return userCount;
    }
    
    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }
}