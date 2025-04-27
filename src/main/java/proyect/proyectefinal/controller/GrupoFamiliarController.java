package proyect.proyectefinal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.model.db.RolDb;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.enums.RolNombre;
import proyect.proyectefinal.repository.GrupoFamiliarRepository;
import proyect.proyectefinal.repository.RolRepository;
import proyect.proyectefinal.security.service.UsuarioService;
import proyect.proyectefinal.service.GrupoFamiliarService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/grupos")
@CrossOrigin
public class GrupoFamiliarController {

    @Autowired
    private GrupoFamiliarService grupoFamiliarService;
    @Autowired
    private GrupoFamiliarRepository grupoFamiliarRepository;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private RolRepository rolRepository;
    @GetMapping
    public List<GrupoFamiliar> getAll() {
        return grupoFamiliarService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<GrupoFamiliar> grupo = grupoFamiliarService.findById(id);
        if (grupo.isPresent()) {
            return ResponseEntity.ok(grupo.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo familiar no encontrado");
        }
    }
@PostMapping
public ResponseEntity<?> createGrupoFamiliar(@RequestBody GrupoFamiliar grupoFamiliar) {
    // Obtener el usuario autenticado
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String nickname = auth.getName();
    UsuarioDb usuario = usuarioService.getByNickname(nickname)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

    // Asignar administrador
    grupoFamiliar.setAdministrador(usuario);
    GrupoFamiliar grupoGuardado = grupoFamiliarService.save(grupoFamiliar);

    // Asignar grupo al usuario
    usuario.setGrupoFamiliar(grupoGuardado);

    // Asignar rol ROL_ADMIN
    RolDb rolAdmin = rolRepository.findByNombre(RolNombre.ROL_ADMIN)
            .orElseThrow(() -> new RuntimeException("Rol ROL_ADMIN no encontrado"));
    usuario.getRoles().add(rolAdmin);

    // Guardar usuario actualizado
    usuarioService.save(usuario);

    return ResponseEntity.status(HttpStatus.CREATED).body(grupoGuardado);
}

    // @PostMapping
    // public ResponseEntity<?> createGrupoFamiliar(@RequestBody GrupoFamiliar grupoFamiliar) {
    //     // Obtener el usuario autenticado
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     String nickname = auth.getName();
    //     UsuarioDb usuario = usuarioService.getByNickname(nickname).orElseThrow(
    //             () -> new UsernameNotFoundException("Usuario no encontrado"));

    //     // Asignar administrador
    //     grupoFamiliar.setAdministrador(usuario);

    //     // Guardar grupo
    //     GrupoFamiliar grupoGuardado = grupoFamiliarService.save(grupoFamiliar);

    //     // Asignar grupo al usuario
    //     usuario.setGrupoFamiliar(grupoGuardado);
    //     usuarioService.save(usuario);

    //     return ResponseEntity.status(HttpStatus.CREATED).body(grupoGuardado);
    // }

    // @PostMapping("/nuevo")
    // public ResponseEntity<GrupoFamiliar> create(@RequestBody GrupoFamiliar grupo) {
    //     return ResponseEntity.ok(grupoFamiliarService.save(grupo));
    // }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGrupo(@PathVariable Long id, @RequestBody GrupoFamiliar grupoActualizado,
            Authentication authentication) {
        Optional<GrupoFamiliar> grupoOptional = grupoFamiliarRepository.findById(id);
        if (grupoOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo familiar no encontrado");
        }

        GrupoFamiliar grupoExistente = grupoOptional.get();

        // Obtener el nickname del usuario autenticado
        String nickname = authentication.getName();

        // Comprobar si el usuario autenticado es el administrador del grupo
        if (!grupoExistente.getAdministrador().getNickname().equals(nickname)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo el administrador puede actualizar este grupo");
        }

        // Actualizar nombre (si quieres permitir modificar el admin, puedes agregar más
        // lógica)
        grupoExistente.setNombre(grupoActualizado.getNombre());
        grupoFamiliarRepository.save(grupoExistente);

        return ResponseEntity.ok(grupoExistente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (grupoFamiliarService.findById(id).isPresent()) {
            grupoFamiliarService.deleteById(id);
            return ResponseEntity.ok("Grupo familiar eliminado");
        } else {
            return ResponseEntity.status(404).body("Grupo familiar no encontrado");
        }
    }
    @PutMapping("/asignar-rol")
    public ResponseEntity<String> asignarRol(@RequestParam String nicknameDestino, @RequestParam String rol) {
        try {
            return ResponseEntity.ok(grupoFamiliarService.asignarRol(nicknameDestino, rol));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
