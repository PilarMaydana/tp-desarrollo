package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dao.CompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto.CaracteristicaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto.PuestoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para {@link GestorPuesto}.
 */
@ExtendWith(MockitoExtension.class)
class GestorPuestoTest {

    @Mock
    private PuestoDAO puestoDAO;

    @Mock
    private CompetenciaDAO competenciaDAO;

    @Mock
    private ContextoSeguridad contextoSeguridad;

    @InjectMocks
    private GestorPuesto gestorPuesto;

    @Test
    @DisplayName("Alta exitosa de puesto retorna DTO con ID generado y características asociadas")
    void crearPuesto_exitoFlujoPrincipal() {
        CaracteristicaDTO car1 = new CaracteristicaDTO(1L, 8.5);
        CaracteristicaDTO car2 = new CaracteristicaDTO(2L, 7.0);
        PuestoDTO entrada = new PuestoDTO(null, "DEV-01", "DESARROLLADOR SENIOR", "DESCRIPCIÓN", "TECH CORP", List.of(car1, car2));

        Competencia comp1 = new Competencia(1L, "COM-01", "COMUNICACIÓN", "HABILIDAD COMUNICATIVA");
        Competencia comp2 = new Competencia(2L, "LID-01", "LIDERAZGO", "HABILIDAD DE LIDERAR");

        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(puestoDAO.existsByCodigo("DEV-01")).thenReturn(false);
        when(puestoDAO.existsByNombre("DESARROLLADOR SENIOR")).thenReturn(false);
        when(competenciaDAO.findById(1L)).thenReturn(Optional.of(comp1));
        when(competenciaDAO.findById(2L)).thenReturn(Optional.of(comp2));
        when(puestoDAO.save(any(Puesto.class))).thenAnswer(invocation -> {
            Puesto p = invocation.getArgument(0);
            p.setId(100L);
            p.getCaracteristicas().get(0).setId(10L);
            p.getCaracteristicas().get(1).setId(11L);
            return p;
        });

        PuestoDTO resultado = gestorPuesto.crearPuesto(entrada);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(100L);
        assertThat(resultado.getCodigo()).isEqualTo("DEV-01");
        assertThat(resultado.getNombre()).isEqualTo("DESARROLLADOR SENIOR");
        assertThat(resultado.getDescripcion()).isEqualTo("DESCRIPCIÓN");
        assertThat(resultado.getEmpresa()).isEqualTo("TECH CORP");
        assertThat(resultado.getCaracteristicas()).hasSize(2);
        assertThat(resultado.getCaracteristicas().get(0).getNombreCompetencia()).isEqualTo("COMUNICACIÓN");
        assertThat(resultado.getCaracteristicas().get(1).getNombreCompetencia()).isEqualTo("LIDERAZGO");

        verify(contextoSeguridad).getUsuarioActualId();
        verify(puestoDAO).save(any(Puesto.class));
    }

    @Test
    @DisplayName("Normaliza código, nombre, descripción y empresa aplicando trim y toUpperCase antes de validar y guardar")
    void crearPuesto_normalizaTextoMayusculasYTrim() {
        CaracteristicaDTO car = new CaracteristicaDTO(1L, 9.0);
        PuestoDTO entrada = new PuestoDTO(
                null,
                "   dev-02   ",
                "   arquitecto de software   ",
                "   diseño de arquitectura limpia   ",
                "   empresa innovadora sa   ",
                List.of(car)
        );

        Competencia comp = new Competencia(1L, "COM-01", "COMUNICACIÓN", "DESC");

        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(puestoDAO.existsByCodigo("DEV-02")).thenReturn(false);
        when(puestoDAO.existsByNombre("ARQUITECTO DE SOFTWARE")).thenReturn(false);
        when(competenciaDAO.findById(1L)).thenReturn(Optional.of(comp));
        when(puestoDAO.save(any(Puesto.class))).thenAnswer(invocation -> {
            Puesto p = invocation.getArgument(0);
            p.setId(101L);
            p.getCaracteristicas().get(0).setId(20L);
            return p;
        });

        PuestoDTO resultado = gestorPuesto.crearPuesto(entrada);

        ArgumentCaptor<Puesto> captor = ArgumentCaptor.forClass(Puesto.class);
        verify(puestoDAO).existsByCodigo("DEV-02");
        verify(puestoDAO).existsByNombre("ARQUITECTO DE SOFTWARE");
        verify(puestoDAO).save(captor.capture());

        Puesto guardado = captor.getValue();
        assertThat(guardado.getCodigo()).isEqualTo("DEV-02");
        assertThat(guardado.getNombre()).isEqualTo("ARQUITECTO DE SOFTWARE");
        assertThat(guardado.getDescripcion()).isEqualTo("DISEÑO DE ARQUITECTURA LIMPIA");
        assertThat(guardado.getEmpresa()).isEqualTo("EMPRESA INNOVADORA SA");
        assertThat(guardado.isEliminado()).isFalse();

        assertThat(resultado.getCodigo()).isEqualTo("DEV-02");
        assertThat(resultado.getNombre()).isEqualTo("ARQUITECTO DE SOFTWARE");
        assertThat(resultado.getDescripcion()).isEqualTo("DISEÑO DE ARQUITECTURA LIMPIA");
        assertThat(resultado.getEmpresa()).isEqualTo("EMPRESA INNOVADORA SA");
    }

    @Test
    @DisplayName("Flujo 2.B: Lanza RecursoDuplicadoException cuando el código ya existe")
    void crearPuesto_codigoDuplicadoLanzaExcepcion() {
        CaracteristicaDTO car = new CaracteristicaDTO(1L, 5.0);
        PuestoDTO entrada = new PuestoDTO(null, " dev-01 ", "Líder Técnico", "Descripción", "Empresa", List.of(car));

        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(puestoDAO.existsByCodigo("DEV-01")).thenReturn(true);

        assertThatThrownBy(() -> gestorPuesto.crearPuesto(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("código")
                .hasMessageContaining("DEV-01");

        verify(puestoDAO, never()).save(any());
    }

    @Test
    @DisplayName("Flujo 2.B: Lanza RecursoDuplicadoException cuando el nombre ya existe")
    void crearPuesto_nombreDuplicadoLanzaExcepcion() {
        CaracteristicaDTO car = new CaracteristicaDTO(1L, 5.0);
        PuestoDTO entrada = new PuestoDTO(null, "DEV-99", "  Desarrollador Senior  ", "Descripción", "Empresa", List.of(car));

        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(puestoDAO.existsByCodigo("DEV-99")).thenReturn(false);
        when(puestoDAO.existsByNombre("DESARROLLADOR SENIOR")).thenReturn(true);

        assertThatThrownBy(() -> gestorPuesto.crearPuesto(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("nombre")
                .hasMessageContaining("DESARROLLADOR SENIOR");

        verify(puestoDAO, never()).save(any());
    }

    @Test
    @DisplayName("Observaciones: Lanza ReglaNegocioException cuando hay competencias duplicadas en el request")
    void crearPuesto_competenciasDuplicadasLanzaReglaNegocioException() {
        CaracteristicaDTO car1 = new CaracteristicaDTO(1L, 7.0);
        CaracteristicaDTO car2 = new CaracteristicaDTO(1L, 9.0);
        PuestoDTO entrada = new PuestoDTO(null, "DEV-03", "ANALISTA FUNCIONAL", "DESCRIPCIÓN", "EMPRESA", List.of(car1, car2));

        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(puestoDAO.existsByCodigo("DEV-03")).thenReturn(false);
        when(puestoDAO.existsByNombre("ANALISTA FUNCIONAL")).thenReturn(false);

        assertThatThrownBy(() -> gestorPuesto.crearPuesto(entrada))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("repetida")
                .hasMessageContaining("1");

        verify(puestoDAO, never()).save(any());
    }

    @Test
    @DisplayName("Observaciones: Lanza ReglaNegocioException cuando la ponderación es menor a 0 o mayor a 10")
    void crearPuesto_ponderacionInvalidaLanzaReglaNegocioException() {
        CaracteristicaDTO carInvalida = new CaracteristicaDTO(1L, 11.0);
        PuestoDTO entrada = new PuestoDTO(null, "DEV-04", "ESPECIALISTA DEV", "DESC", "EMP", List.of(carInvalida));

        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(puestoDAO.existsByCodigo("DEV-04")).thenReturn(false);
        when(puestoDAO.existsByNombre("ESPECIALISTA DEV")).thenReturn(false);

        assertThatThrownBy(() -> gestorPuesto.crearPuesto(entrada))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ponderación");

        verify(puestoDAO, never()).save(any());
    }

    @Test
    @DisplayName("Lanza RecursoNoEncontradoException cuando la competencia referenciada no existe")
    void crearPuesto_competenciaNoExisteLanzaRecursoNoEncontrado() {
        CaracteristicaDTO car = new CaracteristicaDTO(999L, 8.0);
        PuestoDTO entrada = new PuestoDTO(null, "DEV-05", "JEFE DE PROYECTO", "DESC", "EMP", List.of(car));

        when(contextoSeguridad.getUsuarioActualId()).thenReturn(1L);
        when(puestoDAO.existsByCodigo("DEV-05")).thenReturn(false);
        when(puestoDAO.existsByNombre("JEFE DE PROYECTO")).thenReturn(false);
        when(competenciaDAO.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestorPuesto.crearPuesto(entrada))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Competencia");

        verify(puestoDAO, never()).save(any());
    }
}

