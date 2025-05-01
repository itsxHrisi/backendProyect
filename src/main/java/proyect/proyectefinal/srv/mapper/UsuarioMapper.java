package proyect.proyectefinal.srv.mapper;


import java.util.List;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import proyect.proyectefinal.filters.model.FiltroBusqueda;
import proyect.proyectefinal.filters.model.PaginaResponse;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.UsuarioEdit;
import proyect.proyectefinal.model.dto.UsuarioEditConPassword;
import proyect.proyectefinal.model.dto.UsuarioInfo;
import proyect.proyectefinal.model.dto.UsuarioList;


@Mapper
public interface UsuarioMapper {
    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);
    List<UsuarioList> usuariosDbToUsuariosList(List<UsuarioDb> usuariosDb);
    @Mapping(target = "roles", source = "roles")
    UsuarioInfo usuarioDbToUsuarioInfo(UsuarioDb usuarioDb);
    UsuarioEdit UsuarioDbToUsuarioEdit(UsuarioDb UsuarioDb);
    UsuarioDb UsuarioEditToUsuarioDb(UsuarioEdit UsuarioEdit);

    UsuarioDb UsuarioEditConPasswordToUsuarioDb(UsuarioEditConPassword UsuarioEdit);
      /**
     * Convierte una página de usuarios en una respuesta paginada.
     */
    static PaginaResponse<UsuarioList> pageToPaginaResponse(
            Page<UsuarioDb> page,
            List<FiltroBusqueda> filtros,
            List<String> ordenaciones) {
        return new PaginaResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                UsuarioMapper.INSTANCE.usuariosDbToUsuariosList(page.getContent()),
                filtros,
                ordenaciones
        );
    }
}
