package proyect.proyectefinal.model.dto;

import java.time.LocalDate;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GastoInfo {
    private Long id;
    private String tipoGasto;
    private String subtipo;
    private Long cantidad;
    private LocalDate fecha;
    private String usuarioNickname;
}