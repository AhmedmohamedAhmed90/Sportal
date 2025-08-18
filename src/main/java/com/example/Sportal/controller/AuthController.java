package com.example.Sportal.controller;



import com.example.Sportal.models.entities.User;
import com.example.Sportal.repository.UserRepository;
import com.example.Sportal.security.utils.jwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() { return "login"; }


    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpServletResponse response, Model model) {

        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            User user = userRepo.findByEmail(email).get();
            String token = jwtUtil.generateToken(user.getEmail() , user.getRole());

            Cookie cookie = new Cookie("JWT", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);

            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {
        if (userRepo.existsByEmail(email)) {
            model.addAttribute("error", "Email already exists");
            return "register";
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.STUDENT); // BY DEFAULT AnyOne Register is a student until admin change the role
        userRepo.save(user);

        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {

        String email = principal.getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);

        return "profile";
    }
}

