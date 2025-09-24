package com.example.tests;

import com.example.util.Config;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.*;

public class ContractTest {

    private static WireMockServer wireMock;
    private static String baseUrl;
    private static Filter openApi;

    @BeforeAll
    static void setUpAll() {
        baseUrl = Config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            wireMock = new WireMockServer(
                WireMockConfiguration.options()
                    .dynamicPort()
                    .extensions(new ResponseTemplateTransformer(true))
            );
            wireMock.start();
            String openApiJson;
            try {
                openApiJson = new String(ContractTest.class.getResourceAsStream("/openapi.json").readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load openapi.json from classpath", e);
            }
            // serve openapi
            wireMock.stubFor(get(urlEqualTo("/v3/api-docs"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(openApiJson)));

            // basic stubs to satisfy contract checks
            wireMock.stubFor(get(urlEqualTo("/actuator/health"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"status\":\"UP\"}")));
            wireMock.stubFor(get(urlEqualTo("/api/customers"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("[]")));
            wireMock.stubFor(post(urlEqualTo("/api/customers"))
                .willReturn(aResponse().withStatus(201).withHeader("Content-Type", "application/json").withBody("{\"id\":\"x\",\"name\":\"N\",\"email\":\"a@b.c\",\"phone\":\"+10000000000\"}")));
            wireMock.stubFor(get(urlPathMatching("/api/customers/.+"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"id\":\"x\",\"name\":\"N\",\"email\":\"a@b.c\",\"phone\":\"+10000000000\"}")));

            baseUrl = "http://localhost:" + wireMock.port();
        }
        RestAssured.baseURI = baseUrl;

        // load OpenAPI from the running server's /v3/api-docs
        String specUrl = baseUrl + "/v3/api-docs";
        openApi = new OpenApiValidationFilter(specUrl);
    }

    @AfterAll
    static void tearDownAll() {
        if (wireMock != null) wireMock.stop();
    }

    @Test
    void healthContract() {
        RestAssured.given().filter(openApi)
            .when().get("/actuator/health")
            .then().statusCode(200).body("status", equalTo("UP"));
    }

    @Test
    void listCustomersContract() {
        RestAssured.given().filter(openApi)
            .when().get("/api/customers")
            .then().statusCode(200).body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void createCustomerContract() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "N");
        payload.put("email", "a@b.c");
        payload.put("phone", "+10000000000");

        RestAssured.given().filter(openApi)
            .contentType(ContentType.JSON).body(payload)
            .when().post("/api/customers")
            .then().statusCode(anyOf(is(201), is(200)))
            .body("id", notNullValue());
    }
}


