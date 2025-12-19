package com.workflow.workflowmanagementsystem.service;

import com.workflow.workflowmanagementsystem.Repository.DepartmentRepository;
import com.workflow.workflowmanagementsystem.Repository.TeamRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.entity.Department;
import com.workflow.workflowmanagementsystem.entity.Team;
import com.workflow.workflowmanagementsystem.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
    
    public List<Team> getActiveTeams() {
        return teamRepository.findByActive(true);
    }

    public Optional<Team> getTeamById(Long id) {
        return teamRepository.findById(id);
    }

    public List<Team> getTeamsByDepartment(Long departmentId) {
        return teamRepository.findByDepartmentId(departmentId);
    }
    
    public List<Team> getActiveTeamsByDepartment(Long departmentId) {
        return teamRepository.findByDepartmentIdAndActive(departmentId, true);
    }
    
    public List<Team> searchTeams(String name, Long departmentId) {
        return teamRepository.findByNameContainingAndDepartmentId(name, departmentId);
    }
    
    public List<Team> getTeamsByTeamLead(Long teamLeadId) {
        return teamRepository.findByTeamLeadId(teamLeadId);
    }
    
    public List<Team> getTeamsByUserId(Long userId) {
        return teamRepository.findTeamsByUserId(userId);
    }
    
    public Long getMemberCount(Long teamId) {
        return teamRepository.countMembersByTeamId(teamId);
    }

    public Team createTeam(Team team) {
        if (teamRepository.existsByNameAndDepartmentId(team.getName(), team.getDepartment().getId())) {
            throw new RuntimeException("Team with name '" + team.getName() + "' already exists in this department");
        }
        team.setActive(true);
        team.setCreatedAt(LocalDateTime.now());
        return teamRepository.save(team);
    }
    
    public Team createTeamWithLead(Team team, Long teamLeadId) {
        if (teamRepository.existsByNameAndDepartmentId(team.getName(), team.getDepartment().getId())) {
            throw new RuntimeException("Team with name '" + team.getName() + "' already exists in this department");
        }
        
        if (teamLeadId != null) {
            User teamLead = userRepository.findById(teamLeadId)
                    .orElseThrow(() -> new RuntimeException("Team lead not found with id: " + teamLeadId));
            team.setTeamLeadId(teamLeadId);
        }
        
        team.setActive(true);
        team.setCreatedAt(LocalDateTime.now());
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
        team.setUpdatedAt(LocalDateTime.now());

        // Update department if changed
        if (teamDetails.getDepartment() != null && !team.getDepartment().getId().equals(teamDetails.getDepartment().getId())) {
            Department department = departmentRepository.findById(teamDetails.getDepartment().getId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            team.setDepartment(department);
        }
        
        // Update team lead if changed
        if (teamDetails.getTeamLeadId() != null && !teamDetails.getTeamLeadId().equals(team.getTeamLeadId())) {
            User teamLead = userRepository.findById(teamDetails.getTeamLeadId())
                    .orElseThrow(() -> new RuntimeException("Team lead not found with id: " + teamDetails.getTeamLeadId()));
            team.setTeamLeadId(teamDetails.getTeamLeadId());
        }

        return teamRepository.save(team);
    }
    
    public Team updateTeamStatus(Long id, Boolean active) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
        team.setActive(active);
        team.setUpdatedAt(LocalDateTime.now());
        return teamRepository.save(team);
    }
    
    public Team assignTeamLead(Long teamId, Long teamLeadId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        
        User teamLead = userRepository.findById(teamLeadId)
                .orElseThrow(() -> new RuntimeException("Team lead not found with id: " + teamLeadId));
        
        team.setTeamLeadId(teamLeadId);
        team.setUpdatedAt(LocalDateTime.now());
        return teamRepository.save(team);
    }
    
    public Team addMemberToTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (user.getTeam() != null && user.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("User is already a member of this team");
        }
        
        team.addMember(user);
        team.setUpdatedAt(LocalDateTime.now());
        return teamRepository.save(team);
    }
    
    public Team removeMemberFromTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (user.getTeam() == null || !user.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("User is not a member of this team");
        }
        
        team.removeMember(user);
        team.setUpdatedAt(LocalDateTime.now());
        return teamRepository.save(team);
    }

    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
        
        // Remove all members from the team
        for (User member : team.getMembers()) {
            member.setTeam(null);
        }
        
        teamRepository.delete(team);
    }
}