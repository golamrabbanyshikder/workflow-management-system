package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.dto.DepartmentDto;
import com.workflow.workflowmanagementsystem.dto.DepartmentStats;
import com.workflow.workflowmanagementsystem.dto.RoleDto;
import com.workflow.workflowmanagementsystem.dto.RoleStats;
import com.workflow.workflowmanagementsystem.dto.SystemOverviewStats;
import com.workflow.workflowmanagementsystem.dto.TeamDto;
import com.workflow.workflowmanagementsystem.dto.TeamStats;
import com.workflow.workflowmanagementsystem.dto.UserDto;
import com.workflow.workflowmanagementsystem.entity.Department;
import com.workflow.workflowmanagementsystem.entity.Role;
import com.workflow.workflowmanagementsystem.entity.Team;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.service.DepartmentService;
import com.workflow.workflowmanagementsystem.service.RoleService;
import com.workflow.workflowmanagementsystem.service.TeamService;
import com.workflow.workflowmanagementsystem.service.UserService;
import com.workflow.workflowmanagementsystem.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRoleService userRoleService;

    // Department endpoints
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        List<DepartmentDto> departmentDtos = departments.stream()
                .map(this::convertToDepartmentDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(departmentDtos);
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        return department.map(dept -> ResponseEntity.ok(convertToDepartmentDto(dept)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/departments/{id}/teams")
    public ResponseEntity<List<TeamDto>> getTeamsByDepartment(@PathVariable Long id) {
        List<Team> teams = teamService.getTeamsByDepartment(id);
        List<TeamDto> teamDtos = teams.stream()
                .map(this::convertToTeamDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teamDtos);
    }

    // Team endpoints
    @GetMapping("/teams")
    public ResponseEntity<List<TeamDto>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        List<TeamDto> teamDtos = teams.stream()
                .map(this::convertToTeamDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teamDtos);
    }

    @GetMapping("/teams/active")
    public ResponseEntity<List<TeamDto>> getActiveTeams() {
        List<Team> teams = teamService.getActiveTeams();
        List<TeamDto> teamDtos = teams.stream()
                .map(this::convertToTeamDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teamDtos);
    }

    @GetMapping("/teams/{id}")
    public ResponseEntity<TeamDto> getTeamById(@PathVariable Long id) {
        Optional<Team> team = teamService.getTeamById(id);
        return team.map(t -> ResponseEntity.ok(convertToTeamDto(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/teams/{id}/members")
    public ResponseEntity<List<UserDto>> getTeamMembers(@PathVariable Long id) {
        Optional<Team> team = teamService.getTeamById(id);
        if (team.isPresent()) {
            List<User> members = team.get().getMembers();
            List<UserDto> userDtos = members.stream()
                    .map(this::convertToUserDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDtos);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/teams/search")
    public ResponseEntity<List<TeamDto>> searchTeams(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId) {
        List<Team> teams;
        if (name != null && !name.trim().isEmpty()) {
            teams = teamService.searchTeams(name, departmentId);
        } else if (departmentId != null) {
            teams = teamService.getTeamsByDepartment(departmentId);
        } else {
            teams = teamService.getAllTeams();
        }
        List<TeamDto> teamDtos = teams.stream()
                .map(this::convertToTeamDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teamDtos);
    }

    // Role endpoints
    @GetMapping("/roles")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        List<RoleDto> roleDtos = roles.stream()
                .map(this::convertToRoleDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roleDtos);
    }

    @GetMapping("/roles/active")
    public ResponseEntity<List<RoleDto>> getActiveRoles() {
        List<Role> roles = roleService.getActiveRoles();
        List<RoleDto> roleDtos = roles.stream()
                .map(this::convertToRoleDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roleDtos);
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long id) {
        Optional<Role> role = roleService.getRoleById(id);
        return role.map(r -> ResponseEntity.ok(convertToRoleDto(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/roles/{id}/users")
    public ResponseEntity<List<Long>> getUsersByRole(@PathVariable Long id) {
        List<Long> userIds = userRoleService.getUserIdsByRoleId(id);
        return ResponseEntity.ok(userIds);
    }

    @GetMapping("/roles/search")
    public ResponseEntity<List<RoleDto>> searchRoles(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active) {
        List<Role> roles = roleService.searchRoles(name, active);
        List<RoleDto> roleDtos = roles.stream()
                .map(this::convertToRoleDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roleDtos);
    }

    @GetMapping("/roles/permission/{permission}")
    public ResponseEntity<List<RoleDto>> getRolesWithPermission(@PathVariable String permission) {
        List<Role> roles = roleService.getRolesWithPermission(permission);
        List<RoleDto> roleDtos = roles.stream()
                .map(this::convertToRoleDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roleDtos);
    }

    // User endpoints
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/users/active")
    public ResponseEntity<List<UserDto>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(u -> ResponseEntity.ok(convertToUserDto(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String keyword) {
        List<User> users = userService.searchUsers(keyword);
        List<UserDto> userDtos = users.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/users/department/{departmentId}")
    public ResponseEntity<List<UserDto>> getUsersByDepartment(@PathVariable Long departmentId) {
        List<User> users = userService.getUsersByDepartment(departmentId);
        List<UserDto> userDtos = users.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/users/team/{teamId}")
    public ResponseEntity<List<UserDto>> getUsersByTeam(@PathVariable Long teamId) {
        List<User> users = userService.getUsersByTeam(teamId);
        List<UserDto> userDtos = users.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    // Statistics endpoints
    @GetMapping("/stats/overview")
    public ResponseEntity<SystemOverviewStats> getSystemOverview() {
        final long departmentCount = departmentService.getAllDepartments().size();
        final long teamCount = teamService.getAllTeams().size();
        final long roleCount = roleService.getAllRoles().size();
        final long userCount = userService.getAllUsers().size();
        final long activeTeamCount = teamService.getActiveTeams().size();
        final long activeRoleCount = roleService.getActiveRoles().size();
        final long activeUserCount = userService.getActiveUsers().size();

        SystemOverviewStats overview = new SystemOverviewStats(
                departmentCount, teamCount, roleCount, userCount,
                activeTeamCount, activeRoleCount, activeUserCount);

        return ResponseEntity.ok(overview);
    }

    @GetMapping("/stats/departments/{id}")
    public ResponseEntity<DepartmentStats> getDepartmentStats(@PathVariable Long id) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        if (!department.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        final List<Team> teams = teamService.getTeamsByDepartment(id);
        final long teamCount = teams.size();
        final long userCount = teams.stream()
                .mapToLong(team -> team.getMemberCount())
                .sum();

        DepartmentStats stats = new DepartmentStats(
                department.get().getName(), teamCount, userCount);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/teams/{id}")
    public ResponseEntity<TeamStats> getTeamStats(@PathVariable Long id) {
        Optional<Team> team = teamService.getTeamById(id);
        if (!team.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        final long memberCount = team.get().getMemberCount();
        final String departmentName = team.get().getDepartment() != null ? 
                team.get().getDepartment().getName() : "Unassigned";

        TeamStats stats = new TeamStats(
                team.get().getName(), departmentName, memberCount, team.get().isActive());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/roles/{id}")
    public ResponseEntity<RoleStats> getRoleStats(@PathVariable Long id) {
        Optional<Role> role = roleService.getRoleById(id);
        if (!role.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        final long userCount = roleService.getUserCount(id);
        final int permissionCount = role.get().getPermissions() != null ? 
                role.get().getPermissions().size() : 0;

        RoleStats stats = new RoleStats(
                role.get().getName(), userCount, permissionCount, 
                role.get().getRoleLevel(), role.get().isActive());

        return ResponseEntity.ok(stats);
    }
    
    // Helper methods to convert entities to DTOs
    
    private DepartmentDto convertToDepartmentDto(Department department) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setCreatedAt(department.getCreatedAt());
        return dto;
    }
    
    private TeamDto convertToTeamDto(Team team) {
        TeamDto dto = new TeamDto();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setDescription(team.getDescription());
        dto.setActive(team.isActive());
        dto.setDepartmentId(team.getDepartment() != null ? team.getDepartment().getId() : null);
        dto.setDepartmentName(team.getDepartment() != null ? team.getDepartment().getName() : null);
        dto.setMemberCount(team.getMemberCount());
        return dto;
    }
    
    private RoleDto convertToRoleDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setRoleLevel(role.getRoleLevel());
        dto.setActive(role.getActive());
        dto.setPermissions(role.getPermissions());
        dto.setUserCount(roleService.getUserCount(role.getId()));
        return dto;
    }
    
    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEnabled(user.isEnabled());
        dto.setTeamId(user.getTeam() != null ? user.getTeam().getId() : null);
        dto.setTeamName(user.getTeam() != null ? user.getTeam().getName() : null);
        dto.setTeamMemberCount(user.getTeam() != null ? user.getTeam().getMemberCount() : null);
        return dto;
    }
}