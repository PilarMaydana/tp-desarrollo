package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.BloqueCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.PreguntaAsignada;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para {@link GestorCuestionario} y comportamiento de dominio de
 * {@link Factor#tieneMinimoDosPreguntas()} y {@link Cuestionario#armarBloques(List)}.
 */
@ExtendWith(MockitoExtension.class)
class GestorCuestionarioTest {

    @Mock
    private FactorDAO factorDAO;

    private GestorCuestionario gestorCuestionario;

    @BeforeEach
    void setUp() {
        gestorCuestionario = new GestorCuestionario(factorDAO);
    }

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
    @DisplayName("Cuestionario.armarBloques divide preguntas respetando cantidadPreguntasPorBloque con numeración secuencial")
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
        assertThat(cuestionario.getBloques().get(0).getPreguntasAsignadas()).hasSize(3);
        assertThat(cuestionario.getBloques().get(1).getPreguntasAsignadas()).hasSize(3);
        assertThat(cuestionario.getBloques().get(2).getPreguntasAsignadas()).hasSize(1);

        List<Integer> ordenes = cuestionario.getBloques().stream()
                .flatMap(b -> b.getPreguntasAsignadas().stream())
                .map(PreguntaAsignada::getNroOrden)
                .toList();
        assertThat(ordenes).containsExactly(1, 2, 3, 4, 5, 6, 7);
        assertThat(cuestionario.getTotalPreguntasAsignadas()).isEqualTo(7);
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
}
