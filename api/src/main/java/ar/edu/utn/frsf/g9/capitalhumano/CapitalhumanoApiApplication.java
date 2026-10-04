package ar.edu.utn.frsf.g9.capitalhumano;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapAutoConfiguration;

/**
 * Clase principal de la aplicación Capit@l Humano.
 *
 * <p>Arranca el contenedor Spring Boot y habilita la autoconfiguración.
 * Se excluye {@link EmbeddedLdapAutoConfiguration} porque el LDAP embebido
 * se configurará manualmente al implementar el CU 01.</p>
 */
@SpringBootApplication(exclude = {EmbeddedLdapAutoConfiguration.class})
public class CapitalhumanoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CapitalhumanoApiApplication.class, args);
    }

}
