package cl.rednorte.bff.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Acceso local: http://localhost:8090/swagger-ui.html
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "BFF — Hospital System",
                version = "0.1.0",
                description = "Backend For Frontend. Único punto de entrada externo: valida JWT y orquesta ms-auth, ms-user, ms-appointment y ms-waitlist.",
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
