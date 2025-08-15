package com.example.Sportal.controller;

import com.example.Sportal.models.entities.Enrollment;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.security.CustomUserDetails;
import com.example.Sportal.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping
    public String listEnrollments(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.STUDENT) {
                return "redirect:/courses";
            }
            
            model.addAttribute("user", currentUser);
            
            List<Enrollment> enrollments = new ArrayList<>();
            try {
                enrollments = enrollmentService.getEnrollmentsByStudent(currentUser);
            } catch (Exception e) {
                System.err.println("Error fetching enrollments: " + e.getMessage());
                e.printStackTrace();
            }
            
            model.addAttribute("enrollments", enrollments);
            return "enrollments/list";
            
        } catch (Exception e) {
            System.err.println("Error in listEnrollments: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Unable to load enrollments. Please try again.");
            model.addAttribute("enrollments", new ArrayList<>());
            return "enrollments/list";
        }
    }

    @PostMapping("/enroll/{courseId}")
    public String enrollInCourse(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.STUDENT) {
                redirectAttributes.addFlashAttribute("error", "Only students can enroll in courses.");
                return "redirect:/courses";
            }
            
            enrollmentService.enrollStudent(currentUser, courseId);
            redirectAttributes.addFlashAttribute("success", "Successfully enrolled in course!");
            return "redirect:/courses/" + courseId;
            
        } catch (Exception e) {
            System.err.println("Error enrolling in course: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/courses/" + courseId;
        }
    }

    @PostMapping("/drop/{courseId}")
    public String dropCourse(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.STUDENT) {
                redirectAttributes.addFlashAttribute("error", "Only students can drop courses.");
                return "redirect:/enrollments";
            }
            
            enrollmentService.dropCourse(currentUser, courseId);
            redirectAttributes.addFlashAttribute("success", "Successfully dropped course!");
            return "redirect:/enrollments";
            
        } catch (Exception e) {
            System.err.println("Error dropping course: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/enrollments";
        }
    }

    @PostMapping("/{enrollmentId}/status")
    public String updateEnrollmentStatus(
            @PathVariable Long enrollmentId,
            @RequestParam Enrollment.Status status,
            RedirectAttributes redirectAttributes) {
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.INSTRUCTOR) {
                redirectAttributes.addFlashAttribute("error", "Only instructors can update enrollment status.");
                return "redirect:/courses";
            }
            
            enrollmentService.updateEnrollmentStatus(enrollmentId, status, currentUser);
            redirectAttributes.addFlashAttribute("success", "Enrollment status updated successfully!");
            return "redirect:/courses";
            
        } catch (Exception e) {
            System.err.println("Error updating enrollment status: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/courses";
        }
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && 
                authentication.getPrincipal() instanceof CustomUserDetails && 
                !"anonymousUser".equals(authentication.getPrincipal()) &&
                authentication.isAuthenticated()) {
                
                return ((CustomUserDetails) authentication.getPrincipal()).getUser();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
