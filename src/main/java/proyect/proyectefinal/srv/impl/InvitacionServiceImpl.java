package proyect.proyectefinal.srv.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.core.Authentication;

import proyect.proyectefinal.exception.FiltroException;
import proyect.proyectefinal.filters.model.PaginaResponse;
import proyect.proyectefinal.filters.model.PeticionListadoFiltrado;
import proyect.proyectefinal.filters.specification.FiltroBusquedaSpecification;
import proyect.proyectefinal.filters.utils.PaginationFactory;
import proyect.proyectefinal.filters.utils.PeticionListadoFiltradoConverter;
import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.model.db.Invitacion;
import proyect.proyectefinal.model.db.RolDb;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.InvitacionRequest;
import proyect.proyectefinal.model.dto.InvitacionesList;
import proyect.proyectefinal.model.dto.PaginaDto;
import proyect.proyectefinal.model.dto.UsuarioList;
import proyect.proyectefinal.repository.GrupoFamiliarRepository;
import proyect.proyectefinal.repository.InvitacionRepository;
import proyect.proyectefinal.repository.RolRepository;
import proyect.proyectefinal.repository.UsuarioRepository;
import proyect.proyectefinal.security.service.UsuarioService;
import proyect.proyectefinal.service.InvitacionService;
import proyect.proyectefinal.srv.mapper.InvitacionMapper;
import proyect.proyectefinal.srv.mapper.UsuarioMapper;

@Service
public class InvitacionServiceImpl implements InvitacionService {
    @Autowired
    private InvitacionRepository invitacionRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private GrupoFamiliarRepository grupoFamiliarRepository;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private RolRepository rolRepository;
    
private final PeticionListadoFiltradoConverter peticionConverter;
    private final PaginationFactory paginationFactory;
  
 public InvitacionServiceImpl(
                                 PeticionListadoFiltradoConverter peticionConverter,
                                 PaginationFactory paginationFactory) {
        this.peticionConverter   = peticionConverter;
        this.paginationFactory   = paginationFactory;
    }

    @Override
    public String crearInvitacion(InvitacionRequest request) {
        // 1. Validar nickname
        if (request.getNickname() == null || request.getNickname().isBlank()) {
            throw new RuntimeException("El nickname de destino no puede estar vacío");
        }

        // 2. Buscar usuario destino
        UsuarioDb usuarioDestino = usuarioRepository.findByNickname(request.getNickname())
                .orElseThrow(() -> new RuntimeException("Este usuario no existe: " + request.getNickname()));

        if (usuarioDestino.getNickname() == null || usuarioDestino.getNickname().isBlank()) {
            throw new RuntimeException("El usuario encontrado no tiene nickname asignado");
        }

        // 3. Buscar grupo
        GrupoFamiliar grupo = grupoFamiliarRepository.findById(request.getGrupoId())
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado con ID: " + request.getGrupoId()));

        // 4. Obtener usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nickname = auth.getName();
        UsuarioDb usuarioActual = usuarioService.getByNickname(nickname)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // 5. Verificar si el usuario autenticado es el administrador del grupo
        if (!grupo.getAdministrador().getId().equals(usuarioActual.getId())) {
            return "No tienes permisos para crear una invitación";
        }

        // 6. Crear invitación
        Invitacion invitacion = new Invitacion();
        invitacion.setNicknameDestino(usuarioDestino.getNickname());
        invitacion.setGrupo(grupo);
        invitacion.setEstado(Invitacion.EstadoInvitacion.PENDIENTE);
        invitacion.setFechaEnvio(LocalDateTime.now());

        invitacionRepository.save(invitacion);

        return "Invitación enviada";
    }

    @Override
    public List<Invitacion> getInvitacionesPorGrupo(Long grupoId) {
        return invitacionRepository.findByGrupoId(grupoId);
    }
 @Override
    public PaginaDto<InvitacionesList> findAll(Pageable paging) {
        // 1) Extraer usuario autenticado
        String nickname = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2) Pedir sólo las suyas
        Page<Invitacion> pagina = invitacionRepository
            .findAllByNicknameDestinoIgnoreCase(nickname, paging);

        // 3) Mapear al DTO de salida
        List<InvitacionesList> content = InvitacionMapper.INSTANCE
            .invitacionToInvitacionesList(pagina.getContent());

        // 4) Construir y devolver PaginaDto
        return new PaginaDto<>(
            pagina.getNumber(),
            pagina.getSize(),
            pagina.getTotalElements(),
            pagina.getTotalPages(),
            content,
            pagina.getSort()
        );
    }


 // src/main/java/proyect/proyectefinal/srv/impl/InvitacionServiceImpl.java
@Override
public PaginaResponse<InvitacionesList> findAll(
        List<String> filter, int page, int size, List<String> sort
) throws FiltroException {
    // 1) Convierte params a PeticionListadoFiltrado
    PeticionListadoFiltrado peticion = peticionConverter
        .convertFromParams(filter, page, size, sort);

    // 2) Construye pageable
    Pageable pageable = paginationFactory.createPageable(peticion);

    // 3) Construye Specification genérico con tus filtros
    Specification<Invitacion> spec = 
        new FiltroBusquedaSpecification<>(peticion.getListaFiltros());

    // 4) AÑADE SIEMPRE un filtro adicional para quedarte solo con las invitaciones 
    //    cuyo nicknameDestino sea el del usuario autenticado:
    String nicknameAuth = 
      SecurityContextHolder.getContext().getAuthentication().getName();
    Specification<Invitacion> authSpec = (root, cq, cb) -> 
      cb.equal(root.get("nicknameDestino"), nicknameAuth);

    // combinas ambos specs:
    spec = spec.and(authSpec);

    // 5) Ejecuta la consulta paginada
    Page<Invitacion> pageInv = invitacionRepository.findAll(spec, pageable);

    // 6) Mapea a tu DTO y devuelve
    return InvitacionMapper.INSTANCE.pageToPaginaResponse(
        pageInv,
        peticion.getListaFiltros(),
        peticion.getSort()
    );
}


    @Override
    public PaginaResponse<InvitacionesList> findAll(
            PeticionListadoFiltrado peticionListadoFiltrado
    ) throws FiltroException {
        try {
            // construir pageable
            Pageable pageable = paginationFactory.createPageable(peticionListadoFiltrado);
            // construir Specification genérica
            Specification<Invitacion> spec =
                new FiltroBusquedaSpecification<>(peticionListadoFiltrado.getListaFiltros());
            // ejecutar query
            Page<Invitacion> page = invitacionRepository.findAll(spec, pageable);
            // mapear a DTO y devolver PaginaResponse
            return InvitacionMapper.INSTANCE.pageToPaginaResponse(
                page,
                peticionListadoFiltrado.getListaFiltros(),
                peticionListadoFiltrado.getSort()
            );
        } catch (JpaSystemException e) {
            String cause = e.getRootCause() != null && e.getRootCause().getMessage() != null
                         ? e.getRootCause().getMessage()
                         : e.getMessage();
            throw new FiltroException(
                "BAD_OPERATOR_FILTER",
                "Operación no permitida sobre el atributo",
                cause
            );
        } catch (PropertyReferenceException e) {
            throw new FiltroException(
                "BAD_ATTRIBUTE_ORDER",
                "Atributo de ordenación desconocido",
                e.getMessage()
            );
        } catch (InvalidDataAccessApiUsageException e) {
            throw new FiltroException(
                "BAD_ATTRIBUTE_FILTER",
                "Atributo de filtrado desconocido",
                e.getMessage()
            );
        }
    }
    
    @Override
@Transactional
public Optional<Invitacion> actualizarEstado(Long id, Invitacion.EstadoInvitacion nuevoEstado) {
    Optional<Invitacion> invitacionOpt = invitacionRepository.findById(id);

    if (invitacionOpt.isPresent()) {
        Invitacion invitacion = invitacionOpt.get();
        invitacion.setEstado(nuevoEstado);
        invitacionRepository.save(invitacion);

        if (nuevoEstado == Invitacion.EstadoInvitacion.ACEPTADA) {
            // Obtener el usuario destino
            UsuarioDb usuarioDestino = usuarioRepository.findByNickname(invitacion.getNicknameDestino())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + invitacion.getNicknameDestino()));

            // Asignar el grupo familiar
            usuarioDestino.setGrupoFamiliar(invitacion.getGrupo());
            usuarioRepository.save(usuarioDestino);

            // Eliminar todas las invitaciones que tenga el usuario
            List<Invitacion> invitacionesUsuario = invitacionRepository.findByNicknameDestino(usuarioDestino.getNickname());
            invitacionRepository.deleteAll(invitacionesUsuario);
        }

        if (nuevoEstado == Invitacion.EstadoInvitacion.RECHAZADA) {
            // Si es rechazada, solo eliminar esa invitación
            invitacionRepository.delete(invitacion);
        }

        return Optional.of(invitacion);
    }

    return Optional.empty();
}

    
    
    public List<Invitacion> getInvitacionesPorNickname(String nickname) {
        return invitacionRepository.findByNicknameDestino(nickname);
    }

}
