package com.hospital.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Verifica que el contexto de Spring Boot carga correctamente.
 *
 * ReactiveJwtDecoder se mockea para evitar la conexión al JWKS de ms-auth
 * durante el arranque del contexto de test (igual que en ms-user).
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9999/test-jwks"
})
class ApiGatewayApplicationTests {

    @MockitoBean
    ReactiveJwtDecoder reactiveJwtDecoder;

    @Test
    void contextLoads() {
        // Verifica que todos los beans se inicializan sin errores
    }
}
