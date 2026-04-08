package com.tmukimi.hospital_management.config;

import com.tmukimi.hospital_management.services.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/v1/payments/**") // পেমেন্ট কলব্যাকগুলোতে CSRF চেক হবে না
                        .disable()
                )

                // 3. সেশন পলিসি Stateless করা
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {

                    if (!request.getRequestURI().contains("/api/v1/payments")) {
                        response.setContentType("application/json");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\": \"Unauthorized access - Invalid or missing token\"}");
                    }
                }))


                .authorizeHttpRequests(auth -> auth


                        .requestMatchers("/test.html", "/ws-queue/**", "/favicon.ico", "/error").permitAll()



                        .requestMatchers("/uploads/**").permitAll()



                        .requestMatchers("/api/v1/payments/*/receipt").authenticated()
                        .requestMatchers("/api/v1/payments/**").permitAll()




                        .requestMatchers("/api/v1/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/deactivate").hasRole("ADMIN")


                        .requestMatchers(HttpMethod.GET, "/api/v1/doctors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/doctors/me").hasRole("DOCTOR")


                        .requestMatchers(HttpMethod.GET, "/api/medicines/**").authenticated()




                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/dashboard/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/doctor/**").hasRole("DOCTOR")



                        .anyRequest().authenticated()
                )


                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }




    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration frontendConfig = new CorsConfiguration();
        frontendConfig.setAllowedOrigins(List.of("http://localhost:63342", "http://localhost:3000" ));
        frontendConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        frontendConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        frontendConfig.setExposedHeaders(List.of("Authorization"));
        frontendConfig.setAllowCredentials(true);


        CorsConfiguration paymentConfig = new CorsConfiguration();
        paymentConfig.setAllowedOriginPatterns(List.of("*"));
        paymentConfig.setAllowedMethods(Arrays.asList("POST", "GET", "OPTIONS"));
        paymentConfig.setAllowedHeaders(List.of("*"));
        paymentConfig.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration("/api/v1/payments/**", paymentConfig);


        source.registerCorsConfiguration("/**", frontendConfig);

        return source;
    }

}