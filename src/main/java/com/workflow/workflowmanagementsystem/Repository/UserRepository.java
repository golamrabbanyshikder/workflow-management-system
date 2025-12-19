package com.workflow.workflowmanagementsystem.Repository;

import com.workflow.workflowmanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    List<User> findByTeamId(Long teamId);
    
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.firstName LIKE %:keyword% OR u.lastName LIKE %:keyword%")
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(@Param("keyword") String keyword);
    
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.team.id = :teamId AND u.enabled = true")
    List<User> findActiveUsersByTeamId(@Param("teamId") Long teamId);
    
    @Query("SELECT u FROM User u WHERE u.team.department.id = :departmentId AND u.enabled = true")
    List<User> findActiveUsersByDepartmentId(@Param("departmentId") Long departmentId);
}
