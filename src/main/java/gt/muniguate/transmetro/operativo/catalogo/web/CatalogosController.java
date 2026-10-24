package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;

import gt.muniguate.transmetro.operativo.catalogo.domain.*;
import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import org.springframework.transaction.annotation.Transactional;
import gt.muniguate.transmetro.operativo.catalogo.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Transactional
@RestController
@RequestMapping("/api")
public class CatalogosController {
    private final UsuarioService auditoria;
    private final MunicipalidadRepository municipalidades;
    private final ParqueoRepository parqueos;
    private final BusRepository buses;
    private final PilotoRepository pilotos;
    private final GuardiaRepository guardias;
    private final AccesoRepository accesos;
    private final OperacionService operacion;

    public CatalogosController(MunicipalidadRepository municipalidades, ParqueoRepository parqueos, BusRepository buses,
                               PilotoRepository pilotos, GuardiaRepository guardias, AccesoRepository accesos, OperacionService operacion, UsuarioService auditoria) {
        this.auditoria = auditoria;
        this.municipalidades = municipalidades;
        this.parqueos = parqueos;
        this.buses = buses;
        this.pilotos = pilotos;
        this.guardias = guardias;
        this.accesos = accesos;
        this.operacion = operacion;
    }

    @GetMapping("/municipalidades")
    public List<MunicipalidadRespuesta> listarMunicipalidades() {
        return municipalidades.findAllByOrderByNombreAsc().stream().map(this::municipalidad).toList();
    }

    @PostMapping("/municipalidades") @ResponseStatus(HttpStatus.CREATED)
    public MunicipalidadRespuesta crearMunicipalidad(@Valid @RequestBody MunicipalidadSolicitud solicitud) {
        var guardado = municipalidades.save(new Municipalidad(solicitud.nombre()));
        auditoria.registrar("municipalidad", guardado.getId(), "guardar");
        return municipalidad(guardado);
    }

    @PutMapping("/municipalidades/{id}")
    public MunicipalidadRespuesta actualizarMunicipalidad(@PathVariable Integer id, @Valid @RequestBody MunicipalidadSolicitud solicitud) {
        Municipalidad entidad = municipalidades.findById(id).orElseThrow(() -> noEncontrado("Municipalidad"));
        entidad.actualizar(solicitud.nombre());
        var guardado = municipalidades.save(entidad);
        auditoria.registrar("municipalidad", guardado.getId(), "guardar");
        return municipalidad(guardado);
    }

    @GetMapping("/parqueos")
    public List<ParqueoRespuesta> listarParqueos() { return parqueos.findAllByOrderByNombreAsc().stream().map(this::parqueo).toList(); }

    @PostMapping("/parqueos") @ResponseStatus(HttpStatus.CREATED)
    public ParqueoRespuesta crearParqueo(@Valid @RequestBody ParqueoSolicitud solicitud) {
        var guardado = parqueos.save(new Parqueo(solicitud.estacionId(), solicitud.nombre()));
        auditoria.registrar("parqueo", guardado.getId(), "guardar");
        return parqueo(guardado);
    }

    @PutMapping("/parqueos/{id}")
    public ParqueoRespuesta actualizarParqueo(@PathVariable Integer id, @Valid @RequestBody ParqueoSolicitud solicitud) {
        Parqueo entidad = parqueos.findById(id).orElseThrow(() -> noEncontrado("Parqueo"));
        entidad.actualizar(solicitud.estacionId(), solicitud.nombre());
        var guardado = parqueos.save(entidad);
        auditoria.registrar("parqueo", guardado.getId(), "guardar");
        return parqueo(guardado);
    }

    @GetMapping("/buses")
    public List<BusRespuesta> listarBuses() { return buses.findAllByOrderByPlacaAsc().stream().map(this::bus).toList(); }

    @PostMapping("/buses") @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public BusRespuesta crearBus(@Valid @RequestBody BusSolicitud solicitud) {
        operacion.bloquear();
        Bus entidad = buses.saveAndFlush(new Bus(solicitud.placa(), solicitud.capacidadMaxima(), solicitud.lineaId(), solicitud.parqueoId(), solicitud.estado()));
        operacion.validarActivas();
        auditoria.registrar("bus", entidad.getId(), "guardar");
        return bus(entidad);
    }

    @PutMapping("/buses/{id}")
    @Transactional
    public BusRespuesta actualizarBus(@PathVariable Integer id, @Valid @RequestBody BusSolicitud solicitud) {
        operacion.bloquear();
        operacion.validarBusEnCurso(id, solicitud.lineaId(), solicitud.estado());
        Bus entidad = buses.findById(id).orElseThrow(() -> noEncontrado("Bus"));
        entidad.actualizar(solicitud.placa(), solicitud.capacidadMaxima(), solicitud.lineaId(), solicitud.parqueoId(), solicitud.estado());
        buses.saveAndFlush(entidad);
        operacion.validarActivas();
        auditoria.registrar("bus", entidad.getId(), "guardar");
        return bus(entidad);
    }

    @GetMapping("/pilotos")
    public List<PilotoRespuesta> listarPilotos() { return pilotos.findAllByOrderByNombreAsc().stream().map(this::piloto).toList(); }

    @PostMapping("/pilotos") @ResponseStatus(HttpStatus.CREATED)
    public PilotoRespuesta crearPiloto(@Valid @RequestBody PilotoSolicitud solicitud) {
        var guardado = pilotos.save(new Piloto(solicitud.nombre(), solicitud.residencia(), solicitud.telefono(), solicitud.correo()));
        auditoria.registrar("piloto", guardado.getId(), "guardar");
        return piloto(guardado);
    }

    @PutMapping("/pilotos/{id}")
    public PilotoRespuesta actualizarPiloto(@PathVariable Integer id, @Valid @RequestBody PilotoSolicitud solicitud) {
        Piloto entidad = pilotos.findById(id).orElseThrow(() -> noEncontrado("Piloto"));
        entidad.actualizar(solicitud.nombre(), solicitud.residencia(), solicitud.telefono(), solicitud.correo());
        var guardado = pilotos.save(entidad);
        auditoria.registrar("piloto", guardado.getId(), "guardar");
        return piloto(guardado);
    }

    @GetMapping("/guardias")
    public List<GuardiaRespuesta> listarGuardias() { return guardias.findAllByOrderByNombreAsc().stream().map(this::guardia).toList(); }

    @PostMapping("/guardias") @ResponseStatus(HttpStatus.CREATED)
    public GuardiaRespuesta crearGuardia(@Valid @RequestBody GuardiaSolicitud solicitud) {
        var guardado = guardias.save(new Guardia(solicitud.nombre(), solicitud.telefono()));
        auditoria.registrar("guardia", guardado.getId(), "guardar");
        return guardia(guardado);
    }

    @PutMapping("/guardias/{id}")
    public GuardiaRespuesta actualizarGuardia(@PathVariable Integer id, @Valid @RequestBody GuardiaSolicitud solicitud) {
        Guardia entidad = guardias.findById(id).orElseThrow(() -> noEncontrado("Guardia"));
        entidad.actualizar(solicitud.nombre(), solicitud.telefono());
        var guardado = guardias.save(entidad);
        auditoria.registrar("guardia", guardado.getId(), "guardar");
        return guardia(guardado);
    }

    @GetMapping("/accesos")
    public List<AccesoRespuesta> listarAccesos() { return accesos.findAllByOrderByCodigoAsc().stream().map(this::acceso).toList(); }

    @PostMapping("/accesos") @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AccesoRespuesta crearAcceso(@Valid @RequestBody AccesoSolicitud solicitud) {
        operacion.bloquear();
        Acceso entidad = accesos.saveAndFlush(new Acceso(solicitud.estacionId(), solicitud.codigo(), solicitud.descripcion()));
        operacion.validarActivas();
        auditoria.registrar("acceso", entidad.getId(), "guardar");
        return acceso(entidad);
    }

    @PutMapping("/accesos/{id}")
    @Transactional
    public AccesoRespuesta actualizarAcceso(@PathVariable Integer id, @Valid @RequestBody AccesoSolicitud solicitud) {
        operacion.bloquear();
        Acceso entidad = accesos.findById(id).orElseThrow(() -> noEncontrado("Acceso"));
        entidad.actualizar(solicitud.estacionId(), solicitud.codigo(), solicitud.descripcion());
        accesos.saveAndFlush(entidad);
        operacion.validarActivas();
        auditoria.registrar("acceso", entidad.getId(), "guardar");
        return acceso(entidad);
    }

    private ResponseStatusException noEncontrado(String entidad) { return new ResponseStatusException(HttpStatus.NOT_FOUND, entidad + " no encontrado."); }
    private MunicipalidadRespuesta municipalidad(Municipalidad e) { return new MunicipalidadRespuesta(e.getId(), e.getNombre()); }
    private ParqueoRespuesta parqueo(Parqueo e) { return new ParqueoRespuesta(e.getId(), e.getEstacionId(), e.getNombre()); }
    private BusRespuesta bus(Bus e) { return new BusRespuesta(e.getId(), e.getPlaca(), e.getCapacidadMaxima(), e.getLineaId(), e.getParqueoId(), e.getEstado()); }
    private PilotoRespuesta piloto(Piloto e) { return new PilotoRespuesta(e.getId(), e.getNombre(), e.getResidencia(), e.getTelefono(), e.getCorreo()); }
    private GuardiaRespuesta guardia(Guardia e) { return new GuardiaRespuesta(e.getId(), e.getNombre(), e.getTelefono()); }
    private AccesoRespuesta acceso(Acceso e) { return new AccesoRespuesta(e.getId(), e.getEstacionId(), e.getCodigo(), e.getDescripcion()); }

    public record MunicipalidadSolicitud(@NotBlank @Size(max = 120) String nombre) { }
    public record MunicipalidadRespuesta(Integer id, String nombre) { }
    public record ParqueoSolicitud(@Positive Integer estacionId, @NotBlank @Size(max = 120) String nombre) { }
    public record ParqueoRespuesta(Integer id, Integer estacionId, String nombre) { }
    public record BusSolicitud(@NotBlank @Size(max = 20) String placa, @NotNull @Positive Integer capacidadMaxima,
                               @Positive Integer lineaId, @NotNull @Positive Integer parqueoId,
                               @NotBlank @Pattern(regexp = "disponible|mantenimiento|fuera_servicio") String estado) { }
    public record BusRespuesta(Integer id, String placa, Integer capacidadMaxima, Integer lineaId, Integer parqueoId, String estado) { }
    public record PilotoSolicitud(@NotBlank @Size(max = 150) String nombre, @NotBlank @Size(max = 250) String residencia,
                                  @NotBlank @Size(max = 30) String telefono, @Size(max = 150) String correo) { }
    public record PilotoRespuesta(Integer id, String nombre, String residencia, String telefono, String correo) { }
    public record GuardiaSolicitud(@NotBlank @Size(max = 150) String nombre, @Size(max = 30) String telefono) { }
    public record GuardiaRespuesta(Integer id, String nombre, String telefono) { }
    public record AccesoSolicitud(@NotNull @Positive Integer estacionId, @NotBlank @Size(max = 30) String codigo,
                                  @Size(max = 200) String descripcion) { }
    public record AccesoRespuesta(Integer id, Integer estacionId, String codigo, String descripcion) { }
}
