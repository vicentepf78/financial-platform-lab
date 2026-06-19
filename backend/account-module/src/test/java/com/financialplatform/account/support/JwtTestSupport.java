package com.financialplatform.account.support;

import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test helpers for obtaining JWT access tokens and attaching them to MockMvc requests.
 *
 * <p>Copied from {@code application} test sources — consumer modules cannot depend on
 * {@code application} test classes on their classpath.
 */
public final class JwtTestSupport {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String OPERATOR_USERNAME = "operator";
    private static final String OPERATOR_PASSWORD = "operator";
    private static final String TEST_SECRET = "integration-test-jwt-secret-key-32chars";

    private JwtTestSupport() {
    }

    public static String obtainOperatorToken(MockMvc mockMvc) throws Exception {
        MvcResult result = mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(OPERATOR_USERNAME, OPERATOR_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    public static RequestPostProcessor bearerToken(String token) {
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return request;
        };
    }

    public static String generateToken(String subject, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(signingKey())
                .compact();
    }

    private static SecretKey signingKey() {
        try {
            byte[] secretBytes = TEST_SECRET.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = secretBytes.length >= 32
                    ? secretBytes
                    : MessageDigest.getInstance("SHA-256").digest(secretBytes);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
