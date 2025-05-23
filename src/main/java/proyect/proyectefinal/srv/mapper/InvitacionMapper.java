package proyect.proyectefinal.srv.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import proyect.proyectefinal.filters.model.FiltroBusqueda;
import proyect.proyectefinal.filters.model.PaginaResponse;
import proyect.proyectefinal.model.db.Invitacion;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.InvitacionEdit;
import proyect.proyectefinal.model.dto.InvitacionInfo;
import proyect.proyectefinal.model.dto.InvitacionRequest;
import proyect.proyectefinal.model.dto.InvitacionesList;
import proyect.proyectefinal.model.dto.UsuarioList;

@Mapper
public interface InvitacionMapper {
    InvitacionMapper INSTANCE = Mappers.getMapper(InvitacionMapper.class);

    Invitacion invitacionEditToInvitacion(InvitacionEdit dto);

    InvitacionEdit invitacionToInvitacionEdit(Invitacion entity);

    InvitacionInfo invitacionToInvitacionInfo(Invitacion entity);

    @Mapping(source = "nickname", target = "nicknameDestino")
    Invitacion invitacionRequestToInvitacion(InvitacionRequest invitacion);
    
    void updateInvitacionFromEdit(InvitacionEdit dto, @MappingTarget Invitacion entity);
    List<InvitacionesList>invitacionToInvitacionesList(List<Invitacion> invitaciones);

    List<InvitacionInfo> invitacionesToInvitacionInfoList(List<Invitacion> invitaciones);
    default PaginaResponse<InvitacionesList> pageToPaginaResponse(
        Page<Invitacion> page,
        List<FiltroBusqueda> filtros,
        List<String> sort
) {
    List<InvitacionesList> content =
        invitacionToInvitacionesList(page.getContent());

    return new PaginaResponse<>(
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),      // ← ESTE parámetro faltaba
        content,
        filtros,
        sort
    );
}

}
