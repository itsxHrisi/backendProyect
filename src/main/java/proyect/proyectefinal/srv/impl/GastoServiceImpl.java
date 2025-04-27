package proyect.proyectefinal.srv.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proyect.proyectefinal.model.db.Gasto;
import proyect.proyectefinal.model.dto.GastoEdit;
import proyect.proyectefinal.repository.GastoRepository;
import proyect.proyectefinal.service.GastoService;

@Service
public class GastoServiceImpl implements GastoService {
    @Autowired
    private GastoRepository gastoRepository;

    public Gasto crearGasto(Gasto gasto) {
        return gastoRepository.save(gasto);
    }

    public List<Gasto> obtenerTodos() {
        return gastoRepository.findAll();
    }

    public Optional<Gasto> obtenerPorId(Long id) {
        return gastoRepository.findById(id);
    }

    public void eliminarGasto(Long id) {
        gastoRepository.deleteById(id);
    }

    // Opcionales para filtros personalizados
    public List<GastoEdit> obtenerPorUsuarioId(Long usuarioId) {
        return gastoRepository.findByUsuarioId(usuarioId);
    }

    public List<GastoEdit> obtenerPorGrupoId(Long grupoId) {
        return gastoRepository.findByGrupoId(grupoId);
    }
}
