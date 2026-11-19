package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.ErrorImportacionDTO.DetalleErrorFilaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.ImportacionCandidatosResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.exception.CsvImportacionException;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.service.ImportarCandidatosService;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ImportarCandidatosControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ImportarCandidatosService service;

    @BeforeEach
    void setUp() {
        ImportarCandidatosController controller = new ImportarCandidatosController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/candidatos/importar con archivo válido retorna HTTP 200 y contadores")
    void importarCandidatos_valido_retorna200() throws Exception {
        ImportacionCandidatosResponseDTO respuesta = new ImportacionCandidatosResponseDTO(15, 4, 2);
        when(service.importarCandidatos(any(MultipartFile.class))).thenReturn(respuesta);

        MockMultipartFile file = new MockMultipartFile(
                "file", "candidatos.csv", "text/csv", "contenido-csv".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/candidatos/importar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidatosCreados").value(15))
                .andExpect(jsonPath("$.candidatosActualizados").value(4))
                .andExpect(jsonPath("$.candidatosEliminados").value(2));
    }

    @Test
    @DisplayName("POST /api/candidatos/importar con errores en el CSV retorna HTTP 400 y ErrorImportacionDTO")
    void importarCandidatos_errorValidacion_retorna400() throws Exception {
        List<DetalleErrorFilaDTO> detalles = List.of(
                new DetalleErrorFilaDTO(2, "Tipo documento", "Tipo de documento inválido: 'PASAPORTE'. Valores permitidos: DNI, LE, LC, PP."),
                new DetalleErrorFilaDTO(3, "Fecha de nacimiento", "Fecha de nacimiento inválida: '1990-02-31'.")
        );
        when(service.importarCandidatos(any(MultipartFile.class)))
                .thenThrow(new CsvImportacionException("El archivo CSV contiene errores de formato o validación.", detalles));

        MockMultipartFile file = new MockMultipartFile(
                "file", "errores.csv", "text/csv", "contenido-invalido".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/candidatos/importar").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El archivo CSV contiene errores de formato o validación."))
                .andExpect(jsonPath("$.errores").isArray())
                .andExpect(jsonPath("$.errores.length()").value(2))
                .andExpect(jsonPath("$.errores[0].fila").value(2))
                .andExpect(jsonPath("$.errores[0].campo").value("Tipo documento"))
                .andExpect(jsonPath("$.errores[1].fila").value(3))
                .andExpect(jsonPath("$.errores[1].campo").value("Fecha de nacimiento"));
    }
}
