package com.hospital.msauth.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "JWKS", description = "Endpoint público de claves RSA para validación de JWT")
public class JwksController {

    private final RSAKey rsaPublicJwk;

    public JwksController(RSAKey rsaPublicJwk) {
        this.rsaPublicJwk = rsaPublicJwk;
    }

    @Operation(summary = "Obtener claves públicas", description = "Devuelve las claves RSA públicas en formato JWK para verificación de tokens")
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return new JWKSet(rsaPublicJwk.toPublicJWK()).toJSONObject();
    }
}
