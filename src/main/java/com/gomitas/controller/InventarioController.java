package com.gomitas.controller;

import com.gomitas.dto.InventarioDtos;
import com.gomitas.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "Endpoints para la gestión de inventario de productos")
@SecurityRequirement(name = "bearerAuth")
public class InventarioController {

    private final InventarioService inventarioService;

    @Operation(summary = "Consultar estado del inventario", description = "Devuelve el estado actual del inventario de todos los productos.")
    @GetMapping("/productos")
    public ResponseEntity<List<InventarioDtos.InventarioResponseDto>> getEstadoInventario() {
        return ResponseEntity.ok(inventarioService.getEstadoInventario());
    }

    @Operation(summary = "Registrar movimiento de inventario", description = "Registra una entrada, salida o ajuste de stock para un producto.")
    @PostMapping("/productos/movimiento")
    public ResponseEntity<InventarioDtos.MovimientoResponseDto> registrarMovimiento(
            @Valid @RequestBody InventarioDtos.MovimientoRequestDto movimientoDto,
            Authentication authentication) {
        return ResponseEntity.ok(inventarioService.registrarMovimiento(movimientoDto, authentication));
    }

    @Operation(summary = "Consultar productos con stock bajo", description = "Devuelve una lista de productos cuyo stock disponible es inferior al stock mínimo.")
    @GetMapping("/alertas/stock-bajo")
    public ResponseEntity<List<InventarioDtos.InventarioResponseDto>> getProductosConStockBajo() {
        return ResponseEntity.ok(inventarioService.getProductosConStockBajo());
    }
}
