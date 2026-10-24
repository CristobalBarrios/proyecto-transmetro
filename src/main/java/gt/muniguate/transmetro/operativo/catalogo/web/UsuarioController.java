package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;
import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UsuarioController {
    private final JdbcTemplate datos;
    private final UsuarioService usuarios;
    private final PasswordEncoder claves;
    private final OperacionService operacion;
    public UsuarioController(JdbcTemplate datos, UsuarioService usuarios, PasswordEncoder claves,OperacionService operacion) {
        this.datos = datos; this.usuarios = usuarios; this.claves = claves; this.operacion=operacion;
    }
    @GetMapping("/sesion") public UsuarioService.Sesion sesion() { return usuarios.actual(); }
    @GetMapping("/usuarios") public List<Map<String, Object>> listar() {
        return datos.queryForList("SELECT id, nombre, nombre_usuario AS \"nombreUsuario\", perfil, estacion_id AS \"estacionId\", activo FROM usuario ORDER BY id");
    }
    @PostMapping("/usuarios") @Transactional @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crear(@Valid @RequestBody Solicitud s) {
        operacion.bloquear();
        if (s.contrasena() == null || s.contrasena().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingrese una contraseña.");
        int id = datos.queryForObject("""
                INSERT INTO usuario(nombre,nombre_usuario,password_hash,perfil,estacion_id,activo)
                VALUES (?,?,?,?,?,?) RETURNING id
                """, Integer.class, s.nombre(), s.nombreUsuario(), claves.encode(s.contrasena()), s.perfil(), s.estacionId(), s.activo());
        usuarios.registrar("usuario", id, "crear");
        return Map.of("id", id);
    }
    @PutMapping("/usuarios/{id}") @Transactional
    public Map<String, Object> actualizar(@PathVariable int id, @Valid @RequestBody Solicitud s, jakarta.servlet.http.HttpServletRequest request) {
        operacion.bloquear();
        boolean propia=usuarios.actual().id()==id;
        usuarios.registrar("usuario", id, "actualizar");
        int filas = datos.update("UPDATE usuario SET nombre=?,nombre_usuario=?,perfil=?,estacion_id=?,activo=? WHERE id=?",
                s.nombre(), s.nombreUsuario(), s.perfil(), s.estacionId(), s.activo(), id);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        if (s.contrasena() != null && !s.contrasena().isBlank())
            datos.update("UPDATE usuario SET password_hash=? WHERE id=?", claves.encode(s.contrasena()), id);
        operacion.validarActivas();
        if(propia) {
            var sesion=request.getSession(false);
            if(sesion!=null) sesion.invalidate();
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
        return Map.of("id", id);
    }
    @GetMapping("/bitacora") public List<Map<String, Object>> bitacora() {
        return datos.queryForList("""
                SELECT b.id,u.nombre_usuario AS usuario,b.fecha,b.accion,b.entidad,b.registro_id
                FROM bitacora b JOIN usuario u ON u.id=b.usuario_id ORDER BY b.id DESC
                """);
    }
    public record Solicitud(@NotBlank @Size(max=150) String nombre, @NotBlank @Size(max=60) String nombreUsuario,
                            @Size(max=72) String contrasena,
                            @NotBlank @Pattern(regexp="supervisor|operador|flota|estaciones|seguridad|personal|administrador") String perfil,
                            @Positive Integer estacionId, boolean activo) { }
}
