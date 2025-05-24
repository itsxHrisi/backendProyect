package proyect.proyectefinal.model.db;



import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "GrupoFamiliar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoFamiliar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @OneToOne
    @JoinColumn(name = "administrador_id")
    @JsonIgnoreProperties("grupoFamiliar")
    private UsuarioDb administrador;

    @OneToMany(mappedBy = "grupoFamiliar")
    @JsonManagedReference
    private List<UsuarioDb> usuarios;
}
