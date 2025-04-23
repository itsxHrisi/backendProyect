package proyect.proyectefinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import proyect.proyectefinal.model.db.Invitacion;

import java.util.List;

public interface InvitacionRepository extends JpaRepository<Invitacion, Long> {
    List<Invitacion> findByGrupoId(Long grupoId);
    List<Invitacion> findByNicknameDestino(String nicknameDestino);
}
