package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;
import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/referencias")
public class ReferenciaController {
    private final JdbcTemplate datos;
    private final UsuarioService usuarios;
    private final RutaController rutas;
    private final TransactionTemplate transacciones;
    private final Validator validacion;
    private final OperacionService operacion;
    public ReferenciaController(JdbcTemplate datos,UsuarioService usuarios,RutaController rutas,TransactionTemplate transacciones,Validator validacion,OperacionService operacion) {
        this.datos=datos; this.usuarios=usuarios; this.rutas=rutas; this.transacciones=transacciones; this.validacion=validacion; this.operacion=operacion;
    }
    @PostMapping public Map<String,Object> cargar(@Valid @RequestBody Carga s) {
        if(!s.autorizada()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Confirme que la fuente está autorizada.");
        var aceptados=new ArrayList<Integer>();
        var rechazados=new ArrayList<Map<String,Object>>();
        // Cada ruta es una unidad: un rechazo no deja estaciones o secuencias a medio cargar.
        for(int i=0;i<s.rutas().size();i++) {
            final Ruta ruta=s.rutas().get(i);
            try {
                var errores=validacion.validate(ruta);
                if(!errores.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Revise los campos de la ruta y sus estaciones.");
                Integer id=transacciones.execute(estado -> guardar(ruta));
                aceptados.add(id);
            } catch(RuntimeException error) {
                String mensaje=error instanceof ResponseStatusException regla ? regla.getReason() : "Datos duplicados o referencias no válidas.";
                rechazados.add(Map.of("fila",i+1,"codigo",ruta.codigo()==null ? "" : ruta.codigo(),"motivo",mensaje==null ? "Datos no válidos." : mensaje));
            }
        }
        return Map.of("fuente",s.fuente(),"aceptados",aceptados,"rechazados",rechazados);
    }
    private int guardar(Ruta s) {
        operacion.bloquear();
        Integer linea=datos.queryForObject("""
                INSERT INTO linea(municipalidad_id,codigo,nombre) VALUES (?,?,?)
                ON CONFLICT(codigo) DO UPDATE SET nombre=EXCLUDED.nombre RETURNING id
                """,Integer.class,s.municipalidadId(),s.codigo(),s.nombre());
        var secuencia=new ArrayList<RutaController.EstacionRutaSolicitud>();
        for(Estacion e:s.estaciones()) {
            int id=datos.queryForObject("""
                    INSERT INTO estacion(municipalidad_id,nombre,direccion,zona,codigo_ruta) VALUES (?,?,?,?,?)
                    ON CONFLICT(municipalidad_id,codigo_ruta) DO UPDATE SET nombre=EXCLUDED.nombre,
                    direccion=EXCLUDED.direccion,zona=EXCLUDED.zona RETURNING id
                    """,Integer.class,s.municipalidadId(),e.nombre(),e.direccion(),e.zona(),e.codigoRuta());
            secuencia.add(new RutaController.EstacionRutaSolicitud(id,e.distanciaKm()));
        }
        rutas.guardar(linea,new RutaController.RutaSolicitud(secuencia));
        usuarios.registrar("linea",linea,"cargar_referencia");
        return linea;
    }
    public record Carga(@NotBlank String fuente,boolean autorizada,@NotEmpty List<@NotNull Ruta> rutas) { }
    public record Ruta(@NotNull @Positive Integer municipalidadId,@NotBlank @Size(max=20) String codigo,
                       @NotBlank @Size(max=120) String nombre,@NotEmpty List<@Valid Estacion> estaciones) { }
    public record Estacion(@NotBlank @Size(max=120) String nombre,@NotBlank @Size(max=250) String direccion,
                           @NotNull @Positive Integer zona,@NotBlank @Size(max=30) String codigoRuta,
                           @NotNull @DecimalMin("0") @Digits(integer=5,fraction=3) BigDecimal distanciaKm) { }
}
