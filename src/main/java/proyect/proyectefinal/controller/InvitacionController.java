package proyect.proyectefinal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import proyect.proyectefinal.helper.PaginationHelper;
import proyect.proyectefinal.model.db.Invitacion;
import proyect.proyectefinal.model.db.UsuarioDb;
import proyect.proyectefinal.model.dto.InvitacionRequest;
import proyect.proyectefinal.model.dto.InvitacionesList;
import proyect.proyectefinal.model.dto.ListadoRespuesta;
import proyect.proyectefinal.model.dto.PaginaDto;
import proyect.proyectefinal.model.dto.UsuarioList;
import proyect.proyectefinal.repository.InvitacionRepository;
import proyect.proyectefinal.security.service.UsuarioService;
import proyect.proyectefinal.service.GrupoFamiliarService;
import proyect.proyectefinal.service.InvitacionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invitaciones")
public class InvitacionController {

    private final InvitacionService invitacionService;

    public InvitacionController(InvitacionService invitacionService) {
        this.invitacionService = invitacionService;
    }

    @PostMapping
    public ResponseEntity<?> crearInvitacion(@RequestBody InvitacionRequest request) {
        return ResponseEntity.ok(invitacionService.crearInvitacion(request));
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<Invitacion>> getByGrupo(@PathVariable Long grupoId) {
        return ResponseEntity.ok(invitacionService.getInvitacionesPorGrupo(grupoId));
    }
    @PutMapping("/{id}/estado") 
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestParam String nuevoEstado) {
        try {
            Invitacion.EstadoInvitacion estado = Invitacion.EstadoInvitacion.valueOf(nuevoEstado.toUpperCase());
            return invitacionService.actualizarEstado(id, estado)
                    .map(invitacion -> {
                        if (estado == Invitacion.EstadoInvitacion.ACEPTADA) {
                            return ResponseEntity.ok("La petición ha sido aceptada");
                        } else {
                            return ResponseEntity.ok(invitacion);
                        }
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido: debe ser PENDIENTE, ACEPTADA o RECHAZADA");
        }
    }
  @GetMapping
    public ResponseEntity<ListadoRespuesta<InvitacionesList>> getMisInvitaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Pageable paging = PaginationHelper.createPageable(page, size, sort);
        PaginaDto<InvitacionesList> pagina = invitacionService.findAll(paging);

        ListadoRespuesta<InvitacionesList> resp = new ListadoRespuesta<>(
            pagina.getNumber(),
            pagina.getSize(),
            pagina.getTotalElements(),
            pagina.getTotalPages(),
            pagina.getContent()
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/usuario")
public ResponseEntity<List<Invitacion>> getInvitacionesDelUsuarioAutenticado() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String nickname = auth.getName();
    return ResponseEntity.ok(invitacionService.getInvitacionesPorNickname(nickname));
}


}
