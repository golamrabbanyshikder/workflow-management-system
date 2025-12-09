package com.workflow.workflowmanagementsystem.service;

import com.workflow.workflowmanagementsystem.Repository.AuditLogRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.Repository.WorkflowRepository;
import com.workflow.workflowmanagementsystem.entity.AuditLog;
import com.workflow.workflowmanagementsystem.entity.Department;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.Workflow;
import com.workflow.workflowmanagementsystem.entity.Workflow.WorkflowStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class WorkflowService {
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    // Create a new workflow
    public Workflow createWorkflow(Workflow workflow, Long createdByUserId) {
        // Validate workflow name uniqueness
        if (workflowRepository.existsByNameIgnoreCase(workflow.getName())) {
            throw new IllegalArgumentException("Workflow with name '" + workflow.getName() + "' already exists");
        }
        
        // Set creator
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + createdByUserId));
        workflow.setCreatedBy(createdBy);
        
        // Validate department if provided
        if (workflow.getDepartment() != null && workflow.getDepartment().getId() != null) {
            Department department = departmentService.getDepartmentById(workflow.getDepartment().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found with ID: " + workflow.getDepartment().getId()));
            workflow.setDepartment(department);
        }
        
        Workflow savedWorkflow = workflowRepository.save(workflow);
        
        // Log the creation
        logAuditAction(AuditLog.ActionType.CREATE, "Workflow", savedWorkflow.getId(), 
                      "Created workflow: " + savedWorkflow.getName(), createdBy);
        
        return savedWorkflow;
    }
    
    // Update an existing workflow
    public Workflow updateWorkflow(Long id, Workflow workflowDetails, Long updatedByUserId) {
        Workflow existingWorkflow = workflowRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with ID: " + id));
        
        // Check name uniqueness if name is being changed
        if (!existingWorkflow.getName().equalsIgnoreCase(workflowDetails.getName()) &&
            workflowRepository.existsByNameIgnoreCaseAndIdNot(workflowDetails.getName(), id)) {
            throw new IllegalArgumentException("Workflow with name '" + workflowDetails.getName() + "' already exists");
        }
        
        User updatedBy = userRepository.findById(updatedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + updatedByUserId));
        
        // Store old values for audit
        String oldValues = String.format("Name: %s, Description: %s, Status: %s", 
                existingWorkflow.getName(), existingWorkflow.getDescription(), existingWorkflow.getStatus());
        
        // Update fields
        existingWorkflow.setName(workflowDetails.getName());
        existingWorkflow.setDescription(workflowDetails.getDescription());
        existingWorkflow.setStatus(workflowDetails.getStatus());
        existingWorkflow.setIsActive(workflowDetails.getIsActive());
        
        // Update department if provided
        if (workflowDetails.getDepartment() != null && workflowDetails.getDepartment().getId() != null) {
            Department department = departmentService.getDepartmentById(workflowDetails.getDepartment().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found with ID: " + workflowDetails.getDepartment().getId()));
            existingWorkflow.setDepartment(department);
        }
        
        Workflow updatedWorkflow = workflowRepository.save(existingWorkflow);
        
        // Log the update
        String newValues = String.format("Name: %s, Description: %s, Status: %s", 
                updatedWorkflow.getName(), updatedWorkflow.getDescription(), updatedWorkflow.getStatus());
        
        logAuditAction(AuditLog.ActionType.UPDATE, "Workflow", updatedWorkflow.getId(), 
                      "Updated workflow: " + updatedWorkflow.getName(), updatedBy, oldValues, newValues);
        
        return updatedWorkflow;
    }
    
    // Get workflow by ID
    public Optional<Workflow> getWorkflowById(Long id) {
        return workflowRepository.findById(id);
    }
    
    // Get all workflows with pagination
    public Page<Workflow> getAllWorkflows(Pageable pageable) {
        return workflowRepository.findAll(pageable);
    }
    
    // Get workflows by status
    public List<Workflow> getWorkflowsByStatus(WorkflowStatus status) {
        return workflowRepository.findByStatus(status);
    }
    
    // Get workflows by department
    public List<Workflow> getWorkflowsByDepartment(Long departmentId) {
        return workflowRepository.findByDepartmentId(departmentId);
    }
    
    // Get workflows created by user
    public List<Workflow> getWorkflowsByCreator(Long createdBy) {
        return workflowRepository.findByCreatedById(createdBy);
    }
    
    // Get active workflows
    public List<Workflow> getActiveWorkflows() {
        return workflowRepository.findByIsActiveTrue();
    }
    
    // Search workflows
    public Page<Workflow> searchWorkflows(String searchTerm, Pageable pageable) {
        return workflowRepository.searchWorkflows(searchTerm, pageable);
    }
    
    // Get workflows with filters
    public Page<Workflow> getWorkflowsWithFilters(WorkflowStatus status, Long departmentId, 
                                                  Boolean isActive, Pageable pageable) {
        return workflowRepository.findWorkflowsWithFilters(status, departmentId, isActive, pageable);
    }
    
    // Delete workflow
    public void deleteWorkflow(Long id, Long deletedByUserId) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with ID: " + id));
        
        User deletedBy = userRepository.findById(deletedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + deletedByUserId));
        
        // Check if workflow has tasks
        if (!workflow.getTasks().isEmpty()) {
            throw new IllegalStateException("Cannot delete workflow with existing tasks. Please delete all tasks first.");
        }
        
        workflowRepository.delete(workflow);
        
        // Log the deletion
        logAuditAction(AuditLog.ActionType.DELETE, "Workflow", id, 
                      "Deleted workflow: " + workflow.getName(), deletedBy);
    }
    
    // Change workflow status
    public Workflow changeWorkflowStatus(Long id, WorkflowStatus newStatus, Long changedByUserId) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with ID: " + id));
        
        User changedBy = userRepository.findById(changedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + changedByUserId));
        
        WorkflowStatus oldStatus = workflow.getStatus();
        workflow.setStatus(newStatus);
        
        Workflow updatedWorkflow = workflowRepository.save(workflow);
        
        // Log the status change
        logAuditAction(AuditLog.ActionType.UPDATE, "Workflow", id, 
                      "Changed status from " + oldStatus + " to " + newStatus + " for workflow: " + workflow.getName(), 
                      changedBy, oldStatus.toString(), newStatus.toString());
        
        return updatedWorkflow;
    }
    
    // Get workflow statistics
    public Map<String, Object> getWorkflowStatistics() {
        List<Object[]> statusStats = workflowRepository.countWorkflowsByStatus();
        List<Object[]> departmentStats = workflowRepository.countWorkflowsByDepartment();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("statusStatistics", statusStats);
        stats.put("departmentStatistics", departmentStats);
        return stats;
    }
    
    // Get workflows created within date range
    public List<Workflow> getWorkflowsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return workflowRepository.findWorkflowsByDateRange(startDate, endDate);
    }
    
    // Helper method to log audit actions
    private void logAuditAction(AuditLog.ActionType actionType, String entityType, Long entityId, 
                               String description, User user) {
        logAuditAction(actionType, entityType, entityId, description, user, null, null);
    }
    
    private void logAuditAction(AuditLog.ActionType actionType, String entityType, Long entityId, 
                               String description, User user, String oldValues, String newValues) {
        AuditLog auditLog = new AuditLog(actionType, entityType, entityId, description, user);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLogRepository.save(auditLog);
    }
}