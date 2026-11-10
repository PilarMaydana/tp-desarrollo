package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dao.CompetenciaDAO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Gestor de lógica de negocio para la gestión de Preguntas y sus Factores asociados (CU 021).
 */
@Service
public class GestorPregunta {

    private static final Logger log = LoggerFactory.getLogger(GestorPregunta.class);

    private final PreguntaDAO preguntaDAO;
    private final FactorDAO factorDAO;
    private final CompetenciaDAO competenciaDAO;
    private final ContextoSeguridad contextoSeguridad;

    public GestorPregunta(PreguntaDAO preguntaDAO,
                          FactorDAO factorDAO,
                          CompetenciaDAO competenciaDAO,
                          ContextoSeguridad contextoSeguridad) {
        this.preguntaDAO = preguntaDAO;
        this.factorDAO = factorDAO;
        this.competenciaDAO = competenciaDAO;
        this.contextoSeguridad = contextoSeguridad;
    }

    /**
     * Da de alta una nueva pregunta en el sistema con sus opciones de respuesta correspondientes (CU 021).
     *
     * <p>Normaliza los datos literales a mayúsculas y sin espacios iniciales/finales, verifica la existencia
     * del factor asociado, valida la unicidad de nombre y enunciado para dicho factor, y comprueba que las
     * ponderaciones sumen exactamente 10 puntos y sean no negativas.</p>
     *
     * @param dto datos de la pregunta y sus opciones a registrar
     * @return DTO de la pregunta creada con IDs y relaciones resueltas
     * @throws ReglaNegocioException si faltan datos obligatorios o la suma de ponderaciones no es 10 o son negativas
     * @throws RecursoNoEncontradoException si el factor no existe en el sistema
     * @throws RecursoDuplicadoException si ya existe una pregunta con el mismo nombre o enunciado para el factor
     */
    @Transactional
    public PreguntaDTO crearPregunta(PreguntaDTO dto) {
        Long usuarioActualId = contextoSeguridad != null ? contextoSeguridad.getUsuarioActualId() : null;
        log.info("Iniciando alta de pregunta por usuario ID {}", usuarioActualId);

        // 1. Normalización de campos literales
        String nombre = dto.getNombre() != null ? dto.getNombre().trim().toUpperCase(Locale.ROOT) : "";
        String enunciado = dto.getEnunciado() != null ? dto.getEnunciado().trim().toUpperCase(Locale.ROOT) : "";
        String descripcion = (dto.getDescripcion() != null && !dto.getDescripcion().isBlank())
                ? dto.getDescripcion().trim().toUpperCase(Locale.ROOT)
                : null;
        String codigoFactor = dto.getCodigoFactor() != null ? dto.getCodigoFactor().trim().toUpperCase(Locale.ROOT) : "";

        // 2. Validación de campos obligatorios en el gestor
        if (nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }
        if (enunciado.isBlank()) {
            throw new ReglaNegocioException("El enunciado es obligatorio");
        }
        if (codigoFactor.isBlank()) {
            throw new ReglaNegocioException("El código de factor es obligatorio");
        }
        if (dto.getOpcionesRespuesta() == null || dto.getOpcionesRespuesta().size() < 2) {
            throw new ReglaNegocioException("Debe ingresar al menos 2 opciones de respuesta");
        }

        // 3. Resolución de entidad Factor
        Factor factor = factorDAO.findByCodigo(codigoFactor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factor con código '" + codigoFactor + "' no encontrado"));

        // 4. Validación de unicidad de Nombre y Enunciado para el Factor (Flujo 2.C / Observaciones)
        if (preguntaDAO.existsByFactorAndNombre(factor, nombre)) {
            throw new RecursoDuplicadoException("Ya existe una pregunta con el nombre '" + nombre + "' para el factor seleccionado");
        }
        if (preguntaDAO.existsByFactorAndEnunciado(factor, enunciado)) {
            throw new RecursoDuplicadoException("Ya existe una pregunta con el enunciado '" + enunciado + "' para el factor seleccionado");
        }

        // 5. Validación de ponderaciones (Flujo 2 y Observaciones)
        double sumaPonderaciones = 0.0;
        for (OpcionRespuestaDTO opt : dto.getOpcionesRespuesta()) {
            if (opt.getPonderacion() == null) {
                throw new ReglaNegocioException("La ponderación es obligatoria para todas las opciones");
            }
            if (opt.getPonderacion() < 0.0) {
                throw new ReglaNegocioException("La ponderación debe ser mayor o igual a cero");
            }
            sumaPonderaciones += opt.getPonderacion();
        }

        if (Math.abs(sumaPonderaciones - 10.0) > 0.001) {
            throw new ReglaNegocioException("La suma de las ponderaciones de las opciones de respuesta debe ser exactamente 10 puntos (suma actual: " + sumaPonderaciones + ")");
        }

        // 6. Ordenamiento de opciones según su orden
        List<OpcionRespuestaDTO> opcionesOrdenadas = new ArrayList<>(dto.getOpcionesRespuesta());
        opcionesOrdenadas.sort(Comparator.comparing(OpcionRespuestaDTO::getOrden, Comparator.nullsLast(Comparator.naturalOrder())));

        // 7. Construcción de entidades y persistencia en cascada
        Pregunta pregunta = new Pregunta();
        pregunta.setNombre(nombre);
        pregunta.setEnunciado(enunciado);
        pregunta.setDescripcion(descripcion);
        pregunta.setFactor(factor);
        pregunta.setEliminado(false);

        int index = 1;
        for (OpcionRespuestaDTO optDTO : opcionesOrdenadas) {
            String optNombre = optDTO.getNombre() != null ? optDTO.getNombre().trim().toUpperCase(Locale.ROOT) : "";
            String optDescripcion = optDTO.getDescripcion() != null ? optDTO.getDescripcion().trim().toUpperCase(Locale.ROOT) : "";

            OpcionRespuesta opcion = new OpcionRespuesta();
            opcion.setOrden(optDTO.getOrden() != null ? optDTO.getOrden() : index);
            opcion.setNombre(optNombre);
            opcion.setDescripcion(optDescripcion);
            opcion.setPonderacion(optDTO.getPonderacion());
            opcion.setEliminado(false);

            pregunta.addOpcionRespuesta(opcion);
            index++;
        }

        Pregunta guardada = preguntaDAO.save(pregunta);
        log.info("Pregunta creada exitosamente con ID {} y nombre {}", guardada.getId(), guardada.getNombre());

        return aPreguntaDTO(guardada);
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
        if (!competenciaDAO.existsByCodigo(codigo)) {
            throw new RecursoNoEncontradoException("Competencia con código '" + codigo + "' no encontrada");
        }

        return factorDAO.findByCompetenciaCodigoOrderByNroOrdenAsc(codigo).stream()
                .filter(f -> !Boolean.TRUE.equals(f.getEliminado()))
                .map(this::aFactorDTO)
                .toList();
    }

    private PreguntaDTO aPreguntaDTO(Pregunta entidad) {
        List<OpcionRespuestaDTO> opcionesDTO = entidad.getOpcionesRespuesta().stream()
                .sorted(Comparator.comparing(OpcionRespuesta::getOrden))
                .map(op -> new OpcionRespuestaDTO(
                        op.getId(),
                        op.getOrden(),
                        op.getNombre(),
                        op.getDescripcion(),
                        op.getPonderacion()
                ))
                .toList();

        return new PreguntaDTO(
                entidad.getId(),
                entidad.getNombre(),
                entidad.getEnunciado(),
                entidad.getDescripcion(),
                entidad.getFactor().getCodigo(),
                entidad.getFactor().getCompetencia().getCodigo(),
                opcionesDTO
        );
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
