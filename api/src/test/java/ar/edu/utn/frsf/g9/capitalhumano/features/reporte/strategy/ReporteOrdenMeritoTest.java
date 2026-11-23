package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.strategy;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Resultado;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.ResultadoCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.CompetenciaNoAlcanzadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaCandidatoReporteDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaNoAlcanzoMinimoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.ReporteOrdenMeritoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios puros (sin Spring) para la estrategia {@link ReporteOrdenMerito}.
 */
class ReporteOrdenMeritoTest {

    private static final LocalDateTime FECHA_EMISION = LocalDateTime.of(2026, 11, 23, 18, 30, 0);

    @Test
    @DisplayName("Clasifica todos los estados, ordena por puntaje descendente con desempate determinístico y respeta observaciones 1 y 2")
    void clasificacionCompletaOrdenDesempateYObservaciones() {
        Competencia compCom = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Competencia compTec = new Competencia(5L, "TEC-01", "COMPETENCIAS TÉCNICAS", "DESC");

        Puesto puesto = new Puesto(4L, "ING-DATA-01", "INGENIERO DE DATOS", "DESC", "DATA CORP SA");
        CaracteristicaPuesto cpCom = new CaracteristicaPuesto(7L, puesto, compCom, 7.0);
        CaracteristicaPuesto cpTec = new CaracteristicaPuesto(8L, puesto, compTec, 8.0);
        puesto.addCaracteristica(cpCom);
        puesto.addCaracteristica(cpTec);

        Evaluacion eval1 = crearEvaluacion(10L, puesto, FECHA_EMISION.minusDays(5));
        Evaluacion eval2 = crearEvaluacion(11L, puesto, FECHA_EMISION.minusDays(1));

        // 1) COMPLETO top: 9.50
        Cuestionario qTop = crearCuestionario(101L, eval1, crearCandidato(1L, TipoDocumento.DNI, "30000001", "ZAPATA", "ANA"),
                EstadoCuestionario.COMPLETO, FECHA_EMISION.minusDays(4), FECHA_EMISION.minusDays(4), FECHA_EMISION.minusDays(4).plusMinutes(30), 1);
        Resultado rTop = crearResultado(qTop, "9.50", compCom, cpCom, "9.00", compTec, cpTec, "10.00");

        // 2) COMPLETO empate por puntaje 8.50 (mismo apellido y nombre, desempata nroDocumento)
        Cuestionario qEmpateDoc2 = crearCuestionario(102L, eval1, crearCandidato(2L, TipoDocumento.DNI, "30000020", "GOMEZ", "JUAN"),
                EstadoCuestionario.COMPLETO, FECHA_EMISION.minusDays(4), FECHA_EMISION.minusDays(4), FECHA_EMISION.minusDays(4).plusMinutes(25), 2);
        Resultado rEmpateDoc2 = crearResultado(qEmpateDoc2, "8.50", compCom, cpCom, "8.50", compTec, cpTec, "8.50");

        Cuestionario qEmpateDoc1 = crearCuestionario(103L, eval1, crearCandidato(3L, TipoDocumento.DNI, "30000010", "GOMEZ", "JUAN"),
                EstadoCuestionario.COMPLETO, FECHA_EMISION.minusDays(4), FECHA_EMISION.minusDays(4), FECHA_EMISION.minusDays(4).plusMinutes(20), 1);
        Resultado rEmpateDoc1 = crearResultado(qEmpateDoc1, "8.50", compCom, cpCom, "8.00", compTec, cpTec, "9.00");

        Cuestionario qEmpateApellido = crearCuestionario(104L, eval1, crearCandidato(4L, TipoDocumento.DNI, "30000099", "ALVAREZ", "LUCAS"),
                EstadoCuestionario.COMPLETO, FECHA_EMISION.minusDays(4), FECHA_EMISION.minusDays(4), FECHA_EMISION.minusDays(4).plusMinutes(40), 1);
        Resultado rEmpateApellido = crearResultado(qEmpateApellido, "8.50", compCom, cpCom, "8.50", compTec, cpTec, "8.50");

        // 3) COMPLETO borde exacto (>=): COM = 7.00 == 7.0, TEC = 8.00 == 8.0
        Cuestionario qBorde = crearCuestionario(105L, eval2, crearCandidato(5L, TipoDocumento.DNI, "30000005", "DIAZ", "CAMILA"),
                EstadoCuestionario.COMPLETO, FECHA_EMISION.minusHours(10), FECHA_EMISION.minusHours(10), FECHA_EMISION.minusHours(9), 1);
        Resultado rBorde = crearResultado(qBorde, "7.50", compCom, cpCom, "7.00", compTec, cpTec, "8.00");

        // 4) COMPLETO no alcanza 1 competencia (TEC = 7.99 < 8.0)
        Cuestionario qNoAlcanza1 = crearCuestionario(106L, eval2, crearCandidato(6L, TipoDocumento.DNI, "30000006", "PEREZ", "TOMAS"),
                EstadoCuestionario.COMPLETO, FECHA_EMISION.minusHours(8), FECHA_EMISION.minusHours(8), FECHA_EMISION.minusHours(7), 2);
        Resultado rNoAlcanza1 = crearResultado(qNoAlcanza1, "8.50", compCom, cpCom, "9.01", compTec, cpTec, "7.99");

        // 5) COMPLETO no alcanza 2 competencias
        Cuestionario qNoAlcanza2 = crearCuestionario(107L, eval2, crearCandidato(7L, TipoDocumento.LE, "22000007", "SILVA", "PAULA"),
                EstadoCuestionario.COMPLETO, FECHA_EMISION.minusHours(7), FECHA_EMISION.minusHours(7), FECHA_EMISION.minusHours(6), 1);
        Resultado rNoAlcanza2 = crearResultado(qNoAlcanza2, "5.50", compCom, cpCom, "5.00", compTec, cpTec, "6.00");

        // 6) No completos (4 estados)
        LocalDateTime incInicio = FECHA_EMISION.minusHours(5);
        LocalDateTime incUltimo = FECHA_EMISION.minusHours(4);
        Cuestionario qIncompleto = crearCuestionario(108L, eval2, crearCandidato(8L, TipoDocumento.DNI, "30000008", "RUIZ", "MARIO"),
                EstadoCuestionario.INCOMPLETO, incInicio, incUltimo, null, 2);

        LocalDateTime procInicio = FECHA_EMISION.minusMinutes(30);
        LocalDateTime procUltimo = FECHA_EMISION.minusMinutes(10);
        Cuestionario qEnProceso = crearCuestionario(109L, eval2, crearCandidato(9L, TipoDocumento.PP, "AR999009", "SOSA", "CARLA"),
                EstadoCuestionario.EN_PROCESO, procInicio, procUltimo, null, 1);

        Cuestionario qSinContestar = crearCuestionario(110L, eval2, crearCandidato(10L, TipoDocumento.DNI, "30000011", "TORRES", "DIEGO"),
                EstadoCuestionario.SIN_CONTESTAR, null, null, null, 0);

        Cuestionario qActivo = crearCuestionario(111L, eval2, crearCandidato(11L, TipoDocumento.DNI, "30000012", "VEGA", "LAURA"),
                EstadoCuestionario.ACTIVO, null, null, null, 0);

        ReporteOrdenMerito estrategia = new ReporteOrdenMerito(
                puesto,
                List.of(eval2, eval1),
                List.of(qBorde, qEmpateDoc2, qTop, qNoAlcanza2, qIncompleto, qActivo, qEmpateDoc1, qSinContestar, qEmpateApellido, qNoAlcanza1, qEnProceso),
                List.of(rTop, rEmpateDoc2, rEmpateDoc1, rEmpateApellido, rBorde, rNoAlcanza1, rNoAlcanza2),
                FECHA_EMISION
        );

        estrategia.recopilarDatos();
        ReporteOrdenMeritoDTO reporte = estrategia.generarSalida();

        // Encabezado
        assertThat(reporte.puesto().codigo()).isEqualTo("ING-DATA-01");
        assertThat(reporte.fechaEmision()).isEqualTo(FECHA_EMISION);
        assertThat(reporte.evaluacionesIncluidas()).hasSize(2);
        assertThat(reporte.competenciasRequeridas()).hasSize(2);

        // Orden de mérito (5 filas): ZAPATA (9.50), ALVAREZ (8.50), GOMEZ doc10 (8.50), GOMEZ doc20 (8.50), DIAZ borde (7.50)
        assertThat(reporte.ordenDeMerito()).hasSize(5);
        assertThat(reporte.ordenDeMerito())
                .extracting(FilaOrdenMeritoDTO::posicion)
                .containsExactly(1, 2, 3, 4, 5);
        assertThat(reporte.ordenDeMerito())
                .extracting(FilaOrdenMeritoDTO::nroDocumento)
                .containsExactly("30000001", "30000099", "30000010", "30000020", "30000005");

        // Observación 2 en COMPLETO: fechaInicio, fechaFin, ultimoIngreso=null, cantidadAccesos
        FilaOrdenMeritoDTO primera = reporte.ordenDeMerito().get(0);
        assertThat(primera.puntajeObtenido()).isEqualByComparingTo("9.50");
        assertThat(primera.datosAdicionales().fechaInicio()).isEqualTo(qTop.getFechaInicio());
        assertThat(primera.datosAdicionales().fechaFin()).isEqualTo(qTop.getFechaFinalizacion());
        assertThat(primera.datosAdicionales().ultimoIngreso()).isNull();
        assertThat(primera.datosAdicionales().cantidadAccesos()).isEqualTo(1);

        // No alcanzaron mínimos (2 filas, descendente por puntaje)
        assertThat(reporte.noAlcanzaronMinimos()).hasSize(2);
        FilaNoAlcanzoMinimoDTO noAlc1 = reporte.noAlcanzaronMinimos().get(0);
        assertThat(noAlc1.apellido()).isEqualTo("PEREZ");
        assertThat(noAlc1.puntajeObtenido()).isEqualByComparingTo("8.50");
        assertThat(noAlc1.competenciasNoAlcanzadas()).hasSize(1);
        assertThat(noAlc1.competenciasNoAlcanzadas().get(0).codigo()).isEqualTo("TEC-01");
        assertThat(noAlc1.competenciasNoAlcanzadas().get(0).obtenido()).isEqualByComparingTo("7.99");
        assertThat(noAlc1.competenciasNoAlcanzadas().get(0).requerido()).isEqualByComparingTo("8.0");
        assertThat(noAlc1.datosAdicionales().fechaFin()).isEqualTo(qNoAlcanza1.getFechaFinalizacion());
        assertThat(noAlc1.datosAdicionales().ultimoIngreso()).isNull();

        FilaNoAlcanzoMinimoDTO noAlc2 = reporte.noAlcanzaronMinimos().get(1);
        assertThat(noAlc2.apellido()).isEqualTo("SILVA");
        assertThat(noAlc2.competenciasNoAlcanzadas()).hasSize(2);

        // Observaciones 1 y 2 en NO completos: puntajeObtenido = null, fechaFin = null, ultimoIngreso presente cuando aplica
        assertThat(reporte.incompletos()).hasSize(1);
        FilaCandidatoReporteDTO inc = reporte.incompletos().get(0);
        assertThat(inc.puntajeObtenido()).isNull();
        assertThat(inc.datosAdicionales().fechaInicio()).isEqualTo(incInicio);
        assertThat(inc.datosAdicionales().ultimoIngreso()).isEqualTo(incUltimo);
        assertThat(inc.datosAdicionales().fechaFin()).isNull();
        assertThat(inc.datosAdicionales().cantidadAccesos()).isEqualTo(2);

        assertThat(reporte.enProceso()).hasSize(1);
        FilaCandidatoReporteDTO proc = reporte.enProceso().get(0);
        assertThat(proc.puntajeObtenido()).isNull();
        assertThat(proc.datosAdicionales().fechaInicio()).isEqualTo(procInicio);
        assertThat(proc.datosAdicionales().ultimoIngreso()).isEqualTo(procUltimo);
        assertThat(proc.datosAdicionales().fechaFin()).isNull();
        assertThat(proc.datosAdicionales().cantidadAccesos()).isEqualTo(1);

        assertThat(reporte.sinContestar()).hasSize(1);
        FilaCandidatoReporteDTO sc = reporte.sinContestar().get(0);
        assertThat(sc.puntajeObtenido()).isNull();
        assertThat(sc.datosAdicionales().fechaInicio()).isNull();
        assertThat(sc.datosAdicionales().ultimoIngreso()).isNull();
        assertThat(sc.datosAdicionales().fechaFin()).isNull();
        assertThat(sc.datosAdicionales().cantidadAccesos()).isZero();

        assertThat(reporte.activos()).hasSize(1);
        FilaCandidatoReporteDTO act = reporte.activos().get(0);
        assertThat(act.puntajeObtenido()).isNull();
        assertThat(act.datosAdicionales().fechaInicio()).isNull();
        assertThat(act.datosAdicionales().ultimoIngreso()).isNull();
        assertThat(act.datosAdicionales().fechaFin()).isNull();
        assertThat(act.datosAdicionales().cantidadAccesos()).isZero();

        // Totales
        assertThat(reporte.totales().ordenDeMerito()).isEqualTo(5);
        assertThat(reporte.totales().noAlcanzaronMinimos()).isEqualTo(2);
        assertThat(reporte.totales().incompletos()).isEqualTo(1);
        assertThat(reporte.totales().enProceso()).isEqualTo(1);
        assertThat(reporte.totales().sinContestar()).isEqualTo(1);
        assertThat(reporte.totales().activos()).isEqualTo(1);
        assertThat(reporte.totales().totalGeneral()).isEqualTo(11);
    }

    @Test
    @DisplayName("Competencia faltante en Resultado se considera NO alcanzada con obtenido = null")
    void competenciaFaltanteEnResultado_seConsideraNoAlcanzada() {
        Competencia compCom = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Competencia compTec = new Competencia(5L, "TEC-01", "COMPETENCIAS TÉCNICAS", "DESC");

        Puesto puesto = new Puesto(4L, "ING-DATA-01", "INGENIERO DE DATOS", "DESC", "DATA CORP SA");
        CaracteristicaPuesto cpCom = new CaracteristicaPuesto(7L, puesto, compCom, 7.0);
        CaracteristicaPuesto cpTec = new CaracteristicaPuesto(8L, puesto, compTec, 8.0);
        puesto.addCaracteristica(cpCom);
        puesto.addCaracteristica(cpTec);

        Evaluacion eval = crearEvaluacion(10L, puesto, FECHA_EMISION.minusDays(1));
        Cuestionario q = crearCuestionario(200L, eval, crearCandidato(20L, TipoDocumento.DNI, "40000020", "MENDEZ", "SOFIA"),
                EstadoCuestionario.COMPLETO, FECHA_EMISION.minusHours(3), FECHA_EMISION.minusHours(3), FECHA_EMISION.minusHours(2), 1);

        // Resultado solo tiene COM-01 (9.00), le falta TEC-01
        Resultado res = new Resultado(q, FECHA_EMISION.minusHours(2), new BigDecimal("9.00"));
        res.addResultadoCompetencia(new ResultadoCompetencia(compCom, cpCom, new BigDecimal("9.00")));

        ReporteOrdenMerito estrategia = new ReporteOrdenMerito(puesto, List.of(eval), List.of(q), List.of(res), FECHA_EMISION);
        estrategia.recopilarDatos();
        ReporteOrdenMeritoDTO reporte = estrategia.generarSalida();

        assertThat(reporte.ordenDeMerito()).isEmpty();
        assertThat(reporte.noAlcanzaronMinimos()).hasSize(1);
        CompetenciaNoAlcanzadaDTO faltante = reporte.noAlcanzaronMinimos().get(0).competenciasNoAlcanzadas().get(0);
        assertThat(faltante.codigo()).isEqualTo("TEC-01");
        assertThat(faltante.obtenido()).isNull();
        assertThat(faltante.requerido()).isEqualByComparingTo("8.0");
    }

    @Test
    @DisplayName("Mismo candidato en dos evaluaciones seleccionadas genera dos filas independientes identificadas por evaluacionId")
    void candidatoEnDosEvaluaciones_apareceDosVeces() {
        Competencia comp = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Puesto puesto = new Puesto(4L, "COM-SR", "ESPECIALISTA COMUNICACIÓN", "DESC", "EMPRESA X");
        CaracteristicaPuesto cp = new CaracteristicaPuesto(1L, puesto, comp, 7.0);
        puesto.addCaracteristica(cp);

        Evaluacion eval1 = crearEvaluacion(10L, puesto, FECHA_EMISION.minusDays(10));
        Evaluacion eval2 = crearEvaluacion(20L, puesto, FECHA_EMISION.minusDays(2));

        Candidato mismoCandidato = crearCandidato(1L, TipoDocumento.DNI, "31111111", "LOPEZ", "CARLOS");

        Cuestionario q1 = crearCuestionario(301L, eval1, mismoCandidato, EstadoCuestionario.COMPLETO,
                FECHA_EMISION.minusDays(9), FECHA_EMISION.minusDays(9), FECHA_EMISION.minusDays(9).plusMinutes(20), 1);
        Resultado r1 = new Resultado(q1, FECHA_EMISION.minusDays(9), new BigDecimal("7.50"));
        r1.addResultadoCompetencia(new ResultadoCompetencia(comp, cp, new BigDecimal("7.50")));

        Cuestionario q2 = crearCuestionario(302L, eval2, mismoCandidato, EstadoCuestionario.COMPLETO,
                FECHA_EMISION.minusDays(1), FECHA_EMISION.minusDays(1), FECHA_EMISION.minusDays(1).plusMinutes(15), 1);
        Resultado r2 = new Resultado(q2, FECHA_EMISION.minusDays(1), new BigDecimal("9.25"));
        r2.addResultadoCompetencia(new ResultadoCompetencia(comp, cp, new BigDecimal("9.25")));

        ReporteOrdenMerito estrategia = new ReporteOrdenMerito(
                puesto, List.of(eval2, eval1), List.of(q1, q2), List.of(r1, r2), FECHA_EMISION
        );
        estrategia.recopilarDatos();
        ReporteOrdenMeritoDTO reporte = estrategia.generarSalida();

        // Puesto con una sola competencia + mismo candidato en dos evaluaciones -> 2 filas en ordenDeMerito
        assertThat(reporte.competenciasRequeridas()).hasSize(1);
        assertThat(reporte.ordenDeMerito()).hasSize(2);
        assertThat(reporte.ordenDeMerito().get(0).evaluacionId()).isEqualTo(20L);
        assertThat(reporte.ordenDeMerito().get(0).puntajeObtenido()).isEqualByComparingTo("9.25");
        assertThat(reporte.ordenDeMerito().get(1).evaluacionId()).isEqualTo(10L);
        assertThat(reporte.ordenDeMerito().get(1).puntajeObtenido()).isEqualByComparingTo("7.50");
    }

    private Evaluacion crearEvaluacion(Long id, Puesto puesto, LocalDateTime fechaCreacion) {
        Evaluacion e = new Evaluacion(puesto, fechaCreacion, 1L, 60, 5);
        e.setId(id);
        return e;
    }

    private Candidato crearCandidato(Long id, TipoDocumento tipo, String nroDoc, String apellido, String nombre) {
        return new Candidato(id, "C-" + id, tipo, nroDoc, apellido, nombre, apellido.toLowerCase() + "@example.com");
    }

    private Cuestionario crearCuestionario(Long id,
                                           Evaluacion eval,
                                           Candidato candidato,
                                           EstadoCuestionario estado,
                                           LocalDateTime inicio,
                                           LocalDateTime ultimoIngreso,
                                           LocalDateTime fin,
                                           int accesos) {
        Cuestionario c = new Cuestionario();
        c.setId(id);
        c.setEvaluacion(eval);
        c.setPuesto(eval.getPuesto());
        c.setCandidato(candidato);
        c.setEstado(estado);
        c.setClaveAcceso("CLAVE123");
        c.setFechaInicio(inicio);
        c.setFechaUltimoIngreso(ultimoIngreso);
        c.setFechaFinalizacion(fin);
        c.setCantidadAccesos(accesos);
        return c;
    }

    private Resultado crearResultado(Cuestionario c,
                                     String puntaje,
                                     Competencia comp1,
                                     CaracteristicaPuesto cp1,
                                     String val1,
                                     Competencia comp2,
                                     CaracteristicaPuesto cp2,
                                     String val2) {
        Resultado r = new Resultado(c, c.getFechaFinalizacion(), new BigDecimal(puntaje));
        r.addResultadoCompetencia(new ResultadoCompetencia(comp1, cp1, new BigDecimal(val1)));
        r.addResultadoCompetencia(new ResultadoCompetencia(comp2, cp2, new BigDecimal(val2)));
        return r;
    }
}
