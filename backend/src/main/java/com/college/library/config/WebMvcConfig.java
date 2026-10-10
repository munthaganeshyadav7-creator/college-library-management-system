package com.college.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Find parent frontend directory path if running from backend folder
        File frontendDir = new File("../frontend");
        String frontendPath = frontendDir.exists() ? "file:" + frontendDir.getAbsolutePath() + "/" : "file:frontend/";

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", frontendPath)
                .setCachePeriod(0); // Disable caching in development for instant UI updates
    }
}
