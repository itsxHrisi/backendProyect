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
import proyect.proyectefinal.model.dto.ListadoRespuesta;
import proyect.proyectefinal.model.dto.PaginaDto;
import proyect.proyectefinal.model.dto.UsuarioEdit;
import proyect.proyectefinal.model.dto.UsuarioEditSinPassword;
import proyect.proyectefinal.model.dto.UsuarioList;
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
    @Autowired
    UserDetailsService userDetailsService;
@PutMapping("/{nickname}/actualizar")
public ResponseEntity<?> actualizarUsuario(
        @PathVariable String nickname,
        @Valid @RequestBody UsuarioEditSinPassword request,
        BindingResult bindingResult) {

    // 0) errores de validación
    if (bindingResult.hasErrors()) {
        List<String> errores = bindingResult.getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
        return ResponseEntity.badRequest().body(Map.of("errores", errores));
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String nicknameAuth = auth.getName();
    // 1) solo el propio usuario
    if (!nicknameAuth.equals(nickname)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body("No tienes permiso para editar otro usuario");
    }

    // 2) recuperar la entidad
    UsuarioDb usuario = usuarioService.findByNickname(nickname)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

    // 3) verificar si el email o el nickname ya están en uso por OTRO usuario
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

    // 4) aplicar cambios
    usuario.setNombre(request.getNombre());
    usuario.setEmail(request.getEmail());
    usuario.setNickname(request.getNickname());
    usuario.setTelefono(request.getTelefono());

    // 5) guardar
    usuarioService.save(usuario);

    // 6) recargar UserDetails y generar nuevo token
    UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getNickname());
    UsernamePasswordAuthenticationToken newAuth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    String token = jwtProvider.generateToken(newAuth);

    JwtDto jwtDto = new JwtDto(token, userDetails.getUsername(), userDetails.getAuthorities());
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

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDb> verPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nicknameAuth = auth.getName();

        UsuarioDb usuario = usuarioService.getByNickname(nicknameAuth)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(usuario);
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

