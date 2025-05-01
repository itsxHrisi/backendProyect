package proyect.proyectefinal.filters.utils;


import java.util.List;

import org.springframework.stereotype.Component;

import proyect.proyectefinal.exception.FiltroException;
import proyect.proyectefinal.filters.model.FiltroBusqueda;
import proyect.proyectefinal.filters.model.PeticionListadoFiltrado;

@Component
public class PeticionListadoFiltradoConverter {
    private final FiltroBusquedaFactory filtroBusquedaFactory;

    public PeticionListadoFiltradoConverter(FiltroBusquedaFactory filtroBusquedaFactory) {
        this.filtroBusquedaFactory = filtroBusquedaFactory;
    }

    
    public PeticionListadoFiltrado convertFromParams(
            List<String> filter, 
            int page, 
            int size, 
            List<String> sort) throws FiltroException {
        List<FiltroBusqueda> filtros = filtroBusquedaFactory.crearListaFiltrosBusqueda(filter);
        return new PeticionListadoFiltrado(filtros, page, size, sort);
    }
}
