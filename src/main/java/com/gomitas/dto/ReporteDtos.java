package com.gomitas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
}
