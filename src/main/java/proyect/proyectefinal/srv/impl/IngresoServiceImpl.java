package proyect.proyectefinal.srv.impl;


import org.springframework.beans.factory.annotation.Autowired;
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
import proyect.proyectefinal.model.db.IngresoDb;
import proyect.proyectefinal.repository.IngresoRepository;
import proyect.proyectefinal.service.IngresoService;

import java.util.List;
import java.util.Optional;

@Service
public class IngresoServiceImpl implements IngresoService {
    @Autowired
    private PaginationFactory paginationFactory;
    @Autowired
    private PeticionListadoFiltradoConverter peticionConverter;
    @Autowired
    private IngresoRepository ingresoRepository;

    @Override
    public IngresoDb crearIngreso(IngresoDb ingreso) {
        return ingresoRepository.save(ingreso);
    }

    @Override
    public List<IngresoDb> listarPorGrupo(Long grupoId) {
        return ingresoRepository.findByGrupoId(grupoId);
    }

    @Override
    public Optional<IngresoDb> obtenerPorId(Long id) {
        return ingresoRepository.findById(id);
    }

    @Override
    public IngresoDb actualizarIngreso(IngresoDb ingreso) {
        return ingresoRepository.save(ingreso);
    }

    @Override
    public void eliminarIngreso(Long id) {
        ingresoRepository.deleteById(id);
    }

     @Override
    public List<IngresoDb> listarPorUsuario(Long usuarioId) {
        return ingresoRepository.findByUsuarioId(usuarioId);
    }


}
