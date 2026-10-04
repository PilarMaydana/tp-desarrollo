package ar.edu.utn.frsf.g9.capitalhumano.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI / Swagger.
 *
 * <p>Define los metadatos que aparecen en la documentación interactiva
 * generada por springdoc en {@code /swagger-ui.html}.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Bean de metadatos OpenAPI para la documentación de la API.
     *
     * @return instancia de {@link OpenAPI} con título, versión y descripción
     */
    @Bean
    public OpenAPI capitalHumanoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Capit@l Humano API")
                        .version("0.1.0")
                        .description("API REST del sistema de evaluación de candidatos "
                                + "para la gestión de Capital Humano."));
    }
}
