package com.gomitas.service;

import com.gomitas.dto.DashboardDtos;
import com.gomitas.entity.Alerta;
import com.gomitas.entity.Insumo;
import com.gomitas.entity.InventarioProducto;
import com.gomitas.entity.Pedido;
import com.gomitas.enums.PrioridadAlerta;
import com.gomitas.enums.TipoAlerta;
import com.gomitas.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledTasksService {

    private final InventarioProductoRepository inventarioRepository;
    private final InsumoRepository insumoRepository;
    private final AlertaRepository alertaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    @Scheduled(cron = "0 0 * * * *") // cada hora
    public void checkForStockBajo() {
        List<InventarioProducto> inventarioList = inventarioRepository.findAll();
        for (InventarioProducto inv : inventarioList) {
            if (inv.getCantidadDisponible() < inv.getProducto().getStockMinimo()) {
                crearAlertaStockBajo(inv);
            }
        }
    }

    @Scheduled(cron = "0 0 8 * * *") // Todos los días a las 8 AM
    public void checkForInsumosPorVencer() {
        List<Insumo> insumos = insumoRepository.findAll();
        LocalDate unaSemanaDespues = LocalDate.now().plusDays(7);
        for (Insumo insumo : insumos) {
            if (insumo.getFechaVencimiento() != null && insumo.getFechaVencimiento().isBefore(unaSemanaDespues)) {
                crearAlertaInsumoPorVencer(insumo);
            }
        }
    }

    public DashboardDtos.MetricasResponseDto getDashboardMetrics() {
        long totalClientes = clienteRepository.count();
        long totalProductos = productoRepository.count();
        long pedidosPendientes = pedidoRepository.findAll().stream().filter(p -> "Pendiente".equals(p.getEstado())).count();
        long productosConStockBajo = inventarioRepository.findAll().stream().filter(i -> i.getCantidadDisponible() < i.getProducto().getStockMinimo()).count();
        long insumosPorVencer = insumoRepository.findAll().stream().filter(i -> i.getFechaVencimiento() != null && i.getFechaVencimiento().isBefore(LocalDate.now().plusDays(7))).count();
        long alertasActivas = alertaRepository.findAll().stream().filter(a -> !a.isLeida()).count();

        return new DashboardDtos.MetricasResponseDto(totalClientes, totalProductos, pedidosPendientes, productosConStockBajo, insumosPorVencer, alertasActivas);
    }

    private void crearAlertaStockBajo(InventarioProducto inv) {
        String mensaje = String.format("Stock bajo para %s. Cantidad actual: %d, Mínimo: %d",
                inv.getProducto().getNombre(), inv.getCantidadDisponible(), inv.getProducto().getStockMinimo());
        Alerta alerta = Alerta.builder()
                .tipo(TipoAlerta.STOCK_BAJO)
                .entidadId(inv.getProducto().getProductoId())
                .entidadTipo("Producto")
                .mensaje(mensaje)
                .prioridad(PrioridadAlerta.ALTA)
                .build();
        alertaRepository.save(alerta);
    }

    private void crearAlertaInsumoPorVencer(Insumo insumo) {
        String mensaje = String.format("El insumo %s está próximo a vencer el %s.",
                insumo.getNombre(), insumo.getFechaVencimiento().toString());
        Alerta alerta = Alerta.builder()
                .tipo(TipoAlerta.INSUMO_VENCIDO)
                .entidadId(insumo.getInsumoId())
                .entidadTipo("Insumo")
                .mensaje(mensaje)
                .prioridad(PrioridadAlerta.MEDIA)
                .build();
        alertaRepository.save(alerta);
    }
}
