package proyect.proyectefinal.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import proyect.proyectefinal.model.db.RolDb;
import proyect.proyectefinal.model.enums.RolNombre;




public interface RolRepository extends JpaRepository<RolDb, Integer>{
    Optional<RolDb> findByNombre(RolNombre rolNombre);
}
