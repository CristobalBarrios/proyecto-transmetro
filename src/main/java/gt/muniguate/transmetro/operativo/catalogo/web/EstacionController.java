package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;
import gt.muniguate.transmetro.operativo.catalogo.RegistroRedService;
import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import org.springframework.transaction.annotation.Transactional;

import gt.muniguate.transmetro.operativo.catalogo.domain.Estacion;
import gt.muniguate.transmetro.operativo.catalogo.repository.EstacionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

@Transactional
@RestController
@RequestMapping("/api/estaciones")
public class EstacionController {
    private final UsuarioService auditoria;
    private final EstacionRepository estaciones;
    private final RegistroRedService registro;

    public EstacionController(EstacionRepository estaciones, UsuarioService auditoria, RegistroRedService registro) {
        this.auditoria = auditoria;
        this.estaciones = estaciones;
        this.registro = registro;
    }

    @GetMapping
    public List<EstacionRespuesta> listar() {
        return estaciones.findAllByOrderByZonaAscCodigoRutaAsc().stream()
                .map(estacion -> new EstacionRespuesta(
                        estacion.getId(), estacion.getMunicipalidadId(), estacion.getNombre(), estacion.getDireccion(),
                        estacion.getZona(), estacion.getCodigoRuta()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstacionRespuesta crear(@Valid @RequestBody EstacionSolicitud solicitud) {
        var inicial=new RegistroRedService.EstacionInicial(solicitud.nombre(),solicitud.direccion(),solicitud.zona(),solicitud.codigoRuta(),solicitud.operador());
        int id=registro.crearEstacion(solicitud.municipalidadId(),inicial,solicitud.lineaId(),solicitud.distanciaKm(),solicitud.busId());
        return new EstacionRespuesta(id,solicitud.municipalidadId(),solicitud.nombre(),solicitud.direccion(),solicitud.zona(),solicitud.codigoRuta());
    }

    @PutMapping("/{id}")
    public EstacionRespuesta actualizar(@PathVariable Integer id, @Valid @RequestBody EstacionSolicitud solicitud) {
        Estacion estacion = estaciones.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estación no encontrada."));
        estacion.actualizar(solicitud.municipalidadId(), solicitud.nombre(), solicitud.direccion(), solicitud.zona(), solicitud.codigoRuta());
        Estacion actualizada = estaciones.save(estacion);
        auditoria.registrar("estacion", actualizada.getId(), "guardar");
        return new EstacionRespuesta(actualizada.getId(), actualizada.getMunicipalidadId(), actualizada.getNombre(), actualizada.getDireccion(),
                actualizada.getZona(), actualizada.getCodigoRuta());
    }

    public record EstacionSolicitud(
            @NotNull @Positive Integer municipalidadId,
            @NotBlank @Size(max = 120) String nombre,
            @NotBlank @Size(max = 250) String direccion,
            @Min(1) short zona,
            @NotBlank @Size(max = 30) String codigoRuta,
            @Positive Integer lineaId, @DecimalMin("0") @Digits(integer=5,fraction=3) BigDecimal distanciaKm,
            @Positive Integer busId, @Valid RegistroRedService.Operador operador) {
    }

    public record EstacionRespuesta(Integer id, Integer municipalidadId, String nombre, String direccion, short zona, String codigoRuta) {
    }
}
