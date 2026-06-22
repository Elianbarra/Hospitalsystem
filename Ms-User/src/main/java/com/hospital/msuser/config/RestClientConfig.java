package com.hospital.msuser.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * RestClient para llamar a ms-auth (registro de credenciales).
     * Solo se usa en el flujo de registro — no lleva token Bearer.
     */
    @Bean("msAuthRestClient")
    public RestClient authRestClient(@Value("${ms-auth.base-url}") String authBaseUrl) {
        return RestClient.builder()
                .baseUrl(authBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
