package com.gomitas.service.impl;

import com.gomitas.dto.ProductoDtos;
import com.gomitas.entity.InventarioProducto;
import com.gomitas.entity.Producto;
import com.gomitas.exception.ResourceNotFoundException;
import com.gomitas.repository.InventarioProductoRepository;
import com.gomitas.repository.ProductoRepository;
import com.gomitas.entity.Insumo;
import com.gomitas.entity.ProductoInsumo;
import com.gomitas.repository.InsumoRepository;
import com.gomitas.repository.ProductoInsumoRepository;
import com.gomitas.service.ProductoService;
import com.gomitas.service.impl.util.CloudinaryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final InventarioProductoRepository inventarioProductoRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final InsumoRepository insumoRepository;
    private final CloudinaryServiceImpl cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDtos.ProductoResponseDto> findAll() {
        return productoRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDtos.ProductoResponseDto> findAllActive() {
        return productoRepository.findByEstadoTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoDtos.ProductoResponseDto> findById(Long id) {
        return productoRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional
    public ProductoDtos.ProductoResponseDto save(ProductoDtos.CreateProductoRequestDto productoDto, MultipartFile imagen) throws IOException {
        Producto producto = Producto.builder()
                .nombre(productoDto.nombre())
                .descripcion(productoDto.descripcion())
                .precioUnitario(productoDto.precioUnitario())
                .stockMinimo(productoDto.stockMinimo() != null ? productoDto.stockMinimo() : 0)
                .estado(true)
                .build();

        if (imagen != null && !imagen.isEmpty()) {
            Map<?, ?> uploadResult = cloudinaryService.uploadFile(imagen, "gomipets/productos");
            String imageUrl = (String) uploadResult.get("secure_url");
            producto.setImageUrl(imageUrl);
        }

        Producto savedProducto = productoRepository.save(producto);

        InventarioProducto inventario = InventarioProducto.builder()
                .producto(savedProducto)
                .cantidadDisponible(0)
                .build();
        inventarioProductoRepository.save(inventario);

        return mapToDto(savedProducto);
    }

    @Override
    @Transactional
    public ProductoDtos.ProductoResponseDto update(Long id, ProductoDtos.UpdateProductoRequestDto productoDto, MultipartFile imagen) throws IOException {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));

        if (imagen != null && !imagen.isEmpty()) {
            // Borrar imagen anterior solo si existe una URL y se puede extraer un publicId válido
            if (producto.getImageUrl() != null && !producto.getImageUrl().isEmpty()) {
                String publicId = cloudinaryService.getPublicIdFromUrl(producto.getImageUrl());
                if (publicId != null && !publicId.isEmpty()) {
                    try {
                        cloudinaryService.deleteFile(publicId);
                    } catch (IOException e) {
                        // Opcional: Loggear el error si la eliminación falla, pero no detener el proceso
                        System.err.println("Error al eliminar la imagen anterior de Cloudinary: " + e.getMessage());
                    }
                }
            }
            // Subir nueva imagen
            Map<?, ?> uploadResult = cloudinaryService.uploadFile(imagen, "gomipets/productos");
            String newImageUrl = (String) uploadResult.get("secure_url");
            producto.setImageUrl(newImageUrl);
        }

        producto.setNombre(productoDto.nombre());
        producto.setDescripcion(productoDto.descripcion());
        producto.setPrecioUnitario(productoDto.precioUnitario());
        producto.setStockMinimo(productoDto.stockMinimo());
        producto.setEstado(productoDto.estado());

        Producto updatedProducto = productoRepository.save(producto);
        return mapToDto(updatedProducto);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        producto.setEstado(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDtos.RecetaResponseDto> getRecetaByProductoId(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto no encontrado con id: " + productoId);
        }
        List<ProductoInsumo> receta = productoInsumoRepository.findByProducto_ProductoId(productoId);
        return receta.stream()
                .map(this::mapProductoInsumoToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ProductoDtos.RecetaResponseDto> addRecetaToProducto(Long productoId, ProductoDtos.CreateRecetaRequestDto recetaDto) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        // Borra la receta anterior para asegurar que la nueva sea la única
        productoInsumoRepository.deleteByProducto_ProductoId(productoId);

        List<ProductoInsumo> nuevaReceta = recetaDto.insumos().stream().map(insumoDto -> {
            Insumo insumo = insumoRepository.findById(insumoDto.insumoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + insumoDto.insumoId()));

            return ProductoInsumo.builder()
                    .producto(producto)
                    .insumo(insumo)
                    .cantidadRequerida(insumoDto.cantidadRequerida())
                    .unidad(insumo.getUnidadMedida()) // Asigna la unidad del insumo
                    .build();
        }).collect(Collectors.toList());

        List<ProductoInsumo> savedReceta = productoInsumoRepository.saveAll(nuevaReceta);

        return savedReceta.stream()
                .map(this::mapProductoInsumoToDto)
                .collect(Collectors.toList());
    }

    private ProductoDtos.ProductoResponseDto mapToDto(Producto producto) {
        return new ProductoDtos.ProductoResponseDto(
                producto.getProductoId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecioUnitario(),
                producto.getImageUrl(),
                producto.getStockMinimo(),
                producto.getFechaCreacion(),
                producto.getEstado()
        );
    }

    private ProductoDtos.RecetaResponseDto mapProductoInsumoToDto(ProductoInsumo productoInsumo) {
        return new ProductoDtos.RecetaResponseDto(
                productoInsumo.getInsumo().getInsumoId(),
                productoInsumo.getInsumo().getNombre(),
                productoInsumo.getCantidadRequerida(),
                productoInsumo.getUnidad()
        );
    }
}
