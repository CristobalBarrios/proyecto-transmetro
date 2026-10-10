package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.domain.Estacion;
import gt.muniguate.transmetro.operativo.catalogo.repository.EstacionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/estaciones")
public class EstacionController {
    private static final int MUNICIPALIDAD_DEMOSTRACION_ID = 1;
    private final EstacionRepository estaciones;

    public EstacionController(EstacionRepository estaciones) {
        this.estaciones = estaciones;
    }

    @GetMapping
    public List<EstacionRespuesta> listar() {
        return estaciones.findAllByOrderByZonaAscCodigoRutaAsc().stream()
                .map(estacion -> new EstacionRespuesta(
                        estacion.getId(), estacion.getNombre(), estacion.getDireccion(),
                        estacion.getZona(), estacion.getCodigoRuta()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstacionRespuesta crear(@Valid @RequestBody EstacionSolicitud solicitud) {
        Estacion estacion = estaciones.save(new Estacion(
                MUNICIPALIDAD_DEMOSTRACION_ID,
                solicitud.nombre(),
                solicitud.direccion(),
                solicitud.zona(),
                solicitud.codigoRuta()));
        return new EstacionRespuesta(estacion.getId(), estacion.getNombre(), estacion.getDireccion(),
                estacion.getZona(), estacion.getCodigoRuta());
    }

    @PutMapping("/{id}")
    public EstacionRespuesta actualizar(@PathVariable Integer id, @Valid @RequestBody EstacionSolicitud solicitud) {
        Estacion estacion = estaciones.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estación no encontrada."));
        estacion.actualizar(solicitud.nombre(), solicitud.direccion(), solicitud.zona(), solicitud.codigoRuta());
        Estacion actualizada = estaciones.save(estacion);
        return new EstacionRespuesta(actualizada.getId(), actualizada.getNombre(), actualizada.getDireccion(),
                actualizada.getZona(), actualizada.getCodigoRuta());
    }

    public record EstacionSolicitud(
            @NotBlank @Size(max = 120) String nombre,
            @NotBlank @Size(max = 250) String direccion,
            @Min(1) short zona,
            @NotBlank @Size(max = 30) String codigoRuta) {
    }

    public record EstacionRespuesta(Integer id, String nombre, String direccion, short zona, String codigoRuta) {
    }
}
