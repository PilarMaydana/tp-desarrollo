package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.strategy;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.ResultadoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.ReporteOrdenMeritoDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fábrica encargada de instanciar estrategias de reporte nuevas por solicitud,
 * evitando singletons con estado mutable compartido entre peticiones concurrentes.
 */
@Component
public class FabricaGeneradorReporte {

    private final CuestionarioDAO cuestionarioDAO;
    private final ResultadoDAO resultadoDAO;

    public FabricaGeneradorReporte(CuestionarioDAO cuestionarioDAO, ResultadoDAO resultadoDAO) {
        this.cuestionarioDAO = cuestionarioDAO;
        this.resultadoDAO = resultadoDAO;
    }

    /**
     * Crea una nueva instancia de {@link ReporteOrdenMerito} parametrizada para una emisión puntual.
     *
     * @param puesto       puesto con características y competencias cargadas
     * @param evaluaciones evaluaciones incluidas en el reporte
     * @param fechaEmision fecha y hora de emisión del reporte
     * @return estrategia lista para ser ejecutada por {@code GestorReportes}
     */
    public IGeneradorReporte<ReporteOrdenMeritoDTO> crearReporteOrdenMerito(
            Puesto puesto,
            List<Evaluacion> evaluaciones,
            LocalDateTime fechaEmision) {
        return new ReporteOrdenMerito(puesto, evaluaciones, fechaEmision, cuestionarioDAO, resultadoDAO);
    }
}
