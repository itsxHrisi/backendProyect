package proyect.proyectefinal.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import proyect.proyectefinal.model.db.UsuarioDb;




public interface UsuarioRepository extends JpaRepository<UsuarioDb, Long>{
    Optional<UsuarioDb> findByNickname(String nickname);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

}
