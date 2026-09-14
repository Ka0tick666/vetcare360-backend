package com.vetcare360.backend;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VetController {

    // EndpoinT público para verificación de estado (Health Check)
    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        return Map.of("status", "UP");
    }

    // Endpoint restringido solo a usuarios con rol Veterinario o Admin
    @GetMapping("/veterinario/fichas")
    public Map<String, String> consultarFichaMedica() {
        return Map.of(
            "mascota", "Pelusa",
            "especie", "Felino",
            "diagnostico", "Control sano y vacunas al día"
        );
    }

    // Endpoint restringido exclusivamente a rol Admin
    @GetMapping("/admin/dashboard")
    public Map<String, String> obtenerMetricasAdmin() {
        return Map.of(
            "usuariosTotales", "150",
            "ingresosMensuales", "$4,500,000",
            "estadoServidor", "Operacional"
        );
    }
}