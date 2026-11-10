package ar.edu.utn.frsf.g9.capitalhumano.features.factor.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dao.CompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dto.FactorDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.ContextoSeguridad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Gestor de lógica de negocio para la administración de Factores (CU 017).
 */
@Service
public class GestorFactor {

    private static final Logger log = LoggerFactory.getLogger(GestorFactor.class);

    private final FactorDAO factorDAO;
    private final CompetenciaDAO competenciaDAO;
    private final ContextoSeguridad contextoSeguridad;

    public GestorFactor(FactorDAO factorDAO,
                        CompetenciaDAO competenciaDAO,
                        ContextoSeguridad contextoSeguridad) {
        this.factorDAO = factorDAO;
        this.competenciaDAO = competenciaDAO;
        this.contextoSeguridad = contextoSeguridad;
    }

    /**
     * Da de alta un nuevo factor en el sistema (CU 017).
     *
     * <p>Aplica normalización de texto a mayúsculas con trim sobre los campos literales,
     * valida la unicidad de código y nombre en el sistema, verifica la existencia de la
     * competencia asociada, y comprueba que el número de orden sea único para dicha competencia.</p>
     *
     * @param dto datos de entrada del factor a crear
     * @return DTO del factor persistido con su ID generado y relaciones resueltas
     * @throws RecursoDuplicadoException si el código o nombre ya existen en el sistema (Flujo 2.B)
     *                                   o si el número de orden ya existe para la competencia (Flujo 2.C)
     * @throws RecursoNoEncontradoException si la competencia referenciada no existe
     */
    @Transactional
    public FactorDTO crearFactor(FactorDTO dto) {
        Long usuarioActualId = contextoSeguridad != null ? contextoSeguridad.getUsuarioActualId() : null;
        log.info("Iniciando alta de factor por usuario ID {}", usuarioActualId);

        // 1. Normalización de campos literales a mayúsculas
        String codigo = dto.getCodigo() != null ? dto.getCodigo().trim().toUpperCase(Locale.ROOT) : "";
        String nombre = dto.getNombre() != null ? dto.getNombre().trim().toUpperCase(Locale.ROOT) : "";
        String descripcion = dto.getDescripcion() != null ? dto.getDescripcion().trim().toUpperCase(Locale.ROOT) : "";

        // 2. Unicidad de código en el sistema (Flujo 2.B)
        if (factorDAO.existsByCodigo(codigo)) {
            throw new RecursoDuplicadoException("Ya existe un factor con el código '" + codigo + "'");
        }

        // 3. Unicidad de nombre en el sistema (Flujo 2.B)
        if (factorDAO.existsByNombre(nombre)) {
            throw new RecursoDuplicadoException("Ya existe un factor con el nombre '" + nombre + "'");
        }

        // 4. Existencia de la Competencia asociada (404 Not Found)
        Competencia competencia = competenciaDAO.findById(dto.getCompetenciaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Competencia", dto.getCompetenciaId()));

        // 5. Unicidad de número de orden por competencia (Flujo 2.C)
        if (factorDAO.existsByCompetenciaIdAndNroOrden(dto.getCompetenciaId(), dto.getNroOrden())) {
            throw new RecursoDuplicadoException(
                    "Ya existe un factor con el número de orden " + dto.getNroOrden() + " para la competencia seleccionada");
        }

        // 6. Construcción y persistencia de la entidad
        Factor factor = new Factor();
        factor.setCodigo(codigo);
        factor.setNombre(nombre);
        factor.setDescripcion(descripcion);
        factor.setNroOrden(dto.getNroOrden());
        factor.setCompetencia(competencia);
        factor.setEliminado(false);

        Factor guardado = factorDAO.save(factor);
        log.info("Factor creado exitosamente con ID {} y código {}", guardado.getId(), guardado.getCodigo());

        return aDTO(guardado);
    }

    /**
     * Obtiene la lista de todos los factores registrados en el sistema.
     *
     * @return lista de factores en formato DTO
     */
    @Transactional(readOnly = true)
    public List<FactorDTO> listarFactores() {
        return factorDAO.findAll().stream()
                .map(this::aDTO)
                .toList();
    }

    /**
     * Obtiene los factores de una competencia ordenados por número de orden.
     *
     * @param competenciaId identificador de la competencia
     * @return lista de factores pertenecientes a la competencia
     */
    @Transactional(readOnly = true)
    public List<FactorDTO> listarPorCompetencia(Long competenciaId) {
        return factorDAO.findByCompetenciaIdOrderByNroOrdenAsc(competenciaId).stream()
                .map(this::aDTO)
                .toList();
    }

    private FactorDTO aDTO(Factor entidad) {
        return new FactorDTO(
                entidad.getId(),
                entidad.getCodigo(),
                entidad.getNombre(),
                entidad.getDescripcion(),
                entidad.getCompetencia() != null ? entidad.getCompetencia().getId() : null,
                entidad.getCompetencia() != null ? entidad.getCompetencia().getNombre() : null,
                entidad.getNroOrden()
        );
    }
}

