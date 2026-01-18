package com.gomitas.controller;

import com.gomitas.dto.InsumoDtos;
import com.gomitas.service.InsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insumos")
@RequiredArgsConstructor
@Tag(name = "Insumos", description = "Endpoints para la gestión de insumos, materias primas y sus lotes.")
@SecurityRequirement(name = "bearerAuth")
public class InsumoController {

    private final InsumoService insumoService;

    @Operation(summary = "Listar todos los insumos con su stock total y lotes")
    @GetMapping
    public ResponseEntity<List<InsumoDtos.InsumoResponseDto>> getAllInsumos() {
        return ResponseEntity.ok(insumoService.getAllInsumos());
    }

    @Operation(summary = "Obtener un insumo por ID con su stock total y lotes")
    @GetMapping("/{id}")
    public ResponseEntity<InsumoDtos.InsumoResponseDto> getInsumoById(@PathVariable Long id) {
        return ResponseEntity.ok(insumoService.getInsumoById(id));
    }

    @Operation(summary = "Crear un nuevo insumo junto con su primer lote")
    @PostMapping
    public ResponseEntity<InsumoDtos.InsumoResponseDto> createInsumo(@Valid @RequestBody InsumoDtos.CreateInsumoRequestDto insumoDto) {
        InsumoDtos.InsumoResponseDto createdInsumo = insumoService.createInsumo(insumoDto);
        return new ResponseEntity<>(createdInsumo, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar los datos generales de un insumo (no el stock)")
    @PutMapping("/{id}")
    public ResponseEntity<InsumoDtos.InsumoResponseDto> updateInsumo(@PathVariable Long id, @Valid @RequestBody InsumoDtos.UpdateInsumoRequestDto insumoDto) {
        return ResponseEntity.ok(insumoService.updateInsumo(id, insumoDto));
    }

    @Operation(summary = "Agregar un nuevo lote de stock a un insumo existente")
    @PostMapping("/lotes")
    public ResponseEntity<InsumoDtos.LoteDto> addLoteToInsumo(@Valid @RequestBody InsumoDtos.AddLoteRequestDto loteDto) {
        InsumoDtos.LoteDto nuevoLote = insumoService.addLoteToInsumo(loteDto);
        return new ResponseEntity<>(nuevoLote, HttpStatus.CREATED);
    }
}