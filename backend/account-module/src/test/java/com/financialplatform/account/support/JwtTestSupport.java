package com.financialplatform.account.support;

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
 * <p>Copied from {@code application} test sources — consumer modules cannot depend on
 * {@code application} test classes on their classpath.
 */
public final class JwtTestSupport {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String OPERATOR_USERNAME = "admin";
    private static final String OPERATOR_PASSWORD = "admin";

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
}
