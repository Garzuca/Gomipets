package com.gomitas.service.impl;

import com.gomitas.dto.InventarioDtos;
import com.gomitas.dto.PedidoDtos;
import com.gomitas.entity.*;
import com.gomitas.enums.TipoMovimientoInventario;
import com.gomitas.exception.BadRequestException;
import com.gomitas.exception.ResourceNotFoundException;
import com.gomitas.repository.ClienteRepository;
import com.gomitas.repository.PedidoRepository;
import com.gomitas.repository.ProductoRepository;
import com.gomitas.repository.VentasHistoricasRepository;
import com.gomitas.security.UserDetailsImpl;
import com.gomitas.service.InventarioService;
import com.gomitas.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;
    private final VentasHistoricasRepository ventasHistoricasRepository;

    @Override
    @Transactional
    public PedidoDtos.PedidoResponseDto createPedido(PedidoDtos.CreatePedidoRequestDto pedidoDto, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Cliente cliente = clienteRepository.findByUsuarioIdWithUsuario(userDetails.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para el usuario autenticado."));

        // 1. Verificar stock de todos los productos ANTES de hacer cambios
        for (PedidoDtos.CreatePedidoItemDto itemDto : pedidoDto.items()) {
            inventarioService.getInventarioPorProductoId(itemDto.productoId()); // Lanza excepción si no existe
            if (!inventarioService.hayStockSuficiente(itemDto.productoId(), itemDto.cantidad())) {
                throw new BadRequestException("Stock insuficiente para el producto con ID: " + itemDto.productoId());
            }
        }

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .estado("Pendiente")
                .metodoPago(pedidoDto.metodoPago())
                .observaciones(pedidoDto.observaciones())
                .detalles(new ArrayList<>())
                .build();

        BigDecimal totalPedido = BigDecimal.ZERO;

        // 2. Ahora que sabemos que hay stock, procesamos el pedido
        for (PedidoDtos.CreatePedidoItemDto itemDto : pedidoDto.items()) {
            Producto producto = productoRepository.findById(itemDto.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + itemDto.productoId()));

            // Descontar stock
            InventarioDtos.MovimientoRequestDto movimientoDto = new InventarioDtos.MovimientoRequestDto(
                    producto.getProductoId(),
                    TipoMovimientoInventario.SALIDA,
                    itemDto.cantidad(),
                    "Venta - Pedido #" + pedido.getPedidoId() // Se asignará el ID después de guardar
            );
            inventarioService.registrarMovimiento(movimientoDto, authentication);

            BigDecimal subtotal = producto.getPrecioUnitario().multiply(new BigDecimal(itemDto.cantidad()));
            totalPedido = totalPedido.add(subtotal);

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(itemDto.cantidad())
                    .precioUnitario(producto.getPrecioUnitario())
                    .subtotal(subtotal)
                    .build();
            pedido.getDetalles().add(detalle);
        }

        pedido.setTotal(totalPedido);
        Pedido savedPedido = pedidoRepository.save(pedido);
        return mapToDto(savedPedido);
    }

    @Override
    @Transactional
    public PedidoDtos.PedidoResponseDto updatePedidoStatus(Long id, String estado) {
        Pedido pedido = pedidoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));

        // Lógica para registrar ventas históricas cuando el pedido se completa
        if ("Entregado".equalsIgnoreCase(estado) && !"Entregado".equalsIgnoreCase(pedido.getEstado())) {
            registrarVentasHistoricas(pedido);
        }

        pedido.setEstado(estado);
        Pedido updatedPedido = pedidoRepository.save(pedido);
        return mapToDto(updatedPedido);
    }

    private void registrarVentasHistoricas(Pedido pedido) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            VentasHistoricas venta = VentasHistoricas.builder()
                    .producto(detalle.getProducto())
                    .fechaVenta(LocalDate.now())
                    .cantidadVendida(detalle.getCantidad())
                    .precioVenta(detalle.getPrecioUnitario())
                    .pedido(pedido)
                    .build();
            ventasHistoricasRepository.save(venta);
        }
    }

    // --- Otros métodos (findAll, findById, etc.) sin cambios ---
    @Override
    @Transactional(readOnly = true)
    public List<PedidoDtos.PedidoResponseDto> findAll() {
        return pedidoRepository.findAllWithDetails().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PedidoDtos.PedidoResponseDto> findById(Long id) {
        return pedidoRepository.findByIdWithDetails(id).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDtos.PedidoResponseDto> findByClienteId(Long clienteId) {
        return pedidoRepository.findByCliente_ClienteIdWithDetails(clienteId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDtos.PedidoResponseDto> findByCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return pedidoRepository.findByCliente_Usuario_UsuarioIdWithDetails(userDetails.getUsuarioId()).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private PedidoDtos.PedidoResponseDto mapToDto(Pedido pedido) {
        List<PedidoDtos.DetallePedidoResponseDto> detallesDto = pedido.getDetalles().stream()
                .map(detalle -> new PedidoDtos.DetallePedidoResponseDto(
                        detalle.getDetalleId(),
                        detalle.getProducto().getProductoId(),
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                )).collect(Collectors.toList());

        return new PedidoDtos.PedidoResponseDto(
                pedido.getPedidoId(),
                pedido.getCliente().getClienteId(),
                pedido.getCliente().getNombre(),
                pedido.getFechaPedido(),
                pedido.getEstado(),
                pedido.getMetodoPago(),
                pedido.getTotal(),
                pedido.getObservaciones(),
                detallesDto
        );
    }
}