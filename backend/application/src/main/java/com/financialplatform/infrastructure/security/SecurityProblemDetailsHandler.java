package com.financialplatform.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Component
public class SecurityProblemDetailsHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    static final String PROBLEMS_BASE_URI = "https://api.financial-platform.lab/problems/";

    private final ObjectMapper objectMapper;

    public SecurityProblemDetailsHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        UnauthorizedProblem unauthorizedProblem = resolveUnauthorizedProblem(authException);
        writeProblemDetail(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                unauthorizedProblem.typeSlug(),
                unauthorizedProblem.title(),
                unauthorizedProblem.detail());
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        String detail = accessDeniedException.getMessage() != null
                ? accessDeniedException.getMessage()
                : "You do not have permission to access this resource";
        writeProblemDetail(request, response, HttpStatus.FORBIDDEN, "access-denied", "Access denied", detail);
    }

    private UnauthorizedProblem resolveUnauthorizedProblem(AuthenticationException authException) {
        if (authException instanceof BadCredentialsException badCredentials
                && badCredentials.getCause() instanceof ExpiredJwtException expiredJwt) {
            return new UnauthorizedProblem(
                    "token-expired",
                    "Token expired",
                    defaultDetail(expiredJwt, "Access token has expired"));
        }
        if (authException instanceof BadCredentialsException) {
            return new UnauthorizedProblem(
                    "invalid-credentials",
                    "Invalid credentials",
                    defaultDetail(authException, "Invalid credentials"));
        }
        Throwable cause = authException.getCause();
        if (cause instanceof ExpiredJwtException expiredJwt) {
            return new UnauthorizedProblem(
                    "token-expired",
                    "Token expired",
                    defaultDetail(expiredJwt, "Access token has expired"));
        }
        if (cause instanceof JwtException jwtException) {
            return new UnauthorizedProblem(
                    "invalid-token",
                    "Invalid token",
                    defaultDetail(jwtException, "Access token is invalid"));
        }
        return new UnauthorizedProblem(
                "invalid-credentials",
                "Authentication required",
                defaultDetail(authException, "Authentication is required to access this resource"));
    }

    private static String defaultDetail(Throwable exception, String fallback) {
        return exception.getMessage() != null ? exception.getMessage() : fallback;
    }

    private void writeProblemDetail(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String typeSlug,
            String title,
            String detail) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(PROBLEMS_BASE_URI + typeSlug));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    private record UnauthorizedProblem(String typeSlug, String title, String detail) {
    }
}
