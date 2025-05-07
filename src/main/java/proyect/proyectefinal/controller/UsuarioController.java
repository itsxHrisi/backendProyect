package proyect.proyectefinal.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
import proyect.proyectefinal.model.dto.UsuarioEditConPassword;
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

        if (request.getNombre() != null)
            usuario.setNombre(request.getNombre());
        if (request.getEmail() != null)
            usuario.setEmail(request.getEmail());
        if (request.getNickname() != null)
            usuario.setNickname(request.getNickname());

        // Validar y actualizar contraseña
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (!request.getPassword().equals(request.getPassword2())) {
                return ResponseEntity.badRequest().body("Las contraseñas introducidas no coinciden.");
            }
            usuario.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
        }
        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
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

