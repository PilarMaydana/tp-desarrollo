package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.escolaridad;

/**
 * Refined Abstraction del patrón Bridge para el nivel Terciario.
 */
public class Terciaria extends Escolaridad {

    public Terciaria(EstadoEstudio estado) {
        super(estado);
    }

    public Terciaria(EstadoEstudio estado, boolean especificoEstadoExplicito) {
        super(estado, especificoEstadoExplicito);
    }

    @Override
    public String getNivel() {
        return "TERCIARIO";
    }
}
