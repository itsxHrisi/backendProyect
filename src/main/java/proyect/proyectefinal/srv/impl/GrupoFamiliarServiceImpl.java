package proyect.proyectefinal.srv.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.repository.GrupoFamiliarRepository;
import proyect.proyectefinal.repository.UsuarioRepository;
import proyect.proyectefinal.service.GrupoFamiliarService;

@Service
public class GrupoFamiliarServiceImpl implements GrupoFamiliarService {
    @Autowired
    private GrupoFamiliarRepository grupoFamiliarRepository;

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

}
