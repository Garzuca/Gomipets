package com.gomitas.service;

import com.gomitas.dto.ReporteDtos;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface ReporteService {
    ReporteDtos.ReporteVentasResponseDto generarReporteVentas(ReporteDtos.ReporteVentasRequestDto requestDto);
    List<ReporteDtos.ReporteInventarioDto> generarReporteInventario();
    void exportarReporteVentas(HttpServletResponse response, ReporteDtos.ReporteVentasRequestDto requestDto, String format) throws IOException;
    void exportarReporteInventario(HttpServletResponse response, String format) throws IOException;

    // Métodos para el reporte de maquinaria
    List<ReporteDtos.ReporteMaquinariaDto> generarReporteMaquinaria(ReporteDtos.ReporteMaquinariaRequestDto requestDto);
    void exportarReporteMaquinaria(HttpServletResponse response, ReporteDtos.ReporteMaquinariaRequestDto requestDto, String format) throws IOException;
}