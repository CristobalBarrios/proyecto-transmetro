package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PreparacionTest {
    @Test void compruebaRecursosDeTodasLasLineas() {
        var datos = mock(JdbcTemplate.class);
        when(datos.queryForList(contains("SELECT codigo"), eq(1))).thenReturn(List.of(Map.of("codigo", "L1", "nombre", "Línea", "estado", "operativa")));
        when(datos.queryForList(contains("SELECT id FROM linea ORDER BY id"), eq(Integer.class))).thenReturn(List.of(1));
        when(datos.queryForObject(anyString(), eq(Integer.class), eq(1))).thenReturn(2, 3, 0, 0);
        assertDoesNotThrow(() -> new OperacionService(datos).validarActivas());
        verify(datos, times(4)).queryForObject(anyString(), eq(Integer.class), eq(1));
    }
}
