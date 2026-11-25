
package com.vetcare360.products;

import com.vetcare360.products.dto.ProductRequestDto;
import com.vetcare360.products.dto.ProductResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'Veterinario', 'Cliente')")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'Veterinario')")
    public ResponseEntity<ProductResponseDto> createProduct(
            @RequestBody ProductRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'Veterinario')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequestDto dto) {

        return ResponseEntity.ok(productService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {

        productService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
