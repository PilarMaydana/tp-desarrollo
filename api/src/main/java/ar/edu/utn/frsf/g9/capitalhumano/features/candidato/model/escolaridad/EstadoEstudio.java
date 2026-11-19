package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.escolaridad;

/**
 * Interfaz Implementor del patrón Bridge para la Escolaridad.
 * Representa la dimensión del estado de finalización del estudio (Completo, Incompleto).
 */
public interface EstadoEstudio {

    /**
     * Retorna el sufijo o descripción del estado (ej: "COMPLETO", "INCOMPLETO").
     */
    String getDescripcion();

    /**
     * Indica si el nivel de estudio fue completado satisfactoriamente.
     */
    boolean isCompleto();
}
