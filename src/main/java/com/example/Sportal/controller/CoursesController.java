package com.example.Sportal.controller;

import com.example.Sportal.models.dto.course.CourseDto;
import com.example.Sportal.models.dto.course.CreateCourseRequest;
import com.example.Sportal.models.dto.course.UpdateCourseRequest;
import com.example.Sportal.models.entities.Course;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.repository.CourseRepository;
import com.example.Sportal.security.CustomUserDetails;
import com.example.Sportal.service.CoursesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/courses")
public class CoursesController {

    @Autowired
    private CoursesService coursesService;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping
    @Transactional
    public String listCourses(Model model) {
        try {
            User currentUser = getCurrentUser();
            System.err.println("Current userrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr: " + currentUser);
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            model.addAttribute("user", currentUser);
            
            List<CourseDto> courses = new ArrayList<>();
            
            try {
                if (currentUser.getRole() == User.Role.INSTRUCTOR) {
                    courses = coursesService.getCoursesByInstructor(currentUser);
                } else {
                    courses = coursesService.getVisibleCoursesForUser(currentUser);
                }
                System.out.println("Courses fetched: " + (courses != null ? courses.size() : "null"));
                System.out.println("Courses object: " + courses);
                
               
                if (courses == null) {
                    System.out.println("Service returned null, creating empty list");
                    courses = new ArrayList<>();
                }
            } catch (Exception e) {
                System.err.println("Error fetching courses: " + e.getMessage());
                e.printStackTrace();
                courses = new ArrayList<>();
            }
            
            System.out.println("Final courses to be added to model: " + (courses != null ? courses.size() : "null"));
            System.out.println("Final courses object: " + courses);
            
        
            
            model.addAttribute("courses", courses);
            return "courses/list";
            
        } catch (Exception e) {
            System.err.println("Error in listCourses: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("error", "Unable to load courses. Please try again.");
            model.addAttribute("courses", new ArrayList<>());
            return "courses/list";
        }
    }

    @GetMapping("/create")
    @Transactional
    public String createCourseForm(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.INSTRUCTOR) {
                return "redirect:/courses";
            }
            
            model.addAttribute("user", currentUser);
            model.addAttribute("createCourseRequest", new CreateCourseRequest());
            return "courses/create";
        } catch (Exception e) {
            System.err.println("Error in createCourseForm: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/courses";
        }
    }

    @PostMapping
    @Transactional
    public String createCourse(@Valid @ModelAttribute CreateCourseRequest createCourseRequest,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.INSTRUCTOR) {
                return "redirect:/courses";
            }
            
            if (bindingResult.hasErrors()) {
                model.addAttribute("user", currentUser);
                return "courses/create";
            }
            
            CourseDto course = coursesService.createCourse(createCourseRequest, currentUser);
            redirectAttributes.addFlashAttribute("success", "Course created successfully!");
            return "redirect:/courses/" + course.getId();
        } catch (Exception e) {
            System.err.println("Error creating course: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("user", getCurrentUser());
            model.addAttribute("error", e.getMessage());
            return "courses/create";
        }
    }

    @GetMapping("/{id}")
    @Transactional
    public String viewCourse(@PathVariable Long id, Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }

            model.addAttribute("user", currentUser);

            CourseDto course = coursesService.getCourseById(id);
            if (course == null) {
                model.addAttribute("error", "Course not found.");
                return "error";
            }

            model.addAttribute("course", course);

            boolean canAccess = course.getVisibility() == Course.Visibility.PUBLIC|| 
            currentUser.getRole() == User.Role.INSTRUCTOR;

            if (!canAccess) {
                boolean isEnrolled = coursesService.isStudentEnrolledInCourse(currentUser, id);

                if (!isEnrolled) {
                    model.addAttribute("error", "You don't have access to this course.");
                    return "error";
                }
            }

            if (currentUser.getRole() == User.Role.STUDENT) {
                boolean isEnrolled = coursesService.isStudentEnrolledInCourse(currentUser, id);
                model.addAttribute("isEnrolled", isEnrolled);
            }


            return "courses/detail";
        } catch (Exception e) {
            System.err.println("Error in courseDetail: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "An unexpected error occurred.");
            return "error";
        }
    }


    @GetMapping("/{id}/edit")
    @Transactional
    public String editCourseForm(@PathVariable Long id, Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.INSTRUCTOR) {
                return "redirect:/courses";
            }
            
            CourseDto course = coursesService.getCourseById(id);
//            if (!course.getInstructorId().equals(currentUser.getId())) {
//                return "redirect:/courses";
//            }
            
            model.addAttribute("user", currentUser);
            model.addAttribute("course", course);
            return "courses/edit";
        } catch (Exception e) {
            System.err.println("Error in editCourseForm: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/courses";
        }
    }

    @PostMapping("/{id}/edit")
    @Transactional
    public String updateCourse(@PathVariable Long id, 
                              @Valid @ModelAttribute UpdateCourseRequest updateCourseRequest,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.INSTRUCTOR) {
                return "redirect:/courses";
            }
            
            if (bindingResult.hasErrors()) {
                model.addAttribute("user", currentUser);
                return "courses/edit";
            }
            
            CourseDto course = coursesService.updateCourse(id, updateCourseRequest, currentUser);
            redirectAttributes.addFlashAttribute("success", "Course updated successfully!");
            return "redirect:/courses/" + course.getId();
        } catch (Exception e) {
            System.err.println("Error updating course: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("user", getCurrentUser());
            model.addAttribute("error", e.getMessage());
            return "courses/edit";
        }
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.INSTRUCTOR) {
                return "redirect:/courses";
            }
            
            coursesService.deleteCourse(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Course deleted successfully!");
            return "redirect:/courses";
        } catch (Exception e) {
            System.err.println("Error deleting course: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/courses";
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
