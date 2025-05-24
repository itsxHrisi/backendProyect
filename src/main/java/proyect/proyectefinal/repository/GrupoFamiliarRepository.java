package proyect.proyectefinal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.model.db.UsuarioDb;

@Repository
public interface GrupoFamiliarRepository extends JpaRepository<GrupoFamiliar, Long> {
    Optional<GrupoFamiliar> findById(Long id);

}
