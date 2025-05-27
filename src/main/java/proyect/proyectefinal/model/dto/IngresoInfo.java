package proyect.proyectefinal.model.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngresoInfo {
  private Long id;
  private BigDecimal cantidad;
  private LocalDateTime fecha;      // o LocalDate si solo almacenas fecha
  private String usuarioNickname;
  private Long grupoId;
}