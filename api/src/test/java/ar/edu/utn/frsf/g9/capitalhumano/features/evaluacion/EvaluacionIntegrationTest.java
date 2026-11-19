package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto.CandidatoResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor.GestorCandidatos;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CrearEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.DetallePuestoEvaluacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluacionCreadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PrevisualizacionEvaluacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PrevisualizarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.gestor.ExportadorExcelEvaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración end-to-end contra H2 y seed.sql para el CU 025 "Evaluar a Candidato".
 */
@SpringBootTest
@Transactional
class EvaluacionIntegrationTest {

    @Autowired
    private GestorCandidatos gestorCandidatos;

    @Autowired
    private EvaluacionDAO evaluacionDAO;

    @Test
    @DisplayName("H2 seed.sql: búsqueda de candidatos 'contiene' case-insensitive")
    void buscarCandidatos_seedData() {
        List<CandidatoResumenDTO> resultado = gestorCandidatos.buscarCandidatos("fern", "ana", "4");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).apellido()).isEqualTo("FERNÁNDEZ");
        assertThat(resultado.get(0).nombre()).isEqualTo("ANA");
    }

    @Test
    @DisplayName("H2 seed.sql: Flujo 6.A bloquea candidatos 1 (ACTIVO) y 2 (EN_PROCESO) pero permite 3 (COMPLETO), 4 (sin cuestionarios), 6 (INCOMPLETO) y 7 (SIN_CONTESTAR)")
    void validarCandidatos_seedData() {
        assertThatThrownBy(() -> gestorCandidatos.verificarSinCuestionarioActivo(List.of(1L, 2L, 4L)))
                .isInstanceOf(ReglaNegocioException.class)
                .satisfies(ex -> {
                    ReglaNegocioException re = (ReglaNegocioException) ex;
                    assertThat(re.getDetalles()).hasSize(2);
                });

        var validacion = gestorCandidatos.verificarSinCuestionarioActivo(List.of(3L, 4L, 6L, 7L));
        assertThat(validacion.valido()).isTrue();
        assertThat(validacion.cantidadCandidatos()).isEqualTo(4);
    }

    @Test
    @DisplayName("H2 seed.sql: Flujo 9.A rechaza puesto 2 (ANALISTA QA) informando RESOLUCIÓN DE PROBLEMAS")
    void competenciasPuesto_seedData_flujo9A() {
        DetallePuestoEvaluacionDTO puesto1 = gestorCandidatos.obtenerDetallePuesto(1L);
        assertThat(puesto1.competencias()).hasSize(2);

        assertThatThrownBy(() -> gestorCandidatos.obtenerDetallePuesto(2L))
                .isInstanceOf(ReglaNegocioException.class)
                .satisfies(ex -> {
                    ReglaNegocioException re = (ReglaNegocioException) ex;
                    assertThat(re.getDetalles())
                            .extracting(ReglaNegocioException.DetalleError::mensaje)
                            .containsExactly("RESOLUCIÓN DE PROBLEMAS");
                });
    }

    @Test
    @DisplayName("H2 seed.sql: flujo completo previsualizar (no persiste) -> crear evaluación -> verificar bloques/preguntas -> exportar Excel .xls")
    void flujoCompleto_previsualizarCrearYExportarExcel() throws Exception {
        long cantidadInicialEvaluaciones = evaluacionDAO.count();

        // Paso 11: previsualizar sin persistir
        PrevisualizacionEvaluacionDTO preview = gestorCandidatos.previsualizarEvaluacion(
                new PrevisualizarEvaluacionRequestDTO(1L, List.of(4L, 5L))
        );
        assertThat(preview.candidatos()).hasSize(2);
        assertThat(evaluacionDAO.count()).isEqualTo(cantidadInicialEvaluaciones);

        String claveAna = preview.candidatos().get(0).claveAcceso();
        String claveLucas = preview.candidatos().get(1).claveAcceso();

        // Paso 13: confirmar evaluación reutilizando claves previsualizadas
        EvaluacionCreadaDTO creada = gestorCandidatos.evaluarCandidatos(
                new CrearEvaluacionRequestDTO(
                        1L,
                        List.of(4L, 5L),
                        Map.of(4L, claveAna, 5L, claveLucas)
                )
        );
        assertThat(evaluacionDAO.count()).isEqualTo(cantidadInicialEvaluaciones + 1);
        assertThat(creada.cuestionarios()).hasSize(2);

        Evaluacion persistida = evaluacionDAO.findById(creada.evaluacionId()).orElseThrow();
        assertThat(persistida.getCuestionarios()).hasSize(2);
        for (Cuestionario q : persistida.getCuestionarios()) {
            assertThat(q.getEstado()).isEqualTo(EstadoCuestionario.ACTIVO);
            // Puesto 1 tiene 3 factores válidos (FAC-01, FAC-02, FAC-04) => 6 preguntas => 2 bloques (5 y 1)
            assertThat(q.getBloques()).hasSize(2);
            assertThat(q.getTotalPreguntasAsignadas()).isEqualTo(6);
        }

        // Tras crear, candidatos 4 y 5 ahora tienen cuestionario ACTIVO y quedan bloqueados por Flujo 6.A
        assertThatThrownBy(() -> gestorCandidatos.verificarSinCuestionarioActivo(List.of(4L)))
                .isInstanceOf(ReglaNegocioException.class);

        // Pasos 14-17: exportar Excel .xls
        ExportadorExcelEvaluacion.ArchivoExcelEvaluacion archivo =
                gestorCandidatos.exportarEvaluacionExcel(creada.evaluacionId());
        assertThat(archivo.nombreArchivo()).endsWith("-DESARROLLADOR SENIOR.xls");

        try (HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(archivo.contenido()))) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(2);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("FERNÁNDEZ");
            assertThat(sheet.getRow(1).getCell(4).getStringCellValue()).isEqualTo(claveAna);
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("MARTÍNEZ");
            assertThat(sheet.getRow(2).getCell(4).getStringCellValue()).isEqualTo(claveLucas);
        }
    }
}
