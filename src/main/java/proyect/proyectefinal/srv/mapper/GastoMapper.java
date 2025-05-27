package proyect.proyectefinal.srv.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import proyect.proyectefinal.model.db.*;
import proyect.proyectefinal.model.dto.*;

import java.util.List;

@Mapper
public interface GastoMapper {
    GastoMapper INSTANCE = Mappers.getMapper(GastoMapper.class);

    Gasto gastoEditToGasto(GastoEdit gastoEdit);

    GastoEdit gastoToGastoEdit(Gasto gasto);
    
    @Mapping(target = "usuarioNickname", source = "usuario.nickname")
    GastoInfo gastoToGastoInfo(Gasto gasto);

    void updateGastoFromGastoEdit(GastoEdit gastoEdit, @MappingTarget Gasto gasto);

    List<GastoInfo> gastosToGastoInfoList(List<Gasto> gastos);
}



