package proyect.proyectefinal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.repository.GrupoFamiliarRepository;

import java.util.List;
import java.util.Optional;

@Service
public interface GrupoFamiliarService {
    public List<GrupoFamiliar> findAll();
    public Optional<GrupoFamiliar> findById(Long id);
    public String asignarRol(String nicknameDestino, String rolStr);
    public GrupoFamiliar save(GrupoFamiliar grupo);
    public void deleteById(Long id);
}
