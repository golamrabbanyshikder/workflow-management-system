package com.workflow.workflowmanagementsystem.Repository;
import com.workflow.workflowmanagementsystem.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
    List<Role> findByActive(Boolean active);
    List<Role> findByRoleLevel(Integer roleLevel);
    List<Role> findByRoleLevelLessThanEqual(Integer maxLevel);
    
    @Query("SELECT r FROM Role r WHERE r.name LIKE %:name% AND (:active IS NULL OR r.active = :active)")
    List<Role> findByNameContainingAndActive(@Param("name") String name, @Param("active") Boolean active);
    
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.role.id = :roleId AND ur.active = true")
    Long countActiveUsersByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT DISTINCT r FROM Role r JOIN r.userRoles ur WHERE ur.user.id = :userId AND ur.active = true")
    List<Role> findActiveRolesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Role r WHERE r.permissions LIKE %:permission% AND r.active = true")
    List<Role> findRolesWithPermission(@Param("permission") String permission);
}