package proyect.proyectefinal.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import proyect.proyectefinal.exception.FiltroException;
import proyect.proyectefinal.filters.model.PaginaResponse;
import proyect.proyectefinal.filters.model.PeticionListadoFiltrado;
import proyect.proyectefinal.model.db.Invitacion;
import proyect.proyectefinal.model.dto.InvitacionRequest;
import proyect.proyectefinal.model.dto.InvitacionesList;
import proyect.proyectefinal.model.dto.PaginaDto;
import proyect.proyectefinal.model.dto.UsuarioList;
import proyect.proyectefinal.repository.InvitacionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public interface InvitacionService {  
    public String crearInvitacion(InvitacionRequest invitacion);
    public List<Invitacion> getInvitacionesPorGrupo(Long grupoId);
    public Optional<Invitacion> actualizarEstado(Long id, Invitacion.EstadoInvitacion estado);
    public List<Invitacion> getInvitacionesPorNickname(String nickname);
    public PaginaDto<InvitacionesList> findAll(Pageable paging);

  /**
     * Busca invitaciones aplicando filtros, paginación y ordenación
     * a partir de parámetros individuales.
     */
    PaginaResponse<InvitacionesList> findAll(
        List<String> filter,
        int page,
        int size,
        List<String> sort
    ) throws FiltroException;

    /**
     * Busca invitaciones a partir de una petición estructurada.
     */
    PaginaResponse<InvitacionesList> findAll(
        PeticionListadoFiltrado peticionListadoFiltrado
    ) throws FiltroException;
}
