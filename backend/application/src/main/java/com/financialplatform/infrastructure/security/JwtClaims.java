package com.financialplatform.infrastructure.security;

import java.time.Instant;
import java.util.List;

public record JwtClaims(String subject, List<String> roles, Instant issuedAt, Instant expiresAt) {
}
