package proyect.proyectefinal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import proyect.proyectefinal.model.db.Gasto;
import proyect.proyectefinal.model.dto.GastoEdit;

import java.util.List;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long>,
            JpaSpecificationExecutor<Gasto> {
    List<GastoEdit> findByGrupoId(Long grupoId);
    List<GastoEdit> findByUsuarioId(Long usuarioId);

      @Query("""
      select g from Gasto g
       join g.usuario u
      where u.nickname = :nickname
        and (:tipoGasto is null or g.tipoGasto = :tipoGasto)
        and (:subtipo  is null or g.subtipo   = :subtipo)
    """)
    Page<Gasto> findByUsuarioAndTipoAndSubtipo(
      @Param("nickname") String nickname,
      @Param("tipoGasto") String tipoGasto,
      @Param("subtipo")  String subtipo,
      Pageable pageable
    );
}
