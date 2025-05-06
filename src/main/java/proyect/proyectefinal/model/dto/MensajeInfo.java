package proyect.proyectefinal.model.dto;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeInfo {
  private Long id;

    private Long emisorId;
    private Long receptorId;
    private Long grupoId;

    private String contenido;

    /**
     * Formato ISO en JSON: 2025-05-05T14:30:00
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;
}
