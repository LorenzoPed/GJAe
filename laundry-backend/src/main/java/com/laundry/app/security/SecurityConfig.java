package com.laundry.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security configuration for form login, role-based access and endpoint authorization.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Create security configuration with a custom user details service.
     * @param userDetailsService service used to load user details
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configure the security filter chain for HTTP requests.
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception when configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/javax.faces.resource/**"),
                    new AntPathRequestMatcher("/jakarta.faces.resource/**"),
                    new AntPathRequestMatcher("/resources/**")
                ).permitAll()

                .requestMatchers("/login.xhtml", "/login").permitAll()
                .requestMatchers("/register.xhtml", "/register").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                .requestMatchers("/manager-dashboard.xhtml").hasRole("MANAGER")
                .requestMatchers("/home.xhtml", "/my-bookings.xhtml", "/index.xhtml").authenticated()

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.xhtml")
                .defaultSuccessUrl("/index.xhtml", true)
                .failureUrl("/login.xhtml?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login.xhtml")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
