package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.config.CuestionarioProperties;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.ResultadoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.BloqueCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioIniciadoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.InstruccionesCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.OpcionPreguntaCandidatoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.PreguntaAsignadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.ResponderBloqueRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.ResponderBloqueResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.RespuestaItemDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.BloqueCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.PreguntaAsignada;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.RespuestaCandidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Resultado;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ValidacionException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.ContextoSeguridad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Gestor de lógica de negocio para {@link Cuestionario}.
 *
 * <p>Cubre tanto el soporte de generación de evaluaciones (CU 025: validación de suficiencia,
 * selección aleatoria de preguntas y armado de bloques) como la realización de cuestionarios
 * por parte de los candidatos (CU 026 Parte 1: instrucciones, validación de disponibilidad,
 * inicio y reingreso).</p>
 */
@Service
public class GestorCuestionario {

    private static final Logger log = LoggerFactory.getLogger(GestorCuestionario.class);
    private static final int PREGUNTAS_POR_FACTOR = 2;

    private final FactorDAO factorDAO;
    private final CuestionarioDAO cuestionarioDAO;
    private final PuestoDAO puestoDAO;
    private final ResultadoDAO resultadoDAO;
    private final CalculadorResultadosCuestionario calculadorResultados;
    private final ContextoSeguridad contextoSeguridad;
    private final CuestionarioProperties properties;
    private final Clock clock;
    private final Random random;

    @Autowired
    public GestorCuestionario(FactorDAO factorDAO,
                              CuestionarioDAO cuestionarioDAO,
                              PuestoDAO puestoDAO,
                              ResultadoDAO resultadoDAO,
                              CalculadorResultadosCuestionario calculadorResultados,
                              ContextoSeguridad contextoSeguridad,
                              CuestionarioProperties properties,
                              Clock clock,
                              Random random) {
        this.factorDAO = factorDAO;
        this.cuestionarioDAO = cuestionarioDAO;
        this.puestoDAO = puestoDAO;
        this.resultadoDAO = resultadoDAO;
        this.calculadorResultados = calculadorResultados != null
                ? calculadorResultados
                : new CalculadorResultadosCuestionario();
        this.contextoSeguridad = contextoSeguridad;
        this.properties = properties;
        this.clock = clock;
        this.random = random;
    }

    /**
     * Constructor de compatibilidad para pruebas existentes de CU 026 Parte 1.
     */
    public GestorCuestionario(FactorDAO factorDAO,
                              CuestionarioDAO cuestionarioDAO,
                              PuestoDAO puestoDAO,
                              ContextoSeguridad contextoSeguridad,
                              CuestionarioProperties properties,
                              Clock clock,
                              Random random) {
        this(factorDAO, cuestionarioDAO, puestoDAO, null, new CalculadorResultadosCuestionario(),
                contextoSeguridad, properties, clock, random);
    }

    /**
     * Constructor auxiliar utilizado en pruebas unitarias enfocadas únicamente en los métodos de CU 025.
     *
     * @param factorDAO DAO de factores
     */
    public GestorCuestionario(FactorDAO factorDAO) {
        this(factorDAO, null, null, null, new CalculadorResultadosCuestionario(),
                null, new CuestionarioProperties(), Clock.systemDefaultZone(), new SecureRandom());
    }

    // =========================================================================
    // CU 026 (Parte 1): Instrucciones, disponibilidad, inicio y reingreso
    // =========================================================================

    /**
     * Obtiene las instrucciones y datos iniciales de un cuestionario del candidato actual
     * (CU 026 - Paso 1).
     *
     * <p>Aplica la validación de disponibilidad (persistiendo eventuales vencimientos 1.A / 1.B)
     * pero NO verifica ni incrementa la cantidad de accesos.</p>
     *
     * @param cuestionarioId identificador del cuestionario
     * @return DTO con las instrucciones y metadatos básicos del cuestionario
     * @throws RecursoNoEncontradoException si el cuestionario no existe o no pertenece al candidato actual
     * @throws ReglaNegocioException        si el cuestionario ya no está disponible o venció (1.A / 1.B)
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public InstruccionesCuestionarioDTO obtenerInstrucciones(Long cuestionarioId) {
        Cuestionario cuestionario = obtenerCuestionarioDelCandidatoActual(cuestionarioId);
        LocalDateTime ahora = LocalDateTime.now(clock);

        validarDisponibilidad(cuestionario, false, ahora);

        Puesto puesto = resolverPuesto(cuestionario);
        String nombrePuesto = puesto != null ? puesto.getNombre() : null;
        Integer tiempoMaximoMinutos = resolverTiempoMaximoMinutos(cuestionario);

        return new InstruccionesCuestionarioDTO(
                cuestionario.getId(),
                cuestionario.getEstado(),
                nombrePuesto,
                tiempoMaximoMinutos,
                properties.getInstrucciones()
        );
    }

    /**
     * Inicia un cuestionario en estado {@link EstadoCuestionario#ACTIVO} o reingresa a uno
     * en estado {@link EstadoCuestionario#EN_PROCESO} (CU 026 - Pasos 2 a 4 y Flujo 1.C).
     *
     * <p>Si el cuestionario ya tiene bloques generados (creados por CU 025), no sortea ni regenera
     * las preguntas asignadas; únicamente registra {@code fechaInicio}, pasa a {@code EN_PROCESO}
     * e incrementa {@code cantidadAccesos}. Si llegara en estado {@code ACTIVO} sin bloques
     * generados, aplica el sorteo existente de 2 preguntas por factor válido y arma los bloques.</p>
     *
     * @param cuestionarioId identificador del cuestionario
     * @return DTO con el estado, tiempo restante, total de bloques y el primer bloque pendiente
     * @throws RecursoNoEncontradoException si el cuestionario no existe o no pertenece al candidato actual
     * @throws ReglaNegocioException        si el cuestionario no está disponible, venció (1.A / 1.B),
     *                                      superó el límite de accesos o no tiene factores válidos
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public CuestionarioIniciadoDTO iniciarCuestionario(Long cuestionarioId) {
        Cuestionario cuestionario = obtenerCuestionarioDelCandidatoActual(cuestionarioId);
        LocalDateTime ahora = LocalDateTime.now(clock);

        validarDisponibilidad(cuestionario, true, ahora);

        int tiempoMaximoMinutos = resolverTiempoMaximoMinutos(cuestionario);
        int accesosActuales = cuestionario.getCantidadAccesos() != null ? cuestionario.getCantidadAccesos() : 0;

        if (cuestionario.getEstado() == EstadoCuestionario.ACTIVO) {
            if (cuestionario.getBloques() == null || cuestionario.getBloques().isEmpty()) {
                armarBloquesSiFaltan(cuestionario);
            }
            cuestionario.setCantidadAccesos(accesosActuales + 1);
            cuestionario.setFechaInicio(ahora);
            cuestionario.setFechaUltimoIngreso(ahora);
            cuestionario.setFechaCambioEstado(ahora);
            cuestionario.setEstado(EstadoCuestionario.EN_PROCESO);
            cuestionario.setTiempoRestanteSegundos(tiempoMaximoMinutos * 60);

            Cuestionario guardado = cuestionarioDAO.saveAndFlush(cuestionario);
            if (guardado != null) {
                cuestionario = guardado;
            }
            log.info("Cuestionario ID {} iniciado por candidato ID {} (acceso #{})",
                    cuestionario.getId(), contextoSeguridad.obtenerCandidatoActual(), cuestionario.getCantidadAccesos());
        } else {
            // Reingreso en estado EN_PROCESO (Flujo 1.C): NO regenera bloques ni modifica fechaInicio
            cuestionario.setCantidadAccesos(accesosActuales + 1);
            cuestionario.setFechaUltimoIngreso(ahora);
            long segundosRestantes = calcularSegundosRestantes(cuestionario.getFechaInicio(), tiempoMaximoMinutos, ahora);
            cuestionario.setTiempoRestanteSegundos((int) segundosRestantes);

            Cuestionario guardado = cuestionarioDAO.save(cuestionario);
            if (guardado != null) {
                cuestionario = guardado;
            }
            log.info("Reingreso al cuestionario ID {} en proceso por candidato ID {} (acceso #{})",
                    cuestionario.getId(), contextoSeguridad.obtenerCandidatoActual(), cuestionario.getCantidadAccesos());
        }

        long segundosRestantes = calcularSegundosRestantes(cuestionario.getFechaInicio(), tiempoMaximoMinutos, ahora);

        List<BloqueCuestionario> bloques = cuestionario.getBloques().stream()
                .sorted(Comparator.comparing(BloqueCuestionario::getNroBloque, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        BloqueCuestionario bloqueActual = bloques.stream()
                .filter(b -> !Boolean.TRUE.equals(b.getRespondido()))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "El cuestionario no posee bloques pendientes de responder."
                ));

        return new CuestionarioIniciadoDTO(
                cuestionario.getId(),
                cuestionario.getEstado(),
                cuestionario.getFechaInicio(),
                segundosRestantes,
                bloques.size(),
                aBloqueDTO(bloqueActual)
        );
    }

    /**
     * Valida la disponibilidad del cuestionario según las precondiciones y flujos alternativos
     * 1.A y 1.B del CU 026, evaluando estrictamente en el orden definido:
     * <ol>
     *     <li>Estados finales ({@code COMPLETO}, {@code INCOMPLETO}, {@code SIN_CONTESTAR}) -> 422 sin cambiar estado.</li>
     *     <li>{@code ACTIVO} con {@code ahora - fechaCreacion > maxHorasEnEstadoActivo} (1.B) -> pasa a {@code SIN_CONTESTAR}, persiste y lanza 422.</li>
     *     <li>{@code EN_PROCESO} con {@code ahora - fechaInicio > tiempoMaximoMinutos} (1.A) -> pasa a {@code INCOMPLETO}, persiste y lanza 422.</li>
     *     <li>{@code cantidadAccesos >= maxAccesos} (solo en {@code iniciar}) -> pasa a {@code INCOMPLETO}, persiste y lanza 422.</li>
     * </ol>
     *
     * @param cuestionario           cuestionario a validar
     * @param verificarLimiteAccesos {@code true} para validar la regla de accesos máximos (en {@code iniciar})
     * @param ahora                  instante actual provisto por {@link Clock}
     * @throws ReglaNegocioException si el cuestionario no está disponible
     */
    void validarDisponibilidad(Cuestionario cuestionario, boolean verificarLimiteAccesos, LocalDateTime ahora) {
        EstadoCuestionario estado = cuestionario.getEstado();

        // 1. Estados finales: no cambia el estado
        if (estado == EstadoCuestionario.COMPLETO) {
            throw new ReglaNegocioException(
                    "El cuestionario ya fue completado y no se encuentra disponible."
            );
        }
        if (estado == EstadoCuestionario.INCOMPLETO) {
            throw new ReglaNegocioException(
                    "El cuestionario se encuentra en estado incompleto y ya no está disponible."
            );
        }
        if (estado == EstadoCuestionario.SIN_CONTESTAR) {
            throw new ReglaNegocioException(
                    "El cuestionario se encuentra en estado sin contestar y ya no está disponible."
            );
        }

        // 2. Flujo 1.B: ACTIVO vencido -> pasa a SIN_CONTESTAR y persiste
        if (estado == EstadoCuestionario.ACTIVO && cuestionario.getFechaCreacion() != null) {
            Duration tiempoEnActivo = Duration.between(cuestionario.getFechaCreacion(), ahora);
            Duration limiteActivo = Duration.ofHours(properties.getMaxHorasEnEstadoActivo());
            if (tiempoEnActivo.compareTo(limiteActivo) > 0) {
                cuestionario.setEstado(EstadoCuestionario.SIN_CONTESTAR);
                cuestionario.setFechaCambioEstado(ahora);
                cuestionarioDAO.save(cuestionario);
                log.info("Cuestionario ID {} vencido en estado ACTIVO (Flujo 1.B): pasa a SIN_CONTESTAR",
                        cuestionario.getId());
                throw new ReglaNegocioException(
                        "Se superó el tiempo máximo permitido en estado activo ("
                                + properties.getMaxHorasEnEstadoActivo()
                                + " horas). El cuestionario pasó a estado Sin Contestar."
                );
            }
        }

        // 3. Flujo 1.A: EN_PROCESO con tiempo máximo de llenado superado -> pasa a INCOMPLETO y persiste
        if (estado == EstadoCuestionario.EN_PROCESO && cuestionario.getFechaInicio() != null) {
            int tiempoMaximoMinutos = resolverTiempoMaximoMinutos(cuestionario);
            Duration tiempoTranscurrido = Duration.between(cuestionario.getFechaInicio(), ahora);
            Duration limiteLlenado = Duration.ofMinutes(tiempoMaximoMinutos);
            if (tiempoTranscurrido.compareTo(limiteLlenado) > 0) {
                cuestionario.setEstado(EstadoCuestionario.INCOMPLETO);
                cuestionario.setFechaCambioEstado(ahora);
                cuestionario.setTiempoRestanteSegundos(0);
                cuestionarioDAO.save(cuestionario);
                log.info("Cuestionario ID {} superó el tiempo máximo de llenado (Flujo 1.A): pasa a INCOMPLETO",
                        cuestionario.getId());
                throw new ReglaNegocioException(
                        "Se superó el tiempo máximo permitido para completar el cuestionario ("
                                + tiempoMaximoMinutos
                                + " minutos). El cuestionario pasó a estado Incompleto."
                );
            }
        }

        // 4. Precondición límite de accesos (solo al iniciar/reingresar) -> pasa a INCOMPLETO y persiste
        if (verificarLimiteAccesos) {
            int accesosActuales = cuestionario.getCantidadAccesos() != null ? cuestionario.getCantidadAccesos() : 0;
            if (accesosActuales >= properties.getMaxAccesos()) {
                cuestionario.setEstado(EstadoCuestionario.INCOMPLETO);
                cuestionario.setFechaCambioEstado(ahora);
                cuestionarioDAO.save(cuestionario);
                log.info("Cuestionario ID {} superó el límite de accesos ({}/{}): pasa a INCOMPLETO",
                        cuestionario.getId(), accesosActuales, properties.getMaxAccesos());
                throw new ReglaNegocioException(
                        "Se alcanzó la cantidad máxima de accesos permitidos ("
                                + properties.getMaxAccesos()
                                + "). El cuestionario pasó a estado Incompleto."
                );
            }
        }
    }

    // =========================================================================
    // CU 026 (Parte 2): Responder bloques, avance y cálculo de resultados
    // =========================================================================

    /**
     * Registra las respuestas del candidato para el bloque actual del cuestionario, valida la
     * completitud del bloque (Flujo 6.A), avanza al siguiente bloque si corresponde (Flujo 8.A)
     * o calcula resultados y cierra el cuestionario en estado {@link EstadoCuestionario#COMPLETO}
     * si era el último bloque (CU 026 - Pasos 5 a 8).
     *
     * <p>La transacción aplica {@code noRollbackFor = ReglaNegocioException.class} para que la
     * transición a {@code INCOMPLETO} por tiempo agotado (Flujo 1.A) quede persistida; las demás
     * validaciones no mutan entidades antes de lanzar excepciones, garantizando atomicidad en el
     * guardado del bloque y el cierre.</p>
     *
     * @param cuestionarioId identificador del cuestionario
     * @param numeroBloque   número secuencial del bloque a responder (1..N)
     * @param request        respuestas enviadas por el candidato
     * @return DTO indicando avance al siguiente bloque o finalización exitosa (sin puntajes)
     * @throws RecursoNoEncontradoException si el cuestionario no existe/es ajeno o el bloque no existe
     * @throws ReglaNegocioException        si venció el tiempo, no está EN_PROCESO, el bloque no es el actual
     *                                      o ya fue respondido, o pregunta/opción no corresponde
     * @throws ValidacionException          si faltan preguntas por responder en el bloque (Flujo 6.A -> 400)
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public ResponderBloqueResponseDTO responderBloque(Long cuestionarioId,
                                                      Integer numeroBloque,
                                                      ResponderBloqueRequestDTO request) {
        Cuestionario cuestionario = obtenerCuestionarioDelCandidatoActual(cuestionarioId);
        LocalDateTime ahora = LocalDateTime.now(clock);

        // 1. Validación de disponibilidad (reutiliza Parte 1, sin incrementar ni validar accesos)
        validarDisponibilidad(cuestionario, false, ahora);

        // 2. El cuestionario debe estar EN_PROCESO
        if (cuestionario.getEstado() != EstadoCuestionario.EN_PROCESO) {
            throw new ReglaNegocioException(
                    "El cuestionario debe encontrarse en estado EN_PROCESO para responder bloques (estado actual: "
                            + cuestionario.getEstado() + ")."
            );
        }

        // 3. El bloque debe existir (404) y ser el bloque actual (primero no completado -> 422 si ya completado o no es actual)
        List<BloqueCuestionario> bloquesOrdenados = cuestionario.getBloques().stream()
                .sorted(Comparator.comparing(BloqueCuestionario::getNroBloque, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        BloqueCuestionario bloqueSolicitado = bloquesOrdenados.stream()
                .filter(b -> Objects.equals(b.getNroBloque(), numeroBloque))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El bloque " + numeroBloque + " del cuestionario " + cuestionarioId + " no existe."
                ));

        if (Boolean.TRUE.equals(bloqueSolicitado.getRespondido())) {
            throw new ReglaNegocioException(
                    "El bloque " + numeroBloque + " ya fue respondido anteriormente."
            );
        }

        BloqueCuestionario bloqueActual = bloquesOrdenados.stream()
                .filter(b -> !Boolean.TRUE.equals(b.getRespondido()))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "El cuestionario no posee bloques pendientes de responder."
                ));

        if (!Objects.equals(bloqueActual.getNroBloque(), numeroBloque)) {
            throw new ReglaNegocioException(
                    "El bloque " + numeroBloque + " no es el bloque actual pendiente de responder (bloque actual: "
                            + bloqueActual.getNroBloque() + ")."
            );
        }

        // 4. Validar pertenencia de preguntaAsignadaId al bloque y de opcionId a la pregunta
        List<PreguntaAsignada> preguntasDelBloque = bloqueSolicitado.getPreguntasAsignadas().stream()
                .sorted(Comparator.comparing(PreguntaAsignada::getNroOrden, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        Map<Long, PreguntaAsignada> preguntasPorId = new LinkedHashMap<>();
        for (PreguntaAsignada pa : preguntasDelBloque) {
            preguntasPorId.put(pa.getId(), pa);
        }

        List<RespuestaItemDTO> itemsEnviados = (request != null && request.respuestas() != null)
                ? request.respuestas()
                : List.of();

        Set<Long> preguntasProcesadas = new HashSet<>();
        Map<Long, OpcionRespuesta> opcionesElegidasPorPreguntaId = new LinkedHashMap<>();

        for (RespuestaItemDTO item : itemsEnviados) {
            if (item == null || item.preguntaAsignadaId() == null) {
                throw new ReglaNegocioException("Cada respuesta debe indicar una preguntaAsignadaId válida.");
            }
            Long preguntaAsignadaId = item.preguntaAsignadaId();
            if (!preguntasProcesadas.add(preguntaAsignadaId)) {
                throw new ReglaNegocioException(
                        "La pregunta asignada " + preguntaAsignadaId + " fue enviada más de una vez."
                );
            }

            PreguntaAsignada preguntaAsignada = preguntasPorId.get(preguntaAsignadaId);
            if (preguntaAsignada == null) {
                throw new ReglaNegocioException(
                        "La pregunta asignada " + preguntaAsignadaId + " no pertenece al bloque " + numeroBloque + "."
                );
            }

            if (item.opcionId() != null) {
                OpcionRespuesta opcionElegida = buscarOpcionDePregunta(preguntaAsignada, item.opcionId())
                        .orElseThrow(() -> new ReglaNegocioException(
                                "La opción de respuesta " + item.opcionId()
                                        + " no pertenece a la pregunta asignada " + preguntaAsignadaId + "."
                        ));
                opcionesElegidasPorPreguntaId.put(preguntaAsignadaId, opcionElegida);
            }
        }

        // 5. Flujo 6.A: verificar que todas las preguntas del bloque hayan sido respondidas
        List<ValidacionException.DetalleError> faltantes = new ArrayList<>();
        for (PreguntaAsignada pa : preguntasDelBloque) {
            if (!opcionesElegidasPorPreguntaId.containsKey(pa.getId())) {
                faltantes.add(new ValidacionException.DetalleError(
                        String.valueOf(pa.getId()),
                        "Debe responder la pregunta N.° " + pa.getNroOrden()
                                + " (preguntaAsignadaId: " + pa.getId() + ")."
                ));
            }
        }

        if (!faltantes.isEmpty()) {
            throw new ValidacionException(
                    "Debe responder todas las preguntas del bloque antes de continuar.",
                    faltantes
            );
        }

        // 6. Paso 7: guardar respuestas del bloque y marcar bloque como completado
        for (PreguntaAsignada pa : preguntasDelBloque) {
            OpcionRespuesta opcionElegida = opcionesElegidasPorPreguntaId.get(pa.getId());
            if (pa.getRespuestaCandidato() != null) {
                pa.getRespuestaCandidato().setOpcionElegida(opcionElegida);
                pa.getRespuestaCandidato().setFechaRespuesta(ahora);
            } else {
                pa.setRespuestaCandidato(new RespuestaCandidato(pa, opcionElegida, ahora));
            }
        }
        bloqueSolicitado.setRespondido(Boolean.TRUE);
        bloqueSolicitado.setFechaRespuesta(ahora);
        cuestionario.setUltimoBloqueRespondido(bloqueSolicitado.getNroBloque());

        // 7. Verificar si quedan más bloques (Flujo 8.A) o es el último bloque (Paso 8)
        Optional<BloqueCuestionario> bloqueSiguienteOpt = bloquesOrdenados.stream()
                .filter(b -> !Boolean.TRUE.equals(b.getRespondido()))
                .findFirst();

        if (bloqueSiguienteOpt.isPresent()) {
            int tiempoMaximoMinutos = resolverTiempoMaximoMinutos(cuestionario);
            long segundosRestantes = calcularSegundosRestantes(cuestionario.getFechaInicio(), tiempoMaximoMinutos, ahora);
            cuestionario.setTiempoRestanteSegundos((int) segundosRestantes);
            cuestionarioDAO.save(cuestionario);

            BloqueCuestionario siguiente = bloqueSiguienteOpt.get();
            log.info("Bloque {} del cuestionario ID {} guardado; avanza al bloque {}",
                    numeroBloque, cuestionario.getId(), siguiente.getNroBloque());
            return ResponderBloqueResponseDTO.siguienteBloque(aBloqueDTO(siguiente), segundosRestantes);
        }

        // Paso 8: último bloque -> calcular y guardar resultados, cerrar cuestionario en estado COMPLETO
        Puesto puestoConCaracteristicas = resolverPuestoConCaracteristicas(cuestionario);
        Resultado resultado = calculadorResultados.calcular(cuestionario, puestoConCaracteristicas, ahora);

        cuestionario.setEstado(EstadoCuestionario.COMPLETO);
        cuestionario.setFechaFinalizacion(ahora);
        cuestionario.setFechaCambioEstado(ahora);
        cuestionario.setTiempoRestanteSegundos(0);

        cuestionarioDAO.save(cuestionario);
        if (resultadoDAO != null) {
            resultadoDAO.save(resultado);
        }

        log.info("Cuestionario ID {} completado con éxito por candidato ID {}; resultado persistido.",
                cuestionario.getId(), contextoSeguridad.obtenerCandidatoActual());
        return ResponderBloqueResponseDTO.cuestionarioCompletado();
    }

    // =========================================================================
    // CU 025: Validación de competencias, sorteo de preguntas y armado
    // =========================================================================

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
                Collections.shuffle(poolFactor, random);
                seleccionadas.addAll(poolFactor.subList(0, PREGUNTAS_POR_FACTOR));
            }
        }

        Collections.shuffle(seleccionadas, random);
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
        cuestionario.setFechaUltimoIngreso(null);
        cuestionario.setFechaFinalizacion(null);
        cuestionario.setTiempoMaximoMinutos(evaluacion.getTiempoMaximoMinutos());
        cuestionario.setCantidadPreguntasPorBloque(evaluacion.getCantidadPreguntasPorBloque());
        cuestionario.setTiempoRestanteSegundos(evaluacion.getTiempoMaximoMinutos() * 60);
        cuestionario.setUltimoBloqueRespondido(null);
        cuestionario.setEliminado(false);

        cuestionario.armarBloques(preguntas);
        return cuestionario;
    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    private Cuestionario obtenerCuestionarioDelCandidatoActual(Long cuestionarioId) {
        Long candidatoActualId = contextoSeguridad.obtenerCandidatoActual();
        return cuestionarioDAO.findByIdAndCandidatoIdConPuesto(cuestionarioId, candidatoActualId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuestionario", cuestionarioId));
    }

    private void armarBloquesSiFaltan(Cuestionario cuestionario) {
        Puesto puesto = resolverPuesto(cuestionario);
        if (puesto == null || puesto.getId() == null) {
            throw new ReglaNegocioException(
                    "El cuestionario no tiene un puesto asociado con competencias evaluables."
            );
        }

        Puesto puestoConCaracteristicas = puesto;
        if ((puesto.getCaracteristicas() == null || puesto.getCaracteristicas().isEmpty()) && puestoDAO != null) {
            puestoConCaracteristicas = puestoDAO.findByIdActivaConCaracteristicas(puesto.getId())
                    .orElse(puesto);
        }

        List<Pregunta> seleccionadas = seleccionarPreguntas(puestoConCaracteristicas);
        if (seleccionadas.isEmpty()) {
            throw new ReglaNegocioException(
                    "No existen factores válidos con al menos dos preguntas activas para armar el cuestionario."
            );
        }

        Integer preguntasPorBloque = cuestionario.getCantidadPreguntasPorBloque();
        if (preguntasPorBloque == null && cuestionario.getEvaluacion() != null) {
            preguntasPorBloque = cuestionario.getEvaluacion().getCantidadPreguntasPorBloque();
            cuestionario.setCantidadPreguntasPorBloque(preguntasPorBloque);
        }
        if (preguntasPorBloque == null || preguntasPorBloque <= 0) {
            throw new ReglaNegocioException(
                    "La cantidad de preguntas por bloque del cuestionario debe ser mayor a cero."
            );
        }

        cuestionario.armarBloques(seleccionadas);
    }

    private Puesto resolverPuesto(Cuestionario cuestionario) {
        if (cuestionario.getPuesto() != null) {
            return cuestionario.getPuesto();
        }
        return cuestionario.getEvaluacion() != null ? cuestionario.getEvaluacion().getPuesto() : null;
    }

    private Puesto resolverPuestoConCaracteristicas(Cuestionario cuestionario) {
        Puesto puesto = resolverPuesto(cuestionario);
        if (puesto == null || puesto.getId() == null) {
            return puesto;
        }
        if ((puesto.getCaracteristicas() == null || puesto.getCaracteristicas().isEmpty()) && puestoDAO != null) {
            return puestoDAO.findByIdActivaConCaracteristicas(puesto.getId()).orElse(puesto);
        }
        return puesto;
    }

    private Optional<OpcionRespuesta> buscarOpcionDePregunta(PreguntaAsignada preguntaAsignada, Long opcionId) {
        Pregunta pregunta = preguntaAsignada.getPregunta();
        if (pregunta == null || pregunta.getOpciones() == null) {
            return Optional.empty();
        }
        return pregunta.getOpciones().stream()
                .filter(op -> Objects.equals(op.getId(), opcionId))
                .findFirst();
    }

    private int resolverTiempoMaximoMinutos(Cuestionario cuestionario) {
        if (cuestionario.getTiempoMaximoMinutos() != null) {
            return cuestionario.getTiempoMaximoMinutos();
        }
        if (cuestionario.getEvaluacion() != null && cuestionario.getEvaluacion().getTiempoMaximoMinutos() != null) {
            return cuestionario.getEvaluacion().getTiempoMaximoMinutos();
        }
        throw new ReglaNegocioException("El cuestionario no posee un tiempo máximo configurado.");
    }

    private long calcularSegundosRestantes(LocalDateTime fechaInicio, int tiempoMaximoMinutos, LocalDateTime ahora) {
        long totalSegundos = (long) tiempoMaximoMinutos * 60L;
        if (fechaInicio == null) {
            return totalSegundos;
        }
        long transcurridos = Duration.between(fechaInicio, ahora).getSeconds();
        return Math.max(0L, totalSegundos - Math.max(0L, transcurridos));
    }

    private BloqueCuestionarioDTO aBloqueDTO(BloqueCuestionario bloque) {
        List<PreguntaAsignadaDTO> preguntasDTO = bloque.getPreguntasAsignadas().stream()
                .sorted(Comparator.comparing(PreguntaAsignada::getNroOrden, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::aPreguntaAsignadaDTO)
                .toList();

        return new BloqueCuestionarioDTO(
                bloque.getId(),
                bloque.getNroBloque(),
                Boolean.TRUE.equals(bloque.getRespondido()),
                preguntasDTO
        );
    }

    private PreguntaAsignadaDTO aPreguntaAsignadaDTO(PreguntaAsignada preguntaAsignada) {
        Pregunta pregunta = preguntaAsignada.getPregunta();
        List<OpcionPreguntaCandidatoDTO> opcionesDTO = pregunta.getOpciones().stream()
                .sorted(Comparator.comparing(OpcionRespuesta::getNroOrden, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::aOpcionCandidatoDTO)
                .toList();

        RespuestaCandidato respuesta = preguntaAsignada.getRespuestaCandidato();
        Long respuestaOpcionId = (respuesta != null && respuesta.getOpcionElegida() != null)
                ? respuesta.getOpcionElegida().getId()
                : null;

        return new PreguntaAsignadaDTO(
                preguntaAsignada.getId(),
                preguntaAsignada.getNroOrden(),
                pregunta.getEnunciado(),
                opcionesDTO,
                respuestaOpcionId
        );
    }

    private OpcionPreguntaCandidatoDTO aOpcionCandidatoDTO(OpcionRespuesta opcion) {
        String descripcion = (opcion.getDescripcion() != null && !opcion.getDescripcion().isBlank())
                ? opcion.getDescripcion()
                : opcion.getNombre();
        return new OpcionPreguntaCandidatoDTO(
                opcion.getId(),
                opcion.getNroOrden(),
                descripcion
        );
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
