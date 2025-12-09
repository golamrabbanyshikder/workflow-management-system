package com.workflow.workflowmanagementsystem.service;


import com.workflow.workflowmanagementsystem.Repository.DepartmentRepository;
import com.workflow.workflowmanagementsystem.Repository.TeamRepository;
import com.workflow.workflowmanagementsystem.entity.Department;
import com.workflow.workflowmanagementsystem.entity.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Optional<Team> getTeamById(Long id) {
        return teamRepository.findById(id);
    }

    public List<Team> getTeamsByDepartment(Long departmentId) {
        return teamRepository.findByDepartmentId(departmentId);
    }

    public Team createTeam(Team team) {
        if (teamRepository.existsByNameAndDepartmentId(team.getName(), team.getDepartment().getId())) {
            throw new RuntimeException("Team with name '" + team.getName() + "' already exists in this department");
        }
        return teamRepository.save(team);
    }

    public Team updateTeam(Long id, Team teamDetails) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));

        // Check if team name is being changed within the same department
        if (!team.getName().equals(teamDetails.getName()) &&
                teamRepository.existsByNameAndDepartmentId(teamDetails.getName(), team.getDepartment().getId())) {
            throw new RuntimeException("Team with name '" + teamDetails.getName() + "' already exists in this department");
        }

        team.setName(teamDetails.getName());
        team.setDescription(teamDetails.getDescription());

        // Update department if changed
        if (teamDetails.getDepartment() != null && !team.getDepartment().getId().equals(teamDetails.getDepartment().getId())) {
            Department department = departmentRepository.findById(teamDetails.getDepartment().getId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            team.setDepartment(department);
        }

        return teamRepository.save(team);
    }

    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
        teamRepository.delete(team);
    }
}