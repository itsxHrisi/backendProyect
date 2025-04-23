package proyect.proyectefinal.model.db;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Gasto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UsuarioDb usuario;

    @ManyToOne
    private GrupoFamiliar grupo;

    @Column(name = "tipo_gasto", nullable = false)
    private String tipoGasto;
    
    @Column(name = "subtipo", nullable = false)
    private String subtipo;

    private Long cantidad;

    private LocalDate fecha;
}
