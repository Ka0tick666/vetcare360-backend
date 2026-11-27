package com.vetcare360.backend.compras.dto;

public record CompraItemRequestDto(
    Long productoId,
    Integer cantidad
) {
}