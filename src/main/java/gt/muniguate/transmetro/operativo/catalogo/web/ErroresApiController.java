package gt.muniguate.transmetro.operativo.catalogo.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ErroresApiController {

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<Map<String, String>> reglaDeNegocio(
            org.springframework.web.server.ResponseStatusException error) {
        return org.springframework.http.ResponseEntity.status(error.getStatusCode())
                .body(Map.of("message", error.getReason() == null ? "No fue posible completar la operación." : error.getReason()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflictoDeDatos() {
        return Map.of("message", "No se pudo guardar el registro porque duplica un dato o usa una referencia no válida.");
    }
}
