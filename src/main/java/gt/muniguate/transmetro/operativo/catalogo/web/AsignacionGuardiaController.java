package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;

import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/asignaciones-guardias")
public class AsignacionGuardiaController {
    private final UsuarioService auditoria;
    private final JdbcTemplate datos;
    private final OperacionService operacion;
    private final UserDetailsService usuarios;
    public AsignacionGuardiaController(JdbcTemplate datos, OperacionService operacion, UserDetailsService usuarios, UsuarioService auditoria) {
        this.auditoria = auditoria;
        this.datos = datos; this.operacion = operacion; this.usuarios = usuarios;
    }

    @GetMapping
    public List<AsignacionRespuesta> listar() {
        return datos.query("""
                SELECT ga.*, g.nombre AS guardia, a.codigo AS acceso, e.nombre AS estacion,
                u.nombre_usuario AS responsable FROM guardia_acceso ga
                JOIN guardia g ON g.id = ga.guardia_id JOIN acceso a ON a.id = ga.acceso_id
                JOIN estacion e ON e.id = a.estacion_id JOIN usuario u ON u.id = ga.asignado_por_usuario_id
                ORDER BY ga.id DESC
                """, (f, n) -> new AsignacionRespuesta(f.getInt("id"), f.getString("guardia"),
                f.getString("acceso"), f.getString("estacion"), f.getString("responsable"),
                f.getObject("fecha_inicio", OffsetDateTime.class), f.getObject("fecha_fin", OffsetDateTime.class), f.getString("estado")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AsignacionRespuesta asignar(@Valid @RequestBody AsignacionSolicitud solicitud, Principal principal) {
        operacion.bloquear();
        if (!Boolean.TRUE.equals(datos.queryForObject("SELECT EXISTS (SELECT 1 FROM guardia WHERE id = ?)", Boolean.class, solicitud.guardiaId()))
                || !Boolean.TRUE.equals(datos.queryForObject("SELECT EXISTS (SELECT 1 FROM acceso WHERE id = ?)", Boolean.class, solicitud.accesoId())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleccione un guardia y un acceso existentes.");
        // El responsable proviene de la sesión; no puede elegirse otro usuario desde el formulario.
        var usuario = usuarios.loadUserByUsername(principal.getName());
        Integer responsable = datos.queryForObject("""
                INSERT INTO usuario (nombre, nombre_usuario, password_hash, perfil) VALUES (?, ?, ?, 'seguridad')
                ON CONFLICT (nombre_usuario) DO UPDATE SET nombre_usuario = EXCLUDED.nombre_usuario RETURNING id
                """, Integer.class, principal.getName(), principal.getName(), usuario.getPassword());
        Integer id = datos.queryForObject("""
                INSERT INTO guardia_acceso (guardia_id, acceso_id, asignado_por_usuario_id)
                VALUES (?, ?, ?) RETURNING id
                """, Integer.class, solicitud.guardiaId(), solicitud.accesoId(), responsable);
        auditoria.registrar("guardia_acceso", id, "asignar");
        return listar().stream().filter(a -> a.id() == id).findFirst().orElseThrow();
    }

    @PutMapping("/{id}/finalizar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void finalizar(@PathVariable int id) {
        operacion.bloquear();
        int filas = datos.update("""
                UPDATE guardia_acceso SET estado = 'finalizada', fecha_fin = CURRENT_TIMESTAMP
                WHERE id = ? AND estado = 'activa' AND fecha_inicio <= CURRENT_TIMESTAMP
                """, id);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "La asignación no existe o no está activa actualmente.");
        operacion.validarActivas();
        auditoria.registrar("guardia_acceso", id, "finalizar");
    }

    public record AsignacionSolicitud(@NotNull @Positive Integer guardiaId, @NotNull @Positive Integer accesoId) { }
    public record AsignacionRespuesta(int id, String guardia, String acceso, String estacion, String responsable,
                                       OffsetDateTime fechaInicio, OffsetDateTime fechaFin, String estado) { }
}
