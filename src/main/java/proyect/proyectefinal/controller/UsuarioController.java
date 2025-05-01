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
         * Constructor para inyectar el servicio de Usuarios.
         *
         * @param usuarioService Servicio que maneja la lógica de negocio de los
         *                       Usuarios.
         */
        public UsuarioController(UsuarioService usuarioService) {
                this.usuarioService = usuarioService;

        }

         /**
        * Obtiene una lista paginada de Usuarios con opción de filtrado y ordenación.
        *
        * @param filter Opcional. Filtros de búsqueda en formato `campo:operador:valor`
        *               (Ej: "nombre:contiene:Messi").
        * @param page   Número de página (por defecto 0).
        * @param size   Cantidad de elementos por página (por defecto 3).
        * @param sort   Ordenación en formato `campo,direccion` (Ej: "idEquipo,asc").
        * @return Una respuesta con la lista paginada de Usuarios.
        * @throws FiltroException Si ocurre un error en la aplicación de filtros.
        */
       @Operation(summary = "Obtener Usuarios paginados", description = "Retorna una lista paginada de Usuarios aplicando filtros y ordenación opcionales.")
       @ApiResponses({
       @ApiResponse(responseCode = "200", description = "Lista de Usuarios obtenida correctamente", content = {
                       @Content(mediaType = "application/json", schema = @Schema(implementation = PaginaResponse.class)) }),
       @ApiResponse(responseCode = "400", description = "Bad Request: Errores de filtrado u ordenación (errorCodes: 'BAD_OPERATOR_FILTER','BAD_ATTRIBUTE_ORDER','BAD_ATTRIBUTE_FILTER','BAD_FILTER'", content = {
                       @Content(mediaType = "application/json", schema = @Schema(implementation = CustomErrorResponse.class)) }),
       @ApiResponse(responseCode = "500", description = "Error interno del servidor") })
       @GetMapping("/v1/usuarios")
       public ResponseEntity<PaginaResponse<UsuarioList>> getAllUsuarios(
                       @RequestParam(required = false) List<String> filter,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "3") int size,
                       @RequestParam(defaultValue = "id,asc") List<String> sort) throws FiltroException {
               return ResponseEntity.ok(usuarioService.findAll(filter, page, size, sort));
       }

       /**
        * Obtiene una lista paginada de Usuarios mediante una solicitud POST con un
        * objeto de filtros.
        *
        * @param peticionListadoFiltrado Objeto con los filtros de búsqueda y opciones
        *                                de paginación.
        * @return Una respuesta con la lista paginada de Usuarios.
        * @throws FiltroException Si ocurre un error en la aplicación de filtros.
        */
       @Operation(summary = "Obtener Usuarios con filtros avanzados", description = "Retorna Usuarios paginados enviando los filtros en el cuerpo de la petición.")
       @ApiResponses({
                       @ApiResponse(responseCode = "200", description = "Lista de Usuarios obtenida correctamente", content = {
                                       @Content(mediaType = "application/json", schema = @Schema(implementation = PaginaResponse.class)) }),
                       @ApiResponse(responseCode = "400", description = "Bad Request: Errores de filtrado u ordenación (errorCodes: 'BAD_OPERATOR_FILTER','BAD_ATTRIBUTE_ORDER','BAD_ATTRIBUTE_FILTER','BAD_FILTER')", content = {
                                       @Content(mediaType = "application/json", schema = @Schema(implementation = CustomErrorResponse.class)) }),
                       @ApiResponse(responseCode = "500", description = "Error interno del servidor")
       })
       @PostMapping("/v1/usuarios/x")
       public ResponseEntity<PaginaResponse<UsuarioList>> getAllUsuariosPost(
                       @Valid @RequestBody PeticionListadoFiltrado peticionListadoFiltrado) throws FiltroException {
               return ResponseEntity.ok(usuarioService.findAll(
                               peticionListadoFiltrado));
       }
}
