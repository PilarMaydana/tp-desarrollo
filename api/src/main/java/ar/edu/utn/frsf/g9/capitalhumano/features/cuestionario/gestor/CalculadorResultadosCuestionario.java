package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.BloqueCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.PreguntaAsignada;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.RespuestaCandidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Resultado;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.ResultadoCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.ResultadoFactor;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Componente encargado de calcular los puntajes de un {@link Cuestionario} completado
 * según el algoritmo definido en las observaciones del CU 026:
 * <ul>
 *     <li>Por cada factor evaluado: suma de ponderaciones de las opciones elegidas dividido 2 ({@code totalFactor}).</li>
 *     <li>Por cada competencia evaluada: suma de {@code totalFactor} dividido la cantidad de factores evaluados ({@code totalCompetencia}).</li>
 *     <li>Puntaje obtenido global: promedio simple de los {@code totalCompetencia} (provisorio hasta definir en CU de reportes).</li>
 * </ul>
 *
 * <p>Todos los cálculos se realizan con {@link BigDecimal} y se redondean a 2 decimales con {@link RoundingMode#HALF_UP}.</p>
 */
@Component
public class CalculadorResultadosCuestionario {

    private static final int ESCALA = 2;
    private static final BigDecimal DIVISOR_FACTOR = BigDecimal.valueOf(2);

    /**
     * Calcula y construye la jerarquía completa {@link Resultado} -> {@link ResultadoCompetencia}
     * -> {@link ResultadoFactor} a partir de las respuestas registradas en los bloques del cuestionario.
     *
     * @param cuestionario cuestionario con todos sus bloques y respuestas cargados
     * @param puesto       puesto asociado (con sus características cargadas si están disponibles)
     * @param fechaCalculo fecha y hora del cálculo
     * @return instancia de {@link Resultado} lista para persistir en cascada
     * @throws IllegalStateException si faltan respuestas, factor, competencia o ponderaciones para calcular resultados
     */
    public Resultado calcular(Cuestionario cuestionario, Puesto puesto, LocalDateTime fechaCalculo) {
        Map<Long, Factor> factoresPorId = new LinkedHashMap<>();
        Map<Long, BigDecimal> sumaPonderacionesPorFactor = new LinkedHashMap<>();

        List<BloqueCuestionario> bloques = cuestionario.getBloques().stream()
                .sorted(Comparator.comparing(BloqueCuestionario::getNroBloque, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        for (BloqueCuestionario bloque : bloques) {
            List<PreguntaAsignada> preguntasOrdenadas = bloque.getPreguntasAsignadas().stream()
                    .sorted(Comparator.comparing(PreguntaAsignada::getNroOrden, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            for (PreguntaAsignada preguntaAsignada : preguntasOrdenadas) {
                RespuestaCandidato respuesta = preguntaAsignada.getRespuestaCandidato();
                if (respuesta == null || respuesta.getOpcionElegida() == null) {
                    throw new IllegalStateException(
                            "No se pueden calcular los resultados porque la pregunta asignada "
                                    + preguntaAsignada.getId() + " no posee respuesta registrada."
                    );
                }

                Pregunta pregunta = preguntaAsignada.getPregunta();
                Factor factor = pregunta != null ? pregunta.getFactor() : null;
                Competencia competencia = factor != null ? factor.getCompetencia() : null;
                if (factor == null || factor.getId() == null || competencia == null || competencia.getId() == null) {
                    throw new IllegalStateException(
                            "La pregunta asignada " + preguntaAsignada.getId()
                                    + " no posee factor o competencia válidos para calcular el resultado."
                    );
                }

                OpcionRespuesta opcion = respuesta.getOpcionElegida();
                if (opcion.getPonderacion() == null) {
                    throw new IllegalStateException(
                            "La opción de respuesta " + opcion.getId() + " no posee ponderación definida."
                    );
                }

                factoresPorId.putIfAbsent(factor.getId(), factor);
                BigDecimal ponderacion = BigDecimal.valueOf(opcion.getPonderacion());
                sumaPonderacionesPorFactor.merge(factor.getId(), ponderacion, BigDecimal::add);
            }
        }

        if (factoresPorId.isEmpty()) {
            throw new IllegalStateException("El cuestionario no posee respuestas para calcular resultados.");
        }

        Map<Long, CaracteristicaPuesto> caracteristicasPorCompetenciaId = indexarCaracteristicasPuesto(puesto);

        Map<Long, Competencia> competenciasPorId = new LinkedHashMap<>();
        Map<Long, List<ResultadoFactor>> resultadosFactoresPorCompetencia = new LinkedHashMap<>();

        for (Map.Entry<Long, Factor> entry : factoresPorId.entrySet()) {
            Long factorId = entry.getKey();
            Factor factor = entry.getValue();
            BigDecimal sumaFactor = sumaPonderacionesPorFactor.get(factorId);
            BigDecimal totalFactor = sumaFactor.divide(DIVISOR_FACTOR, ESCALA, RoundingMode.HALF_UP);

            ResultadoFactor resultadoFactor = new ResultadoFactor(factor, totalFactor);
            Competencia competencia = factor.getCompetencia();
            competenciasPorId.putIfAbsent(competencia.getId(), competencia);
            resultadosFactoresPorCompetencia
                    .computeIfAbsent(competencia.getId(), k -> new ArrayList<>())
                    .add(resultadoFactor);
        }

        List<ResultadoCompetencia> resultadosCompetencias = new ArrayList<>();
        BigDecimal sumaTotalesCompetencias = BigDecimal.ZERO;

        for (Map.Entry<Long, Competencia> entry : competenciasPorId.entrySet()) {
            Long competenciaId = entry.getKey();
            Competencia competencia = entry.getValue();
            List<ResultadoFactor> factoresDeCompetencia = resultadosFactoresPorCompetencia.get(competenciaId);

            BigDecimal sumaFactores = factoresDeCompetencia.stream()
                    .map(ResultadoFactor::getTotalFactor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCompetencia = sumaFactores.divide(
                    BigDecimal.valueOf(factoresDeCompetencia.size()),
                    ESCALA,
                    RoundingMode.HALF_UP
            );

            CaracteristicaPuesto caracteristica = caracteristicasPorCompetenciaId.get(competenciaId);
            ResultadoCompetencia resultadoCompetencia = new ResultadoCompetencia(
                    competencia,
                    caracteristica,
                    totalCompetencia
            );
            for (ResultadoFactor rf : factoresDeCompetencia) {
                resultadoCompetencia.addResultadoFactor(rf);
            }

            resultadosCompetencias.add(resultadoCompetencia);
            sumaTotalesCompetencias = sumaTotalesCompetencias.add(totalCompetencia);
        }

        // TODO definir con el CU de reportes
        BigDecimal puntajeObtenido = sumaTotalesCompetencias.divide(
                BigDecimal.valueOf(resultadosCompetencias.size()),
                ESCALA,
                RoundingMode.HALF_UP
        );

        Resultado resultado = new Resultado(cuestionario, fechaCalculo, puntajeObtenido);
        for (ResultadoCompetencia rc : resultadosCompetencias) {
            resultado.addResultadoCompetencia(rc);
        }
        return resultado;
    }

    private Map<Long, CaracteristicaPuesto> indexarCaracteristicasPuesto(Puesto puesto) {
        Map<Long, CaracteristicaPuesto> mapa = new LinkedHashMap<>();
        if (puesto == null || puesto.getCaracteristicas() == null) {
            return mapa;
        }
        for (CaracteristicaPuesto cp : puesto.getCaracteristicas()) {
            if (cp != null && cp.getCompetencia() != null && cp.getCompetencia().getId() != null) {
                mapa.putIfAbsent(cp.getCompetencia().getId(), cp);
            }
        }
        return mapa;
    }
}
