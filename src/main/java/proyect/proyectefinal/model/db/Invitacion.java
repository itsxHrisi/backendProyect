package proyect.proyectefinal.model.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname_destino", nullable = false)
    private String nicknameDestino;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoFamiliar grupo;

    @Enumerated(EnumType.STRING)
    private EstadoInvitacion estado;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    // Getters y setters

    public enum EstadoInvitacion {
        PENDIENTE, ACEPTADA, RECHAZADA
    }

    // Getters y Setters
}
