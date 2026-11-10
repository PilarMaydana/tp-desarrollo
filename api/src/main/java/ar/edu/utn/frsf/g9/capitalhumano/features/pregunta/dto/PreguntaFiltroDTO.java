package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto;

/**
 * Criterios de búsqueda para preguntas (CU 020).
 * Al menos uno de los tres criterios debe estar presente.
 */
public class PreguntaFiltroDTO {

    private String competencia;
    private String factor;
    private String nombre;

    public PreguntaFiltroDTO() {
    }

    public PreguntaFiltroDTO(String competencia, String factor, String nombre) {
        this.competencia = competencia;
        this.factor = factor;
        this.nombre = nombre;
    }

    /**
     * Verifica si al menos uno de los criterios contiene texto para realizar la búsqueda.
     *
     * @return {@code true} si hay al menos un criterio no vacío, {@code false} si todos están vacíos o nulos
     */
    public boolean tieneCriterio() {
        return (competencia != null && !competencia.isBlank())
                || (factor != null && !factor.isBlank())
                || (nombre != null && !nombre.isBlank());
    }

    public String getCompetencia() {
        return competencia;
    }

    public void setCompetencia(String competencia) {
        this.competencia = competencia;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
