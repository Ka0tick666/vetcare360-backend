package com.vetcare360.backend.products;

import com.vetcare360.backend.products.dto.ProductRequestDto;
import com.vetcare360.backend.products.dto.ProductResponseDto;
import com.vetcare360.backend.products.entity.ProductEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponseDto> findAll() {
        return productRepository.findAll().stream()
                .map(p -> new ProductResponseDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock()))
                .collect(Collectors.toList());
    }

    public ProductResponseDto create(ProductRequestDto dto) {
        ProductEntity entity = new ProductEntity(dto.getName(), dto.getDescription(), dto.getPrice(), dto.getStock());
        ProductEntity saved = productRepository.save(entity);
        return new ProductResponseDto(saved.getId(), saved.getName(), saved.getDescription(), saved.getPrice(), saved.getStock());
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}