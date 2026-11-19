package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta;

import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dao.PreguntaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaFiltroDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaModificacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor.GestorPregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración end-to-end sobre el backend de Preguntas con base H2 y datos de seed.sql.
 */
@SpringBootTest
@Transactional
class PreguntaIntegrationTest {

    @Autowired
    private GestorPregunta gestorPregunta;

    @Autowired
    private PreguntaDAO preguntaDAO;

    @Test
    @DisplayName("Búsqueda en H2: encuentra la pregunta cargada en seed.sql por nombre o contenido 'POO'")
    void buscarPreguntas_seedData_porNombre() {
        PreguntaFiltroDTO filtro = new PreguntaFiltroDTO(null, null, "POO");
        List<PreguntaDTO> resultados = gestorPregunta.buscarPreguntas(filtro);

        assertThat(resultados).isNotEmpty();
        PreguntaDTO primera = resultados.get(0);
        assertThat(primera.getNombre()).contains("POO");
        assertThat(primera.getCompetencia()).isEqualTo("COMPETENCIAS TÉCNICAS");
        assertThat(primera.getFactor()).isEqualTo("DESARROLLO DE SOFTWARE");
        assertThat(primera.getOpciones()).hasSize(2);
    }

    @Test
    @DisplayName("Búsqueda en H2: encuentra preguntas filtrando por competencia 'Técnicas'")
    void buscarPreguntas_seedData_porCompetencia() {
        PreguntaFiltroDTO filtro = new PreguntaFiltroDTO("Técnicas", null, null);
        List<PreguntaDTO> resultados = gestorPregunta.buscarPreguntas(filtro);

        assertThat(resultados).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Búsqueda en H2: encuentra preguntas filtrando por factor 'Desarrollo'")
    void buscarPreguntas_seedData_porFactor() {
        PreguntaFiltroDTO filtro = new PreguntaFiltroDTO(null, "Desarrollo", null);
        List<PreguntaDTO> resultados = gestorPregunta.buscarPreguntas(filtro);

        assertThat(resultados).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Ciclo completo CRUD: crear pregunta, buscarla, modificarla y darla de baja lógica")
    void cicloCompletoCRUD() {
        // 1. Alta (CU 021)
        PreguntaDTO nueva = new PreguntaDTO(
                null,
                null,
                null,
                "FAC-01",
                "Pregunta Integración",
                "¿Pregunta de prueba de integración?",
                "Descripción test",
                List.of(
                        new OpcionRespuestaDTO("Opción 1", "Desc 1", 1, 5.0),
                        new OpcionRespuestaDTO("Opción 2", "Desc 2", 2, 5.0)
                )
        );

        PreguntaDTO creada = gestorPregunta.crearPregunta(nueva);
        assertThat(creada.getId()).isNotNull();
        Long id = creada.getId();

        // 2. Consulta (CU 020)
        PreguntaDTO obtenida = gestorPregunta.obtenerPorId(id);
        assertThat(obtenida.getNombre()).isEqualTo("Pregunta Integración");
        assertThat(obtenida.getOpciones()).hasSize(2);

        // 3. Modificación (CU 022)
        PreguntaModificacionDTO mod = new PreguntaModificacionDTO(
                "Pregunta Integración Modificada",
                "¿Pregunta de prueba de integración actualizada?",
                "Descripción actualizada",
                List.of(
                        new OpcionRespuestaDTO("Opción 1 Mod", "Desc 1", 1, 3.0),
                        new OpcionRespuestaDTO("Opción 2 Mod", "Desc 2", 2, 7.0)
                )
        );

        PreguntaDTO actualizada = gestorPregunta.modificarPregunta(id, mod);
        assertThat(actualizada.getNombre()).isEqualTo("PREGUNTA INTEGRACIÓN MODIFICADA");
        assertThat(actualizada.getOpciones().get(0).getNombre()).isEqualTo("OPCIÓN 1 MOD");
        assertThat(actualizada.getFactorCodigo()).isEqualTo("FAC-01");

        // 4. Baja lógica (CU 023)
        gestorPregunta.eliminarPregunta(id);

        Pregunta enBase = preguntaDAO.findById(id).orElseThrow();
        assertThat(enBase.getEliminado()).isTrue();
        assertThat(enBase.getFechaEliminacion()).isNotNull();
    }

    @Test
    @DisplayName("CU 022 en H2: rechaza duplicado por nombre y enunciado contra otra pregunta del mismo factor en seed.sql")
    void modificarPregunta_seedData_duplicadoEnMismoFactor_lanza409() {
        // Pregunta 1 y 2 pertenecen al mismo factor (factor_id = 3)
        PreguntaModificacionDTO duplicada = new PreguntaModificacionDTO(
                "  patrones de diseño  ",
                "  ¿para qué sirve el patrón singleton?  ",
                "INTENTO DUPLICADO",
                List.of(
                        new OpcionRespuestaDTO("OPCION A", "DESC A", 1, 5.0),
                        new OpcionRespuestaDTO("OPCION B", "DESC B", 2, 5.0)
                )
        );

        assertThatThrownBy(() -> gestorPregunta.modificarPregunta(1L, duplicada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("nombre")
                .hasMessageContaining("enunciado");

        PreguntaDTO sinCambios = gestorPregunta.obtenerPorId(1L);
        assertThat(sinCambios.getNombre()).isEqualTo("PREGUNTA POO");
    }

    @Test
    @DisplayName("CU 022 en H2: permite modificar ponderaciones y opciones sin cambiar nombre/enunciado en seed.sql")
    void modificarPregunta_seedData_sinCambiarNombreNiEnunciado_reemplazaOpciones() {
        PreguntaDTO original = gestorPregunta.obtenerPorId(3L);
        assertThat(original.getFactorId()).isEqualTo(1L);
        assertThat(original.getCompetenciaId()).isEqualTo(1L);

        PreguntaModificacionDTO actualizacion = new PreguntaModificacionDTO(
                original.getNombre().toLowerCase(),
                original.getEnunciado().toLowerCase(),
                "  descripción actualizada  ",
                List.of(
                        new OpcionRespuestaDTO("EN DESACUERDO", "EN DESACUERDO", 1, 2.5),
                        new OpcionRespuestaDTO("NEUTRAL", "NEUTRAL", 2, 2.5),
                        new OpcionRespuestaDTO("DE ACUERDO", "DE ACUERDO", 3, 5.0)
                )
        );

        PreguntaDTO modificada = gestorPregunta.modificarPregunta(3L, actualizacion);

        assertThat(modificada.getId()).isEqualTo(3L);
        assertThat(modificada.getFactorId()).isEqualTo(1L);
        assertThat(modificada.getCompetenciaId()).isEqualTo(1L);
        assertThat(modificada.getNombre()).isEqualTo("EXPRESIÓN VERBAL");
        assertThat(modificada.getDescripcion()).isEqualTo("DESCRIPCIÓN ACTUALIZADA");
        assertThat(modificada.getOpciones()).hasSize(3);
        assertThat(modificada.getOpciones().get(0).getNombre()).isEqualTo("EN DESACUERDO");
        assertThat(modificada.getOpciones().get(2).getPonderacion()).isEqualTo(5.0);
    }
}

