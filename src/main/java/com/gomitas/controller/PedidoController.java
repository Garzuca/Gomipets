package com.gomitas.controller;

import com.gomitas.dto.PedidoDtos;
import com.gomitas.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Endpoints para la gestión de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;

    @Operation(summary = "Crear un nuevo pedido", description = "Crea un nuevo pedido para el cliente autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','CLIENTE')")
    public ResponseEntity<PedidoDtos.PedidoResponseDto> createPedido(@Valid @RequestBody PedidoDtos.CreatePedidoRequestDto pedidoDto, Authentication authentication) {
        PedidoDtos.PedidoResponseDto nuevoPedido = pedidoService.createPedido(pedidoDto, authentication);
        return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar todos los pedidos (admin)", description = "Devuelve una lista de todos los pedidos en el sistema. Requiere rol de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<PedidoDtos.PedidoResponseDto>> getAllPedidos() {
        return ResponseEntity.ok(pedidoService.findAll());
    }

    @Operation(summary = "Listar mis pedidos", description = "Devuelve una lista de todos los pedidos realizados por el cliente autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de mis pedidos obtenida")
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<PedidoDtos.PedidoResponseDto>> getCurrentUserPedidos(Authentication authentication) {
        return ResponseEntity.ok(pedidoService.findByCurrentUser(authentication));
    }

    @Operation(summary = "Obtener detalle de un pedido", description = "Devuelve la información de un pedido específico. Accesible por el dueño del pedido o un administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','CLIENTE')")
    public ResponseEntity<PedidoDtos.PedidoResponseDto> getPedidoById(@PathVariable Long id) {
        return pedidoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cambiar estado de un pedido (admin)", description = "Actualiza el estado de un pedido. Requiere rol de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del pedido actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PedidoDtos.PedidoResponseDto> updatePedidoStatus(@PathVariable Long id, @Valid @RequestBody PedidoDtos.UpdatePedidoStatusDto statusDto) {
        try {
            PedidoDtos.PedidoResponseDto updatedPedido = pedidoService.updatePedidoStatus(id, statusDto.estado());
            return ResponseEntity.ok(updatedPedido);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Listar pedidos de un cliente específico (admin)", description = "Devuelve todos los pedidos de un cliente por su ID. Requiere rol de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos del cliente obtenidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<PedidoDtos.PedidoResponseDto>> getPedidosByClienteId(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoService.findByClienteId(clienteId));
    }

    @Operation(summary = "Despachar un pedido (admin)", description = "Cambia el estado de un pedido a 'Despachado' y descuenta los productos del inventario. Requiere rol de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido despachado y stock actualizado"),
            @ApiResponse(responseCode = "400", description = "El pedido no puede ser despachado o stock insuficiente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @PostMapping("/{id}/despachar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PedidoDtos.PedidoResponseDto> despacharPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.despacharPedido(id));
    }
}
