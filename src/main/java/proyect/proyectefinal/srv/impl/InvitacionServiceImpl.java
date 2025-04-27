package proyect.proyectefinal.srv.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.model.db.Invitacion;
import proyect.proyectefinal.model.db.RolDb;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.InvitacionRequest;
import proyect.proyectefinal.repository.GrupoFamiliarRepository;
import proyect.proyectefinal.repository.InvitacionRepository;
import proyect.proyectefinal.repository.RolRepository;
import proyect.proyectefinal.repository.UsuarioRepository;
import proyect.proyectefinal.security.service.UsuarioService;
import proyect.proyectefinal.service.InvitacionService;
import proyect.proyectefinal.srv.mapper.InvitacionMapper;

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
    @Transactional
    public Optional<Invitacion> actualizarEstado(Long id, Invitacion.EstadoInvitacion nuevoEstado) {
        Optional<Invitacion> optionalInvitacion = invitacionRepository.findById(id);
    
        if (optionalInvitacion.isPresent()) {
            Invitacion invitacion = optionalInvitacion.get();
            invitacion.setEstado(nuevoEstado);
    
            if (nuevoEstado == Invitacion.EstadoInvitacion.ACEPTADA) {
                // 1. Buscar el usuario que aceptó la invitación
                Optional<UsuarioDb> optionalUsuario = usuarioRepository.findByNickname(invitacion.getNicknameDestino());
    
                if (optionalUsuario.isPresent()) {
                    UsuarioDb usuario = optionalUsuario.get();
                    // 2. Asignarle el grupo familiar
                    usuario.setGrupoFamiliar(invitacion.getGrupo());
    
                    // 3. Guardar el usuario
                    usuarioRepository.save(usuario);
                } else {
                    throw new RuntimeException("No se encontró el usuario destino para la invitación");
                }
            }
    
            // Guardar los cambios en la invitación también
            invitacionRepository.save(invitacion);
        }
    
        return optionalInvitacion;
    }
    
    public List<Invitacion> getInvitacionesPorNickname(String nickname) {
        return invitacionRepository.findByNicknameDestino(nickname);
    }

}
