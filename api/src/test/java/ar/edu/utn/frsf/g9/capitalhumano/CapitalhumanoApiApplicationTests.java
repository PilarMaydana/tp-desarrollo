package ar.edu.utn.frsf.g9.capitalhumano;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test de carga del contexto de Spring.
 *
 * <p>Deshabilitado porque requiere una base de datos PostgreSQL activa.
 * Se rehabilita cuando exista el docker-compose con la base de datos.</p>
 */
@SpringBootTest
@Disabled("Requiere base de datos PostgreSQL — rehabilitar cuando exista docker-compose")
class CapitalhumanoApiApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring levante sin errores
    }

}
