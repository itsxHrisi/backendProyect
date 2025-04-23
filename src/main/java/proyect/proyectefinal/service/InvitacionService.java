package proyect.proyectefinal.service;

import org.springframework.stereotype.Service;

import proyect.proyectefinal.model.db.Invitacion;
import proyect.proyectefinal.model.dto.InvitacionRequest;
import proyect.proyectefinal.repository.InvitacionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public interface InvitacionService {  
    public Invitacion crearInvitacion(InvitacionRequest invitacion);
    public List<Invitacion> getInvitacionesPorGrupo(Long grupoId);
    public Optional<Invitacion> actualizarEstado(Long id, Invitacion.EstadoInvitacion estado);
}
