package com.financialplatform.infrastructure.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final RequestMatcher PUBLIC_PATHS = new OrRequestMatcher(
            new AntPathRequestMatcher("/api/v1/auth/login", "POST"),
            new AntPathRequestMatcher("/api/v1/webhooks/mercadopago", "POST"),
            new AntPathRequestMatcher("/actuator/health", "GET"),
            new AntPathRequestMatcher("/actuator/health/**", "GET"));

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            JwtProperties jwtProperties,
            SecurityProblemDetailsHandler authenticationEntryPoint) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !jwtProperties.isEnabled() || PUBLIC_PATHS.matches(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = extractBearerToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtClaims claims = jwtService.validateToken(token);
            SecurityContextHolder.getContext().setAuthentication(toAuthentication(claims));
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request, response, new BadCredentialsException("Invalid token", ex));
        }
    }

    private static String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private static UsernamePasswordAuthenticationToken toAuthentication(JwtClaims claims) {
        List<SimpleGrantedAuthority> authorities = claims.roles().stream()
                .map(JwtAuthenticationFilter::toRoleAuthority)
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new UsernamePasswordAuthenticationToken(claims.subject(), null, authorities);
    }

    private static String toRoleAuthority(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}
