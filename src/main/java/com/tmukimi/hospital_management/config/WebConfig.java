package com.tmukimi.hospital_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // http://localhost:8080/uploads/profiles/filename.jpg হিট করলে ফোল্ডার থেকে ফাইল দেখাবে
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}