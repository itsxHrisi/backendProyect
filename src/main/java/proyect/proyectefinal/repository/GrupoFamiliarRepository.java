package proyect.proyectefinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyect.proyectefinal.model.db.GrupoFamiliar;

@Repository
public interface GrupoFamiliarRepository extends JpaRepository<GrupoFamiliar, Long> {
}
