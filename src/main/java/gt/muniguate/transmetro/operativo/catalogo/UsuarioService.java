package gt.muniguate.transmetro.operativo.catalogo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class UsuarioService {
    private final JdbcTemplate datos;
    public UsuarioService(JdbcTemplate datos) { this.datos = datos; }

    public Sesion actual() {
        String nombre = SecurityContextHolder.getContext().getAuthentication().getName();
        return datos.queryForObject("SELECT id, nombre_usuario, perfil, estacion_id FROM usuario WHERE nombre_usuario = ? AND activo",
                (f, n) -> new Sesion(f.getInt("id"), f.getString("nombre_usuario"), f.getString("perfil"),
                        f.getObject("estacion_id", Integer.class)), nombre);
    }

    public void comprobarEstacion(int estacionId) {
        var usuario = actual();
        if ("operador".equals(usuario.perfil()) && !Integer.valueOf(estacionId).equals(usuario.estacionId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puede operar su estación asignada.");
    }

    public void registrar(String entidad, int id, String accion) {
        datos.update("INSERT INTO bitacora (usuario_id, accion, entidad, registro_id) VALUES (?, ?, ?, ?)",
                actual().id(), accion, entidad, id);
    }

    public record Sesion(int id, String nombreUsuario, String perfil, Integer estacionId) { }
}
