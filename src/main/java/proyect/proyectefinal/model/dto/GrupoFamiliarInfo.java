package proyect.proyectefinal.model.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoFamiliarInfo {
    private Long id;
    private String nombre;
    private String administradorNickname;
}