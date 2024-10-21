package com.example.mediumRoles.configs;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF
            .authorizeHttpRequests(authz -> authz
                // Permitimos acceso público a las rutas de autenticación
                .requestMatchers("/auth/**").permitAll()
                
                // Permitir solo ADMIN para POST, PUT, DELETE
                .requestMatchers(HttpMethod.POST, "/users/images/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/users/images/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/images/**").hasRole("ADMIN")
                
                // Permitir tanto ADMIN como USER para GET
                .requestMatchers(HttpMethod.GET, "/users/images/**").hasAnyRole("ADMIN", "USER")
                
                // Cualquier otra solicitud debe estar autenticada
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Sesión sin estado (stateless)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Añadimos el filtro JWT

        return http.build();
    }
    

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8005")); // Orígenes permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // Métodos permitidos
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));  // Headers permitidos

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); // Registrar la configuración de CORS para todas las rutas
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}