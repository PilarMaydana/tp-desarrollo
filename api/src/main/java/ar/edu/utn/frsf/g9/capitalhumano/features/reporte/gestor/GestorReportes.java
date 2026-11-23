package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.EvaluacionOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.PuestoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.ReporteOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.strategy.FabricaGeneradorReporte;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.strategy.IGeneradorReporte;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ValidacionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gestor de negocio y contexto del patrón Strategy para la emisión de reportes (CU 028).
 *
 * <p>Permite configurar cualquier estrategia {@link IGeneradorReporte} mediante
 * {@link #setEstrategia(IGeneradorReporte)} y ejecutarla con {@link #emitirReporte()},
 * empleando aislamiento por hilo ({@link ThreadLocal}) para garantizar seguridad ante
 * concurrencia en peticiones simultáneas sin modificar el gestor al incorporar nuevas estrategias.</p>
 */
@Service
public class GestorReportes {

    private static final String MENSAJE_SIN_EVALUACIONES =
            "El puesto seleccionado no posee evaluaciones asociadas.";

    private final PuestoDAO puestoDAO;
    private final EvaluacionDAO evaluacionDAO;
    private final FabricaGeneradorReporte fabricaGeneradorReporte;
    private final Clock clock;

    private final ThreadLocal<IGeneradorReporte<?>> estrategiaActual = new ThreadLocal<>();

    public GestorReportes(PuestoDAO puestoDAO,
                          EvaluacionDAO evaluacionDAO,
                          FabricaGeneradorReporte fabricaGeneradorReporte,
                          Clock clock) {
        this.puestoDAO = puestoDAO;
        this.evaluacionDAO = evaluacionDAO;
        this.fabricaGeneradorReporte = fabricaGeneradorReporte;
        this.clock = clock;
    }

    /**
     * Configura la estrategia concreta de generación de reportes para la ejecución actual.
     *
     * @param estrategia instancia de {@link IGeneradorReporte} a ejecutar
     * @param <T>        tipo de salida de la estrategia
     */
    public <T> void setEstrategia(IGeneradorReporte<T> estrategia) {
        this.estrategiaActual.set(Objects.requireNonNull(estrategia, "La estrategia de reporte no puede ser nula"));
    }

    /**
     * Ejecuta la estrategia previamente configurada ({@code recopilarDatos()} seguido de
     * {@code generarSalida()}) y libera la referencia del hilo actual.
     *
     * @param <T> tipo del reporte generado
     * @return salida producida por la estrategia
     * @throws IllegalStateException si no se configuró ninguna estrategia antes de emitir
     */
    @SuppressWarnings("unchecked")
    public <T> T emitirReporte() {
        IGeneradorReporte<T> estrategia = (IGeneradorReporte<T>) estrategiaActual.get();
        if (estrategia == null) {
            throw new IllegalStateException("No se ha configurado una estrategia de generación de reporte.");
        }
        try {
            estrategia.recopilarDatos();
            return estrategia.generarSalida();
        } finally {
            estrategiaActual.remove();
        }
    }

    /**
     * Busca puestos activos según criterios opcionales «contiene» (case-insensitive)
     * exigiendo al menos un criterio informado (CU 028 - Pasos 2 a 4).
     *
     * @param codigo  fragmento del código de puesto
     * @param nombre  fragmento del nombre de puesto
     * @param empresa fragmento de la empresa
     * @return lista de puestos coincidentes ordenados por nombre con conteo de candidatos y evaluaciones
     * @throws ValidacionException si los tres criterios están vacíos o ausentes (400 VALIDACION)
     */
    @Transactional(readOnly = true)
    public List<PuestoOrdenMeritoDTO> buscarPuestos(String codigo, String nombre, String empresa) {
        String codigoNorm = normalizarCriterio(codigo);
        String nombreNorm = normalizarCriterio(nombre);
        String empresaNorm = normalizarCriterio(empresa);

        if (codigoNorm == null && nombreNorm == null && empresaNorm == null) {
            throw new ValidacionException(
                    "Debe ingresar al menos un criterio de búsqueda (código, nombre o empresa).",
                    List.of(new ValidacionException.DetalleError(
                            "criterios",
                            "Al menos uno de los parámetros codigo, nombre o empresa debe estar informado."
                    ))
            );
        }

        List<Puesto> puestos = puestoDAO.buscarActivosPorCriterios(codigoNorm, nombreNorm, empresaNorm);
        if (puestos.isEmpty()) {
            return List.of();
        }

        List<Long> puestoIds = puestos.stream()
                .map(Puesto::getId)
                .toList();

        List<EvaluacionDAO.ResumenEvaluacionProjection> resumenes =
                evaluacionDAO.findResumenPorPuestoIds(puestoIds, EstadoCuestionario.COMPLETO);

        Map<Long, List<EvaluacionDAO.ResumenEvaluacionProjection>> porPuesto = new LinkedHashMap<>();
        for (EvaluacionDAO.ResumenEvaluacionProjection resumen : resumenes) {
            porPuesto.computeIfAbsent(resumen.getPuestoId(), k -> new ArrayList<>()).add(resumen);
        }

        return puestos.stream()
                .map(puesto -> {
                    List<EvaluacionDAO.ResumenEvaluacionProjection> evals =
                            porPuesto.getOrDefault(puesto.getId(), List.of());
                    long cantidadCandidatos = evals.stream()
                            .mapToLong(e -> e.getCantidadCandidatos() != null ? e.getCantidadCandidatos() : 0L)
                            .sum();
                    long evaluacionesCompletas = evals.stream()
                            .filter(this::esEvaluacionCompleta)
                            .count();
                    long evaluacionesTotal = evals.size();

                    return new PuestoOrdenMeritoDTO(
                            puesto.getId(),
                            puesto.getCodigo(),
                            puesto.getNombre(),
                            puesto.getEmpresa(),
                            cantidadCandidatos,
                            evaluacionesCompletas,
                            evaluacionesTotal
                    );
                })
                .toList();
    }

    /**
     * Lista las evaluaciones asociadas a un puesto (más recientes primero) para que el consultor
     * seleccione cuáles incluir en el reporte (CU 028 - Paso 6 y Flujo 6.A).
     *
     * @param puestoId identificador del puesto seleccionado
     * @return evaluaciones del puesto ordenadas de más reciente a más antigua
     * @throws RecursoNoEncontradoException si el puesto no existe o está eliminado (404)
     * @throws ReglaNegocioException        si el puesto no posee evaluaciones asociadas (Flujo 6.A -> 422)
     */
    @Transactional(readOnly = true)
    public List<EvaluacionOrdenMeritoDTO> listarEvaluacionesPorPuesto(Long puestoId) {
        if (!puestoDAO.existsByIdAndEliminadoFalse(puestoId)) {
            throw new RecursoNoEncontradoException("Puesto", puestoId);
        }

        List<EvaluacionDAO.ResumenEvaluacionProjection> resumenes =
                evaluacionDAO.findResumenPorPuestoIds(List.of(puestoId), EstadoCuestionario.COMPLETO);

        if (resumenes.isEmpty()) {
            throw new ReglaNegocioException(MENSAJE_SIN_EVALUACIONES);
        }

        return resumenes.stream()
                .map(r -> new EvaluacionOrdenMeritoDTO(
                        r.getEvaluacionId(),
                        r.getFechaCreacion(),
                        r.getCantidadCandidatos() != null ? r.getCantidadCandidatos() : 0L,
                        r.getCantidadCompletos() != null ? r.getCantidadCompletos() : 0L
                ))
                .toList();
    }

    /**
     * Emite el reporte de Orden de Mérito para el puesto y las evaluaciones seleccionadas
     * (CU 028 - Paso 7 y Flujo 6.A), delegando la recopilación, clasificación y ordenamiento
     * en la estrategia {@code ReporteOrdenMerito}.
     *
     * @param puestoId      identificador del puesto
     * @param evaluacionIds identificadores de evaluaciones a incluir (ausente o vacío = TODAS las del puesto)
     * @return DTO con el reporte completo de orden de mérito
     * @throws RecursoNoEncontradoException si el puesto no existe o está eliminado (404)
     * @throws ReglaNegocioException        si el puesto no tiene evaluaciones (6.A) o algún ID de evaluación
     *                                      no pertenece al puesto (422)
     */
    @Transactional(readOnly = true)
    public ReporteOrdenMeritoDTO emitirOrdenDeMerito(Long puestoId, List<Long> evaluacionIds) {
        Puesto puesto = puestoDAO.findByIdActivaConCaracteristicas(puestoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Puesto", puestoId));

        List<Evaluacion> evaluacionesDelPuesto = evaluacionDAO.findByPuestoIdOrderByFechaCreacionDescIdDesc(puestoId);
        if (evaluacionesDelPuesto.isEmpty()) {
            throw new ReglaNegocioException(MENSAJE_SIN_EVALUACIONES);
        }

        List<Evaluacion> evaluacionesSeleccionadas = resolverEvaluacionesSeleccionadas(
                puestoId, evaluacionesDelPuesto, evaluacionIds
        );

        LocalDateTime fechaEmision = LocalDateTime.now(clock);
        IGeneradorReporte<ReporteOrdenMeritoDTO> estrategia = fabricaGeneradorReporte.crearReporteOrdenMerito(
                puesto,
                evaluacionesSeleccionadas,
                fechaEmision
        );

        setEstrategia(estrategia);
        return emitirReporte();
    }

    private List<Evaluacion> resolverEvaluacionesSeleccionadas(Long puestoId,
                                                               List<Evaluacion> evaluacionesDelPuesto,
                                                               List<Long> evaluacionIdsSolicitados) {
        if (evaluacionIdsSolicitados == null || evaluacionIdsSolicitados.isEmpty()) {
            return evaluacionesDelPuesto;
        }

        Set<Long> idsSolicitados = evaluacionIdsSolicitados.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (idsSolicitados.isEmpty()) {
            return evaluacionesDelPuesto;
        }

        Set<Long> idsValidosDelPuesto = evaluacionesDelPuesto.stream()
                .map(Evaluacion::getId)
                .collect(Collectors.toSet());

        List<Long> idsAjenos = idsSolicitados.stream()
                .filter(id -> !idsValidosDelPuesto.contains(id))
                .toList();

        if (!idsAjenos.isEmpty()) {
            List<ReglaNegocioException.DetalleError> detalles = idsAjenos.stream()
                    .map(id -> new ReglaNegocioException.DetalleError(
                            "evaluacionIds",
                            "La evaluación con id " + id + " no pertenece al puesto " + puestoId + "."
                    ))
                    .toList();
            throw new ReglaNegocioException(
                    "Una o más evaluaciones seleccionadas no pertenecen al puesto indicado.",
                    detalles
            );
        }

        return evaluacionesDelPuesto.stream()
                .filter(e -> idsSolicitados.contains(e.getId()))
                .toList();
    }

    private boolean esEvaluacionCompleta(EvaluacionDAO.ResumenEvaluacionProjection resumen) {
        long candidatos = resumen.getCantidadCandidatos() != null ? resumen.getCantidadCandidatos() : 0L;
        long completos = resumen.getCantidadCompletos() != null ? resumen.getCantidadCompletos() : 0L;
        return candidatos > 0 && completos == candidatos;
    }

    private String normalizarCriterio(String valor) {
        if (valor == null) {
            return null;
        }
        String recortado = valor.trim();
        return recortado.isEmpty() ? null : recortado.toUpperCase(Locale.ROOT);
    }
}
