package com.workflow.workflowmanagementsystem.Repository;

import com.workflow.workflowmanagementsystem.entity.AuditLog;
import com.workflow.workflowmanagementsystem.entity.AuditLog.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find audit logs by action type
    List<AuditLog> findByActionTypeOrderByCreatedAtDesc(ActionType actionType);
    
    // Find audit logs by entity type
    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);
    
    // Find audit logs by user
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find audit logs by entity type and entity ID
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
    
    // Find audit logs within date range
    @Query("SELECT a FROM AuditLog a WHERE " +
           "a.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY a.createdAt DESC")
    List<AuditLog> findAuditLogsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Find audit logs with pagination and filtering
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findAuditLogsWithFilters(
            @Param("actionType") ActionType actionType,
            @Param("entityType") String entityType,
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    // Count audit logs by action type
    @Query("SELECT a.actionType, COUNT(a) FROM AuditLog a GROUP BY a.actionType")
    List<Object[]> countAuditLogsByActionType();
    
    // Count audit logs by entity type
    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a GROUP BY a.entityType")
    List<Object[]> countAuditLogsByEntityType();
    
    // Count audit logs by user
    @Query("SELECT u.username, COUNT(a) FROM AuditLog a " +
           "JOIN a.user u GROUP BY u.username ORDER BY COUNT(a) DESC")
    List<Object[]> countAuditLogsByUser();
    
    // Find recent audit logs for a user
    @Query("SELECT a FROM AuditLog a WHERE " +
           "a.user.id = :userId AND a.createdAt >= :sinceDate " +
           "ORDER BY a.createdAt DESC")
    List<AuditLog> findRecentAuditLogsForUser(
            @Param("userId") Long userId,
            @Param("sinceDate") LocalDateTime sinceDate);
    
    // Search audit logs by description
    @Query("SELECT a FROM AuditLog a WHERE " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> searchAuditLogs(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Get audit log statistics for dashboard
    @Query("SELECT " +
           "COUNT(a) as totalLogs, " +
           "COUNT(DISTINCT a.user.id) as uniqueUsers, " +
           "COUNT(DISTINCT a.entityType) as entityTypes, " +
           "COUNT(CASE WHEN a.actionType = 'CREATE' THEN 1 END) as creates, " +
           "COUNT(CASE WHEN a.actionType = 'UPDATE' THEN 1 END) as updates, " +
           "COUNT(CASE WHEN a.actionType = 'DELETE' THEN 1 END) as deletes " +
           "FROM AuditLog a WHERE " +
           "a.createdAt BETWEEN :startDate AND :endDate")
    Object[] getAuditLogStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Find failed login attempts
    @Query("SELECT a FROM AuditLog a WHERE " +
           "a.actionType = 'LOGIN' AND a.description LIKE '%failed%' " +
           "ORDER BY a.createdAt DESC")
    List<AuditLog> findFailedLoginAttempts();
    
    // Find audit logs for specific entity changes
    @Query("SELECT a FROM AuditLog a WHERE " +
           "a.entityType = :entityType AND a.entityId = :entityId " +
           "AND a.actionType IN ('CREATE', 'UPDATE', 'DELETE') " +
           "ORDER BY a.createdAt DESC")
    List<AuditLog> findEntityChangeHistory(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId);
    
    // Get daily activity count
    @Query("SELECT DATE(a.createdAt), COUNT(a) FROM AuditLog a " +
           "WHERE a.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt)")
    List<Object[]> getDailyActivityCount(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Additional methods for ReportService
    List<AuditLog> findByActionTypeAndEntityTypeAndUserIdAndCreatedAtGreaterThanOrderByCreatedAtDesc(
            @Param("actionType") ActionType actionType,
            @Param("entityType") String entityType,
            @Param("userId") Long userId,
            @Param("sinceDate") LocalDateTime sinceDate);
    
    List<AuditLog> findByActionTypeAndEntityTypeAndCreatedAtGreaterThanOrderByCreatedAtDesc(
            @Param("actionType") ActionType actionType,
            @Param("entityType") String entityType,
            @Param("sinceDate") LocalDateTime sinceDate);
    
    List<AuditLog> findByActionTypeAndCreatedAtGreaterThanOrderByCreatedAtDesc(
            @Param("actionType") ActionType actionType,
            @Param("sinceDate") LocalDateTime sinceDate);
    
    List<AuditLog> findByEntityTypeAndCreatedAtGreaterThanOrderByCreatedAtDesc(
            @Param("entityType") String entityType,
            @Param("sinceDate") LocalDateTime sinceDate);
    
    List<AuditLog> findByUserIdAndCreatedAtGreaterThanOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("sinceDate") LocalDateTime sinceDate);
    
    List<AuditLog> findByCreatedAtGreaterThanOrderByCreatedAtDesc(
            @Param("sinceDate") LocalDateTime sinceDate);
}