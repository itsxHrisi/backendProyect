package proyect.proyectefinal.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioPasswordDto {
    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    @Pattern(
      regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
      message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número"
    )
    private String newPassword;

    @NotBlank(message = "Debes repetir la nueva contraseña")
    private String confirmPassword;
}


