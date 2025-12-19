package com.workflow.workflowmanagementsystem.dto;

public class TeamStats {
    private final String teamName;
    private final String departmentName;
    private final long memberCount;
    private final boolean isActive;

    public TeamStats(String teamName, String departmentName, long memberCount, boolean isActive) {
        this.teamName = teamName;
        this.departmentName = departmentName;
        this.memberCount = memberCount;
        this.isActive = isActive;
    }

    public String getTeamName() { return teamName; }
    public String getDepartmentName() { return departmentName; }
    public long getMemberCount() { return memberCount; }
    public boolean isActive() { return isActive; }
}