package com.laundry.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

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
                .requestMatchers("/h2-console/**").permitAll()

                .requestMatchers("/manager-dashboard.xhtml").hasRole("MANAGER")
                .requestMatchers("/home.xhtml", "/my-bookings.xhtml", "/index.xhtml").authenticated()

                .requestMatchers(HttpMethod.GET, "/api/machines/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/machines/**").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/machines/**").hasRole("MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/machines/**").hasRole("MANAGER")

                .requestMatchers(HttpMethod.POST, "/api/bookings").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/bookings/my").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/bookings").hasRole("MANAGER")

                .requestMatchers("/users/**").hasRole("MANAGER")

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
