package com.hospital.msappointment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Ms-Appointment no se comunica directamente con otros microservicios.
 * Toda la orquestación entre servicios la hace el BFF.
 * Este bean queda disponible por si en el futuro se requiere alguna consulta interna,
 * pero actualmente no se usa en producción.
 */
@Configuration
public class RestClientConfig {

    @Bean("msUserClient")
    public RestClient userRestClient(@Value("${ms-user.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
