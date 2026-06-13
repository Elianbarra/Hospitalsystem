package com.hospital.msauth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configura la UI de Swagger con:
 * - Información del servicio
 * - SecurityScheme HTTP Bearer para poder probar endpoints autenticados desde la UI
 *
 * Acceso local: http://localhost:8080/swagger-ui.html
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MS-Auth API",
                version = "1.0.0",
                description = "Microservicio de autenticación. Emite y valida JWT con par de claves RSA. Expone JWKS para que los demás microservicios validen tokens localmente.",
                contact = @Contact(name = "Hospital System", email = "admin@hospital.cl")
        ),
        servers = @Server(url = "/", description = "Servidor actual")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingresa el token JWT obtenido de POST /api/auth/login"
)
public class OpenApiConfig {
    // La anotación @OpenAPIDefinition es suficiente — no se necesita @Bean adicional
}
