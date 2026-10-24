package gt.muniguate.transmetro.operativo.catalogo.web;

import gt.muniguate.transmetro.operativo.catalogo.UsuarioService;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitudesTest {
    @Test void registraFormacionDePiloto() {
        var datos = mock(JdbcTemplate.class);
        when(datos.queryForObject(anyString(), eq(Boolean.class), eq(1))).thenReturn(true);
        when(datos.queryForObject(contains("INSERT INTO educacion_piloto"), eq(Integer.class), eq(1), eq("Diversificado"), eq("Instituto"), eq("Bachiller"), eq(2020))).thenReturn(5);
        var resultado = new EducacionPilotoController(datos, mock(UsuarioService.class)).crear(new EducacionPilotoController.EducacionSolicitud(1,"Diversificado","Instituto","Bachiller",2020));
        assertEquals(5, resultado.id());
        assertEquals("Bachiller", resultado.titulo());
    }
}
