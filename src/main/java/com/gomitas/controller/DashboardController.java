package com.gomitas.controller;

import com.gomitas.dto.DashboardDtos;
import com.gomitas.service.ScheduledTasksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints para obtener métricas clave del sistema")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final ScheduledTasksService scheduledTasksService;

    @Operation(summary = "Obtener métricas del dashboard", description = "Devuelve un resumen de las métricas más importantes para el administrador.")
    @GetMapping("/metrics")
    public ResponseEntity<DashboardDtos.MetricasResponseDto> getDashboardMetrics() {
        return ResponseEntity.ok(scheduledTasksService.getDashboardMetrics());
    }
}
