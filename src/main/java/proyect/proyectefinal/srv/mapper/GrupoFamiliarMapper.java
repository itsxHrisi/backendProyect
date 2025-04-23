package proyect.proyectefinal.srv.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import proyect.proyectefinal.model.db.*;
import proyect.proyectefinal.model.dto.*;

import java.util.List;

@Mapper
public interface GrupoFamiliarMapper {
    GrupoFamiliarMapper INSTANCE = Mappers.getMapper(GrupoFamiliarMapper.class);

    GrupoFamiliar grupoFamiliarEditToGrupoFamiliar(GrupoFamiliarEdit edit);

    GrupoFamiliarEdit grupoFamiliarToGrupoFamiliarEdit(GrupoFamiliar grupo);

    GrupoFamiliarInfo grupoFamiliarToGrupoFamiliarInfo(GrupoFamiliar grupo);

    void updateGrupoFamiliarFromEdit(GrupoFamiliarEdit edit, @MappingTarget GrupoFamiliar grupo);

    List<GrupoFamiliarInfo> gruposToGrupoFamiliarInfoList(List<GrupoFamiliar> grupos);
}