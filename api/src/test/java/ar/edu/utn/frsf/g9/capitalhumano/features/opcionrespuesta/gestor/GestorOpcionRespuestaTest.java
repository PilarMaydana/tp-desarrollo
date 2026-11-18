package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dao.OpcionRespuestaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.ItemOpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.OpcionRespuestaResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.model.ItemOpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para {@link GestorOpcionRespuesta} (CU 024 / CU 029).
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

    @Test
    @DisplayName("CU 024 - Búsqueda con texto: normaliza a mayúsculas y trim, y devuelve lista de resúmenes ordenada")
    void buscarOpcionesRespuesta_conTexto_normalizaMayusculasYTrim() {
        OpcionRespuesta op1 = new OpcionRespuesta(1L, "FRECUENCIA", "ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO");
        OpcionRespuesta op2 = new OpcionRespuesta(3L, "FRECUENCIA SEMANAL", "ESCALA DE FRECUENCIA DE EJECUCIÓN SEMANAL DE TAREAS");

        when(opcionRespuestaDAO.findByNombreContainingIgnoreCaseOrderByNombreAsc("FRECUENCIA"))
                .thenReturn(List.of(op1, op2));

        List<OpcionRespuestaResumenDTO> resultados = gestorOpcionRespuesta.buscarOpcionesRespuesta("   frecuencia   ");

        assertThat(resultados).hasSize(2);
        assertThat(resultados.get(0).getId()).isEqualTo(1L);
        assertThat(resultados.get(0).getNombre()).isEqualTo("FRECUENCIA");
        assertThat(resultados.get(0).getDescripcion()).isEqualTo("ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO");
        assertThat(resultados.get(1).getId()).isEqualTo(3L);
        assertThat(resultados.get(1).getNombre()).isEqualTo("FRECUENCIA SEMANAL");
        assertThat(resultados.get(1).getDescripcion()).isEqualTo("ESCALA DE FRECUENCIA DE EJECUCIÓN SEMANAL DE TAREAS");

        verify(opcionRespuestaDAO).findByNombreContainingIgnoreCaseOrderByNombreAsc("FRECUENCIA");
        verify(opcionRespuestaDAO, never()).findAllByOrderByNombreAsc();
    }

    @Test
    @DisplayName("CU 024 - Búsqueda sin texto o en blanco: devuelve todas las opciones de respuesta ordenadas por nombre")
    void buscarOpcionesRespuesta_sinTextoOBlanco_devuelveTodas() {
        OpcionRespuesta op1 = new OpcionRespuesta(2L, "ACUERDO", "ESCALA DE GRADO DE ACUERDO CON LA AFIRMACIÓN");
        OpcionRespuesta op2 = new OpcionRespuesta(1L, "FRECUENCIA", "ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO");

        when(opcionRespuestaDAO.findAllByOrderByNombreAsc()).thenReturn(List.of(op1, op2));

        List<OpcionRespuestaResumenDTO> resNull = gestorOpcionRespuesta.buscarOpcionesRespuesta(null);
        List<OpcionRespuestaResumenDTO> resVacio = gestorOpcionRespuesta.buscarOpcionesRespuesta("");
        List<OpcionRespuestaResumenDTO> resBlanco = gestorOpcionRespuesta.buscarOpcionesRespuesta("   ");

        assertThat(resNull).hasSize(2);
        assertThat(resNull.get(0).getNombre()).isEqualTo("ACUERDO");
        assertThat(resNull.get(1).getNombre()).isEqualTo("FRECUENCIA");
        assertThat(resVacio).hasSize(2);
        assertThat(resBlanco).hasSize(2);

        verify(opcionRespuestaDAO, times(3)).findAllByOrderByNombreAsc();
        verify(opcionRespuestaDAO, never()).findByNombreContainingIgnoreCaseOrderByNombreAsc(any());
    }

    @Test
    @DisplayName("CU 024 - Búsqueda sin coincidencias: devuelve lista vacía sin lanzar error")
    void buscarOpcionesRespuesta_sinCoincidencias_devuelveListaVacia() {
        when(opcionRespuestaDAO.findByNombreContainingIgnoreCaseOrderByNombreAsc("INEXISTENTE"))
                .thenReturn(List.of());

        List<OpcionRespuestaResumenDTO> resultados = gestorOpcionRespuesta.buscarOpcionesRespuesta("inexistente");

        assertThat(resultados).isNotNull().isEmpty();
        verify(opcionRespuestaDAO).findByNombreContainingIgnoreCaseOrderByNombreAsc("INEXISTENTE");
    }

    @Test
    @DisplayName("CU 024 - Búsqueda con caracteres % y _: los delega normalizados al método derivado Containing del DAO para que se traten como literales")
    void buscarOpcionesRespuesta_conCaracteresEspecialesLike_delegaLiteralAlDao() {
        when(opcionRespuestaDAO.findByNombreContainingIgnoreCaseOrderByNombreAsc("ESCALA_100%"))
                .thenReturn(List.of());

        List<OpcionRespuestaResumenDTO> resultados = gestorOpcionRespuesta.buscarOpcionesRespuesta("  escala_100%  ");

        assertThat(resultados).isEmpty();
        verify(opcionRespuestaDAO).findByNombreContainingIgnoreCaseOrderByNombreAsc("ESCALA_100%");
    }

    @Test
    @DisplayName("CU 024 - Detalle por ID existente: devuelve DTO con sus ítems ordenados por orden ascendente")
    void obtenerPorId_existente_devuelveDetalleConItemsOrdenados() {
        OpcionRespuesta entidad = new OpcionRespuesta(1L, "FRECUENCIA", "ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO");
        entidad.addItem(new ItemOpcionRespuesta(2, "A VECES"));
        entidad.addItem(new ItemOpcionRespuesta(1, "NUNCA"));
        entidad.addItem(new ItemOpcionRespuesta(3, "SIEMPRE"));

        when(opcionRespuestaDAO.findById(1L)).thenReturn(Optional.of(entidad));

        OpcionRespuestaDTO resultado = gestorOpcionRespuesta.obtenerPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("FRECUENCIA");
        assertThat(resultado.getDescripcion()).isEqualTo("ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO");
        assertThat(resultado.getItems()).hasSize(3);
        assertThat(resultado.getItems().get(0).getOrden()).isEqualTo(1);
        assertThat(resultado.getItems().get(0).getDescripcion()).isEqualTo("NUNCA");
        assertThat(resultado.getItems().get(1).getOrden()).isEqualTo(2);
        assertThat(resultado.getItems().get(1).getDescripcion()).isEqualTo("A VECES");
        assertThat(resultado.getItems().get(2).getOrden()).isEqualTo(3);
        assertThat(resultado.getItems().get(2).getDescripcion()).isEqualTo("SIEMPRE");

        verify(opcionRespuestaDAO).findById(1L);
    }

    @Test
    @DisplayName("CU 024 - Detalle por ID inexistente: lanza RecursoNoEncontradoException (404)")
    void obtenerPorId_inexistente_lanzaRecursoNoEncontradoException() {
        when(opcionRespuestaDAO.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestorOpcionRespuesta.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Opción de respuesta con id 99 no existe");

        verify(opcionRespuestaDAO).findById(99L);
    }
}

