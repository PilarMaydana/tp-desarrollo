package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.strategy;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.ResultadoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Resultado;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.ResultadoCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.CompetenciaNoAlcanzadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.CompetenciaRequeridaReporteDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.DatosAdicionalesDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.EvaluacionIncluidaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaCandidatoReporteDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaNoAlcanzoMinimoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.PuestoReporteDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.ReporteOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.TotalesReporteDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Estrategia concreta del patrón Strategy para emitir el reporte de Orden de Mérito (CU 028).
 *
 * <p>No es un singleton de Spring: se instancia una nueva estrategia por solicitud (vía
 * {@link FabricaGeneradorReporte}) con el puesto, las evaluaciones seleccionadas y la fecha
 * de emisión, garantizando seguridad ante concurrencia y testabilidad unitaria sin Spring.</p>
 */
public class ReporteOrdenMerito implements IGeneradorReporte<ReporteOrdenMeritoDTO> {

    private static final Comparator<String> COMPARADOR_TEXTO =
            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);

    private final Puesto puesto;
    private final List<Evaluacion> evaluaciones;
    private final LocalDateTime fechaEmision;
    private final CuestionarioDAO cuestionarioDAO;
    private final ResultadoDAO resultadoDAO;

    private List<Cuestionario> cuestionariosPrecargados;
    private List<Resultado> resultadosPrecargados;

    private boolean datosRecopilados = false;
    private List<CompetenciaRequeridaReporteDTO> competenciasRequeridas = List.of();
    private List<FilaOrdenMeritoDTO> ordenDeMerito = List.of();
    private List<FilaNoAlcanzoMinimoDTO> noAlcanzaronMinimos = List.of();
    private List<FilaCandidatoReporteDTO> incompletos = List.of();
    private List<FilaCandidatoReporteDTO> enProceso = List.of();
    private List<FilaCandidatoReporteDTO> sinContestar = List.of();
    private List<FilaCandidatoReporteDTO> activos = List.of();

    /**
     * Constructor utilizado en ejecución con DAOs (invocado por {@link FabricaGeneradorReporte}).
     *
     * @param puesto          puesto con sus características y competencias cargadas
     * @param evaluaciones    evaluaciones incluidas en el reporte
     * @param fechaEmision    fecha y hora de emisión provista por {@code Clock}
     * @param cuestionarioDAO DAO para cargar cuestionarios con candidato y evaluación en una consulta
     * @param resultadoDAO    DAO para cargar resultados con competencias en una consulta
     */
    public ReporteOrdenMerito(Puesto puesto,
                              List<Evaluacion> evaluaciones,
                              LocalDateTime fechaEmision,
                              CuestionarioDAO cuestionarioDAO,
                              ResultadoDAO resultadoDAO) {
        this.puesto = Objects.requireNonNull(puesto, "El puesto es obligatorio");
        this.evaluaciones = evaluaciones != null ? List.copyOf(evaluaciones) : List.of();
        this.fechaEmision = Objects.requireNonNull(fechaEmision, "La fecha de emisión es obligatoria");
        this.cuestionarioDAO = cuestionarioDAO;
        this.resultadoDAO = resultadoDAO;
    }

    /**
     * Constructor directo con datos ya en memoria, pensado para pruebas unitarias puras sin Spring.
     *
     * @param puesto        puesto con sus características y competencias
     * @param evaluaciones  evaluaciones incluidas en el reporte
     * @param cuestionarios cuestionarios pertenecientes a las evaluaciones seleccionadas
     * @param resultados    resultados correspondientes a los cuestionarios completos
     * @param fechaEmision  fecha y hora de emisión del reporte
     */
    public ReporteOrdenMerito(Puesto puesto,
                              List<Evaluacion> evaluaciones,
                              List<Cuestionario> cuestionarios,
                              List<Resultado> resultados,
                              LocalDateTime fechaEmision) {
        this(puesto, evaluaciones, fechaEmision, null, null);
        this.cuestionariosPrecargados = cuestionarios != null ? List.copyOf(cuestionarios) : List.of();
        this.resultadosPrecargados = resultados != null ? List.copyOf(resultados) : List.of();
    }

    @Override
    public void recopilarDatos() {
        List<CaracteristicaPuesto> caracteristicasOrdenadas = obtenerCaracteristicasOrdenadas();
        this.competenciasRequeridas = caracteristicasOrdenadas.stream()
                .map(cp -> new CompetenciaRequeridaReporteDTO(
                        cp.getCompetencia().getId(),
                        cp.getCompetencia().getCodigo(),
                        cp.getCompetencia().getNombre(),
                        aBigDecimal(cp.getPonderacionRequerida())
                ))
                .toList();

        List<Cuestionario> cuestionarios = cargarCuestionarios();
        Map<Long, Resultado> resultadosPorCuestionarioId = cargarResultadosIndexados(cuestionarios);

        List<RegistroEvaluado> cumplenMinimos = new ArrayList<>();
        List<RegistroEvaluado> noCumplenMinimos = new ArrayList<>();
        List<FilaCandidatoReporteDTO> listaIncompletos = new ArrayList<>();
        List<FilaCandidatoReporteDTO> listaEnProceso = new ArrayList<>();
        List<FilaCandidatoReporteDTO> listaSinContestar = new ArrayList<>();
        List<FilaCandidatoReporteDTO> listaActivos = new ArrayList<>();

        for (Cuestionario cuestionario : cuestionarios) {
            if (cuestionario == null || cuestionario.getEstado() == null) {
                continue;
            }
            EstadoCuestionario estado = cuestionario.getEstado();
            if (estado == EstadoCuestionario.COMPLETO) {
                clasificarCuestionarioCompleto(
                        cuestionario,
                        resultadosPorCuestionarioId.get(cuestionario.getId()),
                        caracteristicasOrdenadas,
                        cumplenMinimos,
                        noCumplenMinimos
                );
            } else {
                FilaCandidatoReporteDTO filaNoCompleta = aFilaNoCompleta(cuestionario);
                switch (estado) {
                    case INCOMPLETO -> listaIncompletos.add(filaNoCompleta);
                    case EN_PROCESO -> listaEnProceso.add(filaNoCompleta);
                    case SIN_CONTESTAR -> listaSinContestar.add(filaNoCompleta);
                    case ACTIVO -> listaActivos.add(filaNoCompleta);
                    default -> {
                        // COMPLETO ya fue manejado arriba
                    }
                }
            }
        }

        ordenarPorPuntaje(cumplenMinimos);
        ordenarPorPuntaje(noCumplenMinimos);

        List<FilaOrdenMeritoDTO> filasOrdenMerito = new ArrayList<>(cumplenMinimos.size());
        int posicion = 1;
        for (RegistroEvaluado registro : cumplenMinimos) {
            filasOrdenMerito.add(new FilaOrdenMeritoDTO(
                    posicion++,
                    registro.cuestionarioId(),
                    registro.evaluacionId(),
                    registro.tipoDocumento(),
                    registro.nroDocumento(),
                    registro.apellido(),
                    registro.nombre(),
                    registro.puntajeObtenido(),
                    registro.datosAdicionales()
            ));
        }

        List<FilaNoAlcanzoMinimoDTO> filasNoAlcanzaron = noCumplenMinimos.stream()
                .map(registro -> new FilaNoAlcanzoMinimoDTO(
                        registro.cuestionarioId(),
                        registro.evaluacionId(),
                        registro.tipoDocumento(),
                        registro.nroDocumento(),
                        registro.apellido(),
                        registro.nombre(),
                        registro.puntajeObtenido(),
                        registro.datosAdicionales(),
                        registro.competenciasNoAlcanzadas()
                ))
                .toList();

        Comparator<FilaCandidatoReporteDTO> comparadorNoCompletos = comparadorAlfanumericoNoCompletos();
        listaIncompletos.sort(comparadorNoCompletos);
        listaEnProceso.sort(comparadorNoCompletos);
        listaSinContestar.sort(comparadorNoCompletos);
        listaActivos.sort(comparadorNoCompletos);

        this.ordenDeMerito = List.copyOf(filasOrdenMerito);
        this.noAlcanzaronMinimos = List.copyOf(filasNoAlcanzaron);
        this.incompletos = List.copyOf(listaIncompletos);
        this.enProceso = List.copyOf(listaEnProceso);
        this.sinContestar = List.copyOf(listaSinContestar);
        this.activos = List.copyOf(listaActivos);
        this.datosRecopilados = true;
    }

    @Override
    public ReporteOrdenMeritoDTO generarSalida() {
        if (!datosRecopilados) {
            recopilarDatos();
        }

        PuestoReporteDTO puestoDTO = new PuestoReporteDTO(
                puesto.getId(),
                puesto.getCodigo(),
                puesto.getNombre(),
                puesto.getEmpresa()
        );

        List<EvaluacionIncluidaDTO> evaluacionesDTO = evaluaciones.stream()
                .map(e -> new EvaluacionIncluidaDTO(e.getId(), e.getFechaCreacion()))
                .toList();

        int totalGeneral = ordenDeMerito.size()
                + noAlcanzaronMinimos.size()
                + incompletos.size()
                + enProceso.size()
                + sinContestar.size()
                + activos.size();

        TotalesReporteDTO totales = new TotalesReporteDTO(
                ordenDeMerito.size(),
                noAlcanzaronMinimos.size(),
                incompletos.size(),
                enProceso.size(),
                sinContestar.size(),
                activos.size(),
                totalGeneral
        );

        return new ReporteOrdenMeritoDTO(
                puestoDTO,
                evaluacionesDTO,
                fechaEmision,
                competenciasRequeridas,
                ordenDeMerito,
                noAlcanzaronMinimos,
                incompletos,
                enProceso,
                sinContestar,
                activos,
                totales
        );
    }

    /**
     * Ordena los candidatos evaluados en forma descendente por {@code puntajeObtenido} (usando
     * {@link BigDecimal#compareTo(BigDecimal)}) y aplica desempate determinístico ascendente por
     * apellido, nombre, número de documento, evaluación y cuestionario.
     *
     * @param registros lista mutable de registros evaluados a ordenar
     */
    void ordenarPorPuntaje(List<RegistroEvaluado> registros) {
        if (registros == null || registros.size() <= 1) {
            return;
        }
        registros.sort(
                Comparator.comparing(
                                RegistroEvaluado::puntajeObtenido,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                        .thenComparing(RegistroEvaluado::apellido, COMPARADOR_TEXTO)
                        .thenComparing(RegistroEvaluado::nombre, COMPARADOR_TEXTO)
                        .thenComparing(RegistroEvaluado::nroDocumento, COMPARADOR_TEXTO)
                        .thenComparing(RegistroEvaluado::evaluacionId, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(RegistroEvaluado::cuestionarioId, Comparator.nullsLast(Comparator.naturalOrder()))
        );
    }

    private void clasificarCuestionarioCompleto(Cuestionario cuestionario,
                                                Resultado resultado,
                                                List<CaracteristicaPuesto> caracteristicasPuesto,
                                                List<RegistroEvaluado> cumplenMinimos,
                                                List<RegistroEvaluado> noCumplenMinimos) {
        Map<Long, BigDecimal> puntajePorCompetenciaId = indexarPuntajesPorCompetencia(resultado);
        List<CompetenciaNoAlcanzadaDTO> noAlcanzadas = new ArrayList<>();

        for (CaracteristicaPuesto caracteristica : caracteristicasPuesto) {
            Competencia competencia = caracteristica.getCompetencia();
            BigDecimal requerido = aBigDecimal(caracteristica.getPonderacionRequerida());
            BigDecimal obtenido = puntajePorCompetenciaId.get(competencia.getId());

            boolean alcanzada = obtenido != null && obtenido.compareTo(requerido) >= 0;
            if (!alcanzada) {
                noAlcanzadas.add(new CompetenciaNoAlcanzadaDTO(
                        competencia.getId(),
                        competencia.getCodigo(),
                        competencia.getNombre(),
                        obtenido,
                        requerido
                ));
            }
        }

        Candidato candidato = cuestionario.getCandidato();
        Long evaluacionId = cuestionario.getEvaluacion() != null ? cuestionario.getEvaluacion().getId() : null;
        int accesos = cuestionario.getCantidadAccesos() != null ? cuestionario.getCantidadAccesos() : 0;
        DatosAdicionalesDTO datosAdicionales = DatosAdicionalesDTO.paraCompleto(
                cuestionario.getFechaInicio(),
                cuestionario.getFechaFinalizacion(),
                accesos
        );

        BigDecimal puntajeObtenido = resultado != null ? resultado.getPuntajeObtenido() : null;
        RegistroEvaluado registro = new RegistroEvaluado(
                cuestionario.getId(),
                evaluacionId,
                candidato != null ? candidato.getTipoDocumento() : null,
                candidato != null ? candidato.getNroDocumento() : null,
                candidato != null ? candidato.getApellido() : null,
                candidato != null ? candidato.getNombre() : null,
                puntajeObtenido,
                datosAdicionales,
                List.copyOf(noAlcanzadas)
        );

        if (noAlcanzadas.isEmpty()) {
            cumplenMinimos.add(registro);
        } else {
            noCumplenMinimos.add(registro);
        }
    }

    private FilaCandidatoReporteDTO aFilaNoCompleta(Cuestionario cuestionario) {
        Candidato candidato = cuestionario.getCandidato();
        Long evaluacionId = cuestionario.getEvaluacion() != null ? cuestionario.getEvaluacion().getId() : null;
        int accesos = cuestionario.getCantidadAccesos() != null ? cuestionario.getCantidadAccesos() : 0;

        DatosAdicionalesDTO datosAdicionales = DatosAdicionalesDTO.paraNoCompleto(
                cuestionario.getFechaInicio(),
                cuestionario.getFechaUltimoIngreso(),
                accesos
        );

        return new FilaCandidatoReporteDTO(
                cuestionario.getId(),
                evaluacionId,
                candidato != null ? candidato.getTipoDocumento() : null,
                candidato != null ? candidato.getNroDocumento() : null,
                candidato != null ? candidato.getApellido() : null,
                candidato != null ? candidato.getNombre() : null,
                null,
                datosAdicionales
        );
    }

    private List<CaracteristicaPuesto> obtenerCaracteristicasOrdenadas() {
        if (puesto.getCaracteristicas() == null) {
            return List.of();
        }
        return puesto.getCaracteristicas().stream()
                .filter(cp -> cp != null && cp.getCompetencia() != null && cp.getPonderacionRequerida() != null)
                .sorted(Comparator
                        .comparing((CaracteristicaPuesto cp) -> cp.getCompetencia().getNombre(), COMPARADOR_TEXTO)
                        .thenComparing(cp -> cp.getCompetencia().getId(), Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<Cuestionario> cargarCuestionarios() {
        if (cuestionariosPrecargados != null) {
            return cuestionariosPrecargados;
        }
        if (cuestionarioDAO == null || evaluaciones.isEmpty()) {
            return List.of();
        }
        List<Long> evaluacionIds = evaluaciones.stream()
                .map(Evaluacion::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (evaluacionIds.isEmpty()) {
            return List.of();
        }
        return cuestionarioDAO.findByEvaluacionIdInConCandidatoYEvaluacion(evaluacionIds);
    }

    private Map<Long, Resultado> cargarResultadosIndexados(List<Cuestionario> cuestionarios) {
        List<Resultado> resultados;
        if (resultadosPrecargados != null) {
            resultados = resultadosPrecargados;
        } else {
            List<Long> idsCompletos = cuestionarios.stream()
                    .filter(c -> c != null && c.getEstado() == EstadoCuestionario.COMPLETO && c.getId() != null)
                    .map(Cuestionario::getId)
                    .distinct()
                    .toList();
            if (resultadoDAO == null || idsCompletos.isEmpty()) {
                resultados = List.of();
            } else {
                resultados = resultadoDAO.findByCuestionarioIdInConCompetencias(idsCompletos);
            }
        }

        Map<Long, Resultado> index = new LinkedHashMap<>();
        for (Resultado resultado : resultados) {
            if (resultado != null && resultado.getCuestionario() != null && resultado.getCuestionario().getId() != null) {
                index.put(resultado.getCuestionario().getId(), resultado);
            }
        }
        return index;
    }

    private Map<Long, BigDecimal> indexarPuntajesPorCompetencia(Resultado resultado) {
        Map<Long, BigDecimal> map = new LinkedHashMap<>();
        if (resultado == null || resultado.getResultadosCompetencias() == null) {
            return map;
        }
        for (ResultadoCompetencia rc : resultado.getResultadosCompetencias()) {
            if (rc != null && rc.getCompetencia() != null && rc.getCompetencia().getId() != null) {
                map.put(rc.getCompetencia().getId(), rc.getTotalCompetencia());
            }
        }
        return map;
    }

    private BigDecimal aBigDecimal(Double valor) {
        return valor != null ? BigDecimal.valueOf(valor) : BigDecimal.ZERO;
    }

    private Comparator<FilaCandidatoReporteDTO> comparadorAlfanumericoNoCompletos() {
        return Comparator.comparing(FilaCandidatoReporteDTO::apellido, COMPARADOR_TEXTO)
                .thenComparing(FilaCandidatoReporteDTO::nombre, COMPARADOR_TEXTO)
                .thenComparing(FilaCandidatoReporteDTO::nroDocumento, COMPARADOR_TEXTO)
                .thenComparing(FilaCandidatoReporteDTO::evaluacionId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(FilaCandidatoReporteDTO::cuestionarioId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    /**
     * Registro interno temporal utilizado por {@link #ordenarPorPuntaje(List)} para ordenar
     * candidatos con cuestionarios completos antes de proyectarlos al DTO final.
     */
    record RegistroEvaluado(
            Long cuestionarioId,
            Long evaluacionId,
            ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento tipoDocumento,
            String nroDocumento,
            String apellido,
            String nombre,
            BigDecimal puntajeObtenido,
            DatosAdicionalesDTO datosAdicionales,
            List<CompetenciaNoAlcanzadaDTO> competenciasNoAlcanzadas
    ) {
    }
}
