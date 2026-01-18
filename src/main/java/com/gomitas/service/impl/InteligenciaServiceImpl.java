package com.gomitas.service.impl;

import com.gomitas.dto.AlertaDtos;
import com.gomitas.dto.AnalisisDtos;
import com.gomitas.dto.PronosticoDtos;
import com.gomitas.entity.Alerta;
import com.gomitas.entity.ClasificacionAbc;
import com.gomitas.entity.ParametrosEoq;
import com.gomitas.entity.Pronostico;
import com.gomitas.entity.Producto;
import com.gomitas.entity.VentasHistoricas;
import com.gomitas.enums.CategoriaABC;
import com.gomitas.exception.BadRequestException;
import com.gomitas.exception.ResourceNotFoundException;
import com.gomitas.repository.*;
import com.gomitas.service.InteligenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteligenciaServiceImpl implements InteligenciaService {

    private final PronosticoRepository pronosticoRepository;
    private final ParametrosEoqRepository eoqRepository;
    private final ClasificacionAbcRepository abcRepository;
    private final AlertaRepository alertaRepository;
    private final VentasHistoricasRepository ventasHistoricasRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public PronosticoDtos.PronosticoResponseDto generarPronostico(PronosticoDtos.PronosticoRequestDto requestDto) {
        Producto producto = productoRepository.findById(requestDto.productoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // CONSULTA OPTIMIZADA: La base de datos filtra y ordena los resultados.
        List<VentasHistoricas> ventas = ventasHistoricasRepository.findByProducto_ProductoIdOrderByFechaVentaDesc(producto.getProductoId());

        if (ventas.isEmpty()) {
            throw new BadRequestException("No hay historial de ventas para este producto para generar un pronóstico.");
        }

        int cantidadEstimada;

        switch (requestDto.metodo()) {
            case PROMEDIO_MOVIL:
                cantidadEstimada = calcularPromedioMovil(ventas, 3); // Usando los últimos 3 periodos
                break;
            case PROMEDIO_PONDERADO:
                cantidadEstimada = calcularPromedioPonderado(ventas, new double[]{0.5, 0.3, 0.2}); // Pesos para los últimos 3 periodos
                break;
            case SUAVIZADO_EXPONENCIAL:
                cantidadEstimada = calcularSuavizadoExponencial(ventas, 0.3); // Factor de suavizado alpha
                break;
            case REGRESION_LINEAL:
                // Este es más complejo y se deja como mejora futura o con una implementación simplificada.
                // Por ahora, usará el promedio simple como fallback.
            default:
                cantidadEstimada = (int) Math.round(ventas.stream()
                        .mapToInt(VentasHistoricas::getCantidadVendida)
                        .average()
                        .orElse(10.0));
                break;
        }

        Pronostico pronostico = Pronostico.builder()
                .producto(producto)
                .fechaPronosticada(requestDto.fechaFutura())
                .cantidadEstimada(cantidadEstimada)
                .metodo(requestDto.metodo())
                .build();

        Pronostico savedPronostico = pronosticoRepository.save(pronostico);
        return mapToPronosticoDto(savedPronostico);
    }

    private int calcularPromedioMovil(List<VentasHistoricas> ventas, int periodos) {
        if (ventas.size() < periodos) {
            // Si no hay suficientes datos, calcula el promedio de los que hay
            return (int) Math.round(ventas.stream()
                    .mapToInt(VentasHistoricas::getCantidadVendida)
                    .average().orElse(0));
        }
        return (int) Math.round(ventas.stream().limit(periodos)
                .mapToInt(VentasHistoricas::getCantidadVendida)
                .average().orElse(0));
    }

    private int calcularPromedioPonderado(List<VentasHistoricas> ventas, double[] pesos) {
        if (ventas.size() < pesos.length) {
            // Fallback a promedio móvil simple si no hay suficientes datos
            return calcularPromedioMovil(ventas, ventas.size());
        }
        double sumaPonderada = 0;
        for (int i = 0; i < pesos.length; i++) {
            sumaPonderada += ventas.get(i).getCantidadVendida() * pesos[i];
        }
        return (int) Math.round(sumaPonderada);
    }

    private int calcularSuavizadoExponencial(List<VentasHistoricas> ventas, double alpha) {
        // Implementación de Suavizado Exponencial Simple (SES)
        // El pronóstico para el siguiente periodo es el valor suavizado del último periodo.
        if (ventas.isEmpty()) return 0;

        // Invertimos la lista para empezar desde el más antiguo al más reciente
        List<VentasHistoricas> ventasAsc = ventas.stream()
                .sorted((v1, v2) -> v1.getFechaVenta().compareTo(v2.getFechaVenta()))
                .collect(Collectors.toList());

        double smoothedValue = ventasAsc.get(0).getCantidadVendida(); // El primer valor es el punto de partida

        for (int i = 1; i < ventasAsc.size(); i++) {
            int actual = ventasAsc.get(i).getCantidadVendida();
            smoothedValue = alpha * actual + (1 - alpha) * smoothedValue;
        }

        return (int) Math.round(smoothedValue);
    }


    @Override
    @Transactional
    public PronosticoDtos.ErrorPronosticoDto calcularErrorDePronostico(Long productoId, LocalDate fechaInicio, LocalDate fechaFin) {
        productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        List<Pronostico> pronosticos = pronosticoRepository.findByProducto_ProductoIdAndFechaPronosticadaBetween(productoId, fechaInicio, fechaFin);
        List<VentasHistoricas> ventas = ventasHistoricasRepository.findByProducto_ProductoIdAndFechaVentaBetween(productoId, fechaInicio, fechaFin);

        if (pronosticos.isEmpty() || ventas.isEmpty()) {
            throw new BadRequestException("No hay suficientes datos de pronósticos o ventas en el período especificado para calcular el error.");
        }

        Map<LocalDate, Integer> ventasMap = ventas.stream()
                .collect(Collectors.toMap(VentasHistoricas::getFechaVenta, VentasHistoricas::getCantidadVendida));

        Map<LocalDate, Integer> pronosticosMap = pronosticos.stream()
                .collect(Collectors.toMap(Pronostico::getFechaPronosticada, Pronostico::getCantidadEstimada));

        double sumErrorAbsoluto = 0;
        double sumErrorCuadrado = 0;
        double sumErrorPorcentual = 0;
        int n = 0;

        for (Map.Entry<LocalDate, Integer> pronosticoEntry : pronosticosMap.entrySet()) {
            LocalDate fecha = pronosticoEntry.getKey();
            Integer valorPronosticado = pronosticoEntry.getValue();
            Integer valorReal = ventasMap.get(fecha);

            if (valorReal != null) {
                n++;
                double error = (double) valorReal - valorPronosticado;
                sumErrorAbsoluto += Math.abs(error);
                sumErrorCuadrado += Math.pow(error, 2);
                if (valorReal != 0) {
                    sumErrorPorcentual += Math.abs(error / valorReal);
                }
            }
        }

        if (n == 0) {
            throw new BadRequestException("No hay datos coincidentes entre pronósticos y ventas para las fechas especificadas.");
        }

        double mad = sumErrorAbsoluto / n;
        double mse = sumErrorCuadrado / n;
        double mape = (sumErrorPorcentual / n) * 100;

        return new PronosticoDtos.ErrorPronosticoDto(productoId, fechaInicio, fechaFin, mad, mse, mape);
    }

    @Override
    @Transactional
    public AnalisisDtos.EoqResponseDto calcularEoq(AnalisisDtos.EoqRequestDto requestDto) {
        Producto producto = productoRepository.findById(requestDto.productoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (requestDto.costoMantenimiento().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El costo de mantenimiento debe ser positivo.");
        }

        // Fórmula EOQ: sqrt((2 * Demanda * CostoPedido) / CostoMantenimiento)
        BigDecimal dos = new BigDecimal("2");
        BigDecimal demanda = new BigDecimal(requestDto.demandaAnual());
        BigDecimal costoPedido = requestDto.costoPedido();
        BigDecimal costoMantenimiento = requestDto.costoMantenimiento();

        BigDecimal eoqValue = new BigDecimal(Math.sqrt(
                dos.multiply(demanda).multiply(costoPedido).divide(costoMantenimiento, 2, RoundingMode.HALF_UP).doubleValue()
        ));

        ParametrosEoq eoq = eoqRepository.save(ParametrosEoq.builder()
                .producto(producto)
                .demandaAnual(requestDto.demandaAnual())
                .costoPedido(costoPedido)
                .costoMantenimiento(costoMantenimiento)
                .eoqCalculado(eoqValue)
                .puntoReorden(0) // Simplificado
                .build());

        return new AnalisisDtos.EoqResponseDto(eoq.getProducto().getProductoId(), eoq.getEoqCalculado(), eoq.getPuntoReorden(), eoq.getFechaCalculo());
    }

    @Override
    @Transactional
    public List<AnalisisDtos.AbcResponseDto> generarClasificacionAbc(LocalDate fechaInicio, LocalDate fechaFin) {
        List<VentasHistoricas> ventasAnuales = ventasHistoricasRepository.findByFechaVentaBetween(fechaInicio, fechaFin);

        if (ventasAnuales.isEmpty()) {
            throw new BadRequestException("No hay datos de ventas en el período especificado para generar la clasificación ABC.");
        }

        // Paso 1 y 2: Calcular el valor de venta total por producto
        Map<Producto, BigDecimal> valorVentasPorProducto = ventasAnuales.stream()
                .collect(Collectors.groupingBy(
                        VentasHistoricas::getProducto,
                        Collectors.mapping(
                                venta -> venta.getProducto().getPrecioUnitario().multiply(BigDecimal.valueOf(venta.getCantidadVendida())),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        // Paso 3: Calcular el gran total y la contribución porcentual
        BigDecimal granTotalVentas = valorVentasPorProducto.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (granTotalVentas.compareTo(BigDecimal.ZERO) == 0) {
            throw new BadRequestException("El valor total de ventas es cero. No se puede generar la clasificación ABC.");
        }

        // Paso 4: Ordenar y calcular porcentaje acumulado
        List<Map.Entry<Producto, BigDecimal>> productosOrdenados = valorVentasPorProducto.entrySet().stream()
                .sorted(Map.Entry.<Producto, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toList());

        abcRepository.deleteAll(); // Limpiar clasificaciones anteriores

        BigDecimal porcentajeAcumulado = BigDecimal.ZERO;
        List<AnalisisDtos.AbcResponseDto> resultado = new java.util.ArrayList<>();

        for (Map.Entry<Producto, BigDecimal> entry : productosOrdenados) {
            Producto producto = entry.getKey();
            BigDecimal valorVentas = entry.getValue();
            BigDecimal porcentaje = valorVentas.divide(granTotalVentas, 4, RoundingMode.HALF_UP);
            porcentajeAcumulado = porcentajeAcumulado.add(porcentaje);

            // Paso 5: Asignar categoría
            CategoriaABC categoria;
            if (porcentajeAcumulado.compareTo(new BigDecimal("0.80")) <= 0) {
                categoria = CategoriaABC.A;
            } else if (porcentajeAcumulado.compareTo(new BigDecimal("0.95")) <= 0) {
                categoria = CategoriaABC.B;
            } else {
                categoria = CategoriaABC.C;
            }

            abcRepository.save(ClasificacionAbc.builder()
                    .producto(producto)
                    .valorVentasAnuales(valorVentas)
                    .categoria(categoria)
                    .build());

            resultado.add(new AnalisisDtos.AbcResponseDto(producto.getProductoId(), producto.getNombre(), valorVentas, categoria));
        }

        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaDtos.AlertaResponseDto> getAlertasActivas() {
        return alertaRepository.findAll().stream()
                .filter(a -> !a.isLeida())
                .map(this::mapToAlertaDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AlertaDtos.AlertaResponseDto marcarAlertaComoLeida(Long alertaId) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));
        alerta.setLeida(true);
        alerta.setFechaLectura(LocalDateTime.now());
        return mapToAlertaDto(alertaRepository.save(alerta));
    }

    // Mappers
    private PronosticoDtos.PronosticoResponseDto mapToPronosticoDto(Pronostico p) {
        return new PronosticoDtos.PronosticoResponseDto(p.getId(), p.getProducto().getProductoId(), p.getProducto().getNombre(), p.getFechaPronosticada(), p.getCantidadEstimada(), p.getMetodo(), p.getFechaCalculo().toLocalDate());
    }

    private AlertaDtos.AlertaResponseDto mapToAlertaDto(Alerta a) {
        return new AlertaDtos.AlertaResponseDto(a.getAlertaId(), a.getTipo(), a.getMensaje(), a.getPrioridad(), a.getFechaCreacion(), a.isLeida());
    }
}
