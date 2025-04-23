package proyect.proyectefinal.srv.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.model.db.Invitacion;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.InvitacionRequest;
import proyect.proyectefinal.repository.GrupoFamiliarRepository;
import proyect.proyectefinal.repository.InvitacionRepository;
import proyect.proyectefinal.repository.UsuarioRepository;
import proyect.proyectefinal.service.InvitacionService;
import proyect.proyectefinal.srv.mapper.InvitacionMapper;
@Service
public class InvitacionServiceImpl implements InvitacionService {

    private final InvitacionRepository invitacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoFamiliarRepository grupoFamiliarRepository;

    public InvitacionServiceImpl(
            InvitacionRepository invitacionRepository,
            UsuarioRepository usuarioRepository,
            GrupoFamiliarRepository grupoFamiliarRepository
    ) {
        this.invitacionRepository = invitacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.grupoFamiliarRepository = grupoFamiliarRepository;
    }

    @Override
    public Invitacion crearInvitacion(InvitacionRequest request) {
        // 1. Buscar usuario por nickname
        UsuarioDb usuarioDestino = usuarioRepository.findByNickname(request.getNickname())
                .orElseThrow(() -> new RuntimeException("Este usuario no existe"));

        // 2. Buscar grupo
        GrupoFamiliar grupo = grupoFamiliarRepository.findById(request.getGrupoId())
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        // 3. Crear invitación
        Invitacion invitacion = new Invitacion();
        invitacion.setNicknameDestino(usuarioDestino.getEmail()); // Aunque no uses el email para enviar, sigue siendo identificativo
        invitacion.setGrupo(grupo);
        invitacion.setEstado(Invitacion.EstadoInvitacion.PENDIENTE);
        invitacion.setFechaEnvio(LocalDateTime.now());

        return invitacionRepository.save(invitacion);
    }

    @Override
    public List<Invitacion> getInvitacionesPorGrupo(Long grupoId) {
        return invitacionRepository.findByGrupoId(grupoId);
    }

    @Override
    public Optional<Invitacion> actualizarEstado(Long id, Invitacion.EstadoInvitacion estado) {
        return invitacionRepository.findById(id).map(inv -> {
            inv.setEstado(estado);
            return invitacionRepository.save(inv);
        });
    }
}
