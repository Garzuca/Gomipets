package com.gomitas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id")
    private Long productoId;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "stock_minimo", columnDefinition = "integer default 0")
    private Integer stockMinimo;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDate fechaCreacion;

    @Column(columnDefinition = "boolean default true")
    private Boolean estado;
}
