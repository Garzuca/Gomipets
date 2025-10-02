package com.gomitas.controller;

import com.gomitas.dto.ClienteDtos;
import com.gomitas.security.UserDetailsImpl;
import com.gomitas.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Endpoints para la gestión de clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(summary = "Listar todos los clientes", description = "Devuelve una lista de todos los clientes. Requiere rol de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping
    public ResponseEntity<List<ClienteDtos.ClienteResponseDto>> getAllClientes() {
        List<ClienteDtos.ClienteResponseDto> clientes = clienteService.getAllClientes();
        return ResponseEntity.ok(clientes);
    }

    @Operation(summary = "Obtener cliente por ID", description = "Devuelve la información de un cliente específico. Un administrador puede ver cualquier cliente, un cliente solo puede ver su propia información.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información del cliente obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDtos.ClienteResponseDto> getClienteById(@PathVariable Long id) {
        return clienteService.getClienteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar datos del cliente", description = "Actualiza la información de un cliente. Un cliente solo puede actualizar sus propios datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDtos.ClienteResponseDto> updateCliente(@PathVariable Long id, @Valid @RequestBody ClienteDtos.ClienteUpdateDto clienteUpdateDto) {
        try {
            ClienteDtos.ClienteResponseDto updatedCliente = clienteService.updateCliente(id, clienteUpdateDto);
            return ResponseEntity.ok(updatedCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Obtener mi información de cliente", description = "Devuelve la información del cliente asociado al usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información del cliente obtenida"),
            @ApiResponse(responseCode = "404", description = "No se encontró información de cliente para este usuario")
    })
    @GetMapping("/me")
    public ResponseEntity<ClienteDtos.ClienteResponseDto> getMyClienteInfo(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return clienteService.getClienteByUsuarioId(userDetails.getUsuarioId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
