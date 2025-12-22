package com.workflow.workflowmanagementsystem.util;

import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.entity.UserRole;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Component
public class RoleUtil {
    
    /**
     * Extract user roles and store them in session for UI access
     * @param user The user entity
     * @param userRoleRepository UserRoleRepository to fetch roles
     * @param request HTTP request to store session attributes
     */
    public static void setUserRolesInSession(User user, UserRoleRepository userRoleRepository, HttpServletRequest request) {
        try {
            // Fetch user roles directly from repository to ensure they're loaded
            List<UserRole> userRoles = userRoleRepository.findByIdUserId(user.getId());
            
            // Extract role names and add to session for UI access
            Set<String> roleNames = userRoles.stream()
                    .filter(userRole -> userRole.isActive() == null || userRole.isActive())
                    .map(userRole -> {
                        String roleName = userRole.getRole().getName().toUpperCase();
                        // Remove ROLE_ prefix if present for cleaner checking
                        return roleName.startsWith("ROLE_") ? roleName.substring(5) : roleName;
                    })
                    .collect(Collectors.toSet());

            // Store roles in session for access across all templates
            request.getSession().setAttribute("userRoles", roleNames);
            request.getSession().setAttribute("userRole", roleNames);
            
            // Also update the user entity's roles to ensure consistency
            user.getUserRoles().clear();
            user.getUserRoles().addAll(userRoles);
            
            // Debug logging
            System.out.println("User ID: " + user.getId());
            System.out.println("UserRoles from DB: " + userRoles.size());
            System.out.println("Role names: " + roleNames);
            
        } catch (Exception e) {
            System.err.println("Error setting user roles in session: " + e.getMessage());
            e.printStackTrace();
            // Set empty roles as fallback
            request.getSession().setAttribute("userRoles", new HashSet<String>());
            request.getSession().setAttribute("userRole", new HashSet<String>());
        }
    }
    
    /**
     * Get current authenticated user from security context
     * @param userRepository UserRepository to fetch user details
     * @return Current authenticated user
     */
    public static User getCurrentUser(com.workflow.workflowmanagementsystem.Repository.UserRepository userRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        // Ensure user roles are loaded
        if (user.getUserRoles() != null) {
            user.getUserRoles().size(); // Initialize the collection
        }
        
        return user;
    }
}