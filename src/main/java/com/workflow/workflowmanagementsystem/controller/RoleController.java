package com.workflow.workflowmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.entity.Role;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.service.RoleService;
import com.workflow.workflowmanagementsystem.service.UserRoleService;
import com.workflow.workflowmanagementsystem.service.UserService;
import com.workflow.workflowmanagementsystem.util.RoleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admin/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listRoles(Model model) {
        model.addAttribute("roles", roleService.getAllRoles());
        model.addAttribute("availablePermissions", getAvailablePermissions());
        model.addAttribute("role", new Role()); // Add empty role object for the create modal form
        return "role/list";
    }
    
    @GetMapping("/active")
    public String listActiveRoles(Model model) {
        model.addAttribute("roles", roleService.getActiveRoles());
        model.addAttribute("availablePermissions", getAvailablePermissions());
        model.addAttribute("role", new Role()); // Add empty role object for create modal form
        return "role/list";
    }
    
    @GetMapping("/search")
    public String searchRoles(@RequestParam(required = false) String name,
                            @RequestParam(required = false) Boolean active,
                            @RequestParam(required = false) String sortBy,
                            @RequestParam(required = false) String roleType,
                            @RequestParam(required = false) String permission,
                            @RequestParam(required = false) Integer level,
                            Model model) {
        List<Role> roles;
        if (name != null && !name.trim().isEmpty()) {
            roles = roleService.searchRoles(name, active);
        } else if (active != null) {
            roles = roleService.getActiveRoles();
            if (!active) {
                roles = roleService.getAllRoles().stream()
                        .filter(role -> !role.isActive())
                        .collect(java.util.stream.Collectors.toList());
            }
        } else {
            roles = roleService.getAllRoles();
        }
        model.addAttribute("roles", roles);
        model.addAttribute("searchName", name);
        model.addAttribute("searchActive", active);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("searchType", roleType);
        model.addAttribute("selectedPermission", permission);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("availablePermissions", getAvailablePermissions());
        model.addAttribute("role", new Role()); // Add empty role object for create modal form
        return "role/list";
    }
    
    @GetMapping("/level/{level}")
    public String listRolesByLevel(@PathVariable Integer level, Model model) {
        model.addAttribute("roles", roleService.getRolesByLevelOrLower(level));
        model.addAttribute("selectedLevel", level);
        model.addAttribute("availablePermissions", getAvailablePermissions());
        model.addAttribute("role", new Role()); // Add empty role object for create modal form
        return "role/list";
    }
    
    @GetMapping("/permission/{permission}")
    public String listRolesByPermission(@PathVariable String permission, Model model) {
        model.addAttribute("roles", roleService.getRolesWithPermission(permission));
        model.addAttribute("selectedPermission", permission);
        model.addAttribute("availablePermissions", getAvailablePermissions());
        model.addAttribute("role", new Role()); // Add empty role object for create modal form
        return "role/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("role", new Role());
        model.addAttribute("availablePermissions", getAvailablePermissions());
        return "role/create";
    }

    @PostMapping("/create")
    public String createRole(@ModelAttribute Role role,
                           @RequestParam(required = false) List<String> permissions,
                           RedirectAttributes redirectAttributes) {
        try {
            if (permissions != null && !permissions.isEmpty()) {
                roleService.createRoleWithPermissions(role, permissions);
            } else {
                roleService.createRole(role);
            }
            redirectAttributes.addFlashAttribute("success", "Role created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        model.addAttribute("role", role);
        model.addAttribute("availablePermissions", getAvailablePermissions());
        return "role/edit";
    }

    @PostMapping("/update/{id}")
    public String updateRole(@PathVariable Long id,
                           @ModelAttribute Role role,
                           @RequestParam(required = false) List<String> permissions,
                           RedirectAttributes redirectAttributes) {
        try {
            roleService.updateRole(id, role);
            
            if (permissions != null) {
                roleService.updateRolePermissions(id, permissions);
            }
            
            redirectAttributes.addFlashAttribute("success", "Role updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }
    
    @PostMapping("/status/{id}")
    public String updateRoleStatus(@PathVariable Long id,
                                @RequestParam Boolean active,
                                RedirectAttributes redirectAttributes) {
        try {
            roleService.updateRoleStatus(id, active);
            String status = active ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", "Role " + status + " successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }
    
    @PostMapping("/add-permission/{id}")
    public String addPermissionToRole(@PathVariable Long id,
                                   @RequestParam String permission,
                                   RedirectAttributes redirectAttributes) {
        try {
            roleService.addPermissionToRole(id, permission);
            redirectAttributes.addFlashAttribute("success", "Permission added to role successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles/edit/" + id;
    }
    
    @PostMapping("/remove-permission/{id}")
    public String removePermissionFromRole(@PathVariable Long id,
                                      @RequestParam String permission,
                                      RedirectAttributes redirectAttributes) {
        try {
            roleService.removePermissionFromRole(id, permission);
            redirectAttributes.addFlashAttribute("success", "Permission removed from role successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles/edit/" + id;
    }
    
    @GetMapping("/view/{id}")
    public String viewRole(@PathVariable Long id, Model model) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        Long userCount = roleService.getUserCount(id);
        List<User> assignedUsers = userRoleService.getUserIdsByRoleId(id).stream()
                .map(userId -> userService.getUserById(userId).orElse(null))
                .filter(user -> user != null)
                .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("role", role);
        model.addAttribute("userCount", userCount);
        model.addAttribute("assignedUsers", assignedUsers);
        model.addAttribute("availablePermissions", getAvailablePermissions());
        return "role/view";
    }
    
    @GetMapping("/assign-users/{id}")
    public String showAssignUsersForm(@PathVariable Long id, Model model) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        List<User> availableUsers = userService.getActiveUsers();
        
        model.addAttribute("role", role);
        model.addAttribute("availableUsers", availableUsers);
        return "role/assign-users";
    }
    
    @PostMapping("/assign-user/{roleId}")
    public String assignUserToRole(@PathVariable Long roleId,
                                 @RequestParam Long userId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) Long teamId,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Get current user from security context
            User currentUser = RoleUtil.getCurrentUser(userRepository);
            userRoleService.assignRoleToUser(userId, roleId, departmentId, teamId, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "User assigned to role successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles/view/" + roleId;
    }
    
    @PostMapping("/remove-user/{roleId}")
    public String removeUserFromRole(@PathVariable Long roleId,
                                   @RequestParam Long userId,
                                   RedirectAttributes redirectAttributes) {
        try {
            userRoleService.removeRoleFromUser(userId, roleId);
            redirectAttributes.addFlashAttribute("success", "User removed from role successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles/view/" + roleId;
    }

    @GetMapping("/delete/{id}")
    public String deleteRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roleService.deleteRole(id);
            redirectAttributes.addFlashAttribute("success", "Role deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }
    
    @PostMapping("/initialize-default")
    public String initializeDefaultRoles(RedirectAttributes redirectAttributes) {
        try {
            roleService.initializeDefaultRoles();
            redirectAttributes.addFlashAttribute("success", "Default roles initialized successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }
    
    @GetMapping("/export/pdf")
    public void exportRolesToPDF(jakarta.servlet.http.HttpServletResponse response) throws IOException, DocumentException {
        List<Role> roles = roleService.getAllRoles();
        
        // Create PDF document
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("Role Management Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Add generation timestamp
        Font timestampFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Paragraph timestamp = new Paragraph("Generated on: " + sdf.format(new Date()), timestampFont);
        timestamp.setAlignment(Element.ALIGN_CENTER);
        timestamp.setSpacingAfter(20);
        document.add(timestamp);
        
        // Create table
        PdfPTable table = new PdfPTable(6); // ID, Name, Type, Level, Users, Status
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10f, 25f, 15f, 10f, 15f, 25f});
        
        // Add table headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        String[] headers = {"ID", "Role Name", "Type", "Level", "Users", "Status"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
        
        // Add table data
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        
        for (Role role : roles) {
            // ID
            PdfPCell idCell = new PdfPCell(new Phrase(String.valueOf(role.getId()), dataFont));
            idCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            idCell.setPadding(5);
            table.addCell(idCell);
            
            // Role Name
            PdfPCell nameCell = new PdfPCell(new Phrase(role.getName() != null ? role.getName() : "N/A", dataFont));
            nameCell.setPadding(5);
            table.addCell(nameCell);
            
            // Type
            String roleType = "User";
            if (role.getName() != null) {
                if (role.getName().contains("ADMIN") || role.getName().contains("CEO") || role.getName().contains("DEPARTMENT_HEAD")) {
                    roleType = "Admin";
                } else if (role.getName().contains("TEAM_LEAD")) {
                    roleType = "Lead";
                }
            }
            PdfPCell typeCell = new PdfPCell(new Phrase(roleType, dataFont));
            typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            typeCell.setPadding(5);
            table.addCell(typeCell);
            
            // Level
            PdfPCell levelCell = new PdfPCell(new Phrase(role.getRoleLevel() != null ? String.valueOf(role.getRoleLevel()) : "N/A", dataFont));
            levelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            levelCell.setPadding(5);
            table.addCell(levelCell);
            
            // Users (placeholder - would need actual count)
            PdfPCell usersCell = new PdfPCell(new Phrase("0", dataFont));
            usersCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            usersCell.setPadding(5);
            table.addCell(usersCell);
            
            // Status
            PdfPCell statusCell = new PdfPCell(new Phrase(role.isActive() != null && role.isActive() ? "Active" : "Inactive", dataFont));
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            statusCell.setPadding(5);
            table.addCell(statusCell);
        }
        
        document.add(table);
        document.close();
        
        // Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"roles-report.pdf\"");
        response.getOutputStream().write(outputStream.toByteArray());
    }
    
    private List<String> getAvailablePermissions() {
        return Arrays.asList(
            "USER_CREATE", "USER_READ", "USER_UPDATE", "USER_DELETE",
            "ROLE_CREATE", "ROLE_READ", "ROLE_UPDATE", "ROLE_DELETE",
            "DEPARTMENT_CREATE", "DEPARTMENT_READ", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE",
            "TEAM_CREATE", "TEAM_READ", "TEAM_UPDATE", "TEAM_DELETE", "TEAM_MANAGE",
            "WORKFLOW_CREATE", "WORKFLOW_READ", "WORKFLOW_UPDATE", "WORKFLOW_DELETE",
            "TASK_CREATE", "TASK_READ", "TASK_UPDATE", "TASK_DELETE", "TASK_MANAGE",
            "REPORT_READ", "SYSTEM_ADMIN"
        );
    }
}