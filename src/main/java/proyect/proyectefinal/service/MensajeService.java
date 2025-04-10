package proyect.proyectefinal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proyect.proyectefinal.model.db.Mensaje;
import proyect.proyectefinal.repository.MensajeRepository;

@Service
public class MensajeService {

    @Autowired
    private MensajeRepository mensajeRepository;

    public List<Mensaje> findAll() {
        return mensajeRepository.findAll();
    }

    public Optional<Mensaje> findById(Long id) {
        return mensajeRepository.findById(id);
    }

    public Mensaje save(Mensaje mensaje) {
        return mensajeRepository.save(mensaje);
    }

    public void deleteById(Long id) {
        mensajeRepository.deleteById(id);
    }

    public List<Mensaje> findByReceptorId(Long receptorId) {
        return mensajeRepository.findByReceptorId(receptorId);
    }

    public List<Mensaje> findByGrupoId(Long grupoId) {
        return mensajeRepository.findByGrupoId(grupoId);
    }
}

