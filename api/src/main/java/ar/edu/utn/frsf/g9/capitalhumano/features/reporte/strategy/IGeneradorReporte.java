package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.strategy;

/**
 * Interfaz del patrón Strategy para la generación de reportes del sistema
 * (ej. Orden de Mérito en CU 028 o futuros reportes como Reporte Comparativo).
 *
 * <p>Parametrizada con el tipo de DTO de salida {@code T} para preservar tipado fuerte
 * sin perder la extensibilidad del patrón Strategy en {@code GestorReportes}.</p>
 *
 * @param <T> tipo del reporte generado
 */
public interface IGeneradorReporte<T> {

    /**
     * Recopila, clasifica y ordena los datos necesarios para construir el reporte.
     */
    void recopilarDatos();

    /**
     * Genera y devuelve la representación de salida (DTO) del reporte a partir de los datos recopilados.
     *
     * @return DTO con la salida del reporte
     */
    T generarSalida();
}
