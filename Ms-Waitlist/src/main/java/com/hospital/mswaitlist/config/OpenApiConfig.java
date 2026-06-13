package com.hospital.mswaitlist.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Acceso local: http://localhost:8083/swagger-ui.html
 * En K8s: kubectl port-forward svc/ms-waitlist-svc 8083:8083 -n hospital
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MS-Waitlist API",
                version = "1.0.0",
                description = "Microservicio de lista de espera. Gestiona la cola de pacientes usando estrategias de prioridad (STANDARD / PRIORITY). Almacenamiento en memoria — sin persistencia en base de datos.",
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
