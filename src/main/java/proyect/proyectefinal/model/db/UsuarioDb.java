package proyect.proyectefinal.model.db;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "usuarios")
public class UsuarioDb {
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToOne
    @JoinColumn(name = "grupo_familiar_id")
    @JsonBackReference
    private GrupoFamiliar grupoFamiliar;

    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
 
    @JoinTable(name = "usuarios_roles", joinColumns = @JoinColumn(name = "idUsuario"),
    inverseJoinColumns = @JoinColumn(name = "idRol"))
    private Set<RolDb> roles = new HashSet<>();
    
    public UsuarioDb(String nombre, String nickname, String email, String password) {
        this.nombre = nombre;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }
}
