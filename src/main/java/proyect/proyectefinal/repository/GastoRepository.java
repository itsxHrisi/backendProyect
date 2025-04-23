package proyect.proyectefinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyect.proyectefinal.model.db.Gasto;
import proyect.proyectefinal.model.dto.GastoEdit;

import java.util.List;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {
    List<GastoEdit> findByGrupoId(Long grupoId);
    List<GastoEdit> findByUsuarioId(Long usuarioId);
}
