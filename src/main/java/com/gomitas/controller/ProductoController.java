package com.gomitas.controller;

import com.gomitas.dto.ProductoDtos;
import com.gomitas.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Endpoints para la gestión de productos")
public class ProductoController {

    private final ProductoService productoService;

    @Operation(summary = "Catálogo de productos activos", description = "Devuelve una lista de todos los productos con estado activo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catálogo obtenido exitosamente")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','CLIENTE')")
    public ResponseEntity<List<ProductoDtos.ProductoResponseDto>> getActiveProductos() {
        return ResponseEntity.ok(productoService.findAllActive());
    }
    
    @Operation(summary = "Listar todos los productos (admin)", description = "Devuelve una lista de todos los productos, incluyendo activos e inactivos. Requiere rol de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista completa de productos obtenida"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/all")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<ProductoDtos.ProductoResponseDto>> getAllProductos() {
        return ResponseEntity.ok(productoService.findAll());
    }

    @Operation(summary = "Obtener detalle de producto", description = "Devuelve la información de un producto específico por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','CLIENTE')")
    public ResponseEntity<ProductoDtos.ProductoResponseDto> getProductoById(@PathVariable Long id) {
        return productoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un nuevo producto", description = "Crea un nuevo producto en el catálogo. Requiere rol de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ProductoDtos.ProductoResponseDto> createProducto(
            @Valid @RequestPart("producto") ProductoDtos.CreateProductoRequestDto productoDto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws IOException {
        ProductoDtos.ProductoResponseDto savedProducto = productoService.save(productoDto, imagen);
        return new ResponseEntity<>(savedProducto, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar un producto", description = "Actualiza la información de un producto existente. Requiere rol de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ProductoDtos.ProductoResponseDto> updateProducto(
            @PathVariable Long id,
            @Valid @RequestPart("producto") ProductoDtos.UpdateProductoRequestDto productoDto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws IOException {
        try {
            ProductoDtos.ProductoResponseDto updatedProducto = productoService.update(id, productoDto, imagen);
            return ResponseEntity.ok(updatedProducto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Desactivar un producto", description = "Realiza un borrado lógico del producto (cambia su estado a inactivo). Requiere rol de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto desactivado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        try {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoints para Recetas
    @Operation(summary = "Obtener la receta de un producto", description = "Devuelve la lista de insumos y cantidades requeridas para fabricar un producto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receta obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}/receta")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','CLIENTE')")
    public ResponseEntity<List<ProductoDtos.RecetaResponseDto>> getReceta(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.getRecetaByProductoId(id));
    }

    @Operation(summary = "Crear o actualizar la receta de un producto", description = "Establece la lista de insumos para un producto. Si ya existe una receta, será reemplazada. Requiere rol de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Receta guardada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Producto o insumo no encontrado")
    })
    @PostMapping("/{id}/receta")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<ProductoDtos.RecetaResponseDto>> createOrUpdateReceta(
            @PathVariable Long id,
            @Valid @RequestBody ProductoDtos.CreateRecetaRequestDto recetaDto) {
        List<ProductoDtos.RecetaResponseDto> savedReceta = productoService.addRecetaToProducto(id, recetaDto);
        return new ResponseEntity<>(savedReceta, HttpStatus.CREATED);
    }
}
