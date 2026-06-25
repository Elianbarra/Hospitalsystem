package com.hospital.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Seguridad del API Gateway — WebFlux (reactivo).
 *
 * IMPORTANTE: Spring Cloud Gateway usa WebFlux, NO Spring MVC.
 * Por eso se usa SecurityWebFilterChain (no SecurityFilterChain) y
 * @EnableWebFluxSecurity (no @EnableWebSecurity).
 *
 * Flujo de validación:
 *   1. Request entra al gateway
 *   2. Spring Security valida el JWT contra JWKS de ms-auth
 *   3. Si es válido → se reenvía al BFF con el mismo Bearer token
 *   4. Si no es válido → 401 Unauthorized sin llegar al BFF
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges
                        // Rutas públicas: login, registro y health check
                        .pathMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/actuator/health"
                        ).permitAll()

                        // Todo lo demás requiere JWT válido
                        .anyExchange().authenticated()
                )

                // Valida JWT vía JWKS (ms-auth expone /.well-known/jwks.json)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))

                .build();
    }
}
