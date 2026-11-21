package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.BloqueCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.PreguntaAsignada;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.RespuestaCandidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Resultado;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.ResultadoCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.ResultadoFactor;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios aislados para {@link CalculadorResultadosCuestionario}, verificando el algoritmo
 * de cálculo de puntajes del CU 026 con casos concretos y redondeo HALF_UP a 2 decimales.
 */
class CalculadorResultadosCuestionarioTest {

    private static final LocalDateTime FECHA_CALCULO = LocalDateTime.of(2026, 11, 21, 12, 0, 0);
    private final CalculadorResultadosCuestionario calculador = new CalculadorResultadosCuestionario();

    @Test
    @DisplayName("Algoritmo CU 026: factor (10+6)/2=8 y (6+2)/2=4 -> competencia 6.00; competencia con 1 factor (10+0)/2=5 -> 5.00; puntajeObtenido 5.50")
    void calcular_competenciaMultiFactorYCompetenciaUnSoloFactor() {
        Competencia compMulti = new Competencia(1L, "TEC-01", "COMPETENCIAS TÉCNICAS", "DESC");
        Competencia compUnica = new Competencia(2L, "LID-01", "LIDERAZGO", "DESC");

        Puesto puesto = new Puesto(3L, "ARQ-01", "ARQUITECTO DE SOFTWARE", "DESC", "INNOVATECH");
        CaracteristicaPuesto cpMulti = new CaracteristicaPuesto(10L, puesto, compMulti, 9.0);
        CaracteristicaPuesto cpUnica = new CaracteristicaPuesto(20L, puesto, compUnica, 8.0);
        puesto.addCaracteristica(cpMulti);
        puesto.addCaracteristica(cpUnica);

        Factor factor1 = new Factor(101L, "F1", "FACTOR 1", "D", 1, compMulti);
        Factor factor2 = new Factor(102L, "F2", "FACTOR 2", "D", 2, compMulti);
        Factor factorUnico = new Factor(103L, "F3", "FACTOR 3", "D", 1, compUnica);

        Cuestionario cuestionario = new Cuestionario();
        cuestionario.setId(99L);
        cuestionario.setPuesto(puesto);

        BloqueCuestionario b1 = new BloqueCuestionario(1);
        b1.addPreguntaAsignada(crearPreguntaRespondida(1L, 1, factor1, 10.0));
        b1.addPreguntaAsignada(crearPreguntaRespondida(2L, 2, factor1, 6.0));  // factor1 = (10 + 6) / 2 = 8.00
        b1.addPreguntaAsignada(crearPreguntaRespondida(3L, 3, factor2, 6.0));
        cuestionario.addBloque(b1);

        BloqueCuestionario b2 = new BloqueCuestionario(2);
        b2.addPreguntaAsignada(crearPreguntaRespondida(4L, 4, factor2, 2.0));  // factor2 = (6 + 2) / 2 = 4.00 -> compMulti = (8 + 4) / 2 = 6.00
        b2.addPreguntaAsignada(crearPreguntaRespondida(5L, 5, factorUnico, 10.0));
        b2.addPreguntaAsignada(crearPreguntaRespondida(6L, 6, factorUnico, 0.0)); // factorUnico = (10 + 0) / 2 = 5.00 -> compUnica = 5.00 / 1 = 5.00
        cuestionario.addBloque(b2);

        Resultado resultado = calculador.calcular(cuestionario, puesto, FECHA_CALCULO);

        assertThat(resultado.getCuestionario()).isSameAs(cuestionario);
        assertThat(resultado.getFechaCalculo()).isEqualTo(FECHA_CALCULO);
        assertThat(resultado.getPuntajeObtenido()).isEqualByComparingTo(new BigDecimal("5.50"));
        assertThat(resultado.getPuntajeObtenido().scale()).isEqualTo(2);
        assertThat(resultado.getResultadosCompetencias()).hasSize(2);

        ResultadoCompetencia rcMulti = resultado.getResultadosCompetencias().stream()
                .filter(rc -> rc.getCompetencia().getId().equals(1L))
                .findFirst()
                .orElseThrow();
        assertThat(rcMulti.getTotalCompetencia()).isEqualByComparingTo(new BigDecimal("6.00"));
        assertThat(rcMulti.getCaracteristicaPuesto()).isSameAs(cpMulti);
        assertThat(rcMulti.getResultadosFactores()).hasSize(2);
        assertThat(rcMulti.getResultadosFactores())
                .extracting(rf -> rf.getFactor().getId(), ResultadoFactor::getTotalFactor)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(101L, new BigDecimal("8.00")),
                        org.assertj.core.groups.Tuple.tuple(102L, new BigDecimal("4.00"))
                );

        ResultadoCompetencia rcUnica = resultado.getResultadosCompetencias().stream()
                .filter(rc -> rc.getCompetencia().getId().equals(2L))
                .findFirst()
                .orElseThrow();
        assertThat(rcUnica.getTotalCompetencia()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(rcUnica.getCaracteristicaPuesto()).isSameAs(cpUnica);
        assertThat(rcUnica.getResultadosFactores()).hasSize(1);
        assertThat(rcUnica.getResultadosFactores().get(0).getTotalFactor())
                .isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("Redondeo HALF_UP a 2 decimales con promedios periódicos (3 factores en una competencia)")
    void calcular_redondeoHalfUpDosDecimales() {
        Competencia comp = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Puesto puesto = new Puesto(1L, "DEV-01", "DEV", "DESC", "TECH");

        Factor f1 = new Factor(1L, "F1", "F1", "D", 1, comp);
        Factor f2 = new Factor(2L, "F2", "F2", "D", 2, comp);
        Factor f3 = new Factor(3L, "F3", "F3", "D", 3, comp);

        Cuestionario cuestionario = new Cuestionario();
        BloqueCuestionario bloque = new BloqueCuestionario(1);
        // f1: (10 + 0) / 2 = 5.00
        bloque.addPreguntaAsignada(crearPreguntaRespondida(1L, 1, f1, 10.0));
        bloque.addPreguntaAsignada(crearPreguntaRespondida(2L, 2, f1, 0.0));
        // f2: (10 + 5) / 2 = 7.50
        bloque.addPreguntaAsignada(crearPreguntaRespondida(3L, 3, f2, 10.0));
        bloque.addPreguntaAsignada(crearPreguntaRespondida(4L, 4, f2, 5.0));
        // f3: (10 + 5) / 2 = 7.50
        bloque.addPreguntaAsignada(crearPreguntaRespondida(5L, 5, f3, 10.0));
        bloque.addPreguntaAsignada(crearPreguntaRespondida(6L, 6, f3, 5.0));
        cuestionario.addBloque(bloque);

        // totalCompetencia = (5.00 + 7.50 + 7.50) / 3 = 20.00 / 3 = 6.6666... -> 6.67 (HALF_UP)
        Resultado resultado = calculador.calcular(cuestionario, puesto, FECHA_CALCULO);

        assertThat(resultado.getResultadosCompetencias()).hasSize(1);
        ResultadoCompetencia rc = resultado.getResultadosCompetencias().get(0);
        assertThat(rc.getTotalCompetencia()).isEqualByComparingTo(new BigDecimal("6.67"));
        assertThat(rc.getTotalCompetencia().scale()).isEqualTo(2);
        assertThat(resultado.getPuntajeObtenido()).isEqualByComparingTo(new BigDecimal("6.67"));
    }

    private PreguntaAsignada crearPreguntaRespondida(Long id, int orden, Factor factor, double ponderacion) {
        Pregunta pregunta = new Pregunta(id, "P" + id, "Enunciado " + id, "DESC", factor);
        OpcionRespuesta opcion = new OpcionRespuesta(id * 10, 1, "OP", "DESC", ponderacion);
        pregunta.addOpcion(opcion);

        PreguntaAsignada pa = new PreguntaAsignada(pregunta, orden);
        pa.setId(id);
        pa.setRespuestaCandidato(new RespuestaCandidato(pa, opcion, FECHA_CALCULO.minusMinutes(1)));
        return pa;
    }
}
