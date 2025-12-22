package com.workflow.workflowmanagementsystem.Repository;

import com.workflow.workflowmanagementsystem.entity.UserRole;
import com.workflow.workflowmanagementsystem.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByIdUserId(Long userId);
    List<UserRole> findByIdRoleId(Long roleId);
    List<UserRole> findByDepartmentId(Long departmentId);
    List<UserRole> findByTeamId(Long teamId);
    List<UserRole> findByIdUserIdAndActive(Long userId, Boolean active);
    List<UserRole> findByIdRoleIdAndActive(Long roleId, Boolean active);
    List<UserRole> findByDepartmentIdAndActive(Long departmentId, Boolean active);
    List<UserRole> findByTeamIdAndActive(Long teamId, Boolean active);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.id.userId = :userId AND ur.id.roleId = :roleId AND ur.active = true")
    Optional<UserRole> findActiveUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.id.userId = :userId AND ur.department.id = :departmentId AND ur.team.id = :teamId AND ur.active = true")
    Optional<UserRole> findActiveUserRoleByUserDepartmentTeam(@Param("userId") Long userId, @Param("departmentId") Long departmentId, @Param("teamId") Long teamId);
    
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.id.roleId = :roleId AND ur.active = true")
    Long countActiveUsersByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.department.id = :departmentId AND ur.active = true")
    Long countActiveUsersByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.team.id = :teamId AND ur.active = true")
    Long countActiveUsersByTeamId(@Param("teamId") Long teamId);
    
    @Query("SELECT DISTINCT ur.id.userId FROM UserRole ur WHERE ur.id.roleId = :roleId AND ur.active = true")
    List<Long> findActiveUserIdsByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT DISTINCT ur.id.userId FROM UserRole ur WHERE ur.department.id = :departmentId AND ur.active = true")
    List<Long> findActiveUserIdsByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT DISTINCT ur.id.userId FROM UserRole ur WHERE ur.team.id = :teamId AND ur.active = true")
    List<Long> findActiveUserIdsByTeamId(@Param("teamId") Long teamId);
}