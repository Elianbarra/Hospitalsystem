package com.hospital.msappointment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Ms-Appointment ya no llama a ms-auth directamente.
 * La validación JWT se hace vía Spring Security OAuth2 Resource Server (JWKS).
 * Solo queda el RestClient para consultar datos de usuario a ms-user.
 */
@Configuration
public class RestClientConfig {

    @Bean("userRestClient")
    public RestClient userRestClient(@Value("${ms-user.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
