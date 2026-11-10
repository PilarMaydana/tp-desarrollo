package ar.edu.utn.frsf.g9.capitalhumano.features.factor.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dao.CompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dto.FactorDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para {@link GestorFactor} (CU 017).
 */
@ExtendWith(MockitoExtension.class)
class GestorFactorTest {

    @Mock
    private FactorDAO factorDAO;

    @Mock
    private CompetenciaDAO competenciaDAO;

    @Mock
    private ContextoSeguridad contextoSeguridad;

    @InjectMocks
    private GestorFactor gestorFactor;

    @Test
    @DisplayName("Flujo Principal: Normaliza campos a mayúsculas, asigna competencia y guarda con éxito retornando DTO con ID")
    void crearFactor_flujoPrincipalExitoso() {
        FactorDTO entrada = new FactorDTO(
                null,
                "  fac-03  ",
                "  Capacidad Analítica  ",
                "  Habilidad para descomponer problemas.  ",
                1L,
                3
        );

        Competencia competencia = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESCRIPCIÓN");

        when(factorDAO.existsByCodigo("FAC-03")).thenReturn(false);
        when(factorDAO.existsByNombre("CAPACIDAD ANALÍTICA")).thenReturn(false);
        when(competenciaDAO.findById(1L)).thenReturn(Optional.of(competencia));
        when(factorDAO.existsByCompetenciaIdAndNroOrden(1L, 3)).thenReturn(false);
        when(factorDAO.save(any(Factor.class))).thenAnswer(invocation -> {
            Factor entidad = invocation.getArgument(0);
            entidad.setId(10L);
            return entidad;
        });

        FactorDTO resultado = gestorFactor.crearFactor(entrada);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getCodigo()).isEqualTo("FAC-03");
        assertThat(resultado.getNombre()).isEqualTo("CAPACIDAD ANALÍTICA");
        assertThat(resultado.getDescripcion()).isEqualTo("HABILIDAD PARA DESCOMPONER PROBLEMAS.");
        assertThat(resultado.getCompetenciaId()).isEqualTo(1L);
        assertThat(resultado.getNroOrden()).isEqualTo(3);

        ArgumentCaptor<Factor> captor = ArgumentCaptor.forClass(Factor.class);
        verify(factorDAO).save(captor.capture());
        Factor guardado = captor.getValue();
        assertThat(guardado.getCodigo()).isEqualTo("FAC-03");
        assertThat(guardado.getNombre()).isEqualTo("CAPACIDAD ANALÍTICA");
        assertThat(guardado.getDescripcion()).isEqualTo("HABILIDAD PARA DESCOMPONER PROBLEMAS.");
        assertThat(guardado.getNroOrden()).isEqualTo(3);
        assertThat(guardado.getCompetencia()).isEqualTo(competencia);
        assertThat(guardado.isEliminado()).isFalse();
    }

    @Test
    @DisplayName("Flujo 2.B: Intento de guardar con código existente lanza RecursoDuplicadoException")
    void crearFactor_codigoDuplicado() {
        FactorDTO entrada = new FactorDTO(
                null,
                "  fac-01  ",
                "Nuevo Factor",
                "Descripción de prueba",
                1L,
                1
        );

        when(factorDAO.existsByCodigo("FAC-01")).thenReturn(true);

        assertThatThrownBy(() -> gestorFactor.crearFactor(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("código")
                .hasMessageContaining("FAC-01");

        verify(competenciaDAO, never()).findById(any());
        verify(factorDAO, never()).save(any());
    }

    @Test
    @DisplayName("Flujo 2.B: Intento de guardar con nombre existente lanza RecursoDuplicadoException")
    void crearFactor_nombreDuplicado() {
        FactorDTO entrada = new FactorDTO(
                null,
                "FAC-99",
                "  Comunicación Oral  ",
                "Descripción de prueba",
                1L,
                1
        );

        when(factorDAO.existsByCodigo("FAC-99")).thenReturn(false);
        when(factorDAO.existsByNombre("COMUNICACIÓN ORAL")).thenReturn(true);

        assertThatThrownBy(() -> gestorFactor.crearFactor(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("nombre")
                .hasMessageContaining("COMUNICACIÓN ORAL");

        verify(competenciaDAO, never()).findById(any());
        verify(factorDAO, never()).save(any());
    }

    @Test
    @DisplayName("Flujo 2.C: Intento de guardar con nroOrden ya usado en esa competencia lanza RecursoDuplicadoException")
    void crearFactor_nroOrdenDuplicadoEnCompetencia() {
        FactorDTO entrada = new FactorDTO(
                null,
                "FAC-99",
                "Nuevo Factor",
                "Descripción de prueba",
                1L,
                2
        );

        Competencia competencia = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESCRIPCIÓN");

        when(factorDAO.existsByCodigo("FAC-99")).thenReturn(false);
        when(factorDAO.existsByNombre("NUEVO FACTOR")).thenReturn(false);
        when(competenciaDAO.findById(1L)).thenReturn(Optional.of(competencia));
        when(factorDAO.existsByCompetenciaIdAndNroOrden(1L, 2)).thenReturn(true);

        assertThatThrownBy(() -> gestorFactor.crearFactor(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("número de orden")
                .hasMessageContaining("2");

        verify(factorDAO, never()).save(any());
    }

    @Test
    @DisplayName("Relaciones: Competencia inexistente lanza RecursoNoEncontradoException")
    void crearFactor_competenciaInexistente() {
        FactorDTO entrada = new FactorDTO(
                null,
                "FAC-99",
                "Nuevo Factor",
                "Descripción de prueba",
                999L,
                1
        );

        when(factorDAO.existsByCodigo("FAC-99")).thenReturn(false);
        when(factorDAO.existsByNombre("NUEVO FACTOR")).thenReturn(false);
        when(competenciaDAO.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestorFactor.crearFactor(entrada))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Competencia")
                .hasMessageContaining("999");

        verify(factorDAO, never()).save(any());
    }

    @Test
    @DisplayName("Listar factores retorna todos los factores mapeados a DTO")
    void listarFactores_exitoso() {
        Competencia comp = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");
        Factor factor = new Factor(1L, "FAC-01", "COMUNICACIÓN ORAL", "DESC", 1, comp);

        when(factorDAO.findAll()).thenReturn(List.of(factor));

        List<FactorDTO> resultado = gestorFactor.listarFactores();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
        assertThat(resultado.get(0).getCodigo()).isEqualTo("FAC-01");
        assertThat(resultado.get(0).getCompetenciaId()).isEqualTo(1L);
    }
}

