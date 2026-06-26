package com.hospital.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — punto de entrada único del sistema Hospital.
 *
 * Responsabilidades:
 *   - Validar el JWT (RS256) antes de reenviar cualquier request al BFF
 *   - Gestionar CORS de forma centralizada
 *   - Enrutar /api/auth/** (público) y /api/** (protegido) hacia el BFF
 *
 * Lo que NO hace este gateway:
 *   - Lógica de negocio (eso es responsabilidad del BFF)
 *   - Transformación de payloads
 *   - Persistencia de datos
 *
 * Topología en Kubernetes:
 *   Frontend → api-gateway (LoadBalancer :8091)
 *              → bff (ClusterIP :8090)
 *                → ms-auth / ms-user / ms-appointment / ms-waitlist (ClusterIP)
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
