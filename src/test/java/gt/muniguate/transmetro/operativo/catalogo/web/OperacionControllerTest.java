package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;

import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OperacionControllerTest {
    @Test void activaLineaPreparada() {
        var datos = mock(JdbcTemplate.class);
        var servicio = mock(OperacionService.class);
        var preparada = new OperacionService.Preparacion(1, "L1", "Línea", "borrador", 2, 3, List.of());
        var activa = new OperacionService.Preparacion(1, "L1", "Línea", "operativa", 2, 3, List.of());
        when(servicio.revisar(1)).thenReturn(preparada, activa);
        var respuesta = new OperacionController(datos, servicio, mock(UsuarioService.class)).cambiarEstado(1, new OperacionController.EstadoSolicitud("operativa"));
        assertEquals("operativa", respuesta.estado());
        verify(servicio).bloquear();
        verify(servicio).exigirPreparada(preparada);
        verify(datos).update("UPDATE linea SET estado = ? WHERE id = ?", "operativa", 1);
    }

    @Test void desactivaLineaSinRecorridos() {
        var datos = mock(JdbcTemplate.class);
        var servicio = mock(OperacionService.class);
        when(servicio.revisar(1)).thenReturn(new OperacionService.Preparacion(1, "L1", "Línea", "inactiva", 2, 3, List.of()));
        when(datos.queryForObject(anyString(), eq(Boolean.class), eq(1))).thenReturn(false);
        assertEquals("inactiva", new OperacionController(datos, servicio, mock(UsuarioService.class))
                .cambiarEstado(1, new OperacionController.EstadoSolicitud("inactiva")).estado());
        verify(datos).update("UPDATE linea SET estado = ? WHERE id = ?", "inactiva", 1);
    }
}
