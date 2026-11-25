
package com.vetcare360.backend;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CognitoJwtValidatorTests {

    private static final String CLIENT_ID = "cliente-prueba";

    @ParameterizedTest(name = "token_use={0}, client_id={1}, valido={2}")
    @CsvSource({
        "access, cliente-prueba, true",
        "id, cliente-prueba, false",
        "refresh, cliente-prueba, false",
        "access, otro-cliente, false",
        "id, otro-cliente, false",
        "NULL, cliente-prueba, false",
        "access, NULL, false",
        "NULL, NULL, false"
    })
    void validarClaimsCognito(
            String tokenUse,
            String clientId,
            boolean esperado) {

        SecurityConfig securityConfig = new SecurityConfig();

        ReflectionTestUtils.setField(
            securityConfig,
            "cognitoClientId",
            CLIENT_ID
        );

        Jwt.Builder builder = Jwt.withTokenValue("token-prueba")
            .header("alg", "RS256")
            .issuer("https://example.invalid")
            .subject("usuario-prueba")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600));

        if (!"NULL".equals(tokenUse)) {
            builder.claim("token_use", tokenUse);
        }

        if (!"NULL".equals(clientId)) {
            builder.claim("client_id", clientId);
        }

        Jwt jwt = builder.build();

        OAuth2TokenValidatorResult resultado =
            securityConfig.cognitoClaimsValidator()
                .validate(jwt);

        assertEquals(esperado, !resultado.hasErrors());
    }
}
