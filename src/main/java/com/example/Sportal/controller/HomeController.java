package com.example.Sportal.controller;

import com.example.Sportal.models.entities.User;
import com.example.Sportal.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
   User currentUser = getCurrentUser(); 
    if (currentUser != null) {
        model.addAttribute("user", currentUser);
    }
    return "home";
}
    
    @GetMapping("/home")
    public String homePage(Model model, Authentication authentication) {
        User currentUser = getCurrentUser(); 
    if (currentUser != null) {
        model.addAttribute("user", currentUser);
    }
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

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("=== getCurrentUser Debug ===");
            System.out.println("Authentication: " + authentication);
            
            if (authentication != null) {
                System.out.println("Principal: " + authentication.getPrincipal());
                System.out.println("Principal type: " + authentication.getPrincipal().getClass().getName());
                System.out.println("Is authenticated: " + authentication.isAuthenticated());
                System.out.println("Is anonymous: " + "anonymousUser".equals(authentication.getPrincipal()));
            }
            
            if (authentication != null && 
                authentication.getPrincipal() instanceof CustomUserDetails && 
                !"anonymousUser".equals(authentication.getPrincipal()) &&
                authentication.isAuthenticated()) {
                
                User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
                System.out.println("User retrieved: " + user.getName() + " (Role: " + user.getRole() + ")");
                return user;
            }
            
            System.out.println("No valid authentication found - returning null");
            return null;
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
