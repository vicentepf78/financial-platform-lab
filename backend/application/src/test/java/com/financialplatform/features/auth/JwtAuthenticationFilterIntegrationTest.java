package com.financialplatform.features.auth;

import com.financialplatform.FinancialPlatformApplication;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FinancialPlatformApplication.class)
@AutoConfigureMockMvc
@Testcontainers
class JwtAuthenticationFilterIntegrationTest {

    private static final String INTEGRATION_JWT_SECRET = "integration-test-jwt-secret-key-32chars";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("financial_platform")
            .withUsername("financial")
            .withPassword("financial");

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("security.jwt.enabled", () -> "true");
        registry.add("security.jwt.secret", () -> INTEGRATION_JWT_SECRET);
        registry.add("security.jwt.expiration-seconds", () -> "3600");
    }

    @Test
    void shouldReturn401WhenProtectedRouteCalledWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("https://api.financial-platform.lab/problems/invalid-token"))
                .andExpect(jsonPath("$.title").value("Authentication required"));
    }

    @Test
    void shouldAllowAccessToProtectedRouteWithValidBearerToken() throws Exception {
        String accessToken = obtainAccessToken();

        mockMvc.perform(get("/api/v1/customers").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void shouldAllowLoginWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void shouldAllowActuatorHealthWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldAllowWebhookPathWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenBearerTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/customers").header("Authorization", "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Invalid token"));
    }

    @Test
    void shouldReturn401WhenBearerTokenIsExpired() throws Exception {
        String expiredToken = Jwts.builder()
                .subject("operator")
                .claim("roles", List.of("OPERATOR"))
                .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .expiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(INTEGRATION_JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(get("/api/v1/customers").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("https://api.financial-platform.lab/problems/token-expired"))
                .andExpect(jsonPath("$.title").value("Token expired"));
    }

    @Test
    void shouldReturn401WhenAuthorizationUsesBasicScheme() throws Exception {
        mockMvc.perform(get("/api/v1/customers").header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("https://api.financial-platform.lab/problems/invalid-token"))
                .andExpect(jsonPath("$.title").value("Invalid token"));
    }

    private String obtainAccessToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }
}
