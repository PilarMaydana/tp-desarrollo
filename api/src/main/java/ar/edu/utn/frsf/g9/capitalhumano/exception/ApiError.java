package ar.edu.utn.frsf.g9.capitalhumano.exception;

import java.time.Instant;
import java.util.List;

/**
 * Representacion estandar de las respuestas de error en la API REST.
 *
 * @param timestamp Marca temporal en la que se produjo el error.
 * @param status Codigo de estado HTTP numerico.
 * @param codigo Codigo identificador del tipo de error para el cliente/frontend.
 * @param mensaje Mensaje explicativo del error.
 * @param path Ruta del recurso solicitado donde se origino el error.
 * @param detalles Lista de errores especificos por campo, utilizado principalmente en validaciones.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String codigo,
        String mensaje,
        String path,
        List<CampoError> detalles
) {

    public ApiError {
        detalles = detalles != null ? List.copyOf(detalles) : List.of();
    }

    /**
     * Detalle de un error especifico en un campo de la solicitud.
     *
     * @param campo Nombre del campo que fallo la validacion.
     * @param mensaje Descripcion del motivo de falla.
     */
    public record CampoError(String campo, String mensaje) {}

    /**
     * Metodo factoria para construir una instancia de {@link ApiError} sin detalles por campo.
     *
     * @param status Codigo de estado HTTP numerico.
     * @param codigo Codigo identificador del error.
     * @param mensaje Mensaje explicativo del error.
     * @param path Ruta del recurso solicitado.
     * @return Nueva instancia de {@link ApiError} con lista de detalles vacia y timestamp actual.
     */
    public static ApiError of(int status, String codigo, String mensaje, String path) {
        return new ApiError(Instant.now(), status, codigo, mensaje, path, List.of());
    }
}
