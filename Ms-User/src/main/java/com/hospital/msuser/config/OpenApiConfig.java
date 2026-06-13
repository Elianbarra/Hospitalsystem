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
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MS-User API",
                version = "1.0.0",
                description = "Microservicio de gestión de usuarios (pacientes, médicos, administrativos). Requiere JWT emitido por MS-Auth en todos los endpoints excepto registro.",
                contact = @Contact(name = "Hospital System", email = "admin@hospital.cl")
        ),
        servers = @Server(url = "/", description = "Servidor actual")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingresa el token JWT obtenido de POST /api/auth/login en MS-Auth"
)
public class OpenApiConfig {
}
