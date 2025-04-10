package proyect.proyectefinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyect.proyectefinal.model.db.Gasto;

import java.util.List;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {
    List<Gasto> findByGrupoId(Long grupoId);
    List<Gasto> findByUsuarioId(Long usuarioId);
}
