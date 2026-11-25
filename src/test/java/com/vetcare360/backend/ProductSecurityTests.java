
package com.vetcare360.backend;

import com.vetcare360.products.ProductController;
import com.vetcare360.products.ProductService;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "app.cors.allowed-origins=http://localhost:5173",
    "COGNITO_ISSUER_URI=https://example.invalid",
    "DB_URL=jdbc:postgresql://localhost:5432/test",
    "DB_USERNAME=test",
    "DB_PASSWORD=test",
    "API_GATEWAY_SECRET=test-secret",
    "SNS_TOPIC_ARN=arn:aws:sns:us-east-1:000000000000:test",
    "AWS_REGION=us-east-1"
})
class ProductSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final String PRODUCT_JSON = """
        {
            "name": "Producto de prueba",
            "description": "Prueba automatizada EVA3",
            "price": 5000,
            "stock": 10
        }
        """;

    @ParameterizedTest(name = "{0} - {1} debe responder {2}")
    @CsvSource({
        // Administrador
        "Admin, GET, 200",
        "Admin, POST, 201",
        "Admin, PUT, 200",
        "Admin, DELETE, 204",

        // Veterinario
        "Veterinario, GET, 200",
        "Veterinario, POST, 201",
        "Veterinario, PUT, 200",
        "Veterinario, DELETE, 403",

        // Cliente
        "Cliente, GET, 200",
        "Cliente, POST, 403",
        "Cliente, PUT, 403",
        "Cliente, DELETE, 403",

        // Usuario sin autenticacion
        "Anonimo, GET, 401",
        "Anonimo, POST, 401",
        "Anonimo, PUT, 401",
        "Anonimo, DELETE, 401"
    })
    void verificarPermisos(
            String rol,
            String metodo,
            int estadoEsperado) throws Exception {

        String ruta = "/api/productos";

        // PUT y DELETE requieren un ID
        if (metodo.equals("PUT") || metodo.equals("DELETE")) {
            ruta += "/999";
        }

        MockHttpServletRequestBuilder peticion =
            request(HttpMethod.valueOf(metodo), ruta);

        // POST y PUT requieren un cuerpo JSON
        if (metodo.equals("POST") || metodo.equals("PUT")) {
            peticion
                .contentType(MediaType.APPLICATION_JSON)
                .content(PRODUCT_JSON);
        }

        // Simular un token Cognito para usuarios autenticados
        if (!rol.equals("Anonimo")) {

            String token = "token-prueba-" + rol;

            Jwt jwt = Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .subject("usuario-prueba-" + rol)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("cognito:groups", List.of(rol))
                .claim("token_use", "access")
                .claim("client_id", "cliente-prueba")
                .build();

            when(jwtDecoder.decode(token)).thenReturn(jwt);

            peticion.header(
                "Authorization",
                "Bearer " + token
            );
        }

        // Comprobar el estado HTTP esperado
        mockMvc.perform(peticion)
            .andExpect(status().is(estadoEsperado));

        // Verificar que las peticiones rechazadas
        // no lleguen al servicio de productos
        if (estadoEsperado == 401 || estadoEsperado == 403) {
            verifyNoInteractions(productService);
        }
    }
}

