package com.gomitas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReporteDtos {

    public record ReporteVentasRequestDto(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Long productoId,
            Long clienteId
    ) {}

    public record ReporteVentasResponseDto(
            List<VentaDetalleDto> detalles,
            Integer totalPedidos,
            Integer totalProductosVendidos,
            BigDecimal montoTotalVendido
    ) {}

    public record VentaDetalleDto(
            Long pedidoId,
            LocalDate fechaVenta,
            String nombreCliente,
            String nombreProducto,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {}

    public record ReporteInventarioDto(
            Long productoId,
            String nombreProducto,
            Integer cantidadDisponible,
            Integer stockMinimo,
            String estadoStock
    ) {}

    // DTO para la solicitud del reporte de maquinaria (filtros)
    public record ReporteMaquinariaRequestDto(
            List<Long> maquinariaIds, // Filtrar por máquinas específicas (opcional)
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Long responsableId // Filtrar por responsable (opcional)
    ) {}

    // DTO para la respuesta del reporte de maquinaria (datos a mostrar/exportar)
    public record ReporteMaquinariaDto(
            Long reporteId,
            String nombreMaquinaria,
            LocalDateTime fechaReporte,
            String tipoReporte,
            String accionesRealizadas,
            String nombreResponsable,
            BigDecimal costoReparacion
    ) {}
}
