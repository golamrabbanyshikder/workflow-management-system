package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.dto.DepartmentStats;
import com.workflow.workflowmanagementsystem.dto.RoleStats;
import com.workflow.workflowmanagementsystem.dto.SystemOverviewStats;
import com.workflow.workflowmanagementsystem.dto.TeamStats;
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
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        return department.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/departments/{id}/teams")
    public ResponseEntity<List<Team>> getTeamsByDepartment(@PathVariable Long id) {
        List<Team> teams = teamService.getTeamsByDepartment(id);
        return ResponseEntity.ok(teams);
    }

    // Team endpoints
    @GetMapping("/teams")
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/teams/active")
    public ResponseEntity<List<Team>> getActiveTeams() {
        List<Team> teams = teamService.getActiveTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/teams/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        Optional<Team> team = teamService.getTeamById(id);
        return team.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/teams/{id}/members")
    public ResponseEntity<List<User>> getTeamMembers(@PathVariable Long id) {
        Optional<Team> team = teamService.getTeamById(id);
        if (team.isPresent()) {
            List<User> members = team.get().getMembers();
            return ResponseEntity.ok(members);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/teams/search")
    public ResponseEntity<List<Team>> searchTeams(
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
        return ResponseEntity.ok(teams);
    }

    // Role endpoints
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/roles/active")
    public ResponseEntity<List<Role>> getActiveRoles() {
        List<Role> roles = roleService.getActiveRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        Optional<Role> role = roleService.getRoleById(id);
        return role.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/roles/{id}/users")
    public ResponseEntity<List<Long>> getUsersByRole(@PathVariable Long id) {
        List<Long> userIds = userRoleService.getUserIdsByRoleId(id);
        return ResponseEntity.ok(userIds);
    }

    @GetMapping("/roles/search")
    public ResponseEntity<List<Role>> searchRoles(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active) {
        List<Role> roles = roleService.searchRoles(name, active);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/roles/permission/{permission}")
    public ResponseEntity<List<Role>> getRolesWithPermission(@PathVariable String permission) {
        List<Role> roles = roleService.getRolesWithPermission(permission);
        return ResponseEntity.ok(roles);
    }

    // User endpoints
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String keyword) {
        List<User> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/department/{departmentId}")
    public ResponseEntity<List<User>> getUsersByDepartment(@PathVariable Long departmentId) {
        List<User> users = userService.getUsersByDepartment(departmentId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/team/{teamId}")
    public ResponseEntity<List<User>> getUsersByTeam(@PathVariable Long teamId) {
        List<User> users = userService.getUsersByTeam(teamId);
        return ResponseEntity.ok(users);
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
}