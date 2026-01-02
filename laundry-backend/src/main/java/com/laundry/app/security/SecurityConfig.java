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

    private final CustomUserDetailsService userDetailsService;

    // Iniettiamo solo il service utente, niente filtri JWT
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. Risorse statiche di JSF (immagini, script, CSS di PrimeFaces)
                        .requestMatchers(new AntPathRequestMatcher("/jakarta.faces.resource/**")).permitAll()

                        // 2. Pagina di login deve essere pubblica
                        .requestMatchers(new AntPathRequestMatcher("/login.xhtml")).permitAll()

                        // 3. Tutto il resto richiede autenticazione
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.xhtml") // Pagina custom JSF
                        .defaultSuccessUrl("/index.xhtml", true) // Redirect forzato alla dashboard dopo login
                        .failureUrl("/login.xhtml?error=true") // Redirect in caso di errore
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login.xhtml")
                        .permitAll()
                )
                // Disabilitiamo CSRF per semplicità (JSF lo gestisce internamente in parte)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    // NOTA: PasswordEncoder è già definito in App.java o DataInitializer,
    // quindi non serve ridefinirlo qui per evitare conflitti di "Bean già esistente".
}
