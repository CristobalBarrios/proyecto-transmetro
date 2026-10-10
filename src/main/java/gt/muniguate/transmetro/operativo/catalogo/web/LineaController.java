package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.domain.Linea;
import gt.muniguate.transmetro.operativo.catalogo.repository.LineaRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lineas")
public class LineaController {
    private static final int MUNICIPALIDAD_DEMOSTRACION_ID = 1;
    private final LineaRepository lineas;

    public LineaController(LineaRepository lineas) {
        this.lineas = lineas;
    }

    @GetMapping
    public List<LineaRespuesta> listar() {
        return lineas.findAllByOrderByCodigoAsc().stream()
                .map(linea -> new LineaRespuesta(linea.getId(), linea.getCodigo(), linea.getNombre()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LineaRespuesta crear(@Valid @RequestBody LineaSolicitud solicitud) {
        Linea linea = lineas.save(new Linea(MUNICIPALIDAD_DEMOSTRACION_ID, solicitud.codigo(), solicitud.nombre()));
        return new LineaRespuesta(linea.getId(), linea.getCodigo(), linea.getNombre());
    }

    public record LineaSolicitud(
            @NotBlank @Size(max = 20) String codigo,
            @NotBlank @Size(max = 120) String nombre) {
    }

    public record LineaRespuesta(Integer id, String codigo, String nombre) {
    }
}
