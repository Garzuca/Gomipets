package com.gomitas.service;

import com.gomitas.dto.DashboardDtos;
import com.gomitas.entity.*;
import com.gomitas.enums.PrioridadAlerta;
import com.gomitas.enums.TipoAlerta;
import com.gomitas.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduledTasksService {

    private final InventarioProductoRepository inventarioRepository;
    private final InsumoRepository insumoRepository;
    private final AlertaRepository alertaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final VentasHistoricasRepository ventasHistoricasRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final LoteInsumoRepository loteInsumoRepository;

    @Scheduled(cron = "0 0 * * * *") // cada hora
    @Transactional(readOnly = true)
    public void checkForStockBajo() {
        // Verificar stock bajo de productos terminados
        List<InventarioProducto> inventarioList = inventarioRepository.findAllWithProducto();
        for (InventarioProducto inv : inventarioList) {
            if (inv.getCantidadDisponible() < inv.getProducto().getStockMinimo()) {
                crearAlertaStockBajoProducto(inv);
            }
        }

        // Verificar stock bajo de insumos
        List<Insumo> insumos = insumoRepository.findAllWithLotes();
        for (Insumo insumo : insumos) {
            if (insumo.getStockMinimo() != null && insumo.getStockMinimo().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal stockTotal = insumo.getLotes().stream()
                        .map(LoteInsumo::getCantidad)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (stockTotal.compareTo(insumo.getStockMinimo()) < 0) {
                    crearAlertaStockBajoInsumo(insumo, stockTotal);
                }
            }
        }
    }

    @Scheduled(cron = "0 0 8 * * *") // Todos los días a las 8 AM
    @Transactional
    public void checkForInsumosPorVencer() {
        LocalDate unaSemanaDespues = LocalDate.now().plusDays(7);
        // Filtramos en memoria. Es menos eficiente, pero más estable para no romper otros servicios.
        List<LoteInsumo> lotesPorVencer = loteInsumoRepository.findAll().stream()
                .filter(lote -> lote.getFechaVencimiento() != null && lote.getFechaVencimiento().isBefore(unaSemanaDespues))
                .collect(Collectors.toList());

        for (LoteInsumo lote : lotesPorVencer) {
            crearAlertaInsumoPorVencer(lote);
        }
    }

    @Transactional(readOnly = true)
    public DashboardDtos.MetricasResponseDto getDashboardMetrics() {
        long totalClientes = clienteRepository.count();
        long totalProductos = productoRepository.count();
        
        // Cálculos en memoria para estabilidad
        long pedidosPendientes = pedidoRepository.findAll().stream().filter(p -> "Pendiente".equalsIgnoreCase(p.getEstado())).count();
        long productosConStockBajo = inventarioRepository.findAllWithProducto().stream().filter(i -> i.getCantidadDisponible() < i.getProducto().getStockMinimo()).count();
        long insumosPorVencer = loteInsumoRepository.findAll().stream().filter(l -> l.getFechaVencimiento() != null && l.getFechaVencimiento().isBefore(LocalDate.now().plusDays(7))).count();
        long alertasActivas = alertaRepository.findAll().stream().filter(a -> !a.isLeida()).count();

        List<DashboardDtos.KpiDto> kpis = Arrays.asList(
                new DashboardDtos.KpiDto(pedidosPendientes, "Pedidos Pendientes"),
                new DashboardDtos.KpiDto(alertasActivas, "Alertas Activas"),
                new DashboardDtos.KpiDto(productosConStockBajo, "Stock Bajo"),
                new DashboardDtos.KpiDto(insumosPorVencer, "Insumos por Vencer"),
                new DashboardDtos.KpiDto(totalClientes, "Total Clientes"),
                new DashboardDtos.KpiDto(totalProductos, "Total Productos")
        );

        DashboardDtos.ChartDataDto salesData = getSalesOverTimeData();
        DashboardDtos.ChartDataDto topProductsData = getTopProductsData();

        return new DashboardDtos.MetricasResponseDto(kpis, salesData, topProductsData);
    }
    
    // El resto de los métodos se mantiene igual, ya que son correctos
    
    private DashboardDtos.ChartDataDto getSalesOverTimeData() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);

        List<VentasHistoricas> ventas = ventasHistoricasRepository.findByFechaVentaBetween(startDate, endDate);
        Map<LocalDate, Double> salesByDate = ventas.stream()
                .collect(Collectors.groupingBy(
                        VentasHistoricas::getFechaVenta,
                        Collectors.summingDouble(v -> v.getCantidadVendida() * (v.getPrecioVenta() != null ? v.getPrecioVenta().doubleValue() : 0.0))
                ));

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            labels.add(date.toString());
            data.add(salesByDate.getOrDefault(date, 0.0));
        }

        DashboardDtos.DatasetDto dataset = new DashboardDtos.DatasetDto("Ventas", data);
        return new DashboardDtos.ChartDataDto(labels, List.of(dataset));
    }

    private DashboardDtos.ChartDataDto getTopProductsData() {
        Map<Producto, Integer> productSales = detallePedidoRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        DetallePedido::getProducto,
                        Collectors.summingInt(DetallePedido::getCantidad)
                ));

        List<Map.Entry<Producto, Integer>> topProducts = productSales.entrySet().stream()
                .sorted(Map.Entry.<Producto, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<String> labels = topProducts.stream().map(entry -> entry.getKey().getNombre()).collect(Collectors.toList());
        List<Integer> data = topProducts.stream().map(Map.Entry::getValue).collect(Collectors.toList());

        List<String> backgroundColors = Arrays.asList("rgba(255, 99, 132, 0.7)", "rgba(54, 162, 235, 0.7)", "rgba(255, 206, 86, 0.7)", "rgba(75, 192, 192, 0.7)", "rgba(153, 102, 255, 0.7)");
        DashboardDtos.DatasetDto dataset = new DashboardDtos.DatasetDto("Unidades Vendidas", data, backgroundColors);
        return new DashboardDtos.ChartDataDto(labels, List.of(dataset));
    }

    private void crearAlertaStockBajoProducto(InventarioProducto inv) {
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

    private void crearAlertaStockBajoInsumo(Insumo insumo, BigDecimal stockActual) {
        String mensaje = String.format("Stock bajo para el insumo %s. Cantidad actual: %.2f, Mínimo: %.2f",
                insumo.getNombre(), stockActual, insumo.getStockMinimo());
        Alerta alerta = Alerta.builder()
                .tipo(TipoAlerta.STOCK_BAJO)
                .entidadId(insumo.getInsumoId())
                .entidadTipo("Insumo")
                .mensaje(mensaje)
                .prioridad(PrioridadAlerta.MEDIA)
                .build();
        alertaRepository.save(alerta);
    }

    private void crearAlertaInsumoPorVencer(LoteInsumo lote) {
        // Para evitar N+1, asumimos que el insumo ya está cargado si es EAGER, si no, podría causar una consulta.
        // Dado que provenimos de findAll, la relación podría no estar cargada (si es LAZY).
        // En este contexto, una consulta extra es aceptable para no complicar el repositorio.
        Insumo insumo = insumoRepository.findById(lote.getInsumo().getInsumoId()).orElse(new Insumo());
        String mensaje = String.format("El lote %s del insumo %s está próximo a vencer el %s.",
                lote.getNumeroLote() != null ? lote.getNumeroLote() : lote.getLoteId().toString(),
                insumo.getNombre(),
                lote.getFechaVencimiento().toString());
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