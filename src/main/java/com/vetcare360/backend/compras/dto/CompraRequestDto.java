package com.vetcare360.backend.compras.dto;

import java.util.List;

public record CompraRequestDto(
    List<CompraItemRequestDto> items
) {
}