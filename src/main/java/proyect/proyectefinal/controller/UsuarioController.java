package proyect.proyectefinal.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;
import proyect.proyectefinal.exception.CustomErrorResponse;
import proyect.proyectefinal.exception.FiltroException;
import proyect.proyectefinal.filters.model.PaginaResponse;
import proyect.proyectefinal.filters.model.PeticionListadoFiltrado;
import proyect.proyectefinal.helper.PaginationHelper;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.CambioPasswordDto;
import proyect.proyectefinal.model.dto.ListadoRespuesta;
import proyect.proyectefinal.model.dto.PaginaDto;
import proyect.proyectefinal.model.dto.UsuarioEdit;
import proyect.proyectefinal.model.dto.UsuarioEditSinPassword;
import proyect.proyectefinal.model.dto.UsuarioInfo;
import proyect.proyectefinal.model.dto.UsuarioList;
import proyect.proyectefinal.repository.SesionActivaRepository;
import proyect.proyectefinal.security.dto.JwtDto;
import proyect.proyectefinal.security.entity.SesionActiva;
import proyect.proyectefinal.security.service.JwtService;
import proyect.proyectefinal.security.service.UsuarioService;
import proyect.proyectefinal.srv.mapper.UsuarioMapper;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin
public class UsuarioController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtProvider;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SesionActivaRepository sesionActivaRepository;
@PutMapping("/{nickname}/actualizar")
public ResponseEntity<JwtDto> actualizarUsuario(
        @PathVariable String nickname,
        @Valid @RequestBody UsuarioEditSinPassword request,
        BindingResult bindingResult,
        @RequestHeader("Authorization") String authHeader  // <— <===
) {
    // 0) Extraer token viejo
    String oldToken = authHeader.startsWith("Bearer ")
        ? authHeader.substring(7)
        : authHeader;

    // 1) Validación de errores de binding
    if (bindingResult.hasErrors()) {
        List<String> errores = bindingResult.getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
        return ResponseEntity.badRequest()
            .body(new JwtDto("Null o token no encontrado",null, List.of()));
    }

    // 2) Solo el propio usuario
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!auth.getName().equals(nickname)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // 3) Recuperar la entidad
    UsuarioDb usuario = usuarioService.findByNickname(nickname)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

    // 4) Comprobar unicidad de email y nickname
    usuarioService.findByEmail(request.getEmail())
        .filter(u -> !u.getId().equals(usuario.getId()))
        .ifPresent(u -> {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "El email ya está en uso");
        });
    usuarioService.findByNickname(request.getNickname())
        .filter(u -> !u.getId().equals(usuario.getId()))
        .ifPresent(u -> {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "El nickname ya está en uso");
        });

    // 5) Aplicar cambios
    usuario.setNombre(request.getNombre());
    usuario.setEmail(request.getEmail());
    usuario.setNickname(request.getNickname());
    usuario.setTelefono(request.getTelefono());
    usuarioService.save(usuario);

    // 6) Generar nuevo JWT
    UserDetails userDetails = userDetailsService
        .loadUserByUsername(usuario.getNickname());
    UsernamePasswordAuthenticationToken newAuth = 
        new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
    String newToken = jwtProvider.generateToken(newAuth);

    // 7) Actualizar SesionActiva
    sesionActivaRepository.findByTokenSesion(oldToken)
        .ifPresent(oldSesion -> {
            sesionActivaRepository.delete(oldSesion);
        });
    // crear nueva
    SesionActiva nuevaSesion = new SesionActiva(usuario, newToken);
    sesionActivaRepository.save(nuevaSesion);

    // 8) Devolver el nuevo token al cliente
    JwtDto jwtDto = new JwtDto(newToken,
        userDetails.getUsername(),
        userDetails.getAuthorities());
    return ResponseEntity.ok(jwtDto);
}

@PutMapping("/{nickname}/cambiar-password")
public ResponseEntity<?> cambiarPassword(
    @PathVariable String nickname,
    @Valid @RequestBody CambioPasswordDto dto,
    BindingResult br,
    @RequestHeader("Authorization") String authHeader
) {
    // 1️⃣ Errores de validación de DTO
    if (br.hasErrors()) {
        List<String> errores = br.getFieldErrors()
            .stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
        return ResponseEntity
                .badRequest()
                .body(Map.of("errores", errores));
    }

    // 2️⃣ Sólo el propio usuario
    String nicknameAuth = SecurityContextHolder.getContext()
        .getAuthentication().getName();
    if (!nicknameAuth.equals(nickname)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permiso para cambiar la contraseña de otro usuario");
    }

    // 3️⃣ Comprobar que las dos contraseñas coinciden
    if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
        return ResponseEntity.badRequest()
                .body("Las contraseñas no coinciden");
    }

    // 4️⃣ Recuperar entidad y actualizar contraseña
    UsuarioDb usuario = usuarioService.findByNickname(nickname)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    usuario.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    usuarioService.save(usuario);

    // 5️⃣ Generar nuevo JWT
    UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getNickname());
    UsernamePasswordAuthenticationToken newAuth =
        new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
    String nuevoJwt = jwtProvider.generateToken(newAuth);

    // 6️⃣ Eliminar la sesión activa vieja
    String viejoToken = authHeader.startsWith("Bearer ")
        ? authHeader.substring(7)
        : authHeader;
    sesionActivaRepository.findByTokenSesion(viejoToken)
        .ifPresent(sesionActivaRepository::delete);

    // 7️⃣ Crear y guardar la nueva sesión activa
    SesionActiva nuevaSesion = new SesionActiva(usuario, nuevoJwt);
    sesionActivaRepository.save(nuevaSesion);

    // 8️⃣ Devolver el nuevo JwtDto
    JwtDto jwtDto = new JwtDto(nuevoJwt,
        userDetails.getUsername(),
        userDetails.getAuthorities());
    return ResponseEntity.ok(jwtDto);
}


    @GetMapping("/usuarios")
    public ResponseEntity<ListadoRespuesta<UsuarioList>> getAllUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Pageable paging = PaginationHelper.createPageable(page, size, sort);

        PaginaDto<UsuarioList> paginaUsuariosList = usuarioService.findAll(paging);

        ListadoRespuesta<UsuarioList> response = new ListadoRespuesta<>(
                paginaUsuariosList.getNumber(),
                paginaUsuariosList.getSize(),
                paginaUsuariosList.getTotalElements(),
                paginaUsuariosList.getTotalPages(),
                paginaUsuariosList.getContent());

        return ResponseEntity.ok(response);
    }

    // @GetMapping("/perfil")
    // public ResponseEntity<UsuarioDb> verPerfil() {
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     String nicknameAuth = auth.getName();

    //     UsuarioDb usuario = usuarioService.getByNickname(nicknameAuth)
    //             .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    //     return ResponseEntity.ok(usuario);
    // }

    @GetMapping("/perfil")
public ResponseEntity<UsuarioInfo> verPerfil() {
  String nicknameAuth = SecurityContextHolder.getContext().getAuthentication().getName();
  UsuarioDb usuario = usuarioService.getByNickname(nicknameAuth)
      .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

  // Usamos el singleton de MapStruct en lugar de inyectar
  UsuarioInfo info = UsuarioMapper.INSTANCE.usuarioDbToUsuarioInfo(usuario);

  return ResponseEntity.ok(info);
}
   /**
     * 2a) GET /api/usuarios/filter
     *     Devuelve usuarios paginados aplicando filtros por query-params:
     *       ?filter=campo:OPERADOR:valor
     */
    @GetMapping("/filter")
    public ResponseEntity<PaginaResponse<UsuarioList>> getUsuariosFiltrados(
            @RequestParam List<String> filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") List<String> sort
    ) throws FiltroException {
        PaginaResponse<UsuarioList> resultado =
            usuarioService.findAll(filter, page, size, sort);
        return ResponseEntity.ok(resultado);
    }

    /**
     * 2b) POST /api/usuarios/filter
     *     Devuelve usuarios paginados aplicando un objeto de filtros completo:
     *     {
     *       "listaFiltros": [{ "atributo":"roles", "operacion":"CONTiene", "valor":"USUARIO" }],
     *       "page": 0,
     *       "size": 10,
     *       "sort": ["id,asc"]
     *     }
     */
    @PostMapping("/filter")
    public ResponseEntity<?> getUsuariosFiltradosBody(
            @Valid @RequestBody PeticionListadoFiltrado peticion,
            BindingResult bindingResult
    ) throws FiltroException {
        if (bindingResult.hasErrors()) {
            List<String> errores = bindingResult.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
            return ResponseEntity.badRequest().body(errores);
        }
        PaginaResponse<UsuarioList> resultado =
            usuarioService.findAll(peticion);
        return ResponseEntity.ok(resultado);
    }
}

