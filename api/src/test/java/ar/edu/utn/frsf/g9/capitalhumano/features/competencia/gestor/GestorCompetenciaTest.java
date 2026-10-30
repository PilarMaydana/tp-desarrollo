package ar.edu.utn.frsf.g9.capitalhumano.features.competencia.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dao.CompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dto.CompetenciaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para {@link GestorCompetencia}.
 */
@ExtendWith(MockitoExtension.class)
class GestorCompetenciaTest {

    @Mock
    private CompetenciaDAO competenciaDAO;

    @InjectMocks
    private GestorCompetencia gestorCompetencia;

    @Test
    @DisplayName("Alta exitosa de competencia retorna DTO con ID generado")
    void crearCompetencia_altaExitosa() {
        CompetenciaDTO entrada = new CompetenciaDTO(null, "COM-01", "COMUNICACIÓN", "TRANSMITIR IDEAS CLARAMENTE");

        when(competenciaDAO.existsByCodigo("COM-01")).thenReturn(false);
        when(competenciaDAO.existsByNombre("COMUNICACIÓN")).thenReturn(false);
        when(competenciaDAO.save(any(Competencia.class))).thenAnswer(invocation -> {
            Competencia entidad = invocation.getArgument(0);
            entidad.setId(1L);
            return entidad;
        });

        CompetenciaDTO resultado = gestorCompetencia.crearCompetencia(entrada);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getCodigo()).isEqualTo("COM-01");
        assertThat(resultado.getNombre()).isEqualTo("COMUNICACIÓN");
        assertThat(resultado.getDescripcion()).isEqualTo("TRANSMITIR IDEAS CLARAMENTE");
        verify(competenciaDAO).save(any(Competencia.class));
    }

    @Test
    @DisplayName("Normaliza código, nombre y descripción aplicando trim y pasando a mayúsculas antes de validar y guardar")
    void crearCompetencia_normalizaMayusculasYTrim() {
        CompetenciaDTO entrada = new CompetenciaDTO(
                null,
                "  com-01  ",
                "  Comunicación Efectiva  ",
                "  Capacidad para expresarse con claridad.  "
        );

        when(competenciaDAO.existsByCodigo("COM-01")).thenReturn(false);
        when(competenciaDAO.existsByNombre("COMUNICACIÓN EFECTIVA")).thenReturn(false);
        when(competenciaDAO.save(any(Competencia.class))).thenAnswer(invocation -> {
            Competencia entidad = invocation.getArgument(0);
            entidad.setId(10L);
            return entidad;
        });

        CompetenciaDTO resultado = gestorCompetencia.crearCompetencia(entrada);

        ArgumentCaptor<Competencia> captor = ArgumentCaptor.forClass(Competencia.class);
        verify(competenciaDAO).existsByCodigo("COM-01");
        verify(competenciaDAO).existsByNombre("COMUNICACIÓN EFECTIVA");
        verify(competenciaDAO).save(captor.capture());

        Competencia guardada = captor.getValue();
        assertThat(guardada.getCodigo()).isEqualTo("COM-01");
        assertThat(guardada.getNombre()).isEqualTo("COMUNICACIÓN EFECTIVA");
        assertThat(guardada.getDescripcion()).isEqualTo("CAPACIDAD PARA EXPRESARSE CON CLARIDAD.");

        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getCodigo()).isEqualTo("COM-01");
        assertThat(resultado.getNombre()).isEqualTo("COMUNICACIÓN EFECTIVA");
        assertThat(resultado.getDescripcion()).isEqualTo("CAPACIDAD PARA EXPRESARSE CON CLARIDAD.");
    }

    @Test
    @DisplayName("Lanza RecursoDuplicadoException cuando el código ya existe")
    void crearCompetencia_codigoDuplicado() {
        CompetenciaDTO entrada = new CompetenciaDTO(null, " com-01 ", "Trabajo en equipo", "Descripción de prueba");

        when(competenciaDAO.existsByCodigo("COM-01")).thenReturn(true);

        assertThatThrownBy(() -> gestorCompetencia.crearCompetencia(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("código")
                .hasMessageContaining("COM-01");

        verify(competenciaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Lanza RecursoDuplicadoException cuando el nombre ya existe")
    void crearCompetencia_nombreDuplicado() {
        CompetenciaDTO entrada = new CompetenciaDTO(null, "COM-99", "  Comunicación  ", "Descripción de prueba");

        when(competenciaDAO.existsByCodigo("COM-99")).thenReturn(false);
        when(competenciaDAO.existsByNombre("COMUNICACIÓN")).thenReturn(true);

        assertThatThrownBy(() -> gestorCompetencia.crearCompetencia(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("nombre")
                .hasMessageContaining("COMUNICACIÓN");

        verify(competenciaDAO, never()).save(any());
    }
}
