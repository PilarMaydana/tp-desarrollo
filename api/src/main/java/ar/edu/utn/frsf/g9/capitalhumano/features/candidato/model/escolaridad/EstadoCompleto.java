package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.escolaridad;

/**
 * Concrete Implementor del patrón Bridge que representa un estudio completado.
 */
public class EstadoCompleto implements EstadoEstudio {

    @Override
    public String getDescripcion() {
        return "COMPLETO";
    }

    @Override
    public boolean isCompleto() {
        return true;
    }

    @Override
    public String toString() {
        return getDescripcion();
    }
}
