package com.gomitas.controller;

import com.gomitas.dto.AlertaDtos;
import com.gomitas.dto.AnalisisDtos;
import com.gomitas.dto.PronosticoDtos;
import com.gomitas.service.InteligenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inteligencia")
@RequiredArgsConstructor
@Tag(name = "Inteligencia de Negocio", description = "Endpoints para pronóstico, análisis y alertas")
@SecurityRequirement(name = "bearerAuth")
public class InteligenciaController {

    private final InteligenciaService inteligenciaService;

    @Operation(summary = "Generar pronóstico de demanda", description = "Calcula una estimación de ventas futuras para un producto.")
    @PostMapping("/pronostico")
    public ResponseEntity<PronosticoDtos.PronosticoResponseDto> generarPronostico(@Valid @RequestBody PronosticoDtos.PronosticoRequestDto requestDto) {
        return ResponseEntity.ok(inteligenciaService.generarPronostico(requestDto));
    }

    @Operation(summary = "Calcular error de pronóstico", description = "Calcula las métricas de error (MAD, MSE, MAPE) comparando un pronóstico con las ventas reales.")
    @GetMapping("/pronostico/error")
    public ResponseEntity<PronosticoDtos.ErrorPronosticoDto> calcularErrorPronostico(
            @RequestParam Long productoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(inteligenciaService.calcularErrorDePronostico(productoId, fechaInicio, fechaFin));
    }

    @Operation(summary = "Calcular EOQ para un producto", description = "Calcula la Cantidad Económica de Pedido (EOQ) para optimizar costos de inventario.")
    @PostMapping("/analisis/eoq")
    public ResponseEntity<AnalisisDtos.EoqResponseDto> calcularEoq(@Valid @RequestBody AnalisisDtos.EoqRequestDto requestDto) {
        return ResponseEntity.ok(inteligenciaService.calcularEoq(requestDto));
    }

    @Operation(summary = "Generar clasificación ABC de productos", description = "Clasifica los productos en categorías A, B o C según su importancia en un período de tiempo determinado.")
    @GetMapping("/analisis/abc")
    public ResponseEntity<List<AnalisisDtos.AbcResponseDto>> generarClasificacionAbc(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(inteligenciaService.generarClasificacionAbc(fechaInicio, fechaFin));
    }

    @Operation(summary = "Listar alertas activas", description = "Devuelve todas las alertas del sistema que no han sido marcadas como leídas.")
    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaDtos.AlertaResponseDto>> getAlertasActivas() {
        return ResponseEntity.ok(inteligenciaService.getAlertasActivas());
    }

    @Operation(summary = "Marcar alerta como leída", description = "Marca una alerta específica como leída para que no aparezca en la lista de activas.")
    @PutMapping("/alertas/{id}/leida")
    public ResponseEntity<AlertaDtos.AlertaResponseDto> marcarAlertaComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok(inteligenciaService.marcarAlertaComoLeida(id));
    }
}
