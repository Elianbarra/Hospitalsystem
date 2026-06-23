package com.hospital.msuser.client;

import com.hospital.msuser.client.auth.AuthFeignClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifica que las clases deprecadas existen y son instanciables.
 * La lógica real ha sido migrada a UserService (ver UserServiceTest).
 *
 * @deprecated Eliminar junto con UserClient y AuthFeignClient cuando se confirme
 *             que ningún componente externo depende de ellas.
 */
@Deprecated
class UserClientTest {

    @Test
    void userClient_isInstantiable() {
        UserClient client = new UserClient();
        assertNotNull(client, "UserClient (deprecated) debe ser instanciable");
    }

    @Test
    void authFeignClient_isInstantiable() {
        AuthFeignClient feign = new AuthFeignClient();
        assertNotNull(feign, "AuthFeignClient (deprecated) debe ser instanciable");
    }
}
