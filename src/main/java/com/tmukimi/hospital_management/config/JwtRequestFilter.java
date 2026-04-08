package com.tmukimi.hospital_management.config;

import com.tmukimi.hospital_management.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * ইউজার যখনই কোনো API কল করে, তখন এই ক্লাসটি সবার আগে সক্রিয় হয়।
 * এটি টোকেন ভ্যালিডেট করে এবং ডাটাবেস কল ছাড়াই ইউজারকে অথেন্টিকেট করে।
 * Author: Mohiuddin Rahman Mukim
 */

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {



        ///  ------------------------------Web Socket Part for live queue tracking--------------------------------------


        String path = request.getRequestURI();

        if (path != null && path.startsWith("/ws-queue") || path.contains("/ws-queue/") || path.equals("/test.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        /// ---------------------------------- END --------------------------------------------




        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtUtil.isTokenValid(token)) {
                    Claims claims = jwtUtil.extractAllClaims(token);
                    String email = claims.getSubject();
                    String role = claims.get("role", String.class);

                    //Creating Authentication object without calling the db, In production, it will reduce the load in server
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            email,
                            null,          // no need for password bcz email is already verified
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);   // setting the user in securityContext
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();     // if token expired/invalid
            }
        }
        filterChain.doFilter(request, response);
    }
}