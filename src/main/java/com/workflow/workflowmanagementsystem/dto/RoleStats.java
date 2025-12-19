package com.workflow.workflowmanagementsystem.dto;

public class RoleStats {
    private final String roleName;
    private final long userCount;
    private final int permissionCount;
    private final Integer roleLevel;
    private final boolean isActive;

    public RoleStats(String roleName, long userCount, int permissionCount, Integer roleLevel, boolean isActive) {
        this.roleName = roleName;
        this.userCount = userCount;
        this.permissionCount = permissionCount;
        this.roleLevel = roleLevel;
        this.isActive = isActive;
    }

    public String getRoleName() { return roleName; }
    public long getUserCount() { return userCount; }
    public int getPermissionCount() { return permissionCount; }
    public Integer getRoleLevel() { return roleLevel; }
    public boolean isActive() { return isActive; }
}