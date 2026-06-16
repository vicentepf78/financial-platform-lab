package com.financialplatform.features.auth;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.accessToken(), result.tokenType(), result.expiresIn());
    }
}
