package com.gomitas.service.impl;

import com.gomitas.dto.PedidoDtos;
import com.gomitas.entity.*;
import com.gomitas.enums.PrioridadAlerta;
import com.gomitas.enums.TipoAlerta;
import com.gomitas.exception.BadRequestException;
import com.gomitas.exception.ResourceNotFoundException;
import com.gomitas.repository.*;
import com.gomitas.security.UserDetailsImpl;
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
    private final VentasHistoricasRepository ventasHistoricasRepository;
    private final InventarioProductoRepository inventarioProductoRepository;
    private final AlertaRepository alertaRepository;

    @Override
    @Transactional
    public PedidoDtos.PedidoResponseDto createPedido(PedidoDtos.CreatePedidoRequestDto pedidoDto, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Cliente cliente = clienteRepository.findByUsuarioIdWithUsuario(userDetails.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para el usuario autenticado."));

        String estadoPedido = "Pendiente";

        // 1. Verificar stock y generar alertas si es necesario
        for (PedidoDtos.CreatePedidoItemDto itemDto : pedidoDto.items()) {
            Producto producto = productoRepository.findById(itemDto.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + itemDto.productoId()));

            InventarioProducto inventario = inventarioProductoRepository.findByProducto(producto)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado para el producto con ID: " + itemDto.productoId()));

            if (inventario.getCantidadDisponible() < itemDto.cantidad()) {
                estadoPedido = "Pendiente por Stock";
                
                int cantidadFaltante = itemDto.cantidad() - inventario.getCantidadDisponible();

                Alerta alerta = Alerta.builder()
                        .tipo(TipoAlerta.NECESIDAD_PRODUCCION)
                        .mensaje("Stock insuficiente para '" + producto.getNombre() + "'. Se necesitan " + cantidadFaltante + " unidades para el pedido.")
                        .prioridad(PrioridadAlerta.ALTA)
                        .leida(false)
                        .build();
                alertaRepository.save(alerta);
            }
        }

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .estado(estadoPedido)
                .metodoPago(pedidoDto.metodoPago())
                .observaciones(pedidoDto.observaciones())
                .detalles(new ArrayList<>())
                .build();

        BigDecimal totalPedido = BigDecimal.ZERO;

        // 2. Construir el pedido
        for (PedidoDtos.CreatePedidoItemDto itemDto : pedidoDto.items()) {
            Producto producto = productoRepository.findById(itemDto.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + itemDto.productoId()));

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
    public PedidoDtos.PedidoResponseDto despacharPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findByIdWithDetails(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));

        if (!pedido.getEstado().equalsIgnoreCase("Pendiente") && !pedido.getEstado().equalsIgnoreCase("Pendiente por Stock")) {
            throw new BadRequestException("Solo se pueden despachar pedidos en estado 'Pendiente' o 'Pendiente por Stock'.");
        }

        // Descontar cada producto del inventario
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            int cantidadVendida = detalle.getCantidad();

            InventarioProducto inventarioProducto = inventarioProductoRepository.findByProducto(producto)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado para el producto: " + producto.getNombre()));

            if (inventarioProducto.getCantidadDisponible() < cantidadVendida) {
                throw new BadRequestException("Stock insuficiente para el producto '" + producto.getNombre() + "'. Requerido: " + cantidadVendida + ", Disponible: " + inventarioProducto.getCantidadDisponible());
            }

            inventarioProducto.setCantidadDisponible(inventarioProducto.getCantidadDisponible() - cantidadVendida);
            inventarioProductoRepository.save(inventarioProducto);
        }

        pedido.setEstado("Despachado");
        Pedido pedidoDespachado = pedidoRepository.save(pedido);

        return mapToDto(pedidoDespachado);
    }
    
    @Override
    @Transactional
    public PedidoDtos.PedidoResponseDto updatePedidoStatus(Long id, String estado) {
        Pedido pedido = pedidoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));

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