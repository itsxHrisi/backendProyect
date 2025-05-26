package proyect.proyectefinal.model.db;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ingresos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IngresoDb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que creó este ingreso **/
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioDb usuario;

    /** Grupo en el que se registró el ingreso **/
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoFamiliar grupo;

    /** Importe del ingreso **/
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;


      /** Fecha de creación **/
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;
}
