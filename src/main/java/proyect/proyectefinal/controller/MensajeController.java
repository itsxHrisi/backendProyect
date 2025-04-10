package proyect.proyectefinal.controller;

import java.time.LocalDateTime;
import java.util.List;

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
import proyect.proyectefinal.model.db.Mensaje;
import proyect.proyectefinal.service.MensajeService;

@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;

    @GetMapping
    public List<Mensaje> getAll() {
        return mensajeService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return mensajeService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Mensaje no encontrado"));
    }
    
    @GetMapping("/receptor/{id}")
    public List<Mensaje> getByReceptor(@PathVariable Long id) {
        return mensajeService.findByReceptorId(id);
    }

    @GetMapping("/grupo/{id}")
    public List<Mensaje> getByGrupo(@PathVariable Long id) {
        return mensajeService.findByGrupoId(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Mensaje mensaje) {
        mensaje.setFecha(LocalDateTime.now()); // Asignar fecha automáticamente
        return ResponseEntity.status(HttpStatus.CREATED).body(mensajeService.save(mensaje));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Mensaje mensaje) {
        return mensajeService.findById(id)
                .<ResponseEntity<?>>map(existing -> {
                    mensaje.setId(id);
                    return ResponseEntity.ok(mensajeService.save(mensaje));
                })
                .orElseGet(() -> ResponseEntity.status(404).body("Mensaje no encontrado"));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return mensajeService.findById(id).map(existing -> {
            mensajeService.deleteById(id);
            return ResponseEntity.ok("Mensaje eliminado");
        }).orElse(ResponseEntity.status(404).body("Mensaje no encontrado"));
    }
}
