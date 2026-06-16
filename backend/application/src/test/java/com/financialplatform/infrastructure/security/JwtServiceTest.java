package com.financialplatform.infrastructure.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-jwt-secret-for-unit-tests";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setExpirationSeconds(3600);
        jwtService = new JwtService(properties);
    }

    @Test
    void shouldGenerateTokenWithRequiredClaims() {
        String token = jwtService.generateToken("operator", List.of("OPERATOR"));

        JwtClaims claims = jwtService.validateToken(token);

        assertThat(claims.subject()).isEqualTo("operator");
        assertThat(claims.roles()).containsExactly("OPERATOR");
        assertThat(claims.issuedAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(claims.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void shouldValidateTokenAndReturnClaims() {
        String token = jwtService.generateToken("admin", List.of("ADMIN", "OPERATOR"));

        JwtClaims claims = jwtService.validateToken(token);

        assertThat(claims.subject()).isEqualTo("admin");
        assertThat(claims.roles()).containsExactly("ADMIN", "OPERATOR");
    }

    @Test
    void shouldRejectExpiredToken() {
        Instant expiredAt = Instant.now().minusSeconds(60);
        String expiredToken = Jwts.builder()
                .subject("operator")
                .claim("roles", List.of("OPERATOR"))
                .issuedAt(Date.from(expiredAt.minusSeconds(3600)))
                .expiration(Date.from(expiredAt))
                .signWith(testSigningKey())
                .compact();

        assertThatThrownBy(() -> jwtService.validateToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    private SecretKey testSigningKey() {
        try {
            byte[] secretBytes = SECRET.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = secretBytes.length >= 32
                    ? secretBytes
                    : MessageDigest.getInstance("SHA-256").digest(secretBytes);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
