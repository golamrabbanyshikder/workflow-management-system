package com.workflow.workflowmanagementsystem.service;

import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true, // accountNonExpired, credentialsNonExpired, accountNonLocked
                getAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        user.getUserRoles().stream()
                .filter(userRole -> userRole.isActive() == null || userRole.isActive())
                .forEach(userRole -> {
                    String roleName = userRole.getRole().getName();
                    
                    // Always add the role name as-is (for backward compatibility)
                    authorities.add(new SimpleGrantedAuthority(roleName));
                    
                    // Also add with ROLE_ prefix if not present (for Spring Security)
                    if (!roleName.startsWith("ROLE_")) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                    } else {
                        // Also add without ROLE_ prefix for UI compatibility
                        authorities.add(new SimpleGrantedAuthority(roleName.substring(5)));
                    }
                });
        
        return authorities;
    }
}