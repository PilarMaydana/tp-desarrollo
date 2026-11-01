package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dao.CompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto.CaracteristicaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto.PuestoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.ContextoSeguridad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Gestor de lógica de negocio para la gestión de Puestos (CU 009).
 */
@Service
public class GestorPuesto {

    private static final Logger log = LoggerFactory.getLogger(GestorPuesto.class);

    private final PuestoDAO puestoDAO;
    private final CompetenciaDAO competenciaDAO;
    private final ContextoSeguridad contextoSeguridad;

    public GestorPuesto(PuestoDAO puestoDAO,
                        CompetenciaDAO competenciaDAO,
                        ContextoSeguridad contextoSeguridad) {
        this.puestoDAO = puestoDAO;
        this.competenciaDAO = competenciaDAO;
        this.contextoSeguridad = contextoSeguridad;
    }

    /**
     * Da de alta un nuevo puesto en el sistema (CU 009).
     *
     * <p>Aplica normalización de texto (trim y mayúsculas) antes de validar reglas
     * de unicidad, verificar ausencia de competencias duplicadas y persistir la entidad.</p>
     *
     * @param dto DTO con los datos del puesto y sus características a registrar
     * @return DTO con el puesto creado, IDs generados y entidades asociadas resueltas
     * @throws RecursoDuplicadoException si ya existe un puesto con el mismo código o nombre
     * @throws ReglaNegocioException si alguna regla de negocio es violada (ej. competencias repetidas o ponderación fuera de rango)
     * @throws RecursoNoEncontradoException si alguna de las competencias referenciadas no existe
     */
    @Transactional
    public PuestoDTO crearPuesto(PuestoDTO dto) {
        Long usuarioActualId = contextoSeguridad.getUsuarioActualId();
        log.info("Iniciando alta de puesto por usuario ID {}", usuarioActualId);

        // 1. Normalización de campos literales
        String codigo = dto.getCodigo() != null ? dto.getCodigo().trim().toUpperCase(Locale.ROOT) : "";
        String nombre = dto.getNombre() != null ? dto.getNombre().trim().toUpperCase(Locale.ROOT) : "";
        String descripcion = dto.getDescripcion() != null ? dto.getDescripcion().trim().toUpperCase(Locale.ROOT) : "";
        String empresa = dto.getEmpresa() != null ? dto.getEmpresa().trim().toUpperCase(Locale.ROOT) : "";

        // 2. Validación de campos obligatorios respaldada en gestor
        if (codigo.isBlank()) {
            throw new ReglaNegocioException("El código es obligatorio");
        }
        if (nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }
        if (descripcion.isBlank()) {
            throw new ReglaNegocioException("La descripción es obligatoria");
        }
        if (empresa.isBlank()) {
            throw new ReglaNegocioException("La empresa es obligatoria");
        }
        if (dto.getCaracteristicas() == null || dto.getCaracteristicas().isEmpty()) {
            throw new ReglaNegocioException("Debe ingresar al menos una característica");
        }

        // 3. Unicidad de Código y Nombre (Flujo 2.B)
        if (puestoDAO.existsByCodigo(codigo)) {
            throw new RecursoDuplicadoException("Ya existe un puesto con el código '" + codigo + "'");
        }
        if (puestoDAO.existsByNombre(nombre)) {
            throw new RecursoDuplicadoException("Ya existe un puesto con el nombre '" + nombre + "'");
        }

        // 4. Validación de competencias únicas y ponderaciones (en memoria sobre el DTO)
        Set<Long> competenciasProcesadas = new HashSet<>();
        for (CaracteristicaDTO carDTO : dto.getCaracteristicas()) {
            Long compId = carDTO.getCompetenciaId();
            if (compId == null) {
                throw new ReglaNegocioException("El ID de competencia es obligatorio");
            }
            if (!competenciasProcesadas.add(compId)) {
                throw new ReglaNegocioException("La competencia con ID " + compId + " está repetida en el puesto");
            }

            Double pond = carDTO.getPonderacion();
            if (pond == null || pond < 0.0 || pond > 10.0) {
                throw new ReglaNegocioException("La ponderación debe ser un valor entre 0 y 10");
            }
        }

        // 5. Búsqueda y resolución de entidades Competencia
        List<CaracteristicaPuesto> caracteristicasEntidad = new ArrayList<>();
        for (CaracteristicaDTO carDTO : dto.getCaracteristicas()) {
            Competencia competencia = competenciaDAO.findById(carDTO.getCompetenciaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Competencia", carDTO.getCompetenciaId()));

            CaracteristicaPuesto caracteristica = new CaracteristicaPuesto();
            caracteristica.setCompetencia(competencia);
            caracteristica.setPonderacionRequerida(carDTO.getPonderacion());
            caracteristicasEntidad.add(caracteristica);
        }

        // 6. Creación y persistencia de la entidad Puesto con composición
        Puesto puesto = new Puesto();
        puesto.setCodigo(codigo);
        puesto.setNombre(nombre);
        puesto.setDescripcion(descripcion);
        puesto.setEmpresa(empresa);
        puesto.setEliminado(false);

        for (CaracteristicaPuesto cp : caracteristicasEntidad) {
            puesto.addCaracteristica(cp);
        }

        Puesto guardado = puestoDAO.save(puesto);
        log.info("Puesto creado exitosamente con ID {} y código {}", guardado.getId(), guardado.getCodigo());

        return aDTO(guardado);
    }

    private PuestoDTO aDTO(Puesto entidad) {
        List<CaracteristicaDTO> caracteristicasDTO = entidad.getCaracteristicas().stream()
                .map(cp -> new CaracteristicaDTO(
                        cp.getId(),
                        cp.getCompetencia().getId(),
                        cp.getCompetencia().getNombre(),
                        cp.getPonderacionRequerida()
                ))
                .toList();

        return new PuestoDTO(
                entidad.getId(),
                entidad.getCodigo(),
                entidad.getNombre(),
                entidad.getDescripcion(),
                entidad.getEmpresa(),
                caracteristicasDTO
        );
    }
}
