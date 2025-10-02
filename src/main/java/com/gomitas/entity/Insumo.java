package com.gomitas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insumo")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "insumo_id")
    private Long insumoId;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "unidad_medida", nullable = false)
    private String unidadMedida;

    @Column(name = "cantidad_stock", nullable = false)
    private BigDecimal cantidadStock = BigDecimal.ZERO;

    @Column(name = "stock_minimo")
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "costo_unitario", nullable = false)
    private BigDecimal costoUnitario;

    private String proveedor;

    @Builder.Default
    private Boolean estado = true;
}
