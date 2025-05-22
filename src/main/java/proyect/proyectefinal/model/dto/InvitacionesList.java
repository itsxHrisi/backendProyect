package proyect.proyectefinal.model.dto;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.model.db.Invitacion.EstadoInvitacion;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InvitacionesList {
    
    private String nicknameDestino;
    private GrupoFamiliar grupo;
    private EstadoInvitacion estado;
    private LocalDateTime fechaEnvio;
    
}
