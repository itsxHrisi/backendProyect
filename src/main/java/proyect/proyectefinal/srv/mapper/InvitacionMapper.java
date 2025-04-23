package proyect.proyectefinal.srv.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import proyect.proyectefinal.model.db.Invitacion;
import proyect.proyectefinal.model.dto.InvitacionEdit;
import proyect.proyectefinal.model.dto.InvitacionInfo;
import proyect.proyectefinal.model.dto.InvitacionRequest;

@Mapper
public interface InvitacionMapper {
    InvitacionMapper INSTANCE = Mappers.getMapper(InvitacionMapper.class);

    Invitacion invitacionEditToInvitacion(InvitacionEdit dto);

    InvitacionEdit invitacionToInvitacionEdit(Invitacion entity);

    InvitacionInfo invitacionToInvitacionInfo(Invitacion entity);
    Invitacion invitacionRequestToInvitacion(InvitacionRequest invitacion);
    void updateInvitacionFromEdit(InvitacionEdit dto, @MappingTarget Invitacion entity);

    List<InvitacionInfo> invitacionesToInvitacionInfoList(List<Invitacion> invitaciones);
}
