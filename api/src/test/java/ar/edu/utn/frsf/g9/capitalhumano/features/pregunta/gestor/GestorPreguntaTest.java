package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dao.CompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dto.FactorDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dao.PreguntaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaFiltroDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
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
 * Tests unitarios para {@link GestorPregunta} (CU 020 / CU 021).
 */
@ExtendWith(MockitoExtension.class)
class GestorPreguntaTest {

    @Mock
    private PreguntaDAO preguntaDAO;

    @Mock
    private FactorDAO factorDAO;

    @Mock
    private CompetenciaDAO competenciaDAO;

    @Mock
    private ContextoSeguridad contextoSeguridad;

    @InjectMocks
    private GestorPregunta gestorPregunta;

    private Factor crearFactorPrueba() {
        Competencia competencia = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESCRIPCIÓN COMUNICACIÓN");
        return new Factor(1L, "FAC-COM-01", "CLARIDAD Y CONCISIÓN", "DESCRIPCIÓN FACTOR", 1, competencia);
    }

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
    @DisplayName("crearPregunta: da de alta con éxito asignando factor y opciones con suma 10")
    void crearPregunta_flujoExitoso() {
        Competencia comp = new Competencia(1L, "TEC-01", "COMPETENCIAS TÉCNICAS", "DESC");
        Factor factor = new Factor(2L, "FAC-01", "DESARROLLO", "DESC", 1, comp);

        when(factorDAO.findByCodigo(anyString())).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(eq(2L), anyString())).thenReturn(false);
        when(preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndEliminadoFalse(eq(2L), anyString())).thenReturn(false);

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
                        new OpcionRespuestaDTO("Instancia única", "Garantiza una única instancia", 5.0),
                        new OpcionRespuestaDTO("Múltiples instancias", "Crea muchas instancias", 5.0)
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
    @DisplayName("Normaliza campos literales aplicando trim antes de validar y persistir")
    void crearPregunta_normalizaLiteralesTrim() {
        Factor factor = crearFactorPrueba();
        when(factorDAO.findByCodigo(anyString())).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(eq(1L), anyString())).thenReturn(false);
        when(preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndEliminadoFalse(eq(1L), anyString())).thenReturn(false);

        when(preguntaDAO.save(any(Pregunta.class))).thenAnswer(invocation -> {
            Pregunta p = invocation.getArgument(0);
            p.setId(20L);
            return p;
        });

        List<OpcionRespuestaDTO> opciones = List.of(
                new OpcionRespuestaDTO("  opcion uno  ", "  descripcion uno  ", 4.0),
                new OpcionRespuestaDTO("  opcion dos  ", "  descripcion dos  ", 6.0)
        );
        PreguntaDTO entrada = new PreguntaDTO(null, "  Pregunta Espacios  ", "  ¿Enunciado con espacios?  ", "  descripcion opcional  ", "  fac-com-01  ", opciones);

        PreguntaDTO resultado = gestorPregunta.crearPregunta(entrada);

        ArgumentCaptor<Pregunta> captor = ArgumentCaptor.forClass(Pregunta.class);
        verify(preguntaDAO).save(captor.capture());
        Pregunta guardada = captor.getValue();

        assertThat(guardada.getNombre()).isEqualTo("Pregunta Espacios");
        assertThat(guardada.getEnunciado()).isEqualTo("¿Enunciado con espacios?");
        assertThat(guardada.getDescripcion()).isEqualTo("descripcion opcional");
        assertThat(guardada.getOpciones().get(0).getNombre()).isEqualTo("opcion uno");
        assertThat(guardada.getOpciones().get(0).getDescripcion()).isEqualTo("descripcion uno");
        assertThat(guardada.getOpciones().get(1).getNombre()).isEqualTo("opcion dos");
        assertThat(guardada.getOpciones().get(1).getDescripcion()).isEqualTo("descripcion dos");

        assertThat(resultado.getNombre()).isEqualTo("Pregunta Espacios");
    }

    @Test
    @DisplayName("crearPregunta: lanza 404 si el factor especificado no existe")
    void crearPregunta_factorNoExiste_lanza404() {
        when(factorDAO.findByCodigo(anyString())).thenReturn(Optional.empty());

        PreguntaDTO entrada = new PreguntaDTO(
                null, null, null, "FAC-INEXISTENTE", "Nombre", "Enunciado", "Desc",
                List.of(
                        new OpcionRespuestaDTO("A", "Desc", 5.0),
                        new OpcionRespuestaDTO("B", "Desc", 5.0)
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
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(eq(1L), anyString())).thenReturn(true);

        PreguntaDTO entrada = new PreguntaDTO(
                null, null, null, "FAC-01", "Pregunta Repetida", "Enunciado Único", "Desc",
                List.of(
                        new OpcionRespuestaDTO("A", "Desc", 5.0),
                        new OpcionRespuestaDTO("B", "Desc", 5.0)
                )
        );

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("nombre");

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("crearPregunta: lanza 409 Conflict si ya existe una pregunta con el mismo enunciado en el factor")
    void crearPregunta_enunciadoDuplicado_lanza409() {
        Factor factor = new Factor(1L, "FAC-01", "FACTOR", "DESC", 1, null);
        when(factorDAO.findByCodigo("FAC-01")).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(eq(1L), anyString())).thenReturn(false);
        when(preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndEliminadoFalse(eq(1L), anyString())).thenReturn(true);

        PreguntaDTO entrada = new PreguntaDTO(
                null, null, null, "FAC-01", "Pregunta Única", "Enunciado Repetido", "Desc",
                List.of(
                        new OpcionRespuestaDTO("A", "Desc", 5.0),
                        new OpcionRespuestaDTO("B", "Desc", 5.0)
                )
        );

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("enunciado");

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Lanza ReglaNegocioException cuando la suma de ponderaciones no es igual a 10")
    void crearPregunta_sumaPonderacionesDistintaDeDiez() {
        Factor factor = crearFactorPrueba();
        when(factorDAO.findByCodigo(anyString())).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(eq(1L), anyString())).thenReturn(false);
        when(preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndEliminadoFalse(eq(1L), anyString())).thenReturn(false);

        List<OpcionRespuestaDTO> opciones = List.of(
                new OpcionRespuestaDTO(1, "OPCION 1", "DESC 1", 3.0),
                new OpcionRespuestaDTO(2, "OPCION 2", "DESC 2", 4.0)
        ); // suma = 7.0 != 10.0
        PreguntaDTO entrada = new PreguntaDTO(null, "Pregunta Test", "¿Enunciado Test?", null, "FAC-COM-01", opciones);

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("10 puntos")
                .hasMessageContaining("7.0");

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Lanza ReglaNegocioException cuando una ponderación es negativa")
    void crearPregunta_ponderacionNegativa() {
        Factor factor = crearFactorPrueba();
        when(factorDAO.findByCodigo(anyString())).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(eq(1L), anyString())).thenReturn(false);
        when(preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndEliminadoFalse(eq(1L), anyString())).thenReturn(false);

        List<OpcionRespuestaDTO> opciones = List.of(
                new OpcionRespuestaDTO(1, "OPCION 1", "DESC 1", -2.0),
                new OpcionRespuestaDTO(2, "OPCION 2", "DESC 2", 12.0)
        );
        PreguntaDTO entrada = new PreguntaDTO(null, "Pregunta Test", "¿Enunciado Test?", null, "FAC-COM-01", opciones);

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("mayor o igual a cero");

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Lanza ReglaNegocioException cuando se proporcionan menos de 2 opciones")
    void crearPregunta_menosDeDosOpciones() {
        Factor factor = crearFactorPrueba();
        when(factorDAO.findByCodigo(anyString())).thenReturn(Optional.of(factor));

        List<OpcionRespuestaDTO> opciones = List.of(
                new OpcionRespuestaDTO(1, "OPCION UNICA", "DESC UNICA", 10.0)
        );
        PreguntaDTO entrada = new PreguntaDTO(null, "Pregunta Test", "¿Enunciado Test?", null, "FAC-COM-01", opciones);

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("al menos 2 opciones");

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("modificarPregunta: modifica los datos y opciones de una pregunta existente")
    void modificarPregunta_exitoso() {
        Competencia comp = new Competencia(1L, "TEC-01", "COMPETENCIAS TÉCNICAS", "DESC");
        Factor factor = new Factor(1L, "FAC-01", "DESARROLLO", "DESC", 1, comp);
        Pregunta existente = new Pregunta(1L, "NOMBRE VIEJO", "ENUNCIADO VIEJO", "DESC VIEJA", factor);

        when(preguntaDAO.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(existente));
        when(preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndIdNotAndEliminadoFalse(1L, "NOMBRE NUEVO", 1L)).thenReturn(false);
        when(preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndIdNotAndEliminadoFalse(1L, "ENUNCIADO NUEVO", 1L)).thenReturn(false);
        when(preguntaDAO.save(any(Pregunta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PreguntaDTO actualizacion = new PreguntaDTO(
                null, null, null, null, "NOMBRE NUEVO", "ENUNCIADO NUEVO", "DESC NUEVA",
                List.of(
                        new OpcionRespuestaDTO("Opcion 1", "Desc 1", 1, 1.0),
                        new OpcionRespuestaDTO("Opcion 2", "Desc 2", 2, 0.0)
                )
        );

        PreguntaDTO resultado = gestorPregunta.modificarPregunta(1L, actualizacion);

        assertThat(resultado.getNombre()).isEqualTo("NOMBRE NUEVO");
        assertThat(resultado.getEnunciado()).isEqualTo("ENUNCIADO NUEVO");
        assertThat(resultado.getDescripcion()).isEqualTo("DESC NUEVA");
        assertThat(resultado.getOpciones()).hasSize(2);

        verify(preguntaDAO).save(existente);
    }

    @Test
    @DisplayName("modificarPregunta: lanza 404 si la pregunta no existe")
    void modificarPregunta_noExiste_lanza404() {
        when(preguntaDAO.findByIdAndEliminadoFalse(99L)).thenReturn(Optional.empty());

        PreguntaDTO dto = new PreguntaDTO(null, null, null, null, "A", "B", "C",
                List.of(new OpcionRespuestaDTO("A", "B", 1.0), new OpcionRespuestaDTO("C", "D", 0.0)));

        assertThatThrownBy(() -> gestorPregunta.modificarPregunta(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("eliminarPregunta: aplica baja lógica registrando auditoría")
    void eliminarPregunta_exitoso() {
        Competencia comp = new Competencia(1L, "TEC-01", "COMP", "DESC");
        Factor factor = new Factor(1L, "FAC-01", "FAC", "DESC", 1, comp);
        Pregunta existente = new Pregunta(1L, "NOMBRE", "ENUNCIADO", "DESC", factor);

        when(preguntaDAO.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(existente));
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(77L);

        gestorPregunta.eliminarPregunta(1L);

        assertThat(existente.getEliminado()).isTrue();
        assertThat(existente.getFechaEliminacion()).isNotNull();
        assertThat(existente.getUsuarioEliminacion()).isEqualTo(77L);

        verify(preguntaDAO).save(existente);
    }

    @Test
    @DisplayName("eliminarPregunta: lanza 404 si la pregunta no existe")
    void eliminarPregunta_noExiste_lanza404() {
        when(preguntaDAO.findByIdAndEliminadoFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestorPregunta.eliminarPregunta(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Listar factores por competencia retorna lista ordenada por nroOrden")
    void listarFactoresPorCompetencia_exitoso() {
        Competencia competencia = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESCRIPCIÓN");
        Factor f1 = new Factor(1L, "FAC-01", "FACTOR 1", "DESC 1", 1, competencia);
        Factor f2 = new Factor(2L, "FAC-02", "FACTOR 2", "DESC 2", 2, competencia);

        when(competenciaDAO.existsByCodigo("COM-01")).thenReturn(true);
        when(factorDAO.findByCompetenciaCodigoOrderByNroOrdenAsc("COM-01")).thenReturn(List.of(f1, f2));

        List<FactorDTO> resultado = gestorPregunta.listarFactoresPorCompetencia("COM-01");

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getCodigo()).isEqualTo("FAC-01");
        assertThat(resultado.get(0).getNroOrden()).isEqualTo(1);
        assertThat(resultado.get(1).getCodigo()).isEqualTo("FAC-02");
        assertThat(resultado.get(1).getNroOrden()).isEqualTo(2);
    }

    @Test
    @DisplayName("Listar factores por competencia lanza RecursoNoEncontradoException si la competencia no existe")
    void listarFactoresPorCompetencia_competenciaNoExiste() {
        when(competenciaDAO.existsByCodigo("COM-99")).thenReturn(false);

        assertThatThrownBy(() -> gestorPregunta.listarFactoresPorCompetencia("COM-99"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("COM-99");
    }
}
