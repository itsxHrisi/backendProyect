package proyect.proyectefinal.model.dto;
import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEditConPassword {

    private String nombre;
    private String nickname;

    @Email(message = "Formato de email inválido")
    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
        message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número")
    private String password;

    private String password2;
}
