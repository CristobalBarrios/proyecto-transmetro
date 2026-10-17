package gt.muniguate.transmetro.operativo.catalogo.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RutaControllerTest {
    static class Datos extends JdbcTemplate {
        boolean enCurso;
        boolean existeEstacion = true;
        String estado = "borrador";
        int buses = 2;
        int escrituras;
        List<Object[]> inserciones = new ArrayList<>();
        @Override public <T> List<T> queryForList(String sql, Class<T> tipo, Object... args) { return List.of(tipo.cast(estado)); }
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
    private void rechaza(Datos datos, List<RutaController.EstacionRutaSolicitud> estaciones, int status) {
        var error = assertThrows(ResponseStatusException.class,
                () -> new RutaController(datos).guardar(1, new RutaController.RutaSolicitud(estaciones)));
        assertEquals(status, error.getStatusCode().value());
        assertEquals(0, datos.escrituras);
    }
    @Test void generaOrdenConsecutivo() {
        var datos = new Datos();
        new RutaController(datos).guardar(1, new RutaController.RutaSolicitud(List.of(estacion(8, "0"), estacion(3, "1.250"))));
        assertEquals(2, datos.inserciones.size());
        assertEquals(1, datos.inserciones.get(0)[2]);
        assertEquals(2, datos.inserciones.get(1)[2]);
        assertEquals(3, datos.inserciones.get(1)[1]);
        assertEquals(new BigDecimal("1.250"), datos.inserciones.get(1)[3]);
    }
    @Test void rechazaDuplicados() { rechaza(new Datos(), List.of(estacion(8, "0"), estacion(8, "1")), 400); }
    @Test void rechazaPrimeraDistanciaPositiva() { rechaza(new Datos(), List.of(estacion(8, "1")), 400); }
    @ParameterizedTest @CsvSource({"0", "-1"})
    void rechazaDistanciaPosteriorNoPositiva(String distancia) { rechaza(new Datos(), List.of(estacion(8, "0"), estacion(3, distancia)), 400); }
    @Test void rechazaEstacionInexistente() { var datos = new Datos(); datos.existeEstacion = false; rechaza(datos, List.of(estacion(8, "0")), 400); }
    @Test void rechazaRecorridoEnCurso() { var datos = new Datos(); datos.enCurso = true; rechaza(datos, List.of(estacion(8, "0")), 409); }
    @ParameterizedTest @CsvSource({"0", "3"})
    void rechazaCantidadBusesIncorrecta(int buses) { var datos = new Datos(); datos.estado = "operativa"; datos.buses = buses; rechaza(datos, List.of(estacion(8, "0")), 409); }
    @ParameterizedTest @CsvSource({"1", "2"})
    void aceptaLimitesBuses(int buses) {
        var datos = new Datos(); datos.estado = "operativa"; datos.buses = buses;
        new RutaController(datos).guardar(1, new RutaController.RutaSolicitud(List.of(estacion(8, "0"))));
        assertEquals(1, datos.inserciones.size());
    }
    @Test void rechazaRutaOperativaVacia() { var datos = new Datos(); datos.estado = "operativa"; rechaza(datos, List.of(), 409); }
}
