package proyect.proyectefinal.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import proyect.proyectefinal.model.db.GrupoFamiliar;
import proyect.proyectefinal.model.db.Mensaje;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.MensajeCreateDTO;
import proyect.proyectefinal.model.dto.MensajeEdit;
import proyect.proyectefinal.model.dto.MensajeInfo;
import proyect.proyectefinal.model.dto.MensajeText;
import proyect.proyectefinal.repository.GrupoFamiliarRepository;
import proyect.proyectefinal.repository.UsuarioRepository;
import proyect.proyectefinal.service.MensajeService;


@RestController
@RequestMapping("/api/mensajes")
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService mensajeService;

    /**
     * GET /api/mensajes
     */
    @GetMapping
    public ResponseEntity<List<MensajeInfo>> getAll() {
        List<MensajeInfo> dtos = mensajeService.findAll()
            .stream()
            .map(this::toInfoDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/mensajes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MensajeInfo> getById(@PathVariable Long id) {
        return mensajeService.findById(id)
            .map(m -> ResponseEntity.ok(toInfoDto(m)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/mensajes/receptor/{receptorId}
     */
    @GetMapping("/receptor/{receptorId}")
    public ResponseEntity<List<MensajeInfo>> getByReceptor(@PathVariable Long receptorId) {
        List<MensajeInfo> dtos = mensajeService.findByReceptorId(receptorId)
            .stream()
            .map(this::toInfoDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/mensajes/grupo/{grupoId}
     */
    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<MensajeInfo>> getByGrupo(@PathVariable Long grupoId) {
        List<MensajeInfo> dtos = mensajeService.findByGrupoId(grupoId)
            .stream()
            .map(this::toInfoDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * POST /api/mensajes
     */
    @PostMapping
    public ResponseEntity<MensajeInfo> create(@Valid @RequestBody MensajeCreateDTO createDto) {
        System.out.println(createDto.getEmisorId()+"          "+createDto.getReceptorId());
        Mensaje toSave = Mensaje.builder()
            .emisorId(createDto.getEmisorId())
            .receptorId(createDto.getReceptorId())
            .grupoId(createDto.getGrupoId())
            .contenido(createDto.getContenido())
            .fecha(LocalDateTime.now())
            .build();

        Mensaje saved = mensajeService.save(toSave);
        MensajeInfo info = toInfoDto(saved);

        return ResponseEntity
            .created(URI.create("/api/mensajes/" + saved.getId()))
            .body(info);
    }

    /**
     * PUT /api/mensajes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<MensajeInfo> update(
            @PathVariable Long id,
            @Valid @RequestBody MensajeEdit editDto
    ) {
        return mensajeService.findById(id)
            .map(existing -> {
                existing.setContenido(editDto.getContenido());
                Mensaje updated = mensajeService.save(existing);
                return ResponseEntity.ok(toInfoDto(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/mensajes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return mensajeService.findById(id)
            .map(m -> {
                mensajeService.deleteById(id);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // (Opcional) obtener sólo el texto del mensaje
    /**
     * GET /api/mensajes/{id}/texto
     */
    @GetMapping("/{id}/texto")
    public ResponseEntity<MensajeText> getTexto(@PathVariable Long id) {
        return mensajeService.findById(id)
            .map(m -> ResponseEntity.ok(
                new MensajeText(m.getContenido())
            ))
            .orElse(ResponseEntity.notFound().build());
    }

    // --- utilidades de mapeo ---
    private MensajeInfo toInfoDto(Mensaje m) {
        return MensajeInfo.builder()
            .id(m.getId())
            .emisorId(m.getEmisorId())
            .receptorId(m.getReceptorId())
            .grupoId(m.getGrupoId())
            .contenido(m.getContenido())
            .fecha(m.getFecha())
            .build();
    }
}