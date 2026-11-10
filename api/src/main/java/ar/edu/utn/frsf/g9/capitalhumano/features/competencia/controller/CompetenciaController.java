package ar.edu.utn.frsf.g9.capitalhumano.features.competencia.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dto.CompetenciaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.gestor.GestorCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dto.FactorDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor.GestorPregunta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión de Competencias y sus Factores asociados (CU 013 / CU 021).
 */
@RestController
@RequestMapping("/api/competencias")
@Tag(name = "Competencias", description = "Endpoints para la gestión de Competencias y Factores")
public class CompetenciaController {

    private final GestorCompetencia gestorCompetencia;
    private final GestorPregunta gestorPregunta;

    @Autowired
    public CompetenciaController(GestorCompetencia gestorCompetencia, GestorPregunta gestorPregunta) {
        this.gestorCompetencia = gestorCompetencia;
        this.gestorPregunta = gestorPregunta;
    }

    public CompetenciaController(GestorCompetencia gestorCompetencia) {
        this(gestorCompetencia, null);
    }

    /**
     * Da de alta una nueva competencia.
     *
     * @param dto datos de la competencia a crear
     * @return competencia creada con HTTP 201
     */
    @PostMapping
    @Operation(summary = "Dar de alta una nueva competencia", description = "Registra una competencia en el sistema.")
    public ResponseEntity<CompetenciaDTO> crearCompetencia(@Valid @RequestBody CompetenciaDTO dto) {
        CompetenciaDTO creada = gestorCompetencia.crearCompetencia(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Obtiene la lista completa de competencias disponibles.
     *
     * @return lista de competencias con HTTP 200
     */
    @GetMapping
    @Operation(summary = "Listar competencias", description = "Retorna el listado de competencias activas para selectores.")
    public ResponseEntity<List<CompetenciaDTO>> listarCompetencias() {
        return ResponseEntity.ok(gestorCompetencia.listarCompetencias());
    }

    /**
     * Carga dinámicamente todos los factores correspondientes a una competencia seleccionada (CU 021).
     *
     * @param codigoCompetencia código de la competencia
     * @return lista de factores correspondientes con HTTP 200
     */
    @GetMapping("/{codigoCompetencia}/factores")
    @Operation(summary = "Listar factores por competencia", description = "Carga dinámicamente todos los factores asociados a una competencia seleccionada.")
    public ResponseEntity<List<FactorDTO>> listarFactoresPorCompetencia(@PathVariable String codigoCompetencia) {
        if (gestorPregunta == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(gestorPregunta.listarFactoresPorCompetencia(codigoCompetencia));
    }
}
