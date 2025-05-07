package proyect.proyectefinal.model.dto;


import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UsuarioList {
    private Long id;
    private String nombre;
    private String nickname;
    private String email;
    private String telefono;
    //private String fechaNacimiento;
    private Set<RolInfo> roles;

}
