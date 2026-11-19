package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para {@link ExportadorExcelEvaluacion} (CU 025 - Pasos 14 a 17 y Req. Especial).
 */
class ExportadorExcelEvaluacionTest {

    private final ExportadorExcelEvaluacion exportador = new ExportadorExcelEvaluacion();

    @Test
    @DisplayName("Exporta archivo .xls parseable con HSSFWorkbook con columnas exactas, filas ordenadas y nombre de archivo correcto")
    void exportar_generaXlsValidoConColumnasYFilasCorrectas() throws Exception {
        Puesto puesto = new Puesto(1L, "DEV-01", "DESARROLLADOR SENIOR", "DESC", "TECH SOLUTIONS");
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 19, 14, 35);
        Evaluacion evaluacion = new Evaluacion(puesto, fecha, 1L, 60, 5);

        Candidato c1 = new Candidato(4L, "4", TipoDocumento.DNI, "30456789", "FERNÁNDEZ", "ANA", "ana@example.com");
        Candidato c2 = new Candidato(6L, "6", TipoDocumento.PP, "40112233", "RODRÍGUEZ", "SOFÍA", "sofia@example.com");

        Cuestionario q1 = new Cuestionario(10L, c2, EstadoCuestionario.ACTIVO, "Z9Y8X7W6");
        Cuestionario q2 = new Cuestionario(11L, c1, EstadoCuestionario.ACTIVO, "A1B2C3D4");
        evaluacion.addCuestionario(q1);
        evaluacion.addCuestionario(q2);

        ExportadorExcelEvaluacion.ArchivoExcelEvaluacion archivo = exportador.exportar(evaluacion);

        assertThat(archivo.nombreArchivo()).isEqualTo("2026-11-19-14-35-DESARROLLADOR SENIOR.xls");
        assertThat(archivo.contenido()).isNotEmpty();

        try (HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(archivo.contenido()))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
            Sheet sheet = workbook.getSheetAt(0);

            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Apellido");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Nombre");
            assertThat(header.getCell(2).getStringCellValue()).isEqualTo("Tipo documento");
            assertThat(header.getCell(3).getStringCellValue()).isEqualTo("Nro. documento");
            assertThat(header.getCell(4).getStringCellValue()).isEqualTo("Clave de ingreso");

            // Ordenado alfabéticamente por Apellido, Nombre
            Row fila1 = sheet.getRow(1);
            assertThat(fila1.getCell(0).getStringCellValue()).isEqualTo("FERNÁNDEZ");
            assertThat(fila1.getCell(1).getStringCellValue()).isEqualTo("ANA");
            assertThat(fila1.getCell(2).getStringCellValue()).isEqualTo("DNI");
            assertThat(fila1.getCell(3).getStringCellValue()).isEqualTo("30456789");
            assertThat(fila1.getCell(4).getStringCellValue()).isEqualTo("A1B2C3D4");

            Row fila2 = sheet.getRow(2);
            assertThat(fila2.getCell(0).getStringCellValue()).isEqualTo("RODRÍGUEZ");
            assertThat(fila2.getCell(1).getStringCellValue()).isEqualTo("SOFÍA");
            assertThat(fila2.getCell(2).getStringCellValue()).isEqualTo("PP");
            assertThat(fila2.getCell(3).getStringCellValue()).isEqualTo("40112233");
            assertThat(fila2.getCell(4).getStringCellValue()).isEqualTo("Z9Y8X7W6");

            assertThat(sheet.getLastRowNum()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Sanitiza caracteres inválidos de archivo en el nombre de la función")
    void generarNombreArchivo_sanitizaCaracteresInvalidos() {
        Puesto puesto = new Puesto(2L, "DEV-02", "LÍDER TÉCNICO / QA: *N1* <Sr>", "DESC", "TECH");
        Evaluacion evaluacion = new Evaluacion(puesto, LocalDateTime.of(2026, 5, 7, 9, 4), 1L, 60, 5);

        String nombre = exportador.generarNombreArchivo(evaluacion);

        assertThat(nombre).startsWith("2026-05-07-09-04-");
        assertThat(nombre).endsWith(".xls");
        assertThat(nombre).doesNotContain("/", "\\", ":", "*", "?", "\"", "<", ">", "|");
    }
}
