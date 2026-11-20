package ar.edu.utn.frsf.g9.capitalhumano.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración del módulo de cuestionarios (CU 026).
 * Se leen desde {@code application.yml} bajo el prefijo {@code capitalhumano.cuestionario}.
 */
@Component
@ConfigurationProperties(prefix = "capitalhumano.cuestionario")
public class CuestionarioProperties {

    private String instrucciones = "Lea atentamente cada pregunta y seleccione la opción que mejor refleje su criterio.";
    private int maxAccesos = 3;
    private int maxHorasEnEstadoActivo = 72;

    public CuestionarioProperties() {
    }

    public CuestionarioProperties(String instrucciones, int maxAccesos, int maxHorasEnEstadoActivo) {
        this.instrucciones = instrucciones;
        this.maxAccesos = maxAccesos;
        this.maxHorasEnEstadoActivo = maxHorasEnEstadoActivo;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(String instrucciones) {
        this.instrucciones = instrucciones;
    }

    public int getMaxAccesos() {
        return maxAccesos;
    }

    public void setMaxAccesos(int maxAccesos) {
        this.maxAccesos = maxAccesos;
    }

    public int getMaxHorasEnEstadoActivo() {
        return maxHorasEnEstadoActivo;
    }

    public void setMaxHorasEnEstadoActivo(int maxHorasEnEstadoActivo) {
        this.maxHorasEnEstadoActivo = maxHorasEnEstadoActivo;
    }
}
