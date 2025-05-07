package proyect.proyectefinal.model.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEdit {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "El nickname no puede estar vacío")
    private String nickname;

    @Email(message = "El formato del email no es válido")
    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
        message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número"
    )
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

    @Pattern(
        regexp = "^\\+?\\d{9,15}$",
        message = "El teléfono debe contener sólo dígitos, opcionalmente con '+' y entre 9 y 15 caracteres"
      )
      private String telefono;
}


