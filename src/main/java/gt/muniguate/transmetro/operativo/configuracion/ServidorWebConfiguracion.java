package gt.muniguate.transmetro.operativo.configuracion;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServidorWebConfiguracion {
    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> conectorNio2() {
        // Evita el selector NIO que no puede inicializarse en este entorno Windows/JDK.
        return factory -> factory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
    }
}
