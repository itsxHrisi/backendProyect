package proyect.proyectefinal.srv.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.JpaSystemException;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

import proyect.proyectefinal.exception.FiltroException;
import proyect.proyectefinal.filters.model.PaginaResponse;
import proyect.proyectefinal.filters.model.PeticionListadoFiltrado;
import proyect.proyectefinal.filters.specification.FiltroBusquedaSpecification;
import proyect.proyectefinal.filters.utils.PaginationFactory;
import proyect.proyectefinal.filters.utils.PeticionListadoFiltradoConverter;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.PaginaDto;
import proyect.proyectefinal.model.dto.UsuarioList;
import proyect.proyectefinal.repository.RolRepository;
import proyect.proyectefinal.repository.UsuarioRepository;
import proyect.proyectefinal.security.service.UsuarioService;
import proyect.proyectefinal.srv.mapper.UsuarioMapper;

@Service
public class UsuarioServiceImpl implements UsuarioService {
     @Autowired
    UsuarioRepository usuarioRepository;

    private final RolRepository rolRepository;
    private final PaginationFactory paginationFactory;
    private final PeticionListadoFiltradoConverter peticionConverter;
    public Optional<UsuarioDb> getByNickname(String nickname){
        return usuarioRepository.findByNickname(nickname);
    }

    public boolean existsByNickname(String nickname){
        return usuarioRepository.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email){
        return usuarioRepository.existsByEmail(email);
    }

    public void save(@NonNull UsuarioDb usuario){
        usuarioRepository.save(usuario);
    }





    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository, 
            RolRepository rolRepository,
            PaginationFactory paginationFactory,
            PeticionListadoFiltradoConverter peticionConverter) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.paginationFactory = paginationFactory;
        this.peticionConverter = peticionConverter;
    }

    @Override
    public PaginaResponse<UsuarioList> findAll(List<String> filter, int page, int size, List<String> sort) 
            throws FiltroException {
        /** 'peticionConverter' está en el constructor del service porque utilizando una buena arquitectura
        toda clase externa al Service que contenga un método a ejecutar debería ser testeable de manera
        independiente y para ello debe de estar en el constructor para poderse mockear.**/
        PeticionListadoFiltrado peticion = peticionConverter.convertFromParams(filter, page, size, sort);
        return findAll(peticion);// En vez de hacer 2 veces el filtrado llamamos al método que lo centraliza
    }

   
    @SuppressWarnings("null")
    @Override
    public PaginaResponse<UsuarioList> findAll(PeticionListadoFiltrado peticionListadoFiltrado) throws FiltroException {
        /** 'paginationFactory' está en el constructor del service porque utilizando una buena arquitectura
        toda clase externa al Service que contenga un método a ejecutar debería ser testeable de manera
        independiente y para ello debe de estar en el constructor para poderse mockear.**/
        try {
            // Configurar ordenamiento
            Pageable pageable = paginationFactory.createPageable(peticionListadoFiltrado);
            // Configurar criterio de filtrado con Specification
            Specification<UsuarioDb> filtrosBusquedaSpecification = new FiltroBusquedaSpecification<UsuarioDb>(
                peticionListadoFiltrado.getListaFiltros());
            // Filtrar y ordenar: puede producir cualquier de los errores controlados en el catch
            Page<UsuarioDb> page = usuarioRepository.findAll(filtrosBusquedaSpecification, pageable);
            //Devolver respuesta
            return UsuarioMapper.pageToPaginaResponse(
                page,
                peticionListadoFiltrado.getListaFiltros(), 
                peticionListadoFiltrado.getSort());
        } catch (JpaSystemException e) {
            String cause="";
            if  (e.getRootCause()!=null){
                if (e.getCause().getMessage()!=null)
                    cause= e.getRootCause().getMessage();
            }
            throw new FiltroException("BAD_OPERATOR_FILTER",
                    "Error: No se puede realizar esa operación sobre el atributo por el tipo de dato", e.getMessage()+":"+cause);
        } catch (PropertyReferenceException e) {
            throw new FiltroException("BAD_ATTRIBUTE_ORDER",
                    "Error: No existe el nombre del atributo de ordenación en la tabla", e.getMessage());
        } catch (InvalidDataAccessApiUsageException e) {
            throw new FiltroException("BAD_ATTRIBUTE_FILTER", "Error: Posiblemente no existe el atributo en la tabla",
                    e.getMessage());
        }
}

@Override
public PaginaDto<UsuarioList> findAll(Pageable paging) {
    Page<UsuarioDb> paginaUsuarioDb=usuarioRepository.findAll(paging);
    return new PaginaDto<UsuarioList>(
        paginaUsuarioDb.getNumber(),
        paginaUsuarioDb.getSize(),
        paginaUsuarioDb.getTotalElements(),
        paginaUsuarioDb.getTotalPages(),
        UsuarioMapper.INSTANCE.usuariosDbToUsuariosList(paginaUsuarioDb.getContent()),
        paginaUsuarioDb.getSort());
}

@Override
public Optional<UsuarioDb> findByNickname(String nickname) {
        return usuarioRepository.findByNickname(nickname);
}

@Override
public Optional<UsuarioDb> findByEmail(String email) {
           return usuarioRepository.findByEmail(email);

}

}