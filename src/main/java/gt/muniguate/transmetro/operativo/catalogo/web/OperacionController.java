package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;

import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/operacion/lineas")
public class OperacionController {
    private final UsuarioService auditoria;
    private final JdbcTemplate datos;
    private final OperacionService operacion;
    public OperacionController(JdbcTemplate datos, OperacionService operacion, UsuarioService auditoria) {
        this.auditoria = auditoria;
        this.datos = datos; this.operacion = operacion;
    }

    @GetMapping
    public List<OperacionService.Preparacion> listar() {
        return datos.queryForList("SELECT id FROM linea ORDER BY codigo", Integer.class).stream()
                .map(operacion::revisar).toList();
    }

    @PutMapping("/{id}/estado")
    @Transactional
    public OperacionService.Preparacion cambiarEstado(@PathVariable int id, @Valid @RequestBody EstadoSolicitud solicitud) {
        operacion.bloquear();
        var preparacion = operacion.revisar(id);
        if ("operativa".equals(solicitud.estado())) operacion.exigirPreparada(preparacion);
        else if (Boolean.TRUE.equals(datos.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM recorrido WHERE linea_id = ? AND fin IS NULL)", Boolean.class, id))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede desactivar una línea con recorridos en curso.");
        }
        datos.update("UPDATE linea SET estado = ? WHERE id = ?", solicitud.estado(), id);
        auditoria.registrar("linea", id, solicitud.estado());
        return operacion.revisar(id);
    }

    public record EstadoSolicitud(@NotBlank @Pattern(regexp = "operativa|inactiva") String estado) { }
}
