package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dao.CompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dto.FactorDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dao.PreguntaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaFiltroDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaModificacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.ContextoSeguridad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Gestor de lógica de negocio para la gestión de Preguntas (CU 020 / CU 021 / CU 022).
 */
@Service
public class GestorPregunta {

    private static final Logger log = LoggerFactory.getLogger(GestorPregunta.class);
    private static final BigDecimal SUMA_PONDERACIONES_REQUERIDA = BigDecimal.TEN;

    private final PreguntaDAO preguntaDAO;
    private final FactorDAO factorDAO;
    private final CompetenciaDAO competenciaDAO;
    private final ContextoSeguridad contextoSeguridad;

    @Autowired
    public GestorPregunta(PreguntaDAO preguntaDAO,
                          FactorDAO factorDAO,
                          CompetenciaDAO competenciaDAO,
                          ContextoSeguridad contextoSeguridad) {
        this.preguntaDAO = preguntaDAO;
        this.factorDAO = factorDAO;
        this.competenciaDAO = competenciaDAO;
        this.contextoSeguridad = contextoSeguridad;
    }

    public GestorPregunta(PreguntaDAO preguntaDAO, FactorDAO factorDAO, ContextoSeguridad contextoSeguridad) {
        this(preguntaDAO, factorDAO, null, contextoSeguridad);
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
        String descripcion = (dto.getDescripcion() != null && !dto.getDescripcion().isBlank())
                ? dto.getDescripcion().trim()
                : null;

        if (nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }
        if (enunciado.isBlank()) {
            throw new ReglaNegocioException("El enunciado es obligatorio");
        }
        if (dto.getOpciones() == null || dto.getOpciones().size() < 2) {
            throw new ReglaNegocioException("Debe ingresar al menos 2 opciones de respuesta");
        }

        // 3. Validar unicidad de nombre y enunciado dentro del factor
        if (preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(factor.getId(), nombre)) {
            throw new RecursoDuplicadoException("Ya existe una pregunta con el nombre '" + nombre + "' para el factor seleccionado");
        }
        if (preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndEliminadoFalse(factor.getId(), enunciado)) {
            throw new RecursoDuplicadoException("Ya existe una pregunta con el enunciado '" + enunciado + "' para el factor seleccionado");
        }

        // 4. Validar ponderaciones (deben ser >= 0 y sumar exactamente 10.0)
        double sumaPonderaciones = 0.0;
        for (OpcionRespuestaDTO opcionDTO : dto.getOpciones()) {
            if (opcionDTO.getPonderacion() == null) {
                throw new ReglaNegocioException("La ponderación es obligatoria para todas las opciones");
            }
            if (opcionDTO.getPonderacion() < 0.0) {
                throw new ReglaNegocioException("La ponderación debe ser mayor o igual a cero");
            }
            sumaPonderaciones += opcionDTO.getPonderacion();
        }

        if (Math.abs(sumaPonderaciones - 10.0) > 0.001) {
            throw new ReglaNegocioException("La suma de las ponderaciones de las opciones de respuesta debe ser exactamente 10 puntos (suma actual: " + sumaPonderaciones + ")");
        }

        // 5. Construcción de entidad Pregunta y Opciones
        Pregunta pregunta = new Pregunta();
        pregunta.setNombre(nombre);
        pregunta.setEnunciado(enunciado);
        pregunta.setDescripcion(descripcion);
        pregunta.setFactor(factor);
        pregunta.setEliminado(false);

        int orden = 1;
        for (OpcionRespuestaDTO opcionDTO : dto.getOpciones()) {
            String optNombre = opcionDTO.getNombre() != null ? opcionDTO.getNombre().trim() : "";
            String optDescripcion = (opcionDTO.getDescripcion() != null && !opcionDTO.getDescripcion().isBlank())
                    ? opcionDTO.getDescripcion().trim()
                    : null;

            if (optNombre.isBlank()) {
                throw new ReglaNegocioException("El nombre de la opción es obligatorio");
            }

            Integer nroOrden = opcionDTO.getNroOrden() != null ? opcionDTO.getNroOrden() : orden;
            OpcionRespuesta opcion = new OpcionRespuesta(
                    null,
                    pregunta,
                    optNombre,
                    optDescripcion,
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
     * Modifica los datos y opciones de respuesta de una pregunta existente (CU 022).
     *
     * <p>Competencia y Factor son de solo lectura y no se modifican. Los datos literales se
     * normalizan con {@code trim()} y {@code toUpperCase(Locale.ROOT)} antes de validar
     * duplicados y persistir. Si ocurre cualquier error, la transacción hace rollback y la
     * pregunta permanece sin cambios.</p>
     *
     * @param id  identificador de la pregunta a modificar
     * @param dto nuevos datos de la pregunta y sus opciones ponderadas
     * @return DTO actualizado de la pregunta
     * @throws RecursoNoEncontradoException si no existe una pregunta activa con ese ID (404)
     * @throws RecursoDuplicadoException    si otra pregunta del mismo factor tiene el mismo nombre y/o enunciado (409)
     * @throws ReglaNegocioException        si las ponderaciones no suman exactamente 10 o hay datos inválidos (422)
     */
    @Transactional
    public PreguntaDTO modificarPregunta(Long id, PreguntaModificacionDTO dto) {
        log.info("Iniciando modificación de pregunta con ID {}", id);

        Pregunta existente = preguntaDAO.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pregunta", id));

        if (dto == null) {
            throw new ReglaNegocioException("Los datos de la pregunta son obligatorios");
        }

        // 1. Normalización de campos literales (trim + mayúsculas)
        String nombre = dto.getNombre() != null ? dto.getNombre().trim().toUpperCase(Locale.ROOT) : "";
        String enunciado = dto.getEnunciado() != null ? dto.getEnunciado().trim().toUpperCase(Locale.ROOT) : "";
        String descripcion = (dto.getDescripcion() != null && !dto.getDescripcion().isBlank())
                ? dto.getDescripcion().trim().toUpperCase(Locale.ROOT)
                : null;

        if (nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre de la pregunta es obligatorio");
        }
        if (enunciado.isBlank()) {
            throw new ReglaNegocioException("El enunciado de la pregunta es obligatorio");
        }
        if (dto.getOpciones() == null || dto.getOpciones().size() < 2) {
            throw new ReglaNegocioException("Debe ingresar al menos 2 opciones de respuesta");
        }

        // 2. Validar opciones individuales y acumular ponderaciones con BigDecimal
        BigDecimal sumaPonderaciones = BigDecimal.ZERO;
        for (OpcionRespuestaDTO opcionDTO : dto.getOpciones()) {
            if (opcionDTO == null) {
                throw new ReglaNegocioException("Cada opción de respuesta es obligatoria");
            }
            String optNombre = opcionDTO.getNombre() != null ? opcionDTO.getNombre().trim() : "";
            if (optNombre.isBlank()) {
                throw new ReglaNegocioException("El nombre de la opción es obligatorio");
            }
            Double valorPonderacion = opcionDTO.getPonderacion();
            if (valorPonderacion == null) {
                throw new ReglaNegocioException("La ponderación es obligatoria para todas las opciones");
            }
            if (Double.isNaN(valorPonderacion) || Double.isInfinite(valorPonderacion)) {
                throw new ReglaNegocioException("La ponderación debe ser un número válido");
            }
            BigDecimal ponderacion = BigDecimal.valueOf(valorPonderacion);
            if (ponderacion.compareTo(BigDecimal.ZERO) < 0) {
                throw new ReglaNegocioException("La ponderación debe ser mayor o igual a cero");
            }
            sumaPonderaciones = sumaPonderaciones.add(ponderacion);
        }

        // 3. Validar unicidad de nombre y enunciado en el mismo factor (excluyendo la propia pregunta)
        Long factorId = existente.getFactor().getId();
        boolean nombreDuplicado = preguntaDAO.existsByFactorIdAndNombreIgnoreCaseAndIdNotAndEliminadoFalse(
                factorId, nombre, id);
        boolean enunciadoDuplicado = preguntaDAO.existsByFactorIdAndEnunciadoIgnoreCaseAndIdNotAndEliminadoFalse(
                factorId, enunciado, id);

        if (nombreDuplicado && enunciadoDuplicado) {
            throw new RecursoDuplicadoException(
                    "Ya existe otra pregunta con el mismo nombre ('" + nombre
                            + "') y el mismo enunciado ('" + enunciado + "') para el factor seleccionado");
        }
        if (nombreDuplicado) {
            throw new RecursoDuplicadoException(
                    "Ya existe otra pregunta con el nombre '" + nombre + "' para el factor seleccionado");
        }
        if (enunciadoDuplicado) {
            throw new RecursoDuplicadoException(
                    "Ya existe otra pregunta con el enunciado '" + enunciado + "' para el factor seleccionado");
        }

        // 4. Validar suma exacta de ponderaciones == 10 (BigDecimal)
        if (sumaPonderaciones.compareTo(SUMA_PONDERACIONES_REQUERIDA) != 0) {
            throw new ReglaNegocioException(
                    "La suma de las ponderaciones de las opciones de respuesta debe ser exactamente 10 puntos (suma actual: "
                            + sumaPonderaciones.stripTrailingZeros().toPlainString() + ")");
        }

        // 5. Aplicar cambios en la entidad (Competencia y Factor permanecen intactos)
        existente.setNombre(nombre);
        existente.setEnunciado(enunciado);
        existente.setDescripcion(descripcion);

        // Reemplazo completo de opciones con flush intermedio para evitar conflictos UNIQUE (pregunta, nro_orden)
        existente.clearOpciones();
        preguntaDAO.flush();

        int orden = 1;
        for (OpcionRespuestaDTO opcionDTO : dto.getOpciones()) {
            String optNombre = opcionDTO.getNombre().trim().toUpperCase(Locale.ROOT);
            String optDescripcion = (opcionDTO.getDescripcion() != null && !opcionDTO.getDescripcion().isBlank())
                    ? opcionDTO.getDescripcion().trim().toUpperCase(Locale.ROOT)
                    : null;
            Integer nroOrden = opcionDTO.getNroOrden() != null ? opcionDTO.getNroOrden() : orden;

            OpcionRespuesta opcion = new OpcionRespuesta(
                    null,
                    existente,
                    optNombre,
                    optDescripcion,
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

    /**
     * Carga dinámicamente los factores correspondientes a una competencia seleccionada (CU 021).
     *
     * @param codigoCompetencia código de la competencia
     * @return lista de factores activos ordenados por número de orden ascendente
     * @throws RecursoNoEncontradoException si la competencia indicada no existe
     */
    @Transactional(readOnly = true)
    public List<FactorDTO> listarFactoresPorCompetencia(String codigoCompetencia) {
        String codigo = codigoCompetencia != null ? codigoCompetencia.trim().toUpperCase(Locale.ROOT) : "";
        if (competenciaDAO != null && !competenciaDAO.existsByCodigo(codigo)) {
            throw new RecursoNoEncontradoException("Competencia con código '" + codigo + "' no encontrada");
        }

        return factorDAO.findByCompetenciaCodigoOrderByNroOrdenAsc(codigo).stream()
                .filter(f -> !Boolean.TRUE.equals(f.getEliminado()))
                .map(this::aFactorDTO)
                .toList();
    }

    private Factor resolverFactor(PreguntaDTO dto) {
        if (dto.getFactorCodigo() != null && !dto.getFactorCodigo().isBlank()) {
            String fCod = dto.getFactorCodigo().trim();
            return factorDAO.findByCodigo(fCod.toUpperCase(Locale.ROOT))
                    .or(() -> factorDAO.findByCodigo(fCod))
                    .orElseThrow(() -> new RecursoNoEncontradoException("Factor con código '" + fCod + "' no encontrado"));
        }
        if (dto.getFactorId() != null) {
            return factorDAO.findById(dto.getFactorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Factor", dto.getFactorId()));
        }
        throw new ReglaNegocioException("El código de factor es obligatorio");
    }

    public PreguntaDTO aDTO(Pregunta p) {
        String factorNombre = null;
        String factorCodigo = null;
        Long factorId = null;
        Long competenciaId = null;
        String competenciaNombre = null;
        String competenciaCodigo = null;

        if (p.getFactor() != null) {
            factorNombre = p.getFactor().getNombre();
            factorCodigo = p.getFactor().getCodigo();
            factorId = p.getFactor().getId();
            if (p.getFactor().getCompetencia() != null) {
                competenciaId = p.getFactor().getCompetencia().getId();
                competenciaNombre = p.getFactor().getCompetencia().getNombre();
                competenciaCodigo = p.getFactor().getCompetencia().getCodigo();
            }
        }

        List<OpcionRespuestaDTO> opcionesDTO = p.getOpciones() != null
                ? p.getOpciones().stream()
                .sorted(Comparator.comparing(OpcionRespuesta::getNroOrden, Comparator.nullsLast(Comparator.naturalOrder())))
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
        dto.setCompetenciaId(competenciaId);
        dto.setFactorId(factorId);
        dto.setCodigoCompetencia(competenciaCodigo);
        return dto;
    }

    private FactorDTO aFactorDTO(Factor entidad) {
        return new FactorDTO(
                entidad.getId(),
                entidad.getCodigo(),
                entidad.getNombre(),
                entidad.getDescripcion(),
                entidad.getCompetencia() != null ? entidad.getCompetencia().getId() : null,
                entidad.getCompetencia() != null ? entidad.getCompetencia().getNombre() : null,
                entidad.getCompetencia() != null ? entidad.getCompetencia().getCodigo() : null,
                entidad.getNroOrden()
        );
    }
}
