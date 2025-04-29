package proyect.proyectefinal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.UsuarioEdit;
import proyect.proyectefinal.model.dto.UsuarioEditConPassword;
import proyect.proyectefinal.security.dto.JwtDto;
import proyect.proyectefinal.security.service.JwtService;
import proyect.proyectefinal.security.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin
public class UsuarioController {
    @Autowired
    AuthenticationManager authenticationManager;
 @Autowired
    JwtService jwtProvider;
    @Autowired
    private UsuarioService usuarioService;
@PutMapping("/{nickname}/actualizar")
public ResponseEntity<?> actualizarUsuario(
        @PathVariable String nickname,
        @Valid @RequestBody UsuarioEditConPassword request,
        BindingResult result) {

    if (result.hasErrors()) {
        List<String> errores = result.getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(errores);
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String nicknameAutenticado = auth.getName();

    if (!nickname.equals(nicknameAutenticado)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para modificar los datos de otro usuario");
    }

    UsuarioDb usuario = usuarioService.getByNickname(nickname)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

    if (request.getNombre() != null) usuario.setNombre(request.getNombre());
    if (request.getEmail() != null) usuario.setEmail(request.getEmail());
    if (request.getNickname() != null) usuario.setNickname(request.getNickname());

    // Validar y actualizar contraseña
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
        if (!request.getPassword().equals(request.getPassword2())) {
            return ResponseEntity.badRequest().body("Las contraseñas introducidas no coinciden.");
        }
        usuario.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
    }

    usuarioService.save(usuario);

    // Generar nuevo token
    String nuevoNickname = request.getNickname() != null ? request.getNickname() : nickname;
    String nuevaPassword = request.getPassword() != null ? request.getPassword() : "";

    Authentication nuevaAuth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(nuevoNickname, nuevaPassword));
    SecurityContextHolder.getContext().setAuthentication(nuevaAuth);
    String jwt = jwtProvider.generateToken(nuevaAuth);
    UserDetails userDetails = (UserDetails) nuevaAuth.getPrincipal();
    JwtDto jwtDto = new JwtDto(jwt, userDetails.getUsername(), userDetails.getAuthorities());

    return ResponseEntity.ok(jwtDto);
}




    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDb> verPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nicknameAuth = auth.getName();

        UsuarioDb usuario = usuarioService.getByNickname(nicknameAuth)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(usuario);
    }
}
