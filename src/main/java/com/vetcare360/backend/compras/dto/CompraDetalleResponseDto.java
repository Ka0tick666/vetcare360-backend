package com.vetcare360.backend.compras.dto;

import java.math.BigDecimal;

public record CompraDetalleResponseDto(
    Long productoId,
    String nombreProducto,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {
}