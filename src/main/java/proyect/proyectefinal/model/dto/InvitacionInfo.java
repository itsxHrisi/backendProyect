package proyect.proyectefinal.model.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitacionInfo {
    private Long id;
    private String nicknameDestino;
    private String estado;
    private LocalDateTime fechaEnvio;
    private Long grupoId;
    private String grupoNombre;
}
