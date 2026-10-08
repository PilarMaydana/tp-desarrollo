package ar.edu.utn.frsf.g9.capitalhumano.web;

import ar.edu.utn.frsf.g9.capitalhumano.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.exception.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Manejador global de excepciones para los controladores REST de la aplicacion.
 *
 * <p>Traduce las excepciones de dominio y del framework a respuestas HTTP consistentes
 * utilizando la estructura {@link ApiError}.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleRecursoNoEncontrado(
            RecursoNoEncontradoException ex, HttpServletRequest request) {
        ApiError error = ApiError.of(
                HttpStatus.NOT_FOUND.value(),
                ex.getCodigo(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiError> handleRecursoDuplicado(
            RecursoDuplicadoException ex, HttpServletRequest request) {
        ApiError error = ApiError.of(
                HttpStatus.CONFLICT.value(),
                ex.getCodigo(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> handleReglaNegocio(
            ReglaNegocioException ex, HttpServletRequest request) {
        ApiError error = ApiError.of(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getCodigo(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.CampoError> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ApiError.CampoError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDACION",
                "Error de validacion en los campos enviados",
                request.getRequestURI(),
                detalles
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        ApiError error = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "CUERPO_INVALIDO",
                "El cuerpo de la solicitud es invalido o no pudo ser interpretado",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse errorResponse) {
            int status = errorResponse.getStatusCode().value();
            String mensaje = errorResponse.getBody().getDetail();
            if (mensaje == null || mensaje.isBlank()) {
                mensaje = ex.getMessage();
            }
            ApiError error = ApiError.of(
                    status,
                    "ERROR_SOLICITUD",
                    mensaje,
                    request.getRequestURI()
            );
            return ResponseEntity.status(status).body(error);
        }

        log.error("Error no controlado al procesar la solicitud [{}]: {}",
                request.getRequestURI(), ex.getMessage(), ex);

        ApiError error = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "ERROR_INTERNO",
                "Ocurrio un error inesperado",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
