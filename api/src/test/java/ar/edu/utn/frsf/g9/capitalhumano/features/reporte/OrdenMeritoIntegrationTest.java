package ar.edu.utn.frsf.g9.capitalhumano.features.reporte;

import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.EvaluacionOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaCandidatoReporteDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaNoAlcanzoMinimoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.PuestoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.ReporteOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.gestor.GestorReportes;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ValidacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración reales contra H2 y {@code seed.sql} para el CU 028 "Emitir Orden de mérito".
 */
@SpringBootTest
@Transactional(readOnly = true)
class OrdenMeritoIntegrationTest {

    @Autowired
    private GestorReportes gestorReportes;

    @Test
    @DisplayName("H2 seed.sql: búsqueda de puestos 'contiene' encuentra Puesto A y Puesto B con métricas correctas")
    void buscarPuestos_seedCU28() {
        List<PuestoOrdenMeritoDTO> puestos = gestorReportes.buscarPuestos(null, "  ingeniero ", null);

        assertThat(puestos).hasSize(2);
        assertThat(puestos.get(0).puestoId()).isEqualTo(4L);
        assertThat(puestos.get(0).codigo()).isEqualTo("ING-DATA-01");
        assertThat(puestos.get(0).nombre()).isEqualTo("INGENIERO DE DATOS");
        assertThat(puestos.get(0).empresa()).isEqualTo("DATA CORP SA");
        assertThat(puestos.get(0).cantidadCandidatos()).isEqualTo(10L);
        assertThat(puestos.get(0).evaluacionesCompletas()).isEqualTo(1L);
        assertThat(puestos.get(0).evaluacionesTotal()).isEqualTo(2L);

        assertThat(puestos.get(1).puestoId()).isEqualTo(5L);
        assertThat(puestos.get(1).codigo()).isEqualTo("ING-CLOUD-01");
        assertThat(puestos.get(1).empresa()).isEqualTo("NUBE SUR SRL");
        assertThat(puestos.get(1).cantidadCandidatos()).isZero();
        assertThat(puestos.get(1).evaluacionesCompletas()).isZero();
        assertThat(puestos.get(1).evaluacionesTotal()).isZero();

        // Filtro por empresa distingue Puesto A de Puesto B
        List<PuestoOrdenMeritoDTO> soloDataCorp = gestorReportes.buscarPuestos(null, "ingeniero", "data");
        assertThat(soloDataCorp).hasSize(1);
        assertThat(soloDataCorp.get(0).puestoId()).isEqualTo(4L);

        // Sin coincidencias devuelve lista vacía (no error)
        assertThat(gestorReportes.buscarPuestos("NO-EXISTE", null, null)).isEmpty();

        // Sin criterios lanza 400 VALIDACION
        assertThatThrownBy(() -> gestorReportes.buscarPuestos(null, "", "  "))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    @DisplayName("H2 seed.sql: evaluaciones de Puesto A ordenadas más recientes primero y Puesto B dispara Flujo 6.A (422)")
    void listarEvaluaciones_seedCU28() {
        List<EvaluacionOrdenMeritoDTO> evalsA = gestorReportes.listarEvaluacionesPorPuesto(4L);

        assertThat(evalsA).hasSize(2);
        // Más reciente primero: eval 4 (-2 días), luego eval 3 (-10 días)
        assertThat(evalsA.get(0).evaluacionId()).isEqualTo(4L);
        assertThat(evalsA.get(0).cantidadCandidatos()).isEqualTo(7L);
        assertThat(evalsA.get(0).cantidadCompletos()).isEqualTo(3L);

        assertThat(evalsA.get(1).evaluacionId()).isEqualTo(3L);
        assertThat(evalsA.get(1).cantidadCandidatos()).isEqualTo(3L);
        assertThat(evalsA.get(1).cantidadCompletos()).isEqualTo(3L);

        // Puesto B (ID 5) sin evaluaciones -> Flujo 6.A
        assertThatThrownBy(() -> gestorReportes.listarEvaluacionesPorPuesto(5L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no posee evaluaciones");

        // Puesto inexistente -> 404
        assertThatThrownBy(() -> gestorReportes.listarEvaluacionesPorPuesto(999L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("H2 seed.sql: reporte completo de Puesto A (TODAS las evaluaciones) verifica agrupación, orden, desempate, borde >= y datosAdicionales")
    void emitirReporte_puestoA_todasLasEvaluaciones() {
        ReporteOrdenMeritoDTO reporte = gestorReportes.emitirOrdenDeMerito(4L, null);

        // Encabezado
        assertThat(reporte.puesto().puestoId()).isEqualTo(4L);
        assertThat(reporte.puesto().codigo()).isEqualTo("ING-DATA-01");
        assertThat(reporte.puesto().nombre()).isEqualTo("INGENIERO DE DATOS");
        assertThat(reporte.puesto().empresa()).isEqualTo("DATA CORP SA");
        assertThat(reporte.fechaEmision()).isNotNull();
        assertThat(reporte.evaluacionesIncluidas()).hasSize(2);
        assertThat(reporte.competenciasRequeridas()).hasSize(2);

        // ordenDeMerito (4 filas): ALVAREZ (9.50), BENITEZ (8.50), CASTRO (8.50), DIAZ (7.50 borde >=)
        assertThat(reporte.ordenDeMerito()).hasSize(4);
        assertThat(reporte.ordenDeMerito())
                .extracting(FilaOrdenMeritoDTO::posicion)
                .containsExactly(1, 2, 3, 4);
        assertThat(reporte.ordenDeMerito())
                .extracting(FilaOrdenMeritoDTO::apellido)
                .containsExactly("ALVAREZ", "BENITEZ", "CASTRO", "DIAZ");
        assertThat(reporte.ordenDeMerito().get(0).puntajeObtenido()).isEqualByComparingTo("9.50");
        assertThat(reporte.ordenDeMerito().get(1).puntajeObtenido()).isEqualByComparingTo("8.50");
        assertThat(reporte.ordenDeMerito().get(2).puntajeObtenido()).isEqualByComparingTo("8.50");
        assertThat(reporte.ordenDeMerito().get(3).puntajeObtenido()).isEqualByComparingTo("7.50");

        // Observación 2 en COMPLETO: fechaInicio y fechaFin informadas, ultimoIngreso null
        for (FilaOrdenMeritoDTO fila : reporte.ordenDeMerito()) {
            assertThat(fila.datosAdicionales().fechaInicio()).isNotNull();
            assertThat(fila.datosAdicionales().fechaFin()).isNotNull();
            assertThat(fila.datosAdicionales().ultimoIngreso()).isNull();
            assertThat(fila.datosAdicionales().cantidadAccesos()).isPositive();
        }

        // noAlcanzaronMinimos (2 filas): ESPINOZA (7.50, falla 1: TEC-01), FLORES (5.75, falla 2)
        assertThat(reporte.noAlcanzaronMinimos()).hasSize(2);
        FilaNoAlcanzoMinimoDTO espinoza = reporte.noAlcanzaronMinimos().get(0);
        assertThat(espinoza.apellido()).isEqualTo("ESPINOZA");
        assertThat(espinoza.puntajeObtenido()).isEqualByComparingTo("7.50");
        assertThat(espinoza.competenciasNoAlcanzadas()).hasSize(1);
        assertThat(espinoza.competenciasNoAlcanzadas().get(0).codigo()).isEqualTo("TEC-01");
        assertThat(espinoza.competenciasNoAlcanzadas().get(0).obtenido()).isEqualByComparingTo("6.50");
        assertThat(espinoza.competenciasNoAlcanzadas().get(0).requerido()).isEqualByComparingTo("8.0");
        assertThat(espinoza.datosAdicionales().fechaInicio()).isNotNull();
        assertThat(espinoza.datosAdicionales().fechaFin()).isNotNull();
        assertThat(espinoza.datosAdicionales().ultimoIngreso()).isNull();

        FilaNoAlcanzoMinimoDTO flores = reporte.noAlcanzaronMinimos().get(1);
        assertThat(flores.apellido()).isEqualTo("FLORES");
        assertThat(flores.puntajeObtenido()).isEqualByComparingTo("5.75");
        assertThat(flores.competenciasNoAlcanzadas()).hasSize(2);

        // incompletos (1): GARCIA
        assertThat(reporte.incompletos()).hasSize(1);
        FilaCandidatoReporteDTO garcia = reporte.incompletos().get(0);
        assertThat(garcia.apellido()).isEqualTo("GARCIA");
        assertThat(garcia.puntajeObtenido()).isNull();
        assertThat(garcia.datosAdicionales().fechaInicio()).isNotNull();
        assertThat(garcia.datosAdicionales().ultimoIngreso()).isNotNull();
        assertThat(garcia.datosAdicionales().fechaFin()).isNull();
        assertThat(garcia.datosAdicionales().cantidadAccesos()).isEqualTo(2);

        // enProceso (1): HERRERA
        assertThat(reporte.enProceso()).hasSize(1);
        FilaCandidatoReporteDTO herrera = reporte.enProceso().get(0);
        assertThat(herrera.apellido()).isEqualTo("HERRERA");
        assertThat(herrera.puntajeObtenido()).isNull();
        assertThat(herrera.datosAdicionales().fechaInicio()).isNotNull();
        assertThat(herrera.datosAdicionales().ultimoIngreso()).isNotNull();
        assertThat(herrera.datosAdicionales().fechaFin()).isNull();
        assertThat(herrera.datosAdicionales().cantidadAccesos()).isEqualTo(1);

        // sinContestar (1): IBARRA
        assertThat(reporte.sinContestar()).hasSize(1);
        FilaCandidatoReporteDTO ibarra = reporte.sinContestar().get(0);
        assertThat(ibarra.apellido()).isEqualTo("IBARRA");
        assertThat(ibarra.puntajeObtenido()).isNull();
        assertThat(ibarra.datosAdicionales().fechaInicio()).isNull();
        assertThat(ibarra.datosAdicionales().ultimoIngreso()).isNull();
        assertThat(ibarra.datosAdicionales().fechaFin()).isNull();
        assertThat(ibarra.datosAdicionales().cantidadAccesos()).isZero();

        // activos (1): JUAREZ
        assertThat(reporte.activos()).hasSize(1);
        FilaCandidatoReporteDTO juarez = reporte.activos().get(0);
        assertThat(juarez.apellido()).isEqualTo("JUAREZ");
        assertThat(juarez.puntajeObtenido()).isNull();
        assertThat(juarez.datosAdicionales().fechaInicio()).isNull();
        assertThat(juarez.datosAdicionales().ultimoIngreso()).isNull();
        assertThat(juarez.datosAdicionales().fechaFin()).isNull();
        assertThat(juarez.datosAdicionales().cantidadAccesos()).isZero();

        // Totales
        assertThat(reporte.totales().ordenDeMerito()).isEqualTo(4);
        assertThat(reporte.totales().noAlcanzaronMinimos()).isEqualTo(2);
        assertThat(reporte.totales().incompletos()).isEqualTo(1);
        assertThat(reporte.totales().enProceso()).isEqualTo(1);
        assertThat(reporte.totales().sinContestar()).isEqualTo(1);
        assertThat(reporte.totales().activos()).isEqualTo(1);
        assertThat(reporte.totales().totalGeneral()).isEqualTo(10);
    }

    @Test
    @DisplayName("H2 seed.sql: reporte filtrando por una sola evaluación (eval 3) y error 422 si se pasa evaluación ajena (eval 1)")
    void emitirReporte_puestoA_unaEvaluacionYEvaluacionAjena() {
        ReporteOrdenMeritoDTO soloEval3 = gestorReportes.emitirOrdenDeMerito(4L, List.of(3L));

        assertThat(soloEval3.evaluacionesIncluidas()).hasSize(1);
        assertThat(soloEval3.evaluacionesIncluidas().get(0).evaluacionId()).isEqualTo(3L);
        assertThat(soloEval3.ordenDeMerito()).hasSize(3);
        assertThat(soloEval3.noAlcanzaronMinimos()).isEmpty();
        assertThat(soloEval3.incompletos()).isEmpty();
        assertThat(soloEval3.enProceso()).isEmpty();
        assertThat(soloEval3.sinContestar()).isEmpty();
        assertThat(soloEval3.activos()).isEmpty();
        assertThat(soloEval3.totales().totalGeneral()).isEqualTo(3);

        // Evaluación 1 pertenece al puesto 1 -> 422
        assertThatThrownBy(() -> gestorReportes.emitirOrdenDeMerito(4L, List.of(1L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no pertenecen al puesto");
    }
}
