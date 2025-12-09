package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.entity.AuditLog;
import com.workflow.workflowmanagementsystem.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping({"", "/"})
    public String reports(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        model.addAttribute("currentUser", username);
        model.addAttribute("reportTypes", reportService.getAvailableReportTypes());
        
        return "reports/index";
    }

    @GetMapping("/task-report")
    public String taskReport(@RequestParam(required = false) String status,
                           @RequestParam(required = false) String priority,
                           @RequestParam(required = false) Long departmentId,
                           Model model) {
        
        model.addAttribute("taskReportData", reportService.getTaskReportData(status, priority, departmentId));
        model.addAttribute("statusOptions", reportService.getTaskStatusOptions());
        model.addAttribute("priorityOptions", reportService.getTaskPriorityOptions());
        model.addAttribute("departments", reportService.getDepartmentOptions());
        
        return "reports/task-report";
    }

    @GetMapping("/workflow-report")
    public String workflowReport(@RequestParam(required = false) String status,
                              @RequestParam(required = false) Long departmentId,
                              Model model) {
        
        model.addAttribute("workflowReportData", reportService.getWorkflowReportData(status, departmentId));
        model.addAttribute("statusOptions", reportService.getWorkflowStatusOptions());
        model.addAttribute("departments", reportService.getDepartmentOptions());
        
        return "reports/workflow-report";
    }

    @GetMapping("/user-productivity")
    public String userProductivityReport(@RequestParam(required = false) Long userId,
                                      @RequestParam(required = false) Long departmentId,
                                      Model model) {
        
        model.addAttribute("productivityData", reportService.getUserProductivityData(userId, departmentId));
        model.addAttribute("users", reportService.getUserOptions());
        model.addAttribute("departments", reportService.getDepartmentOptions());
        
        return "reports/user-productivity";
    }

    @GetMapping("/audit-logs")
    public String auditLogs(@RequestParam(required = false) String actionType,
                           @RequestParam(required = false) String entityType,
                           @RequestParam(required = false) Long userId,
                           Model model) {
        
        model.addAttribute("auditLogs", reportService.getAuditLogData(actionType, entityType, userId));
        model.addAttribute("actionTypes", reportService.getActionTypeOptions());
        model.addAttribute("entityTypes", reportService.getEntityTypeOptions());
        model.addAttribute("users", reportService.getUserOptions());
        
        return "reports/audit-logs";
    }
    
    // CSV Export endpoints
    
    @GetMapping("/task-report/export")
    public void exportTaskReport(@RequestParam(required = false) String status,
                              @RequestParam(required = false) String priority,
                              @RequestParam(required = false) Long departmentId,
                              jakarta.servlet.http.HttpServletResponse response) throws IOException {
        
        Map<String, Object> reportData = reportService.getTaskReportData(status, priority, departmentId);
        byte[] csvData = reportService.exportToCSV(reportData);
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"task-report.csv\"");
        response.getOutputStream().write(csvData);
    }
    
    @GetMapping("/workflow-report/export")
    public void exportWorkflowReport(@RequestParam(required = false) String status,
                                 @RequestParam(required = false) Long departmentId,
                                 jakarta.servlet.http.HttpServletResponse response) throws IOException {
        
        Map<String, Object> reportData = reportService.getWorkflowReportData(status, departmentId);
        byte[] csvData = reportService.exportToCSV(reportData);
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"workflow-report.csv\"");
        response.getOutputStream().write(csvData);
    }
    
    @GetMapping("/user-productivity/export")
    public void exportUserProductivityReport(@RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) Long departmentId,
                                         jakarta.servlet.http.HttpServletResponse response) throws IOException {
        
        Map<String, Object> reportData = reportService.getUserProductivityData(userId, departmentId);
        byte[] csvData = reportService.exportToCSV(reportData);
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"user-productivity-report.csv\"");
        response.getOutputStream().write(csvData);
    }
    
    @GetMapping("/audit-logs/export")
    public void exportAuditLogs(@RequestParam(required = false) String actionType,
                              @RequestParam(required = false) String entityType,
                              @RequestParam(required = false) Long userId,
                              jakarta.servlet.http.HttpServletResponse response) throws IOException {
        
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("reportType", "Audit Logs");
        reportData.put("generatedAt", java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        reportData.put("auditLogs", reportService.getAuditLogData(actionType, entityType, userId).stream().map(this::auditLogToMap).collect(java.util.stream.Collectors.toList()));
        
        byte[] csvData = reportService.exportToCSV(reportData);
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"audit-logs.csv\"");
        response.getOutputStream().write(csvData);
    }
    
    private Map<String, Object> auditLogToMap(AuditLog auditLog) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", auditLog.getId());
        map.put("action", auditLog.getActionType().getDisplayName());
        map.put("entityType", auditLog.getEntityType());
        map.put("description", auditLog.getDescription());
        map.put("user", auditLog.getUser().getUsername());
        map.put("ipAddress", auditLog.getIpAddress());
        map.put("createdAt", auditLog.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return map;
    }
}