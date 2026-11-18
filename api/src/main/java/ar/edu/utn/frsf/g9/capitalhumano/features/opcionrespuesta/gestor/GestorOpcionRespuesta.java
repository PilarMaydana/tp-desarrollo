package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dao.OpcionRespuestaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.ItemOpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.model.ItemOpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Gestor de lógica de negocio para el alta de Opciones de Respuesta (CU 029).
 */
@Service
public class GestorOpcionRespuesta {

    private static final Logger log = LoggerFactory.getLogger(GestorOpcionRespuesta.class);

    private final OpcionRespuestaDAO opcionRespuestaDAO;

    public GestorOpcionRespuesta(OpcionRespuestaDAO opcionRespuestaDAO) {
        this.opcionRespuestaDAO = opcionRespuestaDAO;
    }

    /**
     * Da de alta una nueva opción de respuesta con sus ítems en el sistema (CU 029).
     *
     * <p>Orden de validación:</p>
     * <ol>
     *     <li>Normalización de literales (trim + mayúsculas) y validación de datos obligatorios (Flujo 2.A).</li>
     *     <li>Unicidad del nombre normalizado (Flujo 2.B -> 409).</li>
     *     <li>Orden de visualización consecutivo 1..n sin huecos ni repetidos (Flujo 2.C -> 422).</li>
     *     <li>Ausencia de descripciones de ítems duplicadas dentro de la misma opción (Flujo 2.D -> 422).</li>
     * </ol>
     *
     * @param dto datos de la opción de respuesta a crear
     * @return DTO de la opción de respuesta creada con su ID asignado
     * @throws RecursoDuplicadoException si ya existe una opción de respuesta con el mismo nombre (409)
     * @throws ReglaNegocioException     si los datos están incompletos, el orden no es consecutivo 1..n o hay descripciones de ítem duplicadas (422)
     */
    @Transactional
    public OpcionRespuestaDTO crearOpcionRespuesta(OpcionRespuestaDTO dto) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de la opción de respuesta son obligatorios");
        }

        // 1. Normalización de campos literales (trim + toUpperCase) y validación defensiva (Flujo 2.A)
        String nombre = dto.getNombre() != null ? dto.getNombre().trim().toUpperCase(Locale.ROOT) : "";
        String descripcion = dto.getDescripcion() != null ? dto.getDescripcion().trim().toUpperCase(Locale.ROOT) : "";

        if (nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }
        if (descripcion.isBlank()) {
            throw new ReglaNegocioException("La descripción es obligatoria");
        }
        if (dto.getItems() == null || dto.getItems().size() < 2) {
            throw new ReglaNegocioException("La opción de respuesta debe tener al menos 2 ítems");
        }

        List<ItemOpcionRespuestaDTO> itemsNormalizados = new ArrayList<>(dto.getItems().size());
        for (ItemOpcionRespuestaDTO itemDto : dto.getItems()) {
            if (itemDto == null || itemDto.getOrden() == null) {
                throw new ReglaNegocioException("El orden de visualización es obligatorio en todos los ítems");
            }
            String descItem = itemDto.getDescripcion() != null
                    ? itemDto.getDescripcion().trim().toUpperCase(Locale.ROOT)
                    : "";
            if (descItem.isBlank()) {
                throw new ReglaNegocioException("La descripción es obligatoria en todos los ítems");
            }
            itemsNormalizados.add(new ItemOpcionRespuestaDTO(itemDto.getOrden(), descItem));
        }

        // 2. Flujo 2.B: Nombre existente (comparado ya normalizado) -> 409
        if (opcionRespuestaDAO.existsByNombre(nombre)) {
            throw new RecursoDuplicadoException("Ya existe una opción de respuesta con el nombre '" + nombre + "'");
        }

        // 3. Flujo 2.C: Orden de visualización consecutivo 1..n sin huecos ni repetidos -> 422
        int n = itemsNormalizados.size();
        Set<Integer> ordenesVistos = new HashSet<>();
        for (ItemOpcionRespuestaDTO item : itemsNormalizados) {
            Integer orden = item.getOrden();
            if (!ordenesVistos.add(orden)) {
                throw new ReglaNegocioException(
                        "El orden de visualización de los ítems no es consecutivo: el orden " + orden + " está repetido");
            }
        }
        for (int esperado = 1; esperado <= n; esperado++) {
            if (!ordenesVistos.contains(esperado)) {
                throw new ReglaNegocioException(
                        "El orden de visualización de los ítems debe ser consecutivo desde 1 hasta " + n + " sin huecos (falta el orden " + esperado + ")");
            }
        }

        // 4. Flujo 2.D: Descripción de ítem duplicada dentro de la misma opción (comparada ya normalizada) -> 422
        Set<String> descripcionesVistas = new HashSet<>();
        for (ItemOpcionRespuestaDTO item : itemsNormalizados) {
            String descItem = item.getDescripcion();
            if (!descripcionesVistas.add(descItem)) {
                throw new ReglaNegocioException(
                        "La descripción del ítem '" + descItem + "' está duplicada dentro de la misma opción de respuesta");
            }
        }

        // 5. Construcción de la entidad y sus ítems ordenados por orden ascendente
        itemsNormalizados.sort(Comparator.comparingInt(ItemOpcionRespuestaDTO::getOrden));

        OpcionRespuesta entidad = new OpcionRespuesta();
        entidad.setNombre(nombre);
        entidad.setDescripcion(descripcion);

        for (ItemOpcionRespuestaDTO item : itemsNormalizados) {
            entidad.addItem(new ItemOpcionRespuesta(item.getOrden(), item.getDescripcion()));
        }

        OpcionRespuesta guardada = opcionRespuestaDAO.save(entidad);
        log.info("Opción de respuesta creada exitosamente con ID {} y nombre '{}'", guardada.getId(), guardada.getNombre());

        return aDTO(guardada);
    }

    private OpcionRespuestaDTO aDTO(OpcionRespuesta entidad) {
        List<ItemOpcionRespuestaDTO> itemsDto = entidad.getItems().stream()
                .sorted(Comparator.comparingInt(ItemOpcionRespuesta::getOrden))
                .map(item -> new ItemOpcionRespuestaDTO(item.getOrden(), item.getDescripcion()))
                .toList();

        return new OpcionRespuestaDTO(
                entidad.getId(),
                entidad.getNombre(),
                entidad.getDescripcion(),
                itemsDto
        );
    }
}

