package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.escolaridad;

/**
 * Refined Abstraction del patrón Bridge para el nivel Universitario.
 */
public class Universitaria extends Escolaridad {

    public Universitaria(EstadoEstudio estado) {
        super(estado);
    }

    public Universitaria(EstadoEstudio estado, boolean especificoEstadoExplicito) {
        super(estado, especificoEstadoExplicito);
    }

    @Override
    public String getNivel() {
        return "UNIVERSITARIO";
    }
}
