package com.financialplatform.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(String subject, List<String> roles) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(jwtProperties.getExpirationSeconds());

        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey())
                .compact();
    }

    public JwtClaims validateToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        return new JwtClaims(
                claims.getSubject(),
                roles != null ? List.copyOf(roles) : List.of(),
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant());
    }

    private SecretKey signingKey() {
        try {
            byte[] secretBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = secretBytes.length >= 32
                    ? secretBytes
                    : MessageDigest.getInstance("SHA-256").digest(secretBytes);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
