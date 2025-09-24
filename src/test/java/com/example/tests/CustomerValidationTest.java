package com.example.tests;

import com.example.util.Config;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class CustomerValidationTest {

    private static WireMockServer wireMock;
    private static String baseUrl;

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
            stubErrorEndpoints(wireMock);
            baseUrl = "http://localhost:" + wireMock.port();
        }
        RestAssured.baseURI = baseUrl;
    }

    @AfterAll
    static void tearDownAll() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    @Test
    @DisplayName("400 - create customer missing name")
    void createMissingNameReturns400() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "bad@example");
        payload.put("phone", "+10000000000");

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/customers")
            .then()
            .statusCode(400)
            .body("message", containsStringIgnoringCase("name"))
            .body("timestamp", notNullValue())
            .body("details", notNullValue());
    }

    @Test
    @DisplayName("400 - create customer invalid email/phone")
    void createInvalidFormatsReturns400() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Bad Formats");
        payload.put("email", "not-an-email");
        payload.put("phone", "12345");

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/customers")
            .then()
            .statusCode(400)
            .body("message", containsStringIgnoringCase("invalid"))
            .body("timestamp", notNullValue())
            .body("details", notNullValue());
    }

    @Test
    @DisplayName("404 - get customer non-existent id")
    void getNonExistentReturns404() {
        RestAssured
            .given()
            .when()
            .get("/api/customers/{id}", "does-not-exist")
            .then()
            .statusCode(404)
            .body("message", containsStringIgnoringCase("not found"))
            .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("409 - create customer duplicate email")
    void createDuplicateReturns409() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Dup");
        payload.put("email", "duplicate@example.test");
        payload.put("phone", "+10000000000");

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/customers")
            .then()
            .statusCode(409)
            .body("message", containsStringIgnoringCase("exists"))
            .body("timestamp", notNullValue());
    }

    private static void stubErrorEndpoints(WireMockServer wm) {
        // 400 missing name
        wm.stubFor(post(urlEqualTo("/api/customers"))
            .withRequestBody(matchingJsonPath("$.name", absent()))
            .willReturn(jsonError(400, "Field 'name' is required", "validation")));

        // 400 invalid email/phone
        wm.stubFor(post(urlEqualTo("/api/customers"))
            .withRequestBody(matchingJsonPath("$.email", matching("^[^@]+$"))) // crude 'invalid' example
            .willReturn(jsonError(400, "Invalid email format", "validation")));
        wm.stubFor(post(urlEqualTo("/api/customers"))
            .withRequestBody(matchingJsonPath("$.phone", matching("^\\+?\\d{0,9}$"))) // crude invalid phone
            .willReturn(jsonError(400, "Invalid phone format", "validation")));

        // 404 not found
        wm.stubFor(get(urlPathMatching("/api/customers/does-not-exist"))
            .willReturn(jsonError(404, "Customer not found", "not-found")));

        // 409 duplicate
        wm.stubFor(post(urlEqualTo("/api/customers"))
            .withRequestBody(matchingJsonPath("$.email", equalTo("duplicate@example.test")))
            .willReturn(jsonError(409, "Customer with email exists", "conflict")));
    }

    private static ResponseDefinitionBuilder jsonError(int status, String message, String reason) {
        return aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json")
            .withBody("{\n  \"timestamp\": \"2025-01-01T00:00:00Z\",\n  \"message\": \"" + message + "\",\n  \"details\": \"" + reason + "\"\n}");
    }
}


