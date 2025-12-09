package com.workflow.workflowmanagementsystem.Repository;

import com.workflow.workflowmanagementsystem.entity.Task;
import com.workflow.workflowmanagementsystem.entity.Task.TaskPriority;
import com.workflow.workflowmanagementsystem.entity.Task.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Find tasks by status
    List<Task> findByStatus(TaskStatus status);
    
    // Find tasks by priority
    List<Task> findByPriority(TaskPriority priority);
    
    // Find tasks by assigned user
    List<Task> findByAssignedToId(Long assignedToId);
    
    // Find tasks by workflow
    List<Task> findByWorkflowId(Long workflowId);
    
    // Find tasks by creator
    List<Task> findByCreatedById(Long createdBy);
    
    // Find tasks due before a specific date
    List<Task> findByDueDateBefore(LocalDateTime dueDate);
    
    // Find overdue tasks
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDateTime currentDate);
    
    // Find tasks due within next N days
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findTasksDueWithinDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Search tasks by title or description
    @Query("SELECT t FROM Task t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Task> searchTasks(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Count tasks by status
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksByStatus();
    
    // Count tasks by priority
    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority")
    List<Object[]> countTasksByPriority();
    
    // Count tasks by assigned user
    @Query("SELECT u.username, COUNT(t) FROM Task t " +
           "JOIN t.assignedTo u WHERE t.status NOT IN ('COMPLETED', 'CANCELLED') " +
           "GROUP BY u.username")
    List<Object[]> countTasksByAssignedUser();
    
    // Find tasks with pagination and filtering
    @Query("SELECT t FROM Task t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:assignedToId IS NULL OR t.assignedTo.id = :assignedToId) AND " +
           "(:workflowId IS NULL OR t.workflow.id = :workflowId)")
    Page<Task> findTasksWithFilters(
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("assignedToId") Long assignedToId,
            @Param("workflowId") Long workflowId,
            Pageable pageable);
    
    // Find tasks created within date range
    @Query("SELECT t FROM Task t WHERE " +
           "t.createdAt BETWEEN :startDate AND :endDate")
    List<Task> findTasksByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Find completed tasks within date range
    @Query("SELECT t FROM Task t WHERE " +
           "t.completedAt BETWEEN :startDate AND :endDate AND t.status = 'COMPLETED'")
    List<Task> findCompletedTasksByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Get task completion statistics
    @Query("SELECT COUNT(t) as total, " +
           "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
           "SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress, " +
           "SUM(CASE WHEN t.status = 'PENDING' THEN 1 ELSE 0 END) as pending " +
           "FROM Task t WHERE t.workflow.id = :workflowId")
    Object[] getTaskStatisticsByWorkflow(@Param("workflowId") Long workflowId);
    
    // Find tasks for a user with specific filters
    @Query("SELECT t FROM Task t WHERE " +
           "(t.assignedTo.id = :userId OR t.createdBy.id = :userId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority)")
    Page<Task> findTasksForUser(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            Pageable pageable);
    
    // Check if task title exists in a workflow (excluding current task)
    boolean existsByTitleIgnoreCaseAndWorkflowIdAndIdNot(String title, Long workflowId, Long id);
    
    // Check if task title exists in a workflow
    boolean existsByTitleIgnoreCaseAndWorkflowId(String title, Long workflowId);
}