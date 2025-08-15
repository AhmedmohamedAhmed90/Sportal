package com.example.Sportal.controller;

import com.example.Sportal.models.entities.User;
import com.example.Sportal.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }
    
    @GetMapping("/home")
    public String homePage() {
        return "home";
    }
    
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "Test endpoint working!";
    }
    
    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "Application is running!";
    }
    
    @GetMapping("/auth-test")
    @ResponseBody
    public String authTest() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && 
                authentication.getPrincipal() instanceof CustomUserDetails && 
                !"anonymousUser".equals(authentication.getPrincipal()) &&
                authentication.isAuthenticated()) {
                
                User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
                return "Authenticated as: " + user.getName() + " (Role: " + user.getRole() + ")";
            } else {
                return "Not authenticated. Principal: " + (authentication != null ? authentication.getPrincipal() : "null");
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
