package com.example.Sportal.security.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import com.example.Sportal.models.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

@Component
public class jwtUtil {

    private static final String SECRET = "Hazem--MIU-2025-EGYPT-TRIALKEY-12342342355345346547";


    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    // Increased expiration time to 24 hours (86400 seconds)
    private static final long EXPIRATION_TIME = 86400;

    public static String generateToken(String email, User.Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME * 1000))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }


    public static boolean validateToken(String token ) {
        if (token == null || token.trim().isEmpty()) {
            System.err.println("JWT validation error: Token is null or empty");
            return false;
        }

        try {
            // This will throw an exception if the token is invalid or expired
            Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                System.err.println("JWT validation error: Token has expired");
                return false;
            }
            
            System.out.println("JWT validation success for user: " + claims.getSubject());
            return true;
        } catch (Exception e) {
            System.err.println("JWT validation error: " + e.getMessage());
            return false;
        }
    }

    public static String extractEmail(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is null or empty");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Token does not have expected format (3 parts): " + parts.length);
        }

        return extractClaims(token).getSubject();
    }

    private static boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    private static Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    public static Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
