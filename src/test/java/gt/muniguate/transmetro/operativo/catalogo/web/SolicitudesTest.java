package gt.muniguate.transmetro.operativo.catalogo.web;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SolicitudesTest {
    @Test void validaLimitesEducacion() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            for (Integer anio : new Integer[]{1900, 2100, null}) {
                assertTrue(validator.validate(new EducacionPilotoController.EducacionSolicitud(1,"Nivel","Institución",null,anio)).isEmpty());
            }
            for (int anio : new int[]{1899, 2101}) {
                assertFalse(validator.validate(new EducacionPilotoController.EducacionSolicitud(1,"Nivel","Institución",null,anio)).isEmpty());
            }
            assertFalse(validator.validate(new EducacionPilotoController.EducacionSolicitud(null,"","",null,2020)).isEmpty());
        }
    }
    @Test void validaDecimalesYEstacionesAnidadas() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var solicitud = new RutaController.RutaSolicitud(List.of(new RutaController.EstacionRutaSolicitud(0, new BigDecimal("1.0001"))));
            assertFalse(validator.validate(solicitud).isEmpty());
            assertTrue(validator.validate(new RutaController.RutaSolicitud(List.of(new RutaController.EstacionRutaSolicitud(1, new BigDecimal("1.250"))))).isEmpty());
        }
    }
}
