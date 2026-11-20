package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dao.CandidatoImportRepository;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.ImportacionCandidatosResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ImportarCandidatosIntegrationTest {

    @Autowired
    private ImportarCandidatosService importarCandidatosService;

    @Autowired
    private CandidatoImportRepository candidatoImportRepository;

    @Test
    @DisplayName("Integración H2 (seed 1..7): actualiza candidato existente conservando nro '1' y asigna '8' y '9' a nuevos")
    void importarCandidatos_sobreSeedH2_actualizaExistenteYAsignaMaxMasUno() {
        String csvContent = "Apellido;Nombre;Nro documento;Tipo documento;Fecha de nacimiento;Género;Nacionalidad;Email;Escolaridad;Eliminado\n"
                + "PÉREZ ACTUALIZADO;JUAN;12.345.678;DNI;1990-05-14;H;ARGENTINA;juan.nuevo@example.com;UNIVERSITARIO;False\n"
                + "CASTRO;LUCÍA;42.111.222;DNI;1999-04-12;M;ARGENTINA;lucia.castro@example.com;TERCIARIO;False\n"
                + "MORALES;MATEO;ab 998877;PP;1994-09-20;H;URUGUAY;mateo.morales@example.com;UNIVERSITARIO;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "integracion_cu006.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportacionCandidatosResponseDTO respuesta = importarCandidatosService.importarCandidatos(file);

        assertThat(respuesta.candidatosCreados()).isEqualTo(2);
        assertThat(respuesta.candidatosActualizados()).isEqualTo(1);
        assertThat(respuesta.candidatosEliminados()).isEqualTo(0);

        Candidato actualizado = candidatoImportRepository.findByNroCandidato("1").orElseThrow();
        assertThat(actualizado.getApellido()).isEqualTo("PÉREZ ACTUALIZADO");
        assertThat(actualizado.getEmail()).isEqualTo("juan.nuevo@example.com");
        assertThat(actualizado.getNroDocumento()).isEqualTo("12345678");

        Candidato nuevo8 = candidatoImportRepository.findByNroCandidato("8").orElseThrow();
        assertThat(nuevo8.getApellido()).isEqualTo("CASTRO");
        assertThat(nuevo8.getNroDocumento()).isEqualTo("42111222");

        Candidato nuevo9 = candidatoImportRepository.findByNroCandidato("9").orElseThrow();
        assertThat(nuevo9.getApellido()).isEqualTo("MORALES");
        assertThat(nuevo9.getNroDocumento()).isEqualTo("AB998877");
    }
}
