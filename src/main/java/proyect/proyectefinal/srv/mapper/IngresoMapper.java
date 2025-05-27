package proyect.proyectefinal.srv.mapper;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import proyect.proyectefinal.model.db.*;
import proyect.proyectefinal.model.dto.*;

import java.util.List;

@Mapper
public interface IngresoMapper {
  IngresoMapper INSTANCE = Mappers.getMapper(IngresoMapper.class);

  @Mapping(target="usuarioNickname", source="usuario.nickname")
  @Mapping(target="grupoId",         source="grupo.id")
  IngresoInfo ingresoToIngresoInfo(IngresoDb ingreso);
}