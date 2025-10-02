package com.gomitas.service.impl;

import com.gomitas.dto.InventarioDtos;
import com.gomitas.dto.ProduccionDtos;
import com.gomitas.entity.*;
import com.gomitas.enums.EstadoProduccion;
import com.gomitas.enums.TipoMovimientoInventario;
import com.gomitas.exception.BadRequestException;
import com.gomitas.exception.ResourceNotFoundException;
import com.gomitas.repository.*;
import com.gomitas.security.UserDetailsImpl;
import com.gomitas.service.InventarioService;
import com.gomitas.service.ProduccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProduccionServiceImpl implements ProduccionService {

    private final OrdenProduccionRepository ordenProduccionRepository;
    private final ProductoRepository productoRepository;
    private final InsumoRepository insumoRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final DetalleProduccionRepository detalleProduccionRepository;
    private final InventarioService inventarioService;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProduccionDtos.OrdenProduccionResponseDto> getAllOrdenes() {
        return ordenProduccionRepository.findAll().stream()
                .map(this::mapToOrdenProduccionResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProduccionDtos.OrdenProduccionResponseDto getOrdenById(Long id) {
        OrdenProduccion orden = findOrdenById(id);
        return mapToOrdenProduccionResponseDto(orden);
    }

    @Override
    @Transactional
    public ProduccionDtos.OrdenProduccionResponseDto createOrdenProduccion(ProduccionDtos.CreateOrdenProduccionRequestDto requestDto, Authentication authentication) {
        Producto producto = findProductoById(requestDto.productoId());
        List<ProductoInsumo> receta = productoInsumoRepository.findByProducto(producto);

        if (receta.isEmpty()) {
            throw new BadRequestException("El producto no tiene una receta de insumos definida.");
        }

        // Verificar stock de insumos
        for (ProductoInsumo itemReceta : receta) {
            Insumo insumo = itemReceta.getInsumo();
            BigDecimal cantidadRequerida = itemReceta.getCantidadRequerida().multiply(BigDecimal.valueOf(requestDto.cantidadPlanificada()));
            if (insumo.getCantidadStock().compareTo(cantidadRequerida) < 0) {
                throw new BadRequestException("Stock insuficiente para el insumo: " + insumo.getNombre());
            }
        }

        Usuario usuario = getCurrentUsuario(authentication);
        OrdenProduccion orden = OrdenProduccion.builder()
                .producto(producto)
                .cantidadPlanificada(requestDto.cantidadPlanificada())
                .estado(EstadoProduccion.PLANIFICADA)
                .usuario(usuario)
                .observaciones(requestDto.observaciones())
                .build();

        OrdenProduccion savedOrden = ordenProduccionRepository.save(orden);

        // Crear detalles de producción
        receta.forEach(itemReceta -> {
            DetalleProduccion detalle = DetalleProduccion.builder()
                    .ordenProduccion(savedOrden)
                    .insumo(itemReceta.getInsumo())
                    .cantidadRequerida(itemReceta.getCantidadRequerida().multiply(BigDecimal.valueOf(requestDto.cantidadPlanificada())))
                    .cantidadUtilizada(BigDecimal.ZERO)
                    .build();
            detalleProduccionRepository.save(detalle);
        });

        return mapToOrdenProduccionResponseDto(savedOrden);
    }

    @Override
    @Transactional
    public ProduccionDtos.OrdenProduccionResponseDto iniciarOrden(Long id, Authentication authentication) {
        OrdenProduccion orden = findOrdenById(id);
        if (orden.getEstado() != EstadoProduccion.PLANIFICADA) {
            throw new BadRequestException("Solo se pueden iniciar órdenes en estado 'Planificada'.");
        }
        orden.setEstado(EstadoProduccion.EN_PROCESO);
        orden.setFechaInicio(LocalDateTime.now());
        return mapToOrdenProduccionResponseDto(ordenProduccionRepository.save(orden));
    }

    @Override
    @Transactional
    public ProduccionDtos.OrdenProduccionResponseDto completarOrden(Long id, Authentication authentication) {
        OrdenProduccion orden = findOrdenById(id);
        if (orden.getEstado() != EstadoProduccion.EN_PROCESO) {
            throw new BadRequestException("Solo se pueden completar órdenes en estado 'En Proceso'.");
        }

        List<DetalleProduccion> detalles = detalleProduccionRepository.findByOrdenProduccion(orden);

        // Descontar insumos del stock
        for (DetalleProduccion detalle : detalles) {
            Insumo insumo = detalle.getInsumo();
            BigDecimal cantidadRequerida = detalle.getCantidadRequerida();
            if (insumo.getCantidadStock().compareTo(cantidadRequerida) < 0) {
                throw new BadRequestException("Stock insuficiente para el insumo: " + insumo.getNombre() + " al momento de completar.");
            }
            insumo.setCantidadStock(insumo.getCantidadStock().subtract(cantidadRequerida));
            detalle.setCantidadUtilizada(cantidadRequerida);
            insumoRepository.save(insumo);
            detalleProduccionRepository.save(detalle);
        }

        // Incrementar stock del producto terminado
        InventarioDtos.MovimientoRequestDto movimientoDto = new InventarioDtos.MovimientoRequestDto(
                orden.getProducto().getProductoId(),
                TipoMovimientoInventario.ENTRADA,
                orden.getCantidadPlanificada(),
                "Entrada por producción, orden #" + orden.getOrdenId()
        );
        inventarioService.registrarMovimiento(movimientoDto, authentication);

        orden.setEstado(EstadoProduccion.COMPLETADA);
        orden.setFechaFin(LocalDateTime.now());
        orden.setCantidadProducida(orden.getCantidadPlanificada());

        return mapToOrdenProduccionResponseDto(ordenProduccionRepository.save(orden));
    }

    @Override
    @Transactional
    public ProduccionDtos.OrdenProduccionResponseDto cancelarOrden(Long id, Authentication authentication) {
        OrdenProduccion orden = findOrdenById(id);
        if (orden.getEstado() != EstadoProduccion.PLANIFICADA) {
            throw new BadRequestException("Solo se pueden cancelar órdenes en estado 'Planificada'.");
        }
        orden.setEstado(EstadoProduccion.CANCELADA);
        orden.setFechaFin(LocalDateTime.now()); // Se usa fecha de fin para marcar la cancelación
        return mapToOrdenProduccionResponseDto(ordenProduccionRepository.save(orden));
    }

    @Override
    @Transactional(readOnly = true)
    public ProduccionDtos.RecetaProductoResponseDto getRecetaByProductoId(Long productoId) {
        Producto producto = findProductoById(productoId);
        List<ProductoInsumo> receta = productoInsumoRepository.findByProducto(producto);

        List<ProduccionDtos.RecetaInsumoDto> insumosDto = receta.stream()
                .map(item -> new ProduccionDtos.RecetaInsumoDto(
                        item.getInsumo().getInsumoId(),
                        item.getInsumo().getNombre(),
                        item.getCantidadRequerida(),
                        item.getUnidad()
                ))
                .collect(Collectors.toList());

        return new ProduccionDtos.RecetaProductoResponseDto(
                producto.getProductoId(),
                producto.getNombre(),
                insumosDto
        );
    }

    private OrdenProduccion findOrdenById(Long id) {
        return ordenProduccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de producción no encontrada con ID: " + id));
    }

    private Producto findProductoById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    }

    private Usuario getCurrentUsuario(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return usuarioRepository.findById(userDetails.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private ProduccionDtos.OrdenProduccionResponseDto mapToOrdenProduccionResponseDto(OrdenProduccion orden) {
        List<DetalleProduccion> detalles = detalleProduccionRepository.findByOrdenProduccion(orden);
        List<ProduccionDtos.DetalleProduccionResponseDto> detallesDto = detalles.stream()
                .map(detalle -> new ProduccionDtos.DetalleProduccionResponseDto(
                        detalle.getDetalleId(),
                        detalle.getInsumo().getInsumoId(),
                        detalle.getInsumo().getNombre(),
                        detalle.getCantidadRequerida(),
                        detalle.getCantidadUtilizada()
                ))
                .collect(Collectors.toList());

        return new ProduccionDtos.OrdenProduccionResponseDto(
                orden.getOrdenId(),
                orden.getProducto().getProductoId(),
                orden.getProducto().getNombre(),
                orden.getCantidadPlanificada(),
                orden.getCantidadProducida(),
                orden.getFechaCreacion(),
                orden.getFechaInicio(),
                orden.getFechaFin(),
                orden.getEstado(),
                orden.getObservaciones(),
                detallesDto
        );
    }
}