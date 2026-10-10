package gt.muniguate.transmetro.operativo.catalogo.web;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenCsrfController {

    @GetMapping("/api/csrf")
    public TokenCsrfRespuesta obtenerToken(CsrfToken token) {
        return new TokenCsrfRespuesta(token.getHeaderName(), token.getParameterName(), token.getToken());
    }

    public record TokenCsrfRespuesta(String encabezado, String parametro, String valor) {
    }
}
