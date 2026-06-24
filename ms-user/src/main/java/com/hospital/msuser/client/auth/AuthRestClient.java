package com.hospital.msuser.client.auth;

import com.hospital.msuser.dto.auth.RegisterAuthRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP para ms-auth.
 * Usado exclusivamente en el flujo de registro para crear las credenciales del usuario.
 */
@Component
@Slf4j
public class AuthRestClient {

    private final RestClient restClient;

    public AuthRestClient(@Qualifier("authRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void registerUserCredentials(RegisterAuthRequestDTO dto) {
        log.debug("ms-user → ms-auth  POST /api/auth/register  email={}", dto.getEmail());
        restClient.post()
                .uri("/api/auth/register")
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
}
