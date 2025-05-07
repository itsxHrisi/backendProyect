package proyect.proyectefinal.srv.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import proyect.proyectefinal.helper.PaginationHelper;
import proyect.proyectefinal.model.db.Gasto;
import proyect.proyectefinal.model.dto.GastoEdit;
import proyect.proyectefinal.model.dto.GastoInfo;
import proyect.proyectefinal.repository.GastoRepository;
import proyect.proyectefinal.service.GastoService;
import proyect.proyectefinal.srv.mapper.GastoMapper;

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
    
    @Override
    public Page<GastoInfo> findGastosByUser(
            String nickname,
            String tipoGasto,
            String subtipo,
            int page,
            int size,
            List<String> sort
    ) {
        Pageable pageable = PaginationHelper.createPageable(page, size, sort);
        Page<Gasto> gastos = gastoRepository
          .findByUsuarioAndTipoAndSubtipo(nickname,
                                          tipoGasto,
                                          subtipo,
                                          pageable);

        return gastos.map(GastoMapper.INSTANCE::gastoToGastoInfo);
    }
}
