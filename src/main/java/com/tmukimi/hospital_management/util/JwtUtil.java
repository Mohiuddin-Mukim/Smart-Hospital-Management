package com.tmukimi.hospital_management.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // আধুনিক JJWT-তে String secret এর বদলে Key অবজেক্ট লাগে
    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // =====================
    // TOKEN GENERATION
    // =====================
    public String generateToken(String email, Long userId, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role) // এখানে সরাসরি রোল সেভ করছি
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                // signWith(Key) সরাসরি ব্যবহার করা এখনকার স্ট্যান্ডার্ড
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =====================
    // TOKEN VALIDATION
    // =====================
    public boolean isTokenValid(String token) {
        try {
            // টোকেনটি পার্স করতে পারলে এবং এক্সপায়ার না হলে এটি ভ্যালিড
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            // টোকেন টেম্পারড হলে বা এক্সপায়ার হলে এখানে এক্সেপশন আসবে
            return false;
        }
    }

    // ==========================================
    // নতুন মেথড: রিকোয়েস্ট থেকে সরাসরি ইউজার আইডি বের করা
    // ==========================================
    public Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return extractUserId(token);
        }
        throw new RuntimeException("JWT Token is missing or invalid");
    }

    // =====================
    // CLAIM EXTRACTION
    // =====================
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}