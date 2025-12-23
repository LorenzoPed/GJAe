package com.laundry.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Marks this class as a configuration source for Spring Beans
public class WebConfig implements WebMvcConfigurer {

    /**
     * Global CORS (Cross-Origin Resource Sharing) configuration.
     * * WHY IS THIS NEEDED?
     * By default, browsers block requests from a frontend running on one port (e.g., 5500)
     * to a backend running on another port (e.g., 8080).
     * This configuration tells the browser: "It's safe to let these two talk."
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 1. Apply these rules to ALL backend endpoints

                // 2. Allow requests from ANY origin (e.g., localhost:3000, localhost:5500, etc.)
                // This is crucial if developers use "Live Server" in VS Code.
                .allowedOriginPatterns("*")

                // 3. Allow standard HTTP methods used by our API
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                // 4. Allow all headers (needed for 'Authorization' header in JWT)
                .allowedHeaders("*")

                // 5. Allow credentials (cookies, authorization headers) to be sent
                .allowCredentials(true);
    }
}