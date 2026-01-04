package com.laundry.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Note: If you don't explicitly use userDetailsService here, Spring Boot
    // will still find the @Bean from your service class automatically.
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. Allow static resources (CSS, JS, Images) for PrimeFaces/JoinFaces
                        // It is safer to include javax.faces and resources to avoid login page styling issues
                        .requestMatchers(
                                new AntPathRequestMatcher("/javax.faces.resource/**"),
                                new AntPathRequestMatcher("/jakarta.faces.resource/**"),
                                new AntPathRequestMatcher("/resources/**")
                        ).permitAll()

                        // 2. Allow Login Page
                        .requestMatchers("/login.xhtml", "/login").permitAll()

                        // 3. Manager Pages
                        .requestMatchers("/manager-dashboard.xhtml").hasRole("MANAGER")

                        // 4. UPDATED: User Pages (Replaces user-booking.xhtml)
                        // We allow both USER and MANAGER to see the calendar/home if needed,
                        // or restrict to USER only.
                        .requestMatchers("/home.xhtml", "/my-bookings.xhtml").authenticated()

                        // 5. Catch-all for any other request
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.xhtml")
                        // We keep index.xhtml as the landing page.
                        // Ensure your NavigationView redirects to 'home.xhtml' instead of 'user-booking.xhtml'
                        .defaultSuccessUrl("/index.xhtml", true)
                        .failureUrl("/login.xhtml?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login.xhtml")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // Disable CSRF for JSF compatibility
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}