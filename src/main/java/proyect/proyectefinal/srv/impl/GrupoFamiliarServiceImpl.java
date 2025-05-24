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
import proyect.proyectefinal.model.enums.RolNombre;
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
    @Override
    @Transactional
    public String asignarRol(String nicknameDestino, String rolStr) {
        // 1) Admin autenticado
        String nicknameActual = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioDb admin = usuarioService.getByNickname(nicknameActual)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario autenticado no encontrado"));

        // 2) Usuario destino
        UsuarioDb destino = usuarioService.getByNickname(nicknameDestino)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario destino no encontrado"));

        // 3) Solo el admin de ese grupo
        GrupoFamiliar grupo = admin.getGrupoFamiliar();
        if (grupo == null || !grupo.getAdministrador().getId().equals(admin.getId())) {
            throw new RuntimeException("No tienes permisos para asignar roles");
        }

        // 4) Destino debe estar en el mismo grupo
        if (destino.getGrupoFamiliar() == null
         || !destino.getGrupoFamiliar().getId().equals(grupo.getId())) {
            throw new RuntimeException("El usuario no pertenece al mismo grupo");
        }

        // 5) Convertir el parámetro a nuestro enum RolNombre
        RolNombre rolEnum;
        try {
            rolEnum = RolNombre.valueOf("ROL_" + rolStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rol no válido. Debe ser 'PADRE' o 'HIJO'");
        }

        // 6) Buscar la entidad RolDb por enum
        RolDb rolAsignar = rolRepository.findByNombre(rolEnum)
                .orElseThrow(() -> new RuntimeException("Rol " + rolEnum + " no encontrado"));

        // 7) Asignarlo y guardar
        destino.getRoles().add(rolAsignar);
        usuarioService.save(destino);

        return "Rol " + rolEnum + " asignado correctamente a " + nicknameDestino;
    }
}
