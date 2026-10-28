package com.matticry.corebusiness.infrastructure.adapter.security;

import com.matticry.corebusiness.domain.port.out.auth.TokenServicePort;
import com.matticry.corebusiness.infrastructure.config.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador que emite access tokens JWT firmados con HS256 y refresh tokens opacos.
 */
@Component
@RequiredArgsConstructor
public class TokenServiceAdapter implements TokenServicePort {

    private static final int REFRESH_TOKEN_BYTES = 64;
    private static final String SCOPE_USERS = "users";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .audience(List.of(jwtProperties.getAudience()))
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("email", email)
                .claim("scope", SCOPE_USERS)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public String generateRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se pudo inicializar SHA-256.", ex);
        }
    }

    @Override
    public OffsetDateTime getAccessTokenExpiration() {
        return OffsetDateTime.now().plusMinutes(jwtProperties.getAccessTokenExpirationMinutes());
    }

    @Override
    public OffsetDateTime getRefreshTokenExpiration() {
        return OffsetDateTime.now().plusDays(jwtProperties.getRefreshTokenExpirationDays());
    }
}
