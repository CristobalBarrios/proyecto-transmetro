package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;

import gt.muniguate.transmetro.operativo.catalogo.domain.Linea;
import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import gt.muniguate.transmetro.operativo.catalogo.RegistroRedService;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;
import gt.muniguate.transmetro.operativo.catalogo.repository.LineaRepository;
import jakarta.validation.Valid;
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
@RequestMapping("/api/lineas")
public class LineaController {
    private final UsuarioService auditoria;
    private final LineaRepository lineas;
    private final OperacionService operacion;
    private final RegistroRedService registro;

    public LineaController(LineaRepository lineas, OperacionService operacion, UsuarioService auditoria, RegistroRedService registro) {
        this.auditoria = auditoria;
        this.lineas = lineas;
        this.operacion = operacion;
        this.registro = registro;
    }

    @GetMapping
    public List<LineaRespuesta> listar() {
        return lineas.findAllByOrderByCodigoAsc().stream()
                .map(linea -> new LineaRespuesta(linea.getId(), linea.getMunicipalidadId(), linea.getCodigo(), linea.getNombre()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LineaRespuesta crear(@Valid @RequestBody LineaSolicitud solicitud) {
        operacion.bloquear();
        if(solicitud.estacionInicial()==null || solicitud.busId()==null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Registre la primera estación, su operador y un bus sin línea asignada.");
        Linea linea = lineas.saveAndFlush(new Linea(solicitud.municipalidadId(), solicitud.codigo(), solicitud.nombre()));
        registro.crearEstacion(solicitud.municipalidadId(),solicitud.estacionInicial(),linea.getId(),BigDecimal.ZERO,solicitud.busId());
        auditoria.registrar("linea", linea.getId(), "guardar");
        return new LineaRespuesta(linea.getId(), linea.getMunicipalidadId(), linea.getCodigo(), linea.getNombre());
    }

    @PutMapping("/{id}")
    @Transactional
    public LineaRespuesta actualizar(@PathVariable Integer id, @Valid @RequestBody LineaSolicitud solicitud) {
        operacion.bloquear();
        Linea linea = lineas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Línea no encontrada."));
        linea.actualizar(solicitud.municipalidadId(), solicitud.codigo(), solicitud.nombre());
        Linea actualizada = lineas.save(linea);
        auditoria.registrar("linea", actualizada.getId(), "guardar");
        return new LineaRespuesta(actualizada.getId(), actualizada.getMunicipalidadId(), actualizada.getCodigo(), actualizada.getNombre());
    }

    public record LineaSolicitud(
            @NotNull @Positive Integer municipalidadId,
            @NotBlank @Size(max = 20) String codigo,
            @NotBlank @Size(max = 120) String nombre,
            @Valid RegistroRedService.EstacionInicial estacionInicial, @Positive Integer busId) {
    }

    public record LineaRespuesta(Integer id, Integer municipalidadId, String codigo, String nombre) {
    }
}
