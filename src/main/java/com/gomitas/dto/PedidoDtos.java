package com.gomitas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PedidoDtos {

    @Builder
    public record CreatePedidoItemDto(
            @NotNull(message = "El ID del producto es requerido.")
            Long productoId,

            @NotNull(message = "La cantidad es requerida.")
            @Positive(message = "La cantidad debe ser mayor que cero.")
            Integer cantidad
    ) {}

    @Builder
    public record CreatePedidoRequestDto(
            @NotEmpty(message = "La lista de productos no puede estar vacía.")
            @Valid
            List<CreatePedidoItemDto> items,

            String metodoPago,
            String observaciones
    ) {}

    @Builder
    public record DetallePedidoResponseDto(
            Long detalleId,
            Long productoId,
            String nombreProducto,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {}

    @Builder
    public record PedidoResponseDto(
            Long pedidoId,
            Long clienteId,
            String nombreCliente,
            LocalDate fechaPedido,
            String estado,
            String metodoPago,
            BigDecimal total,
            String observaciones,
            List<DetallePedidoResponseDto> detalles
    ) {}

    @Builder
    public record UpdatePedidoStatusDto(
            @NotEmpty(message = "El estado no puede estar vacío.")
            String estado
    ) {}
}
