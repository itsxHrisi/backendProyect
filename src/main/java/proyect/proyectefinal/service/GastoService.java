package proyect.proyectefinal.service;

import org.springframework.stereotype.Service;
import proyect.proyectefinal.model.db.Gasto;
import proyect.proyectefinal.model.dto.GastoEdit;
import proyect.proyectefinal.model.dto.GastoInfo;
import proyect.proyectefinal.repository.GastoRepository;
import org.springframework.data.domain.Page;
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

   
    
        Page<GastoInfo> findGastosByUser(
          String nickname,
          String tipoGasto,
          String subtipo,
          int page,
          int size,
          List<String> sort
        );
    
    
} 


