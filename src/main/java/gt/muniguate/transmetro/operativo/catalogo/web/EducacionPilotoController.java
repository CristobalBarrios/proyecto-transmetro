package gt.muniguate.transmetro.operativo.catalogo.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/educacion-pilotos")
public class EducacionPilotoController {
    private final JdbcTemplate datos;

    public EducacionPilotoController(JdbcTemplate datos) { this.datos = datos; }

    @GetMapping
    public List<EducacionRespuesta> listar() {
        return datos.query("SELECT * FROM educacion_piloto ORDER BY piloto_id, anio_finalizacion DESC NULLS LAST, id",
                (fila, numero) -> new EducacionRespuesta(fila.getInt("id"), fila.getInt("piloto_id"),
                        fila.getString("nivel"), fila.getString("institucion"), fila.getString("titulo"),
                        fila.getObject("anio_finalizacion", Integer.class)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EducacionRespuesta crear(@Valid @RequestBody EducacionSolicitud solicitud) {
        comprobarPiloto(solicitud.pilotoId());
        Integer id = datos.queryForObject("""
                INSERT INTO educacion_piloto (piloto_id, nivel, institucion, titulo, anio_finalizacion)
                VALUES (?, ?, ?, ?, ?) RETURNING id
                """, Integer.class, solicitud.pilotoId(), solicitud.nivel(), solicitud.institucion(),
                solicitud.titulo(), solicitud.anioFinalizacion());
        return respuesta(id, solicitud);
    }

    @PutMapping("/{id}")
    public EducacionRespuesta actualizar(@PathVariable Integer id, @Valid @RequestBody EducacionSolicitud solicitud) {
        comprobarPiloto(solicitud.pilotoId());
        int filas = datos.update("""
                UPDATE educacion_piloto SET piloto_id = ?, nivel = ?, institucion = ?, titulo = ?, anio_finalizacion = ? WHERE id = ?
                """, solicitud.pilotoId(), solicitud.nivel(), solicitud.institucion(), solicitud.titulo(), solicitud.anioFinalizacion(), id);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Formación no encontrada.");
        return respuesta(id, solicitud);
    }

    private void comprobarPiloto(Integer id) {
        if (!Boolean.TRUE.equals(datos.queryForObject("SELECT EXISTS (SELECT 1 FROM piloto WHERE id = ?)", Boolean.class, id))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El piloto seleccionado no existe.");
        }
    }

    private EducacionRespuesta respuesta(Integer id, EducacionSolicitud s) {
        return new EducacionRespuesta(id, s.pilotoId(), s.nivel(), s.institucion(), s.titulo(), s.anioFinalizacion());
    }

    public record EducacionSolicitud(@NotNull @Positive Integer pilotoId, @NotBlank @Size(max = 60) String nivel,
                                     @NotBlank @Size(max = 150) String institucion, @Size(max = 150) String titulo,
                                     @Min(1900) @Max(2100) Integer anioFinalizacion) { }
    public record EducacionRespuesta(Integer id, Integer pilotoId, String nivel, String institucion, String titulo, Integer anioFinalizacion) { }
}
