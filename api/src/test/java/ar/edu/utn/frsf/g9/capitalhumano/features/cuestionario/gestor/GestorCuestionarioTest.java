package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.config.CuestionarioProperties;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.ResultadoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioIniciadoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.InstruccionesCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.ResponderBloqueRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.ResponderBloqueResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.RespuestaItemDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.BloqueCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.PreguntaAsignada;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.RespuestaCandidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Resultado;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios con JUnit 5 y Mockito para {@link GestorCuestionario} (CU 025 y CU 026 Parte 1),
 * utilizando {@link Clock} fijo y {@link Random} con semilla fija para determinismo total.
 */
@ExtendWith(MockitoExtension.class)
class GestorCuestionarioTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 19, 15, 0, 0);
    private static final Clock CLOCK_FIJO = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
    private static final String INSTRUCCIONES_TEST = "Lea atentamente y responda todas las preguntas.";

    @Mock
    private FactorDAO factorDAO;

    @Mock
    private CuestionarioDAO cuestionarioDAO;

    @Mock
    private PuestoDAO puestoDAO;

    @Mock
    private ResultadoDAO resultadoDAO;

    @Mock
    private ContextoSeguridad contextoSeguridad;

    private CuestionarioProperties properties;
    private GestorCuestionario gestorCuestionario;

    @BeforeEach
    void setUp() {
        properties = new CuestionarioProperties(INSTRUCCIONES_TEST, 3, 72);
        lenient().when(contextoSeguridad.obtenerCandidatoActual()).thenReturn(1L);
        gestorCuestionario = new GestorCuestionario(
                factorDAO,
                cuestionarioDAO,
                puestoDAO,
                resultadoDAO,
                new CalculadorResultadosCuestionario(),
                contextoSeguridad,
                properties,
                CLOCK_FIJO,
                new Random(42L)
        );
    }

    // =========================================================================
    // Tests existentes de CU 025
    // =========================================================================

    @Test
    @DisplayName("Factor.tieneMinimoDosPreguntas cuenta solo preguntas activas (eliminado = false)")
    void factor_tieneMinimoDosPreguntas() {
        Competencia comp = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Factor factor = new Factor(1L, "FAC-01", "ORAL", "DESC", 1, comp);

        assertThat(factor.tieneMinimoDosPreguntas()).isFalse();

        Pregunta p1 = new Pregunta(1L, "P1", "E1", "D1", factor);
        Pregunta p2Eliminada = new Pregunta(2L, "P2", "E2", "D2", factor);
        p2Eliminada.setEliminado(true);
        factor.addPregunta(p1);
        factor.addPregunta(p2Eliminada);

        assertThat(factor.tieneMinimoDosPreguntas()).isFalse();

        Pregunta p3 = new Pregunta(3L, "P3", "E3", "D3", factor);
        factor.addPregunta(p3);

        assertThat(factor.tieneMinimoDosPreguntas()).isTrue();
        assertThat(factor.getPreguntasActivas()).containsExactly(p1, p3);
    }

    @Test
    @DisplayName("Cuestionario.armarBloques distribuye preguntas de forma equilibrada sin superar cantidadPreguntasPorBloque")
    void cuestionario_armarBloques() {
        Competencia comp = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Factor factor = new Factor(1L, "FAC-01", "ORAL", "DESC", 1, comp);
        List<Pregunta> preguntas = List.of(
                new Pregunta(1L, "P1", "E1", "D1", factor),
                new Pregunta(2L, "P2", "E2", "D2", factor),
                new Pregunta(3L, "P3", "E3", "D3", factor),
                new Pregunta(4L, "P4", "E4", "D4", factor),
                new Pregunta(5L, "P5", "E5", "D5", factor),
                new Pregunta(6L, "P6", "E6", "D6", factor),
                new Pregunta(7L, "P7", "E7", "D7", factor)
        );

        Cuestionario cuestionario = new Cuestionario();
        cuestionario.setCantidadPreguntasPorBloque(3);
        cuestionario.armarBloques(preguntas);

        assertThat(cuestionario.getBloques()).hasSize(3);
        assertThat(cuestionario.getBloques())
                .extracting(BloqueCuestionario::getNroBloque)
                .containsExactly(1, 2, 3);
        // 7 preguntas con máximo 3 por bloque => 3 bloques equilibrados: 3, 2, 2
        assertThat(cuestionario.getBloques().get(0).getPreguntasAsignadas()).hasSize(3);
        assertThat(cuestionario.getBloques().get(1).getPreguntasAsignadas()).hasSize(2);
        assertThat(cuestionario.getBloques().get(2).getPreguntasAsignadas()).hasSize(2);

        List<Integer> ordenes = cuestionario.getBloques().stream()
                .flatMap(b -> b.getPreguntasAsignadas().stream())
                .map(PreguntaAsignada::getNroOrden)
                .toList();
        assertThat(ordenes).containsExactly(1, 2, 3, 4, 5, 6, 7);
        assertThat(cuestionario.getTotalPreguntasAsignadas()).isEqualTo(7);
    }

    @Test
    @DisplayName("Cuestionario.armarBloques reparte 6 preguntas con máximo 5 en 2 bloques equilibrados de 3 y 3")
    void cuestionario_armarBloques_equilibradoSinSobrasUnitarias() {
        Competencia comp = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Factor factor = new Factor(1L, "FAC-01", "ORAL", "DESC", 1, comp);
        List<Pregunta> preguntas = List.of(
                new Pregunta(1L, "P1", "E1", "D1", factor),
                new Pregunta(2L, "P2", "E2", "D2", factor),
                new Pregunta(3L, "P3", "E3", "D3", factor),
                new Pregunta(4L, "P4", "E4", "D4", factor),
                new Pregunta(5L, "P5", "E5", "D5", factor),
                new Pregunta(6L, "P6", "E6", "D6", factor)
        );

        Cuestionario cuestionario = new Cuestionario();
        cuestionario.setCantidadPreguntasPorBloque(5);
        cuestionario.armarBloques(preguntas);

        assertThat(cuestionario.getBloques()).hasSize(2);
        assertThat(cuestionario.getBloques().get(0).getPreguntasAsignadas()).hasSize(3);
        assertThat(cuestionario.getBloques().get(1).getPreguntasAsignadas()).hasSize(3);
        assertThat(cuestionario.getTotalPreguntasAsignadas()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cuestionario.armarBloques falla si cantidadPreguntasPorBloque no es positiva")
    void cuestionario_armarBloques_configuracionInvalida() {
        Cuestionario cuestionario = new Cuestionario();
        cuestionario.setCantidadPreguntasPorBloque(0);

        assertThatThrownBy(() -> cuestionario.armarBloques(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("obtenerCompetenciasNoEvaluables detecta competencias sin factores válidos con al menos 2 preguntas activas")
    void obtenerCompetenciasNoEvaluables_flujo9A() {
        Competencia compValida = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Competencia compInvalida = new Competencia(2L, "RESOL-01", "RESOLUCIÓN DE PROBLEMAS", "DESC");
        Competencia compSinFactores = new Competencia(3L, "LID-01", "LIDERAZGO", "DESC");

        Puesto puesto = new Puesto(1L, "QA-01", "ANALISTA QA", "DESC", "TECH");
        puesto.addCaracteristica(new CaracteristicaPuesto(1L, puesto, compValida, 7.0));
        puesto.addCaracteristica(new CaracteristicaPuesto(2L, puesto, compInvalida, 8.0));
        puesto.addCaracteristica(new CaracteristicaPuesto(3L, puesto, compSinFactores, 9.0));

        Factor fValido = new Factor(1L, "F1", "ORAL", "D", 1, compValida);
        fValido.addPregunta(new Pregunta(1L, "P1", "E1", "D1", fValido));
        fValido.addPregunta(new Pregunta(2L, "P2", "E2", "D2", fValido));

        Factor fInsuficiente = new Factor(2L, "F2", "CAUSA RAÍZ", "D", 1, compInvalida);
        fInsuficiente.addPregunta(new Pregunta(3L, "P3", "E3", "D3", fInsuficiente));
        Pregunta pEliminada = new Pregunta(4L, "P4", "E4", "D4", fInsuficiente);
        pEliminada.setEliminado(true);
        fInsuficiente.addPregunta(pEliminada);

        when(factorDAO.findActivosConPreguntasByCompetenciaIdIn(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(fValido, fInsuficiente));

        List<String> noEvaluables = gestorCuestionario.obtenerCompetenciasNoEvaluables(puesto);

        assertThat(noEvaluables).containsExactly("RESOLUCIÓN DE PROBLEMAS", "LIDERAZGO");
    }

    @Test
    @DisplayName("seleccionarPreguntas elige exactamente 2 preguntas activas por cada factor activo válido")
    void seleccionarPreguntas_dosPorFactorActivo() {
        Competencia comp1 = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Competencia comp2 = new Competencia(2L, "TRAB-01", "TRABAJO EN EQUIPO", "DESC");

        Puesto puesto = new Puesto(1L, "DEV-01", "DESARROLLADOR SENIOR", "DESC", "TECH");
        puesto.addCaracteristica(new CaracteristicaPuesto(1L, puesto, comp1, 8.0));
        puesto.addCaracteristica(new CaracteristicaPuesto(2L, puesto, comp2, 9.0));

        Factor f1 = new Factor(1L, "F1", "ORAL", "D", 1, comp1);
        f1.addPregunta(new Pregunta(1L, "P1", "E1", "D", f1));
        f1.addPregunta(new Pregunta(2L, "P2", "E2", "D", f1));
        f1.addPregunta(new Pregunta(3L, "P3", "E3", "D", f1));

        Factor f2 = new Factor(2L, "F2", "ESCRITA", "D", 2, comp1);
        f2.addPregunta(new Pregunta(4L, "P4", "E4", "D", f2));
        f2.addPregunta(new Pregunta(5L, "P5", "E5", "D", f2));

        Factor f3 = new Factor(3L, "F3", "COLABORACIÓN", "D", 1, comp2);
        f3.addPregunta(new Pregunta(6L, "P6", "E6", "D", f3));
        f3.addPregunta(new Pregunta(7L, "P7", "E7", "D", f3));
        Pregunta pEliminada = new Pregunta(8L, "P8", "E8", "D", f3);
        pEliminada.setEliminado(true);
        f3.addPregunta(pEliminada);

        when(factorDAO.findActivosConPreguntasByCompetenciaIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(f1, f2, f3));

        List<Pregunta> seleccionadas = gestorCuestionario.seleccionarPreguntas(puesto);

        assertThat(seleccionadas).hasSize(6);
        assertThat(seleccionadas).doesNotContain(pEliminada);
        assertThat(seleccionadas.stream().filter(p -> p.getFactor().equals(f1)).count()).isEqualTo(2);
        assertThat(seleccionadas.stream().filter(p -> p.getFactor().equals(f2)).count()).isEqualTo(2);
        assertThat(seleccionadas.stream().filter(p -> p.getFactor().equals(f3)).count()).isEqualTo(2);
    }

    // =========================================================================
    // Tests de CU 026 Parte 1: Instrucciones, disponibilidad, inicio y reingreso
    // =========================================================================

    @Test
    @DisplayName("CU 026 Paso 1: obtenerInstrucciones devuelve metadatos e instrucciones sin incrementar accesos")
    void obtenerInstrucciones_activoVigente_noIncrementaAccesos() {
        Cuestionario cuestionario = crearCuestionarioBase(6L, EstadoCuestionario.ACTIVO);
        cuestionario.setFechaCreacion(AHORA.minusHours(2));
        cuestionario.setCantidadAccesos(0);

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(6L, 1L)).thenReturn(Optional.of(cuestionario));

        InstruccionesCuestionarioDTO dto = gestorCuestionario.obtenerInstrucciones(6L);

        assertThat(dto.id()).isEqualTo(6L);
        assertThat(dto.estado()).isEqualTo(EstadoCuestionario.ACTIVO);
        assertThat(dto.nombrePuesto()).isEqualTo("ARQUITECTO DE SOFTWARE");
        assertThat(dto.tiempoMaximoMinutos()).isEqualTo(60);
        assertThat(dto.instrucciones()).isEqualTo(INSTRUCCIONES_TEST);
        assertThat(cuestionario.getCantidadAccesos()).isZero();
        verify(cuestionarioDAO, never()).save(any());
        verify(cuestionarioDAO, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("CU 026: cuestionario ajeno o inexistente lanza 404 (RecursoNoEncontradoException)")
    void cuestionarioAjeno_lanza404() {
        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(2L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestorCuestionario.obtenerInstrucciones(2L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        assertThatThrownBy(() -> gestorCuestionario.iniciarCuestionario(2L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("CU 026 Pasos 2-4: primer ingreso con bloques ya generados (CU 25) no regenera, pasa a EN_PROCESO y devuelve bloque 1")
    void iniciarCuestionario_primerIngresoConBloquesYaGenerados_noRegeneraYDevuelvePrimerBloque() {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(6L, EstadoCuestionario.ACTIVO, false);
        cuestionario.setFechaCreacion(AHORA.minusHours(2));
        cuestionario.setCantidadAccesos(0);

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(6L, 1L)).thenReturn(Optional.of(cuestionario));
        when(cuestionarioDAO.saveAndFlush(any(Cuestionario.class))).thenAnswer(inv -> inv.getArgument(0));

        CuestionarioIniciadoDTO resultado = gestorCuestionario.iniciarCuestionario(6L);

        assertThat(resultado.id()).isEqualTo(6L);
        assertThat(resultado.estado()).isEqualTo(EstadoCuestionario.EN_PROCESO);
        assertThat(resultado.fechaInicio()).isEqualTo(AHORA);
        assertThat(resultado.segundosRestantes()).isEqualTo(3600L);
        assertThat(resultado.totalBloques()).isEqualTo(2);
        assertThat(resultado.bloqueActual().numero()).isEqualTo(1);
        assertThat(resultado.bloqueActual().completado()).isFalse();
        assertThat(resultado.bloqueActual().preguntas()).hasSize(3);
        assertThat(resultado.bloqueActual().preguntas().get(0).nroOrden()).isEqualTo(1);
        assertThat(resultado.bloqueActual().preguntas().get(0).respuestaOpcionId()).isNull();

        assertThat(cuestionario.getEstado()).isEqualTo(EstadoCuestionario.EN_PROCESO);
        assertThat(cuestionario.getFechaInicio()).isEqualTo(AHORA);
        assertThat(cuestionario.getFechaCambioEstado()).isEqualTo(AHORA);
        assertThat(cuestionario.getCantidadAccesos()).isEqualTo(1);

        verify(factorDAO, never()).findActivosConPreguntasByCompetenciaIdIn(anyCollection());
        verify(cuestionarioDAO).saveAndFlush(cuestionario);
    }

    @Test
    @DisplayName("CU 026 Pasos 2-4 (sin bloques previos): sortea 2 preguntas por factor válido, excluye factor con 1 pregunta y arma bloques")
    void iniciarCuestionario_sinBloquesPrevios_sorteaDosPorFactorExcluyeFactorConUnaPregunta() {
        Competencia comp1 = new Competencia(5L, "TEC-01", "COMPETENCIAS TÉCNICAS", "DESC");
        Competencia comp2 = new Competencia(2L, "LID-01", "LIDERAZGO", "DESC");
        Puesto puesto = new Puesto(3L, "ARQ-01", "ARQUITECTO DE SOFTWARE", "DESC", "INNOVATECH SA");
        puesto.addCaracteristica(new CaracteristicaPuesto(1L, puesto, comp1, 9.5));
        puesto.addCaracteristica(new CaracteristicaPuesto(2L, puesto, comp2, 8.0));

        Factor fValido1 = new Factor(3L, "FAC-03", "DESARROLLO", "D", 1, comp1);
        fValido1.addPregunta(crearPreguntaConOpciones(1L, "P1", "ENUNCIADO 1", fValido1));
        fValido1.addPregunta(crearPreguntaConOpciones(2L, "P2", "ENUNCIADO 2", fValido1));
        fValido1.addPregunta(crearPreguntaConOpciones(7L, "P7", "ENUNCIADO 7", fValido1));

        Factor fValido2 = new Factor(6L, "FAC-06", "EQUIPOS", "D", 1, comp2);
        fValido2.addPregunta(crearPreguntaConOpciones(12L, "P12", "ENUNCIADO 12", fValido2));
        fValido2.addPregunta(crearPreguntaConOpciones(13L, "P13", "ENUNCIADO 13", fValido2));

        Factor fUnaPregunta = new Factor(8L, "FAC-08", "VISIÓN", "D", 2, comp2);
        Pregunta preguntaExcluida = crearPreguntaConOpciones(16L, "P16", "ENUNCIADO EXCLUIDO", fUnaPregunta);
        fUnaPregunta.addPregunta(preguntaExcluida);

        Cuestionario cuestionario = crearCuestionarioBase(20L, EstadoCuestionario.ACTIVO);
        cuestionario.setPuesto(puesto);
        cuestionario.setFechaCreacion(AHORA.minusHours(1));
        cuestionario.setCantidadPreguntasPorBloque(3);

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(20L, 1L)).thenReturn(Optional.of(cuestionario));
        when(factorDAO.findActivosConPreguntasByCompetenciaIdIn(List.of(5L, 2L)))
                .thenReturn(List.of(fValido1, fValido2, fUnaPregunta));
        when(cuestionarioDAO.saveAndFlush(any(Cuestionario.class))).thenAnswer(inv -> inv.getArgument(0));

        CuestionarioIniciadoDTO resultado = gestorCuestionario.iniciarCuestionario(20L);

        assertThat(resultado.estado()).isEqualTo(EstadoCuestionario.EN_PROCESO);
        assertThat(resultado.fechaInicio()).isEqualTo(AHORA);
        assertThat(resultado.totalBloques()).isEqualTo(2); // 4 preguntas (2 de fValido1 + 2 de fValido2) con máx 3 -> 2 + 2
        assertThat(resultado.bloqueActual().numero()).isEqualTo(1);
        assertThat(resultado.bloqueActual().preguntas()).hasSize(2);

        List<Pregunta> todasAsignadas = cuestionario.getBloques().stream()
                .flatMap(b -> b.getPreguntasAsignadas().stream())
                .map(PreguntaAsignada::getPregunta)
                .toList();
        assertThat(todasAsignadas).hasSize(4);
        assertThat(todasAsignadas).doesNotContain(preguntaExcluida);
        assertThat(todasAsignadas.stream().filter(p -> p.getFactor().equals(fValido1)).count()).isEqualTo(2);
        assertThat(todasAsignadas.stream().filter(p -> p.getFactor().equals(fValido2)).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("CU 026: primer ingreso sin bloques y sin ningún factor válido lanza 422 sin modificar estado ni accesos")
    void iniciarCuestionario_sinFactoresValidos_lanza422() {
        Competencia comp = new Competencia(4L, "RESOL-01", "RESOLUCIÓN", "DESC");
        Puesto puesto = new Puesto(2L, "QA-01", "ANALISTA QA", "DESC", "TECH");
        puesto.addCaracteristica(new CaracteristicaPuesto(1L, puesto, comp, 8.0));

        Factor fInsuficiente = new Factor(5L, "FAC-05", "CAUSA RAÍZ", "D", 1, comp);
        fInsuficiente.addPregunta(crearPreguntaConOpciones(11L, "P11", "ENUNCIADO 11", fInsuficiente));

        Cuestionario cuestionario = crearCuestionarioBase(21L, EstadoCuestionario.ACTIVO);
        cuestionario.setPuesto(puesto);
        cuestionario.setFechaCreacion(AHORA.minusHours(1));

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(21L, 1L)).thenReturn(Optional.of(cuestionario));
        when(factorDAO.findActivosConPreguntasByCompetenciaIdIn(List.of(4L))).thenReturn(List.of(fInsuficiente));

        assertThatThrownBy(() -> gestorCuestionario.iniciarCuestionario(21L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("factores válidos");

        assertThat(cuestionario.getEstado()).isEqualTo(EstadoCuestionario.ACTIVO);
        assertThat(cuestionario.getCantidadAccesos()).isZero();
        verify(cuestionarioDAO, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("CU 026 Flujo 1.C: reingreso en EN_PROCESO no regenera bloques y devuelve el primer bloque pendiente")
    void iniciarCuestionario_reingresoEnProceso_noRegeneraYDevuelveBloquePendiente() {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(7L, EstadoCuestionario.EN_PROCESO, true);
        LocalDateTime inicioOriginal = AHORA.minusMinutes(15);
        cuestionario.setFechaCreacion(AHORA.minusHours(3));
        cuestionario.setFechaInicio(inicioOriginal);
        cuestionario.setCantidadAccesos(1);

        // Simulamos además una pregunta parcialmente respondida en el bloque 2
        PreguntaAsignada primeraBloque2 = cuestionario.getBloques().get(1).getPreguntasAsignadas().get(0);
        OpcionRespuesta opcionElegida = primeraBloque2.getPregunta().getOpciones().get(1);
        primeraBloque2.setRespuestaCandidato(new RespuestaCandidato(primeraBloque2, opcionElegida, AHORA.minusMinutes(2)));

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(7L, 1L)).thenReturn(Optional.of(cuestionario));
        when(cuestionarioDAO.save(any(Cuestionario.class))).thenAnswer(inv -> inv.getArgument(0));

        CuestionarioIniciadoDTO resultado = gestorCuestionario.iniciarCuestionario(7L);

        assertThat(resultado.id()).isEqualTo(7L);
        assertThat(resultado.estado()).isEqualTo(EstadoCuestionario.EN_PROCESO);
        assertThat(resultado.fechaInicio()).isEqualTo(inicioOriginal);
        assertThat(resultado.segundosRestantes()).isEqualTo(45L * 60L);
        assertThat(resultado.totalBloques()).isEqualTo(2);
        assertThat(resultado.bloqueActual().numero()).isEqualTo(2);
        assertThat(resultado.bloqueActual().completado()).isFalse();
        assertThat(resultado.bloqueActual().preguntas().get(0).respuestaOpcionId()).isEqualTo(opcionElegida.getId());
        assertThat(resultado.bloqueActual().preguntas().get(1).respuestaOpcionId()).isNull();

        assertThat(cuestionario.getCantidadAccesos()).isEqualTo(2);
        verify(factorDAO, never()).findActivosConPreguntasByCompetenciaIdIn(anyCollection());
        verify(cuestionarioDAO).save(cuestionario);
    }

    @Test
    @DisplayName("CU 026 Flujo 1.A: tiempo máximo de llenado superado pasa a INCOMPLETO, persiste y lanza 422")
    void flujo1A_tiempoMaximoSuperado_cambiaAIncompletoYPersiste() {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(9L, EstadoCuestionario.EN_PROCESO, false);
        cuestionario.setFechaCreacion(AHORA.minusHours(5));
        cuestionario.setFechaInicio(AHORA.minusMinutes(60).minusSeconds(1)); // 1 segundo excedido
        cuestionario.setCantidadAccesos(1);

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(9L, 1L)).thenReturn(Optional.of(cuestionario));

        assertThatThrownBy(() -> gestorCuestionario.iniciarCuestionario(9L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("tiempo máximo");

        assertThat(cuestionario.getEstado()).isEqualTo(EstadoCuestionario.INCOMPLETO);
        assertThat(cuestionario.getFechaCambioEstado()).isEqualTo(AHORA);
        assertThat(cuestionario.getTiempoRestanteSegundos()).isZero();
        verify(cuestionarioDAO).save(cuestionario);
    }

    @Test
    @DisplayName("CU 026 Flujo 1.A borde: diferencia exactamente igual al tiempo máximo permite continuar")
    void flujo1A_diferenciaIgualAlTiempoMaximo_permiteContinuar() {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(9L, EstadoCuestionario.EN_PROCESO, false);
        cuestionario.setFechaCreacion(AHORA.minusHours(5));
        cuestionario.setFechaInicio(AHORA.minusMinutes(60)); // Exactamente igual a 60 minutos
        cuestionario.setCantidadAccesos(1);

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(9L, 1L)).thenReturn(Optional.of(cuestionario));
        when(cuestionarioDAO.save(any(Cuestionario.class))).thenAnswer(inv -> inv.getArgument(0));

        CuestionarioIniciadoDTO resultado = gestorCuestionario.iniciarCuestionario(9L);

        assertThat(resultado.estado()).isEqualTo(EstadoCuestionario.EN_PROCESO);
        assertThat(resultado.segundosRestantes()).isZero();
    }

    @Test
    @DisplayName("CU 026 Flujo 1.B: ACTIVO vencido supera maxHorasEnEstadoActivo -> pasa a SIN_CONTESTAR, persiste y lanza 422")
    void flujo1B_activoVencido_cambiaASinContestarYPersiste() {
        Cuestionario cuestionario = crearCuestionarioBase(8L, EstadoCuestionario.ACTIVO);
        cuestionario.setFechaCreacion(AHORA.minusHours(72).minusSeconds(1));

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(8L, 1L)).thenReturn(Optional.of(cuestionario));

        assertThatThrownBy(() -> gestorCuestionario.obtenerInstrucciones(8L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Sin Contestar");

        assertThat(cuestionario.getEstado()).isEqualTo(EstadoCuestionario.SIN_CONTESTAR);
        assertThat(cuestionario.getFechaCambioEstado()).isEqualTo(AHORA);
        verify(cuestionarioDAO).save(cuestionario);
    }

    @Test
    @DisplayName("CU 026 Precondición: cantidadAccesos >= maxAccesos en iniciar pasa a INCOMPLETO, persiste y lanza 422")
    void limiteAccesos_alcanzadoEnIniciar_cambiaAIncompletoYPersiste() {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(10L, EstadoCuestionario.ACTIVO, false);
        cuestionario.setFechaCreacion(AHORA.minusHours(1));
        cuestionario.setCantidadAccesos(3); // == maxAccesos

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(10L, 1L)).thenReturn(Optional.of(cuestionario));

        assertThatThrownBy(() -> gestorCuestionario.iniciarCuestionario(10L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("accesos");

        assertThat(cuestionario.getEstado()).isEqualTo(EstadoCuestionario.INCOMPLETO);
        assertThat(cuestionario.getFechaCambioEstado()).isEqualTo(AHORA);
        assertThat(cuestionario.getCantidadAccesos()).isEqualTo(3);
        verify(cuestionarioDAO).save(cuestionario);
    }

    @Test
    @DisplayName("CU 026: estados finales (COMPLETO, INCOMPLETO, SIN_CONTESTAR) lanzan 422 sin cambiar estado ni persistir")
    void estadosFinales_lanzan422SinCambiarEstado() {
        for (EstadoCuestionario estadoFinal : List.of(
                EstadoCuestionario.COMPLETO,
                EstadoCuestionario.INCOMPLETO,
                EstadoCuestionario.SIN_CONTESTAR
        )) {
            Cuestionario cuestionario = crearCuestionarioBase(11L, estadoFinal);
            when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(11L, 1L)).thenReturn(Optional.of(cuestionario));

            assertThatThrownBy(() -> gestorCuestionario.obtenerInstrucciones(11L))
                    .isInstanceOf(ReglaNegocioException.class);
            assertThat(cuestionario.getEstado()).isEqualTo(estadoFinal);
        }

        verify(cuestionarioDAO, never()).save(any());
        verify(cuestionarioDAO, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("CU 026 Seguridad: el DTO de respuesta nunca incluye ponderaciones de opciones")
    void iniciarCuestionario_dtoNuncaExponePonderaciones() throws Exception {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(6L, EstadoCuestionario.ACTIVO, false);
        cuestionario.setFechaCreacion(AHORA.minusHours(1));

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(6L, 1L)).thenReturn(Optional.of(cuestionario));
        when(cuestionarioDAO.saveAndFlush(any(Cuestionario.class))).thenAnswer(inv -> inv.getArgument(0));

        CuestionarioIniciadoDTO resultado = gestorCuestionario.iniciarCuestionario(6L);

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(resultado);

        assertThat(json).doesNotContainIgnoringCase("ponderacion");
        assertThat(resultado.bloqueActual().preguntas().get(0).opciones())
                .allSatisfy(op -> {
                    assertThat(op.id()).isNotNull();
                    assertThat(op.orden()).isNotNull();
                    assertThat(op.descripcion()).isNotBlank();
                });
    }

    // =========================================================================
    // Tests de CU 026 Parte 2: Responder bloques, validación, avance y cierre
    // =========================================================================

    @Test
    @DisplayName("CU 026 Flujo 8.A: bloque correcto guarda respuestas, marca bloque respondido y devuelve bloqueSiguiente sin incrementar accesos")
    void responderBloque_bloqueCorrectoConMasBloques_avanzaAlSiguiente() throws Exception {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(6L, EstadoCuestionario.EN_PROCESO, false);
        cuestionario.setFechaCreacion(AHORA.minusHours(2));
        cuestionario.setFechaInicio(AHORA.minusMinutes(10));
        cuestionario.setCantidadAccesos(1);

        ResponderBloqueRequestDTO request = new ResponderBloqueRequestDTO(List.of(
                new RespuestaItemDTO(1001L, 12L),
                new RespuestaItemDTO(1002L, 21L),
                new RespuestaItemDTO(1003L, 32L)
        ));

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(6L, 1L)).thenReturn(Optional.of(cuestionario));
        when(cuestionarioDAO.save(any(Cuestionario.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponderBloqueResponseDTO respuesta = gestorCuestionario.responderBloque(6L, 1, request);

        assertThat(respuesta.completado()).isFalse();
        assertThat(respuesta.estado()).isNull();
        assertThat(respuesta.mensaje()).isNull();
        assertThat(respuesta.segundosRestantes()).isEqualTo(50L * 60L);
        assertThat(respuesta.bloqueSiguiente()).isNotNull();
        assertThat(respuesta.bloqueSiguiente().numero()).isEqualTo(2);
        assertThat(respuesta.bloqueSiguiente().completado()).isFalse();
        assertThat(respuesta.bloqueSiguiente().preguntas()).hasSize(3);

        BloqueCuestionario bloque1 = cuestionario.getBloques().get(0);
        assertThat(bloque1.getRespondido()).isTrue();
        assertThat(bloque1.getFechaRespuesta()).isEqualTo(AHORA);
        assertThat(bloque1.getPreguntasAsignadas()).allSatisfy(pa -> {
            assertThat(pa.getRespuestaCandidato()).isNotNull();
            assertThat(pa.getRespuestaCandidato().getFechaRespuesta()).isEqualTo(AHORA);
        });
        assertThat(cuestionario.getUltimoBloqueRespondido()).isEqualTo(1);
        assertThat(cuestionario.getCantidadAccesos()).isEqualTo(1); // NO incrementa accesos
        assertThat(cuestionario.getEstado()).isEqualTo(EstadoCuestionario.EN_PROCESO);

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        assertThat(mapper.writeValueAsString(respuesta)).doesNotContainIgnoringCase("ponderacion");

        verify(cuestionarioDAO).save(cuestionario);
        verify(resultadoDAO, never()).save(any());
    }

    @Test
    @DisplayName("CU 026 Flujo 6.A: preguntas sin responder lanza ValidacionException (400) con detalle por pregunta y sin guardar nada")
    void responderBloque_preguntasSinResponder_lanza400SinGuardarNada() {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(6L, EstadoCuestionario.EN_PROCESO, false);
        cuestionario.setFechaCreacion(AHORA.minusHours(2));
        cuestionario.setFechaInicio(AHORA.minusMinutes(10));
        cuestionario.setCantidadAccesos(1);

        // Solo responde pregunta 1001L; deja 1002L con opcionId null y omite 1003L
        ResponderBloqueRequestDTO request = new ResponderBloqueRequestDTO(List.of(
                new RespuestaItemDTO(1001L, 12L),
                new RespuestaItemDTO(1002L, null)
        ));

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(6L, 1L)).thenReturn(Optional.of(cuestionario));

        assertThatThrownBy(() -> gestorCuestionario.responderBloque(6L, 1, request))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> {
                    ValidacionException ve = (ValidacionException) ex;
                    assertThat(ve.getCodigo()).isEqualTo("VALIDACION");
                    assertThat(ve.getDetalles()).hasSize(2);
                    assertThat(ve.getDetalles())
                            .extracting(ValidacionException.DetalleError::campo)
                            .containsExactly("1002", "1003");
                });

        BloqueCuestionario bloque1 = cuestionario.getBloques().get(0);
        assertThat(bloque1.getRespondido()).isFalse();
        assertThat(bloque1.getPreguntasAsignadas()).allSatisfy(pa -> assertThat(pa.getRespuestaCandidato()).isNull());
        verify(cuestionarioDAO, never()).save(any());
        verify(resultadoDAO, never()).save(any());
    }

    @Test
    @DisplayName("CU 026: opción que no pertenece a la pregunta lanza ReglaNegocioException (422) sin guardar nada")
    void responderBloque_opcionAjenaALaPregunta_lanza422() {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(6L, EstadoCuestionario.EN_PROCESO, false);
        cuestionario.setFechaInicio(AHORA.minusMinutes(10));

        // Pregunta 1001L tiene opciones 11L y 12L; enviamos 21L (de pregunta 2)
        ResponderBloqueRequestDTO request = new ResponderBloqueRequestDTO(List.of(
                new RespuestaItemDTO(1001L, 21L),
                new RespuestaItemDTO(1002L, 21L),
                new RespuestaItemDTO(1003L, 32L)
        ));

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(6L, 1L)).thenReturn(Optional.of(cuestionario));

        assertThatThrownBy(() -> gestorCuestionario.responderBloque(6L, 1, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("opción");

        assertThat(cuestionario.getBloques().get(0).getRespondido()).isFalse();
        verify(cuestionarioDAO, never()).save(any());
    }

    @Test
    @DisplayName("CU 026: preguntaAsignadaId que no pertenece al bloque lanza ReglaNegocioException (422)")
    void responderBloque_preguntaAjenaAlBloque_lanza422() {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(6L, EstadoCuestionario.EN_PROCESO, false);
        cuestionario.setFechaInicio(AHORA.minusMinutes(10));

        // 1004L pertenece al bloque 2, no al bloque 1
        ResponderBloqueRequestDTO request = new ResponderBloqueRequestDTO(List.of(
                new RespuestaItemDTO(1001L, 12L),
                new RespuestaItemDTO(1002L, 21L),
                new RespuestaItemDTO(1004L, 42L)
        ));

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(6L, 1L)).thenReturn(Optional.of(cuestionario));

        assertThatThrownBy(() -> gestorCuestionario.responderBloque(6L, 1, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no pertenece al bloque");

        verify(cuestionarioDAO, never()).save(any());
    }

    @Test
    @DisplayName("CU 026: bloque ya completado o que no es el actual lanza 422; bloque inexistente lanza 404")
    void responderBloque_bloqueNoActualYaCompletadoOInexistente() {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(7L, EstadoCuestionario.EN_PROCESO, true);
        cuestionario.setFechaInicio(AHORA.minusMinutes(10));

        // Bloque 1 ya completado -> 422
        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(7L, 1L)).thenReturn(Optional.of(cuestionario));
        ResponderBloqueRequestDTO vacio = new ResponderBloqueRequestDTO(List.of());

        assertThatThrownBy(() -> gestorCuestionario.responderBloque(7L, 1, vacio))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya fue respondido");

        // Bloque 99 inexistente -> 404
        assertThatThrownBy(() -> gestorCuestionario.responderBloque(7L, 99, vacio))
                .isInstanceOf(RecursoNoEncontradoException.class);

        // Bloque 2 cuando bloque 1 aún está pendiente -> 422
        Cuestionario cPendiente = crearCuestionarioConDosBloques(6L, EstadoCuestionario.EN_PROCESO, false);
        cPendiente.setFechaInicio(AHORA.minusMinutes(10));
        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(6L, 1L)).thenReturn(Optional.of(cPendiente));

        assertThatThrownBy(() -> gestorCuestionario.responderBloque(6L, 2, vacio))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no es el bloque actual");

        verify(cuestionarioDAO, never()).save(any());
    }

    @Test
    @DisplayName("CU 026 Flujo 1.A al responder bloque: tiempo agotado persiste estado INCOMPLETO y lanza 422")
    void responderBloque_tiempoAgotado_persisteIncompletoYLanza422() {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(9L, EstadoCuestionario.EN_PROCESO, false);
        cuestionario.setFechaCreacion(AHORA.minusHours(3));
        cuestionario.setFechaInicio(AHORA.minusMinutes(61));
        cuestionario.setCantidadAccesos(1);

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(9L, 1L)).thenReturn(Optional.of(cuestionario));

        assertThatThrownBy(() -> gestorCuestionario.responderBloque(9L, 1, new ResponderBloqueRequestDTO(List.of())))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("tiempo máximo");

        assertThat(cuestionario.getEstado()).isEqualTo(EstadoCuestionario.INCOMPLETO);
        assertThat(cuestionario.getFechaCambioEstado()).isEqualTo(AHORA);
        assertThat(cuestionario.getTiempoRestanteSegundos()).isZero();
        assertThat(cuestionario.getCantidadAccesos()).isEqualTo(1);
        verify(cuestionarioDAO).save(cuestionario);
        verify(resultadoDAO, never()).save(any());
    }

    @Test
    @DisplayName("CU 026: cuestionario en estado ACTIVO (no EN_PROCESO) lanza 422 al responder bloque")
    void responderBloque_cuestionarioNoEnProceso_lanza422() {
        Cuestionario cuestionario = crearCuestionarioConDosBloques(6L, EstadoCuestionario.ACTIVO, false);
        cuestionario.setFechaCreacion(AHORA.minusHours(1));

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(6L, 1L)).thenReturn(Optional.of(cuestionario));

        assertThatThrownBy(() -> gestorCuestionario.responderBloque(6L, 1, new ResponderBloqueRequestDTO(List.of())))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("EN_PROCESO");

        verify(cuestionarioDAO, never()).save(any());
    }

    @Test
    @DisplayName("CU 026: cuestionario ajeno al responder bloque lanza 404 (RecursoNoEncontradoException)")
    void responderBloque_cuestionarioAjeno_lanza404() {
        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(2L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestorCuestionario.responderBloque(2L, 1, new ResponderBloqueRequestDTO(List.of())))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("CU 026 Paso 8: último bloque calcula y guarda resultados, pasa a COMPLETO y devuelve respuesta sin puntajes")
    void responderBloque_ultimoBloque_cierraConCompletoYGuardaResultados() throws Exception {
        Competencia comp = new Competencia(5L, "TEC-01", "COMPETENCIAS TÉCNICAS", "DESC");
        Factor factor = new Factor(3L, "FAC-03", "DESARROLLO", "DESC", 1, comp);

        Cuestionario cuestionario = crearCuestionarioBase(12L, EstadoCuestionario.EN_PROCESO);
        cuestionario.getPuesto().addCaracteristica(new CaracteristicaPuesto(5L, cuestionario.getPuesto(), comp, 9.5));
        cuestionario.setFechaCreacion(AHORA.minusHours(1));
        cuestionario.setFechaInicio(AHORA.minusMinutes(10));
        cuestionario.setCantidadAccesos(1);

        BloqueCuestionario unicoBloque = new BloqueCuestionario(1);
        unicoBloque.setId(501L);
        unicoBloque.addPreguntaAsignada(crearPreguntaAsignada(2001L, crearPreguntaConOpciones(1L, "P1", "E1", factor), 1));
        unicoBloque.addPreguntaAsignada(crearPreguntaAsignada(2002L, crearPreguntaConOpciones(2L, "P2", "E2", factor), 2));
        cuestionario.addBloque(unicoBloque);

        // Opciones: 12L -> pond 8.0, 21L -> pond 2.0 => totalFactor = (8 + 2) / 2 = 5.00
        ResponderBloqueRequestDTO request = new ResponderBloqueRequestDTO(List.of(
                new RespuestaItemDTO(2001L, 12L),
                new RespuestaItemDTO(2002L, 21L)
        ));

        when(cuestionarioDAO.findByIdAndCandidatoIdConPuesto(12L, 1L)).thenReturn(Optional.of(cuestionario));
        when(cuestionarioDAO.save(any(Cuestionario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(resultadoDAO.save(any(Resultado.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponderBloqueResponseDTO respuesta = gestorCuestionario.responderBloque(12L, 1, request);

        assertThat(respuesta.completado()).isTrue();
        assertThat(respuesta.estado()).isEqualTo(EstadoCuestionario.COMPLETO);
        assertThat(respuesta.mensaje()).isEqualTo("El cuestionario ha sido completado con éxito");
        assertThat(respuesta.bloqueSiguiente()).isNull();
        assertThat(respuesta.segundosRestantes()).isNull();

        assertThat(cuestionario.getEstado()).isEqualTo(EstadoCuestionario.COMPLETO);
        assertThat(cuestionario.getFechaFinalizacion()).isEqualTo(AHORA);
        assertThat(cuestionario.getFechaCambioEstado()).isEqualTo(AHORA);
        assertThat(cuestionario.getTiempoRestanteSegundos()).isZero();
        assertThat(cuestionario.getUltimoBloqueRespondido()).isEqualTo(1);
        assertThat(cuestionario.getCantidadAccesos()).isEqualTo(1);

        ArgumentCaptor<Resultado> resultadoCaptor = ArgumentCaptor.forClass(Resultado.class);
        verify(resultadoDAO).save(resultadoCaptor.capture());
        Resultado guardado = resultadoCaptor.getValue();
        assertThat(guardado.getFechaCalculo()).isEqualTo(AHORA);
        assertThat(guardado.getPuntajeObtenido()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(guardado.getResultadosCompetencias()).hasSize(1);
        assertThat(guardado.getResultadosCompetencias().get(0).getTotalCompetencia())
                .isEqualByComparingTo(new BigDecimal("5.00"));

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(respuesta);
        assertThat(json).doesNotContainIgnoringCase("puntaje");
        assertThat(json).doesNotContainIgnoringCase("totalFactor");
        assertThat(json).doesNotContainIgnoringCase("totalCompetencia");
    }

    // =========================================================================
    // Helpers de construcción de fixtures
    // =========================================================================

    private Cuestionario crearCuestionarioBase(Long id, EstadoCuestionario estado) {
        Candidato candidato = new Candidato(1L, "1", TipoDocumento.DNI, "12345678", "PÉREZ", "JUAN", "juan@example.com");
        Puesto puesto = new Puesto(3L, "ARQ-01", "ARQUITECTO DE SOFTWARE", "DESC", "INNOVATECH SA");

        Cuestionario c = new Cuestionario();
        c.setId(id);
        c.setCandidato(candidato);
        c.setPuesto(puesto);
        c.setEstado(estado);
        c.setClaveAcceso("CU26TEST");
        c.setTiempoMaximoMinutos(60);
        c.setCantidadPreguntasPorBloque(3);
        c.setCantidadAccesos(0);
        c.setEliminado(false);
        return c;
    }

    private Cuestionario crearCuestionarioConDosBloques(Long id, EstadoCuestionario estado, boolean primerBloqueRespondido) {
        Cuestionario c = crearCuestionarioBase(id, estado);
        Competencia comp = new Competencia(5L, "TEC-01", "COMPETENCIAS TÉCNICAS", "DESC");
        Factor factor = new Factor(3L, "FAC-03", "DESARROLLO", "DESC", 1, comp);

        BloqueCuestionario b1 = new BloqueCuestionario(1);
        b1.setId(101L);
        b1.setRespondido(primerBloqueRespondido);
        b1.addPreguntaAsignada(crearPreguntaAsignada(1001L, crearPreguntaConOpciones(1L, "P1", "ENUNCIADO 1", factor), 1));
        b1.addPreguntaAsignada(crearPreguntaAsignada(1002L, crearPreguntaConOpciones(2L, "P2", "ENUNCIADO 2", factor), 2));
        b1.addPreguntaAsignada(crearPreguntaAsignada(1003L, crearPreguntaConOpciones(3L, "P3", "ENUNCIADO 3", factor), 3));

        BloqueCuestionario b2 = new BloqueCuestionario(2);
        b2.setId(102L);
        b2.setRespondido(false);
        b2.addPreguntaAsignada(crearPreguntaAsignada(1004L, crearPreguntaConOpciones(4L, "P4", "ENUNCIADO 4", factor), 4));
        b2.addPreguntaAsignada(crearPreguntaAsignada(1005L, crearPreguntaConOpciones(5L, "P5", "ENUNCIADO 5", factor), 5));
        b2.addPreguntaAsignada(crearPreguntaAsignada(1006L, crearPreguntaConOpciones(6L, "P6", "ENUNCIADO 6", factor), 6));

        c.addBloque(b1);
        c.addBloque(b2);
        return c;
    }

    private PreguntaAsignada crearPreguntaAsignada(Long id, Pregunta pregunta, int nroOrden) {
        PreguntaAsignada pa = new PreguntaAsignada(pregunta, nroOrden);
        pa.setId(id);
        return pa;
    }

    private Pregunta crearPreguntaConOpciones(Long id, String nombre, String enunciado, Factor factor) {
        Pregunta p = new Pregunta(id, nombre, enunciado, "DESC", factor);
        p.addOpcion(new OpcionRespuesta(id * 10 + 1, 1, "OP 1", "DESCRIPCIÓN OPCIÓN 1", 2.0));
        p.addOpcion(new OpcionRespuesta(id * 10 + 2, 2, "OP 2", "DESCRIPCIÓN OPCIÓN 2", 8.0));
        return p;
    }
}
