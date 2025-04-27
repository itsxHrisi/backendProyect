package proyect.proyectefinal.srv.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.model.db.RolDb;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.repository.GrupoFamiliarRepository;
import proyect.proyectefinal.repository.RolRepository;
import proyect.proyectefinal.repository.UsuarioRepository;
import proyect.proyectefinal.security.service.UsuarioService;
import proyect.proyectefinal.service.GrupoFamiliarService;

@Service
public class GrupoFamiliarServiceImpl implements GrupoFamiliarService {
    @Autowired
    private GrupoFamiliarRepository grupoFamiliarRepository;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<GrupoFamiliar> findAll() {
        return grupoFamiliarRepository.findAll();
    }

    public Optional<GrupoFamiliar> findById(Long id) {
        return grupoFamiliarRepository.findById(id);
    }

    public GrupoFamiliar save(GrupoFamiliar grupo) {
        return grupoFamiliarRepository.save(grupo);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Optional<GrupoFamiliar> grupoOpt = grupoFamiliarRepository.findById(id);
        if (grupoOpt.isEmpty()) {
            throw new EntityNotFoundException("Grupo no encontrado");
        }

        GrupoFamiliar grupo = grupoOpt.get();

        // Desvincular usuarios del grupo
        List<UsuarioDb> usuarios = usuarioRepository.findByGrupoFamiliar(grupo);
        for (UsuarioDb usuario : usuarios) {
            usuario.setGrupoFamiliar(null);
            usuarioRepository.save(usuario);
        }

        // Quitar la relación con el administrador también
        if (grupo.getAdministrador() != null) {
            grupo.getAdministrador().setGrupoFamiliar(null);
            usuarioRepository.save(grupo.getAdministrador());
        }

        grupoFamiliarRepository.delete(grupo);
    }
public String asignarRol(String nicknameDestino, String rolStr) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String nicknameActual = auth.getName();
    UsuarioDb admin = usuarioService.getByNickname(nicknameActual)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario autenticado no encontrado"));

    UsuarioDb destino = usuarioService.getByNickname(nicknameDestino)
            .orElseThrow(() -> new RuntimeException("Usuario destino no encontrado"));

    GrupoFamiliar grupo = admin.getGrupoFamiliar();
    if (grupo == null || !grupo.getAdministrador().getId().equals(admin.getId())) {
        throw new RuntimeException("No tienes permisos para asignar roles");
    }

    // Validar que el destino esté en el grupo
    if (!destino.getGrupoFamiliar().getId().equals(grupo.getId())) {
        throw new RuntimeException("El usuario no pertenece al mismo grupo");
    }

    // Validar rol
    String rolFormateado = rolStr.toUpperCase();
    RolDb rolAsignar;
    if (rolFormateado.equals("PADRE")) {
        rolAsignar = rolRepository.findByNombre("ROL_PADRE")
                .orElseThrow(() -> new RuntimeException("Rol ROL_PADRE no encontrado"));
    } else if (rolFormateado.equals("HIJO")) {
        rolAsignar = rolRepository.findByNombre("ROL_HIJO")
                .orElseThrow(() -> new RuntimeException("Rol ROL_HIJO no encontrado"));
    } else {
        throw new RuntimeException("Rol no válido. Debe ser 'PADRE' o 'HIJO'");
    }

    destino.getRoles().add(rolAsignar);
    usuarioRepository.save(destino);

    return "Rol asignado correctamente";
}
}
