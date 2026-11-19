package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.escolaridad;

/**
 * Concrete Implementor del patrón Bridge que representa un estudio incompleto.
 */
public class EstadoIncompleto implements EstadoEstudio {

    @Override
    public String getDescripcion() {
        return "INCOMPLETO";
    }

    @Override
    public boolean isCompleto() {
        return false;
    }

    @Override
    public String toString() {
        return getDescripcion();
    }
}
