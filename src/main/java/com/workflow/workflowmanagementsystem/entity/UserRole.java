package com.workflow.workflowmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_roles")
public class UserRole {
    @EmbeddedId
    private UserRoleId id = new UserRoleId();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    @JsonIgnore
    private Role role;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private Department department;

    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;
    
    @Column(name = "is_active")
    private Boolean active = true;
    
    @Column(name = "assigned_by")
    private Long assignedBy;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public UserRole() {}
    
    public UserRole(User user, Role role, Department department, Team team) {
        this.user = user;
        this.role = role;
        this.department = department;
        this.team = team;
        this.active = true;
        this.assignedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UserRoleId getId() { return id; }
    public void setId(UserRoleId id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    
    public Boolean isActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public Long getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Long assignedBy) { this.assignedBy = assignedBy; }
    
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}