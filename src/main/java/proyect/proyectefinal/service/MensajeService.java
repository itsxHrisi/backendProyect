package proyect.proyectefinal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proyect.proyectefinal.model.db.Mensaje;
import proyect.proyectefinal.repository.MensajeRepository;

@Service
public interface MensajeService {
    public List<Mensaje> findAll();
    public Optional<Mensaje> findById(Long id);
    public Mensaje save(Mensaje mensaje);
    public void deleteById(Long id);
    public List<Mensaje> findByReceptorId(Long receptorId);
    public List<Mensaje> findByGrupoId(Long grupoId);
}

