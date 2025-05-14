package proyect.proyectefinal.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import proyect.proyectefinal.model.db.Mensaje;
import proyect.proyectefinal.model.db.RolDb;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.LoginUsuario;
import proyect.proyectefinal.model.dto.MensajeText;
import proyect.proyectefinal.model.enums.RolNombre;
import proyect.proyectefinal.repository.SesionActivaRepository;
import proyect.proyectefinal.repository.UsuarioRepository;
import proyect.proyectefinal.security.dto.JwtDto;
import proyect.proyectefinal.security.dto.NuevoUsuario;
import proyect.proyectefinal.security.entity.SesionActiva;
import proyect.proyectefinal.security.service.JwtService;
import proyect.proyectefinal.security.service.RolService;
import proyect.proyectefinal.security.service.UsuarioService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@CrossOrigin 
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;
@Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    UsuarioService usuarioService;
 @Autowired
    SesionActivaRepository sesionActivaRepository;
    @Autowired
    RolService rolService;

    @Autowired
    JwtService jwtProvider;

    @PostMapping("/nuevo")
public ResponseEntity<?> nuevo(@Valid @RequestBody NuevoUsuario nuevoUsuario, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
        List<String> errores = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    if (usuarioService.existsByNickname(nuevoUsuario.getNickname()))
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeText("El nickname del usuario ya existe"));

    if (usuarioService.existsByEmail(nuevoUsuario.getEmail()))
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeText("El email del usuario ya existe"));

    UsuarioDb usuarioDb = new UsuarioDb(
        nuevoUsuario.getNombre(),
        nuevoUsuario.getNickname(),
        nuevoUsuario.getEmail(),
        passwordEncoder.encode(nuevoUsuario.getPassword()),
        nuevoUsuario.getTelefono()
        
    );

    Set<RolDb> rolesDb = new HashSet<>();
    rolesDb.add(rolService.getByRolNombre(RolNombre.ROL_USER).orElseThrow());
    usuarioDb.setRoles(rolesDb);

    usuarioService.save(usuarioDb);
    return ResponseEntity.status(HttpStatus.CREATED).body(new MensajeText("Usuario creado"));
}


     @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginUsuario loginUsuario,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MensajeText("Datos incorrectos"));
        }

        // 1) Autenticación
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginUsuario.getNickname(), 
                loginUsuario.getPassword()
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2) Generar token JWT
        String jwt = jwtProvider.generateToken(authentication);

        // 3) Recuperar entidad UsuarioDb
        String nickname = ((UserDetails) authentication.getPrincipal()).getUsername();
        UsuarioDb usuario = usuarioRepository.findByNickname(nickname).orElseThrow(() ->
                new UsernameNotFoundException("Usuario no encontrado tras login")
            );

        // 4) Crear y guardar SesionActiva
        SesionActiva sesion = new SesionActiva(usuario, jwt);
        sesionActivaRepository.save(sesion);

        // 5) Devolver DTO
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        JwtDto jwtDto = new JwtDto(jwt, userDetails.getUsername(), userDetails.getAuthorities());
        return ResponseEntity.ok(jwtDto);
    }

     /**
     * Logout de usuario:
     * Borra la SesionActiva y el RefreshToken de la BD.
     */
    @Operation(summary = "Cerrar sesión del usuario",
               description = "Elimina la sesión activa y su refresh token de la base de datos.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sesión cerrada correctamente"),
        @ApiResponse(responseCode = "400", description = "Token inválido o sesión no encontrada",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = Mensaje.class))),
        @ApiResponse(responseCode = "500", description = "Error interno al cerrar sesión",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = Mensaje.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Mensaje> logout(@RequestHeader("Authorization") String authHeader) {
        // 1) Extraer token JWT
        String token = authHeader;
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7);
        }
        logger.info("Logout solicitado, token: {}", token);

        try {
            // 2) Buscar sesión activa
            Optional<SesionActiva> optSesion = sesionActivaRepository.findByTokenSesion(token);
            if (optSesion.isEmpty()) {
                logger.warn("Logout fallido: sesión no encontrada para token {}", token);
                return ResponseEntity
                    .badRequest()
                    .body(new Mensaje("Token inválido o sesión no encontrada"));
            }

            // 3) Borrar sesión activa
            SesionActiva sesion = optSesion.get();
            sesionActivaRepository.delete(sesion);
            logger.info("Sesión activa borrada (id={})", sesion.getId());


            // 5) Responder OK
            return ResponseEntity
                .ok(new Mensaje("Sesión cerrada correctamente"));

        } catch (Exception ex) {
            logger.error("Error interno durante logout", ex);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Mensaje("Error interno al cerrar sesión"));
        }
    }
}






