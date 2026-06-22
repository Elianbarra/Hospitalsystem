package com.hospital.msauth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Acceso local: http://localhost:8080/swagger-ui.html
 * En K8s: kubectl port-forward svc/ms-auth-svc 8080:8080 -n hospital
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MS-Auth API",
                version = "1.0.0",
                description = "Microservicio de autenticación. Emite tokens JWT firmados con RSA y expone la clave pública vía JWKS.",
                contact = @Contact(name = "Hospital System", email = "admin@hospital.cl")
        ),
        servers = @Server(url = "/", description = "Servidor actual")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtenido de POST /api/auth/login"
)
public class OpenApiConfig {
}
