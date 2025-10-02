package com.gomitas.controller;

import com.gomitas.dto.ReporteDtos;
import com.gomitas.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Endpoints para la generación y exportación de reportes")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final ReporteService reporteService;

    @Operation(summary = "Generar reporte de ventas", description = "Obtiene un reporte de ventas filtrado por fecha, producto o cliente.")
    @PostMapping("/ventas")
    public ResponseEntity<ReporteDtos.ReporteVentasResponseDto> generarReporteVentas(@RequestBody ReporteDtos.ReporteVentasRequestDto requestDto) {
        return ResponseEntity.ok(reporteService.generarReporteVentas(requestDto));
    }

    @Operation(summary = "Exportar reporte de ventas", description = "Descarga un archivo con el reporte de ventas en el formato especificado (csv, pdf, excel).")
    @PostMapping("/ventas/export")
    public void exportarReporteVentas(@RequestBody ReporteDtos.ReporteVentasRequestDto requestDto,
                                      @Parameter(description = "Formato del archivo: csv, pdf, excel", required = true) @RequestParam String format,
                                      HttpServletResponse response) throws IOException {
        reporteService.exportarReporteVentas(response, requestDto, format);
    }

    @Operation(summary = "Generar reporte de inventario", description = "Obtiene un reporte del estado actual del inventario.")
    @GetMapping("/inventario")
    public ResponseEntity<List<ReporteDtos.ReporteInventarioDto>> generarReporteInventario() {
        return ResponseEntity.ok(reporteService.generarReporteInventario());
    }

    @Operation(summary = "Exportar reporte de inventario", description = "Descarga un archivo con el reporte de inventario en el formato especificado (csv, pdf, excel).")
    @GetMapping("/inventario/export")
    public void exportarReporteInventario(@Parameter(description = "Formato del archivo: csv, pdf, excel", required = true) @RequestParam String format,
                                          HttpServletResponse response) throws IOException {
        reporteService.exportarReporteInventario(response, format);
    }
}