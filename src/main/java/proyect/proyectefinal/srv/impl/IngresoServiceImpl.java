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
import proyect.proyectefinal.model.dto.IngresoInfo;
import proyect.proyectefinal.repository.IngresoRepository;
import proyect.proyectefinal.service.IngresoService;
import proyect.proyectefinal.srv.mapper.IngresoMapper;

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
  private final IngresoRepository repo;
  private final PeticionListadoFiltradoConverter converter;

  public IngresoServiceImpl(
    IngresoRepository repo,
    PaginationFactory paginationFactory,
    PeticionListadoFiltradoConverter converter
  ) {
    this.repo = repo;
    this.paginationFactory = paginationFactory;
    this.converter = converter;
  }
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

@Override
public PaginaResponse<IngresoInfo> findAll(
        List<String> filter, int page, int size, List<String> sort
) throws FiltroException {
    // 1) convertir params a PeticionListadoFiltrado
    PeticionListadoFiltrado pet = peticionConverter.convertFromParams(filter, page, size, sort);

    // 2) construir pageable
    Pageable pageable = paginationFactory.createPageable(pet);

    // 3) Specification de filtros
    Specification<IngresoDb> spec = new FiltroBusquedaSpecification<>(pet.getListaFiltros());

    // 4) consulta paginada
    Page<IngresoDb> p = ingresoRepository.findAll(spec, pageable);

    // 5) mapear contenido a DTO
    List<IngresoInfo> dtos = p.getContent().stream()
        .map(IngresoMapper.INSTANCE::ingresoToIngresoInfo)
        .toList();

    // 6) construir el PaginaResponse manualmente
    return new PaginaResponse<>(
        p.getNumber(),
        p.getSize(),
        p.getTotalElements(),
        p.getTotalPages(),
        dtos,
        pet.getListaFiltros(),
        pet.getSort()
    );
}

}
