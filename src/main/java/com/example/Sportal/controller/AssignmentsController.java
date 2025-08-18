package com.example.Sportal.controller;

import com.example.Sportal.models.dto.course.CourseDto;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import com.example.Sportal.mapper.GlobalMapper;
import com.example.Sportal.models.entities.Assignment;
import com.example.Sportal.service.AssignmentsService;
import com.example.Sportal.service.CoursesService;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path="/assignments")
public class AssignmentsController {
    private final AssignmentsService assignmentsService;
    private final CoursesService coursesService;
    private final GlobalMapper mapper;

    public AssignmentsController(AssignmentsService assignmentsService, CoursesService coursesService, GlobalMapper mapper) {
        this.assignmentsService = assignmentsService;
        this.coursesService = coursesService;
        this.mapper = mapper;
    }

    @GetMapping
    public String assignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status,
            Model model) {


        User currentUser = getCurrentUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Assignment> assignmentPage;

        // Apply filters based on parameters
        if (search != null && !search.trim().isEmpty()) {
            assignmentPage = assignmentsService.searchAssignments(search, pageable);
        } else if (courseId != null) {
            assignmentPage = assignmentsService.getAssignmentsByCourse(courseId, pageable);
        } else {
            assignmentPage = assignmentsService.getAllAssignments(pageable);
        }

        List<CourseDto> courses = coursesService.getAllCourses();

        long totalAssignments = assignmentsService.getTotalAssignmentsCount();
        long overdueAssignments = assignmentsService.getOverdueAssignmentsCount();
        long totalSubmissions = assignmentsService.getTotalSubmissionsCount();

        model.addAttribute("assignments", assignmentPage.getContent());
        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", assignmentPage.getTotalPages());
        model.addAttribute("totalElements", assignmentPage.getTotalElements());
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("user",currentUser);
        model.addAttribute("totalAssignments", totalAssignments);
        model.addAttribute("overdueAssignments", overdueAssignments);
        model.addAttribute("totalSubmissions", totalSubmissions);

        return "/assignments/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("assignment", new Assignment());
        model.addAttribute("courses", coursesService.getAllCourses());
        return "/assignments/form";
    }

    @PostMapping("/create")
    public String createAssignment(
            @Valid @ModelAttribute Assignment assignment,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", coursesService.getAllCourses());
            model.addAttribute("error", "Please correct the errors below.");
            return "/assignments/form";
        }

        try {
            Assignment savedAssignment = assignmentsService.createAssignment(assignment);
            redirectAttributes.addFlashAttribute("success",
                    "Assignment '" + savedAssignment.getTitle() + "' created successfully!");
            return "redirect:/assignments";
        } catch (Exception e) {
            model.addAttribute("courses", coursesService.getAllCourses());
            model.addAttribute("error", "Error creating assignment: " + e.getMessage());
            return "/assignments/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Assignment> assignmentOpt = assignmentsService.getAssignmentById(id);

        if (assignmentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Assignment not found.");
            return "redirect:/assignments";
        }

        model.addAttribute("assignment", assignmentOpt.get());
        model.addAttribute("courses", coursesService.getAllCourses());
        return "/assignments/form";
    }

    @PostMapping("/edit/{id}")
    public String updateAssignment(
            @PathVariable Long id,
            @Valid @ModelAttribute Assignment assignment,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", coursesService.getAllCourses());
            model.addAttribute("error", "Please correct the errors below.");
            return "/assignments/form";
        }

        try {
            assignment.setId(id);
            Assignment updatedAssignment = assignmentsService.updateAssignment(assignment);
            redirectAttributes.addFlashAttribute("success",
                    "Assignment '" + updatedAssignment.getTitle() + "' updated successfully!");
            return "redirect:/assignments";
        } catch (Exception e) {
            model.addAttribute("courses", coursesService.getAllCourses());
            model.addAttribute("error", "Error updating assignment: " + e.getMessage());
            return "/assignments/form";
        }
    }

    @GetMapping("/view/{id}")
    public String viewAssignment(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Assignment> assignmentOpt = assignmentsService.getAssignmentById(id);

        if (assignmentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Assignment not found.");
            return "redirect:/assignments";
        }

        Assignment assignment = assignmentOpt.get();
        model.addAttribute("assignment", assignment);

        long submissionCount = assignmentsService.getSubmissionCountForAssignment(id);
        model.addAttribute("submissionCount", submissionCount);

        return "/assignments/view";
    }

    // Delete assignment
    @PostMapping("/delete/{id}")
    public String deleteAssignment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Assignment> assignmentOpt = assignmentsService.getAssignmentById(id);

            if (assignmentOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Assignment not found.");
                return "redirect:/assignments";
            }

            String assignmentTitle = assignmentOpt.get().getTitle();
            assignmentsService.deleteAssignmentById(id);
            redirectAttributes.addFlashAttribute("success",
                    "Assignment '" + assignmentTitle + "' deleted successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error deleting assignment: " + e.getMessage());
        }

        return "redirect:/assignments";
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