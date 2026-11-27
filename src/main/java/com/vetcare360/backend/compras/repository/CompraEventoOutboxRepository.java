package com.vetcare360.backend.compras.repository;

import com.vetcare360.backend.compras.entity.CompraEventoOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompraEventoOutboxRepository
        extends JpaRepository<CompraEventoOutboxEntity, UUID> {
}
