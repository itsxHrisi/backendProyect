package proyect.proyectefinal.security.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proyect.proyectefinal.exception.FiltroException;
import proyect.proyectefinal.filters.model.PaginaResponse;
import proyect.proyectefinal.filters.model.PeticionListadoFiltrado;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.PaginaDto;
import proyect.proyectefinal.model.dto.UsuarioList;
import proyect.proyectefinal.repository.UsuarioRepository;
import org.springframework.data.domain.Pageable;

import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@Service
@Transactional //Mantiene la coherencia de la BD si hay varios accesos de escritura concurrentes
public interface UsuarioService {

    public Optional<UsuarioDb> getByNickname(String nickname);
    public boolean existsByNickname(String nickname);
    public boolean existsByEmail(String email);
    public void save(@NonNull UsuarioDb usuario);
    Optional<UsuarioDb> findByNickname(String nickname);
    Optional<UsuarioDb> findByEmail(String email);  
    public PaginaDto<UsuarioList> findAll(Pageable paging);

        /**
         * Busca jugadores aplicando filtros, paginación y ordenación a partir de parámetros individuales.
         * 
         * @param filter Array de strings con los filtros en formato "campo:operador:valor"
         * @param page Número de página (zero-based)
         * @param size Tamaño de cada página
         * @param sort Array con criterios de ordenación en formato "campo", "campo,asc" o "campo,desc"
         * @return PaginaResponse con la lista de jugadores filtrada y paginada
         * @throws FiltroException Si hay errores en los filtros o la ordenación (errorCodes: 'BAD_OPERATOR_FILTER','BAD_ATTRIBUTE_ORDER','BAD_ATTRIBUTE_FILTER','BAD_FILTER')
         */
        public PaginaResponse<UsuarioList> findAll(List<String> filter, int page, int size, List<String> sort) throws FiltroException;
        /**
         * Busca jugadores aplicando filtros, paginación y ordenación a partir de una petición estructurada.
         * 
         * @param peticionListadoFiltrado Objeto que encapsula los criterios de búsqueda (nº pagina, tamaño de la página, lista de filtros y lista de ordenaciones)
         * @return PaginaResponse con la lista de jugadores filtrada y paginada
         * @throws FiltroException Si hay errores en los filtros o la ordenación (errorCodes: 'BAD_OPERATOR_FILTER','BAD_ATTRIBUTE_ORDER','BAD_ATTRIBUTE_FILTER','BAD_FILTER')
         */
        public PaginaResponse<UsuarioList> findAll(PeticionListadoFiltrado peticionListadoFiltrado) throws FiltroException;



}