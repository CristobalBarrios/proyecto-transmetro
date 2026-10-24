package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;

import org.junit.jupiter.api.Test;
import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RutaControllerTest {
    static class Datos extends JdbcTemplate {
        boolean enCurso;
        boolean existeEstacion = true;
        int buses = 2;
        int escrituras;
        List<Object[]> inserciones = new ArrayList<>();
        @Override public <T> List<T> queryForList(String sql, Class<T> tipo, Object... args) { return List.of(tipo.cast(1)); }
        @Override public <T> T queryForObject(String sql, Class<T> tipo, Object... args) {
            Object valor = sql.contains("recorrido") ? enCurso : sql.contains("count(*)") ? buses
                    : sql.contains("FROM estacion") ? existeEstacion : true;
            return tipo.cast(valor);
        }
        @Override public int update(String sql, Object... args) {
            escrituras++;
            if (sql.startsWith("INSERT")) inserciones.add(args);
            return 1;
        }
        @Override public <T> List<T> query(String sql, RowMapper<T> mapper, Object... args) { return List.of(); }
    }
    private RutaController.EstacionRutaSolicitud estacion(int id, String distancia) {
        return new RutaController.EstacionRutaSolicitud(id, new BigDecimal(distancia));
    }
    @Test void generaOrdenConsecutivo() {
        var datos = new Datos();
        new RutaController(datos, mock(OperacionService.class), mock(UsuarioService.class)).guardar(1, new RutaController.RutaSolicitud(List.of(estacion(8, "0"), estacion(3, "1.250"))));
        assertEquals(2, datos.inserciones.size());
        assertEquals(1, datos.inserciones.get(0)[2]);
        assertEquals(2, datos.inserciones.get(1)[2]);
        assertEquals(3, datos.inserciones.get(1)[1]);
        assertEquals(new BigDecimal("1.250"), datos.inserciones.get(1)[3]);
    }
}
