package proyect.proyectefinal.security.dto;



import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NuevoUsuario {
    @NotBlank
    private String nickname;
    @NotBlank
    private String nombre;
    @Email
    private String email;
    @NotBlank
    private String password;

}
