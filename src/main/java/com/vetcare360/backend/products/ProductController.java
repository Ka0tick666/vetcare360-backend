package com.vetcare360.backend.products;

import com.vetcare360.backend.products.dto.ProductRequestDto;
import com.vetcare360.backend.products.dto.ProductResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductController {

    // Lista simulada en memoria (Mock data)
    private final List<ProductResponseDto> productos = new ArrayList<>(List.of(
        new ProductResponseDto(1L, "Vacuna Antirrábica", "Dosis anual preventiva para caninos y felinos", 15000.0, 45),
        new ProductResponseDto(2L, "Desparasitante Interno", "Comprimidos de amplio espectro", 8500.0, 100),
        new ProductResponseDto(3L, "Alimento Clínico 3kg", "Nutrición especializada post-operatoria", 32000.0, 20),
        new ProductResponseDto(4L, "Antiséptico Tópico", "Solución para curación de heridas", 6200.0, 60)
    ));

    // GET /api/productos - Accesible para cualquier usuario autenticado
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> listarProductos() {
        return ResponseEntity.ok(productos);
    }

    // GET /api/productos/{id} - Consultar un producto específico
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> obtenerPorId(@PathVariable Long id) {
        return productos.stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/productos - Crear producto (Restringido en SecurityConfig a Veterinario/Admin)
    @PostMapping
    public ResponseEntity<ProductResponseDto> crearProducto(@RequestBody ProductRequestDto request) {
        Long nuevoId = (long) (productos.size() + 1);
        ProductResponseDto nuevoProducto = new ProductResponseDto(
                nuevoId,
                request.nombre(),
                request.descripcion(),
                request.precio(),
                request.stock()
        );
        productos.add(nuevoProducto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    // DELETE /api/productos/{id} - Eliminar producto (Exclusivo Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        boolean removido = productos.removeIf(p -> p.id().equals(id));
        if (removido) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}