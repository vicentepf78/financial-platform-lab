package com.financialplatform.features.auth;

import com.financialplatform.infrastructure.security.InMemoryUserDetailsService;
import com.financialplatform.infrastructure.security.JwtClaims;
import com.financialplatform.infrastructure.security.JwtProperties;
import com.financialplatform.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginUseCaseTest {

    private static final String SECRET = "test-jwt-secret-for-unit-tests";
    private static final long EXPIRATION_SECONDS = 3600;

    private LoginUseCase useCase;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setExpirationSeconds(EXPIRATION_SECONDS);

        jwtService = new JwtService(jwtProperties);
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        useCase = new LoginUseCase(
                new InMemoryUserDetailsService(jwtProperties),
                passwordEncoder,
                jwtService,
                jwtProperties);
    }

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() {
        LoginResult result = useCase.execute(new LoginCommand("operator", "operator"));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresIn()).isEqualTo(EXPIRATION_SECONDS);

        JwtClaims claims = jwtService.validateToken(result.accessToken());
        assertThat(claims.subject()).isEqualTo("operator");
        assertThat(claims.roles()).containsExactly("OPERATOR");
    }

    @Test
    void shouldReturnTokenWithAllRolesWhenAdminLogsIn() {
        LoginResult result = useCase.execute(new LoginCommand("admin", "admin"));

        JwtClaims claims = jwtService.validateToken(result.accessToken());
        assertThat(claims.subject()).isEqualTo("admin");
        assertThat(claims.roles()).containsExactlyInAnyOrder("ADMIN", "OPERATOR");
    }

    @Test
    void shouldThrowInvalidCredentialsWhenPasswordIsWrong() {
        assertThatThrownBy(() -> useCase.execute(new LoginCommand("operator", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void shouldThrowInvalidCredentialsWhenUserIsUnknown() {
        assertThatThrownBy(() -> useCase.execute(new LoginCommand("unknown", "operator")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }
}
