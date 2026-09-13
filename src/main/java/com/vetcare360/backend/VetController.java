package com.vetcare360.backend;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VetController {

    // Endpoint accesible para cualquier usuario autenticado
    @GetMapping("/productos")
    public List<Map<String, Object>> listarProductos() {
        return List.of(
            Map.of("id", 1, "nombre", "Vacuna Antirrábica", "precio", 15000),
            Map.of("id", 2, "nombre", "Alimento Perro Adulto 10kg", "precio", 32000),
            Map.of("id", 3, "nombre", "Pipeta Antiparasitaria", "precio", 8500)
        );
    }

    // Endpoint restringido solo a rol Veterinario o Admin
    @GetMapping("/veterinario/fichas")
    public Map<String, String> consultarFichaMedica() {
        return Map.of(
            "mascota", "Pelusa",
            "especie", "Felino",
            "diagnostico", "Control sano y vacunas al día"
        );
    }
}