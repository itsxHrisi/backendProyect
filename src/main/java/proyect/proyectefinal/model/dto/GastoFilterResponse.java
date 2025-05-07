package proyect.proyectefinal.model.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;
import proyect.proyectefinal.filters.model.FiltroBusqueda;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GastoFilterResponse {
    private List<GastoInfo> content;
    private BigDecimal totalSum;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<FiltroBusqueda> appliedFilters;
    private List<String> sort;
}
