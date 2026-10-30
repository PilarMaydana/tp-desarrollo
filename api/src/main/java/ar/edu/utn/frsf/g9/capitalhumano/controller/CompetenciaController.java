package ar.edu.utn.frsf.g9.capitalhumano.controller;

import ar.edu.utn.frsf.g9.capitalhumano.dto.CompetenciaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.gestor.GestorCompetencia;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de Competencias (CU 013).
 */
@RestController
@RequestMapping("/api/competencias")
public class CompetenciaController {

    private final GestorCompetencia gestorCompetencia;

    public CompetenciaController(GestorCompetencia gestorCompetencia) {
        this.gestorCompetencia = gestorCompetencia;
    }

    /**
     * Da de alta una nueva competencia.
     *
     * @param dto datos de la competencia a crear
     * @return competencia creada con HTTP 201
     */
    @PostMapping
    public ResponseEntity<CompetenciaDTO> crearCompetencia(@Valid @RequestBody CompetenciaDTO dto) {
        CompetenciaDTO creada = gestorCompetencia.crearCompetencia(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}

