package com.gomitas.controller;

import com.gomitas.dto.ProduccionDtos;
import com.gomitas.service.ProduccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produccion")
@RequiredArgsConstructor
@Tag(name = "Producción", description = "Endpoints para la gestión de órdenes de producción")
@SecurityRequirement(name = "bearerAuth")
public class ProduccionController {

    private final ProduccionService produccionService;

    @Operation(summary = "Listar todas las órdenes de producción")
    @GetMapping("/ordenes")
    public ResponseEntity<List<ProduccionDtos.OrdenProduccionResponseDto>> getAllOrdenes() {
        return ResponseEntity.ok(produccionService.getAllOrdenes());
    }

    @Operation(summary = "Obtener detalle de una orden de producción")
    @GetMapping("/ordenes/{id}")
    public ResponseEntity<ProduccionDtos.OrdenProduccionResponseDto> getOrdenById(@PathVariable Long id) {
        return ResponseEntity.ok(produccionService.getOrdenById(id));
    }

    @Operation(summary = "Crear una nueva orden de producción")
    @PostMapping("/ordenes")
    public ResponseEntity<ProduccionDtos.OrdenProduccionResponseDto> createOrdenProduccion(
            @Valid @RequestBody ProduccionDtos.CreateOrdenProduccionRequestDto requestDto,
            Authentication authentication) {
        ProduccionDtos.OrdenProduccionResponseDto createdOrden = produccionService.createOrdenProduccion(requestDto, authentication);
        return new ResponseEntity<>(createdOrden, HttpStatus.CREATED);
    }

    @Operation(summary = "Iniciar una orden de producción planificada")
    @PutMapping("/ordenes/{id}/iniciar")
    public ResponseEntity<ProduccionDtos.OrdenProduccionResponseDto> iniciarOrden(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(produccionService.iniciarOrden(id, authentication));
    }

    @Operation(summary = "Completar una orden de producción en proceso")
    @PutMapping("/ordenes/{id}/completar")
    public ResponseEntity<ProduccionDtos.OrdenProduccionResponseDto> completarOrden(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(produccionService.completarOrden(id, authentication));
    }

    @Operation(summary = "Cancelar una orden de producción planificada")
    @PutMapping("/ordenes/{id}/cancelar")
    public ResponseEntity<ProduccionDtos.OrdenProduccionResponseDto> cancelarOrden(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(produccionService.cancelarOrden(id, authentication));
    }

    @Operation(summary = "Ver la receta (insumos) de un producto")
    @GetMapping("/recetas/{productoId}")
    public ResponseEntity<ProduccionDtos.RecetaProductoResponseDto> getRecetaByProductoId(@PathVariable Long productoId) {
        return ResponseEntity.ok(produccionService.getRecetaByProductoId(productoId));
    }
}
