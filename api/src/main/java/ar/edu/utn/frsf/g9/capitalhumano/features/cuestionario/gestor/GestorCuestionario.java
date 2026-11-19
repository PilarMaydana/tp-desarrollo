package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor de lógica de negocio responsable de validar suficiencia de factores/preguntas,
 * seleccionar aleatoriamente las preguntas del cuestionario y armar los bloques asignados (CU 025).
 */
@Service
public class GestorCuestionario {

    private static final int PREGUNTAS_POR_FACTOR = 2;

    private final FactorDAO factorDAO;
    private final SecureRandom secureRandom;

    public GestorCuestionario(FactorDAO factorDAO) {
        this.factorDAO = factorDAO;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Identifica las competencias requeridas por el puesto que NO pueden ser evaluadas
     * (Flujo alternativo 9.A del CU 025).
     *
     * <p>Una competencia es evaluable si está activa ({@code eliminado = false}) y posee al menos
     * un factor activo ({@code eliminado = false}) que cumpla {@link Factor#tieneMinimoDosPreguntas()}.</p>
     *
     * @param puesto puesto con sus características y competencias cargadas
     * @return lista con los nombres exactos de todas las competencias no evaluables (vacía si todas son válidas)
     */
    public List<String> obtenerCompetenciasNoEvaluables(Puesto puesto) {
        List<CaracteristicaPuesto> caracteristicas = puesto.getCaracteristicas();
        if (caracteristicas == null || caracteristicas.isEmpty()) {
            return List.of();
        }

        List<Long> competenciaIdsActivas = caracteristicas.stream()
                .map(CaracteristicaPuesto::getCompetencia)
                .filter(c -> c != null && !Boolean.TRUE.equals(c.getEliminado()) && c.getId() != null)
                .map(Competencia::getId)
                .distinct()
                .toList();

        Map<Long, List<Factor>> factoresPorCompetencia = cargarFactoresActivosPorCompetencia(competenciaIdsActivas);

        List<String> noEvaluables = new ArrayList<>();
        for (CaracteristicaPuesto caracteristica : caracteristicas) {
            Competencia competencia = caracteristica.getCompetencia();
            if (competencia == null) {
                continue;
            }
            if (Boolean.TRUE.equals(competencia.getEliminado())) {
                noEvaluables.add(competencia.getNombre());
                continue;
            }

            List<Factor> factoresActivos = factoresPorCompetencia.getOrDefault(competencia.getId(), List.of());
            boolean tieneAlMenosUnFactorValido = factoresActivos.stream()
                    .filter(f -> !Boolean.TRUE.equals(f.getEliminado()))
                    .anyMatch(Factor::tieneMinimoDosPreguntas);

            if (!tieneAlMenosUnFactorValido) {
                noEvaluables.add(competencia.getNombre());
            }
        }

        return noEvaluables;
    }

    /**
     * Selecciona aleatoriamente 2 preguntas activas por cada factor activo (con al menos 2 preguntas activas)
     * perteneciente a las competencias requeridas por el puesto, mezclando el resultado final.
     *
     * @param puesto puesto a evaluar con sus características y competencias cargadas
     * @return lista mezclada de preguntas seleccionadas para conformar un cuestionario
     */
    public List<Pregunta> seleccionarPreguntas(Puesto puesto) {
        List<CaracteristicaPuesto> caracteristicas = puesto.getCaracteristicas();
        if (caracteristicas == null || caracteristicas.isEmpty()) {
            return List.of();
        }

        List<Long> competenciaIds = caracteristicas.stream()
                .map(CaracteristicaPuesto::getCompetencia)
                .filter(c -> c != null && !Boolean.TRUE.equals(c.getEliminado()) && c.getId() != null)
                .map(Competencia::getId)
                .distinct()
                .toList();

        if (competenciaIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Factor>> factoresPorCompetencia = cargarFactoresActivosPorCompetencia(competenciaIds);
        List<Pregunta> seleccionadas = new ArrayList<>();

        for (Long competenciaId : competenciaIds) {
            List<Factor> factores = factoresPorCompetencia.getOrDefault(competenciaId, List.of());
            for (Factor factor : factores) {
                if (Boolean.TRUE.equals(factor.getEliminado()) || !factor.tieneMinimoDosPreguntas()) {
                    continue;
                }
                List<Pregunta> poolFactor = new ArrayList<>(factor.getPreguntasActivas());
                Collections.shuffle(poolFactor, secureRandom);
                seleccionadas.addAll(poolFactor.subList(0, PREGUNTAS_POR_FACTOR));
            }
        }

        Collections.shuffle(seleccionadas, secureRandom);
        return seleccionadas;
    }

    /**
     * Construye un {@link Cuestionario} en estado {@link EstadoCuestionario#ACTIVO} con los parámetros
     * de configuración compartidos de la evaluación y arma sus bloques con las preguntas seleccionadas.
     *
     * @param evaluacion  evaluación padre
     * @param candidato   candidato al que se asigna el cuestionario
     * @param claveAcceso clave aleatoria de acceso de 8 caracteres
     * @param preguntas   preguntas seleccionadas para el cuestionario
     * @param ahora       marca temporal consistente de creación
     * @return cuestionario armado y listo para persistir
     */
    public Cuestionario construirCuestionario(Evaluacion evaluacion,
                                              Candidato candidato,
                                              String claveAcceso,
                                              List<Pregunta> preguntas,
                                              LocalDateTime ahora) {
        Cuestionario cuestionario = new Cuestionario();
        cuestionario.setEvaluacion(evaluacion);
        cuestionario.setPuesto(evaluacion.getPuesto());
        cuestionario.setCandidato(candidato);
        cuestionario.setClaveAcceso(claveAcceso);
        cuestionario.setEstado(EstadoCuestionario.ACTIVO);
        cuestionario.setCantidadAccesos(0);
        cuestionario.setFechaCreacion(ahora);
        cuestionario.setFechaCambioEstado(ahora);
        cuestionario.setFechaInicio(null);
        cuestionario.setFechaFinalizacion(null);
        cuestionario.setTiempoMaximoMinutos(evaluacion.getTiempoMaximoMinutos());
        cuestionario.setCantidadPreguntasPorBloque(evaluacion.getCantidadPreguntasPorBloque());
        cuestionario.setTiempoRestanteSegundos(evaluacion.getTiempoMaximoMinutos() * 60);
        cuestionario.setUltimoBloqueRespondido(null);
        cuestionario.setEliminado(false);

        cuestionario.armarBloques(preguntas);
        return cuestionario;
    }

    private Map<Long, List<Factor>> cargarFactoresActivosPorCompetencia(List<Long> competenciaIds) {
        if (competenciaIds.isEmpty()) {
            return Map.of();
        }
        List<Factor> factores = factorDAO.findActivosConPreguntasByCompetenciaIdIn(competenciaIds);
        Map<Long, List<Factor>> agrupados = new LinkedHashMap<>();
        for (Factor factor : factores) {
            if (factor.getCompetencia() != null && factor.getCompetencia().getId() != null) {
                agrupados.computeIfAbsent(factor.getCompetencia().getId(), k -> new ArrayList<>()).add(factor);
            }
        }
        return agrupados;
    }
}
