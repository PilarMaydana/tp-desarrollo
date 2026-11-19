package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.gestor;

/**
 * Proveedor de parámetros globales de configuración para la generación de evaluaciones y cuestionarios.
 */
public interface ConfiguracionEvaluacionProvider {

    /**
     * Obtiene el tiempo máximo permitido (en minutos) para completar un cuestionario.
     *
     * @return tiempo máximo en minutos
     */
    int getTiempoMaximoMinutos();

    /**
     * Obtiene la cantidad de preguntas que componen cada bloque de un cuestionario.
     *
     * @return cantidad de preguntas por bloque
     */
    int getCantidadPreguntasPorBloque();
}
