package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Gestor de lógica de negocio para la gestión de Preguntas (CU 020).
 */
@Service
public class GestorPregunta {

    private static final Logger log = LoggerFactory.getLogger(GestorPregunta.class);

    private final PreguntaDAO preguntaDAO;
    private final FactorDAO factorDAO;
    private final ContextoSeguridad contextoSeguridad;

    public GestorPregunta(PreguntaDAO preguntaDAO, FactorDAO factorDAO, ContextoSeguridad contextoSeguridad) {
        this.preguntaDAO = preguntaDAO;
        this.factorDAO = factorDAO;
        this.contextoSeguridad = contextoSeguridad;
    }

    /**
     * Busca preguntas según los criterios ingresados con coincidencia parcial ("contiene").
     *
     * @param filtro criterios de búsqueda
     * @return lista de preguntas encontradas
     * @throws ResponseStatusException HTTP 400 si no se ingresó ningún criterio
     */
    @Transactional(readOnly = true)
    public List<PreguntaDTO> buscarPreguntas(PreguntaFiltroDTO filtro) {
        if (filtro == null || !filtro.tieneCriterio()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe ingresar al menos un criterio de búsqueda");
        }

        String comp = (filtro.getCompetencia() != null && !filtro.getCompetencia().isBlank())
                ? filtro.getCompetencia().trim()
                : null;
        String fac = (filtro.getFactor() != null && !filtro.getFactor().isBlank())
                ? filtro.getFactor().trim()
                : null;
        String nom = (filtro.getNombre() != null && !filtro.getNombre().isBlank())
                ? filtro.getNombre().trim()
                : null;

        log.info("Buscando preguntas con filtros - Competencia: '{}', Factor: '{}', Nombre: '{}'", comp, fac, nom);

        List<Pregunta> preguntas = preguntaDAO.buscarConFiltros(comp, fac, nom);
        return preguntas.stream().map(this::aDTO).toList();
    }

    /**
     * Obtiene una pregunta activa por ID.
     *
     * @param id identificador de la pregunta
     * @return DTO de la pregunta
     */
    @Transactional(readOnly = true)
    public PreguntaDTO obtenerPorId(Long id) {
        Pregunta pregunta = preguntaDAO.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pregunta", id));
        return aDTO(pregunta);
    }

    /**
     * Da de alta una nueva pregunta con sus opciones de respuesta (CU 021 - Flujo 3.A).
     *
     * @param dto datos de la pregunta y sus opciones
     * @return DTO de la pregunta creada con IDs asignados
     */
    @Transactional
    public PreguntaDTO crearPregunta(PreguntaDTO dto) {
        Long usuarioActualId = contextoSeguridad != null ? contextoSeguridad.getUsuarioActualId() : null;
        log.info("Iniciando alta de pregunta por usuario ID {}", usuarioActualId);

        // 1. Obtener y validar el Factor
        Factor factor = resolverFactor(dto);

        // 2. Normalización de campos
        String nombre = dto.getNombre() != null ? dto.getNombre().trim() : "";
        String enunciado = dto.getEnunciado() != null ? dto.getEnunciado().trim() : "";
        String descripcion = dto.getDescripcion() != null ? dto.getDescripcion().trim() : null;

        if (nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la pregunta es obligatorio");
        }
        if (enunciado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enunciado de la pregunta es obligatorio");
        }
        if (dto.getOpciones() == null || dto.getOpciones().size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pregunta debe tener al menos 2 opciones de respuesta");
        }

        // 3. Validar unicidad de nombre y enunciado dentro del factor
        if (preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(factor.getId(), nombre)) {
            throw new RecursoDuplicadoException("Ya existe una pregunta con el nombre '" + nombre + "' para el factor seleccionado");
        }
        if (preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndEliminadoFalse(factor.getId(), enunciado)) {
            throw new RecursoDuplicadoException("Ya existe una pregunta con el enunciado '" + enunciado + "' para el factor seleccionado");
        }

        // 4. Construcción de entidad Pregunta y Opciones
        Pregunta pregunta = new Pregunta();
        pregunta.setNombre(nombre);
        pregunta.setEnunciado(enunciado);
        pregunta.setDescripcion(descripcion);
        pregunta.setFactor(factor);
        pregunta.setEliminado(false);

        int orden = 1;
        for (OpcionRespuestaDTO opcionDTO : dto.getOpciones()) {
            if (opcionDTO.getNombre() == null || opcionDTO.getNombre().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de cada opción de respuesta es obligatorio");
            }
            if (opcionDTO.getPonderacion() == null || opcionDTO.getPonderacion() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ponderación de cada opción debe ser mayor o igual a 0");
            }

            Integer nroOrden = opcionDTO.getNroOrden() != null ? opcionDTO.getNroOrden() : orden;
            OpcionRespuesta opcion = new OpcionRespuesta(
                    null,
                    pregunta,
                    opcionDTO.getNombre().trim(),
                    opcionDTO.getDescripcion() != null ? opcionDTO.getDescripcion().trim() : null,
                    nroOrden,
                    opcionDTO.getPonderacion()
            );
            pregunta.addOpcion(opcion);
            orden++;
        }

        Pregunta guardada = preguntaDAO.save(pregunta);
        log.info("Pregunta creada exitosamente con ID {} y nombre '{}'", guardada.getId(), guardada.getNombre());

        return aDTO(guardada);
    }

    /**
     * Modifica los datos de una pregunta existente (CU 022 - Flujo 5.A).
     *
     * @param id identificador de la pregunta
     * @param dto datos a actualizar
     * @return DTO de la pregunta actualizada
     */
    @Transactional
    public PreguntaDTO modificarPregunta(Long id, PreguntaDTO dto) {
        log.info("Iniciando modificación de pregunta con ID {}", id);

        Pregunta existente = preguntaDAO.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pregunta", id));

        String nombre = dto.getNombre() != null ? dto.getNombre().trim() : "";
        String enunciado = dto.getEnunciado() != null ? dto.getEnunciado().trim() : "";
        String descripcion = dto.getDescripcion() != null ? dto.getDescripcion().trim() : null;

        if (nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la pregunta es obligatorio");
        }
        if (enunciado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enunciado de la pregunta es obligatorio");
        }
        if (dto.getOpciones() == null || dto.getOpciones().size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pregunta debe tener al menos 2 opciones de respuesta");
        }

        Long factorId = existente.getFactor().getId();

        // Validar unicidad respecto a otras preguntas en el mismo factor
        if (preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndIdNotAndEliminadoFalse(factorId, nombre, id)) {
            throw new RecursoDuplicadoException("Ya existe otra pregunta con el nombre '" + nombre + "' para el factor seleccionado");
        }
        if (preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndIdNotAndEliminadoFalse(factorId, enunciado, id)) {
            throw new RecursoDuplicadoException("Ya existe otra pregunta con el enunciado '" + enunciado + "' para el factor seleccionado");
        }

        existente.setNombre(nombre);
        existente.setEnunciado(enunciado);
        existente.setDescripcion(descripcion);

        // Actualizar opciones de respuesta
        existente.clearOpciones();
        int orden = 1;
        for (OpcionRespuestaDTO opcionDTO : dto.getOpciones()) {
            if (opcionDTO.getNombre() == null || opcionDTO.getNombre().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de cada opción de respuesta es obligatorio");
            }
            if (opcionDTO.getPonderacion() == null || opcionDTO.getPonderacion() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ponderación de cada opción debe ser mayor o igual a 0");
            }

            Integer nroOrden = opcionDTO.getNroOrden() != null ? opcionDTO.getNroOrden() : orden;
            OpcionRespuesta opcion = new OpcionRespuesta(
                    null,
                    existente,
                    opcionDTO.getNombre().trim(),
                    opcionDTO.getDescripcion() != null ? opcionDTO.getDescripcion().trim() : null,
                    nroOrden,
                    opcionDTO.getPonderacion()
            );
            existente.addOpcion(opcion);
            orden++;
        }

        Pregunta actualizada = preguntaDAO.save(existente);
        log.info("Pregunta ID {} modificada exitosamente", actualizada.getId());

        return aDTO(actualizada);
    }

    /**
     * Da de baja lógica una pregunta registrando auditoría (CU 023 - Flujo 5.B).
     *
     * @param id identificador de la pregunta
     */
    @Transactional
    public void eliminarPregunta(Long id) {
        log.info("Iniciando baja lógica de pregunta con ID {}", id);

        Pregunta existente = preguntaDAO.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pregunta", id));

        existente.setEliminado(true);
        existente.setFechaEliminacion(LocalDateTime.now());
        if (contextoSeguridad != null) {
            existente.setUsuarioEliminacion(contextoSeguridad.getUsuarioActualId());
        }

        preguntaDAO.save(existente);
        log.info("Pregunta ID {} dada de baja exitosamente", id);
    }

    private Factor resolverFactor(PreguntaDTO dto) {
        if (dto.getFactorCodigo() != null && !dto.getFactorCodigo().isBlank()) {
            String fCod = dto.getFactorCodigo().trim();
            return factorDAO.findByCodigo(fCod.toUpperCase(Locale.ROOT))
                    .or(() -> factorDAO.findByCodigo(fCod))
                    .orElseThrow(() -> new RecursoNoEncontradoException("Factor con código '" + fCod + "' no existe"));
        }
        if (dto.getFactorId() != null) {
            return factorDAO.findById(dto.getFactorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Factor", dto.getFactorId()));
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código del factor no puede estar vacío");
    }

    public PreguntaDTO aDTO(Pregunta p) {
        String factorNombre = null;
        String factorCodigo = null;
        Long factorId = null;
        String competenciaNombre = null;

        if (p.getFactor() != null) {
            factorNombre = p.getFactor().getNombre();
            factorCodigo = p.getFactor().getCodigo();
            factorId = p.getFactor().getId();
            if (p.getFactor().getCompetencia() != null) {
                competenciaNombre = p.getFactor().getCompetencia().getNombre();
            }
        }

        List<OpcionRespuestaDTO> opcionesDTO = p.getOpciones() != null
                ? p.getOpciones().stream()
                .map(o -> new OpcionRespuestaDTO(o.getId(), o.getNombre(), o.getDescripcion(), o.getNroOrden(), o.getPonderacion()))
                .toList()
                : List.of();

        PreguntaDTO dto = new PreguntaDTO(
                p.getId(),
                competenciaNombre,
                factorNombre,
                factorCodigo,
                p.getNombre(),
                p.getEnunciado(),
                p.getDescripcion(),
                opcionesDTO
        );
        dto.setFactorId(factorId);
        return dto;
    }
}

