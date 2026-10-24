package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;
import gt.muniguate.transmetro.operativo.catalogo.OperacionService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.TransactionStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GestionControllerTest {
    @Test void CP_0007_05_creaOperadorConContrasenaCodificada() {
        var datos=mock(JdbcTemplate.class);
        var usuarios=mock(UsuarioService.class);
        var claves=mock(PasswordEncoder.class);
        when(claves.encode("clave")).thenReturn("hash");
        when(datos.queryForObject(contains("INSERT INTO usuario"),eq(Integer.class),eq("Operador"),eq("operador"),eq("hash"),eq("operador"),eq(1),eq(true))).thenReturn(8);
        var c=new UsuarioController(datos,usuarios,claves,mock(OperacionService.class));
        assertEquals(8,c.crear(new UsuarioController.Solicitud("Operador","operador","clave","operador",1,true)).get("id"));
        verify(usuarios).registrar("usuario",8,"crear");
    }
    @Test void CP_0007_06_consultaBitacoraSinContrasenas() {
        var datos=mock(JdbcTemplate.class);
        when(datos.queryForList(contains("FROM bitacora"))).thenReturn(List.of(Map.of("usuario","admin","accion","guardar","entidad","linea")));
        var c=new UsuarioController(datos,mock(UsuarioService.class),mock(PasswordEncoder.class),mock(OperacionService.class));
        assertEquals("guardar",c.bitacora().getFirst().get("accion"));
    }
    @Test void CP_0004_06_registraYAtiendeEvento() {
        var datos=mock(JdbcTemplate.class);
        var usuarios=mock(UsuarioService.class);
        when(usuarios.actual()).thenReturn(new UsuarioService.Sesion(1,"admin","administrador",null));
        when(datos.queryForObject(contains("INSERT INTO evento_operativo"),eq(Integer.class),eq("Incidencia"),eq("Acceso"),isNull(),eq(1),isNull(),isNull(),eq(1))).thenReturn(4);
        when(datos.update(contains("UPDATE evento_operativo"),eq("cerrado"),eq("cerrado"),eq(1),eq("cerrado"),eq(4))).thenReturn(1);
        var c=new EventoController(datos,usuarios);
        assertEquals(4,c.crear(new EventoController.Solicitud("Incidencia","Acceso",null,1,null,null)).get("id"));
        c.estado(4,new EventoController.Estado("cerrado"));
        verify(usuarios).registrar("evento_operativo",4,"cerrado");
    }
    private UsuarioService administrador() {
        var usuario=mock(UsuarioService.class);
        when(usuario.actual()).thenReturn(new UsuarioService.Sesion(1,"admin","administrador",null));
        return usuario;
    }
    @Test void CP_0006_01_consultaReporteDeLineas() {
        var datos=mock(JdbcTemplate.class);
        when(datos.queryForList(contains("v_distancia_lineas"),any(Object[].class))).thenReturn(List.of(Map.of("codigo","L1","distancia_km",new BigDecimal("1.250"))));
        var resultado=new ReporteController(datos,administrador()).consultar("lineas",null,null,null,null,null);
        assertEquals("L1",resultado.getFirst().get("codigo"));
    }
    @Test void CP_0006_02_aplicaFiltrosDePeriodoYLinea() {
        var datos=mock(JdbcTemplate.class);
        var inicio=LocalDate.of(2026,10,1);
        var fin=LocalDate.of(2026,10,24);
        when(datos.queryForList(contains("AND linea_id=?"),eq(inicio),eq(fin),eq(1))).thenReturn(List.of(Map.of("tipo","Incidencia")));
        assertEquals(1,new ReporteController(datos,administrador()).consultar("eventos",inicio,fin,1,null,null).size());
    }
    @Test void CP_0008_02_cargaReferenciaAutorizada() {
        var datos=mock(JdbcTemplate.class);
        var usuarios=mock(UsuarioService.class);
        var rutas=mock(RutaController.class);
        var tx=mock(TransactionTemplate.class);
        when(tx.execute(any())).thenAnswer(inv -> ((TransactionCallback<?>)inv.getArgument(0)).doInTransaction(mock(TransactionStatus.class)));
        when(datos.queryForObject(contains("INSERT INTO linea"),eq(Integer.class),eq(1),eq("REF"),eq("Referencia"))).thenReturn(3);
        when(datos.queryForObject(contains("INSERT INTO estacion"),eq(Integer.class),eq(1),eq("Estación"),eq("Dirección"),eq(1),eq("REF-A"))).thenReturn(8);
        try(var fabrica=Validation.buildDefaultValidatorFactory()) {
            var c=new ReferenciaController(datos,usuarios,rutas,tx,fabrica.getValidator(),mock(OperacionService.class));
            var ruta=new ReferenciaController.Ruta(1,"REF","Referencia",List.of(new ReferenciaController.Estacion("Estación","Dirección",1,"REF-A",BigDecimal.ZERO)));
            var resultado=c.cargar(new ReferenciaController.Carga("Fuente",true,List.of(ruta)));
            assertEquals(List.of(3),resultado.get("aceptados"));
            assertEquals(List.of(),resultado.get("rechazados"));
            verify(rutas).guardar(eq(3),any());
        }
    }
}
