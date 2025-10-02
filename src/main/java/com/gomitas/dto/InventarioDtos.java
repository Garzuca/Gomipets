package com.gomitas.dto;

import com.gomitas.enums.TipoMovimientoInventario;

import java.time.LocalDateTime;

public class InventarioDtos {

    public record InventarioResponseDto(
            Long inventarioId,
            Long productoId,
            String nombreProducto,
            Integer cantidadDisponible,
            LocalDateTime fechaActualizacion
    ) {}

    public record MovimientoRequestDto(
            Long productoId,
            TipoMovimientoInventario tipoMovimiento,
            Integer cantidad,
            String motivo
    ) {}

    public record MovimientoResponseDto(
            Long movimientoId,
            Long productoId,
            String nombreProducto,
            TipoMovimientoInventario tipoMovimiento,
            Integer cantidad,
            Integer cantidadAnterior,
            Integer cantidadNueva,
            LocalDateTime fecha,
            String motivo,
            String nombreUsuario
    ) {}
}
