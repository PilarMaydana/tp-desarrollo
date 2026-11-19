package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.escolaridad;

/**
 * Refined Abstraction del patrón Bridge para el nivel Secundario.
 */
public class Secundaria extends Escolaridad {

    public Secundaria(EstadoEstudio estado) {
        super(estado);
    }

    public Secundaria(EstadoEstudio estado, boolean especificoEstadoExplicito) {
        super(estado, especificoEstadoExplicito);
    }

    @Override
    public String getNivel() {
        return "SECUNDARIO";
    }
}
