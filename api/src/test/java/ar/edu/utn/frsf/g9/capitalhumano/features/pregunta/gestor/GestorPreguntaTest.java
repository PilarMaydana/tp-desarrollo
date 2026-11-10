package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dao.CompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dto.FactorDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dao.PreguntaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaDTO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para {@link GestorPregunta} utilizando JUnit 5 y Mockito.
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
    @DisplayName("Alta exitosa de pregunta retorna DTO con ID generado y opciones persistidas")
    void crearPregunta_altaExitosa() {
        Factor factor = crearFactorPrueba();
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(factorDAO.findByCodigo("FAC-COM-01")).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorAndNombre(factor, "COMUNICACIÓN ASERTIVA")).thenReturn(false);
        when(preguntaDAO.existsByFactorAndEnunciado(factor, "¿CÓMO TRANSMITE SUS IDEAS?")).thenReturn(false);

        when(preguntaDAO.save(any(Pregunta.class))).thenAnswer(invocation -> {
            Pregunta p = invocation.getArgument(0);
            p.setId(10L);
            long opcionId = 100L;
            for (OpcionRespuesta op : p.getOpcionesRespuesta()) {
                op.setId(opcionId++);
            }
            return p;
        });

        List<OpcionRespuestaDTO> opciones = List.of(
                new OpcionRespuestaDTO(1, "NUNCA", "NUNCA LO HACE", 2.0),
                new OpcionRespuestaDTO(2, "A VECES", "A VECES LO HACE", 3.0),
                new OpcionRespuestaDTO(3, "SIEMPRE", "SIEMPRE LO HACE", 5.0)
        );
        PreguntaDTO entrada = new PreguntaDTO(null, "Comunicación Asertiva", "¿Cómo transmite sus ideas?", "Descripción de prueba", "fac-com-01", opciones);

        PreguntaDTO resultado = gestorPregunta.crearPregunta(entrada);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getNombre()).isEqualTo("COMUNICACIÓN ASERTIVA");
        assertThat(resultado.getEnunciado()).isEqualTo("¿CÓMO TRANSMITE SUS IDEAS?");
        assertThat(resultado.getDescripcion()).isEqualTo("DESCRIPCIÓN DE PRUEBA");
        assertThat(resultado.getCodigoFactor()).isEqualTo("FAC-COM-01");
        assertThat(resultado.getOpcionesRespuesta()).hasSize(3);
        assertThat(resultado.getOpcionesRespuesta().get(0).getPonderacion()).isEqualTo(2.0);
        assertThat(resultado.getOpcionesRespuesta().get(1).getPonderacion()).isEqualTo(3.0);
        assertThat(resultado.getOpcionesRespuesta().get(2).getPonderacion()).isEqualTo(5.0);

        verify(preguntaDAO).save(any(Pregunta.class));
    }

    @Test
    @DisplayName("Normaliza campos literales aplicando trim y mayúsculas antes de validar y persistir")
    void crearPregunta_normalizaLiteralesMayusculasYTrim() {
        Factor factor = crearFactorPrueba();
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(factorDAO.findByCodigo("FAC-COM-01")).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorAndNombre(factor, "PREGUNTA ESPACIOS")).thenReturn(false);
        when(preguntaDAO.existsByFactorAndEnunciado(factor, "¿ENUNCIADO CON ESPACIOS?")).thenReturn(false);

        when(preguntaDAO.save(any(Pregunta.class))).thenAnswer(invocation -> {
            Pregunta p = invocation.getArgument(0);
            p.setId(20L);
            return p;
        });

        List<OpcionRespuestaDTO> opciones = List.of(
                new OpcionRespuestaDTO(null, "  opcion uno  ", "  descripcion uno  ", 4.0),
                new OpcionRespuestaDTO(null, "  opcion dos  ", "  descripcion dos  ", 6.0)
        );
        PreguntaDTO entrada = new PreguntaDTO(null, "  Pregunta Espacios  ", "  ¿Enunciado con espacios?  ", "  descripcion opcional  ", "  fac-com-01  ", opciones);

        PreguntaDTO resultado = gestorPregunta.crearPregunta(entrada);

        ArgumentCaptor<Pregunta> captor = ArgumentCaptor.forClass(Pregunta.class);
        verify(preguntaDAO).save(captor.capture());
        Pregunta guardada = captor.getValue();

        assertThat(guardada.getNombre()).isEqualTo("PREGUNTA ESPACIOS");
        assertThat(guardada.getEnunciado()).isEqualTo("¿ENUNCIADO CON ESPACIOS?");
        assertThat(guardada.getDescripcion()).isEqualTo("DESCRIPCION OPCIONAL");
        assertThat(guardada.getOpcionesRespuesta().get(0).getNombre()).isEqualTo("OPCION UNO");
        assertThat(guardada.getOpcionesRespuesta().get(0).getDescripcion()).isEqualTo("DESCRIPCION UNO");
        assertThat(guardada.getOpcionesRespuesta().get(1).getNombre()).isEqualTo("OPCION DOS");
        assertThat(guardada.getOpcionesRespuesta().get(1).getDescripcion()).isEqualTo("DESCRIPCION DOS");

        assertThat(resultado.getNombre()).isEqualTo("PREGUNTA ESPACIOS");
    }

    @Test
    @DisplayName("Lanza RecursoDuplicadoException cuando el nombre de la pregunta ya existe para el factor")
    void crearPregunta_nombreDuplicado() {
        Factor factor = crearFactorPrueba();
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(factorDAO.findByCodigo("FAC-COM-01")).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorAndNombre(factor, "COMUNICACIÓN ASERTIVA")).thenReturn(true);

        List<OpcionRespuestaDTO> opciones = List.of(
                new OpcionRespuestaDTO(1, "OPCION 1", "DESC 1", 5.0),
                new OpcionRespuestaDTO(2, "OPCION 2", "DESC 2", 5.0)
        );
        PreguntaDTO entrada = new PreguntaDTO(null, "Comunicación Asertiva", "¿Enunciado nuevo?", null, "FAC-COM-01", opciones);

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("nombre")
                .hasMessageContaining("COMUNICACIÓN ASERTIVA");

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Lanza RecursoDuplicadoException cuando el enunciado de la pregunta ya existe para el factor")
    void crearPregunta_enunciadoDuplicado() {
        Factor factor = crearFactorPrueba();
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(factorDAO.findByCodigo("FAC-COM-01")).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorAndNombre(factor, "NUEVO NOMBRE")).thenReturn(false);
        when(preguntaDAO.existsByFactorAndEnunciado(factor, "¿ENUNCIADO EXISTENTE?")).thenReturn(true);

        List<OpcionRespuestaDTO> opciones = List.of(
                new OpcionRespuestaDTO(1, "OPCION 1", "DESC 1", 5.0),
                new OpcionRespuestaDTO(2, "OPCION 2", "DESC 2", 5.0)
        );
        PreguntaDTO entrada = new PreguntaDTO(null, "Nuevo Nombre", "¿Enunciado Existente?", null, "FAC-COM-01", opciones);

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("enunciado")
                .hasMessageContaining("¿ENUNCIADO EXISTENTE?");

        verify(preguntaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Lanza ReglaNegocioException cuando la suma de ponderaciones no es igual a 10")
    void crearPregunta_sumaPonderacionesDistintaDeDiez() {
        Factor factor = crearFactorPrueba();
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(factorDAO.findByCodigo("FAC-COM-01")).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorAndNombre(factor, "PREGUNTA TEST")).thenReturn(false);
        when(preguntaDAO.existsByFactorAndEnunciado(factor, "¿ENUNCIADO TEST?")).thenReturn(false);

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
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(factorDAO.findByCodigo("FAC-COM-01")).thenReturn(Optional.of(factor));
        when(preguntaDAO.existsByFactorAndNombre(factor, "PREGUNTA TEST")).thenReturn(false);
        when(preguntaDAO.existsByFactorAndEnunciado(factor, "¿ENUNCIADO TEST?")).thenReturn(false);

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
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);

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
    @DisplayName("Lanza RecursoNoEncontradoException cuando el factor no existe")
    void crearPregunta_factorNoExiste() {
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(factorDAO.findByCodigo("FAC-INEXISTENTE")).thenReturn(Optional.empty());

        List<OpcionRespuestaDTO> opciones = List.of(
                new OpcionRespuestaDTO(1, "OPCION 1", "DESC 1", 5.0),
                new OpcionRespuestaDTO(2, "OPCION 2", "DESC 2", 5.0)
        );
        PreguntaDTO entrada = new PreguntaDTO(null, "Pregunta Test", "¿Enunciado Test?", null, "FAC-INEXISTENTE", opciones);

        assertThatThrownBy(() -> gestorPregunta.crearPregunta(entrada))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("FAC-INEXISTENTE");

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
