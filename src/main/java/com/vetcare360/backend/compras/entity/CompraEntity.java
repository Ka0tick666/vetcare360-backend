package com.vetcare360.backend.compras.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "compras", schema = "public")
public class CompraEntity {

    @Id
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private String moneda;

    @Column(nullable = false)
    private String estado;

    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

    @Column(nullable = false)
    private Boolean simulacion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CompraEntity() {}

    public CompraEntity(
            UUID id,
            String clienteId,
            BigDecimal total) {

        this.id = id;
        this.clienteId = clienteId;
        this.total = total;
        this.moneda = "CLP";
        this.estado = "PENDIENTE";
        this.simulacion = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getMoneda() {
        return moneda;
    }

    public String getEstado() {
        return estado;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public Boolean getSimulacion() {
        return simulacion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}