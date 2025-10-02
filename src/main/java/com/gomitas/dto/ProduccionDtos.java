package com.gomitas.dto;

import com.gomitas.enums.EstadoProduccion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProduccionDtos {

    public record CreateOrdenProduccionRequestDto(
            @NotNull(message = "El ID del producto no puede ser nulo")
            Long productoId,

            @NotNull(message = "La cantidad planificada no puede ser nula")
            @Min(value = 1, message = "La cantidad planificada debe ser al menos 1")
            Integer cantidadPlanificada,

            String observaciones
    ) {}

    public record UpdateOrdenProduccionStatusDto(
            @NotNull(message = "El estado no puede ser nulo")
            EstadoProduccion estado
    ) {}

    public record OrdenProduccionResponseDto(
            Long ordenId,
            Long productoId,
            String nombreProducto,
            Integer cantidadPlanificada,
            Integer cantidadProducida,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            EstadoProduccion estado,
            String observaciones,
            List<DetalleProduccionResponseDto> detalles
    ) {}

    public record DetalleProduccionResponseDto(
            Long detalleId,
            Long insumoId,
            String nombreInsumo,
            BigDecimal cantidadRequerida,
            BigDecimal cantidadUtilizada
    ) {}

    public record RecetaProductoResponseDto(
            Long productoId,
            String nombreProducto,
            List<RecetaInsumoDto> insumos
    ) {}

    public record RecetaInsumoDto(
            Long insumoId,
            String nombreInsumo,
            BigDecimal cantidadRequerida,
            String unidad
    ) {}
}
