package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RecorridoController {
    private final JdbcTemplate datos;
    private final OperacionService operacion;
    private final UsuarioService usuarios;
    public RecorridoController(JdbcTemplate datos, OperacionService operacion, UsuarioService usuarios) {
        this.datos = datos; this.operacion = operacion; this.usuarios = usuarios;
    }
    @GetMapping("/recorridos") public List<Map<String,Object>> listar() {
        var sesion=usuarios.actual();
        return datos.queryForList("""
                SELECT r.*,l.codigo AS linea,b.placa AS bus,p.nombre AS piloto FROM recorrido r
                JOIN linea l ON l.id=r.linea_id JOIN bus b ON b.id=r.bus_id JOIN piloto p ON p.id=r.piloto_id
                WHERE (? <> 'operador' OR EXISTS(SELECT 1 FROM linea_estacion le WHERE le.linea_id=r.linea_id AND le.estacion_id=?))
                ORDER BY r.id DESC
                """,sesion.perfil(),sesion.estacionId());
    }
    @PostMapping("/recorridos") @Transactional @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> iniciar(@Valid @RequestBody Inicio s) {
        operacion.bloquear();
        var linea = operacion.revisar(s.lineaId());
        operacion.exigirPreparada(linea);
        if (!"operativa".equals(linea.estado())) throw conflicto("Active la línea antes de iniciar un recorrido.");
        var sesion=usuarios.actual();
        if("operador".equals(sesion.perfil()) && !Boolean.TRUE.equals(datos.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM linea_estacion WHERE linea_id=? AND estacion_id=?)",Boolean.class,s.lineaId(),sesion.estacionId())))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"La línea no pertenece a su estación.");
        if (!Boolean.TRUE.equals(datos.queryForObject("SELECT EXISTS(SELECT 1 FROM bus WHERE id=? AND linea_id=? AND estado='disponible')",
                Boolean.class,s.busId(),s.lineaId()))) throw conflicto("Seleccione un bus disponible de la línea.");
        int id = datos.queryForObject("INSERT INTO recorrido(linea_id,bus_id,piloto_id,inicio) VALUES (?,?,?,CURRENT_TIMESTAMP) RETURNING id",
                Integer.class,s.lineaId(),s.busId(),s.pilotoId());
        usuarios.registrar("recorrido",id,"iniciar");
        return Map.of("id",id);
    }
    @PutMapping("/recorridos/{id}/finalizar") @Transactional @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finalizar(@PathVariable int id) {
        operacion.bloquear();
        var sesion=usuarios.actual();
        if("operador".equals(sesion.perfil()) && !Boolean.TRUE.equals(datos.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM recorrido r JOIN linea_estacion le ON le.linea_id=r.linea_id
                WHERE r.id=? AND le.estacion_id=?)
                """,Boolean.class,id,sesion.estacionId()))) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"La línea no pertenece a su estación.");
        int pendientes = datos.queryForObject("""
                SELECT count(*) FROM recorrido r JOIN linea_estacion le ON le.linea_id=r.linea_id
                WHERE r.id=? AND NOT EXISTS(SELECT 1 FROM visita_estacion v
                WHERE v.recorrido_id=r.id AND v.estacion_id=le.estacion_id AND v.salida IS NOT NULL)
                """,Integer.class,id);
        if (pendientes>0) throw conflicto("Complete las visitas de la ruta antes de finalizar.");
        if (datos.update("UPDATE recorrido SET fin=CURRENT_TIMESTAMP WHERE id=? AND fin IS NULL",id)==0)
            throw conflicto("El recorrido no está en curso.");
        usuarios.registrar("recorrido",id,"finalizar");
    }
    @GetMapping("/visitas") public List<Map<String,Object>> visitas() {
        var sesion=usuarios.actual();
        return datos.queryForList("""
                SELECT v.*,e.nombre AS estacion,b.placa AS bus FROM visita_estacion v
                JOIN estacion e ON e.id=v.estacion_id JOIN recorrido r ON r.id=v.recorrido_id JOIN bus b ON b.id=r.bus_id
                WHERE (? <> 'operador' OR v.estacion_id=?) ORDER BY v.id DESC
                """,sesion.perfil(),sesion.estacionId());
    }
    @PostMapping("/visitas") @Transactional @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> visitar(@Valid @RequestBody Visita s) {
        operacion.bloquear();
        usuarios.comprobarEstacion(s.estacionId());
        var recorridos=datos.queryForList("SELECT r.linea_id,b.capacidad_maxima FROM recorrido r JOIN bus b ON b.id=r.bus_id WHERE r.id=? AND r.fin IS NULL",s.recorridoId());
        if(recorridos.isEmpty()) throw conflicto("Seleccione un recorrido en curso.");
        int abiertas=datos.queryForObject("SELECT count(*) FROM visita_estacion WHERE recorrido_id=? AND salida IS NULL",Integer.class,s.recorridoId());
        if(abiertas>0) throw conflicto("Registre la salida de la visita anterior.");
        var siguientes=datos.queryForList("""
                SELECT le.estacion_id,le.orden FROM linea_estacion le WHERE le.linea_id=?
                AND NOT EXISTS(SELECT 1 FROM visita_estacion v WHERE v.recorrido_id=? AND v.orden_programado=le.orden)
                ORDER BY le.orden LIMIT 1
                """,recorridos.getFirst().get("linea_id"),s.recorridoId());
        if(siguientes.isEmpty() || !s.estacionId().equals(siguientes.getFirst().get("estacion_id")))
            throw conflicto("Respete el orden de estaciones de la ruta.");
        int id=datos.queryForObject("""
                INSERT INTO visita_estacion(recorrido_id,estacion_id,orden_programado,llegada,salida_prevista,
                pasajeros_abordo,pasajeros_esperando,capacidad_bus,usuario_id) VALUES (?,?,?,?,?,?,?,?,?) RETURNING id
                """,Integer.class,s.recorridoId(),s.estacionId(),siguientes.getFirst().get("orden"),s.llegada(),s.salidaPrevista(),
                s.pasajerosAbordo(),s.pasajerosEsperando(),recorridos.getFirst().get("capacidad_maxima"),usuarios.actual().id());
        datos.update("INSERT INTO alerta(visita_id) SELECT id FROM visita_estacion WHERE id=? AND solicita_refuerzo",id);
        usuarios.registrar("visita_estacion",id,"registrar");
        return Map.of("id",id);
    }
    @PutMapping("/visitas/{id}/salida") @Transactional @ResponseStatus(HttpStatus.NO_CONTENT)
    public void salir(@PathVariable int id,@Valid @RequestBody Salida s) {
        operacion.bloquear();
        var visitas=datos.queryForList("SELECT estacion_id FROM visita_estacion WHERE id=? AND salida IS NULL",id);
        if(visitas.isEmpty()) throw conflicto("La visita no está pendiente de salida.");
        usuarios.comprobarEstacion((Integer)visitas.getFirst().get("estacion_id"));
        if(s.salida().isAfter(OffsetDateTime.now())) throw conflicto("La salida no puede registrarse en el futuro.");
        datos.update("UPDATE visita_estacion SET salida=? WHERE id=?",s.salida(),id);
        usuarios.registrar("visita_estacion",id,"salida");
    }
    @GetMapping("/alertas") public List<Map<String,Object>> alertas() {
        var sesion=usuarios.actual();
        return datos.queryForList("""
                SELECT a.*,e.nombre AS estacion,l.codigo AS linea,v.pasajeros_esperando,v.capacidad_bus,
                b.placa AS bus_refuerzo FROM alerta a JOIN visita_estacion v ON v.id=a.visita_id
                JOIN estacion e ON e.id=v.estacion_id JOIN recorrido r ON r.id=v.recorrido_id
                JOIN linea l ON l.id=r.linea_id LEFT JOIN bus b ON b.id=a.bus_refuerzo_id
                WHERE (? <> 'operador' OR v.estacion_id=?) ORDER BY a.id DESC
                """,sesion.perfil(),sesion.estacionId());
    }
    @PutMapping("/alertas/{id}/atender") @Transactional @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atender(@PathVariable int id,@Valid @RequestBody Refuerzo s) {
        operacion.bloquear();
        var estaciones=datos.queryForList("SELECT v.estacion_id FROM alerta a JOIN visita_estacion v ON v.id=a.visita_id WHERE a.id=?",Integer.class,id);
        if(estaciones.isEmpty()) throw conflicto("La alerta no existe.");
        usuarios.comprobarEstacion(estaciones.getFirst());
        if(!Boolean.TRUE.equals(datos.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM bus b WHERE b.id=? AND b.estado='disponible'
                AND NOT EXISTS(SELECT 1 FROM recorrido r WHERE r.bus_id=b.id AND r.fin IS NULL)
                AND NOT EXISTS(SELECT 1 FROM alerta a JOIN visita_estacion v ON v.id=a.visita_id
                    JOIN recorrido r ON r.id=v.recorrido_id WHERE a.id=? AND r.bus_id=b.id))
                """,Boolean.class,s.busId(),id))) throw conflicto("Seleccione otro bus disponible para el refuerzo.");
        if(datos.update("UPDATE alerta SET estado='atendida',bus_refuerzo_id=?,atendida=CURRENT_TIMESTAMP WHERE id=? AND estado='pendiente'",s.busId(),id)==0)
            throw conflicto("La alerta no está pendiente.");
        usuarios.registrar("alerta",id,"atender");
    }
    private ResponseStatusException conflicto(String mensaje) { return new ResponseStatusException(HttpStatus.CONFLICT,mensaje); }
    public record Inicio(@NotNull @Positive Integer lineaId,@NotNull @Positive Integer busId,@NotNull @Positive Integer pilotoId) { }
    public record Visita(@NotNull @Positive Integer recorridoId,@NotNull @Positive Integer estacionId,
                         @NotNull OffsetDateTime llegada,@NotNull OffsetDateTime salidaPrevista,
                         @NotNull @Min(0) Integer pasajerosAbordo,@NotNull @Min(0) Integer pasajerosEsperando) { }
    public record Salida(@NotNull OffsetDateTime salida) { }
    public record Refuerzo(@NotNull @Positive Integer busId) { }
}
