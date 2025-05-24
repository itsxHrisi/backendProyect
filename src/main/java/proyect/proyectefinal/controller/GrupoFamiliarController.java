package proyect.proyectefinal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
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
 // Helper que deja sólo el rol ROL_USER
    private void limpiarRoles(UsuarioDb usuario) {
        usuario.getRoles().removeIf(r -> !r.getNombre().equals(RolNombre.ROL_USER));
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
    /*/sadsa */
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
        System.out.println("Actualizar");
        // Asignar grupo al usuario
        usuario.setGrupoFamiliar(grupoGuardado);
// Asignar rol ROL_Padre
        RolDb rolPadre = rolRepository.findByNombre(RolNombre.ROL_PADRE)
                .orElseThrow(() -> new RuntimeException("Rol ROL_PADRE no encontrado"));
        usuario.getRoles().add(rolPadre);
        // Asignar rol ROL_ADMIN
        RolDb rolAdmin = rolRepository.findByNombre(RolNombre.ROL_ADMIN)
                .orElseThrow(() -> new RuntimeException("Rol ROL_ADMIN no encontrado"));
        usuario.getRoles().add(rolAdmin);

        // Guardar usuario actualizado
        usuarioService.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(grupoGuardado);
    }
@PutMapping("/limpiar-roles")
@Transactional
public ResponseEntity<?> limpiarRolesUsuario(
        @RequestParam String nicknameDestino,
        Authentication authentication) {

    // 1) Comprueba que el que llama es admin de un grupo
    String nicknameAdmin = authentication.getName();
    UsuarioDb admin = usuarioService.getByNickname(nicknameAdmin)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario autenticado no encontrado"));

    GrupoFamiliar grupo = admin.getGrupoFamiliar();
    if (grupo == null || !grupo.getAdministrador().getId().equals(admin.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Solo el administrador puede limpiar roles");
    }

    // 2) Busca el usuario destino y comprueba pertenece al mismo grupo
    UsuarioDb destino = usuarioService.getByNickname(nicknameDestino)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario destino no encontrado"));
    if (destino.getGrupoFamiliar() == null
     || !destino.getGrupoFamiliar().getId().equals(grupo.getId())) {
        return ResponseEntity.badRequest().body("El usuario no pertenece a tu grupo");
    }

    // 3) Aplica tu helper y guarda
    limpiarRoles(destino);
    usuarioService.save(destino);

    return ResponseEntity.ok("Se han eliminado todos los roles de " 
                              + nicknameDestino + " excepto ROL_USER");
}
 @GetMapping("/{id}/usuarios")
    public ResponseEntity<?> getUsuariosDelGrupo(@PathVariable Long id) {
        // 1) Comprobar que existe el grupo
        Optional<GrupoFamiliar> opt = grupoFamiliarService.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Grupo familiar no encontrado");
        }
        // 2) Obtener miembros
        List<UsuarioDb> miembros = usuarioService.findByGrupoFamiliarId(id);
        return ResponseEntity.ok(miembros);
    }
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
 @PutMapping("/asignar-rol")
    public ResponseEntity<String> asignarRol(@RequestParam String nicknameDestino, @RequestParam String rol) {
        try {
            return ResponseEntity.ok(grupoFamiliarService.asignarRol(nicknameDestino, rol));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
    
    /**  Eliminar TODO el grupo: antes quitamos a todos los miembros del grupo  */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<GrupoFamiliar> grupoOpt = grupoFamiliarService.findById(id);
        if (grupoOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Grupo familiar no encontrado");
        }
        GrupoFamiliar grupo = grupoOpt.get();

        // 1) Para cada usuario en este grupo: desvincular y limpiar roles
        List<UsuarioDb> miembros = usuarioService.findByGrupoFamiliarId(grupo.getId());
        miembros.forEach(u -> {
            u.setGrupoFamiliar(null);
            limpiarRoles(u);
            usuarioService.save(u);
        });

        // 2) Borrar el grupo
        grupoFamiliarService.deleteById(id);
        return ResponseEntity.ok("Grupo familiar eliminado");
    }

    /**  Eliminar un usuario concreto del grupo (por el admin)  */
    @PutMapping("/{grupoId}/eliminar-usuario/{nickname}")
    @Transactional
    public ResponseEntity<?> eliminarUsuarioDelGrupo(
            @PathVariable Long grupoId,
            @PathVariable String nickname,
            Authentication authentication) {

        // … código de comprobaciones de existencia y permisos …

        UsuarioDb usuario = usuarioService.getByNickname(nickname)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + nickname));

        // … comprueba que pertenece al grupo y no es el admin …

        usuario.setGrupoFamiliar(null);
        limpiarRoles(usuario);
        usuarioService.save(usuario);

        return ResponseEntity.ok("Usuario eliminado del grupo familiar correctamente");
    }

    /**  Abandonar el grupo: lo hace el propio usuario  */
    @DeleteMapping("/abandonar")
    @Transactional
    public ResponseEntity<?> abandonarGrupo(Authentication authentication) {
        String nicknameAuth = authentication.getName();
        UsuarioDb usuario = usuarioService.getByNickname(nicknameAuth)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + nicknameAuth));

        if (usuario.getGrupoFamiliar() == null) {
            return ResponseEntity.badRequest().body("No perteneces a ningún grupo familiar");
        }
        if (usuario.getId().equals(usuario.getGrupoFamiliar().getAdministrador().getId())) {
            return ResponseEntity.badRequest().body(
                    "El administrador no puede abandonar su propio grupo. Debe transferir la administración o eliminar el grupo.");
        }

        usuario.setGrupoFamiliar(null);
        limpiarRoles(usuario);
        usuarioService.save(usuario);

        return ResponseEntity.ok("Has abandonado el grupo familiar correctamente");
    }

}
