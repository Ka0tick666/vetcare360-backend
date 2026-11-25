
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

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.OAuth2Error;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

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

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${app.cognito.client-id}")
    private String cognitoClientId;

    // ==========================================
    // CONFIGURACION DE SEGURIDAD HTTP
    // ==========================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Solicitudes CORS
                .requestMatchers(
                    HttpMethod.OPTIONS, "/**"
                ).permitAll()

                // Health Check publico
                .requestMatchers(
                    "/api/health", "/health"
                ).permitAll()

                // Consultar productos
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/productos",
                    "/api/productos/**"
                ).hasAnyRole(
                    "Admin", "Veterinario", "Cliente"
                )

                // Crear productos
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/productos",
                    "/api/productos/**"
                ).hasAnyRole(
                    "Admin", "Veterinario"
                )

                // Actualizar productos
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/productos/**"
                ).hasAnyRole(
                    "Admin", "Veterinario"
                )

                // Eliminar productos
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/productos/**"
                ).hasRole("Admin")

                // Generacion de informes
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/informes/generar"
                ).hasAnyRole(
                    "Admin", "Veterinario"
                )

                // Endpoints administrativos
                .requestMatchers(
                    "/api/admin/**"
                ).hasRole("Admin")

                // Endpoints veterinarios
                .requestMatchers(
                    "/api/veterinario/**"
                ).hasAnyRole(
                    "Admin", "Veterinario"
                )

                // Denegar rutas no autorizadas
                .anyRequest().denyAll()
            )

            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(
                        jwtAuthenticationConverter()
                    )
                )
            );

        return http.build();
    }

    // ==========================================
    // VALIDACION DEL TOKEN JWT DE COGNITO
    // ==========================================

    @Bean
    public JwtDecoder jwtDecoder() {

        String jwkSetUri =
            issuerUri + "/.well-known/jwks.json";

        NimbusJwtDecoder decoder =
            NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();

        // Validar emisor y vigencia del token
        OAuth2TokenValidator<Jwt> issuerValidator =
            JwtValidators.createDefaultWithIssuer(
                issuerUri
            );

        // Combinar todas las validaciones
        OAuth2TokenValidator<Jwt> validator =
            new DelegatingOAuth2TokenValidator<>(
                issuerValidator,
                cognitoClaimsValidator()
            );

        decoder.setJwtValidator(validator);

        return decoder;
    }

    // Validar claims especificos de AWS Cognito
    @Bean
    public OAuth2TokenValidator<Jwt> cognitoClaimsValidator() {

        return jwt -> {

            String tokenUse =
                jwt.getClaimAsString("token_use");

            String clientId =
                jwt.getClaimAsString("client_id");

            // Solo permitir Access Tokens
            if (!"access".equals(tokenUse)) {

                return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                        "invalid_token",
                        "Solo se permiten tokens de acceso",
                        null
                    )
                );
            }

            // Validar App Client de Cognito
            if (!cognitoClientId.equals(clientId)) {

                return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                        "invalid_token",
                        "El token no pertenece al App Client autorizado",
                        null
                    )
                );
            }

            return OAuth2TokenValidatorResult.success();
        };
    }

    // ==========================================
    // CONFIGURACION CORS
    // ==========================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
            new CorsConfiguration();

        configuration.setAllowedOrigins(
            Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList()
        );

        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type",
                "X-API-SECRET"
            )
        );

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            configuration
        );

        return source;
    }

    // ==========================================
    // CONVERSION DE GRUPOS COGNITO A ROLES
    // ==========================================

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            List<String> groups =
                jwt.getClaimAsStringList(
                    "cognito:groups"
                );

            if (groups == null) {
                return Collections.emptyList();
            }

            return groups.stream()
                .map(group ->
                    new SimpleGrantedAuthority(
                        "ROLE_" + group
                    )
                )
                .collect(Collectors.toList());
        });

        return converter;
    }
}
