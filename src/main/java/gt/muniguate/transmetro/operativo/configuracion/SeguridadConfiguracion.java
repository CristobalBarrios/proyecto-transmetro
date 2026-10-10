package gt.muniguate.transmetro.operativo.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
public class SeguridadConfiguracion {

    @Bean
    SecurityFilterChain filtroDeSeguridad(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(autorizacion -> autorizacion.anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .csrf(csrf -> csrf.csrfTokenRepository(new HttpSessionCsrfTokenRepository()))
                .build();
    }

    @Bean
    UserDetailsService usuarioDemostracion(
            @Value("${APP_USERNAME:}") String usuario,
            @Value("${APP_PASSWORD:}") String contrasena,
            PasswordEncoder codificador) {
        if (usuario.isBlank() || contrasena.isBlank()) {
            throw new IllegalStateException("Defina APP_USERNAME y APP_PASSWORD antes de iniciar la aplicacion.");
        }

        UserDetails operador = User.withUsername(usuario)
                .password(codificador.encode(contrasena))
                .roles("OPERADOR")
                .build();
        return new InMemoryUserDetailsManager(operador);
    }

    @Bean
    PasswordEncoder codificadorDeContrasena() {
        return new BCryptPasswordEncoder();
    }
}
