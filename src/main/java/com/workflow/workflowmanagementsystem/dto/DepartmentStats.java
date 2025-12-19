package com.workflow.workflowmanagementsystem.dto;

public class DepartmentStats {
    private final String departmentName;
    private final long teamCount;
    private final long userCount;

    public DepartmentStats(String departmentName, long teamCount, long userCount) {
        this.departmentName = departmentName;
        this.teamCount = teamCount;
        this.userCount = userCount;
    }

    public String getDepartmentName() { return departmentName; }
    public long getTeamCount() { return teamCount; }
    public long getUserCount() { return userCount; }
}