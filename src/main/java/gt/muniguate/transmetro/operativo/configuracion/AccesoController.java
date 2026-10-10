package gt.muniguate.transmetro.operativo.configuracion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccesoController {

    @GetMapping("/login")
    public String mostrarInicioSesion() {
        return "forward:/login.html";
    }
}
