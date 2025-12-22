package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.Repository.AuditLogRepository;
import com.workflow.workflowmanagementsystem.Repository.DepartmentRepository;
import com.workflow.workflowmanagementsystem.Repository.RoleRepository;
import com.workflow.workflowmanagementsystem.Repository.TeamRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRoleRepository;
import com.workflow.workflowmanagementsystem.entity.AuditLog;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.util.RoleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping({"", "/"})
    public String adminPanel(Model model, HttpServletRequest request) {
        // Set user roles in session
        User currentUser = RoleUtil.getCurrentUser(userRepository);
        RoleUtil.setUserRolesInSession(currentUser, userRoleRepository, request);

        // Log admin login to audit trail
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType(AuditLog.ActionType.LOGIN);
        auditLog.setEntityType("Admin Panel");
        auditLog.setDescription("Admin user " + currentUser.getUsername() + " accessed admin panel");
        auditLog.setUser(currentUser);
        auditLog.setIpAddress(getClientIpAddress(request));
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLogRepository.save(auditLog);

        // Get admin statistics
        Map<String, Long> stats = new HashMap<>();
        stats.put("userCount", userRepository.count());
        stats.put("departmentCount", departmentRepository.count());
        stats.put("roleCount", roleRepository.count());
        stats.put("teamCount", teamRepository.count());

        model.addAttribute("user", currentUser);
        model.addAttribute("stats", stats);
        model.addAttribute("currentUri", request.getRequestURI());
        
        return "admin/admin-panel";
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}