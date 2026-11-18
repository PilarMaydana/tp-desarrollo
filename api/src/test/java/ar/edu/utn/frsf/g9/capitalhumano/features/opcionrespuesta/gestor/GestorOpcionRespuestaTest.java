package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dao.OpcionRespuestaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.ItemOpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para {@link GestorOpcionRespuesta} (CU 029).
 */
@ExtendWith(MockitoExtension.class)
class GestorOpcionRespuestaTest {

    @Mock
    private OpcionRespuestaDAO opcionRespuestaDAO;

    @InjectMocks
    private GestorOpcionRespuesta gestorOpcionRespuesta;

    @Test
    @DisplayName("Flujo Principal: alta exitosa de opción de respuesta retorna DTO con ID generado y sus ítems")
    void crearOpcionRespuesta_flujoPrincipalExitoso() {
        List<ItemOpcionRespuestaDTO> items = List.of(
                new ItemOpcionRespuestaDTO(1, "MALO"),
                new ItemOpcionRespuestaDTO(2, "REGULAR"),
                new ItemOpcionRespuestaDTO(3, "BUENO")
        );
        OpcionRespuestaDTO entrada = new OpcionRespuestaDTO(
                null,
                "CALIFICACIÓN",
                "ESCALA DE CALIFICACIÓN DEL DESEMPEÑO",
                items
        );

        when(opcionRespuestaDAO.existsByNombre("CALIFICACIÓN")).thenReturn(false);
        when(opcionRespuestaDAO.save(any(OpcionRespuesta.class))).thenAnswer(invocation -> {
            OpcionRespuesta op = invocation.getArgument(0);
            op.setId(10L);
            return op;
        });

        OpcionRespuestaDTO resultado = gestorOpcionRespuesta.crearOpcionRespuesta(entrada);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getNombre()).isEqualTo("CALIFICACIÓN");
        assertThat(resultado.getDescripcion()).isEqualTo("ESCALA DE CALIFICACIÓN DEL DESEMPEÑO");
        assertThat(resultado.getItems()).hasSize(3);
        assertThat(resultado.getItems().get(0).getOrden()).isEqualTo(1);
        assertThat(resultado.getItems().get(0).getDescripcion()).isEqualTo("MALO");
        assertThat(resultado.getItems().get(1).getOrden()).isEqualTo(2);
        assertThat(resultado.getItems().get(1).getDescripcion()).isEqualTo("REGULAR");
        assertThat(resultado.getItems().get(2).getOrden()).isEqualTo(3);
        assertThat(resultado.getItems().get(2).getDescripcion()).isEqualTo("BUENO");

        verify(opcionRespuestaDAO).existsByNombre("CALIFICACIÓN");
        verify(opcionRespuestaDAO).save(any(OpcionRespuesta.class));
    }

    @Test
    @DisplayName("Normalización: aplica trim y toUpperCase en nombre, descripción y descripción de cada ítem antes de validar y guardar")
    void crearOpcionRespuesta_normalizaTextoMayusculasYTrim() {
        List<ItemOpcionRespuestaDTO> items = List.of(
                new ItemOpcionRespuestaDTO(1, "   nunca   "),
                new ItemOpcionRespuestaDTO(2, "   siempre   ")
        );
        OpcionRespuestaDTO entrada = new OpcionRespuestaDTO(
                null,
                "   frecuencia laboral   ",
                "   escala de frecuencia para evaluar candidatos   ",
                items
        );

        when(opcionRespuestaDAO.existsByNombre("FRECUENCIA LABORAL")).thenReturn(false);
        when(opcionRespuestaDAO.save(any(OpcionRespuesta.class))).thenAnswer(invocation -> {
            OpcionRespuesta op = invocation.getArgument(0);
            op.setId(15L);
            return op;
        });

        OpcionRespuestaDTO resultado = gestorOpcionRespuesta.crearOpcionRespuesta(entrada);

        ArgumentCaptor<OpcionRespuesta> captor = ArgumentCaptor.forClass(OpcionRespuesta.class);
        verify(opcionRespuestaDAO).existsByNombre("FRECUENCIA LABORAL");
        verify(opcionRespuestaDAO).save(captor.capture());

        OpcionRespuesta guardada = captor.getValue();
        assertThat(guardada.getNombre()).isEqualTo("FRECUENCIA LABORAL");
        assertThat(guardada.getDescripcion()).isEqualTo("ESCALA DE FRECUENCIA PARA EVALUAR CANDIDATOS");
        assertThat(guardada.getItems()).hasSize(2);
        assertThat(guardada.getItems().get(0).getDescripcion()).isEqualTo("NUNCA");
        assertThat(guardada.getItems().get(1).getDescripcion()).isEqualTo("SIEMPRE");

        assertThat(resultado.getNombre()).isEqualTo("FRECUENCIA LABORAL");
        assertThat(resultado.getDescripcion()).isEqualTo("ESCALA DE FRECUENCIA PARA EVALUAR CANDIDATOS");
        assertThat(resultado.getItems().get(0).getDescripcion()).isEqualTo("NUNCA");
        assertThat(resultado.getItems().get(1).getDescripcion()).isEqualTo("SIEMPRE");
    }

    @Test
    @DisplayName("Flujo 2.A: lanza excepción cuando faltan datos obligatorios (nombre, descripción o campos del ítem)")
    void crearOpcionRespuesta_flujo2A_datosIncompletos() {
        List<ItemOpcionRespuestaDTO> itemsValidos = List.of(
                new ItemOpcionRespuestaDTO(1, "OPCIÓN 1"),
                new ItemOpcionRespuestaDTO(2, "OPCIÓN 2")
        );

        OpcionRespuestaDTO sinNombre = new OpcionRespuestaDTO(null, "   ", "DESCRIPCIÓN", itemsValidos);
        assertThatThrownBy(() -> gestorOpcionRespuesta.crearOpcionRespuesta(sinNombre))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("nombre");

        OpcionRespuestaDTO sinDescripcion = new OpcionRespuestaDTO(null, "NOMBRE", "   ", itemsValidos);
        assertThatThrownBy(() -> gestorOpcionRespuesta.crearOpcionRespuesta(sinDescripcion))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("descripción");

        OpcionRespuestaDTO itemSinOrden = new OpcionRespuestaDTO(
                null,
                "NOMBRE",
                "DESCRIPCIÓN",
                List.of(new ItemOpcionRespuestaDTO(null, "OPCIÓN 1"), new ItemOpcionRespuestaDTO(2, "OPCIÓN 2"))
        );
        assertThatThrownBy(() -> gestorOpcionRespuesta.crearOpcionRespuesta(itemSinOrden))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("orden");

        OpcionRespuestaDTO itemSinDescripcion = new OpcionRespuestaDTO(
                null,
                "NOMBRE",
                "DESCRIPCIÓN",
                List.of(new ItemOpcionRespuestaDTO(1, "   "), new ItemOpcionRespuestaDTO(2, "OPCIÓN 2"))
        );
        assertThatThrownBy(() -> gestorOpcionRespuesta.crearOpcionRespuesta(itemSinDescripcion))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("descripción");

        verify(opcionRespuestaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Flujo 2.A / Mínimo de 2 ítems: lanza ReglaNegocioException cuando hay menos de 2 ítems")
    void crearOpcionRespuesta_flujo2A_minimoDosItems() {
        OpcionRespuestaDTO unSoloItem = new OpcionRespuestaDTO(
                null,
                "ESCALA SIMPLE",
                "DESCRIPCIÓN",
                List.of(new ItemOpcionRespuestaDTO(1, "ÚNICA OPCIÓN"))
        );

        assertThatThrownBy(() -> gestorOpcionRespuesta.crearOpcionRespuesta(unSoloItem))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("al menos 2 ítems");

        verify(opcionRespuestaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Flujo 2.B: lanza RecursoDuplicadoException (409) cuando el nombre normalizado ya existe")
    void crearOpcionRespuesta_flujo2B_nombreExistente() {
        OpcionRespuestaDTO entrada = new OpcionRespuestaDTO(
                null,
                "  frecuencia  ",
                "Escala de frecuencia",
                List.of(
                        new ItemOpcionRespuestaDTO(1, "Nunca"),
                        new ItemOpcionRespuestaDTO(2, "Siempre")
                )
        );

        when(opcionRespuestaDAO.existsByNombre("FRECUENCIA")).thenReturn(true);

        assertThatThrownBy(() -> gestorOpcionRespuesta.crearOpcionRespuesta(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("FRECUENCIA");

        verify(opcionRespuestaDAO).existsByNombre("FRECUENCIA");
        verify(opcionRespuestaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Flujo 2.C (con hueco): lanza ReglaNegocioException (422) cuando los órdenes tienen huecos (ej. 1, 3)")
    void crearOpcionRespuesta_flujo2C_ordenConHueco() {
        OpcionRespuestaDTO entrada = new OpcionRespuestaDTO(
                null,
                "ESCALA CON HUECO",
                "DESCRIPCIÓN",
                List.of(
                        new ItemOpcionRespuestaDTO(1, "BAJO"),
                        new ItemOpcionRespuestaDTO(3, "ALTO")
                )
        );

        when(opcionRespuestaDAO.existsByNombre("ESCALA CON HUECO")).thenReturn(false);

        assertThatThrownBy(() -> gestorOpcionRespuesta.crearOpcionRespuesta(entrada))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("consecutivo")
                .hasMessageContaining("2");

        verify(opcionRespuestaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Flujo 2.C (con repetido): lanza ReglaNegocioException (422) cuando hay órdenes repetidos (ej. 1, 1)")
    void crearOpcionRespuesta_flujo2C_ordenRepetido() {
        OpcionRespuestaDTO entrada = new OpcionRespuestaDTO(
                null,
                "ESCALA REPETIDA",
                "DESCRIPCIÓN",
                List.of(
                        new ItemOpcionRespuestaDTO(1, "BAJO"),
                        new ItemOpcionRespuestaDTO(1, "ALTO")
                )
        );

        when(opcionRespuestaDAO.existsByNombre("ESCALA REPETIDA")).thenReturn(false);

        assertThatThrownBy(() -> gestorOpcionRespuesta.crearOpcionRespuesta(entrada))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("consecutivo")
                .hasMessageContaining("1");

        verify(opcionRespuestaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Flujo 2.C (orden desordenado pero válido): acepta órdenes 3, 1, 2 sin huecos ni repetidos y los guarda ordenados")
    void crearOpcionRespuesta_flujo2C_ordenDesordenadoPeroValido() {
        OpcionRespuestaDTO entrada = new OpcionRespuestaDTO(
                null,
                "NIVEL DE INGLÉS",
                "ESCALA DE DOMINIO DEL IDIOMA",
                List.of(
                        new ItemOpcionRespuestaDTO(3, "AVANZADO"),
                        new ItemOpcionRespuestaDTO(1, "BÁSICO"),
                        new ItemOpcionRespuestaDTO(2, "INTERMEDIO")
                )
        );

        when(opcionRespuestaDAO.existsByNombre("NIVEL DE INGLÉS")).thenReturn(false);
        when(opcionRespuestaDAO.save(any(OpcionRespuesta.class))).thenAnswer(invocation -> {
            OpcionRespuesta op = invocation.getArgument(0);
            op.setId(25L);
            return op;
        });

        OpcionRespuestaDTO resultado = gestorOpcionRespuesta.crearOpcionRespuesta(entrada);

        assertThat(resultado.getId()).isEqualTo(25L);
        assertThat(resultado.getItems()).hasSize(3);
        assertThat(resultado.getItems().get(0).getOrden()).isEqualTo(1);
        assertThat(resultado.getItems().get(0).getDescripcion()).isEqualTo("BÁSICO");
        assertThat(resultado.getItems().get(1).getOrden()).isEqualTo(2);
        assertThat(resultado.getItems().get(1).getDescripcion()).isEqualTo("INTERMEDIO");
        assertThat(resultado.getItems().get(2).getOrden()).isEqualTo(3);
        assertThat(resultado.getItems().get(2).getDescripcion()).isEqualTo("AVANZADO");

        verify(opcionRespuestaDAO).save(any(OpcionRespuesta.class));
    }

    @Test
    @DisplayName("Flujo 2.D: lanza ReglaNegocioException (422) indicando la descripción repetida cuando se duplica tras normalizar")
    void crearOpcionRespuesta_flujo2D_descripcionItemDuplicada() {
        OpcionRespuestaDTO entrada = new OpcionRespuestaDTO(
                null,
                "ESCALA DUPLICADA",
                "DESCRIPCIÓN DE PRUEBA",
                List.of(
                        new ItemOpcionRespuestaDTO(1, "  siempre  "),
                        new ItemOpcionRespuestaDTO(2, "A VECES"),
                        new ItemOpcionRespuestaDTO(3, "SIEMPRE")
                )
        );

        when(opcionRespuestaDAO.existsByNombre("ESCALA DUPLICADA")).thenReturn(false);

        assertThatThrownBy(() -> gestorOpcionRespuesta.crearOpcionRespuesta(entrada))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("SIEMPRE")
                .hasMessageContaining("duplicada");

        verify(opcionRespuestaDAO, never()).save(any());
    }
}

