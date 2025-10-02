package com.gomitas.dto;

public class DashboardDtos {

    public record MetricasResponseDto(
        long totalClientes,
        long totalProductos,
        long pedidosPendientes,
        long productosConStockBajo,
        long insumosPorVencer,
        long alertasActivas
    ) {}
}
