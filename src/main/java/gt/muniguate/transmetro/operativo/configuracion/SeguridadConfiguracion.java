package gt.muniguate.transmetro.operativo.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
public class SeguridadConfiguracion {

    @Bean
    SecurityFilterChain filtroDeSeguridad(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers("/login", "/login.html", "/login.js", "/styles.css", "/transmetro-logo.png", "/api/csrf")
                        .permitAll()
                        .requestMatchers("/api/usuarios/**").hasRole("administrador")
                        .requestMatchers("/api/bitacora").hasAnyRole("administrador", "supervisor")
                        .requestMatchers("/api/referencias/**").hasAnyRole("administrador", "supervisor", "estaciones")
                        .requestMatchers("/api/recorridos/**", "/api/visitas/**", "/api/alertas/**", "/api/operacion/**")
                        .hasAnyRole("administrador", "supervisor", "operador")
                        .requestMatchers(HttpMethod.GET, "/api/pilotos/**", "/api/educacion-pilotos/**")
                        .hasAnyRole("administrador", "supervisor", "personal", "operador", "flota")
                        .requestMatchers(HttpMethod.GET, "/api/guardias/**")
                        .hasAnyRole("administrador", "supervisor", "personal", "seguridad")
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .requestMatchers("/api/lineas/**", "/api/estaciones/**", "/api/rutas/**")
                        .hasAnyRole("administrador", "supervisor", "estaciones")
                        .requestMatchers("/api/buses/**", "/api/parqueos/**").hasAnyRole("administrador", "supervisor", "flota")
                        .requestMatchers("/api/pilotos/**", "/api/educacion-pilotos/**").hasAnyRole("administrador", "supervisor", "personal")
                        .requestMatchers("/api/guardias/**", "/api/accesos/**", "/api/asignaciones-guardias/**")
                        .hasAnyRole("administrador", "supervisor", "seguridad")
                        .requestMatchers("/api/municipalidades/**").hasAnyRole("administrador", "supervisor", "estaciones")
                        .anyRequest()
                        .authenticated())
                .formLogin(formulario -> formulario.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
                .csrf(csrf -> csrf.csrfTokenRepository(new HttpSessionCsrfTokenRepository()))
                .build();
    }

    @Bean
    ApplicationRunner cuentaInicial(JdbcTemplate datos,
            @Value("${APP_USERNAME:}") String usuario,
            @Value("${APP_PASSWORD:}") String contrasena,
            PasswordEncoder codificador) {
        if (usuario.isBlank() || contrasena.isBlank()) {
            throw new IllegalStateException("Defina APP_USERNAME y APP_PASSWORD antes de iniciar la aplicacion.");
        }

        return args -> datos.update("""
                INSERT INTO usuario (nombre, nombre_usuario, password_hash, perfil) VALUES (?, ?, ?, 'administrador')
                ON CONFLICT (nombre_usuario) DO UPDATE SET password_hash = EXCLUDED.password_hash,
                    perfil = 'administrador', estacion_id = NULL, activo = true
                """, usuario, usuario, codificador.encode(contrasena));
    }

    @Bean
    UserDetailsService usuariosRegistrados(JdbcTemplate datos) {
        return nombre -> datos.query("SELECT nombre_usuario, password_hash, perfil, activo FROM usuario WHERE nombre_usuario = ?",
                (f, n) -> User.withUsername(f.getString("nombre_usuario")).password(f.getString("password_hash"))
                        .roles(f.getString("perfil")).disabled(!f.getBoolean("activo")).build(), nombre)
                .stream().findFirst().orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
    }

    @Bean
    PasswordEncoder codificadorDeContrasena() {
        return new BCryptPasswordEncoder();
    }
}
