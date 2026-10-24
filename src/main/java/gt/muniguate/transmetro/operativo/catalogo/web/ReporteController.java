package gt.muniguate.transmetro.operativo.catalogo.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final JdbcTemplate datos;
    private final UsuarioService usuarios;
    public ReporteController(JdbcTemplate datos,UsuarioService usuarios) { this.datos=datos; this.usuarios=usuarios; }
    @GetMapping public List<Map<String,Object>> consultar(@RequestParam String tipo,
            @RequestParam(required=false) LocalDate desde,@RequestParam(required=false) LocalDate hasta,
            @RequestParam(required=false) Integer lineaId,@RequestParam(required=false) Integer estacionId,
            @RequestParam(required=false) Integer busId) {
        var sesion=usuarios.actual();
        if("personal".equals(tipo) && !List.of("administrador","supervisor","personal","flota","operador").contains(sesion.perfil()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Su perfil no permite consultar los datos de pilotos.");
        String sql=switch(tipo) {
            case "estaciones" -> """
                    SELECT e.id,e.nombre,e.direccion,e.zona,e.codigo_ruta,m.nombre AS municipalidad
                    FROM estacion e JOIN municipalidad m ON m.id=e.municipalidad_id
                    """;
            case "lineas" -> """
                    SELECT l.id,l.codigo,l.nombre,m.nombre AS municipalidad,v.estaciones,v.buses,d.distancia_km
                    FROM linea l JOIN municipalidad m ON m.id=l.municipalidad_id
                    JOIN v_validacion_lineas v ON v.linea_id=l.id JOIN v_distancia_lineas d ON d.linea_id=l.id
                    """;
            case "buses" -> """
                    SELECT b.id,b.placa,b.capacidad_maxima,b.estado,l.codigo AS linea,p.nombre AS parqueo
                    FROM bus b LEFT JOIN linea l ON l.id=b.linea_id JOIN parqueo p ON p.id=b.parqueo_id
                    """;
            case "rutas" -> """
                    SELECT l.id AS linea_id,l.codigo,le.orden,e.nombre AS estacion,
                    le.distancia_desde_anterior_km FROM linea_estacion le
                    JOIN linea l ON l.id=le.linea_id JOIN estacion e ON e.id=le.estacion_id
                    """;
            case "accesos" -> """
                    SELECT l.id AS linea_id,l.codigo,e.nombre AS estacion,a.codigo AS acceso,
                    (SELECT count(*) FROM guardia_acceso ga WHERE ga.acceso_id=a.id AND ga.estado='activa') AS guardias_activos
                    FROM linea_estacion le JOIN linea l ON l.id=le.linea_id
                    JOIN estacion e ON e.id=le.estacion_id JOIN acceso a ON a.estacion_id=e.id
                    """;
            case "personal" -> """
                    SELECT p.id,p.nombre,p.residencia,p.telefono,p.correo,ep.nivel,ep.institucion,ep.titulo,ep.anio_finalizacion
                    FROM piloto p LEFT JOIN educacion_piloto ep ON ep.piloto_id=p.id
                    """;
            case "recorridos" -> """
                    SELECT r.id,r.linea_id,NULL::integer AS estacion_id,r.bus_id,r.inicio AS fecha,r.fin,
                    l.codigo AS linea,b.placa,p.nombre AS piloto FROM recorrido r JOIN linea l ON l.id=r.linea_id
                    JOIN bus b ON b.id=r.bus_id JOIN piloto p ON p.id=r.piloto_id
                    """;
            case "visitas" -> """
                    SELECT v.id,r.linea_id,v.estacion_id,r.bus_id,v.llegada AS fecha,e.nombre AS estacion,
                    b.placa,v.orden_programado,v.pasajeros_abordo,v.pasajeros_esperando,v.capacidad_bus,
                    v.salida_prevista,v.espera_extra_min,v.salida FROM visita_estacion v
                    JOIN recorrido r ON r.id=v.recorrido_id JOIN estacion e ON e.id=v.estacion_id JOIN bus b ON b.id=r.bus_id
                    """;
            case "alertas" -> """
                    SELECT a.id,r.linea_id,v.estacion_id,r.bus_id,a.creada AS fecha,e.nombre AS estacion,
                    a.estado,v.pasajeros_esperando,v.capacidad_bus,b.placa AS refuerzo,a.atendida
                    FROM alerta a JOIN visita_estacion v ON v.id=a.visita_id JOIN recorrido r ON r.id=v.recorrido_id
                    JOIN estacion e ON e.id=v.estacion_id LEFT JOIN bus b ON b.id=a.bus_refuerzo_id
                    """;
            case "eventos" -> """
                    SELECT e.id,e.linea_id,e.estacion_id,e.bus_id,e.fecha,e.tipo,e.descripcion,
                    u.nombre_usuario AS registrado_por FROM evento_operativo e
                    JOIN usuario u ON u.id=e.usuario_registra_id
                    """;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Seleccione un reporte disponible.");
        };
        var valores=new ArrayList<Object>();
        sql="SELECT * FROM ("+sql+") informe WHERE true";
        if(List.of("recorridos","visitas","alertas","eventos").contains(tipo)) {
            if("operador".equals(sesion.perfil())) {
                if("recorridos".equals(tipo)) {
                    sql+=" AND EXISTS(SELECT 1 FROM linea_estacion le WHERE le.linea_id=informe.linea_id AND le.estacion_id=?)";
                    valores.add(sesion.estacionId());
                } else { sql+=" AND estacion_id=?"; valores.add(sesion.estacionId()); }
            }
            if(desde!=null) { sql+=" AND fecha >= ?::date"; valores.add(desde); }
            if(hasta!=null) { sql+=" AND fecha < ?::date + interval '1 day'"; valores.add(hasta); }
            if(lineaId!=null) { sql+=" AND linea_id=?"; valores.add(lineaId); }
            if(estacionId!=null) { sql+=" AND estacion_id=?"; valores.add(estacionId); }
            if(busId!=null) { sql+=" AND bus_id=?"; valores.add(busId); }
        }
        return datos.queryForList(sql+" ORDER BY 1",valores.toArray());
    }
}
