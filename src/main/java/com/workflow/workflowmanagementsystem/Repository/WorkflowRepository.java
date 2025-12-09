package com.workflow.workflowmanagementsystem.Repository;

import com.workflow.workflowmanagementsystem.entity.Workflow;
import com.workflow.workflowmanagementsystem.entity.Workflow.WorkflowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    
    // Find workflows by status
    List<Workflow> findByStatus(WorkflowStatus status);
    
    // Find workflows by department
    List<Workflow> findByDepartmentId(Long departmentId);
    
    // Find workflows by creator
    List<Workflow> findByCreatedById(Long createdBy);
    
    // Find active workflows
    List<Workflow> findByIsActiveTrue();
    
    // Find workflows by status and department
    List<Workflow> findByStatusAndDepartmentId(WorkflowStatus status, Long departmentId);
    
    // Search workflows by name or description
    @Query("SELECT w FROM Workflow w WHERE " +
           "LOWER(w.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Workflow> searchWorkflows(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Count workflows by status
    @Query("SELECT w.status, COUNT(w) FROM Workflow w GROUP BY w.status")
    List<Object[]> countWorkflowsByStatus();
    
    // Count workflows by department
    @Query("SELECT d.name, COUNT(w) FROM Workflow w " +
           "JOIN w.department d GROUP BY d.name")
    List<Object[]> countWorkflowsByDepartment();
    
    // Find workflows with pagination and filtering
    @Query("SELECT w FROM Workflow w WHERE " +
           "(:status IS NULL OR w.status = :status) AND " +
           "(:departmentId IS NULL OR w.department.id = :departmentId) AND " +
           "(:isActive IS NULL OR w.isActive = :isActive)")
    Page<Workflow> findWorkflowsWithFilters(
            @Param("status") WorkflowStatus status,
            @Param("departmentId") Long departmentId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
    
    // Find workflows created within date range
    @Query("SELECT w FROM Workflow w WHERE " +
           "w.createdAt BETWEEN :startDate AND :endDate")
    List<Workflow> findWorkflowsByDateRange(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
    
    // Check if workflow name exists (excluding current workflow)
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    
    // Check if workflow name exists
    boolean existsByNameIgnoreCase(String name);
}