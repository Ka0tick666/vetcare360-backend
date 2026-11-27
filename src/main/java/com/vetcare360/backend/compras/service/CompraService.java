package com.vetcare360.backend.compras.service;

import com.vetcare360.backend.compras.dto.*;
import com.vetcare360.backend.compras.entity.*;
import com.vetcare360.backend.compras.repository.*;
import com.vetcare360.products.ProductRepository;
import com.vetcare360.products.entity.ProductEntity;

import jakarta.persistence.EntityManager;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CompraService {

    private static final BigDecimal MAX_TOTAL =
            new BigDecimal("999999999999.99");

    private final CompraRepository compraRepository;
    private final CompraDetalleRepository detalleRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    public CompraService(
            CompraRepository compraRepository,
            CompraDetalleRepository detalleRepository,
            ProductRepository productRepository,
            EntityManager entityManager,
            JdbcTemplate jdbcTemplate) {

        this.compraRepository = compraRepository;
        this.detalleRepository = detalleRepository;
        this.productRepository = productRepository;
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public CompraResponseDto crearCompra(
            String clienteId,
            UUID compraId,
            CompraRequestDto request) {

        validarCliente(clienteId);

        if (compraId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Idempotency-Key es obligatorio"
            );
        }

        Map<Long, Integer> cantidades = validarItems(request);

        // Evitar solicitudes concurrentes con la misma clave.
        jdbcTemplate.queryForObject(
                """
                SELECT 1
                FROM pg_advisory_xact_lock(
                    hashtextextended(CAST(? AS text), 0)
                )
                """,
                Integer.class,
                compraId.toString()
        );

        Optional<CompraEntity> existente =
                compraRepository.findById(compraId);

        if (existente.isPresent()) {

            CompraEntity compra = existente.get();

            if (!compra.getClienteId().equals(clienteId)) {
                throw conflictoIdempotencia();
            }

            List<CompraDetalleEntity> detalles =
                    detalleRepository
                            .findByCompraIdOrderByProductoIdAsc(compraId);

            Map<Long, Integer> cantidadesGuardadas =
                    new TreeMap<>();

            for (CompraDetalleEntity detalle : detalles) {
                cantidadesGuardadas.put(
                        detalle.getProductoId(),
                        detalle.getCantidad()
                );
            }

            if (!cantidadesGuardadas.equals(cantidades)) {
                throw conflictoIdempotencia();
            }

            return convertirRespuesta(compra, detalles);
        }

        BigDecimal total = BigDecimal.ZERO;

        List<CompraDetalleEntity> detalles =
                new ArrayList<>();

        for (Map.Entry<Long, Integer> item :
                cantidades.entrySet()) {

            Long productoId = item.getKey();
            Integer cantidad = item.getValue();

            ProductEntity producto = productRepository
                    .findById(productoId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Producto no encontrado: " + productoId
                            )
                    );

            Double precioActual = producto.getPrice();

            if (precioActual == null
                    || !Double.isFinite(precioActual)
                    || precioActual <= 0) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El producto tiene un precio invalido"
                );
            }

            BigDecimal precio = BigDecimal
                    .valueOf(precioActual)
                    .setScale(2, RoundingMode.HALF_UP);

            if (precio.signum() <= 0
                    || precio.compareTo(MAX_TOTAL) > 0) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Precio fuera del rango permitido"
                );
            }

            BigDecimal subtotal = precio.multiply(
                    BigDecimal.valueOf(cantidad)
            );

            total = total.add(subtotal);

            if (total.compareTo(MAX_TOTAL) > 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El total supera el limite permitido"
                );
            }

            detalles.add(
                    new CompraDetalleEntity(
                            compraId,
                            productoId,
                            producto.getName(),
                            cantidad,
                            precio
                    )
            );
        }

        total = total.setScale(2, RoundingMode.HALF_UP);

        CompraEntity compra = new CompraEntity(
                compraId,
                clienteId,
                total
        );

        // Insertar una compra nueva sin sobrescribir otra existente.
        entityManager.persist(compra);
        entityManager.flush();

        detalleRepository.saveAll(detalles);

        UUID eventoId = UUID.randomUUID();

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("event_id", eventoId.toString());
        payload.put("type", "compra.creada");
        payload.put("compra_id", compraId.toString());
        payload.put("cliente_id", clienteId);
        payload.put("total", total);
        payload.put("moneda", "CLP");
        payload.put("estado", "PENDIENTE");
        payload.put("simulacion", true);

        CompraEventoOutboxEntity evento =
                new CompraEventoOutboxEntity(
                        eventoId,
                        compraId,
                        "compra.creada",
                        payload
                );

        entityManager.persist(evento);
        entityManager.flush();

        return convertirRespuesta(compra, detalles);
    }

    @Transactional(readOnly = true)
    public List<CompraResponseDto> obtenerHistorial(
            String clienteId) {

        validarCliente(clienteId);

        return compraRepository
                .findByClienteIdOrderByCreatedAtDesc(clienteId)
                .stream()
                .map(compra -> convertirRespuesta(
                        compra,
                        detalleRepository
                                .findByCompraIdOrderByProductoIdAsc(
                                        compra.getId()
                                )
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CompraResponseDto obtenerCompra(
            String clienteId,
            UUID compraId) {

        validarCliente(clienteId);

        CompraEntity compra = compraRepository
                .findByIdAndClienteId(compraId, clienteId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Compra no encontrada"
                        )
                );

        List<CompraDetalleEntity> detalles =
                detalleRepository
                        .findByCompraIdOrderByProductoIdAsc(compraId);

        return convertirRespuesta(compra, detalles);
    }

    private Map<Long, Integer> validarItems(
            CompraRequestDto request) {

        if (request == null
                || request.items() == null
                || request.items().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La compra debe contener productos"
            );
        }

        if (request.items().size() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximo 100 productos por compra"
            );
        }

        Map<Long, Integer> cantidades = new TreeMap<>();

        for (CompraItemRequestDto item : request.items()) {

            if (item == null
                    || item.productoId() == null
                    || item.productoId() <= 0
                    || item.cantidad() == null
                    || item.cantidad() <= 0
                    || item.cantidad() > 1000) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Producto o cantidad invalida"
                );
            }

            if (cantidades.containsKey(item.productoId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No se permiten productos duplicados"
                );
            }

            cantidades.put(
                    item.productoId(),
                    item.cantidad()
            );
        }

        return cantidades;
    }

    private void validarCliente(String clienteId) {

        if (clienteId == null || clienteId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Cliente no autenticado"
            );
        }
    }

    private ResponseStatusException conflictoIdempotencia() {

        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La clave de idempotencia ya fue utilizada"
        );
    }

    private CompraResponseDto convertirRespuesta(
            CompraEntity compra,
            List<CompraDetalleEntity> detalles) {

        List<CompraDetalleResponseDto> items =
                detalles.stream()
                        .map(detalle ->
                                new CompraDetalleResponseDto(
                                        detalle.getProductoId(),
                                        detalle.getNombreProducto(),
                                        detalle.getCantidad(),
                                        detalle.getPrecioUnitario(),
                                        detalle.getSubtotal()
                                )
                        )
                        .toList();

        return new CompraResponseDto(
                compra.getId(),
                compra.getClienteId(),
                compra.getTotal(),
                compra.getMoneda(),
                compra.getEstado(),
                compra.getMotivoRechazo(),
                compra.getSimulacion(),
                compra.getCreatedAt(),
                compra.getUpdatedAt(),
                items
        );
    }
}