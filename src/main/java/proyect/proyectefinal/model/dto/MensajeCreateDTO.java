package proyect.proyectefinal.model.dto;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeCreateDTO {
    @NotNull(message = "el emisorId no puede ser nulo")
    private Long emisorId;

    @NotNull(message = "el receptorId no puede ser nulo")
    private Long receptorId;

    /**  
     * Opcional: si es null, el mensaje no pertenece a ningún grupo  
     */
    private Long grupoId;

    @NotBlank(message = "el contenido no puede estar vacío")
    private String contenido;
}