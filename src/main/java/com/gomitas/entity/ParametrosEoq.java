package com.gomitas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parametros_eoq")
public class ParametrosEoq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    @Column(name = "demanda_anual", nullable = false)
    private Integer demandaAnual;

    @Column(name = "costo_pedido", nullable = false)
    private BigDecimal costoPedido;

    @Column(name = "costo_mantenimiento", nullable = false)
    private BigDecimal costoMantenimiento;

    @Column(name = "eoq_calculado")
    private BigDecimal eoqCalculado;

    @Column(name = "punto_reorden")
    private Integer puntoReorden;

    @UpdateTimestamp
    @Column(name = "fecha_calculo")
    private LocalDateTime fechaCalculo;
}
