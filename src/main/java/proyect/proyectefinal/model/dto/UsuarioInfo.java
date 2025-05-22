package proyect.proyectefinal.model.dto;


import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UsuarioInfo {
  private Long id;
  private String nombre;
  private String nickname;
  private String email;
  private String telefono;
  private Long grupoFamiliarId;
  private String grupoFamiliarNombre;
  private Set<RolInfo> roles; 
  // getters y setters...
}