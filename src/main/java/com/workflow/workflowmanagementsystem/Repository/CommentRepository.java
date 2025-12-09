package com.workflow.workflowmanagementsystem.Repository;

import com.workflow.workflowmanagementsystem.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Find comments by task
    List<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    // Find comments by user
    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find comments by task with pagination
    Page<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable);
    
    // Count comments by task
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.task.id = :taskId")
    Long countCommentsByTask(@Param("taskId") Long taskId);
    
    // Find comments created within date range
    @Query("SELECT c FROM Comment c WHERE " +
           "c.createdAt BETWEEN :startDate AND :endDate")
    List<Comment> findCommentsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Find comments by task and user
    List<Comment> findByTaskIdAndUserIdOrderByCreatedAtDesc(Long taskId, Long userId);
    
    // Search comments by content
    @Query("SELECT c FROM Comment c WHERE " +
           "LOWER(c.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Comment> searchComments(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Find edited comments
    List<Comment> findByIsEditedTrueOrderByUpdatedAtDesc();
    
    // Find recent comments for a user
    @Query("SELECT c FROM Comment c WHERE " +
           "c.user.id = :userId AND c.createdAt >= :sinceDate " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsForUser(
            @Param("userId") Long userId,
            @Param("sinceDate") LocalDateTime sinceDate);
    
    // Get comment statistics by user
    @Query("SELECT u.username, COUNT(c) as commentCount " +
           "FROM Comment c JOIN c.user u " +
           "GROUP BY u.username ORDER BY commentCount DESC")
    List<Object[]> getCommentStatisticsByUser();
    
    // Get comment statistics by task
    @Query("SELECT t.title, COUNT(c) as commentCount " +
           "FROM Comment c JOIN c.task t " +
           "GROUP BY t.title ORDER BY commentCount DESC")
    List<Object[]> getCommentStatisticsByTask();
}