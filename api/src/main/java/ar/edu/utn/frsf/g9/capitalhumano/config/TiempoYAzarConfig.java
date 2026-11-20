package ar.edu.utn.frsf.g9.capitalhumano.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Random;

/**
 * Configuración de beans inyectables de tiempo ({@link Clock}) y azar ({@link Random})
 * para permitir pruebas unitarias determinísticas en los gestores de negocio.
 */
@Configuration
public class TiempoYAzarConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public Random random() {
        return new SecureRandom();
    }
}
