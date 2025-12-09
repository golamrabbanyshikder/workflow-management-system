package com.workflow.workflowmanagementsystem.controller;

import com.workflow.workflowmanagementsystem.dto.LoginDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "success", required = false) String success,
                            Model model) {
        
        model.addAttribute("loginDto", new LoginDto());
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        
        if (success != null) {
            model.addAttribute("success", "Registration successful! Please login.");
        }
        
        return "login";
    }
}