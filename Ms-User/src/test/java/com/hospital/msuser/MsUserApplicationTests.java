package com.hospital.msuser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
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
    // H2 en modo PostgreSQL para simular la BD sin servidor externo
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    // Desactivar Flyway: las migraciones usan SQL específico de PostgreSQL
    "spring.flyway.enabled=false",
    // URL ficticia: el bean RestClient se crea pero no conecta al arrancar
    "ms-auth.base-url=http://localhost:9999"
})
class MsUserApplicationTests {

    /**
     * Mockea el JwtDecoder para evitar que Spring intente descargar
     * el JWK Set desde ms-auth durante el arranque del contexto de test.
     */
    @MockBean
    JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
        // Verifica que todos los beans se inicializan sin errores
    }
}
