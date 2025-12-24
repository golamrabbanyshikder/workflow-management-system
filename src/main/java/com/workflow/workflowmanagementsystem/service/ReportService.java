package com.workflow.workflowmanagementsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.workflow.workflowmanagementsystem.Repository.AuditLogRepository;
import com.workflow.workflowmanagementsystem.Repository.DepartmentRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.entity.AuditLog;
import com.workflow.workflowmanagementsystem.entity.Department;
import com.workflow.workflowmanagementsystem.entity.Task;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    
    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Generate workflow summary report
    public Map<String, Object> generateWorkflowSummaryReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "Workflow Summary");
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        report.put("generatedBy", "System");
        
        // Get all workflows
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Workflow> workflows = workflowService.getAllWorkflows(pageable);
        
        // Calculate statistics
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalWorkflows", workflows.getTotalElements());
        
        // Status distribution
        Map<String, Object> workflowStats = workflowService.getWorkflowStatistics();
        List<Object[]> statusStats = (List<Object[]>) workflowStats.get("statusStatistics");
        
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Object[] stat : statusStats) {
            statusDistribution.put(stat[0].toString(), (Long) stat[1]);
        }
        statistics.put("statusDistribution", statusDistribution);
        
        // Department distribution
        List<Object[]> deptStats = (List<Object[]>) workflowStats.get("departmentStatistics");
        Map<String, Long> departmentDistribution = new HashMap<>();
        for (Object[] stat : deptStats) {
            departmentDistribution.put(stat[0].toString(), (Long) stat[1]);
        }
        statistics.put("departmentDistribution", departmentDistribution);
        
        report.put("statistics", statistics);
        
        // Workflow details
        List<Map<String, Object>> workflowDetails = workflows.getContent().stream()
                .map(this::workflowToMap)
                .collect(Collectors.toList());
        report.put("workflows", workflowDetails);
        
        return report;
    }
    
    // Generate task performance report
    public Map<String, Object> generateTaskPerformanceReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "Task Performance");
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        report.put("generatedBy", "System");
        
        // Get all tasks
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Task> tasks = taskService.getAllTasks(pageable);
        
        // Calculate statistics
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalTasks", tasks.getTotalElements());
        
        // Status distribution
        Map<String, Object> taskStats = taskService.getTaskStatistics();
        List<Object[]> statusStats = (List<Object[]>) taskStats.get("statusStatistics");
        
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Object[] stat : statusStats) {
            statusDistribution.put(stat[0].toString(), (Long) stat[1]);
        }
        statistics.put("statusDistribution", statusDistribution);
        
        // Priority distribution
        List<Object[]> priorityStats = (List<Object[]>) taskStats.get("priorityStatistics");
        Map<String, Long> priorityDistribution = new HashMap<>();
        for (Object[] stat : priorityStats) {
            priorityDistribution.put(stat[0].toString(), (Long) stat[1]);
        }
        statistics.put("priorityDistribution", priorityDistribution);
        
        // User assignment distribution
        List<Object[]> userStats = (List<Object[]>) taskStats.get("userStatistics");
        Map<String, Long> userDistribution = new HashMap<>();
        for (Object[] stat : userStats) {
            userDistribution.put(stat[0].toString(), (Long) stat[1]);
        }
        statistics.put("userDistribution", userDistribution);
        
        // Performance metrics
        List<Task> completedTasks = tasks.getContent().stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
                .collect(Collectors.toList());
        
        if (!completedTasks.isEmpty()) {
            double avgEstimatedHours = completedTasks.stream()
                    .filter(task -> task.getEstimatedHours() != null)
                    .mapToInt(Task::getEstimatedHours)
                    .average()
                    .orElse(0.0);
            
            double avgActualHours = completedTasks.stream()
                    .filter(task -> task.getActualHours() != null)
                    .mapToInt(Task::getActualHours)
                    .average()
                    .orElse(0.0);
            
            statistics.put("averageEstimatedHours", avgEstimatedHours);
            statistics.put("averageActualHours", avgActualHours);
            statistics.put("efficiencyRatio", avgEstimatedHours > 0 ? avgActualHours / avgEstimatedHours : 0.0);
        }
        
        report.put("statistics", statistics);
        
        // Task details
        List<Map<String, Object>> taskDetails = tasks.getContent().stream()
                .map(this::taskToMap)
                .collect(Collectors.toList());
        report.put("tasks", taskDetails);
        
        return report;
    }
    
    // Generate user productivity report
    public Map<String, Object> generateUserProductivityReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "User Productivity");
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        report.put("generatedBy", "System");
        
        // Get all tasks
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Task> tasks = taskService.getAllTasks(pageable);
        
        // Group tasks by assigned user
        Map<String, List<Task>> tasksByUser = tasks.getContent().stream()
                .filter(task -> task.getAssignedTo() != null)
                .collect(Collectors.groupingBy(task -> task.getAssignedTo().getUsername()));
        
        List<Map<String, Object>> userProductivity = new ArrayList<>();
        
        for (Map.Entry<String, List<Task>> entry : tasksByUser.entrySet()) {
            String username = entry.getKey();
            List<Task> userTasks = entry.getValue();
            
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("username", username);
            userStats.put("totalTasks", userTasks.size());
            
            long completedTasks = userTasks.stream()
                    .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
                    .count();
            userStats.put("completedTasks", (int) completedTasks);
            
            long overdueTasks = userTasks.stream()
                    .filter(task -> task.getDueDate().isBefore(LocalDateTime.now()) &&
                                   task.getStatus() != Task.TaskStatus.COMPLETED)
                    .count();
            userStats.put("overdueTasks", (int) overdueTasks);
            
            double completionRate = userTasks.size() > 0 ? (double) completedTasks / userTasks.size() * 100 : 0.0;
            userStats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
            
            // Calculate average completion time
            List<Task> completed = userTasks.stream()
                    .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED &&
                                   task.getCompletedAt() != null)
                    .collect(Collectors.toList());
            
            if (!completed.isEmpty()) {
                double avgCompletionDays = completed.stream()
                        .mapToLong(task -> java.time.Duration.between(task.getCreatedAt(), task.getCompletedAt()).toDays())
                        .average()
                        .orElse(0.0);
                userStats.put("averageCompletionDays", Math.round(avgCompletionDays * 100.0) / 100.0);
            } else {
                userStats.put("averageCompletionDays", 0.0);
            }
            
            userProductivity.add(userStats);
        }
        
        // Sort by completion rate (highest first)
        userProductivity.sort((a, b) -> Double.compare((Double) b.get("completionRate"), (Double) a.get("completionRate")));
        
        report.put("userProductivity", userProductivity);
        
        return report;
    }
    
    // Export report to CSV
    public byte[] exportToCSV(Map<String, Object> report) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            
            String reportType = (String) report.get("reportType");
            
            switch (reportType) {
                case "Workflow Summary":
                case "Workflow Report":
                    exportWorkflowSummaryToCSV(report, csvWriter);
                    break;
                case "Task Performance":
                case "Task Report":
                    exportTaskPerformanceToCSV(report, csvWriter);
                    break;
                case "User Productivity":
                    exportUserProductivityToCSV(report, csvWriter);
                    break;
                case "Audit Logs":
                    exportAuditLogsToCSV(report, csvWriter);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown report type: " + reportType);
            }
        }
        
        return outputStream.toByteArray();
    }
    
    // Export workflow summary to CSV
    private void exportWorkflowSummaryToCSV(Map<String, Object> report, CSVWriter csvWriter) {
        // Header
        String[] header = {"Workflow Name", "Status", "Department", "Created By", "Created Date", "Task Count", "Description"};
        csvWriter.writeNext(header);
        
        // Data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> workflows = (List<Map<String, Object>>) report.get("workflows");
        
        for (Map<String, Object> workflow : workflows) {
            String[] row = {
                (String) workflow.get("name"),
                (String) workflow.get("status"),
                (String) workflow.get("department"),
                (String) workflow.get("createdBy"),
                (String) workflow.get("createdAt"),
                workflow.get("taskCount").toString(),
                (String) workflow.get("description")
            };
            csvWriter.writeNext(row);
        }
    }
    
    // Export task performance to CSV
    private void exportTaskPerformanceToCSV(Map<String, Object> report, CSVWriter csvWriter) {
        // Header
        String[] header = {"Task Title", "Status", "Priority", "Workflow", "Assigned To", "Created By", 
                           "Due Date", "Completed Date", "Estimated Hours", "Actual Hours", "Description"};
        csvWriter.writeNext(header);
        
        // Data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) report.get("tasks");
        
        for (Map<String, Object> task : tasks) {
            String[] row = {
                (String) task.get("title"),
                (String) task.get("status"),
                (String) task.get("priority"),
                (String) task.get("workflow"),
                (String) task.get("assignedTo"),
                (String) task.get("createdBy"),
                (String) task.get("dueDate"),
                (String) task.get("completedAt"),
                task.get("estimatedHours") != null ? task.get("estimatedHours").toString() : "",
                task.get("actualHours") != null ? task.get("actualHours").toString() : "",
                (String) task.get("description")
            };
            csvWriter.writeNext(row);
        }
    }
    
    // Export user productivity to CSV
    private void exportUserProductivityToCSV(Map<String, Object> report, CSVWriter csvWriter) {
        // Header
        String[] header = {"Username", "Total Tasks", "Completed Tasks",
                           "Completion Rate (%)", "Average Completion Days"};
        csvWriter.writeNext(header);
        
        // Data - check for both possible keys to handle different report formats
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> userData = (List<Map<String, Object>>) report.get("users");
        
        if (userData == null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> userProductivity = (List<Map<String, Object>>) report.get("userProductivity");
            userData = userProductivity;
        }
        
        if (userData != null) {
            for (Map<String, Object> user : userData) {
                String[] row = {
                    (String) user.get("username"),
                    user.get("totalTasks").toString(),
                    user.get("completedTasks").toString(),
                    user.get("completionRate").toString(),
                    user.get("averageCompletionDays").toString()
                };
                csvWriter.writeNext(row);
            }
        }
    }
    
    // Export audit logs to CSV
    private void exportAuditLogsToCSV(Map<String, Object> report, CSVWriter csvWriter) {
        // Header
        String[] header = {"Date/Time", "Action", "Entity Type", "Description", "User", "IP Address"};
        csvWriter.writeNext(header);
        
        // Data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> auditLogs = (List<Map<String, Object>>) report.get("auditLogs");
        
        for (Map<String, Object> log : auditLogs) {
            String[] row = {
                (String) log.get("createdAt"),
                (String) log.get("action"),
                (String) log.get("entityType"),
                (String) log.get("description"),
                (String) log.get("user"),
                (String) log.get("ipAddress")
            };
            csvWriter.writeNext(row);
        }
    }
    
    // Convert workflow to map
    private Map<String, Object> workflowToMap(Workflow workflow) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", workflow.getId());
        map.put("name", workflow.getName());
        map.put("description", workflow.getDescription());
        map.put("status", workflow.getStatus().getDisplayName());
        map.put("department", workflow.getDepartment() != null ? workflow.getDepartment().getName() : "N/A");
        map.put("createdBy", workflow.getCreatedBy() != null ? workflow.getCreatedBy().getUsername() : "N/A");
        map.put("createdAt", workflow.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        map.put("updatedAt", workflow.getUpdatedAt() != null ? workflow.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "N/A");
        map.put("isActive", workflow.getIsActive());
        map.put("taskCount", workflow.getTasks() != null ? workflow.getTasks().size() : 0);
        return map;
    }
    
    // Convert task to map
    private Map<String, Object> taskToMap(Task task) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", task.getId());
        map.put("title", task.getTitle());
        map.put("description", task.getDescription());
        map.put("status", task.getStatus().getDisplayName());
        map.put("priority", task.getPriority().getDisplayName());
        map.put("workflow", task.getWorkflow() != null ? task.getWorkflow().getName() : "N/A");
        map.put("assignedTo", task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : "Unassigned");
        map.put("createdBy", task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : "N/A");
        map.put("createdAt", task.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        map.put("dueDate", task.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        map.put("completedAt", task.getCompletedAt() != null ? task.getCompletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "N/A");
        map.put("estimatedHours", task.getEstimatedHours());
        map.put("actualHours", task.getActualHours());
        return map;
    }
    
    // Methods for ReportController
    
    public List<String> getAvailableReportTypes() {
        return Arrays.asList("Task Report", "Workflow Report", "User Productivity", "Audit Logs");
    }
    
    public Map<String, Object> getTaskReportData(String status, String priority, Long departmentId) {
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "Task Report");
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Get filtered tasks based on parameters
        List<Task> tasks = getFilteredTasks(status, priority, departmentId);
        report.put("tasks", tasks.stream().map(this::taskToMap).collect(Collectors.toList()));
        report.put("totalTasks", tasks.size());
        
        // Calculate statistics
        Map<String, Long> statusStats = tasks.stream()
                .collect(Collectors.groupingBy(task -> task.getStatus().getDisplayName(), Collectors.counting()));
        report.put("statusStatistics", statusStats);
        
        Map<String, Long> priorityStats = tasks.stream()
                .collect(Collectors.groupingBy(task -> task.getPriority().getDisplayName(), Collectors.counting()));
        report.put("priorityStatistics", priorityStats);
        
        return report;
    }
    
    public Map<String, Object> getWorkflowReportData(String status, Long departmentId) {
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "Workflow Report");
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Get filtered workflows based on parameters
        List<Workflow> workflows = getFilteredWorkflows(status, departmentId);
        report.put("workflows", workflows.stream().map(this::workflowToMap).collect(Collectors.toList()));
        report.put("totalWorkflows", workflows.size());
        
        // Calculate statistics
        Map<String, Long> statusStats = workflows.stream()
                .collect(Collectors.groupingBy(w -> w.getStatus().getDisplayName(), Collectors.counting()));
        report.put("statusStatistics", statusStats);
        
        Map<String, Long> deptStats = workflows.stream()
                .filter(w -> w.getDepartment() != null)
                .collect(Collectors.groupingBy(w -> w.getDepartment().getName(), Collectors.counting()));
        report.put("departmentStatistics", deptStats);
        
        return report;
    }
    
    public Map<String, Object> getUserProductivityData(Long userId, Long departmentId) {
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "User Productivity");
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Get all users with optional filtering
        List<User> users = getFilteredUsers(userId, departmentId);
        
        List<Map<String, Object>> productivityData = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            userData.put("email", user.getEmail());
            
            // Get task statistics for this user
            Pageable pageable = PageRequest.of(0, 1000);
            Page<Task> userTasks = taskService.getTasksForUser(user.getId(), null, null, pageable);
            
            long totalTasks = userTasks.getTotalElements();
            long completedTasks = userTasks.getContent().stream()
                    .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
                    .count();
            
            userData.put("totalTasks", totalTasks);
            userData.put("completedTasks", completedTasks);
            userData.put("completionRate", totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0);
            
            // Calculate average completion days
            List<Task> completed = userTasks.getContent().stream()
                    .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED &&
                                   task.getCompletedAt() != null)
                    .collect(Collectors.toList());
            
            if (!completed.isEmpty()) {
                double avgCompletionDays = completed.stream()
                        .mapToLong(task -> java.time.Duration.between(task.getCreatedAt(), task.getCompletedAt()).toDays())
                        .average()
                        .orElse(0.0);
                userData.put("averageCompletionDays", Math.round(avgCompletionDays * 100.0) / 100.0);
            } else {
                userData.put("averageCompletionDays", 0.0);
            }
            
            productivityData.add(userData);
        }
        
        report.put("users", productivityData);
        report.put("totalUsers", productivityData.size());
        
        return report;
    }
    
    // Helper method to find ActionType by display name
    private AuditLog.ActionType findActionTypeByDisplayName(String displayName) {
        for (AuditLog.ActionType action : AuditLog.ActionType.values()) {
            if (action.getDisplayName().equalsIgnoreCase(displayName)) {
                return action;
            }
        }
        return null;
    }
    
    public List<AuditLog> getAuditLogData(String actionType, String entityType, Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(30); // Last 30 days
        
        if (actionType != null && !actionType.trim().isEmpty() && entityType != null && !entityType.trim().isEmpty() && userId != null) {
            // All filters applied
            AuditLog.ActionType action = findActionTypeByDisplayName(actionType);
            if (action != null) {
                return auditLogRepository.findByActionTypeAndEntityTypeAndUserIdAndCreatedAtGreaterThanOrderByCreatedAtDesc(
                        action, entityType, userId, since);
            }
            return Collections.emptyList();
        } else if (actionType != null && !actionType.trim().isEmpty() && entityType != null && !entityType.trim().isEmpty()) {
            // Action type and entity type filters
            AuditLog.ActionType action = findActionTypeByDisplayName(actionType);
            if (action != null) {
                return auditLogRepository.findByActionTypeAndEntityTypeAndCreatedAtGreaterThanOrderByCreatedAtDesc(
                        action, entityType, since);
            }
            return Collections.emptyList();
        } else if (actionType != null && !actionType.trim().isEmpty()) {
            // Action type filter only
            AuditLog.ActionType action = findActionTypeByDisplayName(actionType);
            if (action != null) {
                return auditLogRepository.findByActionTypeAndCreatedAtGreaterThanOrderByCreatedAtDesc(action, since);
            }
            return Collections.emptyList();
        } else if (entityType != null && !entityType.trim().isEmpty()) {
            // Entity type filter only
            return auditLogRepository.findByEntityTypeAndCreatedAtGreaterThanOrderByCreatedAtDesc(entityType, since);
        } else if (userId != null) {
            // User filter only
            return auditLogRepository.findByUserIdAndCreatedAtGreaterThanOrderByCreatedAtDesc(userId, since);
        } else {
            // No filters - get all recent logs
            return auditLogRepository.findByCreatedAtGreaterThanOrderByCreatedAtDesc(since);
        }
    }
    
    public List<String> getTaskStatusOptions() {
        return Arrays.stream(Task.TaskStatus.values())
                .map(Task.TaskStatus::getDisplayName)
                .collect(Collectors.toList());
    }
    
    public List<String> getTaskPriorityOptions() {
        return Arrays.stream(Task.TaskPriority.values())
                .map(Task.TaskPriority::getDisplayName)
                .collect(Collectors.toList());
    }
    
    public List<String> getWorkflowStatusOptions() {
        return Arrays.stream(Workflow.WorkflowStatus.values())
                .map(Workflow.WorkflowStatus::getDisplayName)
                .collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getDepartmentOptions() {
        return departmentRepository.findAll().stream()
                .map(dept -> {
                    Map<String, Object> option = new HashMap<>();
                    option.put("id", dept.getId());
                    option.put("name", dept.getName());
                    return option;
                })
                .collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getUserOptions() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> option = new HashMap<>();
                    option.put("id", user.getId());
                    option.put("username", user.getUsername());
                    option.put("displayName", user.getFirstName() + " " + user.getLastName());
                    return option;
                })
                .collect(Collectors.toList());
    }
    
    public List<String> getActionTypeOptions() {
        return Arrays.stream(AuditLog.ActionType.values())
                .map(AuditLog.ActionType::getDisplayName)
                .collect(Collectors.toList());
    }
    
    public List<String> getEntityTypeOptions() {
        return Arrays.asList("Task", "Workflow", "User", "Department", "Team", "Role");
    }
    
    // Helper methods for filtering
    
    private List<Task> getFilteredTasks(String status, String priority, Long departmentId) {
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Task> allTasks = taskService.getAllTasks(pageable);
        
        return allTasks.getContent().stream()
                .filter(task -> status == null || status.trim().isEmpty() || task.getStatus().getDisplayName().equalsIgnoreCase(status))
                .filter(task -> priority == null || priority.trim().isEmpty() || task.getPriority().getDisplayName().equalsIgnoreCase(priority))
                .filter(task -> departmentId == null ||
                        (task.getWorkflow() != null && task.getWorkflow().getDepartment() != null &&
                         task.getWorkflow().getDepartment().getId().equals(departmentId)))
                .collect(Collectors.toList());
    }
    
    private List<Workflow> getFilteredWorkflows(String status, Long departmentId) {
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Workflow> allWorkflows = workflowService.getAllWorkflows(pageable);
        
        return allWorkflows.getContent().stream()
                .filter(workflow -> status == null || status.trim().isEmpty() || workflow.getStatus().getDisplayName().equalsIgnoreCase(status))
                .filter(workflow -> departmentId == null ||
                        (workflow.getDepartment() != null && workflow.getDepartment().getId().equals(departmentId)))
                .collect(Collectors.toList());
    }
    
    private List<User> getFilteredUsers(Long userId, Long departmentId) {
        List<User> allUsers = userRepository.findAll();
        
        return allUsers.stream()
                .filter(user -> userId == null || user.getId().equals(userId))
                .filter(user -> departmentId == null ||
                        (user.getUserRoles().stream()
                                .filter(ur -> ur.isActive() == null || ur.isActive())
                                .anyMatch(ur -> ur.getDepartment() != null && ur.getDepartment().getId().equals(departmentId))))
                .collect(Collectors.toList());
    }
}