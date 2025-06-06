package proyect.proyectefinal.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GastoEdit {
    private String tipoGasto;
    private String subtipo;
    private String cantidad; // 🔥 Cambiado a String
    private LocalDate fecha;
}