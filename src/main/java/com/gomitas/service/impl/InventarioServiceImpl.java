package com.gomitas.service.impl;

import com.gomitas.dto.InventarioDtos;
import com.gomitas.entity.InventarioProducto;
import com.gomitas.entity.MovimientoInventario;
import com.gomitas.entity.Producto;
import com.gomitas.entity.Usuario;
import com.gomitas.enums.TipoMovimientoInventario;
import com.gomitas.exception.BadRequestException;
import com.gomitas.exception.ResourceNotFoundException;
import com.gomitas.repository.InventarioProductoRepository;
import com.gomitas.repository.MovimientoInventarioRepository;
import com.gomitas.repository.ProductoRepository;
import com.gomitas.repository.UsuarioRepository;
import com.gomitas.security.UserDetailsImpl;
import com.gomitas.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioProductoRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDtos.InventarioResponseDto> getEstadoInventario() {
        return inventarioRepository.findAll().stream()
                .map(this::mapToInventarioDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioDtos.InventarioResponseDto getInventarioPorProductoId(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));
        return inventarioRepository.findByProducto(producto)
                .map(this::mapToInventarioDto)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado para el producto con id: " + productoId));
    }

    @Override
    @Transactional
    public InventarioDtos.MovimientoResponseDto registrarMovimiento(InventarioDtos.MovimientoRequestDto movimientoDto, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Usuario usuario = usuarioRepository.findById(userDetails.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Producto producto = productoRepository.findById(movimientoDto.productoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + movimientoDto.productoId()));

        InventarioProducto inventario = inventarioRepository.findByProducto(producto)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado para el producto."));

        Integer cantidadAnterior = inventario.getCantidadDisponible();
        Integer cantidadNueva;

        if (movimientoDto.tipoMovimiento() == TipoMovimientoInventario.ENTRADA) {
            cantidadNueva = cantidadAnterior + movimientoDto.cantidad();
        } else if (movimientoDto.tipoMovimiento() == TipoMovimientoInventario.SALIDA) {
            if (cantidadAnterior < movimientoDto.cantidad()) {
                throw new BadRequestException("No hay suficiente stock para realizar la salida.");
            }
            cantidadNueva = cantidadAnterior - movimientoDto.cantidad();
        } else { // AJUSTE
            cantidadNueva = movimientoDto.cantidad();
        }

        inventario.setCantidadDisponible(cantidadNueva);
        inventarioRepository.save(inventario);

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .tipoMovimiento(movimientoDto.tipoMovimiento())
                .cantidad(movimientoDto.cantidad())
                .cantidadAnterior(cantidadAnterior)
                .cantidadNueva(cantidadNueva)
                .motivo(movimientoDto.motivo())
                .usuario(usuario)
                .build();

        MovimientoInventario savedMovimiento = movimientoRepository.save(movimiento);
        return mapToMovimientoDto(savedMovimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDtos.InventarioResponseDto> getProductosConStockBajo() {
        return inventarioRepository.findAll().stream()
                .filter(inv -> inv.getCantidadDisponible() < inv.getProducto().getStockMinimo())
                .map(this::mapToInventarioDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hayStockSuficiente(Long productoId, Integer cantidadRequerida) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));
        InventarioProducto inventario = inventarioRepository.findByProducto(producto)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado para el producto con id: " + productoId));
        return inventario.getCantidadDisponible() >= cantidadRequerida;
    }

    private InventarioDtos.InventarioResponseDto mapToInventarioDto(InventarioProducto inventario) {
        return new InventarioDtos.InventarioResponseDto(
                inventario.getInventarioId(),
                inventario.getProducto().getProductoId(),
                inventario.getProducto().getNombre(),
                inventario.getCantidadDisponible(),
                inventario.getFechaActualizacion()
        );
    }

    private InventarioDtos.MovimientoResponseDto mapToMovimientoDto(MovimientoInventario movimiento) {
        return new InventarioDtos.MovimientoResponseDto(
                movimiento.getMovimientoId(),
                movimiento.getProducto().getProductoId(),
                movimiento.getProducto().getNombre(),
                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),
                movimiento.getCantidadAnterior(),
                movimiento.getCantidadNueva(),
                movimiento.getFecha(),
                movimiento.getMotivo(),
                movimiento.getUsuario() != null ? movimiento.getUsuario().getNombreUsuario() : "Sistema"
        );
    }
}