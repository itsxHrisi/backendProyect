package proyect.proyectefinal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import proyect.proyectefinal.model.db.Gasto;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.GastoCreateRequest;
import proyect.proyectefinal.model.dto.GastoEdit;
import proyect.proyectefinal.model.dto.GastoInfo;
import proyect.proyectefinal.model.enums.SubtipoGasto;
import proyect.proyectefinal.model.enums.TipoGasto;
import proyect.proyectefinal.repository.GastoRepository;
import proyect.proyectefinal.repository.GrupoFamiliarRepository;
import proyect.proyectefinal.repository.UsuarioRepository;
import proyect.proyectefinal.srv.mapper.GastoMapper;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoFamiliarRepository grupoFamiliarRepository;
@PostMapping
public ResponseEntity<?> createGasto(@RequestBody GastoCreateRequest gastoRequest) {
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
                .body("Debes pertenecer a un grupo familiar para crear un gasto.");
    }

    // 3. Validar tipo y subtipo
    try {
        TipoGasto tipo = TipoGasto.valueOf(gastoRequest.getTipoGasto());
        SubtipoGasto subtipo = SubtipoGasto.valueOf(gastoRequest.getSubtipo());

        if (!isSubtipoValidForTipo(tipo, subtipo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El subtipo no corresponde con el tipo.");
        }
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo o subtipo inválido.");
    }

    // 4. Parsear cantidad correctamente
    String cantidadString = gastoRequest.getCantidad().replace(",", "."); // 💥 reemplazar , por .
    BigDecimal cantidadDecimal;
    try {
        cantidadDecimal = new BigDecimal(cantidadString);
    } catch (NumberFormatException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cantidad inválida.");
    }

    // 5. Construir el gasto
    Gasto nuevoGasto = Gasto.builder()
            .usuario(usuarioActual)
            .grupo(usuarioActual.getGrupoFamiliar())
            .tipoGasto(gastoRequest.getTipoGasto())
            .subtipo(gastoRequest.getSubtipo())
            .cantidad(cantidadDecimal)
            .fecha(gastoRequest.getFecha())
            .build();

    // 6. Guardar el gasto
    Gasto saved = gastoRepository.save(nuevoGasto);

    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}

    @GetMapping
    public List<GastoInfo> getAllGastos() {
        return gastoRepository.findAll().stream()
                .map(GastoMapper.INSTANCE::gastoToGastoInfo)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGasto(@PathVariable Long id) {
        Optional<Gasto> gastoOptional = gastoRepository.findById(id);
        if (gastoOptional.isPresent()) {
            GastoInfo dto = GastoMapper.INSTANCE.gastoToGastoInfo(gastoOptional.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El gasto no existe");
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGasto(@PathVariable Long id, @RequestBody GastoEdit gastoEdit) {
        Optional<Gasto> optionalGasto = gastoRepository.findById(id);
        if (optionalGasto.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Gasto no encontrado");
        }
    
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nicknameAuth = auth.getName();
        Gasto gasto = optionalGasto.get();
    
        boolean esCreador = gasto.getUsuario().getNickname().equals(nicknameAuth);
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROL_ADMIN"));
    
        if (!esCreador && !esAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para modificar este gasto");
        }
    
        try {
            TipoGasto tipo = TipoGasto.valueOf(gastoEdit.getTipoGasto());
            SubtipoGasto subtipo = SubtipoGasto.valueOf(gastoEdit.getSubtipo());
            if (!isSubtipoValidForTipo(tipo, subtipo)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El subtipo no corresponde con el tipo");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo o subtipo inválido");
        }
    
        // ✅ Corregir cantidad con coma
        BigDecimal cantidadDecimal;
        try {
            String cantidadNormalizada = gastoEdit.getCantidad().replace(",", ".");
            cantidadDecimal = new BigDecimal(cantidadNormalizada);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Cantidad inválida");
        }
    
        // Manualmente actualizar
        gasto.setTipoGasto(gastoEdit.getTipoGasto());
        gasto.setSubtipo(gastoEdit.getSubtipo());
        gasto.setCantidad(cantidadDecimal);
        gasto.setFecha(gastoEdit.getFecha());
    
        Gasto updated = gastoRepository.save(gasto);
        return ResponseEntity.ok(GastoMapper.INSTANCE.gastoToGastoInfo(updated));
    }
    
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGasto(@PathVariable Long id) {
        if (!gastoRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Gasto no encontrado");
        }
        gastoRepository.deleteById(id);
        return ResponseEntity.ok("Gasto eliminado");
    }

    @GetMapping("/suma-gastos")
    public ResponseEntity<?> sumarGastos(
            @RequestParam String tipoGasto,
            @RequestParam(required = false) String subtipo) {

        // Obtener usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nicknameAuth = auth.getName();

        Optional<UsuarioDb> usuarioOpt = usuarioRepository.findByNickname(nicknameAuth);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado correctamente");
        }

        UsuarioDb usuario = usuarioOpt.get();

        // Validar TipoGasto
        TipoGasto tipo;
        try {
            tipo = TipoGasto.valueOf(tipoGasto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Tipo de gasto inválido");
        }

        // Obtener todos los gastos del usuario
        List<Gasto> gastosUsuario = gastoRepository.findAll().stream()
                .filter(g -> g.getUsuario().getId().equals(usuario.getId()))
                .collect(Collectors.toList());

        BigDecimal sumaTotal;

        // Si hay subtipo filtramos por subtipo
        if (subtipo != null && !subtipo.isBlank()) {
            // Validar subtipo
            SubtipoGasto subtipoEnum;
            try {
                subtipoEnum = SubtipoGasto.valueOf(subtipo);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Subtipo de gasto inválido");
            }

            sumaTotal = gastosUsuario.stream()
                    .filter(g -> g.getSubtipo().equals(subtipoEnum.name()))
                    .map(Gasto::getCantidad) // <--- aquí corregido
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return ResponseEntity.ok(Map.of(
                    "usuario", usuario.getNickname(),
                    "tipoGasto", tipo.name(),
                    "subtipo", subtipoEnum.name(),
                    "totalGasto", sumaTotal));

        } else {
            // Solo tipo
            sumaTotal = gastosUsuario.stream()
                    .filter(g -> g.getTipoGasto().equals(tipo.name()))
                    .map(Gasto::getCantidad) // <--- también corregido aquí
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return ResponseEntity.ok(Map.of(
                    "usuario", usuario.getNickname(),
                    "tipoGasto", tipo.name(),
                    "subtipo", "Todos",
                    "totalGasto", sumaTotal));
        }
    }

    private boolean isSubtipoValidForTipo(TipoGasto tipo, SubtipoGasto subtipo) {
        return switch (tipo) {
            case SUPERVIVENCIA -> Set.of(
                    SubtipoGasto.AGUA, SubtipoGasto.BASURAS, SubtipoGasto.COMIDA, SubtipoGasto.COMUNIDAD,
                    SubtipoGasto.FARMACIA, SubtipoGasto.GAS, SubtipoGasto.GASOIL, SubtipoGasto.GIMNASIO,
                    SubtipoGasto.HIJOS,
                    SubtipoGasto.HIPOTECA, SubtipoGasto.IBI, SubtipoGasto.LUZ, SubtipoGasto.MANT_COCHE,
                    SubtipoGasto.MUTUA,
                    SubtipoGasto.ROPA_IMPRESCINDIBLE, SubtipoGasto.SALUD, SubtipoGasto.SEGURO_HOGAR,
                    SubtipoGasto.SEGURO_VIDA,
                    SubtipoGasto.COCHERA, SubtipoGasto.CASA_APARATOS, SubtipoGasto.DOCUMENTOS).contains(subtipo);
            case LUJO -> Set.of(
                    SubtipoGasto.ESPECTACULOS, SubtipoGasto.ESTETICA, SubtipoGasto.MAQUILLAJE, SubtipoGasto.PARKING,
                    SubtipoGasto.REGALOS, SubtipoGasto.RESTAURANTES, SubtipoGasto.ROPA_COMPLEMENTOS,
                    SubtipoGasto.SUBS_OCIO,
                    SubtipoGasto.VIAJES, SubtipoGasto.SALIDAS_OCIO, SubtipoGasto.TELEFONO_INTERNET,
                    SubtipoGasto.LOTERIA,
                    SubtipoGasto.FALLA, SubtipoGasto.LUJO_NIÑOS).contains(subtipo);
            case EDUCACION -> Set.of(
                    SubtipoGasto.CURSOS, SubtipoGasto.LIBROS, SubtipoGasto.MASTERS, SubtipoGasto.SUBSCRIPCIONES_EDU,
                    SubtipoGasto.PROGRAMAS).contains(subtipo);
            case AHORRO -> Set.of(
                    SubtipoGasto.BANKINTER, SubtipoGasto.TRADE_REPUBLIC, SubtipoGasto.CAIXABANK, SubtipoGasto.SABADELL)
                    .contains(subtipo);
            case INVERSION -> Set.of(
                    SubtipoGasto.HAUSERA, SubtipoGasto.IB, SubtipoGasto.MYINVESTOR, SubtipoGasto.BINANCE)
                    .contains(subtipo);
        };
    }
}