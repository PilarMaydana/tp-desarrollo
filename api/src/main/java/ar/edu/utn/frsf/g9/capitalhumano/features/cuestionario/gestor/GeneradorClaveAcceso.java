package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Generador criptográficamente seguro de claves de acceso para cuestionarios (CU 025 - Req. Especial).
 *
 * <p>Produce claves de exactamente 8 caracteres alfanuméricos en mayúsculas ({@code [A-Z0-9]}),
 * garantizando al menos una letra y al menos un dígito con posiciones mezcladas aleatoriamente.</p>
 */
@Component
public class GeneradorClaveAcceso {

    public static final int LONGITUD_CLAVE = 8;
    private static final String LETRAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITOS = "0123456789";
    private static final String ALFANUMERICOS = LETRAS + DIGITOS;
    private static final Pattern PATRON_CLAVE_VALIDA = Pattern.compile("^(?=.*[A-Z])(?=.*[0-9])[A-Z0-9]{8}$");

    private final SecureRandom secureRandom;

    public GeneradorClaveAcceso() {
        this(new SecureRandom());
    }

    public GeneradorClaveAcceso(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * Genera una nueva clave de acceso aleatoria de 8 caracteres alfanuméricos en mayúsculas
     * con al menos una letra y al menos un dígito.
     *
     * @return clave generada
     */
    public String generar() {
        char[] clave = new char[LONGITUD_CLAVE];
        clave[0] = LETRAS.charAt(secureRandom.nextInt(LETRAS.length()));
        clave[1] = DIGITOS.charAt(secureRandom.nextInt(DIGITOS.length()));

        for (int i = 2; i < LONGITUD_CLAVE; i++) {
            clave[i] = ALFANUMERICOS.charAt(secureRandom.nextInt(ALFANUMERICOS.length()));
        }

        // Fisher-Yates shuffle con SecureRandom
        for (int i = LONGITUD_CLAVE - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temporal = clave[i];
            clave[i] = clave[j];
            clave[j] = temporal;
        }

        return new String(clave);
    }

    /**
     * Verifica si una clave cumple con el formato requerido por el CU 025.
     *
     * @param clave clave normalizada a validar
     * @return {@code true} si tiene 8 caracteres en {@code [A-Z0-9]} con al menos 1 letra y 1 dígito
     */
    public boolean esValida(String clave) {
        return clave != null && PATRON_CLAVE_VALIDA.matcher(clave).matches();
    }
}
