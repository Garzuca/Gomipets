package com.gomitas.service.impl;

import com.gomitas.dto.ProductoDtos;
import com.gomitas.entity.InventarioProducto;
import com.gomitas.entity.Producto;
import com.gomitas.exception.ResourceNotFoundException;
import com.gomitas.repository.InventarioProductoRepository;
import com.gomitas.repository.ProductoRepository;
import com.gomitas.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final InventarioProductoRepository inventarioProductoRepository;

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
    public ProductoDtos.ProductoResponseDto save(ProductoDtos.CreateProductoRequestDto productoDto) {
        Producto producto = Producto.builder()
                .nombre(productoDto.nombre())
                .descripcion(productoDto.descripcion())
                .precioUnitario(productoDto.precioUnitario())
                .stockMinimo(productoDto.stockMinimo() != null ? productoDto.stockMinimo() : 0)
                .estado(true)
                .build();
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
    public ProductoDtos.ProductoResponseDto update(Long id, ProductoDtos.UpdateProductoRequestDto productoDto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));

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

    private ProductoDtos.ProductoResponseDto mapToDto(Producto producto) {
        return new ProductoDtos.ProductoResponseDto(
                producto.getProductoId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecioUnitario(),
                producto.getStockMinimo(),
                producto.getFechaCreacion(),
                producto.getEstado()
        );
    }
}
