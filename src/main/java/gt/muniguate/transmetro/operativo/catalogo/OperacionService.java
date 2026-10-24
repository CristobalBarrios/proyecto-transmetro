package gt.muniguate.transmetro.operativo.catalogo;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class OperacionService {
    private final JdbcTemplate datos;

    public OperacionService(JdbcTemplate datos) { this.datos = datos; }

    public void bloquear() {
        // Serializa cambios de preparación para que dos solicitudes no validen datos incompatibles.
        datos.execute("SELECT pg_advisory_xact_lock(20261016)");
    }

    public Preparacion revisar(int id) {
        var lineas = datos.queryForList("SELECT codigo, nombre, estado FROM linea WHERE id = ?", id);
        if (lineas.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Línea no encontrada.");
        var linea = lineas.getFirst();
        int estaciones = cantidad("SELECT count(*) FROM linea_estacion WHERE linea_id = ?", id);
        int buses = cantidad("SELECT count(*) FROM bus WHERE linea_id = ?", id);
        int sinAccesos = cantidad("""
                SELECT count(*) FROM linea_estacion le WHERE le.linea_id = ?
                AND NOT EXISTS (SELECT 1 FROM acceso a WHERE a.estacion_id = le.estacion_id)
                """, id);
        int sinGuardia = cantidad("""
                SELECT count(*) FROM acceso a JOIN linea_estacion le ON le.estacion_id = a.estacion_id
                WHERE le.linea_id = ? AND NOT EXISTS (SELECT 1 FROM guardia_acceso ga
                WHERE ga.acceso_id = a.id AND ga.estado = 'activa' AND ga.fecha_inicio <= CURRENT_TIMESTAMP)
                """, id);
        int sinOperador = cantidad("""
                SELECT count(*) FROM linea_estacion le WHERE le.linea_id=? AND NOT EXISTS
                (SELECT 1 FROM usuario u WHERE u.estacion_id=le.estacion_id AND u.perfil='operador' AND u.activo)
                """, id);
        var pendientes = new ArrayList<String>();
        if (estaciones == 0) pendientes.add("Configure al menos una estación en la ruta.");
        if (estaciones > 0 && (buses < estaciones || buses > estaciones * 2))
            pendientes.add("Se necesitan entre " + estaciones + " y " + estaciones * 2 + " buses asignados.");
        if (sinAccesos > 0) pendientes.add(sinAccesos + " estaciones sin accesos registrados.");
        if (sinGuardia > 0) pendientes.add(sinGuardia + " accesos sin guardia asignado.");
        if (sinOperador > 0) pendientes.add(sinOperador + " estaciones sin operador activo.");
        return new Preparacion(id, (String) linea.get("codigo"), (String) linea.get("nombre"),
                (String) linea.get("estado"), estaciones, buses, List.copyOf(pendientes));
    }

    public void validarActivas() {
        for (Integer id : datos.queryForList("SELECT id FROM linea WHERE estado = 'operativa' ORDER BY id", Integer.class)) {
            exigirPreparada(revisar(id));
        }
    }

    public void validarBusEnCurso(int busId,Integer lineaId,String estado) {
        var recorridos=datos.queryForList("SELECT linea_id FROM recorrido WHERE bus_id=? AND fin IS NULL",Integer.class,busId);
        if(!recorridos.isEmpty() && (!recorridos.getFirst().equals(lineaId) || !"disponible".equals(estado)))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Finalice el recorrido antes de cambiar la línea o disponibilidad del bus.");
    }

    public void exigirPreparada(Preparacion preparacion) {
        if (!preparacion.pendientes().isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Línea " + preparacion.codigo() + ": " + String.join(" ", preparacion.pendientes()));
    }

    private int cantidad(String sql, int id) { return datos.queryForObject(sql, Integer.class, id); }

    public record Preparacion(int id, String codigo, String nombre, String estado, int estaciones,
                               int buses, List<String> pendientes) { }
}
