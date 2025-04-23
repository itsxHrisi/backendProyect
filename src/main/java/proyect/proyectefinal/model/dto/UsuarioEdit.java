package proyect.proyectefinal.model.dto;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEdit {
    private String nombre;
    private String nickname;
    private String email;
}
