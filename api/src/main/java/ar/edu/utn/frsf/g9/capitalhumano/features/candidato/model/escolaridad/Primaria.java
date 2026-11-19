package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.escolaridad;

/**
 * Refined Abstraction del patrón Bridge para el nivel Primario.
 */
public class Primaria extends Escolaridad {

    public Primaria(EstadoEstudio estado) {
        super(estado);
    }

    public Primaria(EstadoEstudio estado, boolean especificoEstadoExplicito) {
        super(estado, especificoEstadoExplicito);
    }

    @Override
    public String getNivel() {
        return "PRIMARIO";
    }
}
