package com.vetcare360.backend.compras.repository;

import com.vetcare360.backend.compras.entity.CompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompraRepository
        extends JpaRepository<CompraEntity, UUID> {

    List<CompraEntity> findByClienteIdOrderByCreatedAtDesc(
        String clienteId
    );

    Optional<CompraEntity> findByIdAndClienteId(
        UUID id,
        String clienteId
    );
}
