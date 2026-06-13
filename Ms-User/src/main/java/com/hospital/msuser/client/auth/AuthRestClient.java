package com.hospital.msuser.client.auth;

import com.hospital.msuser.dto.auth.RegisterAuthRequestDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthRestClient {

    private final RestClient restClient;

    public AuthRestClient(@Qualifier("authRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void registerUserCredentials(RegisterAuthRequestDTO dto) {
        restClient.post()
                .uri("/api/auth/register")
                .body(dto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RuntimeException("Error al registrar credenciales en MS-AUTH: "
                            + res.getStatusCode());
                })
                .toBodilessEntity();
    }
}
