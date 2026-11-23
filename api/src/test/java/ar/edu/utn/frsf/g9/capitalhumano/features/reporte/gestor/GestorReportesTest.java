package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.EvaluacionOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.PuestoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.PuestoReporteDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.ReporteOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.TotalesReporteDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.strategy.FabricaGeneradorReporte;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.strategy.IGeneradorReporte;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ValidacionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios con JUnit 5 y Mockito para {@link GestorReportes} (CU 028).
 */
@ExtendWith(MockitoExtension.class)
class GestorReportesTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 23, 19, 0, 0);
    private static final Clock CLOCK_FIJO = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);

    @Mock
    private PuestoDAO puestoDAO;

    @Mock
    private EvaluacionDAO evaluacionDAO;

    @Mock
    private FabricaGeneradorReporte fabricaGeneradorReporte;

    private GestorReportes gestorReportes;

    @BeforeEach
    void setUp() {
        gestorReportes = new GestorReportes(puestoDAO, evaluacionDAO, fabricaGeneradorReporte, CLOCK_FIJO);
    }

    @Test
    @DisplayName("buscarPuestos sin criterios (nulos o espacios) lanza ValidacionException (400 VALIDACION)")
    void buscarPuestos_sinCriterios_lanza400() {
        assertThatThrownBy(() -> gestorReportes.buscarPuestos(null, "   ", ""))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("al menos un criterio");

        verify(puestoDAO, never()).buscarActivosPorCriterios(any(), any(), any());
    }

    @Test
    @DisplayName("buscarPuestos normaliza con trim + toUpperCase y devuelve lista vacía cuando no hay coincidencias")
    void buscarPuestos_normalizaYSinCoincidencias() {
        when(puestoDAO.buscarActivosPorCriterios("ING", "DATOS", null)).thenReturn(List.of());

        List<PuestoOrdenMeritoDTO> resultado = gestorReportes.buscarPuestos("  ing ", " datos  ", "   ");

        assertThat(resultado).isEmpty();
        verify(puestoDAO).buscarActivosPorCriterios("ING", "DATOS", null);
        verify(evaluacionDAO, never()).findResumenPorPuestoIds(anyCollection(), any());
    }

    @Test
    @DisplayName("buscarPuestos calcula cantidadCandidatos, evaluacionesCompletas y evaluacionesTotal")
    void buscarPuestos_conCoincidenciasYMetricas() {
        Puesto puestoA = new Puesto(4L, "ING-DATA-01", "INGENIERO DE DATOS", "DESC", "DATA CORP SA");
        Puesto puestoB = new Puesto(5L, "ING-CLOUD-01", "INGENIERO DE PLATAFORMA CLOUD", "DESC", "NUBE SUR SRL");

        when(puestoDAO.buscarActivosPorCriterios(null, "INGENIERO", null)).thenReturn(List.of(puestoA, puestoB));
        when(evaluacionDAO.findResumenPorPuestoIds(List.of(4L, 5L), EstadoCuestionario.COMPLETO))
                .thenReturn(List.of(
                        proyeccion(4L, 4L, AHORA.minusDays(2), 7L, 3L), // incompleta
                        proyeccion(3L, 4L, AHORA.minusDays(10), 3L, 3L), // completa
                        proyeccion(9L, 4L, AHORA.minusDays(15), 0L, 0L)  // 0 candidatos -> NO cuenta como completa
                ));

        List<PuestoOrdenMeritoDTO> resultado = gestorReportes.buscarPuestos(null, "  ingeniero ", null);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).puestoId()).isEqualTo(4L);
        assertThat(resultado.get(0).cantidadCandidatos()).isEqualTo(10L);
        assertThat(resultado.get(0).evaluacionesCompletas()).isEqualTo(1L);
        assertThat(resultado.get(0).evaluacionesTotal()).isEqualTo(3L);

        assertThat(resultado.get(1).puestoId()).isEqualTo(5L);
        assertThat(resultado.get(1).cantidadCandidatos()).isZero();
        assertThat(resultado.get(1).evaluacionesCompletas()).isZero();
        assertThat(resultado.get(1).evaluacionesTotal()).isZero();
    }

    @Test
    @DisplayName("listarEvaluacionesPorPuesto retorna 200 con evaluaciones ordenadas más recientes primero")
    void listarEvaluacionesPorPuesto_200() {
        when(puestoDAO.existsByIdAndEliminadoFalse(4L)).thenReturn(true);
        when(evaluacionDAO.findResumenPorPuestoIds(List.of(4L), EstadoCuestionario.COMPLETO))
                .thenReturn(List.of(
                        proyeccion(4L, 4L, AHORA.minusDays(2), 7L, 3L),
                        proyeccion(3L, 4L, AHORA.minusDays(10), 3L, 3L)
                ));

        List<EvaluacionOrdenMeritoDTO> evals = gestorReportes.listarEvaluacionesPorPuesto(4L);

        assertThat(evals).hasSize(2);
        assertThat(evals.get(0).evaluacionId()).isEqualTo(4L);
        assertThat(evals.get(0).cantidadCandidatos()).isEqualTo(7L);
        assertThat(evals.get(0).cantidadCompletos()).isEqualTo(3L);
        assertThat(evals.get(1).evaluacionId()).isEqualTo(3L);
        assertThat(evals.get(1).cantidadCompletos()).isEqualTo(3L);
    }

    @Test
    @DisplayName("listarEvaluacionesPorPuesto lanza 404 si el puesto no existe")
    void listarEvaluacionesPorPuesto_404() {
        when(puestoDAO.existsByIdAndEliminadoFalse(999L)).thenReturn(false);

        assertThatThrownBy(() -> gestorReportes.listarEvaluacionesPorPuesto(999L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("listarEvaluacionesPorPuesto lanza 422 (Flujo 6.A) si el puesto no tiene evaluaciones")
    void listarEvaluacionesPorPuesto_422_flujo6A() {
        when(puestoDAO.existsByIdAndEliminadoFalse(5L)).thenReturn(true);
        when(evaluacionDAO.findResumenPorPuestoIds(List.of(5L), EstadoCuestionario.COMPLETO))
                .thenReturn(List.of());

        assertThatThrownBy(() -> gestorReportes.listarEvaluacionesPorPuesto(5L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no posee evaluaciones");
    }

    @Test
    @DisplayName("emitirOrdenDeMerito con evaluacionIds ausente delega en la estrategia con TODAS las evaluaciones y usa Clock")
    @SuppressWarnings("unchecked")
    void emitirOrdenDeMerito_todasLasEvaluaciones_delegaEnEstrategia() {
        Puesto puesto = new Puesto(4L, "ING-DATA-01", "INGENIERO DE DATOS", "DESC", "DATA CORP SA");
        Evaluacion e4 = crearEvaluacion(4L, puesto, AHORA.minusDays(2));
        Evaluacion e3 = crearEvaluacion(3L, puesto, AHORA.minusDays(10));

        IGeneradorReporte<ReporteOrdenMeritoDTO> estrategiaMock = mock(IGeneradorReporte.class);
        ReporteOrdenMeritoDTO dtoEsperado = crearReporteVacio(puesto);

        when(puestoDAO.findByIdActivaConCaracteristicas(4L)).thenReturn(Optional.of(puesto));
        when(evaluacionDAO.findByPuestoIdOrderByFechaCreacionDescIdDesc(4L)).thenReturn(List.of(e4, e3));
        when(fabricaGeneradorReporte.crearReporteOrdenMerito(puesto, List.of(e4, e3), AHORA))
                .thenReturn(estrategiaMock);
        when(estrategiaMock.generarSalida()).thenReturn(dtoEsperado);

        ReporteOrdenMeritoDTO resultado = gestorReportes.emitirOrdenDeMerito(4L, null);

        assertThat(resultado).isSameAs(dtoEsperado);
        InOrder orden = inOrder(estrategiaMock);
        orden.verify(estrategiaMock).recopilarDatos();
        orden.verify(estrategiaMock).generarSalida();
    }

    @Test
    @DisplayName("emitirOrdenDeMerito con subconjunto filtra únicamente las evaluaciones seleccionadas")
    @SuppressWarnings("unchecked")
    void emitirOrdenDeMerito_subconjuntoEvaluaciones() {
        Puesto puesto = new Puesto(4L, "ING-DATA-01", "INGENIERO DE DATOS", "DESC", "DATA CORP SA");
        Evaluacion e4 = crearEvaluacion(4L, puesto, AHORA.minusDays(2));
        Evaluacion e3 = crearEvaluacion(3L, puesto, AHORA.minusDays(10));

        IGeneradorReporte<ReporteOrdenMeritoDTO> estrategiaMock = mock(IGeneradorReporte.class);
        ReporteOrdenMeritoDTO dtoEsperado = crearReporteVacio(puesto);

        when(puestoDAO.findByIdActivaConCaracteristicas(4L)).thenReturn(Optional.of(puesto));
        when(evaluacionDAO.findByPuestoIdOrderByFechaCreacionDescIdDesc(4L)).thenReturn(List.of(e4, e3));
        when(fabricaGeneradorReporte.crearReporteOrdenMerito(puesto, List.of(e3), AHORA))
                .thenReturn(estrategiaMock);
        when(estrategiaMock.generarSalida()).thenReturn(dtoEsperado);

        ReporteOrdenMeritoDTO resultado = gestorReportes.emitirOrdenDeMerito(4L, List.of(3L));

        assertThat(resultado).isSameAs(dtoEsperado);
        verify(fabricaGeneradorReporte).crearReporteOrdenMerito(puesto, List.of(e3), AHORA);
        verify(estrategiaMock).recopilarDatos();
        verify(estrategiaMock).generarSalida();
    }

    @Test
    @DisplayName("emitirOrdenDeMerito lanza 404 si el puesto no existe")
    void emitirOrdenDeMerito_404_puestoInexistente() {
        when(puestoDAO.findByIdActivaConCaracteristicas(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestorReportes.emitirOrdenDeMerito(999L, null))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("emitirOrdenDeMerito lanza 422 (Flujo 6.A) si el puesto no posee evaluaciones")
    void emitirOrdenDeMerito_422_sinEvaluaciones() {
        Puesto puesto = new Puesto(5L, "ING-CLOUD-01", "INGENIERO DE PLATAFORMA CLOUD", "DESC", "NUBE SUR SRL");
        when(puestoDAO.findByIdActivaConCaracteristicas(5L)).thenReturn(Optional.of(puesto));
        when(evaluacionDAO.findByPuestoIdOrderByFechaCreacionDescIdDesc(5L)).thenReturn(List.of());

        assertThatThrownBy(() -> gestorReportes.emitirOrdenDeMerito(5L, null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no posee evaluaciones");
    }

    @Test
    @DisplayName("emitirOrdenDeMerito lanza 422 si alguna evaluación indicada no pertenece al puesto")
    void emitirOrdenDeMerito_422_evaluacionAjena() {
        Puesto puesto = new Puesto(4L, "ING-DATA-01", "INGENIERO DE DATOS", "DESC", "DATA CORP SA");
        Evaluacion e3 = crearEvaluacion(3L, puesto, AHORA.minusDays(10));

        when(puestoDAO.findByIdActivaConCaracteristicas(4L)).thenReturn(Optional.of(puesto));
        when(evaluacionDAO.findByPuestoIdOrderByFechaCreacionDescIdDesc(4L)).thenReturn(List.of(e3));

        assertThatThrownBy(() -> gestorReportes.emitirOrdenDeMerito(4L, List.of(3L, 1L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no pertenecen al puesto")
                .satisfies(ex -> {
                    ReglaNegocioException re = (ReglaNegocioException) ex;
                    assertThat(re.getDetalles()).hasSize(1);
                    assertThat(re.getDetalles().get(0).mensaje()).contains("1");
                });
    }

    @Test
    @DisplayName("Patrón Strategy: GestorReportes puede ejecutar cualquier otra estrategia IGeneradorReporte sin modificarse")
    void patronStrategy_extensibleAOtraEstrategia() {
        IGeneradorReporte<String> estrategiaAlternativa = new IGeneradorReporte<>() {
            private String salida;

            @Override
            public void recopilarDatos() {
                this.salida = "REPORTE_COMPARATIVO_OK";
            }

            @Override
            public String generarSalida() {
                return salida;
            }
        };

        gestorReportes.setEstrategia(estrategiaAlternativa);
        String resultado = gestorReportes.emitirReporte();

        assertThat(resultado).isEqualTo("REPORTE_COMPARATIVO_OK");
    }

    private Evaluacion crearEvaluacion(Long id, Puesto puesto, LocalDateTime fecha) {
        Evaluacion e = new Evaluacion(puesto, fecha, 1L, 60, 5);
        e.setId(id);
        return e;
    }

    private ReporteOrdenMeritoDTO crearReporteVacio(Puesto puesto) {
        return new ReporteOrdenMeritoDTO(
                new PuestoReporteDTO(puesto.getId(), puesto.getCodigo(), puesto.getNombre(), puesto.getEmpresa()),
                List.of(),
                AHORA,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new TotalesReporteDTO(0, 0, 0, 0, 0, 0, 0)
        );
    }

    private EvaluacionDAO.ResumenEvaluacionProjection proyeccion(Long evalId,
                                                                 Long puestoId,
                                                                 LocalDateTime fecha,
                                                                 Long candidatos,
                                                                 Long completos) {
        return new EvaluacionDAO.ResumenEvaluacionProjection() {
            @Override
            public Long getEvaluacionId() {
                return evalId;
            }

            @Override
            public Long getPuestoId() {
                return puestoId;
            }

            @Override
            public LocalDateTime getFechaCreacion() {
                return fecha;
            }

            @Override
            public Long getCantidadCandidatos() {
                return candidatos;
            }

            @Override
            public Long getCantidadCompletos() {
                return completos;
            }
        };
    }
}
