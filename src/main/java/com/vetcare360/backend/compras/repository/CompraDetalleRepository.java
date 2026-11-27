package com.vetcare360.backend.compras.repository;

import com.vetcare360.backend.compras.entity.CompraDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompraDetalleRepository
        extends JpaRepository<CompraDetalleEntity, Long> {

    List<CompraDetalleEntity> findByCompraIdOrderByProductoIdAsc(
        UUID compraId
    );
}
