package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dao.CandidatoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto.CandidatoResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GeneradorClaveAcceso;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CrearEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.DetallePuestoEvaluacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluacionCreadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PrevisualizacionEvaluacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PrevisualizarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ValidacionCandidatosResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.gestor.ConfiguracionEvaluacionProvider;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.gestor.ExportadorExcelEvaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.ContextoSeguridad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios con JUnit 5 y Mockito para {@link GestorCandidatos} (CU 025).
 */
@ExtendWith(MockitoExtension.class)
class GestorCandidatosTest {

    @Mock
    private CandidatoDAO candidatoDAO;

    @Mock
    private PuestoDAO puestoDAO;

    @Mock
    private CuestionarioDAO cuestionarioDAO;

    @Mock
    private EvaluacionDAO evaluacionDAO;

    @Mock
    private FactorDAO factorDAO;

    @Mock
    private ConfiguracionEvaluacionProvider configuracionEvaluacionProvider;

    @Mock
    private ContextoSeguridad contextoSeguridad;

    private GestorCuestionario gestorCuestionario;
    private GeneradorClaveAcceso generadorClaveAcceso;
    private ExportadorExcelEvaluacion exportadorExcelEvaluacion;
    private GestorCandidatos gestorCandidatos;

    @BeforeEach
    void setUp() {
        gestorCuestionario = new GestorCuestionario(factorDAO);
        generadorClaveAcceso = new GeneradorClaveAcceso();
        exportadorExcelEvaluacion = new ExportadorExcelEvaluacion();

        lenient().when(configuracionEvaluacionProvider.getTiempoMaximoMinutos()).thenReturn(60);
        lenient().when(configuracionEvaluacionProvider.getCantidadPreguntasPorBloque()).thenReturn(5);
        lenient().when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);

        gestorCandidatos = new GestorCandidatos(
                candidatoDAO,
                puestoDAO,
                cuestionarioDAO,
                evaluacionDAO,
                gestorCuestionario,
                generadorClaveAcceso,
                configuracionEvaluacionProvider,
                exportadorExcelEvaluacion,
                contextoSeguridad
        );
    }

    @Test
    @DisplayName("buscarCandidatos normaliza filtros (trim + mayúsculas) y convierte blancos a null")
    void buscarCandidatos_normalizaFiltros() {
        Candidato ana = new Candidato(4L, "4", TipoDocumento.DNI, "30456789", "FERNÁNDEZ", "ANA", "ana@example.com");
        when(candidatoDAO.buscarActivosConFiltros("FERN", null, "4")).thenReturn(List.of(ana));

        List<CandidatoResumenDTO> resultado = gestorCandidatos.buscarCandidatos("  fern  ", "   ", " 4 ");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).apellido()).isEqualTo("FERNÁNDEZ");
        verify(candidatoDAO).buscarActivosConFiltros("FERN", null, "4");
    }

    @Test
    @DisplayName("Flujo 6.A: rechaza candidatos con cuestionario ACTIVO o EN_PROCESO detallando cada candidato bloqueado")
    void verificarSinCuestionarioActivo_bloqueaActivosYEnProceso() {
        Candidato juan = new Candidato(1L, "1", TipoDocumento.DNI, "12345678", "PÉREZ", "JUAN", "juan@example.com");
        Candidato maria = new Candidato(2L, "2", TipoDocumento.DNI, "87654321", "GÓMEZ", "MARÍA", "maria@example.com");
        Candidato ana = new Candidato(4L, "4", TipoDocumento.DNI, "30456789", "FERNÁNDEZ", "ANA", "ana@example.com");

        Cuestionario qActivo = new Cuestionario(10L, juan, EstadoCuestionario.ACTIVO, "CLAVE123");
        Cuestionario qEnProceso = new Cuestionario(11L, maria, EstadoCuestionario.EN_PROCESO, "TEST4567");

        when(candidatoDAO.findByIdInAndEliminadoFalse(anyCollection())).thenReturn(List.of(juan, maria, ana));
        when(cuestionarioDAO.findActivosByCandidatoIdInAndEstadoIn(anyCollection(), anyCollection()))
                .thenReturn(List.of(qActivo, qEnProceso));

        assertThatThrownBy(() -> gestorCandidatos.verificarSinCuestionarioActivo(List.of(1L, 2L, 4L)))
                .isInstanceOf(ReglaNegocioException.class)
                .satisfies(ex -> {
                    ReglaNegocioException reglaEx = (ReglaNegocioException) ex;
                    assertThat(reglaEx.getDetalles()).hasSize(2);
                    assertThat(reglaEx.getDetalles().get(0).mensaje())
                            .contains("1", "PÉREZ", "JUAN", "ACTIVO");
                    assertThat(reglaEx.getDetalles().get(1).mensaje())
                            .contains("2", "GÓMEZ", "MARÍA", "EN_PROCESO");
                });
    }

    @Test
    @DisplayName("Flujo 6: aprueba candidatos que no tienen cuestionarios o solo tienen COMPLETO/INCOMPLETO")
    void verificarSinCuestionarioActivo_permiteCandidatosConCompletoOIncompleto() {
        Candidato carlos = new Candidato(3L, "3", TipoDocumento.DNI, "11223344", "LÓPEZ", "CARLOS", "carlos@example.com");
        Candidato sofia = new Candidato(6L, "6", TipoDocumento.PP, "40112233", "RODRÍGUEZ", "SOFÍA", "sofia@example.com");

        when(candidatoDAO.findByIdInAndEliminadoFalse(anyCollection())).thenReturn(List.of(carlos, sofia));
        // La consulta filtra solo ACTIVO y EN_PROCESO, por lo tanto candidatos con COMPLETO/INCOMPLETO no aparecen
        when(cuestionarioDAO.findActivosByCandidatoIdInAndEstadoIn(anyCollection(), anyCollection()))
                .thenReturn(List.of());

        ValidacionCandidatosResponseDTO response = gestorCandidatos.verificarSinCuestionarioActivo(List.of(3L, 6L));

        assertThat(response.valido()).isTrue();
        assertThat(response.cantidadCandidatos()).isEqualTo(2);
        assertThat(response.candidatos()).extracting(CandidatoResumenDTO::id).containsExactly(3L, 6L);
    }

    @Test
    @DisplayName("Flujo 9.A: rechaza puesto con competencias sin factores activos con al menos 2 preguntas")
    void obtenerDetallePuesto_flujo9A_lanza422ConNombresExactos() {
        Competencia compValida = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Competencia compInvalida = new Competencia(4L, "RESOL-01", "RESOLUCIÓN DE PROBLEMAS", "DESC");
        Puesto puesto = new Puesto(2L, "QA-01", "ANALISTA QA", "DESC", "TECH SOLUTIONS");
        puesto.addCaracteristica(new CaracteristicaPuesto(1L, puesto, compValida, 7.5));
        puesto.addCaracteristica(new CaracteristicaPuesto(2L, puesto, compInvalida, 8.5));

        Factor fValido = new Factor(1L, "FAC-01", "ORAL", "D", 1, compValida);
        fValido.addPregunta(new Pregunta(1L, "P1", "E1", "D", fValido));
        fValido.addPregunta(new Pregunta(2L, "P2", "E2", "D", fValido));

        Factor fInvalido = new Factor(5L, "FAC-05", "CAUSA RAÍZ", "D", 1, compInvalida);
        fInvalido.addPregunta(new Pregunta(11L, "P11", "E11", "D", fInvalido)); // Solo 1 pregunta

        when(puestoDAO.findByIdActivaConCaracteristicas(2L)).thenReturn(Optional.of(puesto));
        when(factorDAO.findActivosConPreguntasByCompetenciaIdIn(anyCollection())).thenReturn(List.of(fValido, fInvalido));

        assertThatThrownBy(() -> gestorCandidatos.obtenerDetallePuesto(2L))
                .isInstanceOf(ReglaNegocioException.class)
                .satisfies(ex -> {
                    ReglaNegocioException reglaEx = (ReglaNegocioException) ex;
                    assertThat(reglaEx.getDetalles())
                            .extracting(ReglaNegocioException.DetalleError::mensaje)
                            .containsExactly("RESOLUCIÓN DE PROBLEMAS");
                });
    }

    @Test
    @DisplayName("obtenerDetallePuesto retorna competencias y ponderaciones cuando todas son evaluables")
    void obtenerDetallePuesto_exitoso() {
        Puesto puesto = construirPuestoValidoConFactores();
        List<Factor> factores = construirFactoresValidosParaPuesto(puesto);

        when(puestoDAO.findByIdActivaConCaracteristicas(1L)).thenReturn(Optional.of(puesto));
        when(factorDAO.findActivosConPreguntasByCompetenciaIdIn(anyCollection())).thenReturn(factores);

        DetallePuestoEvaluacionDTO detalle = gestorCandidatos.obtenerDetallePuesto(1L);

        assertThat(detalle.puestoId()).isEqualTo(1L);
        assertThat(detalle.nombre()).isEqualTo("DESARROLLADOR SENIOR");
        assertThat(detalle.competencias()).hasSize(2);
    }

    @Test
    @DisplayName("Paso 11 y Flujo 12.A: previsualizarEvaluacion genera claves sin persistir en BD")
    void previsualizarEvaluacion_noPersisteNada() {
        Puesto puesto = construirPuestoValidoConFactores();
        List<Factor> factores = construirFactoresValidosParaPuesto(puesto);
        Candidato ana = new Candidato(4L, "4", TipoDocumento.DNI, "30456789", "FERNÁNDEZ", "ANA", "ana@example.com");

        when(puestoDAO.findByIdActivaConCaracteristicas(1L)).thenReturn(Optional.of(puesto));
        when(factorDAO.findActivosConPreguntasByCompetenciaIdIn(anyCollection())).thenReturn(factores);
        when(candidatoDAO.findByIdInAndEliminadoFalse(anyCollection())).thenReturn(List.of(ana));
        when(cuestionarioDAO.findActivosByCandidatoIdInAndEstadoIn(anyCollection(), anyCollection())).thenReturn(List.of());

        PrevisualizacionEvaluacionDTO preview = gestorCandidatos.previsualizarEvaluacion(
                new PrevisualizarEvaluacionRequestDTO(1L, List.of(4L))
        );

        assertThat(preview.puestoId()).isEqualTo(1L);
        assertThat(preview.tiempoMaximoMinutos()).isEqualTo(60);
        assertThat(preview.cantidadPreguntasPorBloque()).isEqualTo(5);
        assertThat(preview.candidatos()).hasSize(1);
        assertThat(generadorClaveAcceso.esValida(preview.candidatos().get(0).claveAcceso())).isTrue();

        verify(evaluacionDAO, never()).save(any());
    }

    @Test
    @DisplayName("Flujo principal (Paso 13): evaluarCandidatos crea Evaluación y Cuestionarios ACTIVOS con bloques y 2 preguntas por factor")
    void evaluarCandidatos_flujoPrincipal_persisteAtomicamente() {
        Puesto puesto = construirPuestoValidoConFactores();
        List<Factor> factores = construirFactoresValidosParaPuesto(puesto); // 3 factores válidos -> 6 preguntas -> 2 bloques (5 + 1)
        Candidato ana = new Candidato(4L, "4", TipoDocumento.DNI, "30456789", "FERNÁNDEZ", "ANA", "ana@example.com");
        Candidato lucas = new Candidato(5L, "5", TipoDocumento.DNI, "32987654", "MARTÍNEZ", "LUCAS", "lucas@example.com");

        when(puestoDAO.findByIdActivaConCaracteristicas(1L)).thenReturn(Optional.of(puesto));
        when(factorDAO.findActivosConPreguntasByCompetenciaIdIn(anyCollection())).thenReturn(factores);
        when(candidatoDAO.findByIdInAndEliminadoFalse(anyCollection())).thenReturn(List.of(ana, lucas));
        when(cuestionarioDAO.findActivosByCandidatoIdInAndEstadoIn(anyCollection(), anyCollection())).thenReturn(List.of());
        when(evaluacionDAO.save(any(Evaluacion.class))).thenAnswer(inv -> {
            Evaluacion e = inv.getArgument(0);
            e.setId(99L);
            long qId = 100L;
            for (Cuestionario q : e.getCuestionarios()) {
                q.setId(qId++);
            }
            return e;
        });

        EvaluacionCreadaDTO creada = gestorCandidatos.evaluarCandidatos(
                new CrearEvaluacionRequestDTO(1L, List.of(4L, 5L), Map.of(4L, " a7k9m2p4 "))
        );

        ArgumentCaptor<Evaluacion> evaluacionCaptor = ArgumentCaptor.forClass(Evaluacion.class);
        verify(evaluacionDAO).save(evaluacionCaptor.capture());

        Evaluacion persistida = evaluacionCaptor.getValue();
        assertThat(persistida.getConsultorId()).isEqualTo(1L);
        assertThat(persistida.getPuesto()).isSameAs(puesto);
        assertThat(persistida.getTiempoMaximoMinutos()).isEqualTo(60);
        assertThat(persistida.getCantidadPreguntasPorBloque()).isEqualTo(5);
        assertThat(persistida.getCuestionarios()).hasSize(2);

        for (Cuestionario q : persistida.getCuestionarios()) {
            assertThat(q.getEvaluacion()).isSameAs(persistida);
            assertThat(q.getPuesto()).isSameAs(puesto);
            assertThat(q.getEstado()).isEqualTo(EstadoCuestionario.ACTIVO);
            assertThat(q.getCantidadAccesos()).isZero();
            assertThat(q.getFechaCreacion()).isEqualTo(persistida.getFechaCreacion());
            assertThat(q.getFechaCambioEstado()).isEqualTo(persistida.getFechaCreacion());
            assertThat(q.getFechaInicio()).isNull();
            assertThat(q.getFechaFinalizacion()).isNull();
            assertThat(q.getTiempoMaximoMinutos()).isEqualTo(60);
            assertThat(q.getCantidadPreguntasPorBloque()).isEqualTo(5);
            assertThat(q.getBloques()).hasSize(2);
            assertThat(q.getBloques().get(0).getPreguntasAsignadas()).hasSize(5);
            assertThat(q.getBloques().get(1).getPreguntasAsignadas()).hasSize(1);
            assertThat(q.getTotalPreguntasAsignadas()).isEqualTo(6);
            assertThat(generadorClaveAcceso.esValida(q.getClaveAcceso())).isTrue();
        }

        assertThat(creada.evaluacionId()).isEqualTo(99L);
        assertThat(creada.cuestionarios().get(0).claveAcceso()).isEqualTo("A7K9M2P4");
        assertThat(creada.cuestionarios().get(0).estado()).isEqualTo(EstadoCuestionario.ACTIVO);
        assertThat(creada.cuestionarios().get(0).cantidadBloques()).isEqualTo(2);
        assertThat(creada.cuestionarios().get(0).cantidadPreguntas()).isEqualTo(6);
    }

    @Test
    @DisplayName("exportarEvaluacionExcel lanza 404 si la evaluación no existe")
    void exportarEvaluacionExcel_noEncontrada_lanza404() {
        when(evaluacionDAO.findByIdConCuestionariosYCandidatos(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestorCandidatos.exportarEvaluacionExcel(999L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("exportarEvaluacionExcel genera archivo .xls para una evaluación existente")
    void exportarEvaluacionExcel_exitoso() {
        Puesto puesto = new Puesto(1L, "DEV-01", "DESARROLLADOR SENIOR", "DESC", "TECH");
        Evaluacion evaluacion = new Evaluacion(puesto, LocalDateTime.of(2026, 11, 19, 10, 0), 1L, 60, 5);
        Candidato ana = new Candidato(4L, "4", TipoDocumento.DNI, "30456789", "FERNÁNDEZ", "ANA", "ana@example.com");
        evaluacion.addCuestionario(new Cuestionario(1L, ana, EstadoCuestionario.ACTIVO, "A1B2C3D4"));

        when(evaluacionDAO.findByIdConCuestionariosYCandidatos(1L)).thenReturn(Optional.of(evaluacion));

        ExportadorExcelEvaluacion.ArchivoExcelEvaluacion archivo = gestorCandidatos.exportarEvaluacionExcel(1L);

        assertThat(archivo.nombreArchivo()).isEqualTo("2026-11-19-10-00-DESARROLLADOR SENIOR.xls");
        assertThat(archivo.contenido()).isNotEmpty();
    }

    private Puesto construirPuestoValidoConFactores() {
        Competencia comp1 = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Competencia comp3 = new Competencia(3L, "TRAB-01", "TRABAJO EN EQUIPO", "DESC");
        Puesto puesto = new Puesto(1L, "DEV-01", "DESARROLLADOR SENIOR", "DESC", "TECH SOLUTIONS");
        puesto.addCaracteristica(new CaracteristicaPuesto(1L, puesto, comp1, 8.0));
        puesto.addCaracteristica(new CaracteristicaPuesto(2L, puesto, comp3, 9.0));
        return puesto;
    }

    private List<Factor> construirFactoresValidosParaPuesto(Puesto puesto) {
        Competencia comp1 = puesto.getCaracteristicas().get(0).getCompetencia();
        Competencia comp3 = puesto.getCaracteristicas().get(1).getCompetencia();

        Factor f1 = new Factor(1L, "FAC-01", "ORAL", "D", 1, comp1);
        f1.addPregunta(new Pregunta(1L, "P1", "E1", "D", f1));
        f1.addPregunta(new Pregunta(2L, "P2", "E2", "D", f1));

        Factor f2 = new Factor(2L, "FAC-02", "ESCRITA", "D", 2, comp1);
        f2.addPregunta(new Pregunta(3L, "P3", "E3", "D", f2));
        f2.addPregunta(new Pregunta(4L, "P4", "E4", "D", f2));

        Factor f3 = new Factor(4L, "FAC-04", "COLABORACIÓN", "D", 1, comp3);
        f3.addPregunta(new Pregunta(5L, "P5", "E5", "D", f3));
        f3.addPregunta(new Pregunta(6L, "P6", "E6", "D", f3));
        f3.addPregunta(new Pregunta(7L, "P7", "E7", "D", f3));

        return List.of(f1, f2, f3);
    }
}
