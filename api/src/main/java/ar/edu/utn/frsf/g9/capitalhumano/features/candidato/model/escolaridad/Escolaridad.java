package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.escolaridad;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

/**
 * Abstracción del patrón Bridge para la Escolaridad / Nivel de Estudios del Candidato.
 * Desacopla la jerarquía de niveles de estudio (Primaria, Secundaria, Terciaria, Universitaria)
 * de la dimensión de su estado de finalización (Completo / Incompleto).
 */
public abstract class Escolaridad {

    protected final EstadoEstudio estado;
    protected final boolean especificoEstadoExplicito;

    protected Escolaridad(EstadoEstudio estado, boolean especificoEstadoExplicito) {
        this.estado = estado != null ? estado : new EstadoCompleto();
        this.especificoEstadoExplicito = especificoEstadoExplicito;
    }

    protected Escolaridad(EstadoEstudio estado) {
        this(estado, true);
    }

    /**
     * Retorna el nivel de estudio base (ej: "PRIMARIO", "SECUNDARIO", "TERCIARIO", "UNIVERSITARIO").
     */
    public abstract String getNivel();

    public EstadoEstudio getEstado() {
        return estado;
    }

    public boolean isEspecificoEstadoExplicito() {
        return especificoEstadoExplicito;
    }

    /**
     * Retorna la descripción canónica unificada.
     * Si no se especificó si era completo o incompleto (ej: ingresó solo "UNIVERSITARIO"),
     * retorna el nivel "UNIVERSITARIO". Si especificó la variante, retorna ej: "UNIVERSITARIO COMPLETO" o "UNIVERSITARIO INCOMPLETO".
     */
    public String getDescripcion() {
        if (!especificoEstadoExplicito && estado.isCompleto()) {
            return getNivel();
        }
        return getNivel() + " " + estado.getDescripcion();
    }

    public String getDescripcionCompleta() {
        return getNivel() + " " + estado.getDescripcion();
    }

    @Override
    public String toString() {
        return getDescripcion();
    }

    /**
     * Factory / Parser que reconoce el nivel y la variante independientemente de mayúsculas, minúsculas o tildes.
     */
    public static Optional<Escolaridad> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        String normalizado = Normalizer.normalize(raw.trim().toUpperCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ");

        boolean tieneIncompleto = normalizado.contains("INCOMPLET");
        boolean tieneCompleto = normalizado.contains("COMPLET") && !tieneIncompleto;
        boolean explicitVariant = tieneIncompleto || tieneCompleto;

        EstadoEstudio estado = tieneIncompleto ? new EstadoIncompleto() : new EstadoCompleto();

        if (normalizado.contains("PRIMARI")) {
            return Optional.of(new Primaria(estado, explicitVariant));
        } else if (normalizado.contains("SECUNDARI")) {
            return Optional.of(new Secundaria(estado, explicitVariant));
        } else if (normalizado.contains("TERCIARI")) {
            return Optional.of(new Terciaria(estado, explicitVariant));
        } else if (normalizado.contains("UNIVERSITARI")) {
            return Optional.of(new Universitaria(estado, explicitVariant));
        }

        return Optional.empty();
    }
}
