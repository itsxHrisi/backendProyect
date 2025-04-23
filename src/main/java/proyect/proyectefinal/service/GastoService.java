package proyect.proyectefinal.service;

import org.springframework.stereotype.Service;
import proyect.proyectefinal.model.db.Gasto;
import proyect.proyectefinal.model.dto.GastoEdit;
import proyect.proyectefinal.repository.GastoRepository;

import java.util.List;
import java.util.Optional;

@Service
public interface GastoService {
    public Gasto crearGasto(Gasto gasto);
    public List<Gasto> obtenerTodos();
    public Optional<Gasto> obtenerPorId(Long id);
    public void eliminarGasto(Long id);
    public List<GastoEdit> obtenerPorUsuarioId(Long usuarioId);
    public List<GastoEdit> obtenerPorGrupoId(Long grupoId);

} 


