package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dao.CandidatoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto.CandidatoResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GeneradorClaveAcceso;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CandidatoClaveDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CompetenciaRequeridaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CrearEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CuestionarioResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.DetallePuestoEvaluacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluacionCreadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PrevisualizacionEvaluacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PrevisualizarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PuestoResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ValidacionCandidatosResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.gestor.ConfiguracionEvaluacionProvider;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.gestor.ExportadorExcelEvaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.ContextoSeguridad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Gestor coordinador del CU 025 "Evaluar a Candidato".
 *
 * <p>Orquesta la búsqueda de candidatos, validación de ausencia de cuestionarios activos/en proceso,
 * selección y validación de puestos/competencias, previsualización de claves, creación atómica de
 * evaluaciones y exportación del listado a Excel {@code .xls}.</p>
 */
@Service
public class GestorCandidatos {

    private static final Logger log = LoggerFactory.getLogger(GestorCandidatos.class);

    private static final List<EstadoCuestionario> ESTADOS_BLOQUEANTES = List.of(
            EstadoCuestionario.ACTIVO,
            EstadoCuestionario.EN_PROCESO
    );

    private final CandidatoDAO candidatoDAO;
    private final PuestoDAO puestoDAO;
    private final CuestionarioDAO cuestionarioDAO;
    private final EvaluacionDAO evaluacionDAO;
    private final GestorCuestionario gestorCuestionario;
    private final GeneradorClaveAcceso generadorClaveAcceso;
    private final ConfiguracionEvaluacionProvider configuracionEvaluacionProvider;
    private final ExportadorExcelEvaluacion exportadorExcelEvaluacion;
    private final ContextoSeguridad contextoSeguridad;

    public GestorCandidatos(CandidatoDAO candidatoDAO,
                            PuestoDAO puestoDAO,
                            CuestionarioDAO cuestionarioDAO,
                            EvaluacionDAO evaluacionDAO,
                            GestorCuestionario gestorCuestionario,
                            GeneradorClaveAcceso generadorClaveAcceso,
                            ConfiguracionEvaluacionProvider configuracionEvaluacionProvider,
                            ExportadorExcelEvaluacion exportadorExcelEvaluacion,
                            ContextoSeguridad contextoSeguridad) {
        this.candidatoDAO = candidatoDAO;
        this.puestoDAO = puestoDAO;
        this.cuestionarioDAO = cuestionarioDAO;
        this.evaluacionDAO = evaluacionDAO;
        this.gestorCuestionario = gestorCuestionario;
        this.generadorClaveAcceso = generadorClaveAcceso;
        this.configuracionEvaluacionProvider = configuracionEvaluacionProvider;
        this.exportadorExcelEvaluacion = exportadorExcelEvaluacion;
        this.contextoSeguridad = contextoSeguridad;
    }

    /**
     * Busca candidatos activos aplicando criterios opcionales "contiene" (case-insensitive)
     * por apellido, nombre y número de candidato (CU 025 - Pasos 2 y 3).
     *
     * @param apellido     filtro opcional por apellido
     * @param nombre       filtro opcional por nombre
     * @param nroCandidato filtro opcional por número de candidato
     * @return lista de candidatos activos coincidentes
     */
    @Transactional(readOnly = true)
    public List<CandidatoResumenDTO> buscarCandidatos(String apellido, String nombre, String nroCandidato) {
        String apellidoNorm = normalizarFiltro(apellido);
        String nombreNorm = normalizarFiltro(nombre);
        String nroCandidatoNorm = normalizarFiltro(nroCandidato);

        return candidatoDAO.buscarActivosConFiltros(apellidoNorm, nombreNorm, nroCandidatoNorm).stream()
                .map(this::aCandidatoResumenDTO)
                .toList();
    }

    /**
     * Valida que los candidatos seleccionados existan, estén activos y no posean cuestionarios
     * en estado {@link EstadoCuestionario#ACTIVO} o {@link EstadoCuestionario#EN_PROCESO}
     * (CU 025 - Pasos 4.A, 6 y 6.A).
     *
     * @param candidatoIds lista de identificadores de candidatos seleccionados
     * @return resumen de validación exitosa
     * @throws RecursoNoEncontradoException si algún candidato no existe o está eliminado
     * @throws ReglaNegocioException        si la lista está vacía o algún candidato tiene cuestionario activo/en proceso
     */
    @Transactional(readOnly = true)
    public ValidacionCandidatosResponseDTO verificarSinCuestionarioActivo(List<Long> candidatoIds) {
        List<Candidato> candidatos = resolverYValidarCandidatos(candidatoIds);
        List<CandidatoResumenDTO> dtos = candidatos.stream()
                .map(this::aCandidatoResumenDTO)
                .toList();
        return new ValidacionCandidatosResponseDTO(true, dtos.size(), dtos);
    }

    /**
     * Lista todos los puestos activos disponibles para evaluar candidatos (CU 025 - Paso 8).
     *
     * @return lista de puestos activos ordenados alfabéticamente
     */
    @Transactional(readOnly = true)
    public List<PuestoResumenDTO> listarPuestos() {
        return puestoDAO.findByEliminadoFalseOrderByNombreAsc().stream()
                .map(p -> new PuestoResumenDTO(p.getId(), p.getCodigo(), p.getNombre(), p.getEmpresa()))
                .toList();
    }

    /**
     * Obtiene el detalle informativo de un puesto con sus competencias y ponderaciones requeridas,
     * validando que todas las competencias sean evaluables (CU 025 - Pasos 9 y 9.A).
     *
     * @param puestoId identificador del puesto seleccionado
     * @return detalle del puesto con competencias y ponderaciones
     * @throws RecursoNoEncontradoException si el puesto no existe o está eliminado
     * @throws ReglaNegocioException        si alguna competencia no posee factores válidos con al menos 2 preguntas activas
     */
    @Transactional(readOnly = true)
    public DetallePuestoEvaluacionDTO obtenerDetallePuesto(Long puestoId) {
        Puesto puesto = obtenerPuestoActivo(puestoId);
        validarCompetenciasEvaluables(puesto);

        List<CompetenciaRequeridaDTO> competencias = puesto.getCaracteristicas().stream()
                .filter(cp -> cp.getCompetencia() != null)
                .sorted(Comparator.comparing(cp -> cp.getCompetencia().getNombre() != null
                        ? cp.getCompetencia().getNombre() : ""))
                .map(this::aCompetenciaRequeridaDTO)
                .toList();

        return new DetallePuestoEvaluacionDTO(
                puesto.getId(),
                puesto.getCodigo(),
                puesto.getNombre(),
                puesto.getEmpresa(),
                competencias
        );
    }

    /**
     * Verifica que todas las competencias asociadas al puesto estén activas y posean al menos
     * un factor activo con mínimo 2 preguntas activas (CU 025 - Flujo 9.A).
     *
     * @param puesto puesto con sus características cargadas
     * @throws ReglaNegocioException con el listado exacto de competencias no evaluables en {@code detalles}
     */
    public void validarCompetenciasEvaluables(Puesto puesto) {
        if (puesto.getCaracteristicas() == null || puesto.getCaracteristicas().isEmpty()) {
            throw new ReglaNegocioException(
                    "El puesto '" + puesto.getNombre() + "' no posee competencias asociadas para evaluar"
            );
        }

        List<String> noEvaluables = gestorCuestionario.obtenerCompetenciasNoEvaluables(puesto);
        if (!noEvaluables.isEmpty()) {
            List<ReglaNegocioException.DetalleError> detalles = noEvaluables.stream()
                    .map(nombre -> new ReglaNegocioException.DetalleError("competencia", nombre))
                    .toList();
            throw new ReglaNegocioException(
                    "Existen competencias del puesto que no poseen factores válidos con al menos 2 preguntas activas para evaluar",
                    detalles
            );
        }
    }

    /**
     * Genera una clave aleatoria de acceso de 8 caracteres alfanuméricos en mayúsculas
     * garantizando al menos una letra y al menos un dígito.
     *
     * @return clave de acceso generada
     */
    public String generarClaveAcceso() {
        return generadorClaveAcceso.generar();
    }

    /**
     * Previsualiza la evaluación generando las claves de ingreso para cada candidato
     * sin persistir ningún dato en base de datos (CU 025 - Paso 11 y soporte para Flujo 12.A).
     *
     * @param request puesto y candidatos seleccionados
     * @return resumen de previsualización con claves generadas
     */
    @Transactional(readOnly = true)
    public PrevisualizacionEvaluacionDTO previsualizarEvaluacion(PrevisualizarEvaluacionRequestDTO request) {
        if (request == null || request.puestoId() == null) {
            throw new ReglaNegocioException("Debe seleccionar una función o puesto");
        }

        Puesto puesto = obtenerPuestoActivo(request.puestoId());
        validarCompetenciasEvaluables(puesto);
        List<Candidato> candidatos = resolverYValidarCandidatos(request.candidatoIds());

        List<CandidatoClaveDTO> candidatosConClave = candidatos.stream()
                .map(c -> new CandidatoClaveDTO(
                        c.getId(),
                        c.getNroCandidato(),
                        c.getApellido(),
                        c.getNombre(),
                        c.getTipoDocumento(),
                        c.getNroDocumento(),
                        generarClaveAcceso()
                ))
                .toList();

        return new PrevisualizacionEvaluacionDTO(
                puesto.getId(),
                puesto.getCodigo(),
                puesto.getNombre(),
                puesto.getEmpresa(),
                configuracionEvaluacionProvider.getTiempoMaximoMinutos(),
                configuracionEvaluacionProvider.getCantidadPreguntasPorBloque(),
                candidatosConClave
        );
    }

    /**
     * Confirma y persiste atómicamente una evaluación junto con sus cuestionarios en estado
     * {@link EstadoCuestionario#ACTIVO}, bloques y preguntas asignadas (CU 025 - Pasos 12 y 13).
     *
     * @param request puesto, candidatos y mapa opcional de claves previsualizadas
     * @return DTO de la evaluación creada y sus cuestionarios generados
     */
    @Transactional
    public EvaluacionCreadaDTO evaluarCandidatos(CrearEvaluacionRequestDTO request) {
        if (request == null || request.puestoId() == null) {
            throw new ReglaNegocioException("Debe seleccionar una función o puesto");
        }

        Long consultorId = contextoSeguridad.getUsuarioActualId();
        log.info("Iniciando creación de evaluación para puesto ID {} por consultor ID {}",
                request.puestoId(), consultorId);

        // Re-validación atómica de las reglas 9.A y 6.A dentro de la transacción
        Puesto puesto = obtenerPuestoActivo(request.puestoId());
        validarCompetenciasEvaluables(puesto);
        List<Candidato> candidatos = resolverYValidarCandidatos(request.candidatoIds());

        int tiempoMaximoMinutos = configuracionEvaluacionProvider.getTiempoMaximoMinutos();
        int cantidadPreguntasPorBloque = configuracionEvaluacionProvider.getCantidadPreguntasPorBloque();
        LocalDateTime ahora = LocalDateTime.now();

        Evaluacion evaluacion = new Evaluacion(
                puesto,
                ahora,
                consultorId,
                tiempoMaximoMinutos,
                cantidadPreguntasPorBloque
        );

        Map<Long, String> clavesSugeridas = request.clavesPorCandidato() != null
                ? request.clavesPorCandidato()
                : Map.of();

        for (Candidato candidato : candidatos) {
            String claveAcceso = resolverClaveAcceso(candidato.getId(), clavesSugeridas);
            List<Pregunta> preguntasSeleccionadas = gestorCuestionario.seleccionarPreguntas(puesto);
            Cuestionario cuestionario = gestorCuestionario.construirCuestionario(
                    evaluacion,
                    candidato,
                    claveAcceso,
                    preguntasSeleccionadas,
                    ahora
            );
            evaluacion.addCuestionario(cuestionario);
        }

        Evaluacion guardada = evaluacionDAO.save(evaluacion);
        log.info("Evaluación ID {} creada exitosamente con {} cuestionarios en estado ACTIVO",
                guardada.getId(), guardada.getCuestionarios().size());

        return aEvaluacionCreadaDTO(guardada);
    }

    /**
     * Genera el archivo Excel {@code .xls} con el listado de candidatos y claves de ingreso
     * correspondiente a una evaluación persistida (CU 025 - Pasos 14 a 17).
     *
     * @param evaluacionId identificador de la evaluación
     * @return nombre de archivo formateado y bytes del archivo {@code .xls}
     * @throws RecursoNoEncontradoException si la evaluación no existe
     */
    @Transactional(readOnly = true)
    public ExportadorExcelEvaluacion.ArchivoExcelEvaluacion exportarEvaluacionExcel(Long evaluacionId) {
        Evaluacion evaluacion = evaluacionDAO.findByIdConCuestionariosYCandidatos(evaluacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evaluacion", evaluacionId));
        return exportadorExcelEvaluacion.exportar(evaluacion);
    }

    private Puesto obtenerPuestoActivo(Long puestoId) {
        return puestoDAO.findByIdActivaConCaracteristicas(puestoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Puesto", puestoId));
    }

    private List<Candidato> resolverYValidarCandidatos(List<Long> candidatoIds) {
        if (candidatoIds == null || candidatoIds.isEmpty()) {
            throw new ReglaNegocioException("Debe seleccionar uno o más candidatos");
        }

        if (candidatoIds.stream().anyMatch(Objects::isNull)) {
            throw new ReglaNegocioException("La lista de candidatos contiene identificadores nulos");
        }

        Set<Long> idsUnicos = new LinkedHashSet<>(candidatoIds);
        List<Candidato> encontrados = candidatoDAO.findByIdInAndEliminadoFalse(idsUnicos);
        Map<Long, Candidato> porId = encontrados.stream()
                .collect(Collectors.toMap(Candidato::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        List<Long> faltantes = idsUnicos.stream()
                .filter(id -> !porId.containsKey(id))
                .toList();
        if (!faltantes.isEmpty()) {
            if (faltantes.size() == 1) {
                throw new RecursoNoEncontradoException("Candidato", faltantes.get(0));
            }
            throw new RecursoNoEncontradoException("No se encontraron los candidatos activos con IDs: " + faltantes);
        }

        List<Cuestionario> bloqueantes = cuestionarioDAO.findActivosByCandidatoIdInAndEstadoIn(
                idsUnicos, ESTADOS_BLOQUEANTES
        );

        if (!bloqueantes.isEmpty()) {
            Map<Long, Cuestionario> bloqueantePorCandidato = new LinkedHashMap<>();
            for (Cuestionario c : bloqueantes) {
                if (c.getCandidato() != null && c.getCandidato().getId() != null) {
                    bloqueantePorCandidato.putIfAbsent(c.getCandidato().getId(), c);
                }
            }

            List<ReglaNegocioException.DetalleError> detalles = new ArrayList<>();
            for (Long id : idsUnicos) {
                Cuestionario cuestionarioBloqueante = bloqueantePorCandidato.get(id);
                if (cuestionarioBloqueante != null) {
                    Candidato cand = cuestionarioBloqueante.getCandidato();
                    String nro = cand.getNroCandidato() != null ? cand.getNroCandidato() : String.valueOf(cand.getId());
                    String descripcion = String.format(
                            "Nro. %s - %s, %s (Estado: %s)",
                            nro,
                            cand.getApellido(),
                            cand.getNombre(),
                            cuestionarioBloqueante.getEstado().name()
                    );
                    detalles.add(new ReglaNegocioException.DetalleError("candidato:" + nro, descripcion));
                }
            }

            throw new ReglaNegocioException(
                    "Existen candidatos que poseen cuestionarios activos o en proceso y no pueden ser evaluados nuevamente",
                    detalles
            );
        }

        return idsUnicos.stream()
                .map(porId::get)
                .toList();
    }

    private String resolverClaveAcceso(Long candidatoId, Map<Long, String> clavesSugeridas) {
        String claveProvista = clavesSugeridas.get(candidatoId);
        if (claveProvista == null || claveProvista.isBlank()) {
            return generarClaveAcceso();
        }
        String normalizada = claveProvista.trim().toUpperCase(Locale.ROOT);
        if (!generadorClaveAcceso.esValida(normalizada)) {
            throw new ReglaNegocioException(
                    "La clave de acceso provista para el candidato ID " + candidatoId
                            + " no cumple el formato requerido (8 caracteres alfanuméricos en mayúsculas con al menos 1 letra y 1 dígito)"
            );
        }
        return normalizada;
    }

    private String normalizarFiltro(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio.toUpperCase(Locale.ROOT);
    }

    private CandidatoResumenDTO aCandidatoResumenDTO(Candidato c) {
        return new CandidatoResumenDTO(
                c.getId(),
                c.getNroCandidato(),
                c.getApellido(),
                c.getNombre(),
                c.getTipoDocumento(),
                c.getNroDocumento(),
                c.getEmail()
        );
    }

    private CompetenciaRequeridaDTO aCompetenciaRequeridaDTO(CaracteristicaPuesto cp) {
        return new CompetenciaRequeridaDTO(
                cp.getCompetencia().getId(),
                cp.getCompetencia().getCodigo(),
                cp.getCompetencia().getNombre(),
                cp.getPonderacionRequerida()
        );
    }

    private EvaluacionCreadaDTO aEvaluacionCreadaDTO(Evaluacion evaluacion) {
        List<CuestionarioResumenDTO> cuestionariosDTO = evaluacion.getCuestionarios().stream()
                .map(c -> new CuestionarioResumenDTO(
                        c.getId(),
                        c.getCandidato().getId(),
                        c.getCandidato().getNroCandidato(),
                        c.getCandidato().getApellido(),
                        c.getCandidato().getNombre(),
                        c.getCandidato().getTipoDocumento(),
                        c.getCandidato().getNroDocumento(),
                        c.getClaveAcceso(),
                        c.getEstado(),
                        c.getBloques().size(),
                        c.getTotalPreguntasAsignadas()
                ))
                .toList();

        return new EvaluacionCreadaDTO(
                evaluacion.getId(),
                evaluacion.getPuesto().getId(),
                evaluacion.getPuesto().getCodigo(),
                evaluacion.getPuesto().getNombre(),
                evaluacion.getPuesto().getEmpresa(),
                evaluacion.getFechaCreacion(),
                evaluacion.getTiempoMaximoMinutos(),
                evaluacion.getCantidadPreguntasPorBloque(),
                cuestionariosDTO
        );
    }
}
