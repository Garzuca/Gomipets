package com.gomitas.service.impl;

import com.gomitas.dto.ReporteDtos;
import com.gomitas.entity.DetallePedido;
import com.gomitas.entity.InventarioProducto;
import com.gomitas.repository.DetallePedidoRepository;
import com.gomitas.repository.InventarioProductoRepository;
import com.gomitas.repository.ReporteMaquinariaRepository;
import com.gomitas.service.ReporteService;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final InventarioProductoRepository inventarioProductoRepository;
    private final ReporteMaquinariaRepository reporteMaquinariaRepository;

    @Override
    public ReporteDtos.ReporteVentasResponseDto generarReporteVentas(ReporteDtos.ReporteVentasRequestDto requestDto) {
        Specification<DetallePedido> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (requestDto.fechaInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pedido").get("fechaPedido"), requestDto.fechaInicio()));
            }
            if (requestDto.fechaFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pedido").get("fechaPedido"), requestDto.fechaFin()));
            }
            if (requestDto.productoId() != null) {
                predicates.add(cb.equal(root.get("producto").get("productoId"), requestDto.productoId()));
            }
            if (requestDto.clienteId() != null) {
                predicates.add(cb.equal(root.get("pedido").get("cliente").get("clienteId"), requestDto.clienteId()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<DetallePedido> detalles = detallePedidoRepository.findAll(spec);

        List<ReporteDtos.VentaDetalleDto> ventaDetalles = detalles.stream()
                .map(this::mapToVentaDetalleDto)
                .collect(Collectors.toList());

        BigDecimal totalVentas = ventaDetalles.stream()
                .map(ReporteDtos.VentaDetalleDto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalProductos = ventaDetalles.stream()
                .map(ReporteDtos.VentaDetalleDto::cantidad)
                .reduce(0, Integer::sum);

        return new ReporteDtos.ReporteVentasResponseDto(ventaDetalles, ventaDetalles.size(), totalProductos, totalVentas);
    }

    @Override
    public List<ReporteDtos.ReporteInventarioDto> generarReporteInventario() {
        return inventarioProductoRepository.findAll().stream()
                .map(this::mapToReporteInventarioDto)
                .collect(Collectors.toList());
    }

    @Override
    public void exportarReporteVentas(HttpServletResponse response, ReporteDtos.ReporteVentasRequestDto requestDto, String format) throws IOException {
        ReporteDtos.ReporteVentasResponseDto reporte = generarReporteVentas(requestDto);
        switch (format.toLowerCase()) {
            case "pdf":
                exportarVentasPdf(response, reporte);
                break;
            case "excel":
                exportarVentasExcel(response, reporte);
                break;
            case "csv":
            default:
                exportarVentasCsv(response, reporte);
                break;
        }
    }

    @Override
    public void exportarReporteInventario(HttpServletResponse response, String format) throws IOException {
        List<ReporteDtos.ReporteInventarioDto> reporte = generarReporteInventario();
        switch (format.toLowerCase()) {
            case "pdf":
                exportarInventarioPdf(response, reporte);
                break;
            case "excel":
                exportarInventarioExcel(response, reporte);
                break;
            case "csv":
            default:
                exportarInventarioCsv(response, reporte);
                break;
        }
    }

    private void exportarVentasCsv(HttpServletResponse response, ReporteDtos.ReporteVentasResponseDto reporte) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_ventas.csv\"");
        try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT
                .withHeader("Fecha", "Cliente", "Producto", "Cantidad", "Precio Unitario", "Subtotal"))) {
            for (ReporteDtos.VentaDetalleDto detalle : reporte.detalles()) {
                csvPrinter.printRecord(
                        detalle.fechaVenta().toString(),
                        detalle.nombreCliente(),
                        detalle.nombreProducto(),
                        detalle.cantidad(),
                        detalle.precioUnitario(),
                        detalle.subtotal()
                );
            }
        }
    }

    private void exportarInventarioCsv(HttpServletResponse response, List<ReporteDtos.ReporteInventarioDto> reporte) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_inventario.csv\"");
        try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT
                .withHeader("ID Producto", "Producto", "Cantidad Disponible", "Stock Mínimo", "Estado"))) {
            for (ReporteDtos.ReporteInventarioDto item : reporte) {
                csvPrinter.printRecord(
                        item.productoId(),
                        item.nombreProducto(),
                        item.cantidadDisponible(),
                        item.stockMinimo(),
                        item.estadoStock()
                );
            }
        }
    }

    private void exportarVentasExcel(HttpServletResponse response, ReporteDtos.ReporteVentasResponseDto reporte) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_ventas.xlsx\"");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ventas");
            String[] headers = {"Fecha", "Cliente", "Producto", "Cantidad", "Precio Unitario", "Subtotal"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (ReporteDtos.VentaDetalleDto detalle : reporte.detalles()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(detalle.fechaVenta().toString());
                row.createCell(1).setCellValue(detalle.nombreCliente());
                row.createCell(2).setCellValue(detalle.nombreProducto());
                row.createCell(3).setCellValue(detalle.cantidad());
                row.createCell(4).setCellValue(detalle.precioUnitario().doubleValue());
                row.createCell(5).setCellValue(detalle.subtotal().doubleValue());
            }
            workbook.write(response.getOutputStream());
        }
    }

    private void exportarInventarioExcel(HttpServletResponse response, List<ReporteDtos.ReporteInventarioDto> reporte) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_inventario.xlsx\"");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inventario");
            String[] headers = {"ID Producto", "Producto", "Cantidad Disponible", "Stock Mínimo", "Estado"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (ReporteDtos.ReporteInventarioDto item : reporte) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.productoId());
                row.createCell(1).setCellValue(item.nombreProducto());
                row.createCell(2).setCellValue(item.cantidadDisponible());
                row.createCell(3).setCellValue(item.stockMinimo());
                row.createCell(4).setCellValue(item.estadoStock());
            }
            workbook.write(response.getOutputStream());
        }
    }

    private void exportarVentasPdf(HttpServletResponse response, ReporteDtos.ReporteVentasResponseDto reporte) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_ventas.pdf\"");

        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            document.add(new Paragraph("Reporte de Ventas", fontTitle));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            String[] headers = {"Fecha", "Cliente", "Producto", "Cantidad", "Precio", "Subtotal"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            for (ReporteDtos.VentaDetalleDto detalle : reporte.detalles()) {
                table.addCell(detalle.fechaVenta().format(DateTimeFormatter.ISO_LOCAL_DATE));
                table.addCell(detalle.nombreCliente());
                table.addCell(detalle.nombreProducto());
                table.addCell(String.valueOf(detalle.cantidad()));
                table.addCell(detalle.precioUnitario().toString());
                table.addCell(detalle.subtotal().toString());
            }
            document.add(table);
        }
    }

    private void exportarInventarioPdf(HttpServletResponse response, List<ReporteDtos.ReporteInventarioDto> reporte) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_inventario.pdf\"");

        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            document.add(new Paragraph("Reporte de Inventario", fontTitle));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            String[] headers = {"ID", "Producto", "Disponible", "Stock Mínimo", "Estado"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            for (ReporteDtos.ReporteInventarioDto item : reporte) {
                table.addCell(item.productoId().toString());
                table.addCell(item.nombreProducto());
                table.addCell(String.valueOf(item.cantidadDisponible()));
                table.addCell(String.valueOf(item.stockMinimo()));
                table.addCell(item.estadoStock());
            }
            document.add(table);
        }
    }

    private ReporteDtos.VentaDetalleDto mapToVentaDetalleDto(DetallePedido detalle) {
        return new ReporteDtos.VentaDetalleDto(
                detalle.getPedido().getPedidoId(),
                detalle.getPedido().getFechaPedido(),
                detalle.getPedido().getCliente().getNombre(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
        );
    }

    private ReporteDtos.ReporteInventarioDto mapToReporteInventarioDto(InventarioProducto inventario) {
        String estado = inventario.getCantidadDisponible() <= inventario.getProducto().getStockMinimo() ? "BAJO" : "OK";
        return new ReporteDtos.ReporteInventarioDto(
                inventario.getProducto().getProductoId(),
                                inventario.getProducto().getNombre(),
                                inventario.getCantidadDisponible(),
                                inventario.getProducto().getStockMinimo(),
                                estado
                        );
                    }
                
                    @Override
                    public List<ReporteDtos.ReporteMaquinariaDto> generarReporteMaquinaria(ReporteDtos.ReporteMaquinariaRequestDto requestDto) {
                        Specification<com.gomitas.entity.ReporteMaquinaria> spec = (root, query, cb) -> {
                            List<Predicate> predicates = new ArrayList<>();
                            if (requestDto.fechaInicio() != null) {
                                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaReporte"), requestDto.fechaInicio().atStartOfDay()));
                            }
                            if (requestDto.fechaFin() != null) {
                                predicates.add(cb.lessThan(root.get("fechaReporte"), requestDto.fechaFin().plusDays(1).atStartOfDay()));
                            }
                            if (requestDto.maquinariaIds() != null && !requestDto.maquinariaIds().isEmpty()) {
                                predicates.add(root.get("maquinaria").get("maquinariaId").in(requestDto.maquinariaIds()));
                            }
                            if (requestDto.responsableId() != null) {
                                predicates.add(cb.equal(root.get("usuario").get("usuarioId"), requestDto.responsableId()));
                            }
                            return cb.and(predicates.toArray(new Predicate[0]));
                        };
                
                        return reporteMaquinariaRepository.findAll(spec).stream()
                                .map(this::mapToReporteMaquinariaDto)
                                .collect(Collectors.toList());
                    }
                
                    @Override
                    public void exportarReporteMaquinaria(HttpServletResponse response, ReporteDtos.ReporteMaquinariaRequestDto requestDto, String format) throws IOException {
                        List<ReporteDtos.ReporteMaquinariaDto> reporte = generarReporteMaquinaria(requestDto);
                        switch (format.toLowerCase()) {
                            case "pdf":
                                exportarMaquinariaPdf(response, reporte);
                                break;
                            case "excel":
                                exportarMaquinariaExcel(response, reporte);
                                break;
                            case "csv":
                            default:
                                exportarMaquinariaCsv(response, reporte);
                                break;
                        }
                    }
                
                    private void exportarMaquinariaCsv(HttpServletResponse response, List<ReporteDtos.ReporteMaquinariaDto> reporte) throws IOException {
                        response.setContentType("text/csv");
                        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_maquinaria.csv\"");
                        try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT
                                .withHeader("Fecha", "Máquina", "Tipo Reporte", "Realizado por", "Acciones Realizadas", "Costo"))) {
                            for (ReporteDtos.ReporteMaquinariaDto item : reporte) {
                                csvPrinter.printRecord(
                                        item.fechaReporte().toString(),
                                        item.nombreMaquinaria(),
                                        item.tipoReporte(),
                                        item.nombreResponsable(),
                                        item.accionesRealizadas(),
                                        item.costoReparacion()
                                );
                            }
                        }
                    }
                
                    private void exportarMaquinariaExcel(HttpServletResponse response, List<ReporteDtos.ReporteMaquinariaDto> reporte) throws IOException {
                        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_maquinaria.xlsx\"");
                
                        try (Workbook workbook = new XSSFWorkbook()) {
                            Sheet sheet = workbook.createSheet("Maquinaria");
                            String[] headers = {"Fecha", "Máquina", "Tipo Reporte", "Realizado por", "Acciones Realizadas", "Costo"};
                            Row headerRow = sheet.createRow(0);
                            for (int i = 0; i < headers.length; i++) {
                                Cell cell = headerRow.createCell(i);
                                cell.setCellValue(headers[i]);
                            }
                
                            int rowNum = 1;
                            for (ReporteDtos.ReporteMaquinariaDto item : reporte) {
                                Row row = sheet.createRow(rowNum++);
                                row.createCell(0).setCellValue(item.fechaReporte().toString());
                                row.createCell(1).setCellValue(item.nombreMaquinaria());
                                row.createCell(2).setCellValue(item.tipoReporte());
                                row.createCell(3).setCellValue(item.nombreResponsable());
                                row.createCell(4).setCellValue(item.accionesRealizadas());
                                row.createCell(5).setCellValue(item.costoReparacion().doubleValue());
                            }
                            workbook.write(response.getOutputStream());
                        }
                    }
                
                    private void exportarMaquinariaPdf(HttpServletResponse response, List<ReporteDtos.ReporteMaquinariaDto> reporte) throws IOException {
                        response.setContentType("application/pdf");
                        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_maquinaria.pdf\"");
                
                        try (Document document = new Document(PageSize.A4.rotate())) {
                            PdfWriter.getInstance(document, response.getOutputStream());
                            document.open();
                
                            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
                            document.add(new Paragraph("Reporte de Mantenimiento de Maquinaria", fontTitle));
                            document.add(new Paragraph(" "));
                
                            PdfPTable table = new PdfPTable(6);
                            table.setWidthPercentage(100);
                            String[] headers = {"Fecha", "Máquina", "Tipo", "Realizado por", "Acciones", "Costo"};
                            for (String header : headers) {
                                PdfPCell cell = new PdfPCell(new Phrase(header));
                                cell.setBackgroundColor(Color.LIGHT_GRAY);
                                table.addCell(cell);
                            }
                
                            for (ReporteDtos.ReporteMaquinariaDto item : reporte) {
                                table.addCell(item.fechaReporte().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                                table.addCell(item.nombreMaquinaria());
                                table.addCell(item.tipoReporte());
                                table.addCell(item.nombreResponsable());
                                table.addCell(item.accionesRealizadas());
                                table.addCell(item.costoReparacion().toString());
                            }
                            document.add(table);
                        }
                    }
                
                        private ReporteDtos.ReporteMaquinariaDto mapToReporteMaquinariaDto(com.gomitas.entity.ReporteMaquinaria reporte) {
                
                        
                
                                return new ReporteDtos.ReporteMaquinariaDto(
                
                        
                
                                        reporte.getReporteId(),
                
                        
                
                                        reporte.getMaquinaria().getNombre(),
                
                        
                
                                        reporte.getFechaReporte(),
                
                        
                
                                        reporte.getTipoReporte().name(),
                
                        
                
                                        reporte.getAccionesRealizadas(),
                
                        
                
                                        reporte.getUsuario().getNombreUsuario(),
                
                        
                
                                        reporte.getCostoReparacion()
                
                        
                
                                );
                
                        
                
                            }                }
                