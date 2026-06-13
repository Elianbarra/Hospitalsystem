package cl.rednorte.bff;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "ms.auth.url=http://localhost:8080",
        "ms.user.url=http://localhost:8081",
        "ms.appointments.url=http://localhost:8082",
        "ms.waitlist.url=http://localhost:8083",
        "frontend.url=http://localhost:3001"
})
class BffApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring Boot carga correctamente
    }
}
