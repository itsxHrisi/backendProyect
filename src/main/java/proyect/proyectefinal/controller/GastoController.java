package proyect.proyectefinal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import proyect.proyectefinal.model.db.Gasto;
import proyect.proyectefinal.model.dto.GastoEdit;
import proyect.proyectefinal.model.dto.GastoInfo;
import proyect.proyectefinal.model.enums.SubtipoGasto;
import proyect.proyectefinal.model.enums.TipoGasto;
import proyect.proyectefinal.repository.GastoRepository;
import proyect.proyectefinal.repository.GrupoFamiliarRepository;
import proyect.proyectefinal.repository.UsuarioRepository;
import proyect.proyectefinal.srv.mapper.GastoMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public ResponseEntity<?> createGasto(@RequestBody Gasto gasto) {
        // Validar tipo y subtipo
        try {
            TipoGasto tipo = TipoGasto.valueOf(gasto.getTipoGasto());
            SubtipoGasto subtipo = SubtipoGasto.valueOf(gasto.getSubtipo());
            if (!isSubtipoValidForTipo(tipo, subtipo)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El subtipo no corresponde con el tipo");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo o subtipo inválido");
        }

        Gasto saved = gastoRepository.save(gasto);
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

        try {
            TipoGasto tipo = TipoGasto.valueOf(gastoEdit.getTipoGasto());
            SubtipoGasto subtipo = SubtipoGasto.valueOf(gastoEdit.getSubtipo());
            if (!isSubtipoValidForTipo(tipo, subtipo)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El subtipo no corresponde con el tipo");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo o subtipo inválido");
        }

        Gasto gasto = optionalGasto.get();
        GastoMapper.INSTANCE.updateGastoFromGastoEdit(gastoEdit, gasto);
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