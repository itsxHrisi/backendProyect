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

    @Column(name = "emisor_id", nullable = false)
    private Long emisorId;

    @Column(name = "receptor_id", nullable = false)
    private Long receptorId;

    @Column(name = "grupo_id")
    private Long grupoId;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}
