package com.vetcare360.backend.products.dto;

public record ProductResponseDto(
    Long id,
    String nombre,
    String descripcion,
    Double precio,
    Integer stock
) {}