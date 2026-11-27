package com.vetcare360.backend.compras.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CompraResponseDto(
    UUID id,
    String clienteId,
    BigDecimal total,
    String moneda,
    String estado,
    String motivoRechazo,
    Boolean simulacion,
    Instant createdAt,
    Instant updatedAt,
    List<CompraDetalleResponseDto> items
) {
}