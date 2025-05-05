package proyect.proyectefinal.model.dto;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GastoCreateRequest {
    private String tipoGasto;
    private String subtipo;
    private String cantidad;  // <- como String para controlar el formato
    private LocalDate fecha;
}
