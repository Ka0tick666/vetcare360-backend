package com.vetcare360.backend.compras.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
    name = "compra_eventos_outbox",
    schema = "public"
)
public class CompraEventoOutboxEntity {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "compra_id", nullable = false)
    private UUID compraId;

    @Column(nullable = false)
    private String tipo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(nullable = false)
    private Boolean publicado;

    @Column(nullable = false)
    private Integer intentos;

    @Column(name = "ultimo_error")
    private String ultimoError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "publicado_at")
    private Instant publicadoAt;

    protected CompraEventoOutboxEntity() {}

    public CompraEventoOutboxEntity(
            UUID eventId,
            UUID compraId,
            String tipo,
            Map<String, Object> payload) {

        this.eventId = eventId;
        this.compraId = compraId;
        this.tipo = tipo;
        this.payload = payload;
        this.publicado = false;
        this.intentos = 0;
        this.createdAt = Instant.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getCompraId() {
        return compraId;
    }

    public String getTipo() {
        return tipo;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Boolean getPublicado() {
        return publicado;
    }

    public Integer getIntentos() {
        return intentos;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublicadoAt() {
        return publicadoAt;
    }
}