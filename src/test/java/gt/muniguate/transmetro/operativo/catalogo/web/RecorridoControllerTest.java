package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecorridoControllerTest {
    private final JdbcTemplate datos=mock(JdbcTemplate.class);
    private final OperacionService operacion=mock(OperacionService.class);
    private final UsuarioService usuarios=mock(UsuarioService.class);
    private RecorridoController controlador() {
        when(usuarios.actual()).thenReturn(new UsuarioService.Sesion(1,"admin","administrador",null));
        return new RecorridoController(datos,operacion,usuarios);
    }
    @Test void CP_0004_04_iniciaRecorrido() {
        var c=controlador();
        when(operacion.revisar(1)).thenReturn(new OperacionService.Preparacion(1,"L1","Línea",2,3,List.of()));
        when(datos.queryForObject(anyString(),eq(Boolean.class),eq(1),eq(1))).thenReturn(true);
        when(datos.queryForObject(contains("INSERT INTO recorrido"),eq(Integer.class),eq(1),eq(1),eq(1))).thenReturn(8);
        assertEquals(8,c.iniciar(new RecorridoController.Inicio(1,1,1)).get("id"));
        verify(usuarios).registrar("recorrido",8,"iniciar");
    }
    @Test void CP_0004_05_finalizaRecorrido() {
        var c=controlador();
        when(datos.queryForObject(contains("count(*)"),eq(Integer.class),eq(1))).thenReturn(0);
        when(datos.update(contains("UPDATE recorrido"),eq(1))).thenReturn(1);
        c.finalizar(1);
        verify(usuarios).registrar("recorrido",1,"finalizar");
    }
    private RecorridoController prepararVisita(int abordo,int esperando) {
        var c=controlador();
        when(datos.queryForList(contains("SELECT r.linea_id"),eq(1))).thenReturn(List.of(Map.of("linea_id",1,"capacidad_maxima",100)));
        when(datos.queryForObject(contains("count(*)"),eq(Integer.class),eq(1))).thenReturn(0);
        when(datos.queryForList(contains("SELECT le.estacion_id"),eq(1),eq(1))).thenReturn(List.of(Map.of("estacion_id",1,"orden",1)));
        when(datos.queryForObject(contains("INSERT INTO visita_estacion"),eq(Integer.class),any(Object[].class))).thenReturn(9);
        // La respuesta identifica la visita; PostgreSQL calcula los umbrales en las columnas existentes.
        when(datos.queryForObject(contains("INSERT INTO visita_estacion"),eq(Integer.class),eq(1),eq(1),eq(1),any(),any(),eq(abordo),eq(esperando),eq(100),eq(1))).thenReturn(9);
        return c;
    }
    @Test void CP_0005_01_registraVisitaEnOrden() {
        var c=prepararVisita(40,0);
        var llegada=OffsetDateTime.parse("2026-10-01T12:00:00Z");
        assertEquals(9,c.visitar(new RecorridoController.Visita(1,1,llegada,llegada.plusMinutes(1),40,0)).get("id"));
        verify(usuarios).comprobarEstacion(1);
    }
    @Test void CP_0005_02_registraSalida() {
        var c=controlador();
        when(datos.queryForList(contains("SELECT estacion_id"),eq(1))).thenReturn(List.of(Map.of("estacion_id",1)));
        var salida=OffsetDateTime.parse("2026-10-01T12:06:00Z");
        c.salir(1,new RecorridoController.Salida(salida));
        verify(datos).update("UPDATE visita_estacion SET salida=? WHERE id=?",salida,1);
    }
    @Test void CP_0005_03_solicitaAlertaSegunOcupacion() {
        var c=prepararVisita(40,200);
        var llegada=OffsetDateTime.parse("2026-10-01T12:00:00Z");
        c.visitar(new RecorridoController.Visita(1,1,llegada,llegada,40,200));
        verify(datos).update("INSERT INTO alerta(visita_id) SELECT id FROM visita_estacion WHERE id=? AND solicita_refuerzo",9);
    }
    @Test void CP_0005_04_asignaRefuerzo() {
        var c=controlador();
        when(datos.queryForList(contains("SELECT v.estacion_id"),eq(Integer.class),eq(1))).thenReturn(List.of(1));
        when(datos.queryForObject(anyString(),eq(Boolean.class),eq(2),eq(1))).thenReturn(true);
        when(datos.update(contains("UPDATE alerta"),eq(2),eq(1))).thenReturn(1);
        c.atender(1,new RecorridoController.Refuerzo(2));
        verify(usuarios).registrar("alerta",1,"atender");
    }
}
