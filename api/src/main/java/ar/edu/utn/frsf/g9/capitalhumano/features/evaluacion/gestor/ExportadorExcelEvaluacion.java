package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Componente encargado de exportar a formato Excel {@code .xls} ({@link HSSFWorkbook})
 * el listado de candidatos con sus claves de ingreso generadas en una {@link Evaluacion}
 * (CU 025 - Pasos 14 a 17 y Requerimiento Especial).
 */
@Component
public class ExportadorExcelEvaluacion {

    public static final String CONTENT_TYPE_XLS = "application/vnd.ms-excel";
    private static final DateTimeFormatter FORMATO_FECHA_ARCHIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
    private static final String[] ENCABEZADOS = {
            "Apellido",
            "Nombre",
            "Tipo documento",
            "Nro. documento",
            "Clave de ingreso"
    };

    /**
     * Resultado binario y nombre de archivo sanitizado del Excel exportado.
     *
     * @param nombreArchivo nombre con formato {@code aaaa-mm-dd-hh-mm-NombreFunción.xls}
     * @param contenido     bytes del archivo {@code .xls}
     */
    public record ArchivoExcelEvaluacion(String nombreArchivo, byte[] contenido) {
    }

    /**
     * Genera el archivo Excel {@code .xls} completo para la evaluación indicada.
     *
     * @param evaluacion evaluación persistida con puesto, cuestionarios y candidatos cargados
     * @return nombre de archivo y contenido binario del libro {@code .xls}
     */
    public ArchivoExcelEvaluacion exportar(Evaluacion evaluacion) {
        String nombreArchivo = generarNombreArchivo(evaluacion);
        byte[] contenido = generarContenidoXls(evaluacion);
        return new ArchivoExcelEvaluacion(nombreArchivo, contenido);
    }

    /**
     * Construye el nombre del archivo respetando el formato {@code aaaa-mm-dd-hh-mm-NombreFunción.xls}
     * y sanitizando caracteres inválidos para sistemas de archivos.
     *
     * @param evaluacion evaluación de origen
     * @return nombre de archivo válido
     */
    public String generarNombreArchivo(Evaluacion evaluacion) {
        LocalDateTime fecha = evaluacion.getFechaCreacion() != null
                ? evaluacion.getFechaCreacion()
                : LocalDateTime.now();
        String prefijoFecha = fecha.format(FORMATO_FECHA_ARCHIVO);
        String nombrePuesto = evaluacion.getPuesto() != null && evaluacion.getPuesto().getNombre() != null
                ? evaluacion.getPuesto().getNombre()
                : "Evaluacion";
        String puestoSanitizado = sanitizarNombreArchivo(nombrePuesto);
        return prefijoFecha + "-" + puestoSanitizado + ".xls";
    }

    private String sanitizarNombreArchivo(String texto) {
        String limpio = texto.trim()
                .replaceAll("[\\\\/:*?\"<>|\\r\\n\\t]+", "_")
                .replaceAll("\\s+", " ")
                .replaceAll("_+", "_")
                .replaceAll("^[._ ]+|[._ ]+$", "");
        return limpio.isEmpty() ? "Evaluacion" : limpio;
    }

    private byte[] generarContenidoXls(Evaluacion evaluacion) {
        try (Workbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Candidatos");

            CellStyle estiloEncabezado = workbook.createCellStyle();
            Font fuenteNegrita = workbook.createFont();
            fuenteNegrita.setBold(true);
            estiloEncabezado.setFont(fuenteNegrita);

            Row filaEncabezado = sheet.createRow(0);
            for (int col = 0; col < ENCABEZADOS.length; col++) {
                Cell celda = filaEncabezado.createCell(col);
                celda.setCellValue(ENCABEZADOS[col]);
                celda.setCellStyle(estiloEncabezado);
            }

            List<Cuestionario> cuestionariosOrdenados = evaluacion.getCuestionarios().stream()
                    .sorted(Comparator
                            .comparing((Cuestionario c) -> c.getCandidato() != null && c.getCandidato().getApellido() != null
                                    ? c.getCandidato().getApellido() : "")
                            .thenComparing(c -> c.getCandidato() != null && c.getCandidato().getNombre() != null
                                    ? c.getCandidato().getNombre() : "")
                            .thenComparing(c -> c.getId() != null ? c.getId() : 0L))
                    .toList();

            int filaIdx = 1;
            for (Cuestionario cuestionario : cuestionariosOrdenados) {
                Row fila = sheet.createRow(filaIdx++);
                Candidato candidato = cuestionario.getCandidato();
                String apellido = candidato != null && candidato.getApellido() != null ? candidato.getApellido() : "";
                String nombre = candidato != null && candidato.getNombre() != null ? candidato.getNombre() : "";
                String tipoDoc = candidato != null && candidato.getTipoDocumento() != null ? candidato.getTipoDocumento().name() : "";
                String nroDoc = candidato != null && candidato.getNroDocumento() != null ? candidato.getNroDocumento() : "";
                String clave = cuestionario.getClaveAcceso() != null ? cuestionario.getClaveAcceso() : "";

                fila.createCell(0).setCellValue(apellido);
                fila.createCell(1).setCellValue(nombre);
                fila.createCell(2).setCellValue(tipoDoc);
                fila.createCell(3).setCellValue(nroDoc);
                fila.createCell(4).setCellValue(clave);
            }

            // Anchos fijos razonables (seguro en entornos sin fuentes gráficas AWT)
            sheet.setColumnWidth(0, 25 * 256);
            sheet.setColumnWidth(1, 25 * 256);
            sheet.setColumnWidth(2, 18 * 256);
            sheet.setColumnWidth(3, 20 * 256);
            sheet.setColumnWidth(4, 20 * 256);

            workbook.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Error al generar el archivo Excel de la evaluación", ex);
        }
    }
}
