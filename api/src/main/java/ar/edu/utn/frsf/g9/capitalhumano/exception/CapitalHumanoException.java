package ar.edu.utn.frsf.g9.capitalhumano.exception;

/**
 * Excepcion base abstracta para todas las excepciones de negocio del sistema Capital Humano.
 *
 * <p>Define un codigo identificador de error y desacopla la logica de dominio
 * de cualquier protocolo o detalle de transporte (como HTTP).</p>
 */
public abstract class CapitalHumanoException extends RuntimeException {

    private final String codigo;

    public CapitalHumanoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public CapitalHumanoException(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
