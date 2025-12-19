package com.workflow.workflowmanagementsystem.dto;

public class SystemOverviewStats {
    private final long departments;
    private final long teams;
    private final long roles;
    private final long users;
    private final long activeTeams;
    private final long activeRoles;
    private final long activeUsers;

    public SystemOverviewStats(long departments, long teams, long roles, long users, 
                           long activeTeams, long activeRoles, long activeUsers) {
        this.departments = departments;
        this.teams = teams;
        this.roles = roles;
        this.users = users;
        this.activeTeams = activeTeams;
        this.activeRoles = activeRoles;
        this.activeUsers = activeUsers;
    }

    public long getDepartments() { return departments; }
    public long getTeams() { return teams; }
    public long getRoles() { return roles; }
    public long getUsers() { return users; }
    public long getActiveTeams() { return activeTeams; }
    public long getActiveRoles() { return activeRoles; }
    public long getActiveUsers() { return activeUsers; }
}