package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Enumeración que representa las nacionalidades de Latinoamérica admitidas para los candidatos.
 * Permite reconocer de forma indistinta países y gentilicios (femenino y masculino),
 * sin distinguir entre mayúsculas, minúsculas o tildes.
 */
public enum Nacionalidad {

    ARGENTINA("Argentina", "argentina", "argentino"),
    BOLIVIA("Bolivia", "bolivia", "boliviano", "boliviana"),
    BRASIL("Brasil", "brasil", "brazil", "brasileño", "brasileña", "brasilero", "brasilera"),
    CHILE("Chile", "chile", "chileno", "chilena"),
    COLOMBIA("Colombia", "colombia", "colombiano", "colombiana"),
    COSTA_RICA("Costa Rica", "costa rica", "costarricense", "costarricence"),
    CUBA("Cuba", "cuba", "cubano", "cubana"),
    ECUADOR("Ecuador", "ecuador", "ecuatoriano", "ecuatoriana"),
    EL_SALVADOR("El Salvador", "el salvador", "salvadoreño", "salvadoreña"),
    GUATEMALA("Guatemala", "guatemala", "guatemalteco", "guatemalteca"),
    HONDURAS("Honduras", "honduras", "hondureño", "hondureña"),
    MEXICO("México", "mexico", "méxico", "mexicano", "mexicana"),
    NICARAGUA("Nicaragua", "nicaragua", "nicaragüense", "nicaraguense"),
    PANAMA("Panamá", "panama", "panamá", "panameño", "panameña"),
    PARAGUAY("Paraguay", "paraguay", "paraguayo", "paraguaya"),
    PERU("Perú", "peru", "perú", "peruano", "peruana"),
    PUERTO_RICO("Puerto Rico", "puerto rico", "puertorriqueño", "puertorriqueña"),
    REPUBLICA_DOMINICANA("República Dominicana", "republica dominicana", "república dominicana", "dominicano", "dominicana"),
    URUGUAY("Uruguay", "uruguay", "uruguayo", "uruguaya"),
    VENEZUELA("Venezuela", "venezuela", "venezolano", "venezolana");

    private final String nombrePais;
    private final Set<String> aliasNormalizados;

    Nacionalidad(String nombrePais, String... alias) {
        this.nombrePais = nombrePais;
        Set<String> set = new HashSet<>();
        set.add(normalizarCadena(nombrePais));
        set.add(normalizarCadena(name()));
        for (String a : alias) {
            set.add(normalizarCadena(a));
        }
        this.aliasNormalizados = Collections.unmodifiableSet(set);
    }

    public String getNombrePais() {
        return nombrePais;
    }

    public Set<String> getAliasNormalizados() {
        return aliasNormalizados;
    }

    /**
     * Parsea una cadena de texto y retorna la Nacionalidad correspondiente si coincide
     * con alguno de los países o gentilicios admitidos.
     *
     * @param raw texto ingresado
     * @return Optional con la Nacionalidad encontrada o empty si no es válida
     */
    public static Optional<Nacionalidad> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        String normalizado = normalizarCadena(raw);

        for (Nacionalidad n : values()) {
            if (n.aliasNormalizados.contains(normalizado)) {
                return Optional.of(n);
            }
        }

        return Optional.empty();
    }

    /**
     * Quita acentos, signos diacríticos, espacios redundantes y pasa a minúsculas.
     */
    private static String normalizarCadena(String input) {
        if (input == null) return "";
        String base = Normalizer.normalize(input.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return base.replaceAll("\\p{M}", "").replaceAll("\\s+", " ");
    }
}
