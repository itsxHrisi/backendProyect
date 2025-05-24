package proyect.proyectefinal.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import io.micrometer.common.lang.NonNull;

import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.model.db.UsuarioDb;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;




public interface UsuarioRepository extends JpaRepository<UsuarioDb, Long>,JpaSpecificationExecutor<UsuarioDb>{
    Optional<UsuarioDb> findByNickname(String nickname);
    Optional<UsuarioDb> findByEmail(String email);
    List<UsuarioDb> findByGrupoFamiliarId(Long grupoId);

    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
List<UsuarioDb> findByGrupoFamiliar(GrupoFamiliar grupoFamiliar);
@NonNull Page<UsuarioDb> findAll(@NonNull Pageable pageable);


}
