package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.ResultadoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioIniciadoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.InstruccionesCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.ResponderBloqueRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.ResponderBloqueResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.RespuestaItemDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.BloqueCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.PreguntaAsignada;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Resultado;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.ResultadoCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.ResultadoFactor;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ValidacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración reales contra H2 y {@code seed.sql} para el CU 026 Parte 1.
 *
 * <p>Intencionalmente SIN {@code @Transactional} a nivel de clase para verificar que los
 * cambios de estado producidos al lanzar {@link ReglaNegocioException} (flujos 1.A, 1.B y
 * límite de accesos) quedan efectivamente persistidos en base de datos ({@code noRollbackFor}).
 * Cada test que modifica filas del seed las restaura en un bloque {@code finally} para mantener
 * el aislamiento con el resto de la suite.</p>
 */
@SpringBootTest
class CuestionarioIntegrationTest {

    @Autowired
    private GestorCuestionario gestorCuestionario;

    @Autowired
    private CuestionarioDAO cuestionarioDAO;

    @Autowired
    private ResultadoDAO resultadoDAO;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("Seed (a) ID 6: instrucciones no incrementa accesos; iniciar pasa a EN_PROCESO y devuelve bloque 1")
    void seedA_instruccionesYIniciar() {
        Cuestionario original = cuestionarioDAO.findById(6L).orElseThrow();
        EstadoCuestionario estadoOrig = original.getEstado();
        LocalDateTime fechaInicioOrig = original.getFechaInicio();
        LocalDateTime fechaUltimoIngresoOrig = original.getFechaUltimoIngreso();
        LocalDateTime fechaCambioOrig = original.getFechaCambioEstado();
        Integer accesosOrig = original.getCantidadAccesos();
        Integer restanteOrig = original.getTiempoRestanteSegundos();

        try {
            InstruccionesCuestionarioDTO instrucciones = gestorCuestionario.obtenerInstrucciones(6L);
            assertThat(instrucciones.id()).isEqualTo(6L);
            assertThat(instrucciones.estado()).isEqualTo(EstadoCuestionario.ACTIVO);
            assertThat(instrucciones.nombrePuesto()).isEqualTo("ARQUITECTO DE SOFTWARE");
            assertThat(instrucciones.tiempoMaximoMinutos()).isEqualTo(60);
            assertThat(instrucciones.instrucciones()).isNotBlank();

            assertThat(cuestionarioDAO.findById(6L).orElseThrow().getCantidadAccesos()).isZero();

            CuestionarioIniciadoDTO iniciado = gestorCuestionario.iniciarCuestionario(6L);
            assertThat(iniciado.estado()).isEqualTo(EstadoCuestionario.EN_PROCESO);
            assertThat(iniciado.totalBloques()).isEqualTo(2);
            assertThat(iniciado.bloqueActual().numero()).isEqualTo(1);
            assertThat(iniciado.bloqueActual().preguntas()).hasSize(3);
            assertThat(iniciado.segundosRestantes()).isBetween(3590L, 3600L);

            Cuestionario persistido = cuestionarioDAO.findById(6L).orElseThrow();
            assertThat(persistido.getEstado()).isEqualTo(EstadoCuestionario.EN_PROCESO);
            assertThat(persistido.getCantidadAccesos()).isEqualTo(1);
            assertThat(persistido.getFechaInicio()).isNotNull();
            assertThat(persistido.getFechaUltimoIngreso()).isEqualTo(persistido.getFechaInicio());
        } finally {
            restaurar(6L, estadoOrig, fechaInicioOrig, fechaUltimoIngresoOrig, fechaCambioOrig, accesosOrig, restanteOrig);
        }
    }

    @Test
    @DisplayName("Seed (b) ID 7: reingreso EN_PROCESO (1.C) devuelve bloque 2 pendiente e incrementa accesos")
    void seedB_reingresoEnProceso() {
        Cuestionario original = cuestionarioDAO.findById(7L).orElseThrow();
        EstadoCuestionario estadoOrig = original.getEstado();
        LocalDateTime fechaInicioOrig = original.getFechaInicio();
        LocalDateTime fechaUltimoIngresoOrig = original.getFechaUltimoIngreso();
        LocalDateTime fechaCambioOrig = original.getFechaCambioEstado();
        Integer accesosOrig = original.getCantidadAccesos();
        Integer restanteOrig = original.getTiempoRestanteSegundos();

        try {
            CuestionarioIniciadoDTO reingreso = gestorCuestionario.iniciarCuestionario(7L);

            assertThat(reingreso.id()).isEqualTo(7L);
            assertThat(reingreso.estado()).isEqualTo(EstadoCuestionario.EN_PROCESO);
            assertThat(reingreso.totalBloques()).isEqualTo(2);
            assertThat(reingreso.bloqueActual().numero()).isEqualTo(2);
            assertThat(reingreso.bloqueActual().completado()).isFalse();
            assertThat(reingreso.bloqueActual().preguntas()).hasSize(3);
            assertThat(reingreso.segundosRestantes()).isPositive();

            Cuestionario persistido = cuestionarioDAO.findById(7L).orElseThrow();
            assertThat(persistido.getCantidadAccesos()).isEqualTo(2);
            assertThat(persistido.getFechaInicio()).isEqualTo(fechaInicioOrig);
            assertThat(persistido.getFechaUltimoIngreso()).isAfter(fechaUltimoIngresoOrig);
        } finally {
            restaurar(7L, estadoOrig, fechaInicioOrig, fechaUltimoIngresoOrig, fechaCambioOrig, accesosOrig, restanteOrig);
        }
    }

    @Test
    @DisplayName("Seed (c) ID 8 — Flujo 1.B: ACTIVO vencido lanza 422 y PERSISTE el cambio a SIN_CONTESTAR en H2")
    void seedC_flujo1B_persisteSinContestarPeseAExcepcion() {
        Cuestionario original = cuestionarioDAO.findById(8L).orElseThrow();
        EstadoCuestionario estadoOrig = original.getEstado();
        LocalDateTime fechaInicioOrig = original.getFechaInicio();
        LocalDateTime fechaCambioOrig = original.getFechaCambioEstado();
        Integer accesosOrig = original.getCantidadAccesos();
        Integer restanteOrig = original.getTiempoRestanteSegundos();

        try {
            assertThatThrownBy(() -> gestorCuestionario.obtenerInstrucciones(8L))
                    .isInstanceOf(ReglaNegocioException.class);

            // Relectura fuera de la transacción del gestor: el cambio DEBE haber hecho commit
            Cuestionario enBase = cuestionarioDAO.findById(8L).orElseThrow();
            assertThat(enBase.getEstado()).isEqualTo(EstadoCuestionario.SIN_CONTESTAR);
            assertThat(enBase.getFechaCambioEstado()).isAfter(fechaCambioOrig);
        } finally {
            restaurar(8L, estadoOrig, fechaInicioOrig, fechaCambioOrig, accesosOrig, restanteOrig);
        }
    }

    @Test
    @DisplayName("Seed (d) ID 9 — Flujo 1.A: EN_PROCESO vencido lanza 422 y PERSISTE el cambio a INCOMPLETO en H2")
    void seedD_flujo1A_persisteIncompletoPeseAExcepcion() {
        Cuestionario original = cuestionarioDAO.findById(9L).orElseThrow();
        EstadoCuestionario estadoOrig = original.getEstado();
        LocalDateTime fechaInicioOrig = original.getFechaInicio();
        LocalDateTime fechaCambioOrig = original.getFechaCambioEstado();
        Integer accesosOrig = original.getCantidadAccesos();
        Integer restanteOrig = original.getTiempoRestanteSegundos();

        try {
            assertThatThrownBy(() -> gestorCuestionario.iniciarCuestionario(9L))
                    .isInstanceOf(ReglaNegocioException.class);

            Cuestionario enBase = cuestionarioDAO.findById(9L).orElseThrow();
            assertThat(enBase.getEstado()).isEqualTo(EstadoCuestionario.INCOMPLETO);
            assertThat(enBase.getFechaCambioEstado()).isAfter(fechaCambioOrig);
        } finally {
            restaurar(9L, estadoOrig, fechaInicioOrig, fechaCambioOrig, accesosOrig, restanteOrig);
        }
    }

    @Test
    @DisplayName("Seed (e) ID 10 — Límite de accesos: lanza 422 y PERSISTE el cambio a INCOMPLETO en H2")
    void seedE_limiteAccesos_persisteIncompletoPeseAExcepcion() {
        Cuestionario original = cuestionarioDAO.findById(10L).orElseThrow();
        EstadoCuestionario estadoOrig = original.getEstado();
        LocalDateTime fechaInicioOrig = original.getFechaInicio();
        LocalDateTime fechaCambioOrig = original.getFechaCambioEstado();
        Integer accesosOrig = original.getCantidadAccesos();
        Integer restanteOrig = original.getTiempoRestanteSegundos();

        try {
            assertThatThrownBy(() -> gestorCuestionario.iniciarCuestionario(10L))
                    .isInstanceOf(ReglaNegocioException.class);

            Cuestionario enBase = cuestionarioDAO.findById(10L).orElseThrow();
            assertThat(enBase.getEstado()).isEqualTo(EstadoCuestionario.INCOMPLETO);
            assertThat(enBase.getFechaCambioEstado()).isAfter(fechaCambioOrig);
        } finally {
            restaurar(10L, estadoOrig, fechaInicioOrig, fechaCambioOrig, accesosOrig, restanteOrig);
        }
    }

    @Test
    @DisplayName("Seed (f) ID 11 (COMPLETO -> 422) y Seed (g) ID 2 (ajeno -> 404)")
    void seedF_y_seedG() {
        assertThatThrownBy(() -> gestorCuestionario.obtenerInstrucciones(11L))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(cuestionarioDAO.findById(11L).orElseThrow().getEstado())
                .isEqualTo(EstadoCuestionario.COMPLETO);

        assertThatThrownBy(() -> gestorCuestionario.obtenerInstrucciones(2L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        assertThatThrownBy(() -> gestorCuestionario.iniciarCuestionario(2L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("CU 026 Flujo completo E2E sobre seed (a) ID 6: iniciar -> responder bloque 1 (8.A) -> responder bloque 2 -> COMPLETO + resultados en H2")
    void seedA_flujoCompletoIniciarResponderTodosLosBloquesYCalcularResultados() {
        Cuestionario original = cuestionarioDAO.findById(6L).orElseThrow();
        EstadoCuestionario estadoOrig = original.getEstado();
        LocalDateTime fechaInicioOrig = original.getFechaInicio();
        LocalDateTime fechaUltimoIngresoOrig = original.getFechaUltimoIngreso();
        LocalDateTime fechaFinOrig = original.getFechaFinalizacion();
        LocalDateTime fechaCambioOrig = original.getFechaCambioEstado();
        Integer accesosOrig = original.getCantidadAccesos();
        Integer restanteOrig = original.getTiempoRestanteSegundos();
        Integer ultimoBloqueOrig = original.getUltimoBloqueRespondido();

        try {
            // 1. Iniciar cuestionario (a) -> pasa a EN_PROCESO y devuelve bloque 1 (preguntas asignadas 1, 2, 3)
            CuestionarioIniciadoDTO iniciado = gestorCuestionario.iniciarCuestionario(6L);
            assertThat(iniciado.estado()).isEqualTo(EstadoCuestionario.EN_PROCESO);
            assertThat(iniciado.bloqueActual().numero()).isEqualTo(1);
            assertThat(iniciado.bloqueActual().preguntas()).hasSize(3);

            // 1.b Validar Flujo 6.A: falta responder 2 preguntas -> 400 VALIDACION sin guardar nada
            assertThatThrownBy(() -> gestorCuestionario.responderBloque(6L, 1, new ResponderBloqueRequestDTO(
                    List.of(new RespuestaItemDTO(1L, 1L))
            )))
                    .isInstanceOf(ValidacionException.class)
                    .satisfies(ex -> assertThat(((ValidacionException) ex).getDetalles()).hasSize(2));

            // 2. Responder bloque 1 completo:
            //    PA 1 -> preg 1 (FAC-03) -> opcion 1 (10.0)
            //    PA 2 -> preg 14 (FAC-07) -> opcion 30 (6.0)
            //    PA 3 -> preg 12 (FAC-06) -> opcion 26 (10.0)
            ResponderBloqueResponseDTO respBloque1 = gestorCuestionario.responderBloque(6L, 1, new ResponderBloqueRequestDTO(
                    List.of(
                            new RespuestaItemDTO(1L, 1L),
                            new RespuestaItemDTO(2L, 30L),
                            new RespuestaItemDTO(3L, 26L)
                    )
            ));
            assertThat(respBloque1.completado()).isFalse();
            assertThat(respBloque1.bloqueSiguiente()).isNotNull();
            assertThat(respBloque1.bloqueSiguiente().numero()).isEqualTo(2);
            assertThat(respBloque1.segundosRestantes()).isPositive();

            // 3. Responder bloque 2 completo (último bloque):
            //    PA 4 -> preg 2 (FAC-03) -> opcion 3 (10.0) => FAC-03 = (10 + 10) / 2 = 10.00
            //    PA 5 -> preg 15 (FAC-07) -> opcion 34 (6.0) => FAC-07 = (6 + 6) / 2 = 6.00 => TEC-01 = (10 + 6) / 2 = 8.00
            //    PA 6 -> preg 13 (FAC-06) -> opcion 27 (0.0) => FAC-06 = (10 + 0) / 2 = 5.00 => LID-01 = 5.00 / 1 = 5.00
            //    puntajeObtenido = (8.00 + 5.00) / 2 = 6.50
            ResponderBloqueResponseDTO respBloque2 = gestorCuestionario.responderBloque(6L, 2, new ResponderBloqueRequestDTO(
                    List.of(
                            new RespuestaItemDTO(4L, 3L),
                            new RespuestaItemDTO(5L, 34L),
                            new RespuestaItemDTO(6L, 27L)
                    )
            ));
            assertThat(respBloque2.completado()).isTrue();
            assertThat(respBloque2.estado()).isEqualTo(EstadoCuestionario.COMPLETO);
            assertThat(respBloque2.mensaje()).isEqualTo("El cuestionario ha sido completado con éxito");

            // 4. Verificar en H2 cuestionario COMPLETO y jerarquía Resultado -> ResultadoCompetencia -> ResultadoFactor
            transactionTemplate.executeWithoutResult(status -> {
                Cuestionario enBase = cuestionarioDAO.findById(6L).orElseThrow();
                assertThat(enBase.getEstado()).isEqualTo(EstadoCuestionario.COMPLETO);
                assertThat(enBase.getFechaFinalizacion()).isNotNull();
                assertThat(enBase.getUltimoBloqueRespondido()).isEqualTo(2);
                assertThat(enBase.getCantidadAccesos()).isEqualTo(1);
                assertThat(enBase.getBloques()).allSatisfy(b -> assertThat(b.getRespondido()).isTrue());

                Resultado resultado = resultadoDAO.findByCuestionarioId(6L).orElseThrow();
                assertThat(resultado.getFechaCalculo()).isNotNull();
                assertThat(resultado.getPuntajeObtenido()).isEqualByComparingTo(new BigDecimal("6.50"));
                assertThat(resultado.getResultadosCompetencias()).hasSize(2);

                Map<String, ResultadoCompetencia> porCodigoComp = resultado.getResultadosCompetencias().stream()
                        .collect(Collectors.toMap(rc -> rc.getCompetencia().getCodigo(), rc -> rc));

                ResultadoCompetencia tec = porCodigoComp.get("TEC-01");
                assertThat(tec.getTotalCompetencia()).isEqualByComparingTo(new BigDecimal("8.00"));
                assertThat(tec.getCaracteristicaPuesto()).isNotNull();
                assertThat(tec.getCaracteristicaPuesto().getPonderacionRequerida()).isEqualTo(9.5);
                Map<String, BigDecimal> factoresTec = tec.getResultadosFactores().stream()
                        .collect(Collectors.toMap(rf -> rf.getFactor().getCodigo(), ResultadoFactor::getTotalFactor));
                assertThat(factoresTec.get("FAC-03")).isEqualByComparingTo(new BigDecimal("10.00"));
                assertThat(factoresTec.get("FAC-07")).isEqualByComparingTo(new BigDecimal("6.00"));

                ResultadoCompetencia lid = porCodigoComp.get("LID-01");
                assertThat(lid.getTotalCompetencia()).isEqualByComparingTo(new BigDecimal("5.00"));
                assertThat(lid.getCaracteristicaPuesto()).isNotNull();
                assertThat(lid.getCaracteristicaPuesto().getPonderacionRequerida()).isEqualTo(8.0);
                assertThat(lid.getResultadosFactores()).hasSize(1);
                assertThat(lid.getResultadosFactores().get(0).getTotalFactor())
                        .isEqualByComparingTo(new BigDecimal("5.00"));
            });
        } finally {
            restaurarConBloquesYResultado(6L, estadoOrig, fechaInicioOrig, fechaUltimoIngresoOrig, fechaFinOrig,
                    fechaCambioOrig, accesosOrig, restanteOrig, ultimoBloqueOrig);
        }
    }

    @Test
    @DisplayName("Seed (h) ID 12: cierre directo en un único bloque calcula resultados exactos en H2 y persiste COMPLETO")
    void seedH_cierreDirectoEnUnBloqueYCalculoExacto() {
        Cuestionario original = cuestionarioDAO.findById(12L).orElseThrow();
        EstadoCuestionario estadoOrig = original.getEstado();
        LocalDateTime fechaInicioOrig = original.getFechaInicio();
        LocalDateTime fechaUltimoIngresoOrig = original.getFechaUltimoIngreso();
        LocalDateTime fechaFinOrig = original.getFechaFinalizacion();
        LocalDateTime fechaCambioOrig = original.getFechaCambioEstado();
        Integer accesosOrig = original.getCantidadAccesos();
        Integer restanteOrig = original.getTiempoRestanteSegundos();
        Integer ultimoBloqueOrig = original.getUltimoBloqueRespondido();

        try {
            // Cuestionario 12 bloque 1: PA 13..18
            ResponderBloqueResponseDTO resp = gestorCuestionario.responderBloque(12L, 1, new ResponderBloqueRequestDTO(
                    List.of(
                            new RespuestaItemDTO(13L, 1L),  // preg 1 (FAC-03): 10.0
                            new RespuestaItemDTO(14L, 3L),  // preg 2 (FAC-03): 10.0 -> FAC-03 = 10.00
                            new RespuestaItemDTO(15L, 30L), // preg 14 (FAC-07): 6.0
                            new RespuestaItemDTO(16L, 34L), // preg 15 (FAC-07): 6.0 -> FAC-07 = 6.00 -> TEC-01 = 8.00
                            new RespuestaItemDTO(17L, 26L), // preg 12 (FAC-06): 10.0
                            new RespuestaItemDTO(18L, 27L)  // preg 13 (FAC-06): 0.0 -> FAC-06 = 5.00 -> LID-01 = 5.00
                    )
            ));

            assertThat(resp.completado()).isTrue();
            assertThat(resp.estado()).isEqualTo(EstadoCuestionario.COMPLETO);
            assertThat(resp.mensaje()).isEqualTo("El cuestionario ha sido completado con éxito");

            transactionTemplate.executeWithoutResult(status -> {
                Resultado res = resultadoDAO.findByCuestionarioId(12L).orElseThrow();
                assertThat(res.getPuntajeObtenido()).isEqualByComparingTo(new BigDecimal("6.50"));
                assertThat(res.getResultadosCompetencias()).hasSize(2);
            });
        } finally {
            restaurarConBloquesYResultado(12L, estadoOrig, fechaInicioOrig, fechaUltimoIngresoOrig, fechaFinOrig,
                    fechaCambioOrig, accesosOrig, restanteOrig, ultimoBloqueOrig);
        }
    }

    @Test
    @DisplayName("Seed (d) ID 9 — Flujo 1.A al responder bloque: tiempo agotado lanza 422 y PERSISTE INCOMPLETO en H2")
    void seedD_responderBloqueTiempoAgotado_persisteIncompleto() {
        Cuestionario original = cuestionarioDAO.findById(9L).orElseThrow();
        EstadoCuestionario estadoOrig = original.getEstado();
        LocalDateTime fechaInicioOrig = original.getFechaInicio();
        LocalDateTime fechaCambioOrig = original.getFechaCambioEstado();
        Integer accesosOrig = original.getCantidadAccesos();
        Integer restanteOrig = original.getTiempoRestanteSegundos();

        try {
            assertThatThrownBy(() -> gestorCuestionario.responderBloque(9L, 1, new ResponderBloqueRequestDTO(List.of())))
                    .isInstanceOf(ReglaNegocioException.class);

            Cuestionario enBase = cuestionarioDAO.findById(9L).orElseThrow();
            assertThat(enBase.getEstado()).isEqualTo(EstadoCuestionario.INCOMPLETO);
            assertThat(enBase.getFechaCambioEstado()).isAfter(fechaCambioOrig);
        } finally {
            restaurar(9L, estadoOrig, fechaInicioOrig, fechaCambioOrig, accesosOrig, restanteOrig);
        }
    }

    private void restaurarConBloquesYResultado(Long id,
                                               EstadoCuestionario estado,
                                               LocalDateTime fechaInicio,
                                               LocalDateTime fechaUltimoIngreso,
                                               LocalDateTime fechaFinalizacion,
                                               LocalDateTime fechaCambio,
                                               Integer accesos,
                                               Integer restante,
                                               Integer ultimoBloque) {
        transactionTemplate.executeWithoutResult(status -> {
            resultadoDAO.findByCuestionarioId(id).ifPresent(r -> {
                resultadoDAO.delete(r);
                resultadoDAO.flush();
            });

            Cuestionario c = cuestionarioDAO.findById(id).orElseThrow();
            c.setEstado(estado);
            c.setFechaInicio(fechaInicio);
            c.setFechaUltimoIngreso(fechaUltimoIngreso);
            c.setFechaFinalizacion(fechaFinalizacion);
            c.setFechaCambioEstado(fechaCambio);
            c.setCantidadAccesos(accesos);
            c.setTiempoRestanteSegundos(restante);
            c.setUltimoBloqueRespondido(ultimoBloque);

            for (BloqueCuestionario b : c.getBloques()) {
                b.setRespondido(Boolean.FALSE);
                b.setFechaRespuesta(null);
                for (PreguntaAsignada pa : b.getPreguntasAsignadas()) {
                    pa.setRespuestaCandidato(null);
                }
            }
            cuestionarioDAO.saveAndFlush(c);
        });
    }

    private void restaurar(Long id,
                           EstadoCuestionario estado,
                           LocalDateTime fechaInicio,
                           LocalDateTime fechaCambio,
                           Integer accesos,
                           Integer restante) {
        Cuestionario c = cuestionarioDAO.findById(id).orElseThrow();
        restaurar(id, estado, fechaInicio, c.getFechaUltimoIngreso(), fechaCambio, accesos, restante);
    }

    private void restaurar(Long id,
                           EstadoCuestionario estado,
                           LocalDateTime fechaInicio,
                           LocalDateTime fechaUltimoIngreso,
                           LocalDateTime fechaCambio,
                           Integer accesos,
                           Integer restante) {
        Cuestionario c = cuestionarioDAO.findById(id).orElseThrow();
        c.setEstado(estado);
        c.setFechaInicio(fechaInicio);
        c.setFechaUltimoIngreso(fechaUltimoIngreso);
        c.setFechaCambioEstado(fechaCambio);
        c.setCantidadAccesos(accesos);
        c.setTiempoRestanteSegundos(restante);
        cuestionarioDAO.saveAndFlush(c);
    }
}
