package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dao.PreguntaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaFiltroDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.ContextoSeguridad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para {@link GestorPregunta} (CU 020).
 */
@ExtendWith(MockitoExtension.class)
class GestorPreguntaTest {

    @Mock
    private PreguntaDAO preguntaDAO;

    @Mock
    private FactorDAO factorDAO;

    @Mock
    private ContextoSeguridad contextoSeguridad;

    @InjectMocks
    private GestorPregunta gestorPregunta;

    @Test
    @DisplayName("buscarPreguntas: lanza 400 Bad Request si no se envía ningún criterio de búsqueda")
    void buscarPreguntas_sinCriterios_lanza400() {
        PreguntaFiltroDTO filtroVacio = new PreguntaFiltroDTO(null, "", "   ");

        assertThatThrownBy(() -> gestorPregunta.buscarPreguntas(filtroVacio))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(preguntaDAO, never()).buscarConFiltros(any(), any(), any());
    }

    @Test
    @DisplayName("buscarPreguntas: retorna lista de DTOs coincidentes cuando se ingresa al menos un criterio")
    void buscarPreguntas_conCriterio_retornaLista() {
        Competencia comp = new Competencia(1L, "TEC-01", "COMPETENCIAS TÉCNICAS", "DESC");
        Factor factor = new Factor(1L, "FAC-01", "DESARROLLO DE SOFTWARE", "DESC", 1, comp);
        Pregunta pregunta = new Pregunta(1L, "PREGUNTA POO", "¿QUÉ ES LA POO?", "CONCEPTOS", factor);
        pregunta.addOpcion(new OpcionRespuesta(1L, pregunta, "OPCIÓN A", "PARADIGMA", 1, 1.0));
        pregunta.addOpcion(new OpcionRespuesta(2L, pregunta, "OPCIÓN B", "LENGUAJE", 2, 0.0));

        when(preguntaDAO.buscarConFiltros(eq("Tecnicas"), eq(null), eq(null)))
                .thenReturn(List.of(pregunta));

        PreguntaFiltroDTO filtro = new PreguntaFiltroDTO("Tecnicas", null, "");
        List<PreguntaDTO> resultados = gestorPregunta.buscarPreguntas(filtro);

        assertThat(resultados).hasSize(1);
        PreguntaDTO dto = resultados.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNombre()).isEqualTo("PREGUNTA POO");
        assertThat(dto.getCompetencia()).isEqualTo("COMPETENCIAS TÉCNICAS");
        assertThat(dto.getFactor()).isEqualTo("DESARROLLO DE SOFTWARE");
        assertThat(dto.getOpciones()).hasSize(2);
    }

    @Test
    @DisplayName("obtenerPorId: retorna DTO de pregunta si existe y está activa")
    void obtenerPorId_exitoso() {
        Competencia comp = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Factor factor = new Factor(1L, "FAC-01", "ORAL", "DESC", 1, comp);
        Pregunta pregunta = new Pregunta(10L, "¿CÓMO TE COMUNICÁS?", "ENUNCIADO", "DESC", factor);
        pregunta.addOpcion(new OpcionRespuesta(1L, pregunta, "BIEN", "DESC", 1, 10.0));
        pregunta.addOpcion(new OpcionRespuesta(2L, pregunta, "MAL", "DESC", 2, 0.0));

        when(preguntaDAO.findByIdAndEliminadoFalse(10L)).thenReturn(Optional.of(pregunta));

        PreguntaDTO resultado = gestorPregunta.obtenerPorId(10L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getNombre()).isEqualTo("¿CÓMO TE COMUNICÁS?");
        assertThat(resultado.getOpciones()).hasSize(2);
    }

    @Test
    @DisplayName("obtenerPorId: lanza 404 Not Found si la pregunta no existe o está eliminada")
    void obtenerPorId_noExiste_lanza404() {
        when(preguntaDAO.findByIdAndEliminadoFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestorPregunta.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("crearPregunta: da de alta con éxito asignando factor y opciones")
    void crearPregunta_flujoExitoso() {
        Competencia comp = new Competencia(1L, "TEC-01", "COMPETENCIAS TÉCNICAS", "DESC");
        Factor factor = new Factor(2L, "FAC-01", "DESARROLLO", "DESC", 1, comp);

        when(factorDAO.findByCodigo("FAC-01")).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(2L, "Patrones de Diseño")).thenReturn(false);
        when(preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndEliminadoFalse(2L, "¿Para qué sirve Singleton?")).thenReturn(false);

        when(preguntaDAO.save(any(Pregunta.class))).thenAnswer(invocation -> {
            Pregunta p = invocation.getArgument(0);
            p.setId(5L);
            return p;
        });

        PreguntaDTO entrada = new PreguntaDTO(
                null,
                null,
                null,
                "FAC-01",
                "Patrones de Diseño",
                "¿Para qué sirve Singleton?",
                "Pregunta de arquitectura",
                List.of(
                        new OpcionRespuestaDTO("Instancia única", "Garantiza una única instancia", 1.0),
                        new OpcionRespuestaDTO("Múltiples instancias", "Crea muchas instancias", 0.0)
                )
        );

        PreguntaDTO creada = gestorPregunta.crearPregunta(entrada);

        assertThat(creada.getId()).isEqualTo(5L);
        assertThat(creada.getNombre()).isEqualTo("Patrones de Diseño");
        assertThat(creada.getEnunciado()).isEqualTo("¿Para qué sirve Singleton?");
        assertThat(creada.getFactor()).isEqualTo("DESARROLLO");
        assertThat(creada.getCompetencia()).isEqualTo("COMPETENCIAS TÉCNICAS");
        assertThat(creada.getOpciones()).hasSize(2);

        ArgumentCaptor<Pregunta> captor = ArgumentCaptor.forClass(Pregunta.class);
        verify(preguntaDAO).save(captor.capture());
        Pregunta persistida = captor.getValue();
        assertThat(persistida.getFactor()).isEqualTo(factor);
        assertThat(persistida.getOpciones()).hasSize(2);
        assertThat(persistida.getOpciones().get(0).getNroOrden()).isEqualTo(1);
        assertThat(persistida.getOpciones().get(1).getNroOrden()).isEqualTo(2);
    }

    @Test
    @DisplayName("crearPregunta: lanza 404 si el factor especificado no existe")
    void crearPregunta_factorNoExiste_lanza404() {
        when(factorDAO.findByCodigo(anyString())).thenReturn(Optional.empty());

        PreguntaDTO entrada = new PreguntaDTO(
                null, null, null, "FAC-INEXISTENTE", "Nombre", "Enunciado", "Desc",
                List.of(
                        new OpcionRespuestaDTO("A", "Desc", 1.0),
                        new OpcionRespuestaDTO("B", "Desc", 0.0)
                )
        );

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("crearPregunta: lanza 409 Conflict si ya existe una pregunta con el mismo nombre en el factor")
    void crearPregunta_nombreDuplicado_lanza409() {
        Factor factor = new Factor(1L, "FAC-01", "FACTOR", "DESC", 1, null);
        when(factorDAO.findByCodigo("FAC-01")).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(1L, "Pregunta Repetida")).thenReturn(true);

        PreguntaDTO entrada = new PreguntaDTO(
                null, null, null, "FAC-01", "Pregunta Repetida", "Enunciado Único", "Desc",
                List.of(
                        new OpcionRespuestaDTO("A", "Desc", 1.0),
                        new OpcionRespuestaDTO("B", "Desc", 0.0)
                )
        );

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("nombre 'Pregunta Repetida'");

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("crearPregunta: lanza 409 Conflict si ya existe una pregunta con el mismo enunciado en el factor")
    void crearPregunta_enunciadoDuplicado_lanza409() {
        Factor factor = new Factor(1L, "FAC-01", "FACTOR", "DESC", 1, null);
        when(factorDAO.findByCodigo("FAC-01")).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(1L, "Nombre Nuevo")).thenReturn(false);
        when(preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndEliminadoFalse(1L, "Enunciado Repetido")).thenReturn(true);

        PreguntaDTO entrada = new PreguntaDTO(
                null, null, null, "FAC-01", "Nombre Nuevo", "Enunciado Repetido", "Desc",
                List.of(
                        new OpcionRespuestaDTO("A", "Desc", 1.0),
                        new OpcionRespuestaDTO("B", "Desc", 0.0)
                )
        );

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("enunciado 'Enunciado Repetido'");

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("crearPregunta: lanza 400 Bad Request si la pregunta tiene menos de 2 opciones")
    void crearPregunta_menosDeDosOpciones_lanza400() {
        Factor factor = new Factor(1L, "FAC-01", "FACTOR", "DESC", 1, null);
        when(factorDAO.findByCodigo("FAC-01")).thenReturn(Optional.of(factor));

        PreguntaDTO entrada = new PreguntaDTO(
                null, null, null, "FAC-01", "Nombre", "Enunciado", "Desc",
                List.of(new OpcionRespuestaDTO("Solo una opción", "Desc", 1.0))
        );

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("modificarPregunta: actualiza con éxito los datos y opciones de una pregunta existente")
    void modificarPregunta_flujoExitoso() {
        Factor factor = new Factor(1L, "FAC-01", "FACTOR", "DESC", 1, null);
        Pregunta existente = new Pregunta(3L, "Nombre Viejo", "Enunciado Viejo", "Desc Vieja", factor);
        existente.addOpcion(new OpcionRespuesta(1L, existente, "Op 1", "Desc 1", 1, 1.0));
        existente.addOpcion(new OpcionRespuesta(2L, existente, "Op 2", "Desc 2", 2, 0.0));

        when(preguntaDAO.findByIdAndEliminadoFalse(3L)).thenReturn(Optional.of(existente));
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndIdNotAndEliminadoFalse(1L, "Nombre Modificado", 3L)).thenReturn(false);
        when(preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndIdNotAndEliminadoFalse(1L, "Enunciado Modificado", 3L)).thenReturn(false);
        when(preguntaDAO.save(any(Pregunta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PreguntaDTO updateDTO = new PreguntaDTO(
                null, null, null, null, "Nombre Modificado", "Enunciado Modificado", "Desc Nueva",
                List.of(
                        new OpcionRespuestaDTO("Nueva Op A", "Desc A", 0.5),
                        new OpcionRespuestaDTO("Nueva Op B", "Desc B", 0.5)
                )
        );

        PreguntaDTO resultado = gestorPregunta.modificarPregunta(3L, updateDTO);

        assertThat(resultado.getId()).isEqualTo(3L);
        assertThat(resultado.getNombre()).isEqualTo("Nombre Modificado");
        assertThat(resultado.getEnunciado()).isEqualTo("Enunciado Modificado");
        assertThat(resultado.getOpciones()).hasSize(2);
        assertThat(resultado.getOpciones().get(0).getNombre()).isEqualTo("Nueva Op A");

        verify(preguntaDAO).save(existente);
    }

    @Test
    @DisplayName("modificarPregunta: lanza 404 si la pregunta no existe")
    void modificarPregunta_noExiste_lanza404() {
        when(preguntaDAO.findByIdAndEliminadoFalse(99L)).thenReturn(Optional.empty());

        PreguntaDTO updateDTO = new PreguntaDTO(
                null, null, null, null, "Nombre", "Enunciado", "Desc",
                List.of(
                        new OpcionRespuestaDTO("A", "Desc", 1.0),
                        new OpcionRespuestaDTO("B", "Desc", 0.0)
                )
        );

        assertThatThrownBy(() -> gestorPregunta.modificarPregunta(99L, updateDTO))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("modificarPregunta: lanza 409 Conflict si el nuevo nombre ya está tomado por otra pregunta en el factor")
    void modificarPregunta_nombreDuplicado_lanza409() {
        Factor factor = new Factor(1L, "FAC-01", "FACTOR", "DESC", 1, null);
        Pregunta existente = new Pregunta(3L, "Nombre Original", "Enunciado", "Desc", factor);

        when(preguntaDAO.findByIdAndEliminadoFalse(3L)).thenReturn(Optional.of(existente));
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndIdNotAndEliminadoFalse(1L, "Nombre Ocupado", 3L)).thenReturn(true);

        PreguntaDTO updateDTO = new PreguntaDTO(
                null, null, null, null, "Nombre Ocupado", "Enunciado", "Desc",
                List.of(
                        new OpcionRespuestaDTO("A", "Desc", 1.0),
                        new OpcionRespuestaDTO("B", "Desc", 0.0)
                )
        );

        assertThatThrownBy(() -> gestorPregunta.modificarPregunta(3L, updateDTO))
                .isInstanceOf(RecursoDuplicadoException.class);

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("eliminarPregunta: aplica baja lógica registrando usuario y fecha de auditoría")
    void eliminarPregunta_flujoExitoso() {
        Factor factor = new Factor(1L, "FAC-01", "FACTOR", "DESC", 1, null);
        Pregunta existente = new Pregunta(7L, "Pregunta a borrar", "Enunciado", "Desc", factor);

        when(preguntaDAO.findByIdAndEliminadoFalse(7L)).thenReturn(Optional.of(existente));
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(42L);

        gestorPregunta.eliminarPregunta(7L);

        assertThat(existente.getEliminado()).isTrue();
        assertThat(existente.getFechaEliminacion()).isNotNull();
        assertThat(existente.getUsuarioEliminacion()).isEqualTo(42L);

        verify(preguntaDAO).save(existente);
    }

    @Test
    @DisplayName("eliminarPregunta: lanza 404 si la pregunta no existe o ya fue eliminada")
    void eliminarPregunta_noExiste_lanza404() {
        when(preguntaDAO.findByIdAndEliminadoFalse(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestorPregunta.eliminarPregunta(88L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(preguntaDAO, never()).save(any());
    }
}

