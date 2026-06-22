package com.hospital.msuser.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Acceso local: http://localhost:8081/swagger-ui.html
 * En K8s: kubectl port-forward svc/ms-user-svc 8081:8081 -n hospital
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MS-User API",
                version = "1.0.0",
                description = "Microservicio de gestión de usuarios. Registro, consulta y actualización de pacientes y doctores.",
                contact = @Contact(name = "Hospital System", email = "admin@hospital.cl")
        ),
        servers = @Server(url = "/", description = "Servidor actual")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtenido de POST /api/auth/login en MS-Auth"
)
public class OpenApiConfig {
}
