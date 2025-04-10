package proyect.proyectefinal.model.db;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Mensaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UsuarioDb emisor;

    @ManyToOne
    private UsuarioDb receptor;

    @ManyToOne
    private GrupoFamiliar grupo;

    private String contenido;

    private LocalDateTime fecha;

    private String tipo;
}
