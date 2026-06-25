package com.hospital.msuser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifica que el contexto de Spring Boot carga correctamente.
 *
 * Ajustes para el entorno de test:
 *   - H2 en memoria en modo PostgreSQL (sin necesidad de una BD real)
 *   - Flyway desactivado (las migraciones .sql usan sintaxis PostgreSQL)
 *   - JwtDecoder mockeado (evita la conexión al JWKS endpoint de ms-auth)
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9999/test-jwks"
})
class MsUserApplicationTests {

    /**
     * Mockea el JwtDecoder para evitar que Spring intente descargar
     * el JWK Set desde ms-auth durante el arranque del contexto de test.
     */
    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
        // Verifica que todos los beans se inicializan sin errores
    }
}
