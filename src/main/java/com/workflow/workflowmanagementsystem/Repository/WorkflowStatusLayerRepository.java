package com.workflow.workflowmanagementsystem.Repository;

import com.workflow.workflowmanagementsystem.entity.WorkflowStatusLayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowStatusLayerRepository extends JpaRepository<WorkflowStatusLayer, Long> {
    
    /**
     * Find all workflow statuses for a specific workflow, ordered by their order field
     */
    List<WorkflowStatusLayer> findByWorkflowIdOrderByOrderAsc(Long workflowId);
    
    /**
     * Find a workflow status by name and workflow
     */
    Optional<WorkflowStatusLayer> findByNameAndWorkflowId(String name, Long workflowId);
    
    /**
     * Find the first status (lowest order) for a workflow
     */
    @Query("SELECT ws FROM WorkflowStatusLayer ws WHERE ws.workflow.id = :workflowId ORDER BY ws.order ASC")
    Optional<WorkflowStatusLayer> findFirstByWorkflowIdOrderByOrderAsc(@Param("workflowId") Long workflowId);
    
    /**
     * Find the next status in the workflow sequence
     */
    @Query("SELECT ws FROM WorkflowStatusLayer ws WHERE ws.workflow.id = :workflowId AND ws.order > :currentOrder ORDER BY ws.order ASC")
    Optional<WorkflowStatusLayer> findNextStatus(@Param("workflowId") Long workflowId, @Param("currentOrder") Integer currentOrder);
    
    /**
     * Find the previous status in the workflow sequence
     */
    @Query("SELECT ws FROM WorkflowStatusLayer ws WHERE ws.workflow.id = :workflowId AND ws.order < :currentOrder ORDER BY ws.order DESC")
    Optional<WorkflowStatusLayer> findPreviousStatus(@Param("workflowId") Long workflowId, @Param("currentOrder") Integer currentOrder);
    
    /**
     * Check if a status name already exists for a workflow (excluding a specific status ID)
     */
    @Query("SELECT COUNT(ws) > 0 FROM WorkflowStatusLayer ws WHERE ws.name = :name AND ws.workflow.id = :workflowId AND ws.id != :excludeId")
    boolean existsByNameAndWorkflowIdAndIdNot(@Param("name") String name, @Param("workflowId") Long workflowId, @Param("excludeId") Long excludeId);
    
    /**
     * Check if a status name already exists for a workflow
     */
    boolean existsByNameAndWorkflowId(String name, Long workflowId);
    
    /**
     * Find all final statuses for a workflow
     */
    List<WorkflowStatusLayer> findByWorkflowIdAndIsFinalTrueOrderByOrderAsc(Long workflowId);
    
    /**
     * Count the number of statuses for a workflow
     */
    @Query("SELECT COUNT(ws) FROM WorkflowStatusLayer ws WHERE ws.workflow.id = :workflowId")
    long countByWorkflowId(@Param("workflowId") Long workflowId);
    
    /**
     * Find the maximum order value for a workflow
     */
    @Query("SELECT COALESCE(MAX(ws.order), 0) FROM WorkflowStatusLayer ws WHERE ws.workflow.id = :workflowId")
    Integer findMaxOrderByWorkflowId(@Param("workflowId") Long workflowId);
}