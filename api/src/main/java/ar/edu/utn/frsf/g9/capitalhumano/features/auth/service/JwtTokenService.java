package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para la generación y firma de tokens JWT (JSON Web Tokens) bajo algoritmo HMAC-SHA256.
 */
@Service
public class JwtTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final String secretKey;
    private final long expirationSeconds;
    private final ObjectMapper objectMapper;

    public JwtTokenService(
            @Value("${app.jwt.secret:capital-humano-super-secret-key-for-jwt-signing-2026-utn-frsf}") String secretKey,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds,
            ObjectMapper objectMapper
    ) {
        this.secretKey = secretKey;
        this.expirationSeconds = expirationSeconds;
        this.objectMapper = objectMapper;
    }

    /**
     * Genera un token JWT para un usuario consultor.
     *
     * @param usuario nombre de usuario
     * @param usuarioId identificador numérico
     * @return token JWT firmado
     */
    public String generarTokenConsultor(String usuario, Long usuarioId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", usuario);
        claims.put("rol", "CONSULTOR");
        claims.put("usuarioId", usuarioId);
        return generarToken(claims);
    }

    /**
     * Genera un token JWT para un candidato que realiza un cuestionario.
     *
     * @param nroDocumento número de documento
     * @param candidatoId identificador del candidato
     * @param cuestionarioId identificador del cuestionario
     * @return token JWT firmado
     */
    public String generarTokenCandidato(String nroDocumento, Long candidatoId, Long cuestionarioId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", nroDocumento);
        claims.put("rol", "CANDIDATO");
        claims.put("usuarioId", candidatoId);
        claims.put("cuestionarioId", cuestionarioId);
        return generarToken(claims);
    }

    /**
     * Construye y firma un JWT a partir del mapa de claims.
     *
     * @param claims contenido a incluir en el payload
     * @return JWT firmado en formato standard (header.payload.signature)
     */
    public String generarToken(Map<String, Object> claims) {
        try {
            long now = Instant.now().getEpochSecond();
            Map<String, Object> payload = new HashMap<>(claims);
            payload.put("iat", now);
            payload.put("exp", now + expirationSeconds);

            Map<String, Object> header = Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            );

            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

            String headerEncoded = encoder.encodeToString(
                    objectMapper.writeValueAsString(header).getBytes(StandardCharsets.UTF_8)
            );
            String payloadEncoded = encoder.encodeToString(
                    objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8)
            );

            String contentToSign = headerEncoded + "." + payloadEncoded;
            String signatureEncoded = firmarHmacSha256(contentToSign, secretKey);

            return contentToSign + "." + signatureEncoded;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error al serializar claims de JWT", e);
        }
    }

    /**
     * Valida la firma HMAC-SHA256 y vigencia de un token JWT y extrae sus claims principales.
     *
     * @param token token JWT sin prefijo "Bearer "
     * @return claims validados o {@link Optional#empty()} si el token es inválido o expiró
     */
    public Optional<JwtClaims> validarYParsearToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            return Optional.empty();
        }

        String contentToVerify = partes[0] + "." + partes[1];
        String firmaEsperada = firmarHmacSha256(contentToVerify, secretKey);
        if (!MessageDigest.isEqual(
                firmaEsperada.getBytes(StandardCharsets.UTF_8),
                partes[2].getBytes(StandardCharsets.UTF_8)
        )) {
            return Optional.empty();
        }

        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(partes[1]);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<>() {});

            Long exp = aLong(payload.get("exp"));
            if (exp == null || Instant.now().getEpochSecond() >= exp) {
                return Optional.empty();
            }

            String subject = payload.get("sub") != null ? String.valueOf(payload.get("sub")) : null;
            String rol = payload.get("rol") != null ? String.valueOf(payload.get("rol")) : null;
            Long usuarioId = aLong(payload.get("usuarioId"));
            Long cuestionarioId = aLong(payload.get("cuestionarioId"));

            if (usuarioId == null || rol == null || rol.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new JwtClaims(subject, rol, usuarioId, cuestionarioId));
        } catch (IllegalArgumentException | IOException e) {
            return Optional.empty();
        }
    }

    private Long aLong(Object valor) {
        if (valor instanceof Number num) {
            return num.longValue();
        }
        if (valor instanceof String str && !str.isBlank()) {
            try {
                return Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Claims extraídos de un JWT válido emitido por {@link JwtTokenService}.
     */
    public record JwtClaims(
            String subject,
            String rol,
            Long usuarioId,
            Long cuestionarioId
    ) {
    }

    private String firmarHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Error al firmar token JWT con algoritmo HMAC-SHA256", e);
        }
    }
}
