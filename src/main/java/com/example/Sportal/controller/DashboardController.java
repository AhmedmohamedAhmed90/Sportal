package com.example.Sportal.controller;

import com.example.Sportal.models.dto.course.CourseDto;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.security.CustomUserDetails;
import com.example.Sportal.service.CoursesService;
import com.example.Sportal.service.EnrollmentService;
import com.example.Sportal.service.MaterialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private CoursesService coursesService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private MaterialsService materialsService;

    @GetMapping
    public String dashboard(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            model.addAttribute("user", currentUser);
            
            List<CourseDto> recentCourses = new ArrayList<>();
            int courseCount = 0;
            int totalStudents = 0;
            int enrollmentCount = 0;
            int materialCount = 0;
            
            try {
                if (currentUser.getRole() == User.Role.INSTRUCTOR) {
                    recentCourses = coursesService.getCoursesByInstructor(currentUser);
                    courseCount = recentCourses.size();
                    
                    for (CourseDto course : recentCourses) {
                        try {
                            List<com.example.Sportal.models.entities.Enrollment> enrollments = 
                                enrollmentService.getEnrollmentsByCourse(course.getId());
                            enrollmentCount += enrollments.size();
                        } catch (Exception e) {
                            System.err.println("Error getting enrollments for course " + course.getId() + ": " + e.getMessage());
                        }
                    }
                    
                    for (CourseDto course : recentCourses) {
                        try {
                            List<com.example.Sportal.models.dto.material.MaterialDto> materials = 
                                materialsService.getMaterialsByCourse(course.getId(), currentUser);
                            materialCount += materials.size();
                        } catch (Exception e) {
                            System.err.println("Error getting materials for course " + course.getId() + ": " + e.getMessage());
                        }
                    }
                    
                } else {
                    recentCourses = coursesService.getEnrolledCoursesForStudent(currentUser);
                    courseCount = recentCourses.size();
                    enrollmentCount = courseCount; 
                    
                    for (CourseDto course : recentCourses) {
                        try {
                            List<com.example.Sportal.models.dto.material.MaterialDto> materials = 
                                materialsService.getMaterialsByCourse(course.getId(), currentUser);
                            materialCount += materials.size();
                        } catch (Exception e) {
                            System.err.println("Error getting materials for course " + course.getId() + ": " + e.getMessage());
                        }
                    }
                }
                
                if (recentCourses.size() > 5) {
                    recentCourses = recentCourses.subList(0, 5);
                }
                
            } catch (Exception e) {
                System.err.println("Error fetching dashboard data: " + e.getMessage());
                e.printStackTrace();
            }
            
            model.addAttribute("recentCourses", recentCourses);
            model.addAttribute("courseCount", courseCount);
            model.addAttribute("totalStudents", totalStudents);
            model.addAttribute("enrollmentCount", enrollmentCount);
            model.addAttribute("materialCount", materialCount);
            
            return "dashboard";
            
        } catch (Exception e) {
            System.err.println("Dashboard error: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("error", "Unable to load dashboard data: " + e.getMessage());
            model.addAttribute("recentCourses", new ArrayList<>());
            model.addAttribute("courseCount", 0);
            model.addAttribute("totalStudents", 0);
            model.addAttribute("enrollmentCount", 0);
            model.addAttribute("materialCount", 0);
            
            return "dashboard";
        }
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("=== Dashboard getCurrentUser Debug ===");
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
