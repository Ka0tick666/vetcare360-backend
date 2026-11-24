
package com.vetcare360.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // Solicitudes CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Health Check publico
                .requestMatchers("/api/health", "/health").permitAll()

                // Productos
                .requestMatchers(HttpMethod.GET,
                    "/api/productos", "/api/productos/**")
                    .hasAnyRole("Admin", "Veterinario", "Cliente")

                .requestMatchers(HttpMethod.POST,
                    "/api/productos", "/api/productos/**")
                    .hasAnyRole("Admin", "Veterinario")

                .requestMatchers(HttpMethod.PUT,
                    "/api/productos/**")
                    .hasAnyRole("Admin", "Veterinario")

                .requestMatchers(HttpMethod.DELETE,
                    "/api/productos/**")
                    .hasRole("Admin")

                // Informes
                .requestMatchers(HttpMethod.POST,
                    "/api/informes/generar")
                    .hasAnyRole("Admin", "Veterinario")

                // Endpoints por rol
                .requestMatchers("/api/admin/**")
                    .hasRole("Admin")

                .requestMatchers("/api/veterinario/**")
                    .hasAnyRole("Admin", "Veterinario")

                // Denegar rutas no autorizadas explicitamente
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
            Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList()
        );

        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
            List.of("Authorization", "Content-Type", "X-API-SECRET")
        );

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> groups =
                jwt.getClaimAsStringList("cognito:groups");

            if (groups == null) {
                return Collections.emptyList();
            }

            return groups.stream()
                .map(group -> new SimpleGrantedAuthority("ROLE_" + group))
                .collect(Collectors.toList());
        });

        return converter;
    }
}
