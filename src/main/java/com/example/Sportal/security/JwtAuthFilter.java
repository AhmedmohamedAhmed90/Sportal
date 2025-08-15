package com.example.Sportal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.Sportal.security.utils.jwtUtil;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = null;
        String authHeader = request.getHeader("Authorization");

        System.out.println("=== JWT Filter Debug ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request method: " + request.getMethod());
        System.out.println("Cookies: " + (request.getCookies() != null ? Arrays.toString(request.getCookies()) : "null"));

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("Token from Authorization header: " + token.substring(0, Math.min(20, token.length())) + "...");
        } else if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> "JWT".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
            if (token != null) {
                System.out.println("Token from cookie: " + token.substring(0, Math.min(20, token.length())) + "...");
            }
        }

        System.out.println("JWT Token found: " + (token != null ? "YES" : "NO"));
        System.out.println("Current authentication: " + SecurityContextHolder.getContext().getAuthentication());

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String email = jwtUtil.extractEmail(token);
                System.out.println("Extracted email: " + email);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println("UserDetails loaded: " + userDetails.getUsername());

                if (jwtUtil.validateToken(token)) {
                    System.out.println("JWT token is valid");
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Authentication set in SecurityContextHolder");
                } else {
                    System.out.println("JWT token is invalid");
                }
            } catch (Exception e) {
                System.err.println("Error in JWT filter: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (token == null) {
            System.out.println("No JWT token found in request");
        } else {
            System.out.println("Authentication already exists, skipping JWT processing");
        }

        System.out.println("Final authentication: " + SecurityContextHolder.getContext().getAuthentication());
        System.out.println("=== End JWT Filter Debug ===");

        chain.doFilter(request, response);
    }
}
