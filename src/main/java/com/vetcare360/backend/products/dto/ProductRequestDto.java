package com.vetcare360.backend.products.dto;

public record ProductRequestDto(
    String nombre,
    String descripcion,
    Double precio,
    Integer stock
) {}