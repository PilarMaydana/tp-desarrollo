package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta;

import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dao.PreguntaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaFiltroDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor.GestorPregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        PreguntaDTO mod = new PreguntaDTO(
                null,
                null,
                null,
                null,
                "Pregunta Integración Modificada",
                "¿Pregunta de prueba de integración actualizada?",
                "Descripción actualizada",
                List.of(
                        new OpcionRespuestaDTO("Opción 1 Mod", "Desc 1", 1, 3.0),
                        new OpcionRespuestaDTO("Opción 2 Mod", "Desc 2", 2, 7.0)
                )
        );

        PreguntaDTO actualizada = gestorPregunta.modificarPregunta(id, mod);
        assertThat(actualizada.getNombre()).isEqualTo("Pregunta Integración Modificada");
        assertThat(actualizada.getOpciones().get(0).getNombre()).isEqualTo("Opción 1 Mod");

        // 4. Baja lógica (CU 023)
        gestorPregunta.eliminarPregunta(id);

        Pregunta enBase = preguntaDAO.findById(id).orElseThrow();
        assertThat(enBase.getEliminado()).isTrue();
        assertThat(enBase.getFechaEliminacion()).isNotNull();
    }
}

