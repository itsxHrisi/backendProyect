package proyect.proyectefinal.model.dto;


import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proyect.proyectefinal.model.db.GrupoFamiliar;

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
    private GrupoFamiliar grupoFamiliar;

    //private String fechaNacimiento;
    private Set<RolInfo> roles;

}
