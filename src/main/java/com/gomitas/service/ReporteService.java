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
}