
package com.vetcare360.products;

import com.vetcare360.products.dto.ProductRequestDto;
import com.vetcare360.products.dto.ProductResponseDto;
import com.vetcare360.products.entity.ProductEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ProductResponseDto create(ProductRequestDto dto) {
        validateProduct(dto);

        ProductEntity entity = new ProductEntity(
                dto.getName().trim(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getStock()
        );

        return toDto(productRepository.save(entity));
    }

    @Transactional
    public ProductResponseDto update(Long id, ProductRequestDto dto) {
        validateProduct(dto);

        ProductEntity entity = findProduct(id);

        entity.setName(dto.getName().trim());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());

        return toDto(productRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        ProductEntity entity = findProduct(id);
        productRepository.delete(entity);
    }

    private ProductEntity findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));
    }

    private void validateProduct(ProductRequestDto dto) {

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre es obligatorio"
            );
        }

        if (dto.getPrice() == null
                || !Double.isFinite(dto.getPrice())
                || dto.getPrice() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El precio debe ser mayor a cero"
            );
        }

        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock no puede ser negativo"
            );
        }
    }

    private ProductResponseDto toDto(ProductEntity entity) {
        return new ProductResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStock()
        );
    }
}
