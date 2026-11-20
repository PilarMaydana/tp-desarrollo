package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioIniciadoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.InstruccionesCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

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

    @Test
    @DisplayName("Seed (a) ID 6: instrucciones no incrementa accesos; iniciar pasa a EN_PROCESO y devuelve bloque 1")
    void seedA_instruccionesYIniciar() {
        Cuestionario original = cuestionarioDAO.findById(6L).orElseThrow();
        EstadoCuestionario estadoOrig = original.getEstado();
        LocalDateTime fechaInicioOrig = original.getFechaInicio();
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
        } finally {
            restaurar(6L, estadoOrig, fechaInicioOrig, fechaCambioOrig, accesosOrig, restanteOrig);
        }
    }

    @Test
    @DisplayName("Seed (b) ID 7: reingreso EN_PROCESO (1.C) devuelve bloque 2 pendiente e incrementa accesos")
    void seedB_reingresoEnProceso() {
        Cuestionario original = cuestionarioDAO.findById(7L).orElseThrow();
        EstadoCuestionario estadoOrig = original.getEstado();
        LocalDateTime fechaInicioOrig = original.getFechaInicio();
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
        } finally {
            restaurar(7L, estadoOrig, fechaInicioOrig, fechaCambioOrig, accesosOrig, restanteOrig);
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

    private void restaurar(Long id,
                           EstadoCuestionario estado,
                           LocalDateTime fechaInicio,
                           LocalDateTime fechaCambio,
                           Integer accesos,
                           Integer restante) {
        Cuestionario c = cuestionarioDAO.findById(id).orElseThrow();
        c.setEstado(estado);
        c.setFechaInicio(fechaInicio);
        c.setFechaCambioEstado(fechaCambio);
        c.setCantidadAccesos(accesos);
        c.setTiempoRestanteSegundos(restante);
        cuestionarioDAO.saveAndFlush(c);
    }
}
