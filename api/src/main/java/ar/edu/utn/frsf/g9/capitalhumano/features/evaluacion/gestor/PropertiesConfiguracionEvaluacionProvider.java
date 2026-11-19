package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.gestor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implementación stub de {@link ConfiguracionEvaluacionProvider} respaldada por propiedades
 * configurables en {@code application.yml} hasta que exista el módulo de parámetros del sistema.
 */
@Component
public class PropertiesConfiguracionEvaluacionProvider implements ConfiguracionEvaluacionProvider {

    private final int tiempoMaximoMinutos;
    private final int cantidadPreguntasPorBloque;

    public PropertiesConfiguracionEvaluacionProvider(
            @Value("${capitalhumano.evaluacion.tiempo-maximo-minutos:60}") int tiempoMaximoMinutos,
            @Value("${capitalhumano.evaluacion.cantidad-preguntas-por-bloque:5}") int cantidadPreguntasPorBloque) {
        this.tiempoMaximoMinutos = tiempoMaximoMinutos;
        this.cantidadPreguntasPorBloque = cantidadPreguntasPorBloque;
    }

    @Override
    public int getTiempoMaximoMinutos() {
        return tiempoMaximoMinutos;
    }

    @Override
    public int getCantidadPreguntasPorBloque() {
        return cantidadPreguntasPorBloque;
    }
}
