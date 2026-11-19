package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para {@link GeneradorClaveAcceso} (CU 025 - Requerimiento Especial).
 */
class GeneradorClaveAccesoTest {

    private final GeneradorClaveAcceso generador = new GeneradorClaveAcceso();

    @Test
    @DisplayName("100 iteraciones: genera claves de longitud 8, mayúsculas alfanuméricas, con al menos 1 letra y 1 dígito")
    void generar_100Iteraciones_cumpleReglasDeFormatoYEntropia() {
        Set<String> generadas = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            String clave = generador.generar();

            assertThat(clave)
                    .hasSize(8)
                    .matches("^[A-Z0-9]{8}$");
            assertThat(clave.chars().anyMatch(c -> c >= 'A' && c <= 'Z'))
                    .as("La clave '%s' debe contener al menos una letra mayúscula", clave)
                    .isTrue();
            assertThat(clave.chars().anyMatch(c -> c >= '0' && c <= '9'))
                    .as("La clave '%s' debe contener al menos un dígito", clave)
                    .isTrue();
            assertThat(generador.esValida(clave)).isTrue();

            generadas.add(clave);
        }

        // Con 36^8 combinaciones posibles, 100 iteraciones deben producir alta diversidad
        assertThat(generadas.size()).isGreaterThan(95);
    }

    @Test
    @DisplayName("esValida rechaza claves sin letras, sin dígitos, en minúsculas o de longitud incorrecta")
    void esValida_casosBorde() {
        assertThat(generador.esValida("A1B2C3D4")).isTrue();
        assertThat(generador.esValida("ABCDEFGH")).isFalse();
        assertThat(generador.esValida("12345678")).isFalse();
        assertThat(generador.esValida("a1b2c3d4")).isFalse();
        assertThat(generador.esValida("A1B2C3D")).isFalse();
        assertThat(generador.esValida("A1B2C3D45")).isFalse();
        assertThat(generador.esValida("A1B2-3D4")).isFalse();
        assertThat(generador.esValida(null)).isFalse();
    }
}
