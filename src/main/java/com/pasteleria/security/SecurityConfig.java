package com.pasteleria.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    // =======================
    // CORS (React Vite)
    // =======================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Frontend
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // Métodos permitidos
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Headers permitidos
        config.setAllowedHeaders(List.of("*"));

        // Para JWT en Authorization
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    // =======================
    // Security Filter Chain
    // =======================
    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        // CORS + CSRF
        .cors(cors -> {})
        .csrf(csrf -> csrf.disable())

        // Stateless JWT
        .sessionManagement(sm ->
            sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )

        // Manejo de errores (CLAVE)
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(
                (request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"No autorizado\"}");
                }
            )
        )

        .authorizeHttpRequests(auth -> auth

            // ⛔ MUY IMPORTANTE
            .requestMatchers("/error").permitAll()

            // Preflight
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

            // Auth público
            .requestMatchers("/api/v1/auth/**").permitAll()

            // Swagger
            .requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
            ).permitAll()

            // Tienda pública
            .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/categorias/**").permitAll()

            // Todo lo demás
            .anyRequest().authenticated()
        )

        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

}
