package com.financialplatform.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityProblemDetailsHandlerTest {

    private SecurityProblemDetailsHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = Jackson2ObjectMapperBuilder.json().build();
        handler = new SecurityProblemDetailsHandler(objectMapper);
    }

    @Test
    void shouldReturnProblemDetailsForMissingAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/accounts");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(
                request, response, new InsufficientAuthenticationException("Full authentication is required"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/problem+json");

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("type").asText())
                .isEqualTo(SecurityProblemDetailsHandler.PROBLEMS_BASE_URI + "invalid-token");
        assertThat(body.get("title").asText()).isEqualTo("Authentication required");
        assertThat(body.get("status").asInt()).isEqualTo(401);
        assertThat(body.get("detail").asText()).isEqualTo("Full authentication is required");
        assertThat(body.get("instance").asText()).isEqualTo("/api/v1/accounts");
    }

    @Test
    void shouldReturnProblemDetailsForForbidden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/transfers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Insufficient privileges"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/problem+json");

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("type").asText())
                .isEqualTo(SecurityProblemDetailsHandler.PROBLEMS_BASE_URI + "access-denied");
        assertThat(body.get("title").asText()).isEqualTo("Access denied");
        assertThat(body.get("status").asInt()).isEqualTo(403);
        assertThat(body.get("detail").asText()).isEqualTo("Insufficient privileges");
        assertThat(body.get("instance").asText()).isEqualTo("/api/v1/transfers");
    }

    @Test
    void shouldMapExpiredJwtToTokenExpiredProblem() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/accounts");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ExpiredJwtException expiredJwt = new ExpiredJwtException(null, null, "JWT expired");

        handler.commence(request, response, new BadCredentialsException("JWT expired", expiredJwt));

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("type").asText())
                .isEqualTo(SecurityProblemDetailsHandler.PROBLEMS_BASE_URI + "token-expired");
        assertThat(body.get("title").asText()).isEqualTo("Token expired");
        assertThat(body.get("status").asInt()).isEqualTo(401);
    }
}
