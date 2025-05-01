package proyect.proyectefinal.filters.specification.operacion;
import proyect.proyectefinal.filters.model.FiltroBusqueda;
import proyect.proyectefinal.filters.model.TipoOperacionBusqueda;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class EmpiezaOperacionStrategy implements OperacionBusquedaStrategy {
    @Override
     public Predicate crearPredicado(
        Root<?> root, 
        CriteriaBuilder criteriaBuilder, 
        FiltroBusqueda filtro
    ) {
        return criteriaBuilder.like(
            root.get(filtro.getAtributo()), filtro.getValor() + "%");
    }

    @Override
    public boolean soportaOperacion(TipoOperacionBusqueda operacion) {
        return operacion == TipoOperacionBusqueda.EMPIEZA;
    }
}