package proyect.proyectefinal.model.dto;
import java.time.LocalDateTime;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeInfo {
    private Long id;
    private String contenido;
    private String emisorNickname;
    private String receptorNickname;
    private LocalDateTime fecha;
    private String tipo;
}
