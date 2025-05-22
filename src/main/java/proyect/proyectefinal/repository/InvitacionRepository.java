package proyect.proyectefinal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import proyect.proyectefinal.model.db.Invitacion;

import java.util.List;

public interface InvitacionRepository extends JpaRepository<Invitacion, Long>,
                JpaSpecificationExecutor<Invitacion>  {
    List<Invitacion> findByGrupoId(Long grupoId);
    List<Invitacion> findByNicknameDestino(String nicknameDestino);
    Page<Invitacion> findAllByNicknameDestinoIgnoreCase(String nicknameDestino, Pageable pageable);

}
