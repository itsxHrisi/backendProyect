package proyect.proyectefinal.service;

import proyect.proyectefinal.exception.FiltroException;
import proyect.proyectefinal.filters.model.PaginaResponse;
import proyect.proyectefinal.filters.model.PeticionListadoFiltrado;
import proyect.proyectefinal.model.db.IngresoDb;
import java.util.List;
import java.util.Optional;

public interface IngresoService {
    IngresoDb crearIngreso(IngresoDb ingreso);
    List<IngresoDb> listarPorGrupo(Long grupoId);
    Optional<IngresoDb> obtenerPorId(Long id);
    IngresoDb actualizarIngreso(IngresoDb ingreso);
    void eliminarIngreso(Long id);
  
    /**
     * Devuelve todos los ingresos creados por un usuario.
     * @param usuarioId id del usuario
     */
    List<IngresoDb> listarPorUsuario(Long usuarioId);
}
