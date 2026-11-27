package com.vetcare360.backend.compras.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "compra_detalles", schema = "public")
public class CompraDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compra_id", nullable = false)
    private UUID compraId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(
        name = "precio_unitario",
        nullable = false,
        precision = 14,
        scale = 2
    )
    private BigDecimal precioUnitario;

    @Column(
        precision = 14,
        scale = 2,
        insertable = false,
        updatable = false
    )
    private BigDecimal subtotal;

    protected CompraDetalleEntity() {}

    public CompraDetalleEntity(
            UUID compraId,
            Long productoId,
            String nombreProducto,
            Integer cantidad,
            BigDecimal precioUnitario) {

        this.compraId = compraId;
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Long getId() {
        return id;
    }

    public UUID getCompraId() {
        return compraId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(
            BigDecimal.valueOf(cantidad)
        );
    }
}