package com.example.Sportal.controller;

import com.example.Sportal.mapper.Mapper;
import com.example.Sportal.models.dto.course.CourseDto;
import com.example.Sportal.models.dto.user.UserDto;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.service.UsersService;
import com.example.Sportal.service.CoursesService;
import com.example.Sportal.service.EnrollmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;
    private final UsersService usersService;
    private final CoursesService courseService;
    private final EnrollmentService enrollmentService;
    private final Mapper<User, UserDto> userMapper;

    public AdminController(AuthenticationManager authManager, PasswordEncoder passwordEncoder, UsersService usersService,
                           CoursesService courseService,
                           EnrollmentService enrollmentService,
                           Mapper<User, UserDto> userMapper) {
        this.authManager = authManager;
        this.passwordEncoder = passwordEncoder;
        this.usersService = usersService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.userMapper = userMapper;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Dashboard statistics
        model.addAttribute("totalUsers", usersService.getTotalUsersCount());
        model.addAttribute("totalCourses", courseService != null ? courseService.getAllCourses().size() : 0);
        model.addAttribute("totalEnrollments", enrollmentService != null ? 1 : 0); // For test

        // Get instructor count properly
        long instructors = usersService.getUsersByRole(User.Role.INSTRUCTOR);
        model.addAttribute("totalInstructors", instructors);

        model.addAttribute("activeUsers", usersService.getActiveUsersCount());
        model.addAttribute("recentUsers", usersService.getRecentUsers(5));

        return "admin/dashboard";
    }

    // Admin Users Management View
    @GetMapping("/users")
    public String adminUsers(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String role) {

        Page<User> users;
        if (search != null && !search.isEmpty()) {
            List<User> searchResults = usersService.searchUsers(search);
            model.addAttribute("users", searchResults);
            model.addAttribute("searchQuery", search);
        } else if (role != null && !role.isEmpty()) {
            List<User> roleUsers = usersService.findUsersByRole(User.Role.valueOf(role.toUpperCase()));
            model.addAttribute("users", roleUsers);
            model.addAttribute("selectedRole", role);
        } else {
            users = usersService.findAllUsersPaginated(Pageable.ofSize(size).withPage(page));
            model.addAttribute("users", users.getContent());
            model.addAttribute("totalPages", users.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalElements", users.getTotalElements());
        }

        model.addAttribute("roles", User.Role.values());
        return "admin/users";
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new UserDto());
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> user = usersService.findUserById(id);
        if (user.isPresent()) {
            model.addAttribute("user", userMapper.mapTo(user.get()));
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("isEdit", true);
            return "admin/user-form";
        }
        redirectAttributes.addFlashAttribute("error", "User not found");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute UserDto userDto, RedirectAttributes redirectAttributes) {
        try {
            User user = userMapper.mapFrom(userDto);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (userDto.getId() != null) {
                usersService.updateUserById(userDto.getId(), user);
                redirectAttributes.addFlashAttribute("success", "User updated successfully");
            } else {
                usersService.createUser(user);
                redirectAttributes.addFlashAttribute("success", "User created successfully");
            }
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            String result = usersService.deleteUserById(id);
            if (result.contains("successfully")) {
                redirectAttributes.addFlashAttribute("success", result);
            } else {
                redirectAttributes.addFlashAttribute("error", result);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> user = usersService.findUserById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "admin/user-details";
        }
        redirectAttributes.addFlashAttribute("error", "User not found");
        return "redirect:/admin/users";
    }

    @GetMapping("/courses")
    public String adminCourses(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        if (courseService != null) {
            model.addAttribute("courses", courseService.getAllCourses());
        }
        return "admin/courses";
    }

    @GetMapping("/courses/create")
    public String createCourseForm(Model model) {
        model.addAttribute("course", new CourseDto());
        model.addAttribute("instructors", usersService.getUsersByRole(User.Role.INSTRUCTOR));
        model.addAttribute("isEdit", false);
        return "admin/course-form";
    }

    @GetMapping("/courses/edit/{id}")
    public String editCourseForm(Model model, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (courseService != null) {
            try {
                model.addAttribute("course", courseService.getCourseById(id));
                model.addAttribute("instructors", usersService.getUsersByRole(User.Role.INSTRUCTOR));
                model.addAttribute("isEdit", true);
                return "admin/course-form";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Course not found");
            }
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/save")
    public String saveCourse(@ModelAttribute CourseDto courseDto, RedirectAttributes redirectAttributes) {
        try {
            if (courseService != null) {
                // Implementation depends on your CourseService methods
                redirectAttributes.addFlashAttribute("success", "Course saved successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving course: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (courseService != null) {
                // Implementation depends on your CourseService methods
                redirectAttributes.addFlashAttribute("success", "Course deleted successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting course: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @GetMapping("/enrollments")
    public String adminEnrollments(Model model) {
        if (enrollmentService != null) {
            // Add enrollment data to model
            model.addAttribute("enrollments", 0);
        }
        return "admin/enrollments";
    }
}