package proyect.proyectefinal.model.dto;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginUsuario {
    @Size(min = 5, message = "El nickname debe de tener un tamaño minimo de 5 carácteres")
    private String nickname;
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "El password debe tener al menos 8 caracteres, una mayuscula, una minuscula, un numero y un caracter especioal"
    )
    private String password;
}
