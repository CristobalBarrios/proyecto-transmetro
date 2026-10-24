package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {
    private final JdbcTemplate datos;
    private final UsuarioService usuarios;
    public EventoController(JdbcTemplate datos,UsuarioService usuarios) { this.datos=datos; this.usuarios=usuarios; }
    @GetMapping public List<Map<String,Object>> listar() {
        var sesion=usuarios.actual();
        return datos.queryForList("""
                SELECT e.*,u.nombre_usuario AS registrado_por,c.nombre_usuario AS cerrado_por
                FROM evento_operativo e JOIN usuario u ON u.id=e.usuario_registra_id
                LEFT JOIN usuario c ON c.id=e.usuario_cierra_id
                WHERE (? <> 'operador' OR e.estacion_id=?) ORDER BY e.id DESC
                """,sesion.perfil(),sesion.estacionId());
    }
    @PostMapping @Transactional @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> crear(@Valid @RequestBody Solicitud s) {
        if("operador".equals(usuarios.actual().perfil()) && s.estacionId()==null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Seleccione su estación para registrar el evento.");
        if(s.lineaId()==null && s.estacionId()==null && s.busId()==null && s.accesoId()==null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Seleccione un recurso afectado.");
        if(s.estacionId()!=null) usuarios.comprobarEstacion(s.estacionId());
        int id=datos.queryForObject("""
                INSERT INTO evento_operativo(tipo,descripcion,linea_id,estacion_id,bus_id,acceso_id,usuario_registra_id)
                VALUES (?,?,?,?,?,?,?) RETURNING id
                """,Integer.class,s.tipo(),s.descripcion(),s.lineaId(),s.estacionId(),s.busId(),s.accesoId(),usuarios.actual().id());
        usuarios.registrar("evento_operativo",id,"registrar");
        return Map.of("id",id);
    }
    @PutMapping("/{id}/estado") @Transactional @ResponseStatus(HttpStatus.NO_CONTENT)
    public void estado(@PathVariable int id,@Valid @RequestBody Estado s) {
        if("operador".equals(usuarios.actual().perfil())) {
            var estaciones=datos.queryForList("SELECT estacion_id FROM evento_operativo WHERE id=?",Integer.class,id);
            if(estaciones.isEmpty() || estaciones.getFirst()==null)
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"El evento no pertenece a su estación.");
            usuarios.comprobarEstacion(estaciones.getFirst());
        }
        int filas=datos.update("""
                UPDATE evento_operativo SET estado=?,usuario_cierra_id=CASE WHEN ?='cerrado' THEN ? ELSE NULL END,
                fecha_cierre=CASE WHEN ?='cerrado' THEN CURRENT_TIMESTAMP ELSE NULL END WHERE id=? AND estado<>'cerrado'
                """,s.estado(),s.estado(),usuarios.actual().id(),s.estado(),id);
        if(filas==0) throw new ResponseStatusException(HttpStatus.CONFLICT,"El evento no existe o ya está cerrado.");
        usuarios.registrar("evento_operativo",id,s.estado());
    }
    public record Solicitud(@NotBlank @Size(max=60) String tipo,@NotBlank @Size(max=500) String descripcion,
                            @Positive Integer lineaId,@Positive Integer estacionId,@Positive Integer busId,@Positive Integer accesoId) { }
    public record Estado(@NotBlank @Pattern(regexp="en_atencion|cerrado") String estado) { }
}
