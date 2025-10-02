package com.gomitas.entity;

import com.gomitas.enums.CategoriaABC;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clasificacion_abc")
public class ClasificacionAbc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "valor_ventas_anuales", nullable = false)
    private BigDecimal valorVentasAnuales;

    @Column(name = "porcentaje_acumulado")
    private BigDecimal porcentajeAcumulado;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "categoria")
    private CategoriaABC categoria;

    @UpdateTimestamp
    @Column(name = "fecha_clasificacion")
    private LocalDateTime fechaClasificacion;
}
