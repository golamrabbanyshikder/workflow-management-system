package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.Repository.AuditLogRepository;
import com.workflow.workflowmanagementsystem.Repository.DepartmentRepository;
import com.workflow.workflowmanagementsystem.Repository.RoleRepository;
import com.workflow.workflowmanagementsystem.Repository.TeamRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.entity.AuditLog;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.service.TaskService;
import com.workflow.workflowmanagementsystem.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    
    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    // Main dashboard page
    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get basic statistics for dashboard
        Map<String, Long> stats = new HashMap<>();
        stats.put("roleCount", roleRepository.count());
        stats.put("departmentCount", departmentRepository.count());
        stats.put("teamCount", teamRepository.count());
        stats.put("userCount", userRepository.count());

        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        return "dashboard";
    }
    
    // API endpoint for dashboard statistics
    @GetMapping("/api/statistics")
    @ResponseBody
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Get workflow statistics
        Map<String, Object> workflowStats = workflowService.getWorkflowStatistics();
        List<Object[]> workflowStatusStats = (List<Object[]>) workflowStats.get("statusStatistics");
        
        // Get task statistics
        Map<String, Object> taskStats = taskService.getTaskStatistics();
        List<Object[]> taskStatusStats = (List<Object[]>) taskStats.get("statusStatistics");
        List<Object[]> taskPriorityStats = (List<Object[]>) taskStats.get("priorityStatistics");
        
        // Calculate totals
        int totalWorkflows = workflowStatusStats.stream().mapToInt(arr -> ((Long) arr[1]).intValue()).sum();
        int totalTasks = taskStatusStats.stream().mapToInt(arr -> ((Long) arr[1]).intValue()).sum();
        int pendingTasks = getTaskCountByStatus(taskStatusStats, "PENDING");
        int overdueTasks = taskService.getOverdueTasks().size();
        
        // Basic statistics
        statistics.put("totalWorkflows", totalWorkflows);
        statistics.put("totalTasks", totalTasks);
        statistics.put("pendingTasks", pendingTasks);
        statistics.put("overdueTasks", overdueTasks);
        statistics.put("taskStatusStats", taskStatusStats);
        statistics.put("workflowStatusStats", workflowStatusStats);
        statistics.put("priorityStats", taskPriorityStats);
        
        // Task trend data (last 7 days)
        statistics.put("taskTrendData", getTaskTrendData());
        
        return statistics;
    }
    
    // API endpoint for recent activities
    @GetMapping("/api/audit/recent")
    @ResponseBody
    public List<Map<String, Object>> getRecentActivities() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Get current user from database
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        // Get recent audit logs for the current user (last 24 hours)
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<AuditLog> recentLogs = auditLogRepository.findRecentAuditLogsForUser(
            currentUser.getId(), // Use actual user ID from database
            since
        );
        
        return recentLogs.stream().limit(10).map(log -> {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", log.getId());
            activity.put("action", log.getActionType().getDisplayName());
            activity.put("description", log.getDescription());
            activity.put("timestamp", log.getCreatedAt());
            activity.put("entityType", log.getEntityType());
            activity.put("entityId", log.getEntityId());
            return activity;
        }).collect(Collectors.toList());
    }
    
    // API endpoint for notifications
    @GetMapping("/api/notifications")
    @ResponseBody
    public List<Map<String, Object>> getNotifications() {
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        // Get overdue tasks as urgent notifications
        List<com.workflow.workflowmanagementsystem.entity.Task> overdueTasks = taskService.getOverdueTasks();
        for (com.workflow.workflowmanagementsystem.entity.Task task : overdueTasks) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", System.currentTimeMillis() + Math.random());
            notification.put("title", "Overdue Task");
            notification.put("message", "Task '" + task.getTitle() + "' is overdue!");
            notification.put("type", "URGENT");
            notification.put("timestamp", task.getDueDate());
            notification.put("entityId", task.getId());
            notifications.add(notification);
        }
        
        // Get tasks due soon (within 3 days) as warning notifications
        List<com.workflow.workflowmanagementsystem.entity.Task> tasksDueSoon = taskService.getTasksDueWithinDays(3);
        for (com.workflow.workflowmanagementsystem.entity.Task task : tasksDueSoon) {
            if (task.getStatus() != com.workflow.workflowmanagementsystem.entity.Task.TaskStatus.COMPLETED) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("id", System.currentTimeMillis() + Math.random());
                notification.put("title", "Task Due Soon");
                notification.put("message", "Task '" + task.getTitle() + "' is due soon!");
                notification.put("type", "WARNING");
                notification.put("timestamp", task.getDueDate());
                notification.put("entityId", task.getId());
                notifications.add(notification);
            }
        }
        
        // Sort by timestamp (most recent first)
        notifications.sort((a, b) -> ((Comparable) b.get("timestamp")).compareTo(a.get("timestamp")));
        
        return notifications.stream().limit(10).collect(Collectors.toList());
    }
    
    // Helper method to get task count by status
    private int getTaskCountByStatus(List<Object[]> taskStatusStats, String status) {
        return taskStatusStats.stream()
                .filter(arr -> status.equals(arr[0]))
                .mapToInt(arr -> ((Long) arr[1]).intValue())
                .findFirst()
                .orElse(0);
    }
    
    // Helper method to generate task trend data
    private Map<String, Object> getTaskTrendData() {
        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        
        // Get completed tasks for the last 7 days
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        
        // For demo purposes, return sample data
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = endDate.minusDays(i);
            labels.add(dayStart.toLocalDate().toString());
            data.add((int) (Math.random() * 10)); // Sample data
        }
        
        Map<String, Object> trendData = new HashMap<>();
        trendData.put("labels", labels);
        trendData.put("data", data);
        
        return trendData;
    }
}