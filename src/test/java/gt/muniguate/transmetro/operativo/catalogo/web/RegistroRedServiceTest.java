package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistroRedServiceTest {
    @Test void CP_0001_02_creaEstacionConLineaOperadorYBus() {
        var datos=mock(JdbcTemplate.class);
        var claves=mock(PasswordEncoder.class);
        var operacion=mock(OperacionService.class);
        var usuarios=mock(UsuarioService.class);
        when(datos.queryForObject(contains("max(orden)"),eq(Integer.class),eq(1))).thenReturn(2);
        when(datos.queryForObject(contains("INSERT INTO estacion"),eq(Integer.class),eq(1),eq("Estación"),eq("Dirección"),eq((short)1),eq("EST-A"))).thenReturn(8);
        when(claves.encode("clave")).thenReturn("hash");
        when(datos.update(contains("UPDATE bus"),eq(1),eq(3))).thenReturn(1);
        var preparada=new OperacionService.Preparacion(1,"L1","Línea",2,2,List.of());
        when(operacion.revisar(1)).thenReturn(preparada);
        var estacion=new RegistroRedService.EstacionInicial("Estación","Dirección",(short)1,"EST-A",new RegistroRedService.Operador("Operador","op.estacion","clave"));
        assertEquals(8,new RegistroRedService(datos,claves,operacion,usuarios).crearEstacion(1,estacion,1,new BigDecimal("1.250"),3));
        verify(datos).update(contains("INSERT INTO usuario"),eq("Operador"),eq("op.estacion"),eq("hash"),eq(8));
        verify(datos).update(contains("INSERT INTO linea_estacion"),eq(1),eq(8),eq(2),eq(new BigDecimal("1.250")));
        verify(operacion).exigirPreparada(preparada);
        verify(usuarios).registrar("estacion",8,"guardar");
    }
}
