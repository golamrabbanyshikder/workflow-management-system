package com.workflow.workflowmanagementsystem.controller;


import com.workflow.workflowmanagementsystem.Repository.TeamRepository;
import com.workflow.workflowmanagementsystem.entity.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private TeamRepository teamRepository;

    @GetMapping("/teams")
    public List<Team> getTeamsByDepartment(@RequestParam Long departmentId) {
        return teamRepository.findByDepartmentId(departmentId);
    }
}