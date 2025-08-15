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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    private final UsersService usersService;
    private final CoursesService courseService;
    private final EnrollmentService enrollmentService;
    private final Mapper<User, UserDto> userMapper;

    public AdminController(UsersService usersService,
                           CoursesService courseService,
                           EnrollmentService enrollmentService,
                           Mapper<User, UserDto> userMapper) {
        this.usersService = usersService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.userMapper = userMapper;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("totalUsers", usersService.getTotalUsersCount());
//        model.addAttribute("totalCourses", courseService != null ? courseService.getTotalCoursesCount() : 0);
//        model.addAttribute("totalEnrollments", enrollmentService != null ? enrollmentService.getTotalEnrollmentsCount() : 0);
        model.addAttribute("totalInstructors", usersService.getUsersByRole(User.Role.INSTRUCTOR));
        model.addAttribute("activeUsers", usersService.getActiveUsersCount());

        model.addAttribute("recentUsers", usersService.getRecentUsers(5));

        return "admin/dashboard";
    }

    // Admin Users Management View
    @GetMapping("/admin/users")
    public String adminUsers(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String role) {

        Page<User> users;
        if (search != null && !search.isEmpty()) {
            List<User> searchResults = usersService.searchUsers(search);
            model.addAttribute("users", searchResults);
        } else if (role != null && !role.isEmpty()) {
            List<User> roleUsers = usersService.findUsersByRole(User.Role.valueOf(role.toUpperCase()));
            model.addAttribute("users", roleUsers);
        } else {
            users = usersService.findAllUsersPaginated(Pageable.ofSize(size).withPage(page));
            model.addAttribute("users", users.getContent());
            model.addAttribute("totalPages", users.getTotalPages());
            model.addAttribute("currentPage", page);
        }

        model.addAttribute("roles", User.Role.values());
        return "admin/users";
    }

    @GetMapping("/admin/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new UserDto());
        model.addAttribute("roles", User.Role.values());
        return "admin/user-form";
    }

    @GetMapping("/admin/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        Optional<User> user = usersService.findUserById(id);
        if (user.isPresent()) {
            model.addAttribute("user", userMapper.mapTo(user.get()));
            model.addAttribute("roles", User.Role.values());
            return "admin/user-form";
        }
        return "redirect:/admin/users?error=User not found";
    }

    @PostMapping("/admin/users/save")
    public String saveUser(@ModelAttribute UserDto userDto) {
        try {
            User user = userMapper.mapFrom(userDto);
            if (userDto.getId() != null) {
                usersService.updateUserById(userDto.getId(), user);
            } else {
                usersService.createUser(user);
            }
            return "redirect:/admin/users?success=User saved successfully";
        } catch (Exception e) {
            return "redirect:/admin/users?error=" + e.getMessage();
        }
    }

    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        String result = usersService.deleteUserById(id);
        return "redirect:/admin/users?message=" + result;
    }

    @GetMapping("/admin/courses")
    public String adminCourse(Model model){
        model.addAttribute("courses",courseService.getAllCourses());
        return "admin/courses";
    }

    @GetMapping("/admin/courses/create")
    public String createCourse(Model model){
        model.addAttribute("course", new CourseDto());
        return "admin/course-form";
    }

    @GetMapping("/admin/courses/edit/{id}")
    public String editCourse(Model model,@PathVariable Long id){
        model.addAttribute("course",courseService.getCourseById(id));
        return "admin/course-form";
    }
}
