package gt.muniguate.transmetro.operativo.catalogo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;

@Service
public class RegistroRedService {
    private final JdbcTemplate datos;
    private final PasswordEncoder claves;
    private final OperacionService operacion;
    private final UsuarioService usuarios;

    public RegistroRedService(JdbcTemplate datos, PasswordEncoder claves, OperacionService operacion, UsuarioService usuarios) {
        this.datos=datos; this.claves=claves; this.operacion=operacion; this.usuarios=usuarios;
    }

    @Transactional
    public int crearEstacion(int municipalidadId, EstacionInicial estacion, Integer lineaId, BigDecimal distancia, Integer busId) {
        operacion.bloquear();
        if(estacion==null || lineaId==null || distancia==null || estacion.operador()==null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Indique la línea, la distancia y los datos del operador de la estación.");
        int orden=datos.queryForObject("SELECT coalesce(max(orden),0)+1 FROM linea_estacion WHERE linea_id=?",Integer.class,lineaId);
        if((orden==1 && distancia.signum()!=0) || (orden>1 && distancia.signum()<=0))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La primera estación tiene distancia cero; las siguientes requieren una distancia positiva.");
        int id=datos.queryForObject("""
                INSERT INTO estacion(municipalidad_id,nombre,direccion,zona,codigo_ruta)
                VALUES (?,?,?,?,?) RETURNING id
                """,Integer.class,municipalidadId,estacion.nombre(),estacion.direccion(),estacion.zona(),estacion.codigoRuta());
        crearOperador(id,estacion.operador());
        datos.update("INSERT INTO linea_estacion(linea_id,estacion_id,orden,distancia_desde_anterior_km) VALUES (?,?,?,?)",
                lineaId,id,orden,distancia);
        if(busId!=null && datos.update("UPDATE bus SET linea_id=? WHERE id=? AND linea_id IS NULL",lineaId,busId)!=1)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Seleccione un bus sin línea asignada.");
        operacion.exigirPreparada(operacion.revisar(lineaId));
        usuarios.registrar("estacion",id,"guardar");
        return id;
    }

    public void crearOperador(int estacionId, Operador operador) {
        if(operador==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Incluya los datos del operador de cada estación nueva.");
        datos.update("""
                INSERT INTO usuario(nombre,nombre_usuario,password_hash,perfil,estacion_id)
                VALUES (?,?,?,'operador',?)
                """,operador.nombre(),operador.usuario(),claves.encode(operador.contrasena()),estacionId);
    }

    public record Operador(@NotBlank @Size(max=150) String nombre, @NotBlank @Size(max=60) String usuario,
                           @NotBlank @Size(max=72) String contrasena) { }
    public record EstacionInicial(@NotBlank @Size(max=120) String nombre, @NotBlank @Size(max=250) String direccion,
                                 @NotNull @Positive Short zona, @NotBlank @Size(max=30) String codigoRuta,
                                 @NotNull @Valid Operador operador) { }
}
