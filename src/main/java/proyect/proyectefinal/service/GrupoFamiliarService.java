package proyect.proyectefinal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.repository.GrupoFamiliarRepository;

import java.util.List;
import java.util.Optional;

@Service
public class GrupoFamiliarService {

    @Autowired
    private GrupoFamiliarRepository grupoFamiliarRepository;

    public List<GrupoFamiliar> findAll() {
        return grupoFamiliarRepository.findAll();
    }

    public Optional<GrupoFamiliar> findById(Long id) {
        return grupoFamiliarRepository.findById(id);
    }

    public GrupoFamiliar save(GrupoFamiliar grupo) {
        return grupoFamiliarRepository.save(grupo);
    }

    public void deleteById(Long id) {
        grupoFamiliarRepository.deleteById(id);
    }
}
