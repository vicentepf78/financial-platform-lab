package com.financialplatform.features.auth;

public record LoginResult(String accessToken, String tokenType, long expiresIn) {
}
