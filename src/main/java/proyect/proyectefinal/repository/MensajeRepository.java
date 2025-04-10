package proyect.proyectefinal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import proyect.proyectefinal.model.db.Mensaje;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByReceptorId(Long receptorId);
    List<Mensaje> findByGrupoId(Long grupoId);
}
