package proyect.proyectefinal.model.dto;
import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEditSinPassword {
private String nombre;

    @NotBlank(message = "El nickname no puede estar vacío")
    private String nickname;

    @Email(message = "Formato de email inválido")
    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    @Pattern(
      regexp = "^\\+?\\d{9,15}$",
      message = "El teléfono debe contener sólo dígitos, opcionalmente con '+' y entre 9 y 15 caracteres"
    )
    private String telefono;
}
