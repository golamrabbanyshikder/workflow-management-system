package com.workflow.workflowmanagementsystem.controller;



import com.workflow.workflowmanagementsystem.Repository.DepartmentRepository;
import com.workflow.workflowmanagementsystem.Repository.RoleRepository;
import com.workflow.workflowmanagementsystem.Repository.TeamRepository;
import com.workflow.workflowmanagementsystem.Repository.UserRepository;
import com.workflow.workflowmanagementsystem.dto.LoginDto;
import com.workflow.workflowmanagementsystem.dto.RegistrationDto;
import com.workflow.workflowmanagementsystem.entity.User;
import com.workflow.workflowmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {


    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    TeamRepository teamRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationDto());
        model.addAttribute("departments", departmentRepository.findAll());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") RegistrationDto registrationDto,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentRepository.findAll());
            return "register";
        }

        try {
            userService.registerUser(registrationDto);
            return "redirect:/login?success=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("departments", departmentRepository.findAll());
            return "register";
        }
    }

}
