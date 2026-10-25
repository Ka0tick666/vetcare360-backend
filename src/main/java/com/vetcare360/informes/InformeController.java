package com.vetcare360.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.Map;

@RestControllers
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class InformeController {

    @Value("${api.secret}")
    private String expectedSecret;

    @Value("${aws.sns.topic.arn}")
    private String topicArn;

    @Value("${aws.region}")
    private String awsRegion;

    // 1. Endpoint Healthcheck requerido por la arquitectura
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    // 2. Endpoint de Generación de Informes con validación de X-API-SECRET y envío a SNS
    @PostMapping("/informes/generar")
    public ResponseEntity<?> generarInforme(@RequestHeader(value = "X-API-SECRET", required = false) String apiSecret) {
        
        // Validación de header de seguridad
        if (apiSecret == null || !apiSecret.equals(expectedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "X-API-SECRET inválido o no provisto"));
        }

        try (SnsClient snsClient = SnsClient.builder()
                .region(Region.of(awsRegion))
                .build()) {

            // Mensaje en formato JSON con la clave type = 'informe'
            String payload = "{\"type\": \"informe\", \"mensaje\": \"Informe generado desde BFF\"}";

            PublishRequest request = PublishRequest.builder()
                    .message(payload)
                    .topicArn(topicArn)
                    .build();

            snsClient.publish(request);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "mensaje", "Evento 'informe' publicado exitosamente en SNS"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al publicar mensaje en SNS: " + e.getMessage()));
        }
    }
}