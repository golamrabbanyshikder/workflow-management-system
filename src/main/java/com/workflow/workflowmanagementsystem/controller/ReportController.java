package com.workflow.workflowmanagementsystem.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
        
        // Add current filter values to maintain selection
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedDepartmentId", departmentId);
        
        return "reports/task-report";
    }

    @GetMapping("/workflow-report")
    public String workflowReport(@RequestParam(required = false) String status,
                              @RequestParam(required = false) Long departmentId,
                              Model model) {
        
        model.addAttribute("workflowReportData", reportService.getWorkflowReportData(status, departmentId));
        model.addAttribute("statusOptions", reportService.getWorkflowStatusOptions());
        model.addAttribute("departments", reportService.getDepartmentOptions());
        
        // Add current filter values to maintain selection
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDepartmentId", departmentId);
        
        return "reports/workflow-report";
    }

    @GetMapping("/user-productivity")
    public String userProductivityReport(@RequestParam(required = false) Long userId,
                                      @RequestParam(required = false) Long departmentId,
                                      Model model) {
        
        model.addAttribute("productivityData", reportService.getUserProductivityData(userId, departmentId));
        model.addAttribute("users", reportService.getUserOptions());
        model.addAttribute("departments", reportService.getDepartmentOptions());
        
        // Add current filter values to maintain selection
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedDepartmentId", departmentId);
        
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
        
        // Add current filter values to maintain selection
        model.addAttribute("selectedActionType", actionType);
        model.addAttribute("selectedEntityType", entityType);
        model.addAttribute("selectedUserId", userId);
        
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
    
    // PDF Export endpoints
    
    @GetMapping("/task-report/export/pdf")
    public void exportTaskReportToPDF(@RequestParam(required = false) String status,
                                     @RequestParam(required = false) String priority,
                                     @RequestParam(required = false) Long departmentId,
                                     jakarta.servlet.http.HttpServletResponse response) throws IOException, DocumentException {
        
        Map<String, Object> reportData = reportService.getTaskReportData(status, priority, departmentId);
        generateTaskReportPDF(reportData, response);
    }
    
    @GetMapping("/workflow-report/export/pdf")
    public void exportWorkflowReportToPDF(@RequestParam(required = false) String status,
                                        @RequestParam(required = false) Long departmentId,
                                        jakarta.servlet.http.HttpServletResponse response) throws IOException, DocumentException {
        
        Map<String, Object> reportData = reportService.getWorkflowReportData(status, departmentId);
        generateWorkflowReportPDF(reportData, response);
    }
    
    @GetMapping("/user-productivity/export/pdf")
    public void exportUserProductivityToPDF(@RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) Long departmentId,
                                         jakarta.servlet.http.HttpServletResponse response) throws IOException, DocumentException {
        
        Map<String, Object> reportData = reportService.getUserProductivityData(userId, departmentId);
        generateUserProductivityPDF(reportData, response);
    }
    
    @GetMapping("/audit-logs/export/pdf")
    public void exportAuditLogsToPDF(@RequestParam(required = false) String actionType,
                                    @RequestParam(required = false) String entityType,
                                    @RequestParam(required = false) Long userId,
                                    jakarta.servlet.http.HttpServletResponse response) throws IOException, DocumentException {
        
        List<AuditLog> auditLogs = reportService.getAuditLogData(actionType, entityType, userId);
        generateAuditLogsPDF(auditLogs, response);
    }
    
    // PDF generation methods
    
    private void generateTaskReportPDF(Map<String, Object> reportData, jakarta.servlet.http.HttpServletResponse response) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("Task Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Add generation timestamp
        Font timestampFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Paragraph timestamp = new Paragraph("Generated on: " + sdf.format(new Date()), timestampFont);
        timestamp.setAlignment(Element.ALIGN_CENTER);
        timestamp.setSpacingAfter(20);
        document.add(timestamp);
        
        // Create table
        PdfPTable table = new PdfPTable(8); // Title, Status, Priority, Workflow, Assigned To, Created By, Due Date, Completed Date
        table.setWidthPercentage(100);
        table.setWidths(new float[]{20f, 12f, 12f, 15f, 15f, 15f, 15f, 15f});
        
        // Add table headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        String[] headers = {"Task Title", "Status", "Priority", "Workflow", "Assigned To", "Created By", "Due Date", "Completed Date"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
        
        // Add table data
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) reportData.get("tasks");
        
        for (Map<String, Object> task : tasks) {
            table.addCell(createCell((String) task.get("title"), dataFont));
            table.addCell(createCell((String) task.get("status"), dataFont));
            table.addCell(createCell((String) task.get("priority"), dataFont));
            table.addCell(createCell((String) task.get("workflow"), dataFont));
            table.addCell(createCell((String) task.get("assignedTo"), dataFont));
            table.addCell(createCell((String) task.get("createdBy"), dataFont));
            table.addCell(createCell((String) task.get("dueDate"), dataFont));
            table.addCell(createCell((String) task.get("completedAt"), dataFont));
        }
        
        document.add(table);
        document.close();
        
        // Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"task-report.pdf\"");
        response.getOutputStream().write(outputStream.toByteArray());
    }
    
    private void generateWorkflowReportPDF(Map<String, Object> reportData, jakarta.servlet.http.HttpServletResponse response) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("Workflow Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Add generation timestamp
        Font timestampFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Paragraph timestamp = new Paragraph("Generated on: " + sdf.format(new Date()), timestampFont);
        timestamp.setAlignment(Element.ALIGN_CENTER);
        timestamp.setSpacingAfter(20);
        document.add(timestamp);
        
        // Create table
        PdfPTable table = new PdfPTable(6); // Name, Status, Department, Created By, Created Date, Task Count
        table.setWidthPercentage(100);
        table.setWidths(new float[]{25f, 12f, 15f, 15f, 18f, 15f});
        
        // Add table headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        String[] headers = {"Workflow Name", "Status", "Department", "Created By", "Created Date", "Task Count"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
        
        // Add table data
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> workflows = (List<Map<String, Object>>) reportData.get("workflows");
        
        for (Map<String, Object> workflow : workflows) {
            table.addCell(createCell((String) workflow.get("name"), dataFont));
            table.addCell(createCell((String) workflow.get("status"), dataFont));
            table.addCell(createCell((String) workflow.get("department"), dataFont));
            table.addCell(createCell((String) workflow.get("createdBy"), dataFont));
            table.addCell(createCell((String) workflow.get("createdAt"), dataFont));
            table.addCell(createCell(workflow.get("taskCount") != null ? workflow.get("taskCount").toString() : "0", dataFont));
        }
        
        document.add(table);
        document.close();
        
        // Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"workflow-report.pdf\"");
        response.getOutputStream().write(outputStream.toByteArray());
    }
    
    private void generateUserProductivityPDF(Map<String, Object> reportData, jakarta.servlet.http.HttpServletResponse response) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("User Productivity Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Add generation timestamp
        Font timestampFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Paragraph timestamp = new Paragraph("Generated on: " + sdf.format(new Date()), timestampFont);
        timestamp.setAlignment(Element.ALIGN_CENTER);
        timestamp.setSpacingAfter(20);
        document.add(timestamp);
        
        // Create table
        PdfPTable table = new PdfPTable(5); // Username, Total Tasks, Completed Tasks, Completion Rate, Average Completion Days
        table.setWidthPercentage(100);
        table.setWidths(new float[]{20f, 15f, 15f, 20f, 20f});
        
        // Add table headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        String[] headers = {"Username", "Total Tasks", "Completed Tasks", "Completion Rate (%)", "Average Completion Days"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
        
        // Add table data
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> users = (List<Map<String, Object>>) reportData.get("users");
        
        for (Map<String, Object> user : users) {
            table.addCell(createCell((String) user.get("username"), dataFont));
            table.addCell(createCell(user.get("totalTasks").toString(), dataFont));
            table.addCell(createCell(user.get("completedTasks").toString(), dataFont));
            table.addCell(createCell(user.get("completionRate").toString(), dataFont));
            table.addCell(createCell(user.get("averageCompletionDays").toString(), dataFont));
        }
        
        document.add(table);
        document.close();
        
        // Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"user-productivity-report.pdf\"");
        response.getOutputStream().write(outputStream.toByteArray());
    }
    
    private void generateAuditLogsPDF(List<AuditLog> auditLogs, jakarta.servlet.http.HttpServletResponse response) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("Audit Logs Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Add generation timestamp
        Font timestampFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Paragraph timestamp = new Paragraph("Generated on: " + sdf.format(new Date()), timestampFont);
        timestamp.setAlignment(Element.ALIGN_CENTER);
        timestamp.setSpacingAfter(20);
        document.add(timestamp);
        
        // Create table
        PdfPTable table = new PdfPTable(6); // Date/Time, Action, Entity Type, Description, User, IP Address
        table.setWidthPercentage(100);
        table.setWidths(new float[]{15f, 12f, 12f, 25f, 15f, 15f});
        
        // Add table headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        String[] headers = {"Date/Time", "Action", "Entity Type", "Description", "User", "IP Address"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
        
        // Add table data
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        
        for (AuditLog log : auditLogs) {
            table.addCell(createCell(log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), dataFont));
            table.addCell(createCell(log.getActionType().getDisplayName(), dataFont));
            table.addCell(createCell(log.getEntityType(), dataFont));
            table.addCell(createCell(log.getDescription(), dataFont));
            table.addCell(createCell(log.getUser().getUsername(), dataFont));
            table.addCell(createCell(log.getIpAddress(), dataFont));
        }
        
        document.add(table);
        document.close();
        
        // Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"audit-logs-report.pdf\"");
        response.getOutputStream().write(outputStream.toByteArray());
    }
    
    private PdfPCell createCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content != null ? content : "N/A", font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
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