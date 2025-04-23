package proyect.proyectefinal.srv.mapper;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import proyect.proyectefinal.model.db.*;
import proyect.proyectefinal.model.dto.*;

import java.util.List;

@Mapper
public interface MensajeMapper {
    MensajeMapper INSTANCE = Mappers.getMapper(MensajeMapper.class);

    Mensaje mensajeEditToMensaje(MensajeEdit edit);

    MensajeEdit mensajeToMensajeEdit(Mensaje mensaje);

    MensajeInfo mensajeToMensajeInfo(Mensaje mensaje);

    void updateMensajeFromEdit(MensajeEdit edit, @MappingTarget Mensaje mensaje);

    List<MensajeInfo> mensajesToMensajeInfoList(List<Mensaje> mensajes);
}
