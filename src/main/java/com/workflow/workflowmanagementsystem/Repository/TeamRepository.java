package com.workflow.workflowmanagementsystem.Repository;


import com.workflow.workflowmanagementsystem.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByDepartmentId(Long departmentId);
    boolean existsByNameAndDepartmentId(String name, Long departmentId);
    List<Team> findByDepartmentName(String departmentName);
    java.util.Optional<Team> findByName(String name);
}
