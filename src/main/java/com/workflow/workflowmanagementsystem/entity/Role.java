package com.workflow.workflowmanagementsystem.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
    
    private String description;
    
    @Column(name = "role_level")
    private Integer roleLevel;
    
    @Column(name = "is_active")
    private Boolean active = true;
    
    @ElementCollection
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission")
    private List<String> permissions = new ArrayList<>();
    
    @OneToMany(mappedBy = "role")
    private List<UserRole> userRoles = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Role() {}

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
        this.active = true;
    }
    
    public Role(String name, String description, Integer roleLevel) {
        this.name = name;
        this.description = description;
        this.roleLevel = roleLevel;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getRoleLevel() { return roleLevel; }
    public void setRoleLevel(Integer roleLevel) { this.roleLevel = roleLevel; }
    
    public Boolean isActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    // Add getter for 'active' property to support Spring form binding
    public Boolean getActive() { return active; }
    
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    
    public void addPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }
    
    public void removePermission(String permission) {
        permissions.remove(permission);
    }
    
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
    
    public List<UserRole> getUserRoles() { return userRoles; }
    public void setUserRoles(List<UserRole> userRoles) { this.userRoles = userRoles; }
    
    public int getUserCount() {
        return userRoles != null ? userRoles.size() : 0;
    }
    
    public int getPermissionCount() {
        return permissions != null ? permissions.size() : 5;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}