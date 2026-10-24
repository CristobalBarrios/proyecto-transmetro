package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;

import gt.muniguate.transmetro.operativo.catalogo.OperacionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/rutas")
public class RutaController {
    private final UsuarioService auditoria;
    private final JdbcTemplate datos;
    private final OperacionService operacion;

    public RutaController(JdbcTemplate datos, OperacionService operacion, UsuarioService auditoria) {
        this.auditoria = auditoria; this.datos = datos; this.operacion = operacion; }

    @GetMapping("/{lineaId}")
    public List<EstacionRutaRespuesta> listar(@PathVariable Integer lineaId) {
        comprobarLinea(lineaId);
        return datos.query("""
                SELECT estacion_id, orden, distancia_desde_anterior_km
                FROM linea_estacion WHERE linea_id = ? ORDER BY orden
                """, (fila, numero) -> new EstacionRutaRespuesta(fila.getInt("estacion_id"),
                fila.getInt("orden"), fila.getBigDecimal("distancia_desde_anterior_km")), lineaId);
    }

    @PutMapping("/{lineaId}")
    @Transactional
    public List<EstacionRutaRespuesta> guardar(@PathVariable Integer lineaId,
                                               @Valid @RequestBody RutaSolicitud solicitud) {
        operacion.bloquear();
        // Bloquea la línea para guardar toda la secuencia como una sola operación.
        var lineas = datos.queryForList("SELECT id FROM linea WHERE id = ? FOR UPDATE", Integer.class, lineaId);
        if (lineas.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Línea no encontrada.");
        if (Boolean.TRUE.equals(datos.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM recorrido WHERE linea_id = ? AND fin IS NULL)", Boolean.class, lineaId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cambiar la ruta con recorridos en curso.");
        }
        var identificadores = new HashSet<Integer>();
        var anteriores = datos.queryForList("SELECT estacion_id FROM linea_estacion WHERE linea_id=?", Integer.class, lineaId);
        for (int indice = 0; indice < solicitud.estaciones().size(); indice++) {
            var estacion = solicitud.estaciones().get(indice);
            if (!identificadores.add(estacion.estacionId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una estación no puede repetirse en la misma línea.");
            }
            if ((indice == 0 && estacion.distanciaKm().signum() != 0)
                    || (indice > 0 && estacion.distanciaKm().signum() <= 0)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La primera estación debe tener distancia cero; las siguientes, una distancia mayor que cero.");
            }
            if (!Boolean.TRUE.equals(datos.queryForObject(
                    "SELECT EXISTS (SELECT 1 FROM estacion WHERE id = ?)", Boolean.class, estacion.estacionId()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La estación seleccionada no existe.");
            }
        }
        datos.update("DELETE FROM linea_estacion WHERE linea_id = ?", lineaId);
        for (int indice = 0; indice < solicitud.estaciones().size(); indice++) {
            var estacion = solicitud.estaciones().get(indice);
            datos.update("INSERT INTO linea_estacion (linea_id, estacion_id, orden, distancia_desde_anterior_km) VALUES (?, ?, ?, ?)",
                    lineaId, estacion.estacionId(), indice + 1, estacion.distanciaKm());
        }
        for(Integer estacionId:anteriores) operacion.exigirEstacionConLinea(estacionId);
        operacion.validarActivas();
        auditoria.registrar("linea", lineaId, "configurar_ruta");
        return listar(lineaId);
    }

    private void comprobarLinea(Integer id) {
        if (!Boolean.TRUE.equals(datos.queryForObject("SELECT EXISTS (SELECT 1 FROM linea WHERE id = ?)", Boolean.class, id))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Línea no encontrada.");
        }
    }

    public record RutaSolicitud(@NotNull @Size(max = 200) List<@NotNull @Valid EstacionRutaSolicitud> estaciones) { }
    public record EstacionRutaSolicitud(@NotNull @Positive Integer estacionId,
                                        @NotNull @DecimalMin("0") @Digits(integer = 5, fraction = 3) BigDecimal distanciaKm) { }
    public record EstacionRutaRespuesta(Integer estacionId, Integer orden, BigDecimal distanciaKm) { }
}
