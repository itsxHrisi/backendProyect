package proyect.proyectefinal.model.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;
import proyect.proyectefinal.filters.model.FiltroBusqueda;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngresoFilterResponse {
  private List<IngresoInfo> content;
  private BigDecimal totalSum;
  private int number, size, totalPages;
  private long totalElements;
  private List<FiltroBusqueda> appliedFilters;
  private List<String> sort;
}