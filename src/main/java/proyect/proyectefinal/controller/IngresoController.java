package proyect.proyectefinal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import proyect.proyectefinal.model.dto.IngresoFilterResponse;
import proyect.proyectefinal.model.dto.IngresoInfo;
import proyect.proyectefinal.model.dto.IngresoRequest;
import proyect.proyectefinal.exception.FiltroException;
import proyect.proyectefinal.filters.model.FiltroBusqueda;
import proyect.proyectefinal.filters.model.PaginaResponse;
import proyect.proyectefinal.filters.model.TipoOperacionBusqueda;
import proyect.proyectefinal.filters.specification.FiltroBusquedaSpecification;
import proyect.proyectefinal.helper.PaginationHelper;
import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.model.db.IngresoDb;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.repository.IngresoRepository;
import proyect.proyectefinal.repository.UsuarioRepository;
import proyect.proyectefinal.service.GrupoFamiliarService;
import proyect.proyectefinal.service.IngresoService;
import proyect.proyectefinal.srv.mapper.IngresoMapper;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ingresos")
public class IngresoController {

    @Autowired
    private IngresoService ingresoService;

    @Autowired
    private UsuarioRepository usuarioRepository;
 @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private GrupoFamiliarService grupoFamiliarService;

@PostMapping
public ResponseEntity<?> createIngreso(@RequestBody IngresoRequest ingresoRequest) {
    // 1. Obtener usuario autenticado
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String nicknameAuth = auth.getName();
    Optional<UsuarioDb> optionalUsuario = usuarioRepository.findByNickname(nicknameAuth);

    if (optionalUsuario.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado.");
    }

    UsuarioDb usuarioActual = optionalUsuario.get();

    // 2. Validar que el usuario pertenece a un grupo
    if (usuarioActual.getGrupoFamiliar() == null) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Debes pertenecer a un grupo familiar para crear un ingreso.");
    }

    // 3. Parsear cantidad correctamente (reemplazando ',' por '.')
    String cantidadString = ingresoRequest.getCantidad().replace(',', '.');
    BigDecimal cantidadDecimal;
    try {
        cantidadDecimal = new BigDecimal(cantidadString);
    } catch (NumberFormatException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cantidad inválida.");
    }

    // 4. Construir el ingreso
    IngresoDb nuevoIngreso = IngresoDb.builder()
            .usuario(usuarioActual)
            .grupo(usuarioActual.getGrupoFamiliar())
            .cantidad(cantidadDecimal)
            .fecha(LocalDateTime.now())
            .build();

    // 5. Guardar el ingreso
    IngresoDb saved = ingresoRepository.save(nuevoIngreso);

    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}

    // 2️⃣ Listar todos del grupo
    @GetMapping
    public ResponseEntity<?> listarIngresos() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nick = auth.getName();
        Optional<UsuarioDb> optUser = usuarioRepository.findByNickname(nick);
        if (optUser.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado.");

        UsuarioDb u = optUser.get();
        if (u.getGrupoFamiliar() == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Debes pertenecer a un grupo familiar para listar ingresos.");

        Long grupoId = u.getGrupoFamiliar().getId();
        List<IngresoDb> lista = ingresoService.listarPorGrupo(grupoId);
        return ResponseEntity.ok(lista);
    }

    // 3️⃣ Obtener por id (solo si es de tu grupo)
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nick = auth.getName();
        UsuarioDb u = usuarioRepository.findByNickname(nick)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        IngresoDb ing = ingresoService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));

        if (u.getGrupoFamiliar() == null ||
            !ing.getGrupo().getId().equals(u.getGrupoFamiliar().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a este ingreso");
        }
        return ResponseEntity.ok(ing);
    }

   @PutMapping("/{id}")
public ResponseEntity<?> actualizarIngreso(
        @PathVariable Long id,
        @Valid @RequestBody IngresoRequest req,
        BindingResult br
) {
    if (br.hasErrors()) {
        return ResponseEntity.badRequest().body("Cantidad inválida");
    }

    // 1️⃣ Usuario autenticado
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String nick = auth.getName();
    UsuarioDb u = usuarioRepository.findByNickname(nick)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // 2️⃣ Ingreso a actualizar
    IngresoDb ing = ingresoService.obtenerPorId(id)
            .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));

    // 3️⃣ Permisos: solo miembros del mismo grupo
    if (u.getGrupoFamiliar() == null ||
        !ing.getGrupo().getId().equals(u.getGrupoFamiliar().getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a este ingreso");
    }

    // 4️⃣ Convertir String a BigDecimal (reemplazando coma si hace falta)
    BigDecimal nuevaCantidad;
    try {
        String texto = req.getCantidad().replace(',', '.');
        nuevaCantidad = new BigDecimal(texto);
    } catch (NumberFormatException e) {
        return ResponseEntity.badRequest().body("Cantidad no válida");
    }

    // 5️⃣ Actualizar y guardar
    ing.setCantidad(nuevaCantidad);
    IngresoDb updated = ingresoService.actualizarIngreso(ing);
    return ResponseEntity.ok(updated);
}

@GetMapping("/usuario")
    public ResponseEntity<?> listarIngresosUsuario(Authentication authentication) {
        // 1) Obtener usuario autenticado
        String nick = authentication.getName();
        UsuarioDb u = usuarioRepository.findByNickname(nick)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2) Recuperar e devolver
        List<IngresoDb> lista = ingresoService.listarPorUsuario(u.getId());
        return ResponseEntity.ok(lista);
    }
    // 5️⃣ Eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarIngreso(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nick = auth.getName();
        UsuarioDb u = usuarioRepository.findByNickname(nick)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        IngresoDb ing = ingresoService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));

        if (u.getGrupoFamiliar() == null ||
            !ing.getGrupo().getId().equals(u.getGrupoFamiliar().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a este ingreso");
        }

        ingresoService.eliminarIngreso(id);
        return ResponseEntity.ok("Ingreso eliminado");
    }



    @GetMapping("/filter")
public ResponseEntity<IngresoFilterResponse> filterIngresos(
    @RequestParam(required = false) String nickname,
    @RequestParam(required = false) List<String> filter,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "fecha,desc") List<String> sort
) throws FiltroException {
    // 0️⃣ Usuario autenticado y su grupo
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String nickAuth = auth.getName();
    UsuarioDb usuario = usuarioRepository.findByNickname(nickAuth)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no autenticado"));
    Long grupoId = usuario.getGrupoFamiliar().getId();

    // 1️⃣ Arrancamos spec vacío
    Specification<IngresoDb> spec = Specification.where(null);

    // 2️⃣ Filtrar siempre por grupo
    spec = spec.and((root, query, cb) ->
        cb.equal(root.join("grupo").get("id"), grupoId)
    );

    // 3️⃣ Si nos dieron nickname, añadimos esa condición
    if (nickname != null && !nickname.isBlank()) {
        spec = spec.and((root, query, cb) ->
            cb.equal(root.join("usuario").get("nickname"), nickname)
        );
    }

    // 4️⃣ Filtros extra (cantidad, fecha, etc.)
    if (filter != null && !filter.isEmpty()) {
        List<FiltroBusqueda> filtros = filter.stream().map(f -> {
            String[] p = f.split(":", 3);
            if (p.length != 3) throw new IllegalArgumentException("Filtro inválido: " + f);
            return new FiltroBusqueda(
                p[0],
                TipoOperacionBusqueda.valueOf(p[1]),
                p[2]
            );
        }).toList();
        spec = spec.and(new FiltroBusquedaSpecification<>(filtros));
    }

    // 5️⃣ Paginación y orden
    Pageable pageable = PaginationHelper.createPageable(page, size, sort);

    // 6️⃣ Ejecutar consulta
    Page<IngresoDb> pageResult = ingresoRepository.findAll(spec, pageable);

    // 7️⃣ Mapeo a DTO y suma
    List<IngresoInfo> content = pageResult.getContent().stream()
        .map(IngresoMapper.INSTANCE::ingresoToIngresoInfo)
        .toList();
    BigDecimal totalSum = content.stream()
        .map(IngresoInfo::getCantidad)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 8️⃣ Construir respuesta
    IngresoFilterResponse resp = IngresoFilterResponse.builder()
        .content(content)
        .totalSum(totalSum)
        .number(pageResult.getNumber())
        .size(pageResult.getSize())
        .totalElements(pageResult.getTotalElements())
        .totalPages(pageResult.getTotalPages())
        .appliedFilters(filter != null
            ? filter.stream().map(f -> {
                String[] p = f.split(":", 3);
                return new FiltroBusqueda(p[0], TipoOperacionBusqueda.valueOf(p[1]), p[2]);
              }).toList()
            : List.of())
        .sort(sort)
        .build();

    return ResponseEntity.ok(resp);
}

}
