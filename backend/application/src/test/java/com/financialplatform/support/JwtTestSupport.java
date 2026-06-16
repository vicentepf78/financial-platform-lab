package com.financialplatform.support;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test helpers for obtaining JWT access tokens and attaching them to MockMvc requests.
 *
 * <p>{@code obtainOperatorToken} calls {@code POST /api/v1/auth/login} with the seeded
 * operator credentials ({@code operator}/{@code operator}) and returns the {@code accessToken}
 * from the API envelope ({@code $.data.accessToken}).
 *
 * <p>{@code bearerToken} returns a {@link RequestPostProcessor} that sets
 * {@code Authorization: Bearer &lt;token&gt;} on the request.
 *
 * <h2>Consumer modules (T9–T12)</h2>
 * <p>This class lives in {@code application} test sources. Modules such as
 * {@code customer-module} and {@code account-module} do not depend on {@code application}
 * test classes on their classpath. For secured integration tests in those modules, either:
 * <ul>
 *   <li>copy this minimal helper into {@code src/test/java/.../support}, or</li>
 *   <li>add a shared test artifact dependency if the build introduces one.</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * String token = obtainOperatorToken(mockMvc);
 * mockMvc.perform(get("/api/v1/customers")
 *         .with(bearerToken(token)))
 *     .andExpect(status().isOk());
 * }</pre>
 */
public final class JwtTestSupport {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String OPERATOR_USERNAME = "operator";
    private static final String OPERATOR_PASSWORD = "operator";

    private JwtTestSupport() {
    }

    /**
     * Obtains a JWT access token for the seeded operator via the login endpoint.
     *
     * @param mockMvc configured MockMvc instance with auth endpoints available
     * @return the {@code accessToken} value from the login response
     */
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

    /**
     * Adds {@code Authorization: Bearer &lt;token&gt;} to a MockMvc request.
     *
     * @param token JWT access token (non-blank)
     * @return request post-processor for use with {@code MockHttpServletRequestBuilder#with}
     */
    public static RequestPostProcessor bearerToken(String token) {
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return request;
        };
    }
}
