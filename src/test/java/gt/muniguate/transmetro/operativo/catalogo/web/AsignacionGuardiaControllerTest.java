package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;

import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AsignacionGuardiaControllerTest {
    @Test void asignaGuardiaConResponsableDeSesion() {
        var datos = mock(JdbcTemplate.class);
        var servicio = mock(OperacionService.class);
        var usuarios = mock(UserDetailsService.class);
        when(usuarios.loadUserByUsername("seguridad")).thenReturn(User.withUsername("seguridad").password("hash").roles("OPERADOR").build());
        when(datos.queryForObject(anyString(), eq(Boolean.class), eq(1))).thenReturn(true);
        when(datos.queryForObject(contains("INSERT INTO usuario"), eq(Integer.class), eq("seguridad"), eq("seguridad"), eq("hash"))).thenReturn(7);
        when(datos.queryForObject(contains("INSERT INTO guardia_acceso"), eq(Integer.class), eq(1), eq(1), eq(7))).thenReturn(9);
        var controlador = spy(new AsignacionGuardiaController(datos, servicio, usuarios, mock(UsuarioService.class)));
        doReturn(List.of(new AsignacionGuardiaController.AsignacionRespuesta(9, "Guardia", "A1", "Estación", "seguridad", OffsetDateTime.now(), null, "activa"))).when(controlador).listar();
        Principal principal = () -> "seguridad";
        var resultado = controlador.asignar(new AsignacionGuardiaController.AsignacionSolicitud(1, 1), principal);
        assertEquals("seguridad", resultado.responsable());
        assertEquals("activa", resultado.estado());
        verify(servicio).bloquear();
    }

    @Test void finalizaAsignacionConCoberturaConservada() {
        var datos = mock(JdbcTemplate.class);
        var servicio = mock(OperacionService.class);
        when(datos.update(contains("UPDATE guardia_acceso"), eq(9))).thenReturn(1);
        new AsignacionGuardiaController(datos, servicio, mock(UserDetailsService.class), mock(UsuarioService.class)).finalizar(9);
        verify(servicio).bloquear();
        verify(servicio).validarActivas();
    }
}
