package proyect.proyectefinal.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import proyect.proyectefinal.model.db.IngresoDb;

import java.util.List;

@Repository
public interface IngresoRepository extends JpaRepository<IngresoDb, Long>,JpaSpecificationExecutor<IngresoDb> {
    List<IngresoDb> findByGrupoId(Long grupoId);
    List<IngresoDb> findByUsuarioId(Long usuarioId);

}
