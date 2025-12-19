package com.workflow.workflowmanagementsystem.Repository;

import com.workflow.workflowmanagementsystem.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByDepartmentId(Long departmentId);
    boolean existsByNameAndDepartmentId(String name, Long departmentId);
    List<Team> findByDepartmentName(String departmentName);
    Optional<Team> findByName(String name);
    List<Team> findByActive(Boolean active);
    List<Team> findByDepartmentIdAndActive(Long departmentId, Boolean active);
    List<Team> findByTeamLeadId(Long teamLeadId);
    
    @Query("SELECT t FROM Team t WHERE t.name LIKE %:name% AND (:departmentId IS NULL OR t.department.id = :departmentId)")
    List<Team> findByNameContainingAndDepartmentId(@Param("name") String name, @Param("departmentId") Long departmentId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.team.id = :teamId")
    Long countMembersByTeamId(@Param("teamId") Long teamId);
    
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.id = :userId")
    List<Team> findTeamsByUserId(@Param("userId") Long userId);
}
