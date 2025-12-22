package com.workflow.workflowmanagementsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/audit")
public class AuditController {

    @GetMapping({"", "/"})
    public String auditLogs() {
        // Redirect to the existing audit logs page in reports
        return "redirect:/reports/audit-logs";
    }
}